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


package org.opennms.netmgt.syslogd;

import java.io.ByteArrayInputStream;
import java.io.UnsupportedEncodingException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import org.apache.log4j.Category;
import org.exolab.castor.xml.MarshalException;
import org.exolab.castor.xml.ValidationException;
import org.opennms.core.utils.ThreadCategory;
import org.opennms.netmgt.config.syslogd.HideMatch;
import org.opennms.netmgt.config.syslogd.HideMessage;
import org.opennms.netmgt.config.syslogd.UeiList;
import org.opennms.netmgt.config.syslogd.UeiMatch;
import org.opennms.netmgt.dao.castor.CastorUtils;
import org.opennms.netmgt.xml.event.Event;
import org.opennms.netmgt.xml.event.Log;
import org.opennms.netmgt.xml.event.Logmsg;
import org.opennms.netmgt.xml.event.Parm;
import org.opennms.netmgt.xml.event.Parms;
import org.opennms.netmgt.xml.event.Value;

/**
 * @author <a href="mailto:joed@opennms.org">Johan Edstrom</a>
 * @author <a href="mailto:brozow@opennms.org">Mathew Brozowski</a>
 * @author <a href="mailto:dj@opennms.org">DJ Gregor</a>
 * @author <a href="mailto:mhuot@opennms.org">Mike Huot</a>
 * @author <a href="mailto:weave@oculan.com">Brian Weaver </a>
 * @author <a href="http://www.oculan.com">Oculan Corporation </a>
 */

// This routine do the majority of the Syslogd's work
// Improvements most likely are to be made.
final class ConvertToEvent {

    private static final String LOG4J_CATEGORY = "OpenNMS.Syslogd";
    protected static final String HIDDEN_MESSAGE = "The message logged has been removed due to configuration of Syslogd; it may contain sensitive data.";

    @SuppressWarnings("unused")
    private static String m_localAddr;

    /**
     * The received XML event, decoded using the US-ASCII encoding.
     */
    private String m_eventXML;

    @SuppressWarnings("unused")
    private static Event e;

    /**
     * The decoded event document. The classes are defined in an XSD and
     * generated by castor.
     */
    private Log m_log;

    /**
     * The Internet address of the sending agent.
     */
    private InetAddress m_sender;

    /**
     * The port of the agent on the remote system.
     */
    private int m_port;

    /**
     * The list of event that have been acknowledged.
     */
    private List<Event> m_ackEvents;

    private Event m_event;

    /**
     * Private constructor to prevent the used of <em>new</em> except by the
     * <code>make</code> method.
     */
    private ConvertToEvent() {
        // constructor not supported
        // except through make method!
    }

    /**
     * Constructs a new event encapsulation instance based upon the
     * information passed to the method. The passed datagram data is decoded
     * into a string using the <tt>US-ASCII</tt> character encoding.
     *
     * @param packet The datagram received from the remote agent.
     * @throws java.io.UnsupportedEncodingException
     *          Thrown if the data buffer cannot be decoded using the
     *          US-ASCII encoding.
     * @throws MessageDiscardedException 
     */
    static ConvertToEvent make(DatagramPacket packet, String matchPattern,
                               int hostGroup, int messageGroup,
                               UeiList ueiList, HideMessage hideMessage,
                               String discardUei)

            throws UnsupportedEncodingException, MessageDiscardedException {
        return make(packet.getAddress(), packet.getPort(), packet.getData(),
                packet.getLength(), matchPattern, hostGroup, messageGroup,
                ueiList, hideMessage, discardUei);
    }

