/* ====================================================================
 * 
 * The ObjectStyle Group Software License, version 1.1
 * ObjectStyle Group - http://objectstyle.org/
 * 
 * Copyright (c) 2002-2005, Andrei (Andrus) Adamchik and individual authors
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
package org.objectstyle.cayenne;

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;

/**
 * A list of persistent objects lazily resolved on the first access.
 * 
 * @since 1.2
 * @author Andrus Adamchik
 */
public class PersistentObjectList extends RelationshipFault implements List {

    // wrapped objects list
    protected List objectList;

    // track additions/removals in unresolved...
    protected LinkedList addedToUnresolved;
    protected LinkedList removedFromUnresolved;

    /**
     * Creates PersistentListHolder initializing it with ObjectContext and query to
     * resolve the list.
     * 
     * @param parentObject persistent object that owns this list.
     * @param listQuery a query used to resolve the list
     */
    public PersistentObjectList(Persistent relationshipOwner, String relationshipName) {
        super(relationshipOwner, relationshipName);

        // if not a fault, set object list right away...
        if (!isFault()) {
            objectList = new LinkedList();
        }
    }

    /**
     * Returns whether this list is not yet resolved and requires a fetch.
     */
    public boolean isFault() {
        return objectList == null;
    }

    /**
     * Turns itself into a fault, thus forcing a refresh on the next access.
     */
    public void invalidateObjectList() {
        setObjectList(null);
    }

    public void setObjectList(List objectList) {
        this.objectList = objectList;
    }

    // ====================================================
    // Standard List Methods.
    // ====================================================

    public boolean add(Object o) {
        return (isFault()) ? addLocal(o) : objectList.add(o);
    }

    public void add(int index, Object element) {
        resolvedObjectList().add(index, element);
    }

    public boolean addAll(Collection c) {
        return resolvedObjectList().addAll(c);
    }

    public boolean addAll(int index, Collection c) {
        return resolvedObjectList().addAll(index, c);
    }

    public void clear() {
        resolvedObjectList().clear();
    }

    public boolean contains(Object o) {
        return resolvedObjectList().contains(o);
    }

    public boolean containsAll(Collection c) {
        return resolvedObjectList().containsAll(c);
    }

    public boolean equals(Object o) {
        if (o == null) {
            return false;
        }

        if (!(o instanceof PersistentObjectList)) {
            return false;
        }

        return resolvedObjectList().equals(((PersistentObjectList) o).resolvedObjectList());
    }

    public int hashCode() {
        return 37 + resolvedObjectList().hashCode();
    }

    public Object get(int index) {
        return resolvedObjectList().get(index);
    }

    public int indexOf(Object o) {
        return resolvedObjectList().indexOf(o);
    }

    public boolean isEmpty() {
        return resolvedObjectList().isEmpty();
    }

    public Iterator iterator() {
        return resolvedObjectList().iterator();
    }

    public int lastIndexOf(Object o) {
        return resolvedObjectList().lastIndexOf(o);
    }

    public ListIterator listIterator() {
        return resolvedObjectList().listIterator();
    }

    public ListIterator listIterator(int index) {
        return resolvedObjectList().listIterator(index);
    }

    public Object remove(int index) {
        return resolvedObjectList().remove(index);
    }

    public boolean remove(Object o) {
        return (isFault()) ? removeLocal(o) : objectList.remove(o);
    }

    public boolean removeAll(Collection c) {
        return resolvedObjectList().removeAll(c);
    }

    public boolean retainAll(Collection c) {
        return resolvedObjectList().retainAll(c);
    }

    public Object set(int index, Object element) {
        return resolvedObjectList().set(index, element);
    }

    public int size() {
        return resolvedObjectList().size();
    }

    public List subList(int fromIndex, int toIndex) {
        return resolvedObjectList().subList(fromIndex, toIndex);
    }

    public Object[] toArray() {
        return resolvedObjectList().toArray();
    }

    public Object[] toArray(Object[] a) {
        return resolvedObjectList().toArray(a);
    }

    // ====================================================
    // Tracking list modifications, and resolving it
    // on demand
    // ====================================================

    /**
     * Returns internal objects list resolving it if needed.
     */
    protected List resolvedObjectList() {
        if (isFault()) {

            synchronized (this) {

                // now that we obtained the lock, check
                // if another thread just resolved the list
                if (isFault()) {
                    List localList;

                    if (isTransientParent()) {
                        localList = new LinkedList();
                    }
                    else {
                        localList = resolveFromDB();
                    }

                    mergeLocalChanges(localList);
                    this.objectList = localList;
                }
            }
        }

        return objectList;
    }

    void clearLocalChanges() {
        addedToUnresolved = null;
        removedFromUnresolved = null;
    }

    void mergeLocalChanges(List fetchedList) {

        // only merge if an object is in an uncommitted state
        // any other state means that our local tracking
        // is invalid...
        if (isUncommittedParent()) {

            if (removedFromUnresolved != null) {
                fetchedList.removeAll(removedFromUnresolved);
            }

            // add only those that are not already on the list
            // do not include transient objects...
            if (addedToUnresolved != null && !addedToUnresolved.isEmpty()) {
                Iterator it = addedToUnresolved.iterator();
                while (it.hasNext()) {
                    Object next = it.next();

                    if (next instanceof DataObject) {
                        DataObject dataObject = (DataObject) next;
                        if (dataObject.getPersistenceState() == PersistenceState.TRANSIENT) {
                            continue;
                        }
                    }

                    if (!fetchedList.contains(next)) {
                        fetchedList.add(next);
                    }
                }
            }
        }

        // clear local information in any event
        clearLocalChanges();
    }

    boolean addLocal(Object object) {

        if (removedFromUnresolved != null) {
            removedFromUnresolved.remove(object);
        }

        if (addedToUnresolved == null) {
            addedToUnresolved = new LinkedList();
        }

        addedToUnresolved.addLast(object);

        // this is really meaningless, since we don't know
        // if an object was present in the list
        return true;
    }

    boolean removeLocal(Object object) {
        if (addedToUnresolved != null) {
            addedToUnresolved.remove(object);
        }

        if (removedFromUnresolved == null) {
            removedFromUnresolved = new LinkedList();
        }

        removedFromUnresolved.addLast(object);

        // this is really meaningless, since we don't know
        // if an object was present in the list
        return true;
    }
}
