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

package org.apache.cayenne.pref;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.prefs.Preferences;

import org.apache.cayenne.access.DataContext;
import org.apache.cayenne.modeler.Application;
import org.apache.cayenne.modeler.pref.DBConnectionInfo;


/**
 * An editor for modifying CayennePreferenceService.
 * 
 */
public abstract class CayennePreferenceEditor implements PreferenceEditor {

    protected CayennePreferenceService service;
    protected DataContext editingContext;
    protected boolean restartRequired;
    protected int saveInterval;
    protected CayenneProjectPreferences cayenneProjectPreferences;
    private Map<Preferences, Map<String, String>> changedPreferences;
    private Map<Preferences, Map<String, String>> removedPreferences;
    private Map<Preferences, Map<String, Boolean>> changedBooleanPreferences;

    public CayennePreferenceEditor(CayennePreferenceService service,
            CayenneProjectPreferences cayenneProjectPreferences) {
        this.service = service;
        this.editingContext = service
                .getDataContext()
                .getParentDataDomain()
                .createDataContext();
        this.saveInterval = service.getSaveInterval();
        this.cayenneProjectPreferences = cayenneProjectPreferences;
        this.changedPreferences = new HashMap<Preferences, Map<String, String>>();
        this.removedPreferences = new HashMap<Preferences, Map<String, String>>();
        this.changedBooleanPreferences = new HashMap<Preferences, Map<String, Boolean>>();
    }

    
    
    public Map<Preferences, Map<String, String>> getRemovedPreferences() {
        return removedPreferences;
    }


    public Map<Preferences, Map<String, String>> getChangedPreferences() {
        return changedPreferences;
    }

    public Map<Preferences, Map<String, Boolean>> getChangedBooleanPreferences() {
        return changedBooleanPreferences;
    }

    protected boolean isRestartRequired() {
        return restartRequired;
    }

    protected void setRestartRequired(boolean restartOnSave) {
        this.restartRequired = restartOnSave;
    }

    protected DataContext getEditingContext() {
        return editingContext;
    }

    public Domain editableInstance(Domain object) {
        if (object.getObjectContext() == getEditingContext()) {
            return object;
        }

        return (Domain) getEditingContext().localObject(object.getObjectId(), null);
    }

    public PreferenceDetail createDetail(Domain domain, String key) {
        domain = editableInstance(domain);
        DomainPreference preference = getEditingContext().newObject(
                DomainPreference.class);
        preference.setDomain(domain);
        preference.setKey(key);

        return preference.getPreference();
    }

    public PreferenceDetail createDetail(Domain domain, String key, Class javaClass) {
        domain = editableInstance(domain);
        DomainPreference preferenceLink = getEditingContext().newObject(
                DomainPreference.class);
        preferenceLink.setDomain(domain);
        preferenceLink.setKey(key);

        PreferenceDetail detail = (PreferenceDetail) getEditingContext().newObject(
                javaClass);

        detail.setDomainPreference(preferenceLink);
        return detail;
    }

    public PreferenceDetail deleteDetail(Domain domain, String key) {
        domain = editableInstance(domain);
        PreferenceDetail detail = domain.getDetail(key, false);

        if (detail != null) {
            DomainPreference preference = detail.getDomainPreference();
            preference.setDomain(null);
            getEditingContext().deleteObject(preference);
            getEditingContext().deleteObject(detail);
        }

        return detail;
    }

    public int getSaveInterval() {
        return saveInterval;
    }

    public void setSaveInterval(int ms) {
        if (saveInterval != ms) {
            saveInterval = ms;
            restartRequired = true;
        }
    }

    public PreferenceService getService() {
        return service;
    }

    public void save() {
        cayenneProjectPreferences.getDetailObject(DBConnectionInfo.class).save();
        service.setSaveInterval(saveInterval);
        editingContext.commitChanges();

        if (restartRequired) {
            restart();
        }

        // update boolean preferences
        Iterator it = changedBooleanPreferences.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry entry = (Map.Entry) it.next();
            Preferences pref = (Preferences) entry.getKey();
            Map<String, Boolean> map =  (Map<String, Boolean>) entry.getValue();

            Iterator iterator = map.entrySet().iterator();
            while (iterator.hasNext()) {
                Map.Entry en = (Map.Entry) iterator.next();
                String key = (String) en.getKey();
                Boolean value = (Boolean) en.getValue();
                
                pref.putBoolean(key, value);
            }
        }
        
        // update string preferences
        Iterator iter = changedPreferences.entrySet().iterator();
        while (iter.hasNext()) {
            Map.Entry entry = (Map.Entry) iter.next();
            Preferences pref = (Preferences) entry.getKey();
            Map<String, String> map =  (Map<String, String>) entry.getValue();

            Iterator iterator = map.entrySet().iterator();
            while (iterator.hasNext()) {
                Map.Entry en = (Map.Entry) iterator.next();
                String key = (String) en.getKey();
                String value = (String) en.getValue();
                
                pref.put(key, value);
            }
        }
        
        // remove string preferences
        Iterator iterator = removedPreferences.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry entry = (Map.Entry) iterator.next();
            Preferences pref = (Preferences) entry.getKey();
            Map<String, String> map =  (Map<String, String>) entry.getValue();

            Iterator itRem = map.entrySet().iterator();
            while (itRem.hasNext()) {
                Map.Entry en = (Map.Entry) itRem.next();
                String key = (String) en.getKey();
                pref.remove(key);
            }
        }
        
        Application.getInstance().initClassLoader();
    }

    public void revert() {
        cayenneProjectPreferences.getDetailObject(DBConnectionInfo.class).cancel();
        editingContext.rollbackChanges();
        restartRequired = false;
    }

    protected abstract void restart();
}
