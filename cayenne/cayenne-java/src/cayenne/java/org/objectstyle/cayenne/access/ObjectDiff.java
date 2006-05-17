package org.objectstyle.cayenne.access;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.objectstyle.cayenne.CayenneRuntimeException;
import org.objectstyle.cayenne.Fault;
import org.objectstyle.cayenne.ObjectId;
import org.objectstyle.cayenne.PersistenceState;
import org.objectstyle.cayenne.Persistent;
import org.objectstyle.cayenne.graph.GraphChangeHandler;
import org.objectstyle.cayenne.graph.GraphDiff;
import org.objectstyle.cayenne.graph.NodeDiff;
import org.objectstyle.cayenne.map.ObjEntity;
import org.objectstyle.cayenne.map.ObjRelationship;
import org.objectstyle.cayenne.property.ClassDescriptor;
import org.objectstyle.cayenne.property.CollectionProperty;
import org.objectstyle.cayenne.property.Property;
import org.objectstyle.cayenne.property.PropertyVisitor;
import org.objectstyle.cayenne.property.SingleObjectArcProperty;
import org.objectstyle.cayenne.util.Util;

/**
 * A dynamic GraphDiff that represents a delta between object simple properties at diff
 * creation time and its current state.
 */
class ObjectDiff extends NodeDiff {

    private final ObjectStore objectStore;
    private Collection otherDiffs;

    private Map snapshot;
    private Map arcSnapshot;
    private Map currentArcSnapshot;
    private Map flatIds;

    ObjectDiff(ObjectStore objectStore, final Persistent object) {

        super(object.getObjectId());

        this.objectStore = objectStore;

        int state = object.getPersistenceState();

        // take snapshot of simple properties and arcs used for optimistic locking..

        if (state == PersistenceState.COMMITTED
                || state == PersistenceState.DELETED
                || state == PersistenceState.MODIFIED) {

            ObjEntity entity = objectStore.getContext().getEntityResolver().getObjEntity(
                    object.getObjectId().getEntityName());
            final boolean lock = entity.getLockType() == ObjEntity.LOCK_TYPE_OPTIMISTIC;

            this.snapshot = new HashMap();
            this.arcSnapshot = new HashMap();

            getClassDescriptor().visitProperties(new PropertyVisitor() {

                public boolean visitProperty(Property property) {
                    snapshot.put(property.getName(), property.readProperty(object));
                    return true;
                }

                public boolean visitCollectionArc(CollectionProperty property) {
                    return true;
                }

                public boolean visitSingleObjectArc(SingleObjectArcProperty property) {

                    // eagerly resolve optimistically locked relationships
                    Object target = lock ? property.readProperty(object) : property
                            .readPropertyDirectly(object);

                    if (target instanceof Persistent) {
                        target = ((Persistent) target).getObjectId();
                    }
                    // else - null || Fault

                    arcSnapshot.put(property.getName(), target);
                    return true;
                }
            });
        }
    }

    Object getSnapshotValue(String propertyName) {
        return snapshot != null ? snapshot.get(propertyName) : null;
    }

    ObjectId getArcSnapshotValue(String propertyName) {
        Object value = arcSnapshot != null ? arcSnapshot.get(propertyName) : null;

        if (value instanceof Fault) {
            Persistent source = (Persistent) objectStore.getNode(nodeId);
            Persistent target = (Persistent) ((Fault) value).resolveFault(
                    source,
                    propertyName);

            value = target != null ? target.getObjectId() : null;
            arcSnapshot.put(propertyName, value);
        }

        return (ObjectId) value;
    }

    boolean containsArcSnapshot(String propertyName) {
        return arcSnapshot != null ? arcSnapshot.containsKey(propertyName) : false;
    }

    /**
     * Appends individual diffs to collection.
     */
    void appendDiffs(Collection collection) {

        if (otherDiffs != null) {
            collection.addAll(otherDiffs);
        }

        collection.add(new NodeDiff(nodeId, diffId) {

            public void apply(GraphChangeHandler tracker) {
                applySimplePropertyChanges(tracker);
            }

            public void undo(GraphChangeHandler tracker) {
                throw new UnsupportedOperationException();
            }
        });
    }

    void addDiff(GraphDiff diff) {

        boolean addDiff = true;

        if (diff instanceof ArcOperation && snapshot != null) {

            ArcOperation arcDiff = (ArcOperation) diff;
            Object targetId = arcDiff.getTargetNodeId();
            String arcId = arcDiff.getArcId().toString();

            Persistent object = (Persistent) objectStore.getNode(nodeId);
            Property property = getClassDescriptor().getProperty(arcId);

            // note that some collection properties implement 'SingleObjectArcProperty',
            // so we cant't do 'instanceof SingleObjectArcProperty'
            // TODO: andrus, 3.22.2006 - should we consider this a bug?

            if (property instanceof CollectionProperty) {

                // record flattened op changes
                ObjEntity entity = objectStore.context.getEntityResolver().getObjEntity(
                        object.getObjectId().getEntityName());

                ObjRelationship relationship = (ObjRelationship) entity
                        .getRelationship(property.getName());
                if (relationship.isFlattened()) {

                    if (flatIds == null) {
                        flatIds = new HashMap();
                    }

                    ArcOperation oldOp = (ArcOperation) flatIds.put(arcDiff, arcDiff);

                    // "delete" cancels "create" and vice versa...
                    if (oldOp != null && oldOp.isDelete() != arcDiff.isDelete()) {
                        addDiff = false;
                        flatIds.remove(arcDiff);

                        if (otherDiffs != null) {
                            otherDiffs.remove(oldOp);
                        }
                    }
                }
            }
            else if (property instanceof SingleObjectArcProperty) {

                if (currentArcSnapshot == null) {
                    currentArcSnapshot = new HashMap();
                }

                currentArcSnapshot.put(arcId, targetId);
            }
            else {
                String message = (property == null)
                        ? "No property for arcId " + arcId
                        : "Unrecognized property for arcId " + arcId + ": " + property;
                throw new CayenneRuntimeException(message);
            }
        }

        if (addDiff) {
            if (otherDiffs == null) {
                otherDiffs = new ArrayList(3);
            }

            otherDiffs.add(diff);
        }
    }