    /**
     * Constructs a new event encapsulation instance based upon the
     * information passed to the method. The passed byte array is decoded into
     * a string using the <tt>US-ASCII</tt> character encoding.
     *
     * @param addr The remote agent's address.
     * @param port The remote agent's port
     * @param data The XML data in US-ASCII encoding.
     * @param len  The length of the XML data in the buffer.
     * @throws java.io.UnsupportedEncodingException
     *          Thrown if the data buffer cannot be decoded using the
     *          US-ASCII encoding.
     * @throws MessageDiscardedException 
     */
    static ConvertToEvent make(InetAddress addr, int port, byte[] data,
                               int len, String matchPattern, int hostGroup, int messageGroup,
                               UeiList ueiList, HideMessage hideMessage, String discardUei)
            throws UnsupportedEncodingException, MessageDiscardedException {

        ConvertToEvent e = new ConvertToEvent();

        // Get host address /

        e.m_sender = addr;
        e.m_port = port;
        e.m_eventXML = new String(data, 0, len, "US-ASCII");
        e.m_ackEvents = new ArrayList<Event>(16);
        e.m_log = null;

        String m_logPrefix = Syslogd.LOG4J_CATEGORY;
        ThreadCategory.setPrefix(m_logPrefix);
        ThreadCategory.setPrefix(LOG4J_CATEGORY);
        Category log = ThreadCategory.getInstance();

        log.debug("In the make part of UdpReceivedSyslog " + e.toString());

        // Build a basic event out of the syslog message

        Event event = new Event();
        event.setSource("syslogd");

        // Set nodeId

        long nodeId = SyslogdIPMgr.getNodeId(addr.toString().replaceAll("/",
                ""));
        // log.debug("Nodeid via SyslogdIPMgr " +
        // SyslogdIPMgr.getNodeId(addr.toString().replaceAll("/","")));

        if (nodeId != -1)
            event.setNodeid(nodeId);

        // Set event host
        //
        try {
            event.setHost(InetAddress.getLocalHost().getHostName());
        } catch (UnknownHostException uhE) {
            event.setHost("unresolved.host");
            log.warn("Failed to resolve local hostname", uhE);
        }

        event.setInterface(addr.toString().replaceAll("/", ""));

        event.setTime(org.opennms.netmgt.EventConstants.formatToString(new java.util.Date()));
        Logmsg logmsg = new Logmsg();
        logmsg.setDest("logndisplay");

        String message = new String(data, 0, len, "US-ASCII");

        // log.debug("The parsed message... " + message );

        int lbIdx = message.indexOf('<');
        int rbIdx = message.indexOf('>');

        if (lbIdx < 0 || rbIdx < 0 || lbIdx >= (rbIdx - 1)) {
            log.warn("Syslogd received an unparsable message!");
        }

        int priCode = 0;
        String priStr = message.substring(lbIdx + 1, rbIdx);

        try {
            priCode = Integer.parseInt(priStr);
        } catch (NumberFormatException ex) {
            log.debug("ERROR Bad priority code '" + priStr + "'");

        }

        int facility = SyslogDefs.extractFacility(priCode);
        int priority = SyslogDefs.extractPriority(priCode);

        String priorityTxt = SyslogDefs.getPriorityName(priority);
        // event.setSeverity(priorityTxt);
        // We leave the priority alone, this might need to be set.

        String facilityTxt = SyslogDefs.getFacilityName(facility);

        //Check for UEI matching or allow a simple standard one.

        event.setUei("uei.opennms.org/syslogd/" + facilityTxt + "/"
                + priorityTxt);

        // message = message.substring(rbIdx + 1, (message.length() - 1));

        message = message.substring(rbIdx + 1, (message.length()));

        //
        // Check to see if msg looks non-standard.
        // In this case, it means that there is not a standard
        // date in the front of the message text.
        //
        boolean stdMsg = true;

        if (message.length() < 16) {
            stdMsg = false;
        } else if (message.charAt(3) != ' ' || message.charAt(6) != ' '
                || message.charAt(9) != ':' || message.charAt(12) != ':'
                || message.charAt(15) != ' ') {
            stdMsg = false;
        }

        String timestamp;

        if (!stdMsg) {
            try {
                timestamp = SyslogTimeStamp.getInstance().format(new Date());
            } catch (IllegalArgumentException ex) {
                log.debug("ERROR INTERNAL DATE ERROR!");
                timestamp = "";
            }
        } else {
            timestamp = message.substring(0, 15);
            message = message.substring(16);
        }

        // These 2 debugs will aid in analyzing the regexpes as syslog seems
        // to differ alot
        // depending on implementation or message structure.

        log.debug("Message : " + message);
        log.debug("Pattern : " + matchPattern);
        log.debug("Host group: " + hostGroup);
        log.debug("Message group: " + messageGroup);

        // We will also here find out if, the host needs to
        // be replaced, the message matched to a UEI, and
        // last if we need to actually hide the message.
        // this being potentially helpful in avoiding showing
        // operator a password or other data that should be
        // confindential.

        Pattern pattern = Pattern.compile(matchPattern);
        Matcher m = pattern.matcher(message);

        /*
        * We matched on a regexp for host/message pair.
        * This can be a forwarded message as in BSD Style
        * or syslog-ng.
        * We assume that the host is given to us
        * as an IP/Hostname and that the resolver
        * on the ONMS host actually can resolve the
        * node to match against nodeId.
         */

        if ((m = pattern.matcher(message)).matches()) {

            log.debug("Regexp matched message: " + message);
            log.debug("Host: " + m.group(hostGroup));
            log.debug("Message: " + m.group(messageGroup));

            // We will try and extract an IP address from
            // a hostname.....

            String myHost = "";

            try {
                InetAddress address = InetAddress.getByName(m.group(hostGroup));
                byte[] ipAddr = address.getAddress();

                // Convert to dot representation
                for (int i = 0; i < ipAddr.length; i++) {
                    if (i > 0) {
                        myHost += ".";
                    }
                    myHost += ipAddr[i] & 0xFF;
                }
            } catch (UnknownHostException e1) {
                log.info("Could not parse the host: " + e1);

            }

            if (!"".equals(myHost)) {
                nodeId = SyslogdIPMgr.getNodeId(myHost.replaceAll(
                        "/",
                        ""));

                if (nodeId != -1)
                  event.setNodeid(nodeId);
                  // Clean up for further processing....
                  event.setInterface(myHost.replaceAll("/", ""));
                message = m.group(messageGroup);
                log.debug("Regexp used to find node: " + event.getNodeid());
            }
        }

        // We will need these shortly
        Parms eventParms = new Parms();
        Parm eventParm = null;
        Value parmValue = null;
        
        Pattern msgPat;
        Matcher msgMat;

        // Time to verify UEI matching.

        for (UeiMatch uei : ueiList.getUeiMatchCollection()) {
            if (uei.getMatch().getType().equals("substr")) {
                if (log.isDebugEnabled()) {
                    log.debug("Attempting substring match for text of a Syslogd event to :" + uei.getMatch().getExpression());
                }
            	if (message.contains(uei.getMatch().getExpression())) {
            	    if (discardUei.equals(uei.getUei())) {
            	        if (log.isDebugEnabled()) {
            	            log.debug("Specified UEI '" + uei.getUei() + "' is same as discard-uei, discarding this message.");
            	            throw new MessageDiscardedException();
            	        }
            	    }
                    //We can pass a new UEI on this
                    log.debug("Changed the UEI of a Syslogd event, based on substring match, to :" + uei.getUei());
                    event.setUei(uei.getUei());
                    // I think we want to stop processing here so the first
                    // ueiMatch wins, right?
                    break;
                }
            } else if (uei.getMatch().getType().equals("regex")) {
                if (log.isDebugEnabled()) {
                    log.debug("Attempting regex match for text of a Syslogd event to :" + uei.getMatch().getExpression());
                }
                try {
            		msgPat = Pattern.compile(uei.getMatch().getExpression(), Pattern.MULTILINE);
            		msgMat = msgPat.matcher(message);
                } catch(PatternSyntaxException pse) {
            		log.error("Failed to compile regex pattern '"+uei.getMatch().getExpression()+"'", pse);
            		msgMat = null;
            	}
            	if ((msgMat != null) && (msgMat.matches())) {
                    if (discardUei.equals(uei.getUei())) {
                        if (log.isDebugEnabled()) {
                            log.debug("Specified UEI '" + uei.getUei() + "' is same as discard-uei, discarding this message.");
                            throw new MessageDiscardedException();
                        }
                    }
            	    // We matched a UEI
            		log.debug("Changed the UEI of a Syslogd event, based on regex match, to :" + uei.getUei());
            		event.setUei(uei.getUei());
            		if (msgMat.groupCount() > 0) {
            			for (int groupNum = 1; groupNum <= msgMat.groupCount(); groupNum++) {
            				log.debug("Added parm 'group"+groupNum+"' with value '"+msgMat.group(groupNum)+"' to Syslogd event based on regex match group");
            				eventParm = new Parm();
            				eventParm.setParmName("group"+groupNum);
            				parmValue = new Value();
            				parmValue.setContent(msgMat.group(groupNum));
            				eventParm.setValue(parmValue);
            				eventParms.addParm(eventParm);
            			}
            		}
                    // I think we want to stop processing here so the first
                    // ueiMatch wins, right?
            		break;
            	}
            }
        }

        // Time to verify if we need to hide the message

        boolean doHide = false;
        for (HideMatch hide : hideMessage.getHideMatchCollection()) {
            if (hide.getMatch().getType().equals("substr")) {
                if (message.contains(hide.getMatch().getExpression())) {
                    // We should hide the message based on this match
                	doHide = true;
                }            	
            } else if (hide.getMatch().getType().equals("regex")) {
            	try {
                	msgPat = Pattern.compile(hide.getMatch().getExpression(), Pattern.MULTILINE);
                	msgMat = msgPat.matcher(message);            		
            	} catch (PatternSyntaxException pse) {
            		log.error("Failed to compile regex pattern '"+hide.getMatch().getExpression()+"'", pse);
            		msgMat = null;
            	}
            	if ((msgMat != null) && (msgMat.matches())) {
                    // We should hide the message based on this match
            		doHide = true;
            	}
            }
            if (doHide) {
	            log.debug("Hiding syslog message from Event - May contain sensitive data");
	            message = HIDDEN_MESSAGE;
	            // We want to stop here, no point in checking further hideMatches
	            break;
            }
        }

        lbIdx = message.indexOf('[');
        rbIdx = message.indexOf(']');
        int colonIdx = message.indexOf(':');
        int spaceIdx = message.indexOf(' ');

        int processId = 0;
        String processName = "";
        String processIdStr = "";

        if (lbIdx < (rbIdx - 1) && colonIdx == (rbIdx + 1) && spaceIdx == (colonIdx + 1)) {
            processName = message.substring(0, lbIdx);
            processIdStr = message.substring(lbIdx + 1, rbIdx);
            message = message.substring(colonIdx + 2);

            try {
                processId = Integer.parseInt(processIdStr);
            } catch (NumberFormatException ex) {
                log.debug("ERROR Bad process id '" + processIdStr + "'");
                processId = 0;
            }
        } else if (lbIdx < 0 && rbIdx < 0 && colonIdx > 0 && spaceIdx == (colonIdx + 1)) {
            processName = message.substring(0, colonIdx);
            message = message.substring(colonIdx + 2);
        }

        // Using parms provides configurability.
        logmsg.setContent(message);
        event.setLogmsg(logmsg);

        // Add appropriate parms
        eventParm = new Parm();
        eventParm.setParmName("syslogmessage");
        parmValue = new Value();
        parmValue.setContent((message));
        eventParm.setValue(parmValue);
        eventParms.addParm(eventParm);

        eventParm = new Parm();
        eventParm.setParmName("severity");
        parmValue = new Value();
        parmValue.setContent("" + priorityTxt);
        eventParm.setValue(parmValue);
        eventParms.addParm(eventParm);

        eventParm = new Parm();
        eventParm.setParmName("timestamp");
        parmValue = new Value();
        parmValue.setContent(timestamp);
        eventParm.setValue(parmValue);
        eventParms.addParm(eventParm);

        eventParm = new Parm();
        eventParm.setParmName("process");
        parmValue = new Value();
        parmValue.setContent(processName);
        eventParm.setValue(parmValue);
        eventParms.addParm(eventParm);

        eventParm = new Parm();
        eventParm.setParmName("service");
        parmValue = new Value();
        parmValue.setContent("" + facilityTxt);
        eventParm.setValue(parmValue);
        eventParms.addParm(eventParm);

        eventParm = new Parm();
        eventParm.setParmName("processid");
        parmValue = new Value();
        parmValue.setContent("" + processId);
        eventParm.setValue(parmValue);
        eventParms.addParm(eventParm);

        // Good thing(TM)
        event.setParms(eventParms);

        e.m_event = event;
        return e;
    }

