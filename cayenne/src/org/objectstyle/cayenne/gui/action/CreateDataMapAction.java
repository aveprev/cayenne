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
package org.objectstyle.cayenne.gui.action;

import java.awt.event.ActionEvent;
import java.io.File;

import javax.swing.JFileChooser;

import org.objectstyle.cayenne.access.DataDomain;
import org.objectstyle.cayenne.gui.Editor;
import org.objectstyle.cayenne.gui.event.Mediator;
import org.objectstyle.cayenne.gui.util.FileSystemViewDecorator;
import org.objectstyle.cayenne.map.DataMap;
import org.objectstyle.cayenne.util.NamedObjectFactory;
import org.objectstyle.cayenne.util.Preferences;


/**
 *  Action that creates new DataMap in the project.
 */
public class CreateDataMapAction extends CayenneAction {
	public static final String ACTION_NAME = "CreateDataMap";
		
	public CreateDataMapAction() {
		super(ACTION_NAME);
	}
	
	/** Calls addDataMap() or creates new data map if no data node selected.*/
	protected void createDataMap() {
		Mediator mediator = getMediator();
		String relative_location = getMapLocation(mediator);
		if (null == relative_location) {
			return;
		}
		
		DataDomain currentDomain = mediator.getCurrentDataDomain();
		DataMap map = (DataMap)NamedObjectFactory.createObject(DataMap.class, currentDomain);
		map.setLocation(relative_location);
		mediator.addDataMap(this, map);
	}
	
	/** Returns location relative to Project or null if nothing selected. */
	static String getMapLocation(Mediator mediator) {
    	Preferences pref = Preferences.getPreferences();
       	String init_dir = (String)pref.getProperty(Preferences.LAST_DIR);
       	// Data map file
   	    File file = null;
   	    // Map location relative to proj dir
   	    String relative_location = null;
        try {
            String proj_dir_str = mediator.getConfig().getProjDir();
            File proj_dir = null;
            if (proj_dir_str != null)
            	proj_dir = new File(proj_dir_str);
            JFileChooser fc;
            FileSystemViewDecorator file_view;
            file_view = new FileSystemViewDecorator(proj_dir);
            // Get the data map file name
            fc = new JFileChooser(file_view);
            fc.setFileSelectionMode(JFileChooser.FILES_ONLY);
            fc.setDialogTitle("Enter data map file name");
            if (null != init_dir) {
            	File init_dir_file = new File(init_dir);
            	if (init_dir_file.exists())
            		fc.setCurrentDirectory(init_dir_file);
            }
            int ret_code = fc.showSaveDialog(Editor.getFrame());
            if ( ret_code != JFileChooser.APPROVE_OPTION)
                return relative_location;
            file = fc.getSelectedFile();
            if (!file.exists())
            	file.createNewFile();
			String new_file_location = file.getAbsolutePath();
			// If it is set, use path striped of proj dir and following separator
			// If proj dir not set, use absolute location.
			if (proj_dir_str == null)
			 	relative_location = new_file_location;
			else
				relative_location
					= new_file_location.substring(proj_dir_str.length() + 1);
        } catch (Exception e) {
            System.out.println("Error creating data map file, " + e.getMessage());
            e.printStackTrace();
        }
        return relative_location;
	}
	
	public void performAction(ActionEvent e) {
		createDataMap();
	}
}