package org.eclipse.epsilon.picto.diff;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import guru.nidi.graphviz.attribute.Color;
import guru.nidi.graphviz.attribute.Style;
import guru.nidi.graphviz.engine.Format;
import guru.nidi.graphviz.engine.Graphviz;
import guru.nidi.graphviz.model.MutableGraph;
import guru.nidi.graphviz.parse.Parser;

public class Example2 {

	public static void main(String[] args) throws IOException {
		Example2 ex2 = new Example2();
		try (InputStream dot = ex2.getClass().getResourceAsStream("color.dot")) {
		    MutableGraph g = new Parser().read(dot);
		    Graphviz.fromGraph(g).width(700).render(Format.PNG).toFile(new File("example/ex2-1.png"));

		    g.graphAttrs()
		            .add(Color.WHITE.gradient(Color.rgb("888888")).background().angle(90))
		            .nodeAttrs().add(Color.WHITE.fill())
		            .nodes().forEach(node ->
		            node.add(
		                    Color.named(node.name().toString()),
		                    Style.lineWidth(4).and(Style.FILLED)));
		    Graphviz.fromGraph(g).width(700).render(Format.PNG).toFile(new File("example/ex2-2.png"));
		}

	}
}
