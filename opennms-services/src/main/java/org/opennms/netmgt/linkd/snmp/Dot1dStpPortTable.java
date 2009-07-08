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

package org.opennms.netmgt.linkd.snmp;

import java.net.InetAddress;

import org.apache.log4j.Category;
import org.opennms.core.utils.ThreadCategory;
import org.opennms.netmgt.capsd.snmp.SnmpTable;
import org.opennms.netmgt.snmp.SnmpInstId;
import org.opennms.netmgt.snmp.SnmpObjId;

/**
 * <P>Dot1dStpPortTable uses a SnmpSession to collect the dot1dBridge.dot1dStp 
 * tableentries.
 * It implements the SnmpHandler to receive notifications when a reply is 
 * received/error occurs in the SnmpSession used to send requests /recieve 
 * replies.</P>
 *
 * @author <A HREF="mailto:rssntn67@yahoo.it">Antonio Russo</A>
 * @author <A HREF="mailto:jamesz@opennms.org">James Zuo</A>
 * @author <A HREF="mailto:sowmya@opennms.org">Sowmya</A>
 * @author <A HREF="mailto:weave@oculan.com">Weave</A>
 * @author <A HREF="http://www.opennms.org/">OpenNMS</A>
 *
 * @see <A HREF="http://www.ietf.org/rfc/rfc1213.txt">RFC1213</A>
 */
public class Dot1dStpPortTable extends SnmpTable<Dot1dStpPortTableEntry> {

	public Dot1dStpPortTable(InetAddress address) {
        super(address, "dot1dStpPortTable", Dot1dStpPortTableEntry.stpport_elemList);
    }
    
    protected Dot1dStpPortTableEntry createTableEntry(SnmpObjId base, SnmpInstId inst, Object val) {
        return new Dot1dStpPortTableEntry();
    }


    protected final Category log() {
        return ThreadCategory.getInstance(Dot1dStpPortTable.class);
    }
	
}
				



