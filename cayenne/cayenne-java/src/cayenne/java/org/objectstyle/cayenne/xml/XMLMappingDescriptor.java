/* ====================================================================
 *
 * The ObjectStyle Group Software License, version 1.1
 * ObjectStyle Group - http://objectstyle.org/
 * 
 * Copyright (c) 2002-2007, Andrei (Andrus) Adamchik and individual authors
 * of the software. All rights reserved.
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
 * 3. The end-user documentation included with the redistribution, if any,
 *    must include the following acknowlegement:
 *    "This product includes software developed by independent contributors
 *    and hosted on ObjectStyle Group web site (http://objectstyle.org/)."
 *    Alternately, this acknowlegement may appear in the software itself,
 *    if and wherever such third-party acknowlegements normally appear.
 * 
 * 4. The names "ObjectStyle Group" and "Cayenne" must not be used to endorse
 *    or promote products derived from this software without prior written
 *    permission. For written permission, email
 *    "andrus at objectstyle dot org".
 * 
 * 5. Products derived from this software may not be called "ObjectStyle"
 *    or "Cayenne", nor may "ObjectStyle" or "Cayenne" appear in their
 *    names without prior written permission.
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
 * individuals and hosted on ObjectStyle Group web site.  For more
 * information on the ObjectStyle Group, please see
 * <http://objectstyle.org/>.
 */
package org.objectstyle.cayenne.xml;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;

import org.objectstyle.cayenne.CayenneRuntimeException;
import org.objectstyle.cayenne.DataObject;
import org.objectstyle.cayenne.access.DataContext;
import org.objectstyle.cayenne.property.PropertyUtils;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;

/**
 * A convenience class for dealing with the mapping file. This can encode and decode
 * objects based upon the schema given by the map file.
 * 
 * @author Kevin J. Menard, Jr.
 * @since 1.2
 */
final class XMLMappingDescriptor {

    private SerializableEntity rootEntity;
    private Map entities;
    private DataContext dataContext;

    /**
     * Creates new XMLMappingDescriptor using a URL that points to the mapping file.
     * 
     * @param mappingUrl A URL to the mapping file that specifies the mapping model.
     * @throws CayenneRuntimeException
     */
    XMLMappingDescriptor(String mappingUrl) throws CayenneRuntimeException {

        // Read in the mapping file.
        DocumentBuilder builder = XMLUtil.newBuilder();

        Document document;
        try {
            document = builder.parse(mappingUrl);
        }
        catch (Exception ex) {
            throw new CayenneRuntimeException("Error parsing XML at " + mappingUrl, ex);
        }

        Element root = document.getDocumentElement();

        if (!"model".equals(root.getNodeName())) {
            throw new CayenneRuntimeException(
                    "Root of the mapping model must be \"model\"");
        }

        Map entities = new HashMap();
        Iterator it = XMLUtil.getChildren(root).iterator();
        while (it.hasNext()) {
            Element e = (Element) it.next();

            SerializableEntity entity = new SerializableEntity(this, e);
            String tag = e.getAttribute("xmlTag");
            entities.put(tag, entity);

            if (rootEntity == null) {
                rootEntity = entity;
            }
        }

        this.entities = entities;
    }

    SerializableEntity getRootEntity() {
        return rootEntity;
    }

    /**
     * Decodes the supplied DOM document into an object.
     * 
     * @param xml The DOM document containing the encoded object.
     * @return The decoded object.
     * @throws CayenneRuntimeException
     */
    Object decode(Element xml, DataContext dataContext) throws CayenneRuntimeException {

        // TODO: Add an error check to make sure the mapping file actually is for this
        // data file.
        
        // Store a local copy of the data context.
        this.dataContext = dataContext;

        // Create the object to be returned.
        Object ret = createObject(rootEntity.getDescriptor(), xml);

        // We want to read each value from the XML file and then set the corresponding
        // property value in the object to be returned.
        for (Iterator it = XMLUtil.getChildren(xml).iterator(); it.hasNext();) {
            Element value = (Element) it.next();
            decodeProperty(ret, rootEntity.getDescriptor(), value);
        }

        return ret;
    }

