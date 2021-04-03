/*******************************************************************************
 * Copyright (C) 2018 OTK Software
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * 
 * The license allows developers and companies to use and integrate a software 
 * component released under the LGPL into their own (even proprietary) software 
 * without being required by the terms of a strong copyleft license to release the 
 * source code of their own components. However, any developer who modifies 
 * an LGPL-covered component is required to make their modified version 
 * available under the same LGPL license. For proprietary software, code under 
 * the LGPL is usually used in the form of a shared library, so that there is a clear 
 * separation between the proprietary and LGPL components.
 * 
 * The GNU Lesser General Public License allows you also to freely redistribute the 
 * libraries under the same license, if you provide the terms of the GNU Lesser 
 * General Public License with them and add the following copyright notice at the 
 * appropriate place (with a link to http://javacollection.net/reflectionui/ web site 
 * when possible).
 ******************************************************************************/
package xy.reflect.ui.util.component;
import java.util.Enumeration;
import java.util.List;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.MutableTreeNode;
import javax.swing.tree.TreeNode;

@SuppressWarnings("unchecked")
public abstract class AbstractLazyTreeNode extends DefaultMutableTreeNode {

	protected static final long serialVersionUID = 1L;
	protected boolean childrenLoaded = false;;
	protected abstract List<AbstractLazyTreeNode> createChildrenNodes();
	
	protected void ensureChildrenAreLoaded() {
		if (childrenLoaded) {
			return;
		}
		childrenLoaded = true;
		int i = 0;
		for (AbstractLazyTreeNode node: createChildrenNodes()) {
			super.insert(node, i); 
			i++;
		}
	}

	@Override
	public int getIndex(TreeNode aChild) {
		ensureChildrenAreLoaded();
		return super.getIndex(aChild);
	}

	@Override
	public TreeNode getChildAt(int index) {
		ensureChildrenAreLoaded();
		return super.getChildAt(index);
	}

	@Override
	public int getChildCount() {
		ensureChildrenAreLoaded();
		return super.getChildCount();
	}

	@Override
	public void insert(MutableTreeNode newChild, int childIndex) {
		ensureChildrenAreLoaded();
		super.insert(newChild, childIndex);
	}

	@Override
	public void remove(int childIndex) {
		ensureChildrenAreLoaded();
		super.remove(childIndex);
	}

	@Override
	public Enumeration<TreeNode> children() {
		ensureChildrenAreLoaded();
		return super.children();
	}

	@Override
	public void remove(MutableTreeNode aChild) {
		ensureChildrenAreLoaded();
		super.remove(aChild);
	}

	@Override
	public void add(MutableTreeNode newChild) {
		ensureChildrenAreLoaded();
		super.add(newChild);
	}

	@Override
	public boolean isNodeDescendant(DefaultMutableTreeNode anotherNode) {
		ensureChildrenAreLoaded();
		return super.isNodeDescendant(anotherNode);
	}

	@Override
	public boolean isNodeRelated(DefaultMutableTreeNode aNode) {
		ensureChildrenAreLoaded();
		return super.isNodeRelated(aNode);
	}

	@Override
	public int getDepth() {
		ensureChildrenAreLoaded();
		return super.getDepth();
	}

	@Override
	public DefaultMutableTreeNode getNextNode() {
		ensureChildrenAreLoaded();
		return super.getNextNode();
	}

	@Override
	public DefaultMutableTreeNode getPreviousNode() {
		ensureChildrenAreLoaded();
		return super.getPreviousNode();
	}

	@Override
	public Enumeration<TreeNode> preorderEnumeration() {
		ensureChildrenAreLoaded();
		return super.preorderEnumeration();
	}

	@Override
	public Enumeration<TreeNode> postorderEnumeration() {
		ensureChildrenAreLoaded();
		return super.postorderEnumeration();
	}

	@Override
	public Enumeration<TreeNode> breadthFirstEnumeration() {
		ensureChildrenAreLoaded();
		return super.breadthFirstEnumeration();
	}

	@Override
	public Enumeration<TreeNode> depthFirstEnumeration() {
		ensureChildrenAreLoaded();
		return super.depthFirstEnumeration();
	}

	@Override
	public boolean isNodeChild(TreeNode aNode) {
		ensureChildrenAreLoaded();
		return super.isNodeChild(aNode);
	}

	@Override
	public TreeNode getFirstChild() {
		ensureChildrenAreLoaded();
		return super.getFirstChild();
	}

	@Override
	public TreeNode getLastChild() {
		ensureChildrenAreLoaded();
		return super.getLastChild();
	}

	@Override
	public TreeNode getChildAfter(TreeNode aChild) {
		ensureChildrenAreLoaded();
		return super.getChildAfter(aChild);
	}

	@Override
	public TreeNode getChildBefore(TreeNode aChild) {
		ensureChildrenAreLoaded();
		return super.getChildBefore(aChild);
	}

	@Override
	public boolean isLeaf() {
		ensureChildrenAreLoaded();
		return super.isLeaf();
	}

	@Override
	public DefaultMutableTreeNode getFirstLeaf() {
		ensureChildrenAreLoaded();
		return super.getFirstLeaf();
	}

	@Override
	public DefaultMutableTreeNode getLastLeaf() {
		ensureChildrenAreLoaded();
		return super.getLastLeaf();
	}

	@Override
	public DefaultMutableTreeNode getNextLeaf() {
		ensureChildrenAreLoaded();
		return super.getNextLeaf();
	}

	@Override
	public DefaultMutableTreeNode getPreviousLeaf() {
		ensureChildrenAreLoaded();
		return super.getPreviousLeaf();
	}

	@Override
	public int getLeafCount() {
		ensureChildrenAreLoaded();
		return super.getLeafCount();
	}

	@Override
	public String toString() {
		ensureChildrenAreLoaded();
		return super.toString();
	}

	@Override
	public Object clone() {
		ensureChildrenAreLoaded();
		return super.clone();
	}

	@Override
	public int hashCode() {
		ensureChildrenAreLoaded();
		return super.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		ensureChildrenAreLoaded();
		return super.equals(obj);
	}

}
