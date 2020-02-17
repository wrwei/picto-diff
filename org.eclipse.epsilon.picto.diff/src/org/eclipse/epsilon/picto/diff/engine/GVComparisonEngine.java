package org.eclipse.epsilon.picto.diff.engine;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map.Entry;

import org.eclipse.epsilon.picto.diff.util.GraphUtil;
import org.eclipse.epsilon.picto.diff.util.GraphValidator;
import org.eclipse.epsilon.picto.diff.util.IDUtil;

import guru.nidi.graphviz.engine.Format;
import guru.nidi.graphviz.engine.Graphviz;
import guru.nidi.graphviz.model.Link;
import guru.nidi.graphviz.model.MutableGraph;
import guru.nidi.graphviz.model.MutableNode;
import guru.nidi.graphviz.model.PortNode;

import static guru.nidi.graphviz.model.Factory.*;

public class GVComparisonEngine {
	
	public enum DISPLAY_MODE {ALL, CHANGED};

	protected GVContext context = null;
	
	protected MutableGraph source_temp;
	protected MutableGraph target_temp;
	protected MutableGraph result;

	protected GraphValidator graphValidator = new GraphValidator();
	

	protected HashSet<MutableNode> changedNodes = new HashSet<MutableNode>();
	protected HashSet<MutableNode> unchangedNodes = new HashSet<MutableNode>();
	protected HashSet<MutableNode> added3dNodes = new HashSet<MutableNode>();
	protected HashSet<MutableNode> removedNodes = new HashSet<MutableNode>();
	
	protected HashMap<MutableNode, HashSet<Link>> unchangedLinks = new HashMap<MutableNode, HashSet<Link>>();
	protected HashMap<MutableNode, HashSet<Link>> addedLinks = new HashMap<MutableNode, HashSet<Link>>();
	protected HashMap<MutableNode, HashSet<Link>> removedLinks = new HashMap<MutableNode, HashSet<Link>>();
	protected HashMap<MutableNode, HashSet<Link>> changedLinks = new HashMap<MutableNode, HashSet<Link>>();
	
	protected HashMap<MutableNode, HashSet<String>> addedAttrs = new HashMap<MutableNode, HashSet<String>>();
	protected HashMap<MutableNode, HashSet<String>> removedAttrs = new HashMap<MutableNode, HashSet<String>>();
	protected HashMap<MutableNode, HashSet<String>> changedAttrs = new HashMap<MutableNode, HashSet<String>>();
	protected HashMap<MutableNode, HashSet<String>> unchangedAttrs = new HashMap<MutableNode, HashSet<String>>();
	
	public GVComparisonEngine(GVContext context, DISPLAY_MODE mode) {
		this.context = context;
	}
	
