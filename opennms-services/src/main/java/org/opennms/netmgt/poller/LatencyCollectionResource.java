/*******************************************************************************
 * This file is part of the OpenNMS(R) Application.
 *
 * Copyright (C) 2009 The OpenNMS Group, Inc.  All rights reserved.
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


package org.opennms.netmgt.poller;

import java.io.File;

import org.opennms.netmgt.collectd.CollectionResource;
import org.opennms.netmgt.collectd.CollectionSetVisitor;
import org.opennms.netmgt.collectd.ServiceParameters;
import org.opennms.netmgt.model.RrdRepository;

/**
 * @author <a href="mailto:agalue@opennms.org">Alejandro Galue</a>
 *
 */
public class LatencyCollectionResource implements CollectionResource {
    
    private String m_serviceName;
    private String m_ipAddress;

    public LatencyCollectionResource(String serviceName, String ipAddress) {
        super();
        m_serviceName = serviceName;
        m_ipAddress = ipAddress;
    }

    public String getInstance() {
        return m_ipAddress + "[" + m_serviceName + "]";
    }

    public String getLabel() {
        return m_serviceName;
    }

    public String getResourceTypeName() {
        return "if";
    }

    public int getType() {
        return 0;
    }

    public boolean rescanNeeded() {
        return false;
    }

    public boolean shouldPersist(ServiceParameters params) {
        return true;
    }

    public void visit(CollectionSetVisitor visitor) {
    }

    public String getOwnerName() {
        return m_ipAddress;
    }

    public File getResourceDir(RrdRepository repository) {
        return new File(repository.getRrdBaseDir(), m_ipAddress);
    }

    @Override
    public String toString() {
        return m_serviceName + "@" + m_ipAddress;
    }

}
