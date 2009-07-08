/*******************************************************************************
 * This file is part of the OpenNMS(R) Application.
 *
 * Copyright (C) 2006-2008 The OpenNMS Group, Inc.  All rights reserved.
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc.:
 *
 *      51 Franklin Street
 *      5th Floor
 *      Boston, MA 02110-1301
 *      USA
 *
 * For more information contact:
 *
 *      OpenNMS Licensing <license@opennms.org>
 *      http://www.opennms.org/
 *      http://www.opennms.com/
 *
 *******************************************************************************/

package org.opennms.netmgt.collectd;

import java.io.File;

import org.opennms.netmgt.config.StorageStrategy;
import org.opennms.netmgt.model.RrdRepository;
import org.opennms.netmgt.snmp.SnmpInstId;

public class GenericIndexResource extends SnmpCollectionResource {

    private SnmpInstId m_inst;
    private String m_name;
    private String m_resourceLabel;

    public GenericIndexResource(ResourceType def, String name, SnmpInstId inst) {
        super(def);
        m_name = name;
        m_inst = inst;
    }

    @Override
    public File getResourceDir(RrdRepository repository) {
        String resourcePath = getStrategy().getRelativePathForAttribute(getParent(), getLabel(), null);
        File resourceDir = new File(repository.getRrdBaseDir(), resourcePath);
        log().debug("getResourceDir: " + resourceDir);
        return resourceDir;
    }

    public String toString() {
        return "node["+getCollectionAgent().getNodeId() + "]." + getResourceTypeName() + "[" + getLabel() + "]";
    }


    @Override
    public int getType() {
        return -1;	// XXX is this right?
    }

    @Override
    public boolean shouldPersist(ServiceParameters params) {
        return true;// XXX should be based on the persistanceSelectorStrategy
    }

    public String getResourceTypeName() {
        return m_name;
    }
    
    public String getInstance() {
        return m_inst.toString();
    }

    private StorageStrategy getStrategy() {
        return ((GenericIndexResourceType)getResourceType()).getStorageStrategy();
    }

    private String getParent() {
        return String.valueOf(getCollectionAgent().getNodeId());
    }

    /*
     * Because call getResourceNameFromIndex could be expensive.
     * This class save the returned value from Strategy on a local variable.
     */
    public String getLabel() {
        if (m_resourceLabel == null) {
            m_resourceLabel = getStrategy().getResourceNameFromIndex(getParent(), getInstance());
        }
        return m_resourceLabel;
    }
}
