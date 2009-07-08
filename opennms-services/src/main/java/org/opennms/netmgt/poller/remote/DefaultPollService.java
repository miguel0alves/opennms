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

package org.opennms.netmgt.poller.remote;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.opennms.netmgt.model.PollStatus;
import org.opennms.netmgt.poller.ServiceMonitor;
import org.opennms.netmgt.poller.ServiceMonitorLocator;
import org.springframework.util.Assert;

/**
 * 
 * @author <a href="mailto:brozow@opennms.org">Mathew Brozowski</a>
 */
public class DefaultPollService implements PollService {
	
    Collection<ServiceMonitorLocator> m_locators;
    Map<String, ServiceMonitor> m_monitors;
	
    public void setServiceMonitorLocators(Collection<ServiceMonitorLocator> locators) {
        m_locators = locators;
        
        Map<String, ServiceMonitor> monitors = new HashMap<String, ServiceMonitor>();
        for (ServiceMonitorLocator locator : locators) {
            monitors.put(locator.getServiceName(), locator.getServiceMonitor());
        }
        
        m_monitors = monitors;
    }
    
    public void initialize(PolledService polledService) {
        ServiceMonitor monitor = getServiceMonitor(polledService);
        monitor.initialize(polledService);
    }

    public PollStatus poll(PolledService polledService) {
        ServiceMonitor monitor = getServiceMonitor(polledService);
        return monitor.poll(polledService, polledService.getMonitorConfiguration());
    }

    private ServiceMonitor getServiceMonitor(PolledService polledService) {
        Assert.notNull(m_monitors, "setServiceMonitorLocators must be called before any other operations");
        ServiceMonitor monitor = (ServiceMonitor)m_monitors.get(polledService.getSvcName());
        Assert.notNull(monitor, "Unable to find monitor for service "+polledService.getSvcName());
        return monitor;
    }

    // FIXME: this is never called but should be
    // also monitor.release() isn't called either
    public void release(PolledService polledService) {
        ServiceMonitor monitor = getServiceMonitor(polledService);
        monitor.release(polledService);
    }




}
