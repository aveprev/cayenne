package org.objectstyle.testui;
/* ====================================================================
 * 
 * The ObjectStyle Group Software License, Version 1.0 
 *
 * Copyright (c) 2002 The ObjectStyle Group 
 * and individual authors of the software.  All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer. 
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. The end-user documentation included with the redistribution, if
 *    any, must include the following acknowlegement:  
 *       "This product includes software developed by the 
 *        ObjectStyle Group (http://objectstyle.org/)."
 *    Alternately, this acknowlegement may appear in the software itself,
 *    if and wherever such third-party acknowlegements normally appear.
 *
 * 4. The names "ObjectStyle Group" and "Cayenne" 
 *    must not be used to endorse or promote products derived
 *    from this software without prior written permission. For written 
 *    permission, please contact andrus@objectstyle.org.
 *
 * 5. Products derived from this software may not be called "ObjectStyle"
 *    nor may "ObjectStyle" appear in their names without prior written
 *    permission of the ObjectStyle Group.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE OBJECTSTYLE GROUP OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the ObjectStyle Group.  For more
 * information on the ObjectStyle Group, please see
 * <http://objectstyle.org/>.
 *
 */

import java.util.*;
import java.util.logging.Logger;
import java.util.logging.Level;
import java.util.regex.*;
import javax.swing.*;
import java.awt.event.*;
import org.objectstyle.*;
import org.objectstyle.cayenne.*;
import org.objectstyle.util.*;
import org.objectstyle.cayenne.gui.*;
import org.objectstyle.cayenne.conf.DriverDataSourceFactory;
import org.objectstyle.cayenne.access.*;

/** Login handler used for unit tests.
  *
  * @author Andrei Adamchik
  */
public class TestLogin extends InteractiveLogin implements TestConstants {
    static Logger logObj = Logger.getLogger(TestLogin.class.getName());

    private static final String[] dbLabels = new String[] {"Sybase", "Oracle", "MySQL", "PostgreSQL"};
    private static final String[] testFiles
    = new String[] {"testnode_syb.xml", "testnode_ora.xml", "testnode_mysql.xml", "testnode_postgresql.xml"};
    private static final String defaultSel = "Default";

    public static InteractiveLogin getTestGuiLoginObject() {
        return new TestLogin();
    }

    private HashMap dsiMap;
    private List labelList;

    public TestLogin() {
        prepareDsiList();
    }



    public void collectLoginInfo() {
        JFrame frame = new JFrame("Hidden Login Frame");
        final TestLoginPanel loginPanel = new TestLoginPanel(frame);
        loginPanel.setDataSrcInfo(dataSrcInfo);

        if(labelList != null && labelList.size() > 0)
            loginPanel.setConfigOptions(labelList, new ActionListener() {
                                        public void actionPerformed(ActionEvent e) {
                                            JComboBox cb = (JComboBox)e.getSource();
                                            Object label = cb.getSelectedItem();
                                            dataSrcInfo = (DataSourceInfo)dsiMap.get(label);
                                            loginPanel.setDataSrcInfo(dataSrcInfo);
                                        }
                                    }
                                   );
        frame.pack();

        // call to show will block until user closes the dialog
        loginPanel.show();
        dataSrcInfo = loginPanel.getDataSrcInfo();
        loginPanel.dispose();
        frame.dispose();
    }


    private void prepareDsiList() {
        // load default settings for tests
        dsiMap = new HashMap();
        labelList = new ArrayList();

        // init selected default
        dataSrcInfo = infoForFilePath("test_resources/testnode.xml");
        if(dataSrcInfo != null) {
            dsiMap.put(defaultSel, dataSrcInfo);
            labelList.add(defaultSel);
        }

        // try to preload initial settings
        for(int i = 0; i < dbLabels.length; i++) {
            DataSourceInfo dsi = infoForFilePath("test_resources/" + testFiles[i]);
            if(dsi != null) {
                dsiMap.put(dbLabels[i], dsi);
                labelList.add(dbLabels[i]);
            }
        }


    }

    private DataSourceInfo infoForFilePath(String path) {
        try {
            TstDataSourceFactory factory = new TstDataSourceFactory();
            factory.load(path);
            DataSourceInfo dsi = factory.getDriverInfo();
            return dsi;
        } catch(Exception ex) {
            logObj.log(Level.WARNING, "error", ex);
            // ignoring
            return null;
        }
    }

    private class TstDataSourceFactory extends DriverDataSourceFactory {
        public TstDataSourceFactory() throws Exception {}

        /** Change access to public. */
        public void load(String location) throws Exception {
            super.load(location);

            // guess adapter
            String driver = getDriverInfo().getJdbcDriver();
            getDriverInfo().setAdapterClass(guessAdapter(driver));
        }

        /** Change access to public. */
        public DataSourceInfo getDriverInfo() {
            return super.getDriverInfo();
        }

        private String guessAdapter(String driver) {
            if(Pattern.compile("oracle", Pattern.CASE_INSENSITIVE).matcher(driver).find())
                return "org.objectstyle.cayenne.dba.oracle.OracleAdapter";
            else if(Pattern.compile("sybase", Pattern.CASE_INSENSITIVE).matcher(driver).find())
                return "org.objectstyle.cayenne.dba.sybase.SybaseAdapter";
            else if(Pattern.compile("mysql", Pattern.CASE_INSENSITIVE).matcher(driver).find())
                return "org.objectstyle.cayenne.dba.mysql.MySQLAdapter";
            else if(Pattern.compile("postgres", Pattern.CASE_INSENSITIVE).matcher(driver).find())
                return "org.objectstyle.cayenne.dba.postgres.PostgresAdapter";
            else
                return null;
        }
    }
}
