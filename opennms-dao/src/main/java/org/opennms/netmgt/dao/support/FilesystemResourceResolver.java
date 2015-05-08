package org.opennms.netmgt.dao.support;

import java.io.File;
import java.io.FileFilter;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

import org.opennms.netmgt.dao.util.ResourceResolver;
import org.opennms.netmgt.model.OnmsAttribute;
import org.opennms.netmgt.model.ResourceTypeUtils;
import org.opennms.netmgt.rrd.RrdFileConstants;

import com.google.common.collect.Sets;

public class FilesystemResourceResolver implements ResourceResolver {

    private File m_rrdDirectory;

    @Override
    public boolean exists(String type, String... pathComponents) {
        return toFile(type, pathComponents).isDirectory();
    }

    @Override
    public Set<String> list(String type, String... pathComponents) {
        return Sets.newTreeSet(Arrays.asList(toFile(type, pathComponents).list()));
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
    public Set<Integer> findSnmpNodeDirectories() {
        Set<Integer> nodes = new TreeSet<Integer>();
        
        File directory = new File(getRrdDirectory(), ResourceTypeUtils.SNMP_DIRECTORY);
        File[] nodeDirs = directory.listFiles(RrdFileConstants.NODE_DIRECTORY_FILTER);

        if (nodeDirs == null || nodeDirs.length == 0) {
            return nodes;
        }

        for (File nodeDir : nodeDirs) {
            try {
                Integer nodeId = Integer.valueOf(nodeDir.getName());
                nodes.add(nodeId);
            } catch (NumberFormatException e) {
                // skip... don't add
            }
        }
        
        return nodes;
    }

    @Override
    public Set<String> findResponseTimeDirectories() {
        return findChildrenMatchingFilter(new File(getRrdDirectory(), ResourceTypeUtils.RESPONSE_DIRECTORY), RrdFileConstants.INTERFACE_DIRECTORY_FILTER);
    }

    @Override
    public Set<String> findDistributedResponseTimeDirectories() {
        return findChildrenChildrenMatchingFilter(new File(new File(getRrdDirectory(), ResourceTypeUtils.RESPONSE_DIRECTORY), "distributed"), RrdFileConstants.INTERFACE_DIRECTORY_FILTER);
    }

    @Override
    public Set<String> findDomainDirectories() {
        File snmp = new File(getRrdDirectory(), ResourceTypeUtils.SNMP_DIRECTORY);

        // Get all of the non-numeric directory names in the RRD directory; these
        // are the names of the domains that have performance data
        File[] domainDirs = snmp.listFiles(RrdFileConstants.DOMAIN_DIRECTORY_FILTER);

        return Arrays.asList(domainDirs).stream()
            .map(dir -> dir.getName())
            .collect(Collectors.toSet());
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

    private static Set<String> findChildrenChildrenMatchingFilter(File directory, FileFilter filter) {
        Set<String> children = new HashSet<String>();
        
        File[] locationMonitorDirs = directory.listFiles();
        if (locationMonitorDirs == null) {
            return children;
        }
        
        for (File locationMonitorDir : locationMonitorDirs) {
            File[] intfDirs = locationMonitorDir.listFiles(filter);

            if (intfDirs == null || intfDirs.length == 0) {
                continue;
            }

            for (File intfDir : intfDirs) {
                children.add(intfDir.getName());
            }
        }
        
        return children;
    }
}
