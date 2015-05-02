package org.opennms.netmgt.rrd.newts;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.stream.Collectors;

import org.opennms.core.logging.Logging;
import org.opennms.newts.api.Sample;
import org.opennms.newts.api.SampleProcessorService;
import org.opennms.newts.api.SampleRepository;
import org.opennms.newts.api.search.Indexer;
import org.opennms.newts.cassandra.CassandraSession;
import org.opennms.newts.cassandra.search.CassandraIndexer;
import org.opennms.newts.cassandra.search.GuavaResourceMetadataCache;
import org.opennms.newts.cassandra.search.ResourceMetadataCache;
import org.opennms.newts.persistence.cassandra.CassandraSampleRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.codahale.metrics.MetricRegistry;
import com.google.common.collect.Lists;

/**
 * The NewtsPersistor is responsible for persisting samples gathered
 * by the NewtsRrdStrategy.
 *
 * This is not ideal and we should find a way of using the sample-storage-newts features from
 * Minion project instead.
 *
 * @author jwhite
 */
public class NewtsPersistor implements Runnable {

    private static final Logger LOG = LoggerFactory.getLogger(NewtsPersistor.class);

    private static final boolean NO_DELAY = false;

    private static final long DELAY_IN_MS = 250;

    // Ideally this value would correspond to the number of unique resource this system
    // may process. However, this comes at the cost of storing a ResourceMetadata object
    // for every one of these.
    private static final long MAX_CACHE_ENTRIES = 4096;

    private static final int SAMPLE_PROCESSOR_MAX_THREADS = 4;

    private static final String CASSANDRA_COMPRESSION = "LZ4";

    private static final long DELAY_AFTER_FAILURE_IN_MS = 5 * 1000;

    private final String m_hostname;

    private final int m_port;

    private final String m_keyspace;

    private final int m_ttl;

    private final LinkedBlockingQueue<Collection<Sample>> m_queue;

    private final MetricRegistry m_registry = new MetricRegistry();

    private final ResourceMetadataCache m_cache = new GuavaResourceMetadataCache(MAX_CACHE_ENTRIES, m_registry);

    private CassandraSession m_session = null;

    private SampleRepository m_sampleRepository = null;

    private Indexer m_indexer = null;

    public NewtsPersistor(String hostname, int port, String keyspace, int ttl, LinkedBlockingQueue<Collection<Sample>> queue) {
        m_hostname = hostname;
        m_port = port;
        m_keyspace = keyspace;
        m_ttl = ttl;
        m_queue = queue;
    }

    @Override
    public void run() {
        // We'd expect the logs from this thread to be in collectd.log
        Logging.putPrefix("collectd");

        try {
            while(true) {
                final List<Collection<Sample>> samples = Lists.newLinkedList();
                final List<Sample> flattenedSamples = Lists.newLinkedList();

                // Block and wait for an element
                samples.add(m_queue.take());
                try {
                    if (!NO_DELAY) {
                        // We only have a single sample, if there are no other samples
                        // pending on the queue, then sleep for short delay before
                        // checking again and initiating the insert
                        if (m_queue.size() == 0) {
                            Thread.sleep(DELAY_IN_MS);
                        }
                    }

                    // Grab all of the remaining samples on the queue and flatten them
                    m_queue.drainTo(samples);
                    samples.stream().forEach(flattenedSamples::addAll);

                    LOG.debug("Inserting {} samples", flattenedSamples.size());
                    getSampleRepository().insert(flattenedSamples);

                    try {
                        getIndexer().update(flattenedSamples);
                    } catch (Throwable t) {
                        LOG.error("An error occured while indexing samples. {} samples will not be indexed.", flattenedSamples.size(), t);
                    }

                    if (LOG.isDebugEnabled()) {
                        String uniqueResourceIds = flattenedSamples.stream()
                            .map(s -> s.getResource().getId())
                            .distinct()
                            .collect(Collectors.joining(", "));
                        LOG.debug("Successfully inserted samples for resources with ids {}", uniqueResourceIds);
                    }
                } catch (Throwable t) {
                    LOG.error("Failed to insert the samples. Adding them back to the end of the queue.", t);

                    if (m_queue.offer(flattenedSamples)) {
                        LOG.debug("Succesfully restored the samples in the queue.");
                    } else {
                        LOG.error("Failed to restore the samples in the queue. Current size: {}", m_queue.size());
                    }

                    // Rest before trying again
                    Thread.sleep(DELAY_AFTER_FAILURE_IN_MS);
                }
            }
        } catch (InterruptedException e) {
            LOG.warn("Interrupted.", e);
        }
    }

    private synchronized CassandraSession getSession() {
        if (m_session == null) {
            m_session = new CassandraSession(m_keyspace, m_hostname, m_port, CASSANDRA_COMPRESSION);
        }
        return m_session;
    }

    private synchronized Indexer getIndexer() {
        if (m_indexer == null) {
            m_indexer = new CassandraIndexer(getSession(), m_ttl, m_cache, m_registry);
        }
        return m_indexer;
    }

    public synchronized SampleRepository getSampleRepository() {
        if (m_sampleRepository == null) {
            // Create an empty sample processor service
            SampleProcessorService processors = new SampleProcessorService(SAMPLE_PROCESSOR_MAX_THREADS, Collections.emptySet());

            // Sample repositories are used for reading/writing
            m_sampleRepository = new CassandraSampleRepository(getSession(), m_ttl, m_registry, processors);
        }
        return m_sampleRepository;
    }
}
