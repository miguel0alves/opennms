/*******************************************************************************
 * This file is part of the OpenNMS(R) Application.
 *
 * Copyright (C) 2008 The OpenNMS Group, Inc.  All rights reserved.
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

package org.opennms.netmgt.tl1d;

import java.util.Date;

/**
 * Abstraction for generic TL1 Messages.  Must generic methods are used to populate
 * OpenNMS Event fields.
 * 
 * @author <a href="mailto:david@opennms.org">David Hustace</a>
 */
public abstract class Tl1Message {
    
    public static final int INPUT = 1;
    public static final int OUTPUT = 2;
    public static final int ACKNOWLEDGEMENT = 3;
    public static final int AUTONOMOUS = 4;

    private Date m_timestamp;
    private String m_rawMessage;
    private String m_host;
    
    public Date getTimestamp() {
        return m_timestamp;
    }

    public void setTimestamp(Date timestamp) {
        m_timestamp = timestamp;
    }

    public String getRawMessage() {
        return m_rawMessage;
    }

    public void setRawMessage(String rawMessage) {
        this.m_rawMessage = rawMessage;
    }

    public String getHost() {
        return m_host;
    }

    public void setHost(String host) {
        m_host = host;
    }
    
    public String toString() {
        return "Message from: "+m_host+"\n"+m_rawMessage;
    }
        
}