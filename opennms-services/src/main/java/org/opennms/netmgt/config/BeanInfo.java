/*******************************************************************************
 * This file is part of the OpenNMS(R) Application.
 *
 * Copyright (C) 2005-2006, 2008-2009 The OpenNMS Group, Inc.  All rights reserved.
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


package org.opennms.netmgt.config;

import java.util.*;

/*
 * 
 * @author <A HREF="mailto:mike@opennms.org">Mike Jamison </A>
 * @author <A HREF="http://www.opennms.org/">OpenNMS </A>
 */
public class BeanInfo {
    private String mbeanName;

    private String objectName;

    private String keyField;

    private String excludes;
    
    private String keyAlias;

    private String[] attributes;

    private ArrayList<Object> operations;

    public BeanInfo() {
        operations = new ArrayList<Object>();
    }

    public void setAttributes(String[] attr) {
        attributes = attr;
    }

    public String[] getAttributeNames() {
        return attributes;
    }

    public void addOperations(Object attr) {
        operations.add(attr);
    }

    public ArrayList<Object> getOperations() {
        return operations;
    }

    /**
     * @return Returns the mbeanName.
     */
    public String getMbeanName() {
        return mbeanName;
    }

    /**
     * @param mbeanName
     *            The mbeanName to set.
     */
    public void setMbeanName(String mbeanName) {
        this.mbeanName = mbeanName;
    }

    /**
     * @return Returns the objectName.
     */
    public String getObjectName() {
        return objectName;
    }

    /**
     * @param objectName
     *            The objectName to set.
     */
    public void setObjectName(String objectName) {
        this.objectName = objectName;
    }

    /**
     * @return Returns the excludes.
     */
    public String getExcludes() {
        return excludes;
    }

    /**
     * @param excludes
     *            The excludes to set.
     */
    public void setExcludes(String excludes) {
        this.excludes = excludes;
    }

    /**
     * @return Returns the keyField.
     */
    public String getKeyField() {
        return keyField;
    }

    /**
     * @param keyField
     *            The keyField to set.
     */
    public void setKeyField(String keyField) {
        this.keyField = keyField;
    }

    /**
     * @return Returns the substitutions.
     */
    public String getKeyAlias() {
        return keyAlias;
    }
    
    /**
     * @param substitutions The substitutions to set.
     */
    public void setKeyAlias(String substitutions) {
        this.keyAlias = substitutions;
    }
}