package org.objectstyle.cayenne.gui;
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

import java.awt.Component;
import java.util.*;
import java.io.*;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.tree.*;

import org.objectstyle.cayenne.access.*;
import org.objectstyle.cayenne.map.*;
import org.objectstyle.cayenne.gui.event.*;
import org.objectstyle.cayenne.gui.util.*;


/** Tree of domains, data maps, data nodes (sources) and entities. 
  * When item of the tree is selected, detailed view for that 
  * item comes up. */
public class BrowseView extends JScrollPane 
implements TreeSelectionListener, DomainDisplayListener, DomainListener
, DataMapDisplayListener, DataMapListener, DataNodeDisplayListener, DataNodeListener
, ObjEntityListener, ObjEntityDisplayListener
, DbEntityListener, DbEntityDisplayListener
{
	
	private static final int DOMAIN_NODE = 1;
	private static final int NODE_NODE = 2;
	private static final int MAP_NODE = 3;
	private static final int OBJ_ENTITY_NODE = 4;
	private static final int DB_ENTITY_NODE = 5;
	
	protected Mediator mediator;
  	protected JTree	browseTree = new JTree();
	protected DefaultMutableTreeNode rootNode;
	protected DefaultMutableTreeNode currentNode;
	
	protected DefaultTreeModel model;
	
	public BrowseView(Mediator data_map_editor) {
		super();
		mediator = data_map_editor;
		setViewportView(browseTree);
		//browseTree.setCellRenderer(new BrowseViewRenderer());
		load();
		browseTree.addTreeSelectionListener(this);		
		mediator.addDomainListener(this);
		mediator.addDomainDisplayListener(this);
		mediator.addDataNodeListener(this);
		mediator.addDataNodeDisplayListener(this);
		mediator.addDataMapListener(this);
		mediator.addDataMapDisplayListener(this);
		mediator.addObjEntityListener(this);
		mediator.addObjEntityDisplayListener(this);
		mediator.addDbEntityListener(this);
		mediator.addDbEntityDisplayListener(this);
	}

	/** Traverses domains, nodes, maps and entities and populates tree.
	 */
	private void load() {
		rootNode = new DefaultMutableTreeNode(null, true);
		// create tree model with root objects
		model = new DefaultTreeModel(rootNode);
		// Populate obj tree
		DataDomain[] temp_domains = mediator.getDomains();
		for (int i = 0; i < temp_domains.length; i++) {
			DefaultMutableTreeNode domain_ele;
			domain_ele = loadDomain(temp_domains[i]);
			rootNode.add(domain_ele);
		}// End for(data domains)

		// Put models into trees.
		browseTree.setModel(model);
		browseTree.setRootVisible(false);
		// Set selection policy (one at a time) and add listeners
		browseTree.getSelectionModel().setSelectionMode(
									TreeSelectionModel.SINGLE_TREE_SELECTION);
	}
	
	private DefaultMutableTreeNode loadDomain(DataDomain temp_domain)  {
		DefaultMutableTreeNode domain_ele;
		domain_ele = new DefaultMutableTreeNode(
							new DataDomainWrapper(temp_domain), true);
		List map_list = temp_domain.getMapList();
		Iterator map_iter = map_list.iterator();
		while(map_iter.hasNext()) {
			DefaultMutableTreeNode map_ele;
			map_ele = loadMap((DataMap)map_iter.next());
			domain_ele.add(map_ele);
		}// End maps
		DataNode[] nodes = temp_domain.getDataNodes();
		for (int node_count = 0; node_count < nodes.length; node_count++) {
			DefaultMutableTreeNode node_ele = loadNode(nodes[node_count]);
			domain_ele.add(node_ele);
		}// End for(data nodes)
		return domain_ele;
	}
	
	private DefaultMutableTreeNode loadMap(DataMap map) {
		DefaultMutableTreeNode map_ele;
		DataMapWrapper map_wrap = new DataMapWrapper(map);
		map_ele = new DefaultMutableTreeNode(map_wrap, true);
		List obj_entities = map.getObjEntitiesAsList();
		Iterator obj_iter = obj_entities.iterator();
		while(obj_iter.hasNext()) {
			Entity entity = (Entity)obj_iter.next();
			EntityWrapper obj_entity_wrap = new EntityWrapper(entity);
			DefaultMutableTreeNode obj_entity_ele;
			obj_entity_ele = new DefaultMutableTreeNode(obj_entity_wrap, false);
			map_ele.add(obj_entity_ele);
		}// End obj entities
		List db_entities = map.getDbEntitiesAsList();
		Iterator db_iter = db_entities.iterator();
		while(db_iter.hasNext()) {
			Entity entity = (Entity)db_iter.next();
			EntityWrapper db_entity_wrap = new EntityWrapper(entity);
			DefaultMutableTreeNode db_entity_ele;
			db_entity_ele = new DefaultMutableTreeNode(db_entity_wrap, false);
			map_ele.add(db_entity_ele);
		}// End db entities
		return map_ele;
	}
	
	private DefaultMutableTreeNode loadNode(DataNode node) {
		DefaultMutableTreeNode node_ele;
		DataNodeWrapper node_wrap = new DataNodeWrapper(node);
		node_ele = new DefaultMutableTreeNode(node_wrap, true);
		DataMap[] maps = node.getDataMaps();
		for (int j = 0; j < maps.length; j++) {
			DefaultMutableTreeNode map_ele = loadMap(maps[j]);
			node_ele.add(map_ele);
		}// End for(maps)
		return node_ele;
	}
	
	public void currentDomainChanged(DomainDisplayEvent e) {
		if (e.getSource() == this)
			return;
		DefaultMutableTreeNode temp;
		temp = getDomainNode(e.getDomain());
		if (null == temp)
			return;
		showNode(temp);
		
	}
	public void currentDataNodeChanged(DataNodeDisplayEvent e) {
		if (e.getSource() == this)
			return;
		DefaultMutableTreeNode temp;
		temp = getDataSourceNode(e.getDomain(), e.getDataNode());
		if (null == temp)
			return;
		showNode(temp);
	}
	public void currentDataMapChanged(DataMapDisplayEvent e) {
		if (e.getSource() == this)
			return;
		DefaultMutableTreeNode temp;
		temp = getMapNode(e.getDomain(), e.getDataMap());
		if (null == temp)
			return;
		showNode(temp);
	}

	public void currentObjEntityChanged(EntityDisplayEvent e){
		currentEntityChanged(e);
	}
	
	public void currentDbEntityChanged(EntityDisplayEvent e){
		currentEntityChanged(e);
	}

	private void currentEntityChanged(EntityDisplayEvent e) {
		if (e.getSource() == this)
			return;
		DefaultMutableTreeNode temp;
		temp = getEntityNode(e.getDomain(), e.getDataMap(), e.getEntity());
		if (null == temp)
			return;
		showNode(temp);
	}
	
	public void domainChanged(DomainEvent e) {
		if (e.getSource() == this)
			return;
		DefaultMutableTreeNode node;
		node = getDomainNode(e.getDomain());
		if (null != node)
			model.nodeChanged(node);
	}
	public void domainAdded(DomainEvent e) {
		if (e.getSource() == this)
			return;
		DefaultMutableTreeNode node;
		node = loadDomain(e.getDomain());
		model.insertNodeInto(node, rootNode, rootNode.getChildCount());
	}
	public void domainRemoved(DomainEvent e) {
		if (e.getSource() == this)
			return;
		DefaultMutableTreeNode node;
		node = getDomainNode(e.getDomain());
		if (null != node)
			model.removeNodeFromParent(node);
	}
	public void dataNodeChanged(DataNodeEvent e) {
		if (e.getSource() == this)
			return;
		DefaultMutableTreeNode node;
		node = getDataSourceNode(mediator.getCurrentDataDomain(), e.getDataNode());
		if (null != node) {
			model.nodeChanged(node);
			// If assigned map to this node
			DataMap[] maps = e.getDataNode().getDataMaps();
			if (maps.length != node.getChildCount()) {
				// Find map not already under node and add it
				for (int i = 0; i < maps.length; i++) {
					boolean found = false;
					for (int j = 0; j < node.getChildCount(); j++) {
						DefaultMutableTreeNode child;
						child = (DefaultMutableTreeNode)node.getChildAt(j);
						DataMapWrapper wrap;
						wrap = (DataMapWrapper)child.getUserObject();
						if (maps[i] == wrap.getDataMap()) {
							found = true;
							break;
						}
					}// End for(j)
					if (false == found) {
						DefaultMutableTreeNode map_ele = loadMap(maps[i]);
						node.add(map_ele);
						break;
					}
				}// End for(i)
			}// End if number of maps changed
		}// End if node found
	}
	public void dataNodeAdded(DataNodeEvent e) {
		if (e.getSource() == this)
			return;
		DefaultMutableTreeNode parent;
		parent = getDomainNode(mediator.getCurrentDataDomain());
		if (null == parent)
			return;
		DefaultMutableTreeNode node;
		node = loadNode(e.getDataNode());
		model.insertNodeInto(node, parent, parent.getChildCount());
	}
	public void dataNodeRemoved(DataNodeEvent e) {
		if (e.getSource() == this)
			return;
		DefaultMutableTreeNode node;
		node = getDataSourceNode(mediator.getCurrentDataDomain(), e.getDataNode());
		if (null != node)
			model.removeNodeFromParent(node);
	}
	
	public void dataMapChanged(DataMapEvent e) {
		if (e.getSource() == this)
			return;
		DefaultMutableTreeNode node;
		node = getMapNode(mediator.getCurrentDataDomain(), e.getDataMap());
		if (null != node)
			model.nodeChanged(node);
	}
	
	public void dataMapAdded(DataMapEvent e) {
		if (e.getSource() == this)
			return;
		DefaultMutableTreeNode parent;
		parent = getDomainNode(mediator.getCurrentDataDomain());
		if (null == parent)
			return;
		DefaultMutableTreeNode node;
		node = loadMap(e.getDataMap());
		model.insertNodeInto(node, parent, parent.getChildCount());
	}
	public void dataMapRemoved(DataMapEvent e) {
		if (e.getSource() == this)
			return;
		DefaultMutableTreeNode node;
		DataMap map = e.getDataMap();
		DataDomain domain = mediator.getCurrentDataDomain();
		node = getMapNode(domain, map);
		if (null != node)
			model.removeNodeFromParent(node);
		// Clean up map from the nodes
		DataNode[] nodes = domain.getDataNodes();
		for (int i = 0; i < nodes.length; i++) {
			DataMap[] maps = nodes[i].getDataMaps();
			for (int j = 0; j < maps.length; j++) {
				if (maps[j] == map) {
					node = getMapNode(domain, nodes[i], map);
					if (null != node) 
						model.removeNodeFromParent(node);
				}// End if found the map under node
			}// End for(maps in the node)
		}// End for(nodes in current domain)
	}
	
	private ArrayList getNodesWithMap(DataMap map) {
		return null;
	}
	
	public void objEntityChanged(EntityEvent e)
	{ entityChanged(e);}
	public void objEntityAdded(EntityEvent e)
	{ entityAdded(e); }
	public void objEntityRemoved(EntityEvent e){}

	public void dbEntityChanged(EntityEvent e)
	{ entityChanged(e);}
	public void dbEntityAdded(EntityEvent e)
	{ entityAdded(e); }
	public void dbEntityRemoved(EntityEvent e){}

	/** Makes Entity is visible and selected.<br>
	 *  - If entity is from the current node, refresh the node making sure 
	 *  changes in the entity name are reflected. <br>
	 *  - If entity is in another node, make that node visible and selected.<br>
	 */
	public void entityChanged(EntityEvent e) {
		if (e.getSource() == this)
			return;
		DefaultMutableTreeNode temp;
		temp = getEntityNode(mediator.getCurrentDataDomain()
							, mediator.getCurrentDataMap()
							, e.getEntity());
		if (null != temp)
			model.nodeChanged(temp);
		temp = getEntityNode(mediator.getCurrentDataDomain()
							, mediator.getCurrentDataNode()
							, mediator.getCurrentDataMap()
							, e.getEntity());
		if (null != temp)
			model.nodeChanged(temp);
	}

	/** Add the node for the entity and make it selected. */
	public void entityAdded(EntityEvent e) {
		if (e.getSource() == this)
			return;
		Entity entity = e.getEntity();
		// Add a node and make it selected.
		EntityWrapper wrapper = new EntityWrapper(entity);
		DefaultMutableTreeNode map_node;
		if (mediator.getCurrentDataNode() != null) {
			map_node = getMapNode(mediator.getCurrentDataDomain()
								, mediator.getCurrentDataNode()
								, mediator.getCurrentDataMap());
			if (map_node != null) {
				currentNode = new DefaultMutableTreeNode(wrapper, false);
				model.insertNodeInto(currentNode, map_node
									, map_node.getChildCount());
			}
		}
		map_node = getMapNode(mediator.getCurrentDataDomain()
							, mediator.getCurrentDataMap());
		currentNode = new DefaultMutableTreeNode(wrapper, false);
		model.insertNodeInto(currentNode, map_node, map_node.getChildCount());
		showNode(currentNode);
	}

	public void entityRemoved(EntityEvent e) {
		if (e.getSource() == this)
			return;
	}


	
	/** Get domain node by DataDomain. */
	private DefaultMutableTreeNode getDomainNode(DataDomain domain) {
		if (null == domain )
			return null;
		Enumeration domains = rootNode.children();
		while (domains.hasMoreElements()) {
			DefaultMutableTreeNode temp_node;
			temp_node = (DefaultMutableTreeNode)domains.nextElement();
			DataDomainWrapper wrap = (DataDomainWrapper)temp_node.getUserObject();
			if (wrap.getDataDomain() == domain)
				return temp_node;
		}
		return null;
	}

	/** Get map node by DataDomain and DataMap. */
	private DefaultMutableTreeNode getMapNode(DataDomain domain, DataMap map) {
		if (null == map )
			return null;
		DefaultMutableTreeNode domain_node = getDomainNode(domain);
		if (null == domain_node)
			return null;
		Enumeration maps = domain_node.children();
		while (maps.hasMoreElements()) {
			DefaultMutableTreeNode temp_node;
			temp_node = (DefaultMutableTreeNode)maps.nextElement();
			DataMapWrapper wrap = (DataMapWrapper)temp_node.getUserObject();
			if (wrap.getDataMap() == map)
				return temp_node;
		}
		return null;
	}

	
	/** Get data source (a.k.a. data node) node by DataDomain and DataNode. */
	private DefaultMutableTreeNode getDataSourceNode(DataDomain domain, DataNode data) {
		if (null == data )
			return null;
		DefaultMutableTreeNode domain_node = getDomainNode(domain);
		if (null == domain_node)
			return null;
		Enumeration data_sources = domain_node.children();
		while (data_sources.hasMoreElements()) {
			DefaultMutableTreeNode temp_node;
			temp_node = (DefaultMutableTreeNode)data_sources.nextElement();
			DataNodeWrapper wrap = (DataNodeWrapper)temp_node.getUserObject();
			if (wrap.getDataNode() == data)
				return temp_node;
		}
		return null;
	}

	
	/** Get map node by DataDomain, DataNode and DataMap. */
	private DefaultMutableTreeNode getMapNode(DataDomain domain, DataNode data, DataMap map) {
		if (null == map )
			return null;
		DefaultMutableTreeNode data_node = getDataSourceNode(domain, data);
		if (null == data_node)
			return null;
		Enumeration maps = data_node.children();
		while (maps.hasMoreElements()) {
			DefaultMutableTreeNode temp_node;
			temp_node = (DefaultMutableTreeNode)maps.nextElement();
			DataMapWrapper wrap = (DataMapWrapper)temp_node.getUserObject();
			if (wrap.getDataMap() == map)
				return temp_node;
		}
		return null;
	}

	
	/** Get entity node by DataDomain, DataMap and Entity. */
	private DefaultMutableTreeNode getEntityNode(DataDomain domain, DataMap map, Entity entity) {
		if (null == entity )
			return null;
		DefaultMutableTreeNode map_node = getMapNode(domain, map);
		if (null == map_node)
			return null;
		Enumeration entities = map_node.children();
		while (entities.hasMoreElements()) {
			DefaultMutableTreeNode temp_node;
			temp_node = (DefaultMutableTreeNode)entities.nextElement();
			EntityWrapper wrap = (EntityWrapper)temp_node.getUserObject();
			if (wrap.getEntity() == entity)
				return temp_node;
		}
		return null;
	}

	
	/** Get entity node by DataDomain, DataNode, DataMap and Entity. */
	private DefaultMutableTreeNode getEntityNode(DataDomain domain, DataNode data
	, DataMap map, Entity entity) {
		if (null == entity )
			return null;
		DefaultMutableTreeNode map_node = getMapNode(domain, data, map);
		if (null == map_node)
			return null;
		Enumeration entities = map_node.children();
		while (entities.hasMoreElements()) {
			DefaultMutableTreeNode temp_node;
			temp_node = (DefaultMutableTreeNode)entities.nextElement();
			EntityWrapper wrap = (EntityWrapper)temp_node.getUserObject();
			if (wrap.getEntity() == entity)
				return temp_node;
		}
		return null;
	}


	/** Makes node current, visible and selected.*/
	private void showNode(DefaultMutableTreeNode node) {
		currentNode = node;
		TreePath path = new TreePath(currentNode.getPath());
		browseTree.scrollPathToVisible(path);
		browseTree.setSelectionPath(path);
	}
	
	/** Called when different node is selected. 
	  * Changes current node and causes reloading of the corresponding
	  * detail view by calling fireEntityDisplayEvent(). */
	public void valueChanged(TreeSelectionEvent e) {
	    TreePath path = e.getNewLeadSelectionPath();
	    if (path==null) 
	    	return;
	    	        
	    currentNode = (DefaultMutableTreeNode)path.getLastPathComponent();
	    Object[] data = getUserObjects(currentNode);
	    if (data.length == 0)
	    	return;
	    Object obj = data[data.length-1];
	    if (obj instanceof DataDomain) {
	    	mediator.fireDomainDisplayEvent(new DomainDisplayEvent(this, (DataDomain)obj));
	    }
	    else if (obj instanceof DataMap) {
	    	if (data.length == 3) {
	    		mediator.fireDataMapDisplayEvent(
	    								new DataMapDisplayEvent(this
	    								    , (DataMap)obj
	    								    , (DataDomain)data[data.length-3]
	    								    , (DataNode)data[data.length-2]));
	    	} else if (data.length == 2){
	    		mediator.fireDataMapDisplayEvent(
	    								new DataMapDisplayEvent(this
	    								    , (DataMap)obj
	    								    , (DataDomain)data[data.length-2]));
	    	}
	    } else if (obj instanceof ObjEntity) {
	    	if (data.length == 4) {
	    		mediator.fireObjEntityDisplayEvent(
	    								new EntityDisplayEvent(this
	    								    , (Entity)obj
	    								    , (DataMap)data[data.length-2]
	    								    , (DataDomain)data[data.length-4]
	    								    , (DataNode)data[data.length-3]));
	    	} else if (data.length == 3){
	    		mediator.fireObjEntityDisplayEvent(
	    								new EntityDisplayEvent(this
	    								    , (Entity)obj
	    								    , (DataMap)data[data.length-2]
	    								    , (DataDomain)data[data.length-3]));
	    	}
	    } else if (obj instanceof DbEntity) {
	    	if (data.length == 4) {
	    		mediator.fireDbEntityDisplayEvent(
	    								new EntityDisplayEvent(this
	    								    , (Entity)obj
	    								    , (DataMap)data[data.length-2]
	    								    , (DataDomain)data[data.length-4]
	    								    , (DataNode)data[data.length-3]));
	    	} else if (data.length == 3){
	    		mediator.fireDbEntityDisplayEvent(
	    								new EntityDisplayEvent(this
	    								    , (Entity)obj
	    								    , (DataMap)data[data.length-2]
	    								    , (DataDomain)data[data.length-3]));
	    	}
	    }
	} // End valueChanged()
	
	/** Gets array of the user objects ending with this and starting with one under root. 
	  * That is the array of actual objects rather than wrappers.*/
	private Object[] getUserObjects(DefaultMutableTreeNode node) {
		ArrayList list = new ArrayList();
		Object[] arr;
		while (!node.isRoot()) {
			Object obj = node.getUserObject();
			if (obj instanceof DataDomainWrapper)
				list.add(0, ((DataDomainWrapper)obj).getDataDomain());
			else if (obj instanceof DataNodeWrapper)
				list.add(0, ((DataNodeWrapper)obj).getDataNode());
			else if (obj instanceof DataMapWrapper)
				list.add(0, ((DataMapWrapper)obj).getDataMap() );
			else if (obj instanceof EntityWrapper)
				list.add(0, ((EntityWrapper)obj).getEntity());
			else throw new UnsupportedOperationException("Tree contains invalid wrapper");
			node = (DefaultMutableTreeNode)node.getParent();
		}// End while()
		return list.toArray();
	}
}


