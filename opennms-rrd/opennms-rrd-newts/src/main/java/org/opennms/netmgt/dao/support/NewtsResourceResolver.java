package org.opennms.netmgt.dao.support;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.Set;

import org.opennms.netmgt.dao.util.ResourceResolver;
import org.opennms.netmgt.model.OnmsAttribute;
import org.opennms.netmgt.model.ResourceTypeUtils;
import org.opennms.netmgt.model.RrdGraphAttribute;
import org.opennms.netmgt.model.StringPropertyAttribute;
import org.opennms.netmgt.rrd.newts.NewtsUtils;
import org.opennms.newts.api.search.BooleanQuery;
import org.opennms.newts.api.search.Operator;
import org.opennms.newts.api.search.SearchResults;
import org.opennms.newts.api.search.SearchResults.Result;
import org.opennms.newts.api.search.Searcher;
import org.opennms.newts.api.search.Term;
import org.opennms.newts.api.search.TermQuery;
import org.opennms.newts.cassandra.CassandraSession;
import org.opennms.newts.cassandra.search.CassandraSearcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.codahale.metrics.MetricRegistry;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

/**
 *
 * Notes:
 *   * Make the resolver cache aware i.e.:
 *     For exists() calls, we can try searching the cache first, if there's
 *     a positive hit, we don't need to query Cassandra
 * 
 * @author jwhite
 */
public class NewtsResourceResolver implements ResourceResolver {

    private static final Logger LOG = LoggerFactory.getLogger(NewtsResourceResolver.class);

    private CassandraSession m_session = null;

    private CassandraSearcher m_searcher = null;

    private MetricRegistry m_registry = new MetricRegistry();

    private static final Pattern NODE_SOURCE_PATTERN = Pattern.compile("^" + ResourceTypeUtils.SNMP_DIRECTORY + ":fs:(.*?):(.*?):.*$");

    private static final Pattern RESPONSE_TIME_PATTERN = Pattern.compile("^" + ResourceTypeUtils.RESPONSE_DIRECTORY + ":(.*?):.*$");

    @Override
    public boolean exists(String type, String... pathComponents) {
        return getResourceDetailsFor(type, pathComponents) != null;
    }

    @Override
    public Set<String> list(String type, String... pathComponents) {
        
        Set<String> matches = Sets.newTreeSet();
        
        List<Result> results = searchFor(type, pathComponents);

        final String prefix = toResourceId(type, pathComponents);
        
        for (Result res : results) {
            // We know all of the resource ids match
            String sub = res.getResource().getId().substring(prefix.length() + 1);
            int idx = sub.indexOf(':');
            if (idx >= 0) {
                sub = sub.substring(0, idx);
            }
            matches.add(sub);
        }

        return matches;
    }

    @Override
    public Set<OnmsAttribute> getAttributes(String type, String... pathComponents) {
        Set<OnmsAttribute> attributes =  Sets.newHashSet();
        
        List<Result> results = searchFor(type, pathComponents);
        
        final String prefix = toResourceId(type, pathComponents);
        
        for (Result result : results) {
            final String resourceId = result.getResource().getId();
            
            // We know all of the resource ids match
            String sub = resourceId.substring(prefix.length() + 1);
            int idx = sub.indexOf(':');
            if (idx >= 0) {
                continue;
            }

            for (String metric : result.getMetrics()) {
                // Use the metric name as the dsName
                // Store the resource id in the rrdFile field
                List<String> path = Lists.newArrayList(pathComponents);
                path.add(metric);
                attributes.add(new RrdGraphAttribute(metric, "", resourceId));
            }

            Map<String, String> resourceAttributes = result.getResource().getAttributes().orNull();
            if (resourceAttributes != null) {
                for (Entry<String, String> entry : resourceAttributes.entrySet()) {
                    attributes.add(new StringPropertyAttribute(entry.getKey(), entry.getValue()));
                }
            }
        }

        return attributes;
    }

    @Override
    public Set<String> findResponseTimeDirectories() {
        Set<String> responseTimeDirectories = Sets.newHashSet();

        BooleanQuery q = new BooleanQuery();
        q.add(new TermQuery(new Term(ResourceTypeUtils.RESPONSE_DIRECTORY)), Operator.OR);

        SearchResults results = getSearcher().search(q);
        for (final Result result : results) {
            Matcher m = RESPONSE_TIME_PATTERN.matcher(result.getResource().getId());
            if (!m.matches()) {
                continue;
            }

            responseTimeDirectories.add(m.group(1));
        }

        return responseTimeDirectories;
    }

    @Override
    public Set<String> findNodeSourceDirectories() {
        Set<String> nodeSourceDirectories = Sets.newHashSet();

        BooleanQuery q = new BooleanQuery();
        q.add(new TermQuery(new Term(ResourceTypeUtils.SNMP_DIRECTORY)), Operator.OR);

        SearchResults results = getSearcher().search(q);
        for (final Result result : results) {
            Matcher m = NODE_SOURCE_PATTERN.matcher(result.getResource().getId());
            if (!m.matches()) {
                continue;
            }

            nodeSourceDirectories.add(String.format("%s:%s", m.group(1), m.group(2)));
        }

        return nodeSourceDirectories;
    }

    @Override
    public Set<Integer> findSnmpNodeDirectories() {
        return Collections.emptySet();
    }

    @Override
    public Set<String> findDistributedResponseTimeDirectories() {
        return Collections.emptySet();
    }

    @Override
    public Set<String> findDomainDirectories() {
        return Collections.emptySet();
    }

    private Result getResourceDetailsFor(String type, String... pathComponents) {
        return Iterables.getFirst(searchFor(type, pathComponents), null);
    }

    private List<Result> searchFor(String type, String... pathComponents) {
        BooleanQuery q = new BooleanQuery();
        q.add(new TermQuery(new Term(type)), Operator.OR);
        for (final String pathComponent : pathComponents) {
            q.add(new TermQuery(new Term(pathComponent)), Operator.AND);
        }

        List<Result> matchingResults = Lists.newArrayList();
        
        LOG.debug("Searching for '{}'.", q);
        SearchResults results = getSearcher().search(q);
        LOG.debug("Found {} results.", results.size());
        for (final Result result : results) {
            if(result.getResource().getId().startsWith(toResourceId(type, pathComponents))) {
                LOG.debug("Found match with {}.", result.getResource().getId());
                matchingResults.add(result);
            }
        }

        return matchingResults;
    }

    private String toResourceId(String type, String... pathComponents) {
        StringBuilder sb = new StringBuilder();
        sb.append(type);
        for (final String pathComponent : pathComponents) {
            sb.append(":");
            sb.append(pathComponent);
        }
        return sb.toString();
    }

    private synchronized Searcher getSearcher() {
        if (m_searcher == null) {
            if (m_session == null) {
                m_session = NewtsUtils.getCassrandraSession();
            }

            m_searcher = new CassandraSearcher(m_session, m_registry);
        }

        return m_searcher;
    }

    /**
     * Used for testing
     */
    protected void setSearcher(CassandraSearcher searcher) {
        m_searcher = searcher;
    }
    
}
