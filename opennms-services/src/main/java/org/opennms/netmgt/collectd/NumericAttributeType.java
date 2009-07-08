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


import org.opennms.netmgt.config.MibObject;

public class NumericAttributeType extends SnmpAttributeType {
    
    private static String[] s_supportedTypes = new String[] { "counter", "gauge", "timeticks", "integer", "octetstring" };
    
    public static boolean supportsType(String rawType) {
        String type = rawType.toLowerCase();
        for (int i = 0; i < s_supportedTypes.length; i++) {
            String supportedType = s_supportedTypes[i];
            if (type.startsWith(supportedType))
                return true;
        }
        return false;
    }



    static final String DST_COUNTER = "COUNTER";
    public NumericAttributeType(ResourceType resourceType, String collectionName, MibObject mibObj, AttributeGroupType groupType) {
        super(resourceType, collectionName, mibObj, groupType);
        
            // Assign the data source object identifier and instance
            if (log().isDebugEnabled()) {
                log().debug(
                        "buildDataSourceList: ds_name: "+ getName()
                        + " ds_oid: " + getOid()
                        + "." + getInstance());
            }
            
            String alias = getAlias();
            if (alias.length() > PersistOperationBuilder.MAX_DS_NAME_LENGTH) {
                logNameTooLong();
            }


    }
    
    public void storeAttribute(CollectionAttribute attribute, Persister persister) {
        persister.persistNumericAttribute(attribute);
    }

    void logNameTooLong() {
        log().warn(
                "buildDataSourceList: Mib object name/alias '"
                + getAlias()
                + "' exceeds 19 char maximum for RRD data source names, truncating.");
   }


}
