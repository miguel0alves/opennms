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

package org.opennms.netmgt.notifd;

import java.util.LinkedList;
import java.util.List;
import java.util.TreeMap;

public class DuplicateTreeMap<K, V> extends TreeMap<K, List<V>> {
    /**
     * 
     */
    private static final long serialVersionUID = 8020472612288161254L;

    public V putItem(K key, V value) {
        List<V> l;
        if (super.containsKey(key)) {
            l = super.get(key);
        } else {
            l = new LinkedList<V>();
            put(key, l);
        }
        
        if (l.contains(value)) {
            return value;
        } else {
            l.add(value);
            return null;
        }
    }

    @Override
    public String toString() {
        StringBuffer buffer = new StringBuffer();

        for (List<V> list : values()) {
            for (V item : list) {
                buffer.append(item.toString() + System.getProperty("line.separator"));
            }
        }

        return buffer.toString();
    }
}

