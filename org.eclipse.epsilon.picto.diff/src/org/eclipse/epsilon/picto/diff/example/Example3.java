package org.eclipse.epsilon.picto.diff.example;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import guru.nidi.graphviz.attribute.Label;
import guru.nidi.graphviz.engine.Format;
import guru.nidi.graphviz.engine.Graphviz;
import guru.nidi.graphviz.model.LinkSource;
import guru.nidi.graphviz.model.MutableGraph;
import guru.nidi.graphviz.model.MutableNode;
import guru.nidi.graphviz.parse.Parser;

import static guru.nidi.graphviz.model.Factory.*;

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
	    	n.setName(Label.of(n.name().toString()+" "));
	    	g2ns.add(n);
	    }
	    g.add(graph("dot1").cluster().graphAttr().with("label", "dot1").with(g1ns));
//	    		with(node("foo").link(node("bar"))));
	    g.add(graph("dot2").cluster().graphAttr().with("label", "dot2").with(g2ns));
//	    		with(node("foo ").with("shape", "box").link(node("bar "))));
//	    MutableGraph g1c = g1.copy().setCluster(true);
//	    g1c.setName("dot1");
//	    MutableGraph g2c = g2.copy().setCluster(true);
//	    g2c.setName("dot2");
//	    g1c.addTo(g);
//	    g2c.addTo(g);
//	    MutableNode n = g.nodes().iterator().next();
//	    g.nodes().remove(n);
//	    System.out.println(g.nodes());
//	    System.out.println(g.nodes().size());
	    		
//	    		.setCluster(true).setName("foo1");
	    //g2.setCluster(true).setName("foo2").addTo(g);

	    Graphviz.fromGraph(g).width(700).render(Format.PNG).toFile(new File("example/foo.png"));
	    Graphviz.fromGraph(g).render(Format.DOT).toFile(new File("files/foo_.dot"));
	}
}
