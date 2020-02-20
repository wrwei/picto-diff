/*********************************************************************
* Copyright (c) 2008 The University of York.
*
* This program and the accompanying materials are made
* available under the terms of the Eclipse Public License 2.0
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* SPDX-License-Identifier: EPL-2.0
**********************************************************************/
package org.eclipse.epsilon.picto;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.swt.graphics.Point;

public class ViewTree {
	
	protected List<ViewTree> children = new ArrayList<>();
	protected ContentPromise promise;
	protected String name = "";
	protected String format = "html";
	protected String icon = "folder";
	protected ViewTree parent;
	protected Point scrollPosition = new Point(0, 0);
	protected String cachedContent = null;
	
	public static void main(String[] args) {
		
		ViewTree pathTree = new ViewTree("");
		pathTree.addPath(Arrays.asList("e1", "e2"), new StringContentPromise("c1"), "text", "");
		pathTree.addPath(Arrays.asList("e1", "e3", "e4"), new StringContentPromise("c2"), "text", "");
		ViewTree result = pathTree.forPath(Arrays.asList("", "e1", "e3", "e4"));
		System.out.println(result.getPath());
		
	}
	
	public ViewTree() {}

	public ViewTree(String name) {
		this.name = name;
	}
	
	public ViewTree(String content, String format) {
		this.promise = new StringContentPromise(content);
		this.format = format;
	}
	
	public void addPath(List<String> path, ContentPromise promise, String format, String icon) {
		
		if (path.size() > 1) {
			String name = path.get(0);
			List<String> rest = path.subList(1, path.size());
			ViewTree child = null;
			for (ViewTree candidate : children) {
				if (candidate.getName().equals(name)) {
					child = candidate;
				}
			}
			
			if (child == null) {
				child = new ViewTree(name);
				children.add(child);
			}
			
			child.addPath(rest, promise, format, icon);
		}
		else if (path.size() == 1) {
			ViewTree child = null;
			for (ViewTree candidate : children) {
				if (candidate.getName() != null && candidate.getName().equals(path.get(0))) {
					child = candidate;
				}
			}
			
			if (child == null) {
				child = new ViewTree(path.get(0));
				children.add(child);
			}
			
			child.setFormat(format);
			child.setPromise(promise);
			child.setIcon(icon);
		}
	}
	
	public void ingest(ViewTree other) {
		
		List<ViewTree> obsolete = new ArrayList<>();
		List<ViewTree> fresh = new ArrayList<>(other.getChildren());
		
		for (ViewTree child : getChildren()) {
			ViewTree counterpart = null;
			for (ViewTree otherChild : other.getChildren()) {
				if (child.getName().equals(otherChild.getName())) {
					counterpart = otherChild;
					fresh.remove(counterpart);
					child.setPromise(counterpart.getPromise());
					child.setFormat(counterpart.getFormat());
					child.setIcon(counterpart.getIcon());
					child.ingest(counterpart);
				}
			}
			if (counterpart == null) {
				obsolete.add(child);
			}
		}
		
		getChildren().removeAll(obsolete);
		getChildren().addAll(fresh);
		
	}
	
	public String getName() {
		return name;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public List<ViewTree> getChildren() {
		children.stream().forEach(c -> c.setParent(this));
		return children;
	}
	
	public void setParent(ViewTree parent) {
		this.parent = parent;
	}
	
	public ViewTree getParent() {
		return parent;
	}
	
	public String getContent() {
		
		if (cachedContent == null) {
			
			if (promise == null) {
				cachedContent = "";
			}
			else {
				try {
					cachedContent = promise.getContent();
				} catch (Exception e) {
					throw new RuntimeException(e);
				}
			}
		}
		
		return cachedContent;
	}
	
	public void setPromise(ContentPromise promise) {
		if (promise != this.promise) {
			this.promise = promise;
			cachedContent = null;
		}
	}
	
	public ContentPromise getPromise() {
		return promise;
	}
	
	public String getFormat() {
		return format;
	}
	
	public void setFormat(String format) {
		this.format = format;
	}
	
	public void setIcon(String icon) {
		this.icon = icon;
	}
	
	public String getIcon() {
		return icon;
	}
	
	public Point getScrollPosition() {
		return scrollPosition;
	}
	
	public void setScrollPosition(Point scrollPosition) {
		this.scrollPosition = scrollPosition;
	}

	@Override
	public String toString() {
		return super.toString();
		//return name + " { content: " + content + " [" + children.stream().map( n -> n.toString() ) .collect( Collectors.joining( "," ) ) + "]";
	}
	
	public List<String> getPath() {
		List<String> path = new ArrayList<>();
		if (parent != null) path.addAll(parent.getPath());
		path.add(this.getName() + "");
		return path;
	}
	
	public ViewTree forPath(List<String> path) {
		if (path.get(0).equals(getName())) {
			if (path.size() > 1) {
				for (ViewTree child : getChildren()) {
					ViewTree forPath = child.forPath(path.subList(1, path.size()));
					if (forPath != null) return forPath;
				}
			}
			else {
				return this;
			}
		}
		
		return null;
	}
	
	public ViewTree getFirstWithContent() {
		if (promise != null) return this;
		for (ViewTree child : getChildren()) {
			ViewTree result = child.getFirstWithContent();
			if (result != null) return result;
		}
		return null;
	}
	
}