    /**
     * Decodes the XML package from the remote agent. If an error occurs or
     * the datagram had malformed XML then an exception is generated.
     *
     * @return The top-level <code>Log</code> element of the XML document.
     * @throws org.exolab.castor.xml.ValidationException
     *          Throws if the documents data does not match the defined XML
     *          Schema Definition.
     * @throws org.exolab.castor.xml.MarshalException
     *          Thrown if the XML is malformed and cannot be converted.
     */
    Log unmarshal() throws ValidationException, MarshalException {
        if (m_log == null) {
            m_log = CastorUtils.unmarshal(Log.class, new ByteArrayInputStream(this.m_eventXML.getBytes()));
        }
        return m_log;
    }

    /**
     * Adds the event to the list of events acknowledged in this event XML
     * document.
     *
     * @param e The event to acknowledge.
     */
    void ackEvent(Event e) {
        if (!m_ackEvents.contains(e))
            m_ackEvents.add(e);
    }

    /**
     * Returns the raw XML data as a string.
     */
    String getXmlData() {
        return m_eventXML;
    }

    /**
     * Returns the sender's address.
     */
    InetAddress getSender() {
        return m_sender;
    }

    /**
     * Returns the sender's port
     */
    int getPort() {
        return m_port;
    }

    /**
     * Get the acknowledged events
     */
    public List<Event> getAckedEvents() {
        return m_ackEvents;
    }

    public Event getEvent() {
        return m_event;
    }

    /**
     * Returns true if the instance matches the object based upon the remote
     * agent's address &amp; port. If the passed instance is from the same
     * agent then it is considered equal.
     *
     * @param o instance of the class to compare.
     * @return Returns true if the objects are logically equal, false
     *         otherwise.
     */
    public boolean equals(Object o) {
        if (o != null && o instanceof ConvertToEvent) {
            ConvertToEvent e = (ConvertToEvent) o;
            return (this == e || (m_port == e.m_port && m_sender.equals(e.m_sender)));
        }
        return false;
    }

    /**
     * Returns the hash code of the instance. The hash code is computed by
     * taking the bitwise XOR of the port and the agent's Internet address
     * hash code.
     *
     * @return The 32-bit has code for the instance.
     */
    public int hashCode() {
        return (m_port ^ m_sender.hashCode());
    }
}
