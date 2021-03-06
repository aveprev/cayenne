/*****************************************************************
 *   Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 ****************************************************************/
package org.apache.cayenne.modeler;

import java.io.File;
import java.util.Iterator;

import javax.swing.JOptionPane;

import org.apache.cayenne.configuration.DataChannelDescriptor;
import org.apache.cayenne.map.DataMap;
import org.apache.cayenne.modeler.action.OpenProjectAction;
import org.apache.cayenne.modeler.action.SaveAction;
import org.apache.cayenne.modeler.dialog.FileDeletedDialog;
import org.apache.cayenne.modeler.util.FileWatchdog;
import org.apache.cayenne.project.Project;

/**
 * ProjectWatchdog class is responsible for tracking changes in cayenne.xml and other
 * Cayenne project files
 * 
 */
public class ProjectWatchdog extends FileWatchdog {

    /**
     * A project to watch
     */
    protected ProjectController mediator;

    /**
     * Creates new watchdog for a specified project
     */
    public ProjectWatchdog(ProjectController mediator) {
        setName("cayenne-project-watchdog");
        this.mediator = mediator;
        setSingleNotification(true); // one message is more than enough
    }

    /**
     * Reloads files to watch from the project. Useful when project's structure has
     * changed
     */
    public void reconfigure() {
        pauseWatching();

        removeAllFiles();

        Project project = mediator.getProject();

        if (project != null // project opened
                && project.getConfigurationResource() != null) // not new project
        {
            String projectPath = project.getConfigurationResource().getURL().getPath()
                    + File.separator;
            addFile(projectPath);

            Iterator<DataMap> it = ((DataChannelDescriptor) project.getRootNode())
                    .getDataMaps()
                    .iterator();
            while (it.hasNext()) {
                DataMap dm = it.next();
                addFile(dm.getConfigurationSource().getURL().getPath());
            }

        }

        resumeWatching();
    }

    @Override
    protected void doOnChange(FileInfo fileInfo) {
        if (showConfirmation("One or more project files were changed by external program. "
                + "Do you want to load the changes?")) {

            // Currently we are reloading all project
            if (mediator.getProject() != null) {

                File fileDirectory = new File(mediator
                        .getProject()
                        .getConfigurationResource()
                        .getURL()
                        .getPath());
                Application.getInstance().getActionManager().getAction(
                        OpenProjectAction.class).openProject(fileDirectory);
            }

        }
        else
            mediator.setDirty(true);
    }

    @Override
    protected void doOnRemove(FileInfo fileInfo) {
        if (mediator.getProject() != null) {
            FileDeletedDialog dialog = new FileDeletedDialog(Application.getFrame());
            dialog.show();

            if (dialog.shouldSave()) {
                Application
                        .getInstance()
                        .getActionManager()
                        .getAction(SaveAction.class)
                        .performAction(null);
            }
            else if (dialog.shouldClose()) {
                CayenneModelerController controller = Application
                        .getInstance()
                        .getFrameController();

                controller.projectClosedAction();
            }
            else
                mediator.setDirty(true);
        }
    }

    /**
     * Shows confirmation dialog
     */
    private boolean showConfirmation(String message) {
        return JOptionPane.YES_OPTION == JOptionPane.showConfirmDialog(
                Application.getFrame(),
                message,
                "File changed",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE);
    }
}
