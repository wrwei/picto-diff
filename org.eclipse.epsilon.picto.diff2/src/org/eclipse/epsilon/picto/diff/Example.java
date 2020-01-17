package org.eclipse.epsilon.picto.diff;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.Map;

import com.paypal.digraph.parser.GraphEdge;
import com.paypal.digraph.parser.GraphNode;
import com.paypal.digraph.parser.GraphParser;
import com.paypal.digraph.parser.GraphParserException;

public class Example {

	public static void main(String[] args) throws GraphParserException, FileNotFoundException {
		GraphParser parser = new GraphParser(new FileInputStream("files/test.dot"));
		Map<String, GraphNode> nodes = parser.getNodes();
		Map<String, GraphEdge> edges = parser.getEdges();	

		log("--- nodes:");
		for (GraphNode node : nodes.values()) {
			log(node.getId() + " " + node.getAttributes());
		}

		log("--- edges:");
		for (GraphEdge edge : edges.values()) {
			log(edge.getNode1().getId() + "->" + edge.getNode2().getId() + " " + edge.getAttributes());
		}

	}
	
	public static void log(Object o) {
		System.out.println(o);
	}

}
