package org.eclipse.epsilon.picto.diff.example;

import static guru.nidi.graphviz.model.Factory.graph;
import static guru.nidi.graphviz.model.Factory.mutGraph;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import guru.nidi.graphviz.attribute.Label;
import guru.nidi.graphviz.engine.Format;
import guru.nidi.graphviz.engine.Graphviz;
import guru.nidi.graphviz.model.Link;
import guru.nidi.graphviz.model.MutableGraph;
import guru.nidi.graphviz.model.MutableNode;
import guru.nidi.graphviz.parse.Parser;

public class Example3 {

	public static void main(String[] args) throws IOException {
		InputStream dot1 = new FileInputStream("files/foo.dot");
	    MutableGraph g1 = new Parser().read(dot1);
	    
		InputStream dot2 = new FileInputStream("files/foo1.dot");
	    MutableGraph g2 = new Parser().read(dot2);
	    
	    MutableGraph g = mutGraph("example3");
	    
	    List<MutableNode> g1ns = new ArrayList<MutableNode>();
	    for(MutableNode n: g1.nodes()) {
	    	g1ns.add(n);
	    }
	    List<MutableNode> g2ns = new ArrayList<MutableNode>();
	    for(MutableNode n: g2.nodes()) {
	    	n.setName(Label.of("_"+n.name().toString()));
	    	g2ns.add(n);
	    	for(Link l: n.links()) {
	    		String label = (String) l.attrs().get("name");
	    		if (label != null) {
					label = "_"+label;
				}
	    		l.attrs().add("name", label);
	    	}
	    }
	    
	    for(Link l: g1.links()) {
	    	System.out.println("This is run");
	    	System.out.println(l);
	    }
	    
	    g.add(graph("dot1").cluster().graphAttr().with("label", "dot1").directed().with(g1ns));
	    g.add(graph("dot2").cluster().graphAttr().with("label", "dot2").directed().with(g2ns));

	    Graphviz.fromGraph(g).width(700).render(Format.PNG).toFile(new File("example/foo.png"));
	    Graphviz.fromGraph(g).render(Format.DOT).toFile(new File("files/foo_.dot"));
	}
}