    /**
     * Returns the entity XML block with the same "xmlTag" value as the passed in name.
     * 
     * @param name The name of the entity to retrieve.
     * @return The entity with "xmlTag" equal to the passed in name.
     */
    SerializableEntity getEntity(String name) {
        return (SerializableEntity) entities.get(name);
    }

    /**
     * Returns the property that is associated with the passed in XML tag.
     * 
     * @param entityMapping The root to which the reference to find is relative to.
     * @param propertyXmlTag The name of the entity.
     * @return A name of the Java property mapped for the XML tag.
     */
    private String getPropertyMappingName(Element entityMapping, String propertyXmlTag) {
        for (Iterator it = XMLUtil.getChildren(entityMapping).iterator(); it.hasNext();) {
            Element propertyMapping = (Element) it.next();

            if (propertyXmlTag.equals(propertyMapping.getAttribute("xmlTag"))) {
                return propertyMapping.getAttribute("name");
            }
        }

        return null;
    }

    /**
     * Decodes a property.
     * 
     * @param object The object to be updated with the decoded property's value.
     * @param entityMapping The entity block that contains the property mapping for the
     *            value.
     * @param propertyData The encoded property.
     * @throws CayenneRuntimeException
     */
    private void decodeProperty(Object object, Element entityMapping, Element propertyData)
            throws CayenneRuntimeException {

        String xmlTag = propertyData.getNodeName();
        String propertyName = getPropertyMappingName(entityMapping, xmlTag);

        // check unmapped data
        if (propertyName == null) {
            return;
        }

        SerializableEntity targetEntityMapping = getEntity(xmlTag);

        // This is a "simple" encoded property.
        if (targetEntityMapping == null) {
            setProperty(object, propertyName, XMLUtil.getText(propertyData));
        }
        // nested entity property
        else {

            Object o = createObject(targetEntityMapping.getDescriptor(), propertyData);

            // Decode each of the property's children, setting values in the newly
            // created object.
            Iterator it = XMLUtil.getChildren(propertyData).iterator();
            while (it.hasNext()) {
                Element child = (Element) it.next();
                decodeProperty(o, targetEntityMapping.getDescriptor(), child);
            }

            setProperty(object, propertyName, o);
        }
    }

    /**
     * Sets decoded object property. If a property is of Collection type, an object is
     * added to the collection.
     */
    private void setProperty(Object object, String propertyName, Object value) {

        // attempt to first set as a simple property, on failure try collection...
        // checking for collection first via 'PropertyUtils.getProperty' would throw an
        // exception on valid simple properties that are settable but not gettable

        try {
            PropertyUtils.setProperty(object, propertyName, value);
        }
        catch (CayenneRuntimeException e) {
            Object existingValue = PropertyUtils.getProperty(object, propertyName);
            if (existingValue instanceof Collection && !(value instanceof Collection)) {
                ((Collection) existingValue).add(value);
            }
            else {
                throw e;
            }
        }
    }

    /**
     * Instantiates a new object using information from entity mapping. Initializes all
     * properties that exist as 'objectData' attributes. Wraps all exceptions in
     * CayenneRuntimeException.
     * 
     * @param entityMapping Element that describes object to XML mapping.
     * @return The newly created object.
     * @throws CayenneRuntimeException
     */
    private Object createObject(Element entityMapping, Element objectData) {
        String className = entityMapping.getAttribute("name");

        Object object;
        try {
            object = Class.forName(
                    className,
                    true,
                    Thread.currentThread().getContextClassLoader()).newInstance();
        }
        catch (Exception ex) {
            throw new CayenneRuntimeException("Error creating instance of class "
                    + className, ex);
        }
        
        // If a data context has been supplied by the user, then register the data object with the context.
        if ((null != dataContext) && (object instanceof DataObject)) {
            dataContext.registerNewObject((DataObject) object);
        }

        NamedNodeMap attributes = objectData.getAttributes();
        for (int i = 0; i < attributes.getLength(); i++) {
            Attr attribute = (Attr) attributes.item(i);
            String propertyName = getPropertyMappingName(entityMapping, attribute
                    .getName());

            if (propertyName != null) {
                PropertyUtils.setProperty(object, propertyName, attribute.getValue());
            }
        }

        return object;
    }
}