	public boolean load() {
		try {
			if (context.loadGraphs()) {
				result = mutGraph();
				result.setName("pdiff");
				source_temp = mutGraph();
				source_temp.setDirected(true);
				source_temp.setName("left");
				source_temp.graphAttrs().add("label", "digraph");
				source_temp.setCluster(true);
				source_temp.addTo(result);
				
				target_temp = mutGraph();
				target_temp.setDirected(true);
				target_temp.setName("right");
				target_temp.graphAttrs().add("label", "digraph++");
				target_temp.setCluster(true);
				target_temp.addTo(result);
			}
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}
	
	public void compare() {
		for(MutableNode n: getUnmutableSourceNodes()) {
			compareNode(n);
		}
		if (getUnmutableSourceNodes().size() != 0) {
			System.err.println("something is wrong");
		}
	}
	
	public void compareNode(MutableNode left_node) {
		//get counter part node
		MutableNode right_node = findNode(left_node.name().value());
		//if node exists
		if (right_node != null) {
			//TODO: record each change in attr
			
			//compare all attributes
			for(Entry<String, Object> attr: left_node.attrs()) {
				compareAttribute(left_node, right_node, attr);
			}
			
			//if there are changed attributes, change color of the right node
			if (getChangedAttrs(right_node).size() != 0) {
				//paint orange for changed attributes
				for(String s: getChangedAttrs(right_node)) {
					if (s.equals("label")) {
						GraphUtil.paintLabelOrange(right_node);
					}
					else {
						GraphUtil.paintOrange(right_node);
					}
				}
				/*
				 * The following is done because in this nidi3 library adding a node = adding all its links and the targets of the links recursively
				 * So a copy of the node is made 
				 */
				//left node copy
				MutableNode left_node_copy = left_node.copy();
				left_node_copy.links().clear();
				addToSourceTemp(left_node_copy);
				
				//right node copy
				MutableNode right_node_copy = right_node.copy();
				right_node_copy.links().clear();
				IDUtil.prefixNode(right_node_copy);
				addToTargetTemp(right_node_copy);
				
				//create a link between the two nodes
//				GraphUtil.linkCrossCluster(result, left_node_copy.name().toString(), right_node_copy.name().toString());
			}
			
			
			//compare all links of the left node
			for(Link left_link: left_node.links()) {
				//find counter part
				Link right_link = findLink(right_node, left_link.attrs().get("name").toString());
				//if link exists
				if (right_link != null) {
					//if link has changed
					if (!compareLink(left_node, right_node, left_link, right_link)) {
						addChangedLink(left_node, left_link); 
						addChangedLink(right_node, right_link);
					}
					else {
						//add unchanged link
						addUnchangedLink(right_node, right_link);
					}
				}
				else {
					//add to removed links
					addRemovedLink(left_node, left_link);
				}
			}
			
			//for all changed links for the left node
			for(Link changed_link: getChangedLinks(left_node))
			{
				//find the link target
				MutableNode link_target = findLinkTarget(context.getSourceGraph(), changed_link);
				//copy link target
				MutableNode temp = link_target.copy();
				//clear all the links
				temp.links().clear();
				//add to source
				addToSourceTemp(temp);
				//get link source
				MutableNode link_source = findNodeInSourceTemp(left_node.name().toString());
				//if cannot find link source, make a copy of the left node, add it to left
				if (link_source == null) {
					link_source = left_node.copy();
					link_source.links().clear();
					addToSourceTemp(link_source);
				}
				//establish link
				Link link = link_source.linkTo(temp);
				link.attrs().add(changed_link.copy());
				link_source.addLink(link);
			}
			
			
			//for all changed links for the right node
			for(Link changed_link: getChangedLinks(right_node))
			{
				//find target and addit to the right temp graph
				MutableNode link_target = findLinkTarget(context.getTargetGraph(), changed_link);
				MutableNode temp = link_target.copy();
				temp.links().clear();
				IDUtil.prefixNode(temp);
				addToTargetTemp(temp);
				//add the source to the right temp graph too
				MutableNode link_source = findNodeInTargetTemp(IDUtil.getPrefix() + right_node.name().toString());
				if (link_source == null) {
					link_source = right_node.copy();
					link_source.links().clear();
					IDUtil.prefixNode(link_source);
					addToTargetTemp(link_source);
				}
				
				Link link = link_source.linkTo(temp);
				link.attrs().add(changed_link.copy());
				GraphUtil.paintOrange(link);
				link_source.addLink(link);
			}
			
			//for all removed links
			for(Link removed_link: getRemovedLinks(left_node)) {
				/*
				 * deal with the left node
				 */
				MutableNode left_node_copy = left_node.copy();
				left_node_copy.links().clear();
				addToSourceTemp(left_node_copy);
				
				MutableNode left_link_target = findLinkTarget(context.getSourceGraph(), removed_link);
				MutableNode left_temp = left_link_target.copy();
				left_temp.links().clear();
				addToSourceTemp(left_temp);
				MutableNode left_link_source = findNodeInSourceTemp(left_node_copy.name().toString());
				
				Link left_link = left_link_source.linkTo(left_temp);
				left_link.attrs().add(removed_link.copy());
				left_link_source.addLink(left_link);
				
				//////////////////////////////////////////
				/*
				 * deal with the right node
				 */
				
				MutableNode right_node_copy = right_node.copy();
				right_node_copy.links().clear();
				IDUtil.prefixNode(right_node_copy);
				addToTargetTemp(right_node_copy);
				
				MutableNode right_link_target = findLinkTarget(context.getTargetGraph(), removed_link);
				if (right_link_target != null) {
					MutableNode right_temp = right_link_target.copy();
					right_temp.links().clear();
					IDUtil.prefixNode(right_temp);
					addToTargetTemp(right_temp);
					MutableNode r_link_source = findNodeInTargetTemp(right_node_copy.name().toString());
					
					Link r_link = r_link_source.linkTo(right_temp);
					r_link.attrs().add(removed_link.copy());
					GraphUtil.paintRed(r_link);
					r_link_source.addLink(r_link);
				}
				else {
					right_link_target = findLinkTarget(context.getSourceGraph(), removed_link);
					MutableNode right_temp = right_link_target.copy();
					right_temp.links().clear();
					IDUtil.prefixNode(right_temp);
					GraphUtil.paintRed(right_temp);
					addToTargetTemp(right_temp);
					MutableNode r_link_source = findNodeInTargetTemp(right_node_copy.name().toString());
					
					Link r_link = r_link_source.linkTo(right_temp);
					r_link.attrs().add(removed_link.copy());
					GraphUtil.paintRed(r_link);
					r_link_source.addLink(r_link);
				}
//				GraphUtil.linkCrossClusterNorm(result, l_node_copy.name().toString(), r_node_copy.name().toString());
			}
			
			/*
			 * below handles added links
			 */
			//get right node copy
			MutableNode right_node_copy = right_node.copy();
			//remove changed and unchanged links
			right_node_copy.links().removeAll(getChangedLinks(right_node));
			right_node_copy.links().removeAll(getUnchangedLinks(right_node));
			
			//for each link in the right node copy - they are added ones
			for(Link right_link: right_node_copy.links()) {
				MutableNode link_target = findLinkTarget(context.getTargetGraph(), right_link);
				MutableNode temp = link_target.copy();
				temp.links().clear();
				IDUtil.prefixNode(temp);
				addToTargetTemp(temp);
				MutableNode link_source = findNodeInTargetTemp(IDUtil.getPrefix() + right_node.name().toString());
				if (link_source == null) {
					link_source = right_node.copy();
					link_source.links().clear();
					IDUtil.prefixNode(link_source);
					addToTargetTemp(link_source);
				}
				
				Link link = link_source.linkTo(temp);
				link.attrs().add(right_link.copy());
				GraphUtil.paintGreen(link);
				link_source.addLink(link);
			}		
			
			//remove left node and right node from their graphs (to reduce memory footprint)
			getSourceNodes().remove(left_node);
			getTargetNodes().remove(right_node);
		}
		else {
			// if node is deleted
			addRemovedNode(left_node);
			MutableNode node_copy = left_node.copy();
			node_copy.links().clear();
			addToSourceTemp(node_copy);
			
			MutableNode node_copy_2 = left_node.copy();
			node_copy_2.links().clear();
			addToTargetTemp(node_copy_2);
			GraphUtil.paintRed(node_copy_2);
			IDUtil.prefixNode(node_copy_2);
			getSourceNodes().remove(left_node);

		}
	}
	
	public void compareAttribute(MutableNode source, MutableNode target, Entry<String, Object> attribute) {
		Entry<String, Object> correspond = findAttribute(target, attribute.getKey());
		
		//if attr does not exhist - attr is removed
		if (correspond == null) {
			addRemovedAttr(source, attribute.getKey());
		}
		else {
			//if attrs are same - add to unchanged?
			if (attribute.getValue().toString().equals(correspond.getValue().toString())) {
				addUnchangedAttr(target, correspond.getKey());
				addUnchangedAttr(source, attribute.getKey());
			}
			//add to changed attr
			else {
				addChangedAttr(target, correspond.getKey());
				addChangedAttr(source, attribute.getKey());
			}
		}
	}
	
	private HashSet<MutableNode> getUnmutableSourceNodes() {
		return (HashSet<MutableNode>) context.getSourceGraph().nodes();
	}
	
	private HashSet<MutableNode> getUnmutableTargetNodes() {
		return (HashSet<MutableNode>) context.getTargetGraph().nodes();
	}

	
	private HashSet<MutableNode> getSourceNodes() {
		return (HashSet<MutableNode>) context.getSourceGraph().rootNodes();
	}
	
	private HashSet<MutableNode> getTargetNodes() {
		return (HashSet<MutableNode>) context.getTargetGraph().rootNodes();
	}
	
	public MutableNode findNodeInSourceTemp(String name) {
		for(MutableNode n: source_temp.rootNodes()) {
			if (n.name().toString().equals(name)) {
				return n;
			}
		}
		return null;
	}
	
	public MutableNode findNodeInTargetTemp(String name) {
		for(MutableNode n: target_temp.rootNodes()) {
			if (n.name().toString().equals(name)) {
				return n;
			}
		}
		return null;
	}
	
	public void addToSourceTemp(MutableNode node) {
		if (!source_temp.rootNodes().contains(node)) {
			source_temp.rootNodes().add(node);
		}
	}
	
	public void addToTargetTemp(MutableNode node) {
		if (!target_temp.rootNodes().contains(node)) {
			target_temp.rootNodes().add(node);
		}
	}
	
	public boolean compareLink(MutableNode ln, MutableNode rn, Link s, Link t) {
		boolean result = true;
		if (s.attrs().get("label") != null) {
			if (!s.attrs().get("label").toString().equals(t.attrs().get("label"))) {
				result = false;
			}
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
	

	
	public MutableNode findNode(String name) {
		for(MutableNode n: context.getTargetGraph().nodes()) {
			if (n.name().value().equals(name)) {
				return n;
			}
		}
		return null;
	}
	
	public Link findLink(MutableNode n, String name) {
		//this is assuming that links have 'name'
		for(Link l: n.links()) {
			if (l.attrs().get("name").toString().equals(name)) {
				return l;
			}
		}
		return null;
	}
	
	public MutableNode findLinkTarget(MutableGraph graph, Link link) {
		String name = "";
		//obtain name for the target
		if (link.to() instanceof PortNode) {
			PortNode portNode = (PortNode) link.to();
			name = portNode.name().toString();
		}
		else if (link.to() instanceof MutableNode) {
			MutableNode mutableNode = (MutableNode) link.to();
			name = mutableNode.name().toString();
		}
		
		//get the node in the graph by name
		for(MutableNode node: graph.nodes()) {
			if (node.name().toString().equals(name)) {
				return node;
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
	
	private void addRemovedNode(MutableNode node) {
		removedNodes.add(node);
	}

	
	private void addUnchangedLink(MutableNode node, Link link) {
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
	
	private void addChangedLink(MutableNode node, Link link) {
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
	
	private void addAddedLink(MutableNode node, Link link) {
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
	
	private void addRemovedLink(MutableNode node, Link link) {
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
	
	private void addChangedAttr(MutableNode node, String attr) {
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
	
	private void addUnchangedAttr(MutableNode node, String attr) {
		HashSet<String> attrs = unchangedAttrs.get(node);
		if (attrs == null) {
			attrs = new HashSet<String>();
			attrs.add(attr);
			unchangedAttrs.put(node, attrs);
		}
		else {
			attrs.add(attr);
		}
	}
	
	private void addRemovedAttr(MutableNode node, String attr) {
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
	
	private void addAddedAttr(MutableNode node, String attr) {
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
	
	public void serialise() throws IOException {
	    Graphviz.fromGraph(result).render(Format.DOT).toFile(new File(context.getSerialise_dot()));
	    Graphviz.fromGraph(result).width(700).render(Format.SVG).toFile(new File(context.getSerialise_image()));
	}
	
	public static void main(String[] args) throws IOException {
		GVContext context = new GVContext("files/simple_filesystem.dot", "files/simple_filesystem2.dot");
	    GVComparisonEngine comparisonEngine = new GVComparisonEngine(context, DISPLAY_MODE.CHANGED);
	    comparisonEngine.load();
	    context.setSerialiseOptions("example/result.svg", "/files/result.dot");
	    comparisonEngine.compare();
	    comparisonEngine.serialise();
	    
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
