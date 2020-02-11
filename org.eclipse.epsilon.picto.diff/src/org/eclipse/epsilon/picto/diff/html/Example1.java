package org.eclipse.epsilon.picto.diff.html;

import static guru.nidi.graphviz.model.Factory.*;

import java.io.File;
import java.io.IOException;

import guru.nidi.graphviz.attribute.Color;
import guru.nidi.graphviz.attribute.Image;
import guru.nidi.graphviz.attribute.Label;
import guru.nidi.graphviz.attribute.Rank;
import guru.nidi.graphviz.attribute.Style;
import guru.nidi.graphviz.attribute.Rank.RankDir;
import guru.nidi.graphviz.attribute.Size;
import guru.nidi.graphviz.engine.Format;
import guru.nidi.graphviz.engine.Graphviz;
import guru.nidi.graphviz.engine.GraphvizCmdLineEngine;
import guru.nidi.graphviz.model.Graph;

public class Example1 {

	public static void main(String[] args) {
		
//		Graph g = graph("fileSystem").directed()
//		        .nodeAttr().with("shape", "record")
//		        .linkAttr().with("dir", "back")
//		        .with(
//		                node("n1").with("label", "PDFFile")
//		        );
//		try {
//			Graphviz.fromGraph(g).render(Format.PNG).toFile(new File("example/file.png"));
//		    Graphviz.fromGraph(g).render(Format.DOT).toFile(new File("files/file.dot"));
//
//		} catch (IOException e) {
//			e.printStackTrace();
//		}
//		
		Graphviz g = Graphviz.fromGraph(graph()
		        .with(node("n1").with(Size.std().margin(.8, .7), Image.of("file.svg"))));
		try {
			g.basedir(new File("example")).render(Format.PNG).toFile(new File("example/ex7.png"));
			g.basedir(new File("example")).render(Format.DOT).toFile(new File("example/file.dot"));

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
