/*******************************************************************************
 * This file is part of the OpenNMS(R) Application.
 *
 * Copyright (C) 2005-2009 The OpenNMS Group, Inc.  All rights reserved.
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


package org.opennms.netmgt.capsd.plugins;

import java.net.InetAddress;
import java.util.Map;

import org.opennms.core.utils.IPLike;
import org.opennms.core.utils.ParameterMap;
import org.opennms.netmgt.capsd.Plugin;
/**
 * @author david
 *
 */
public class LoopPlugin implements Plugin {

    private final String m_protocolName = "LOOP";

    /* (non-Javadoc)
     * @see org.opennms.netmgt.capsd.Plugin#getProtocolName()
     */
    public String getProtocolName() {
        return m_protocolName;
    }

    /* (non-Javadoc)
     * @see org.opennms.netmgt.capsd.Plugin#isProtocolSupported(java.net.InetAddress)
     */
    public boolean isProtocolSupported(InetAddress address) {
        return isProtocolSupported(address, null);
    }

    /* (non-Javadoc)
     * @see org.opennms.netmgt.capsd.Plugin#isProtocolSupported(java.net.InetAddress, java.util.Map)
     */
    public boolean isProtocolSupported(InetAddress address, Map<String, Object> qualifiers) {
        
        if (qualifiers == null) {
            return false;
        }
        
        String ipMatch = getIpMatch(qualifiers);
        if (IPLike.matches(address.getHostAddress(), ipMatch)) {
            return isSupported(qualifiers);
        } else {
            return false;
        }
        
    }

    private boolean isSupported(Map<String, Object> parameters) {
        return ParameterMap.getKeyedString(parameters, "is-supported", "false").equalsIgnoreCase("true");
    }

    private String getIpMatch(Map<String, Object> parameters) {
        return ParameterMap.getKeyedString(parameters, "ip-match", "*.*.*.*");
    }

}
