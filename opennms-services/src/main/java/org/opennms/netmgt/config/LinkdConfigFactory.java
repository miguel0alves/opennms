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


package org.opennms.netmgt.config;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;

import org.apache.commons.io.IOUtils;
import org.apache.log4j.Category;
import org.exolab.castor.xml.MarshalException;
import org.exolab.castor.xml.ValidationException;
import org.opennms.core.utils.ThreadCategory;
import org.opennms.netmgt.ConfigFileConstants;

/**
 * This is the singleton class used to load the configuration for the OpenNMS
 * linkd service from the linkd-configuration xml file.
 * 
 * A mapping of the configured URLs to the iplist they contain is built at
 * init() time so as to avoid numerous file reads.
 * 
 * <strong>Note: </strong>Users of this class should make sure the
 * <em>init()</em> is called before calling any other method to ensure the
 * config is loaded before accessing other convenience methods.
 * 
 * @author <a href="mailto:antonio@opennms.it">Antonio Russo</a>
 * @author <a href="mailto:jamesz@opennms.com">James Zuo </a>
 * @author <a href="mailto:mike@opennms.org">Mike Davidson </a>
 * @author <a href="mailto:sowmya@opennms.org">Sowmya Nataraj </a>
 * @author <a href="http://www.opennms.org/">OpenNMS </a>
 */
public final class LinkdConfigFactory extends LinkdConfigManager {
    /**
     * The singleton instance of this factory
     */
    private static LinkdConfig m_singleton = null;

    /**
     * This member is set to true if the configuration file has been loaded.
     */
    private static boolean m_loaded = false;
    
    /**
     * Loaded version
     */
    private long m_currentVersion = -1L;

    /**
     * Private constructor
     * 
     * @exception java.io.IOException
     *                Thrown if the specified config file cannot be read
     * @exception org.exolab.castor.xml.MarshalException
     *                Thrown if the file does not conform to the schema.
     * @exception org.exolab.castor.xml.ValidationException
     *                Thrown if the contents do not match the required schema.
     */
    @Deprecated
    public LinkdConfigFactory(long currentVersion, Reader reader) throws MarshalException, ValidationException, IOException {
        super(reader);
        m_currentVersion = currentVersion;
    }

    public LinkdConfigFactory(long currentVersion, InputStream stream) throws MarshalException, ValidationException, IOException {
        super(stream);
        m_currentVersion = currentVersion;
    }

    /**
     * Load the config from the default config file and create the singleton
     * instance of this factory.
     * 
     * @exception java.io.IOException
     *                Thrown if the specified config file cannot be read
     * @exception org.exolab.castor.xml.MarshalException
     *                Thrown if the file does not conform to the schema.
     * @exception org.exolab.castor.xml.ValidationException
     *                Thrown if the contents do not match the required schema.
     */
    public static synchronized void init() throws IOException, MarshalException, ValidationException {
        if (m_loaded) {
            // init already called - return
            // to reload, reload() will need to be called
            return;
        }

        File cfgFile = ConfigFileConstants.getFile(ConfigFileConstants.LINKD_CONFIG_FILE_NAME);

        logStatic().debug("init: config file path: " + cfgFile.getPath());

        InputStream stream = null;
        try {
            stream = new FileInputStream(cfgFile);
            LinkdConfigFactory config = new LinkdConfigFactory(cfgFile.lastModified(), stream);
            setInstance(config);
        } finally {
            if (stream != null) {
                IOUtils.closeQuietly(stream);
            }
        }
    }

    private static Category logStatic() {
        return ThreadCategory.getInstance(LinkdConfigFactory.class);
    }

    /**
     * Reload the config from the default config file
     * 
     * @exception java.io.IOException
     *                Thrown if the specified config file cannot be read/loaded
     * @exception org.exolab.castor.xml.MarshalException
     *                Thrown if the file does not conform to the schema.
     * @exception org.exolab.castor.xml.ValidationException
     *                Thrown if the contents do not match the required schema.
     */
    public static synchronized void reload() throws IOException, MarshalException, ValidationException {
        init();
        getInstance().update();
    }

    protected synchronized void saveXml(String xml) throws IOException {
        if (xml != null) {
            long timestamp = System.currentTimeMillis();
            File cfgFile = ConfigFileConstants.getFile(ConfigFileConstants.LINKD_CONFIG_FILE_NAME);
            logStatic().debug("saveXml: saving config file at "+timestamp+": " + cfgFile.getPath());
            FileWriter fileWriter = new FileWriter(cfgFile);
            fileWriter.write(xml);
            fileWriter.flush();
            fileWriter.close();
            logStatic().debug("saveXml: finished saving config file: " + cfgFile.getPath());
        }
    }

    /**
     * Return the singleton instance of this factory.
     * 
     * @return The current factory instance.
     * 
     * @throws java.lang.IllegalStateException
     *             Thrown if the factory has not yet been initialized.
     */
    public static synchronized LinkdConfig getInstance() {
        if (!m_loaded) {
            throw new IllegalStateException("The factory has not been initialized");
        }

        return m_singleton;
    }
    
    public static synchronized void setInstance(LinkdConfig instance) {
        m_singleton = instance;
        m_loaded = true;
    }

    public synchronized void update() throws IOException, MarshalException, ValidationException {

        File cfgFile = ConfigFileConstants.getFile(ConfigFileConstants.LINKD_CONFIG_FILE_NAME);
        if (cfgFile.lastModified() > m_currentVersion) {
            m_currentVersion = cfgFile.lastModified();
            logStatic().debug("init: config file path: " + cfgFile.getPath());
            InputStream stream = null;
            try {
                stream = new FileInputStream(cfgFile);
                reloadXML(stream);
            } finally {
                if (stream != null) {
                    IOUtils.closeQuietly(stream);
                }
            }
            logStatic().debug("init: finished loading config file: " + cfgFile.getPath());
        }
    }
}
