package org.eclipse.epsilon.picto.diff;

import java.io.File;
import java.io.IOException;

import guru.nidi.graphviz.attribute.Color;
import guru.nidi.graphviz.attribute.Rank;
import guru.nidi.graphviz.attribute.Style;
import guru.nidi.graphviz.attribute.Rank.RankDir;
import guru.nidi.graphviz.engine.Format;
import guru.nidi.graphviz.engine.Graphviz;
import guru.nidi.graphviz.model.Graph;

import static guru.nidi.graphviz.model.Factory.*;

public class Example1 {

	public static void main(String[] args) {
		Graph g = graph("example1").directed()
		        .graphAttr().with(Rank.dir(RankDir.LEFT_TO_RIGHT))
		        .with(
		                node("a").with(Color.RED).link(node("b")),
		                node("b").link(to(node("c")).with(Style.DASHED))
		        );
		try {
			Graphviz.fromGraph(g).height(100).render(Format.PNG).toFile(new File("example/ex1.png"));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
