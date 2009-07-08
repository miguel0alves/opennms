/*******************************************************************************
 * This file is part of the OpenNMS(R) Application.
 *
 * Copyright (C) 2006-2009 The OpenNMS Group, Inc.  All rights reserved.
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

/**
 * 
 */
package org.opennms.netmgt.config;

import java.io.Serializable;
import java.util.Map;

import org.opennms.netmgt.dao.CastorObjectRetrievalFailureException;
import org.opennms.netmgt.poller.ServiceMonitor;
import org.opennms.netmgt.poller.ServiceMonitorLocator;

public class DefaultServiceMonitorLocator implements ServiceMonitorLocator, Serializable {
    
    private static final long serialVersionUID = 1L;

    String m_serviceName;
    Class<? extends ServiceMonitor> m_serviceClass;
    
    public DefaultServiceMonitorLocator(String serviceName, Class<? extends ServiceMonitor> serviceClass) {
        m_serviceName = serviceName;
        m_serviceClass = serviceClass;
    }

    /*
     * FIXME The use of CastorObjectRetrievalFailureException doesn't seem
     * appropriate below, as I don't see Castor being used at all. - dj@opennms.org
     */
    @SuppressWarnings("unchecked")
    public ServiceMonitor getServiceMonitor() {
        try {
            ServiceMonitor mon = m_serviceClass.newInstance();
            mon.initialize((Map)null);
            return mon;
        } catch (InstantiationException e) {
            throw new CastorObjectRetrievalFailureException("Unable to instantiate monitor for service "
                    +m_serviceName+" with class-name "+m_serviceClass.getName(), e);
        } catch (IllegalAccessException e) {
            throw new CastorObjectRetrievalFailureException("Illegal access trying to instantiate monitor for service "
                    +m_serviceName+" with class-name "+m_serviceClass.getName(), e);
        }
    }

    public String getServiceName() {
        return m_serviceName;
    }

    public String getServiceLocatorKey() {
        return m_serviceClass.getName();
    }
    
}