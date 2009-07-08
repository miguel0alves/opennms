/*******************************************************************************
 * This file is part of the OpenNMS(R) Application.
 *
 * Copyright (C) 2002-2004, 2006, 2008 The OpenNMS Group, Inc.  All rights reserved.
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


package org.opennms.netmgt.eventd.db;

import org.opennms.netmgt.xml.event.Autoaction;

/**
 * This is an utility class used to format the event autoaction info - to be
 * inserted into the 'events' table
 * 
 * @author <A HREF="mailto:sowmya@opennms.org">Sowmya Nataraj </A>
 * @author <A HREF="http://www.opennms.org/">OpenNMS </A>
 */
public class AutoAction {
    /**
     * Format each autoaction entry
     * 
     * @param autoact
     *            the entry
     * 
     * @return the formatted string
     */
    public static String format(Autoaction autoact) {
        String text = autoact.getContent();
        String state = autoact.getState();

        return Constants.escape(text, Constants.DB_ATTRIB_DELIM) + Constants.DB_ATTRIB_DELIM + state;

    }

    /**
     * Format the list of autoaction entries of the event
     * 
     * @param autoacts
     *            the list
     * @param sz
     *            the size to which the formatted string is to be limited
     *            to(usually the size of the column in the database)
     * 
     * @return the formatted string
     */
    public static String format(Autoaction[] autoacts, int sz) {
        StringBuffer buf = new StringBuffer();
        boolean first = true;

        for (int index = 0; index < autoacts.length; index++) {
            if (!first)
                buf.append(Constants.MULTIPLE_VAL_DELIM);
            else
                first = false;

            buf.append(Constants.escape(format(autoacts[index]), Constants.MULTIPLE_VAL_DELIM));
        }

        if (buf.length() >= sz) {
            buf.setLength(sz - 4);
            buf.append(Constants.VALUE_TRUNCATE_INDICATOR);
        }

        return buf.toString();
    }
}
