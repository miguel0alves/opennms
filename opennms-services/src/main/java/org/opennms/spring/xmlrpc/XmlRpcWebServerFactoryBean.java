/*******************************************************************************
 * This file is part of the OpenNMS(R) Application.
 *
 * Copyright (C) 2005-2006, 2008 The OpenNMS Group, Inc.  All rights reserved.
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

package org.opennms.spring.xmlrpc;

import java.net.InetAddress;

import org.apache.xmlrpc.WebServer;
import org.apache.xmlrpc.XmlRpcServer;
import org.apache.xmlrpc.secure.SecureWebServer;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;

/**
 * 
 * @author <a href="mailto:brozow@opennms.org">Mathew Brozowski</a>
 */
public class XmlRpcWebServerFactoryBean implements FactoryBean, InitializingBean, DisposableBean {
    
    WebServer webServer;
    int port = -1;
    InetAddress address = null;
    XmlRpcServer xmlRpcServer = null;
    boolean secure = false;

    public int getPort() {
        return this.port;
    }
    
    public void setPort(int port) {
        this.port = port;
    }
    
    public InetAddress getAddress() {
        return this.address;
    }
    
    public void setAddress(InetAddress addrress) {
        this.address = addrress;
    }
    
    public boolean getSecure() {
        return this.secure;
    }

    public void setSecure(boolean secure) {
        this.secure = secure;
    }

    public XmlRpcServer getXmlRpcServer() {
        return this.xmlRpcServer;
    }

    public void setXmlRpcServer(XmlRpcServer xmlRpcServer) {
        this.xmlRpcServer = xmlRpcServer;
    }

    public Object getObject() throws Exception {
        return webServer;
    }

    public Class getObjectType() {
        return WebServer.class;
    }

    public boolean isSingleton() {
        return true;
    }

    public void afterPropertiesSet() throws Exception {
        if (this.port == -1)
            throw new IllegalArgumentException("port is required");
        
        if (this.xmlRpcServer == null)
            this.xmlRpcServer = new XmlRpcServer();
        
        if (secure) {
            webServer = new SecureWebServer(this.port, this.address, this.xmlRpcServer);
        }
        else
            webServer = new WebServer(this.port, this.address, this.xmlRpcServer);
        webServer.start();
    }

    public void destroy() throws Exception {
        webServer.shutdown();
    }
    


}
