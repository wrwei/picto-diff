package org.eclipse.epsilon.picto.diff.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map.Entry;


import guru.nidi.graphviz.engine.Format;
import guru.nidi.graphviz.engine.Graphviz;
import guru.nidi.graphviz.model.Link;
import guru.nidi.graphviz.model.MutableGraph;
import guru.nidi.graphviz.model.MutableNode;
import guru.nidi.graphviz.model.PortNode;
import guru.nidi.graphviz.parse.Parser;

import static guru.nidi.graphviz.model.Factory.*;

public class DotComparator {

	protected InputStream ss;
	protected InputStream ts;
	protected String sourceFile;
	protected String targetFile;
	protected MutableGraph sg;
	protected MutableGraph tg;
	
	protected MutableGraph s_temp;
	protected MutableGraph t_temp;
	
	protected GraphValidator graphValidator = new GraphValidator();
	
	protected MutableGraph result;
	
	protected HashMap<MutableNode, HashSet<Link>> unchangedLinks = new HashMap<MutableNode, HashSet<Link>>();
	protected HashMap<MutableNode, HashSet<Link>> addedLinks = new HashMap<MutableNode, HashSet<Link>>();
	protected HashMap<MutableNode, HashSet<Link>> removedLinks = new HashMap<MutableNode, HashSet<Link>>();
	protected HashMap<MutableNode, HashSet<Link>> changedLinks = new HashMap<MutableNode, HashSet<Link>>();
	
	protected HashMap<MutableNode, HashSet<String>> addedAttrs = new HashMap<MutableNode, HashSet<String>>();
	protected HashMap<MutableNode, HashSet<String>> removedAttrs = new HashMap<MutableNode, HashSet<String>>();
	protected HashMap<MutableNode, HashSet<String>> changedAttrs = new HashMap<MutableNode, HashSet<String>>();
	
	protected MutableNode cache_node;
	protected Link cache_link;
	
	public DotComparator(String sourceFile, String targetFile) {
		this.sourceFile = sourceFile;
		this.targetFile = targetFile;
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
			tg = new Parser().read(ts);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}
//		if (!graphValidator.validate(sg) || !graphValidator.validate(tg)) {
//			return false;
//		}
		result = mutGraph();
		s_temp = mutGraph();
		s_temp.setDirected(true);
		s_temp.setName("left");
		s_temp.graphAttrs().add("label", "digraph");
		s_temp.setCluster(true);
		s_temp.addTo(result);
		
