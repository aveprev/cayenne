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
package org.objectstyle.cayenne.modeler.action;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.io.File;

import javax.swing.JOptionPane;
import javax.swing.KeyStroke;

import org.apache.log4j.Logger;
import org.objectstyle.cayenne.modeler.Editor;
import org.objectstyle.cayenne.modeler.ErrorDebugDialog;
import org.objectstyle.cayenne.modeler.ModelerPreferences;
import org.objectstyle.cayenne.modeler.event.Mediator;
import org.objectstyle.cayenne.modeler.util.RecentFileMenuItem;
import org.objectstyle.cayenne.project.Project;
import org.objectstyle.cayenne.project.ProjectException;
/**
 * @author Andrei Adamchik
 */
public class OpenProjectAction extends ProjectAction {
    static Logger logObj = Logger.getLogger(OpenProjectAction.class.getName());
    public static final String ACTION_NAME = "Open Project";
    /**
     * Constructor for OpenProjectAction.
     */
    public OpenProjectAction() {
        super(ACTION_NAME);
    }
    public String getIconName() {
        return "icon-open.gif";
    }
    public KeyStroke getAcceleratorKey() {
        return KeyStroke.getKeyStroke(KeyEvent.VK_O, ActionEvent.CTRL_MASK);
    }
    /**
     * @see org.objectstyle.cayenne.modeler.action.CayenneAction#performAction(ActionEvent)
     */
    public void performAction(ActionEvent e) {
        File f = null;
        if (e.getSource() instanceof RecentFileMenuItem) {
            RecentFileMenuItem menu = (RecentFileMenuItem) e.getSource();
            f = menu.getFile();
        }
        if (f == null) {
            openProject();
        } else {
            openProject(f);
        }
    }
    /** Opens cayenne.xml file using file chooser. */
    protected void openProject() {
        ModelerPreferences pref = ModelerPreferences.getPreferences();
        String init_dir = (String) pref.getProperty(ModelerPreferences.LAST_DIR);
        try {
            // Get the project file name (always cayenne.xml)
            File file = fileChooser.openProjectFile(Editor.getFrame());

            if (file != null) {
                openProject(file);
            }
        } catch (Exception e) {
            logObj.warn("Error loading project file.", e);
        }
    }
    
    
    /** Opens specified project file. File must already exist. */
    protected void openProject(File file) {
        // Save and close (if needed) currently open project.
        if (getMediator() != null && !closeProject()) {
            return;
        }
        ModelerPreferences pref = ModelerPreferences.getPreferences();
        try {
            // Save dir path to the preferences
            pref.setProperty(ModelerPreferences.LAST_DIR, file.getParent());
            Editor.getFrame().addToLastProjList(file.getAbsolutePath());
            // Initialize gui configuration
            // uncomment to debug GUI
            Project project = Project.createProject(file);
            Editor.getFrame().getController().getTopModel().setCurrentProject(project);
            // if upgrade was canceled
            if (project.isUpgradeNeeded() && !processUpgrades(project)) {
                closeProject();
            } else {
                setMediator(new Mediator());
                Editor.getFrame().projectOpened(project);
            }
        } catch (Exception ex) {
            logObj.warn("Error loading project file.", ex);
            ErrorDebugDialog.guiWarning(ex, "Error loading project");
        }
    }
    protected boolean processUpgrades(Project project) throws ProjectException {
        // must really concat all messages, this is a temp hack...
        String msg = (String) project.getUpgradeMessages().get(0);
        // need an upgrade
        int returnCode =
            JOptionPane.showConfirmDialog(
                Editor.getFrame(),
                "Project needs an upgrade to a newer version. " + msg + ". Upgrade?",
                "Upgrade Needed",
                JOptionPane.YES_NO_OPTION);
        if (returnCode == JOptionPane.NO_OPTION) {
            return false;
        }
        // perform upgrade
        logObj.info("Will upgrade project " + project.getMainFile());
        project.save();
        return true;
    }
}
