package org.eclipse.epsilon.picto.diff.util;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;

import guru.nidi.graphviz.model.Link;
import guru.nidi.graphviz.model.MutableGraph;
import guru.nidi.graphviz.model.MutableNode;
import guru.nidi.graphviz.parse.Parser;

public class DotComparator {

	protected InputStream ss;
	protected InputStream ts;
	protected String sourceFile;
	protected String targetFile;
	protected MutableGraph sg;
	protected MutableGraph tg;
	
	protected MutableGraph cg;
	protected ArrayList<MutableNode> addedNodes = new ArrayList<MutableNode>();
	protected ArrayList<MutableNode> removedNodes = new ArrayList<MutableNode>();
	protected ArrayList<MutableNode> changedNodes = new ArrayList<MutableNode>();
	
	protected ArrayList<Link> addedLinks = new ArrayList<Link>();
	protected ArrayList<Link> removedLinks = new ArrayList<Link>();
	protected ArrayList<Link> changedLinks = new ArrayList<Link>();

	
	protected HashMap<MutableNode, ArrayList<Entry<String, Object>>> addedAttributes = new HashMap<MutableNode, ArrayList<Entry<String, Object>>>();
	protected HashMap<MutableNode, ArrayList<Entry<String, Object>>> removedAttributes = new HashMap<MutableNode, ArrayList<Entry<String, Object>>>();
	protected HashMap<MutableNode, ArrayList<Entry<String, Object>>> changedAttributes = new HashMap<MutableNode, ArrayList<Entry<String, Object>>>();
	
	protected MutableNode cache_node;
	protected Link cache_link;
	
	public DotComparator(String sourceFile, String targetFile) {
		this.sourceFile = sourceFile;
		this.targetFile = targetFile;
	}
	
	public static void main(String[] args) {
		
	}
	
	public boolean initialise() {
		try {
			ss = new FileInputStream(sourceFile);
			sg = new Parser().read(ss);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}
		try {
			ts = new FileInputStream(targetFile);
			tg = new Parser().read(ss);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}
		cg = sg.copy();
		cg.nodes().clear();
		return true;
	}
	
	public void compare() {
		for(MutableNode n: sg.nodes()) {
			compareNode(n);
		}
	}
	
	public void compareNode(MutableNode node) {
		MutableNode correspond = findNode(node.name().value());
		//do removed
		if (correspond == null) {
			removedNodes.add(node);
		}
		else {
			//do changed
			for(Entry<String, Object> attr: node.attrs()) {
				compareAttribute(node, correspond, attr);
			}
		}
	}
	
	
	
	public void compareAttribute(MutableNode s, MutableNode t, Entry<String, Object> attr) {
		Entry<String, Object> correspond = findAttribute(t, attr.getKey());
		//removed
		if (correspond == null) {
			if (removedAttributes.containsKey(s)) {
				ArrayList<Entry<String, Object>> attrs = removedAttributes.get(s);
				attrs.add(correspond);
			}
			else {
				ArrayList<Entry<String, Object>> attrs = new ArrayList<Entry<String,Object>>();
				attrs.add(correspond);
				removedAttributes.put(s, attrs);
			}
		}
		else {
			//unchanged
			if (attr.getValue().toString().equals(correspond.getValue().toString())) {
				
			}
			else {
				if (changedAttributes.containsKey(s)) {
					ArrayList<Entry<String, Object>> attrs = changedAttributes.get(s);
					attrs.add(correspond);
				}
				else {
					ArrayList<Entry<String, Object>> attrs = new ArrayList<Entry<String,Object>>();
					attrs.add(correspond);
					changedAttributes.put(s, attrs);
				}
			}
		}
	}
	
	public MutableNode findNode(String name) {
		for(MutableNode n: tg.nodes()) {
			if (n.name().value().equals(name)) {
				return n;
			}
		}
		return null;
	}
	
	public Entry<String, Object> findAttribute(MutableNode node, String key) {
		for(Entry<String, Object> attr: node.attrs()) {
			if (attr.getKey().equals(key)) {
				return attr;
			}
		}
		return null;
	}
	
	

}
