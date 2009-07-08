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


package org.opennms.netmgt.collectd;
import java.io.File;
import java.util.LinkedHashMap;
import java.util.Map;

import org.opennms.netmgt.dao.support.ResourceTypeUtils;
import org.opennms.netmgt.model.RrdRepository;

public class GroupPersister extends BasePersister {

    public GroupPersister(ServiceParameters params, RrdRepository repository) {
        super(params, repository);

    }

    public void visitGroup(AttributeGroup group) {
        pushShouldPersist(group);
        if (shouldPersist()) {
            
            Map<String, String> dsNamesToRrdNames = new LinkedHashMap<String , String>();
            for (CollectionAttribute a : group.getAttributes()) {
                if (NumericAttributeType.supportsType(a.getType())) {
                    dsNamesToRrdNames.put(a.getName(), group.getName());
                }
            }
            
            createBuilder(group.getResource(), group.getName(), group.getGroupType().getAttributeTypes());
            File path = group.getResource().getResourceDir(getRepository());
            ResourceTypeUtils.updateDsProperties(path, dsNamesToRrdNames);
        }
    }

    public void completeGroup(AttributeGroup group) {
        if (shouldPersist()) {
            commitBuilder();
        }
        popShouldPersist();
    }


}
