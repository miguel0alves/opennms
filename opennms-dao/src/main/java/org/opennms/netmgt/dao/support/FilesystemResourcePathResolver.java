package org.opennms.netmgt.dao.support;

import java.io.File;
import java.io.FileFilter;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.Set;

import org.opennms.netmgt.dao.util.ResourcePathResolver;
import org.opennms.netmgt.model.OnmsAttribute;
import org.opennms.netmgt.model.ResourceTypeUtils;
import org.opennms.netmgt.rrd.RrdFileConstants;

public class FilesystemResourcePathResolver implements ResourcePathResolver {

    private File m_rrdDirectory;

    @Override
    public boolean exists(String type, String... pathComponents) {
        return toFile(type, pathComponents).isDirectory();
    }

    @Override
    public Set<OnmsAttribute> getAttributes(String type, String... pathComponents) {
        return ResourceTypeUtils.getAttributesAtRelativePath(m_rrdDirectory, toRelativePath(type, pathComponents));
    }

    private File toFile(String type, String... pathComponents) {
        final File root = new File(m_rrdDirectory, type);
        return Paths.get(root.getAbsolutePath(), pathComponents).toFile();
    }

    private String toRelativePath(String type, String... pathComponents) {
        StringBuilder sb = new StringBuilder();
        sb.append(type);
        for (final String pathComponent : pathComponents) {
            sb.append(File.separator);
            sb.append(pathComponent);
        }
        return sb.toString();
    }

    public void setRrdDirectory(File rrdDirectory) {
        m_rrdDirectory = rrdDirectory;
    }

    public File getRrdDirectory() {
        return m_rrdDirectory;
    }

    @Override
    public Set<String> findNodeSourceDirectories() {
       Set<String> nodeSourceDirectories = new HashSet<String>();
       File snmpDir = new File(getRrdDirectory(), ResourceTypeUtils.SNMP_DIRECTORY);
       File forSrcDir = new File(snmpDir, ResourceTypeUtils.FOREIGN_SOURCE_DIRECTORY);
       File[] sourceDirs = forSrcDir.listFiles(); // TODO There is no need to filter by RrdFileConstants.SOURCE_DIRECTORY_FILTER
       if (sourceDirs != null && sourceDirs.length > 0) {
           for (File sourceDir : sourceDirs) {
               File [] ids = sourceDir.listFiles(RrdFileConstants.NODESOURCE_DIRECTORY_FILTER);
               for (File id : ids) {
                   nodeSourceDirectories.add(sourceDir.getName() + ":" + id.getName());
               }
           }
       }
       
       return nodeSourceDirectories;
    }

    @Override
    public Set<String> findResponseTimeDirectories() {
        return findChildrenMatchingFilter(new File(getRrdDirectory(), ResourceTypeUtils.RESPONSE_DIRECTORY), RrdFileConstants.INTERFACE_DIRECTORY_FILTER);
    }

    private static Set<String> findChildrenMatchingFilter(File directory, FileFilter filter) {
        Set<String> children = new HashSet<String>();
        
        File[] nodeDirs = directory.listFiles(filter);

        if (nodeDirs == null || nodeDirs.length == 0) {
            return children;
        }

        for (File nodeDir : nodeDirs) {
            children.add(nodeDir.getName());
        }
        
        return children;
    }
}