		t_temp = mutGraph();
		t_temp.setDirected(true);
		t_temp.setName("right");
		t_temp.graphAttrs().add("label", "digraph++");
		t_temp.setCluster(true);
		t_temp.addTo(result);
//		sg.copy().setCluster(true).addTo(result);
//		MutableGraph tg_copy = tg.copy();
//		IDUtil.prefixGraph(tg);
//		tg_copy.setCluster(true).addTo(result);
		return true;
	}
	
	public void compare() {
		for(MutableNode n: sg.nodes()) {
			compareNode(n);
		}
//		if (sg.nodes().size() > 0) {
//			for(MutableNode n: sg.nodes()) {
//				MutableNode l_node_copy = n.copy();
//				l_node_copy.links().clear();
//				GraphUtil.paintRed(l_node_copy);
//				addToTTemp(l_node_copy);
//			}
//		}
	}
	
	public void compareNode(MutableNode left_node) {
		//get counter part node
		MutableNode right_node = findNode(left_node.name().value());
		//if node exists
		if (right_node != null) {
			//compare all attributes
			//TODO: record each change in attr
			for(Entry<String, Object> attr: left_node.attrs()) {
				compareAttribute(left_node, right_node, attr);
			}
			
			if (getChangedAttrs(right_node).size() != 0) {
				for(String s: getChangedAttrs(right_node)) {
					if (s.equals("label")) {
						GraphUtil.paintLabelOrange(right_node);
					}
					else {
						GraphUtil.paintOrange(right_node);
					}
				}
				MutableNode l_node_copy = left_node.copy();
				l_node_copy.links().clear();
				addToSTemp(l_node_copy);
				
				MutableNode r_node_copy = right_node.copy();
				r_node_copy.links().clear();
				IDUtil.prefixNode(r_node_copy);
				addToTTemp(r_node_copy);
				GraphUtil.linkCrossCluster(result, l_node_copy.name().toString(), r_node_copy.name().toString());
			}
			
			
			//compare all links
			for(Link link: left_node.links()) {
				//find counter part
				Link correspond_l = findLink(right_node, link.attrs().get("name").toString());
				//if link exists
				if (correspond_l != null) {
					//if link has changed
					if (!compareLink(left_node, right_node, link, correspond_l)) {
						addChangedLink(left_node, link);
						addChangedLink(right_node, correspond_l);
					}
					else {
						addUnchangedLink(right_node, correspond_l);
					}
				}
				else {
					//add to removed links
					addRemovedLink(left_node, link);
				}
			}
			
			
			for(Link l: getChangedLinks(left_node))
			{
				MutableNode link_target = findLinkTarget(sg, l);
				MutableNode temp = link_target.copy();
				temp.links().clear();
				addToSTemp(temp);
				MutableNode link_source = findNodeInSTemp(left_node.name().toString());
				if (link_source == null) {
					link_source = left_node.copy();
					link_source.links().clear();
					addToSTemp(link_source);
				}
				Link link = link_source.linkTo(temp);
				link.attrs().add(l.copy());
				link_source.addLink(link);
				
				/////////////////////////////////////////////
				
//				MutableNode r_link_target = findLinkTarget(tg, l);
//				MutableNode r_temp = r_link_target.copy();
//				r_temp.links().clear();
//				IDUtil.prefixNode(r_temp);
//				addToTTemp(r_temp);
//				MutableNode r_link_source = findNodeInTTemp(IDUtil.getPrefix() + right_node.name().toString());
//				if (r_link_source == null) {
//					r_link_source = right_node.copy();
//					r_link_source.links().clear();
//					IDUtil.prefixNode(r_link_source);
//					addToTTemp(r_link_source);
//				}
//				
//				Link r_link = r_link_source.linkTo(r_temp);
//				r_link.attrs().add(l.copy());
//				GraphUtil.paintOrange(r_link);
//				r_link_source.addLink(r_link);
			}
			
			for(Link l: getChangedLinks(right_node))
			{
				MutableNode link_target = findLinkTarget(tg, l);
				MutableNode temp = link_target.copy();
				temp.links().clear();
				IDUtil.prefixNode(temp);
				addToTTemp(temp);
				MutableNode link_source = findNodeInTTemp(IDUtil.getPrefix() + right_node.name().toString());
				if (link_source == null) {
					link_source = right_node.copy();
					link_source.links().clear();
					IDUtil.prefixNode(link_source);
					addToTTemp(link_source);
				}
				
				Link link = link_source.linkTo(temp);
				link.attrs().add(l.copy());
				GraphUtil.paintOrange(link);
				link_source.addLink(link);
			}
			
			for(Link l: getRemovedLinks(left_node)) {
				MutableNode l_node_copy = left_node.copy();
				l_node_copy.links().clear();
				addToSTemp(l_node_copy);
				
				MutableNode l_link_target = findLinkTarget(sg, l);
				MutableNode l_temp = l_link_target.copy();
				l_temp.links().clear();
				addToSTemp(l_temp);
				MutableNode l_link_source = findNodeInSTemp(l_node_copy.name().toString());
				
				Link l_link = l_link_source.linkTo(l_temp);
				l_link.attrs().add(l.copy());
				l_link_source.addLink(l_link);
				
				//////////////////////////////////////////
				
				MutableNode r_node_copy = right_node.copy();
				r_node_copy.links().clear();
				IDUtil.prefixNode(r_node_copy);
				addToTTemp(r_node_copy);
				
				MutableNode r_link_target = findLinkTarget(tg, l);
				if (r_link_target != null) {
					MutableNode r_temp = r_link_target.copy();
					r_temp.links().clear();
					IDUtil.prefixNode(r_temp);
					addToTTemp(r_temp);
					MutableNode r_link_source = findNodeInTTemp(r_node_copy.name().toString());
					
					Link r_link = r_link_source.linkTo(r_temp);
					r_link.attrs().add(l.copy());
					GraphUtil.paintRed(r_link);
					r_link_source.addLink(r_link);
				}
				else {
					r_link_target = findLinkTarget(sg, l);
					MutableNode r_temp = r_link_target.copy();
					r_temp.links().clear();
					IDUtil.prefixNode(r_temp);
					GraphUtil.paintRed(r_temp);
					addToTTemp(r_temp);
					MutableNode r_link_source = findNodeInTTemp(r_node_copy.name().toString());
					
					Link r_link = r_link_source.linkTo(r_temp);
					r_link.attrs().add(l.copy());
					GraphUtil.paintRed(r_link);
					r_link_source.addLink(r_link);
				}
				
				
//				GraphUtil.linkCrossClusterNorm(result, l_node_copy.name().toString(), r_node_copy.name().toString());

			}
			
			MutableNode r_node_copy = right_node.copy();
			r_node_copy.links().removeAll(getChangedLinks(right_node));
			r_node_copy.links().removeAll(getUnchangedLinks(right_node));
			for(Link l: r_node_copy.links()) {
				MutableNode link_target = findLinkTarget(tg, l);
				MutableNode temp = link_target.copy();
				temp.links().clear();
				IDUtil.prefixNode(temp);
				addToTTemp(temp);
				MutableNode link_source = findNodeInTTemp(IDUtil.getPrefix() + right_node.name().toString());
				if (link_source == null) {
					link_source = right_node.copy();
					link_source.links().clear();
					IDUtil.prefixNode(link_source);
					addToTTemp(link_source);
				}
				
				Link link = link_source.linkTo(temp);
				link.attrs().add(l.copy());
				GraphUtil.paintGreen(link);
				link_source.addLink(link);
			}		
			
//			if (correspond_n.links().removeAll(getChangedLinks(correspond_n))) {
//				if (correspond_n.links().size() > 0) {
//					for(Link l: correspond_n.links()) {
//						addAddedLink(correspond_n, l);
//					}
//				}
//			}
			
			sg.rootNodes().remove(left_node);
			tg.rootNodes().remove(right_node);
		}
	}
	
	public MutableNode findNodeInSTemp(String name) {
		for(MutableNode n: s_temp.rootNodes()) {
			if (n.name().toString().equals(name)) {
				return n;
			}
		}
		return null;
	}
	
	public MutableNode findNodeInTTemp(String name) {
		for(MutableNode n: t_temp.rootNodes()) {
			if (n.name().toString().equals(name)) {
				return n;
			}
		}
		return null;
	}
	
	public void addToSTemp(MutableNode node) {
		if (!s_temp.rootNodes().contains(node)) {
			s_temp.rootNodes().add(node);
		}
	}
	
	public void addToTTemp(MutableNode node) {
		if (!t_temp.rootNodes().contains(node)) {
			t_temp.rootNodes().add(node);
		}
	}
	
	public MutableNode findLinkTarget(MutableGraph graph, Link link) {
		String name = "";
		if (link.to() instanceof PortNode) {
			PortNode portNode = (PortNode) link.to();
			name = portNode.name().toString();
		}
		else if (link.to() instanceof MutableNode) {
			MutableNode mutableNode = (MutableNode) link.to();
			name = mutableNode.name().toString();
		}
		for(MutableNode node: graph.nodes()) {
			if (node.name().toString().equals(name)) {
				return node;
			}
		}
		return null;
	}
	
	
	public boolean compareLink(MutableNode ln, MutableNode rn, Link s, Link t) {
		boolean result = true;
		if (!s.attrs().get("label").toString().equals(t.attrs().get("label"))) {
			result = false;
		}
		if (s.to() instanceof PortNode) {
			PortNode s_temp = (PortNode) s.to();
			if (t.to() instanceof PortNode) {
				PortNode t_temp = (PortNode) t.to();
				if (!s_temp.name().toString().equals(t_temp.name().toString())) {
					result = false;
				}
			}
			else {
				result = false;
			}
		}
		else if (s.to() instanceof MutableNode) {
			MutableNode s_temp = (MutableNode) s.to();
			if (t.to() instanceof MutableNode) {
				MutableNode t_temp = (MutableNode) t.to();
				if (!s_temp.name().toString().equals(t_temp.name().toString())) {
					result = false;
				}
			}
			else {
				result = false;
			}
		}
		return result;
	}
	
	public void compareAttribute(MutableNode s, MutableNode t, Entry<String, Object> attr) {
		//find counter part attr
		Entry<String, Object> correspond = findAttribute(t, attr.getKey());
		
		//if attr does not exhist
		if (correspond == null) {
			//TODO: deal with removed attribute
			//addRemovedAttr(s, attr.getKey());
		}
		else {
			//if attrs are same
			if (attr.getValue().toString().equals(correspond.getValue().toString())) {
			}
			else {
				addChangedAttr(t, correspond.getKey());
				addChangedAttr(s, attr.getKey());
			}
		}
	}
	
	public Link findLink(MutableNode n, String name) {
		for(Link l: n.links()) {
			if (l.attrs().get("name").toString().equals(name)) {
				return l;
			}
		}
		return null;
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
	
	public void serialise() throws IOException {
		
	    Graphviz.fromGraph(result).width(700).render(Format.PNG).toFile(new File("example/result.png"));
	    Graphviz.fromGraph(result).render(Format.DOT).toFile(new File("files/result.dot"));

	}
	
	public void addUnchangedLink(MutableNode node, Link link) {
		HashSet<Link> links = unchangedLinks.get(node);
		if (links == null) {
			links = new HashSet<Link>();
			links.add(link);
			unchangedLinks.put(node, links);
		}
		else {
			links.add(link);	
		}
	}
	
	public void addChangedLink(MutableNode node, Link link) {
		HashSet<Link> links = changedLinks.get(node);
		if (links == null) {
			links = new HashSet<Link>();
			links.add(link);
			changedLinks.put(node, links);
		}
		else {
			links.add(link);	
		}
	}
	
	public void addAddedLink(MutableNode node, Link link) {
		HashSet<Link> links = addedLinks.get(node);
		if (links == null) {
			links = new HashSet<Link>();
			links.add(link);
			addedLinks.put(node, links);
		}
		else {
			links.add(link);
		}
	}
	
	public void addRemovedLink(MutableNode node, Link link) {
		HashSet<Link> links = removedLinks.get(node);
		if (links == null) {
			links = new HashSet<Link>();
			links.add(link);
			removedLinks.put(node, links);
		}
		else {
			links.add(link);
		}
	}
	
	public HashSet<Link> getUnchangedLinks(MutableNode node) {
		HashSet<Link> links = unchangedLinks.get(node);
		if (links == null) {
			links = new HashSet<Link>();
		}
		return links;
	}
	
	public HashSet<Link> getChangedLinks(MutableNode node) {
		HashSet<Link> links = changedLinks.get(node);
		if (links == null) {
			links = new HashSet<Link>();
		}
		return links;
	}
	
	public HashSet<Link> getAddedLinks(MutableNode node) {
		HashSet<Link> links = addedLinks.get(node);
		if (links == null) {
			links = new HashSet<Link>();
		}
		return links;
	}
	
	public HashSet<Link> getRemovedLinks(MutableNode node) {
		HashSet<Link> links = removedLinks.get(node);
		if (links == null) {
			links = new HashSet<Link>();
		}
		return links;
	}
	
	public void addChangedAttr(MutableNode node, String attr) {
		HashSet<String> attrs = changedAttrs.get(node);
		if (attrs == null) {
			attrs = new HashSet<String>();
			attrs.add(attr);
			changedAttrs.put(node, attrs);
		}
		else {
			attrs.add(attr);
		}
	}
	
	public void addRemovedAttr(MutableNode node, String attr) {
		HashSet<String> attrs = removedAttrs.get(node);
		if (attrs == null) {
			attrs = new HashSet<String>();
			attrs.add(attr);
			removedAttrs.put(node, attrs);
		}
		else {
			attrs.add(attr);
		}
	}
	
	public void addAddedAttr(MutableNode node, String attr) {
		HashSet<String> attrs = addedAttrs.get(node);
		if (attrs == null) {
			attrs = new HashSet<String>();
			attrs.add(attr);
			addedAttrs.put(node, attrs);
		}
		else {
			attrs.add(attr);
		}
	}
	
	public HashSet<String> getChangedAttrs(MutableNode node) {
		HashSet<String> attrs = changedAttrs.get(node);
		if (attrs == null) {
			attrs = new HashSet<String>();
		}
		return attrs;
	}
	
	public HashSet<String> getAddedAttrs(MutableNode node) {
		HashSet<String> attrs = addedAttrs.get(node);
		if (attrs == null) {
			attrs = new HashSet<String>();
		}
		return attrs;
	}
	
	public HashSet<String> getRemovedAttrs(MutableNode node) {
		HashSet<String> attrs = removedAttrs.get(node);
		if (attrs == null) {
			attrs = new HashSet<String>();
		}
		return attrs;
	}
	
	
	public static void main(String[] args) throws IOException {
	    DotComparator dotComparator = new DotComparator("files/foo.dot", "files/foo2.dot");
	    dotComparator.initialise();
	    dotComparator.compare();
	    dotComparator.serialise();
	    
		/*
		 * Pseudo code:
		 * For each node {
		 * 		Find added;
		 * 		Find changed;
		 * 		Find removed;
		 * }
		 */
	}

}
