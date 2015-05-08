package org.opennms.netmgt.dao.util;

import java.util.List;
import java.util.Set;

import org.opennms.netmgt.model.OnmsAttribute;

public interface ResourceResolver {

    public boolean exists(String type, String... pathComponents);

    public Set<String> list(String type, String... pathComponents);

    public Set<OnmsAttribute> getAttributes(String type, String... pathComponents);

    public Set<Integer> findSnmpNodeDirectories();

    public Set<String> findNodeSourceDirectories();

    public Set<String> findResponseTimeDirectories();

    public Set<String> findDistributedResponseTimeDirectories();

    public Set<String> findDomainDirectories();

}
