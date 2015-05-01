package org.opennms.netmgt.rrd.newts;

import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;

import org.opennms.netmgt.rrd.RrdDataSource;
import org.opennms.newts.api.Resource;

/**
 * Wrapper for holding the definition of an RRD database.
 *
 * Used to determine the names of the metrics from the RRA definitions.
 *
 * @author jwhite
 */
public class RrdDef {
    private final String m_path;

    private final List<RrdDs> m_dataSources;

    public RrdDef(String directory, String rrdName, List<RrdDataSource> dataSources) {
        m_path = Paths.get(directory, rrdName + NewtsRrdStrategy.FILE_EXTENSION).toString();

        // Map the RrdDataSources to our own representation
        m_dataSources = dataSources.stream()
            .map(ds -> new RrdDs(ds))
            .collect(Collectors.toList());
    }

    public String getPath() {
        return m_path;
    }

    public Resource getResource() {
        return NewtsUtils.getResourceFromPath(m_path);
    }

    public List<RrdDs> getDataSources() {
        return m_dataSources;
    }
}
