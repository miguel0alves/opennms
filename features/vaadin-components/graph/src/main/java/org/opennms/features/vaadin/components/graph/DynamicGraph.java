/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2013-2015 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2015 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/
package org.opennms.features.vaadin.components.graph;

import java.util.Date;

import com.vaadin.annotations.JavaScript;
import com.vaadin.server.Sizeable.Unit;
import com.vaadin.ui.AbstractJavaScriptComponent;

/**
 * Renders pre-fabricated graphs.
 *
 * Using this components allows the graphing implementation to be chosen at run-time
 * using Javascript.
 *
 * The graph may be rendered server-side via an image tag or client-side using
 * a Javascript-based graphing stack.
 *
 * @author jwhite
 */
// Vaadin doensn't allow us to reference .js files outside of the application using relative paths
// so we resort to copying all of the dependencies into the target .jar and importing them here.
// The resources are copied using the maven-resources-plugin definition in this module's pom.xml.
@JavaScript({
    "jquery.min.js",
    "holder.min.js",
    "graph.js",
    "dynamicgraph-connector.js"
})
public class DynamicGraph extends AbstractJavaScriptComponent {
    private static final long serialVersionUID = 3363043899957566308L;

    public DynamicGraph(final String graphName, final String resourceId) {
        final DynamicGraphState state = getState();
        state.graphName = graphName;
        state.resourceId = resourceId;

        setWidth(100, Unit.PERCENTAGE);
    }

    public void setBaseHref(String baseHref) {
        getState().baseHref = baseHref;
    }

    public void setStart(Date start) {
        getState().start = start.getTime();
    }

    public void setEnd(Date end) {
        getState().end = end.getTime();
    }

    public void setWidthRatio(Double widthRatio) {
        getState().widthRatio = widthRatio;
    }

    public void setHeightRatio(Double heightRatio) {
        getState().heightRatio = heightRatio;
    }

    public void setTitle(String title) {
        getState().title = title;
    }

    @Override
    protected DynamicGraphState getState() {
        return (DynamicGraphState) super.getState();
    }
}
