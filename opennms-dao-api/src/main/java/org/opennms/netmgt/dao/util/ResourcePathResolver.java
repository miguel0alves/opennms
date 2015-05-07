package org.opennms.netmgt.dao.util;

import java.util.Set;

import org.opennms.netmgt.model.OnmsAttribute;

public interface ResourcePathResolver {

    public boolean exists(String type, String... pathComponents);

    public Set<OnmsAttribute> getAttributes(String type, String... pathComponents);

    public Set<String> findNodeSourceDirectories();

    public Set<String> findResponseTimeDirectories();

}
