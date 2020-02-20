package org.eclipse.epsilon.picto.diff.engine;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import guru.nidi.graphviz.model.MutableGraph;
import guru.nidi.graphviz.parse.Parser;

public class GVContext {

	protected InputStream ss;
	protected InputStream ts;
	protected String sourceFile;
	protected String targetFile;
	protected MutableGraph sourceGraph;
	protected MutableGraph targetGraph;
	
	protected String serialise_image = null;
	protected String serialise_dot = null;
	
	public GVContext(String sourceFile, String targetFile) {
		this.sourceFile = sourceFile;
		this.targetFile = targetFile;
	}
	
	public boolean loadGraphs() throws IOException {
 		ss = new FileInputStream(sourceFile);
		sourceGraph = new Parser().read(ss);
		ts = new FileInputStream(targetFile);
		targetGraph = new Parser().read(ts);

		ss.close();
		ts.close();
		return true;
	}
	
	public MutableGraph getSourceGraph() {
		return sourceGraph;
	}
	
	public MutableGraph getTargetGraph() {
		return targetGraph;
	}
	
	public void clean() {
		sourceGraph = null;
		targetGraph = null;
	}
	
	public void setSerialiseOptions(String img_destination, String dot_destination) {
		serialise_image = img_destination;
		serialise_dot = dot_destination;
	}
	
	public String getSerialise_image() {
		return serialise_image;
	}
	
	public String getSerialise_dot() {
		return serialise_dot;
	}
}
