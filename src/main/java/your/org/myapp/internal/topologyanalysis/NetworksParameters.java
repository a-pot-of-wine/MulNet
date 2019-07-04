package your.org.myapp.internal.topologyanalysis;

import java.util.ArrayList;
import java.util.List;

public class NetworksParameters {
	private final boolean directed;
	private final List<String> nodesAttrs;
	private final List<String> edgesAttrs;
	
	public NetworksParameters(boolean directed,List<String> selectedNodeAttributes,List<String> selectedEdgeAttributes) {
		this.directed=directed;
		this.nodesAttrs=selectedNodeAttributes;
		this.edgesAttrs=selectedEdgeAttributes;
	}

	public boolean getDirected() {
		return directed;
	}

	public List<String> getNodesAttrs() {
		return nodesAttrs;
	}
	
	public List<String> getEdgesAttrs() {
		return edgesAttrs;
	}
}