class BrowseViewRenderer extends DefaultTreeCellRenderer {
    ImageIcon domainIcon;
    ImageIcon nodeIcon;
    ImageIcon mapIcon;
    ImageIcon dbEntityIcon;
    ImageIcon objEntityIcon;

    public BrowseViewRenderer() {
    	String path = "org/objectstyle/cayenne/gui/";
    	File file = new File(path + "images/domain.jpg");
    	if (!file.exists()){
    		System.out.println("File "+file.getAbsolutePath()+" doesn't exist");
    	}
        domainIcon = new ImageIcon(path + "images/domain.jpg");
        nodeIcon = new ImageIcon(path + "images/node.jpg");
        mapIcon = new ImageIcon(path + "images/map.jpg");
        dbEntityIcon = new ImageIcon(path + "images/dbentity.jpg");
        objEntityIcon = new ImageIcon(path + "images/objentity.jpg");
    }

    public Component getTreeCellRendererComponent(
                        JTree tree,
                        Object value,
                        boolean sel,
                        boolean expanded,
                        boolean leaf,
                        int row,
                        boolean hasFocus) {

        super.getTreeCellRendererComponent(
                        tree, value, sel,
                        expanded, leaf, row,
                        hasFocus);
        DefaultMutableTreeNode node = (DefaultMutableTreeNode)value;
        if (node.isRoot())
        	return this;
        Object obj = node.getUserObject();
        if (obj instanceof DataDomainWrapper) {
            setIcon(domainIcon);
        } 
        else if (obj instanceof DataNodeWrapper) {
            setIcon(nodeIcon);
        } 
        else if (obj instanceof DataMapWrapper) {
            setIcon(mapIcon);
        } 
        else if (obj instanceof EntityWrapper) {
        	EntityWrapper wrap = (EntityWrapper)obj;
        	if (wrap.getEntity() instanceof DbEntity)
            	setIcon(dbEntityIcon);
            else if (wrap.getEntity() instanceof ObjEntity)
            	setIcon(objEntityIcon);
        } 
        return this;
    }
}
