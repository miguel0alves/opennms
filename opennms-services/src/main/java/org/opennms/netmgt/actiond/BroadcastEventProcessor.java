/*******************************************************************************
 * This file is part of the OpenNMS(R) Application.
 *
 * Copyright (C) 2002-2008 The OpenNMS Group, Inc.  All rights reserved.
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


package org.opennms.netmgt.actiond;

import java.util.Enumeration;

import org.apache.log4j.Category;
import org.opennms.core.queue.FifoQueue;
import org.opennms.core.queue.FifoQueueException;
import org.opennms.core.utils.ThreadCategory;
import org.opennms.netmgt.eventd.EventIpcManagerFactory;
import org.opennms.netmgt.model.events.EventListener;
import org.opennms.netmgt.xml.event.Autoaction;
import org.opennms.netmgt.xml.event.Event;

/**
 * 
 * @author <a href="mailto:weave@oculan.com">Brian Weaver </a>
 * @author <a href="http://www.opennms.org/">OpenNMS </a>
 */
final class BroadcastEventProcessor implements EventListener {
    /**
     * The location where executable events are enqueued to be executed.
     */
    private final FifoQueue<String> m_execQ;

    /**
     * This constructor subscribes to eventd for all events
     * 
     * @param execQ
     *            The queue where executable events are stored.
     * 
     */
    BroadcastEventProcessor(FifoQueue<String> execQ) {
        // set up the exectuable queue first
        //
        m_execQ = execQ;

        // subscribe for all events
        EventIpcManagerFactory.init();
        EventIpcManagerFactory.getIpcManager().addEventListener(this);

    }

    /**
     * Unsubscribe from eventd
     */
    public synchronized void close() {
        EventIpcManagerFactory.getIpcManager().removeEventListener(this);
    }

    /**
     * This method is invoked by the EventIpcManager when a new event is
     * available for processing. Each event's autoactions and trouble tickets
     * are queued to be run
     * 
     * @param event
     *            The event
     */
    public void onEvent(Event event) {
        Category log = ThreadCategory.getInstance(BroadcastEventProcessor.class);

        if (event == null) {
            return;
        }

        // Handle autoactions
        //
        Enumeration<Autoaction> walker = event.enumerateAutoaction();
        while (walker.hasMoreElements()) {
            try {
                Autoaction aact = walker.nextElement();
                if (aact.getState().equalsIgnoreCase("on")) {
                    m_execQ.add(aact.getContent()); // java.lang.String
                }

                if (log.isDebugEnabled()) {
                    log.debug("Added event \'" + event.getUei() + "\' to execute autoaction \'" + aact.getContent() + "\'");
                }
            } catch (FifoQueueException ex) {
                log.error("Failed to add event to execution queue", ex);
                break;
            } catch (InterruptedException ex) {
                log.error("Failed to add event to execution queue", ex);
                break;
            }
        }

        // Handle trouble tickets
        //
        if (event.getTticket() != null && event.getTticket().getState().equalsIgnoreCase("on")) {
            try {
                m_execQ.add(event.getTticket().getContent()); // java.lang.String

                if (log.isDebugEnabled())
                    log.debug("Added event \'" + event.getUei() + "\' to execute tticket \'" + event.getTticket().getContent() + "\'");
            } catch (FifoQueueException ex) {
                log.error("Failed to add event to execution queue", ex);
            } catch (InterruptedException ex) {
                log.error("Failed to add event to execution queue", ex);
            }
        }

    } // end onMessage()

    /**
     * Return an id for this event listener
     */
    public String getName() {
        return "Actiond:BroadcastEventProcessor";
    }

} // end class
