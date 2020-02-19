package org.eclipse.epsilon.picto.test;

import org.eclipse.epsilon.picto.diff.engine.GVComparisonEngine_Cluster;
import org.eclipse.epsilon.picto.diff.engine.GVContext;
import org.eclipse.epsilon.picto.diff.engine.GVComparisonEngine_Cluster.DISPLAY_MODE;

public class TestCompare {

	public static void main(String[] args) {
		GVContext context = new GVContext("files/simple_filesystem.dot", "files/simple_filesystem2.dot");
	    GVComparisonEngine_Cluster comparisonEngine = new GVComparisonEngine_Cluster(context, DISPLAY_MODE.CHANGED);
	    comparisonEngine.load();
	    comparisonEngine.compare();
	    System.out.println(comparisonEngine.getSVGString());
	}
}