    /**
     * Checks whether at least a single property is modified.
     */
    public boolean isNoop() {

        // if we have no baseline to compare with, assume that there are changes
        if (snapshot == null) {
            return false;
        }

        if (flatIds != null && !flatIds.isEmpty()) {
            return false;
        }

        final boolean[] modFound = new boolean[1];
        final Persistent object = (Persistent) objectStore.getNode(nodeId);

        int state = object.getPersistenceState();
        if (state == PersistenceState.NEW || state == PersistenceState.DELETED) {
            return false;
        }

        // check phantom mods

        getClassDescriptor().visitProperties(new PropertyVisitor() {

            public boolean visitProperty(Property property) {
                Object oldValue = snapshot.get(property.getName());
                Object newValue = property.readProperty(object);

                if (!Util.nullSafeEquals(oldValue, newValue)) {
                    modFound[0] = true;
                }

                return !modFound[0];
            }

            public boolean visitCollectionArc(CollectionProperty property) {
                // flattened changes
                return true;
            }

            public boolean visitSingleObjectArc(SingleObjectArcProperty property) {
                if (arcSnapshot == null) {
                    return true;
                }

                Object newValue = property.readPropertyDirectly(object);
                if (newValue instanceof Fault) {
                    return true;
                }

                Object oldValue = arcSnapshot.get(property.getName());
                if (!Util.nullSafeEquals(oldValue, newValue != null
                        ? ((Persistent) newValue).getObjectId()
                        : null)) {
                    modFound[0] = true;
                }

                return !modFound[0];
            }
        });

        return !modFound[0];
    }

    public void undo(GraphChangeHandler handler) {
        throw new UnsupportedOperationException();
    }

    public void apply(final GraphChangeHandler handler) {

        if (otherDiffs != null) {
            Iterator it = otherDiffs.iterator();
            while (it.hasNext()) {
                ((GraphDiff) it.next()).apply(handler);
            }
        }

        applySimplePropertyChanges(handler);
    }

    private void applySimplePropertyChanges(final GraphChangeHandler handler) {
        final Persistent object = (Persistent) objectStore.objectMap.get(nodeId);
        getClassDescriptor().visitProperties(new PropertyVisitor() {

            public boolean visitProperty(Property property) {

                Object newValue = property.readProperty(object);

                // no baseline to compare
                if (snapshot == null) {

                    if (newValue != null) {
                        handler.nodePropertyChanged(
                                nodeId,
                                property.getName(),
                                null,
                                newValue);
                    }
                }
                // have baseline to compare
                else {
                    Object oldValue = snapshot.get(property.getName());

                    if (!Util.nullSafeEquals(oldValue, newValue)) {
                        handler.nodePropertyChanged(
                                nodeId,
                                property.getName(),
                                oldValue,
                                newValue);
                    }
                }

                return true;
            }

            public boolean visitCollectionArc(CollectionProperty property) {
                return true;
            }

            public boolean visitSingleObjectArc(SingleObjectArcProperty property) {
                return true;
            }
        });
    }

    /**
     * This is used to update faults.
     */
    void updateArcSnapshot(String propertyName, Persistent object) {
        if (arcSnapshot == null) {
            arcSnapshot = new HashMap();
        }

        arcSnapshot.put(propertyName, object != null ? object.getObjectId() : null);
    }

    ClassDescriptor getClassDescriptor() {
        return objectStore.context.getEntityResolver().getClassDescriptor(
                ((ObjectId) nodeId).getEntityName());
    }

    static final class ArcOperation extends NodeDiff {

        private Object targetNodeId;
        private Object arcId;
        private boolean delete;

        public ArcOperation(Object nodeId, Object targetNodeId, Object arcId,
                boolean delete) {

            super(nodeId);
            this.targetNodeId = targetNodeId;
            this.arcId = arcId;
            this.delete = delete;
        }

        boolean isDelete() {
            return delete;
        }

        public int hashCode() {
            // assuming String and ObjectId provide a good hashCode
            return arcId.hashCode() + targetNodeId.hashCode() + 5;
        }

        public boolean equals(Object object) {
            // compare ignoring op type...
            if (object == this) {
                return true;
            }

            if (object == null) {
                return false;
            }

            if (!(object instanceof ArcOperation)) {
                return false;
            }

            ArcOperation other = (ArcOperation) object;
            return arcId.equals(other.arcId)
                    && Util.nullSafeEquals(targetNodeId, other.targetNodeId);
        }

        public void apply(GraphChangeHandler tracker) {

            if (delete) {
                tracker.arcDeleted(nodeId, targetNodeId, arcId);
            }
            else {
                tracker.arcCreated(nodeId, targetNodeId, arcId);
            }
        }

        public void undo(GraphChangeHandler tracker) {
            throw new UnsupportedOperationException();
        }

        public Object getArcId() {
            return arcId;
        }

        public Object getTargetNodeId() {
            return targetNodeId;
        }
    }
}