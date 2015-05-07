package org.opennms.netmgt.dao.support;

import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.Set;

import org.opennms.netmgt.dao.util.ResourcePathResolver;
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
public class NewtsResourcePathResolver implements ResourcePathResolver {

    private static final Logger LOG = LoggerFactory.getLogger(NewtsResourcePathResolver.class);

    private CassandraSession m_session = null;

    private CassandraSearcher m_searcher = null;

    private MetricRegistry m_registry = new MetricRegistry();

    private static final Pattern NODE_SOURCE_PATTERN = Pattern.compile("^" + ResourceTypeUtils.SNMP_DIRECTORY + ":fs:(.*?):(.*?):.*$");

    private static final Pattern RESPONSE_TIME_PATTERN = Pattern.compile("^" + ResourceTypeUtils.RESPONSE_DIRECTORY + ":(.*?):.*$");

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
    public boolean exists(String type, String... pathComponents) {
        return getResourceDetailsFor(type, pathComponents) != null;
    }

    @Override
    public Set<OnmsAttribute> getAttributes(String type, String... pathComponents) {
        Set<OnmsAttribute> attributes =  Sets.newHashSet();

        Result result = getResourceDetailsFor(type, pathComponents);
        if (result == null) {
            return attributes;
        }

        for (String metric : result.getMetrics()) {
            // Use the metric name as the dsName - keep everything else empty
            attributes.add(new RrdGraphAttribute(metric, "", ""));
        }

        Map<String, String> resourceAttributes = result.getResource().getAttributes().orNull();
        if (resourceAttributes != null) {
            for (Entry<String, String> entry : resourceAttributes.entrySet()) {
                attributes.add(new StringPropertyAttribute(entry.getKey(), entry.getValue()));
            }
        }

        return attributes;
    }

    private Result getResourceDetailsFor(String type, String... pathComponents) {
        BooleanQuery q = new BooleanQuery();
        q.add(new TermQuery(new Term(type)), Operator.OR);
        for (final String pathComponent : pathComponents) {
            q.add(new TermQuery(new Term(pathComponent)), Operator.AND);
        }

        LOG.debug("Searching for '{}'.", q);
        SearchResults results = getSearcher().search(q);
        LOG.debug("Found {} results.", results.size());
        for (final Result result : results) {
            if(result.getResource().getId().startsWith(toResourceId(type, pathComponents))) {
                LOG.debug("Found match with {}.", result.getResource().getId());
                return result;
            }
        }

        return null;
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
