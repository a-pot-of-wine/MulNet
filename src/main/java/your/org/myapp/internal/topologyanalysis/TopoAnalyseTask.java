package your.org.myapp.internal.topologyanalysis;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Set;

import org.cytoscape.app.CyAppAdapter;
import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.TaskMonitor;

public class TopoAnalyseTask extends AbstractTask {
	
	private static final long serialVersionUID = 1L;
	private final CyAppAdapter swingAdapter;
	private final NetworksParameters npar;
	private final List<CyNetwork> networks;

	
	public TopoAnalyseTask(CyAppAdapter swingAdapter,List<CyNetwork> networks,NetworksParameters npar) {
		// TODO Auto-generated constructor stub
		this.swingAdapter=swingAdapter;
		this.networks=networks;
		this.npar=npar;
	
	}
	
	public void run(TaskMonitor taskMonitor) throws Exception {
		// TODO Auto-generated method stub
		if(networks.size() < 1) {
            throw new RuntimeException("At least one networks needed for analysing nodes topology.  "+networks.size());
        }
		
		taskMonitor.setTitle("Analysing networks");
        taskMonitor.setStatusMessage("Preparing analysis");
        
        int n=networks.size();
        int i=0;
        for(CyNetwork network:networks) {
        	i++;
        	for(String nodesAttr:npar.getNodesAttrs()) {
        		if(nodesAttr.equalsIgnoreCase("Degree")) {
        			analyseDegree(network);
        		}
        		if(nodesAttr.equalsIgnoreCase("Clusteringcoefficient")) {
        			analyseCC(network);
        		}
        		if(nodesAttr.equalsIgnoreCase("IsSingleNode")) {
        			analyseSingle(network);
        		}
        		if(nodesAttr.equalsIgnoreCase("DegreeCentrality")) {
        			analyseDegCen(network);
        		}
        		if(nodesAttr.equalsIgnoreCase("ClosenessCentrality")) {
        			analyseCloCen(network);
        		}
        		if(nodesAttr.equalsIgnoreCase("AverageShortestPathLength")) {
        			analyseAverPath(network);
        		}
        		if(nodesAttr.equalsIgnoreCase("BetweennessCentrality")) {
        			analyseBetCen(network);
        		}
        		if(nodesAttr.equalsIgnoreCase("SelfLoop")) {
        			analyseSelfLoop(network);
        		}
        		if(nodesAttr.equalsIgnoreCase("NeighborsConnectivity")) {
        			analyseNeiCon(network);
        		}
        		
        	}
        	for(String edgesAttr:npar.getEdgesAttrs()) {
        		if(edgesAttr.equalsIgnoreCase("EdgeBetweenness")) {
        			//throw new RuntimeException("EdgeBetweenness selected");
        			analyseEdgeBet(network);
        		}
        		if(edgesAttr.equalsIgnoreCase("EdgeClusteringcoefficient")) {
        			analyseEdgecluster(network);
        		}
        	}
        	
        	taskMonitor.setStatusMessage(String.format("Netwotks : %d/"+n+"have been analysed.", i));
        }
	}
	
	public void analyseEdgecluster(CyNetwork network) {
		// TODO Auto-generated method stub
		String columnName="EdgeClusteringcoefficient";
		if(network.getTable(CyEdge.class, CyNetwork.LOCAL_ATTRS).getColumn(columnName)!=null) return;
		network.getTable(CyEdge.class, CyNetwork.LOCAL_ATTRS).createColumn(columnName, Double.class, false);
		for(CyEdge edge:network.getEdgeList()) {
			CyNode sNode=edge.getSource();
			CyNode tNode=edge.getTarget();
			int z=0;
			List<CyNode> neighbors1=network.getNeighborList(sNode, CyEdge.Type.ANY);
			Set<CyNode> nSet=new HashSet<>(neighbors1) ;
			List<CyNode> neighbors2=network.getNeighborList(tNode, CyEdge.Type.ANY);
			Set<CyNode> eSet=new HashSet<>(neighbors2) ;
			eSet.removeAll(nSet);
			z=eSet.size()+1;
			int k=Math.min(neighbors1.size()-1,neighbors2.size()-1);
			network.getRow(edge).set(columnName, (double)z/k);
		}
	}

	private void analyseEdgeBet(CyNetwork network) {
		// TODO Auto-generated method stub
		String columnName="EdgeBetweenness";
		if(network.getTable(CyEdge.class, CyNetwork.LOCAL_ATTRS).getColumn(columnName)!=null) return;
		network.getTable(CyEdge.class, CyNetwork.LOCAL_ATTRS).createColumn(columnName, Double.class, false);
		int n=0;
		for(int i=0;i<network.getNodeCount()-1;i++) {
			int[] sigmas=shortestPathNum(network.getNodeList().get(i), network);
			
			for(int j=i+1;j<network.getNodeCount();j++) {
				n+=sigmas[j];
			}
		}
		
		for(CyEdge edge:network.getEdgeList()) {
			int num=0;
			
			int s=network.getNodeList().indexOf(edge.getSource());
			int t=network.getNodeList().indexOf(edge.getTarget());
			for(int i=0;i<network.getNodeCount()-1;i++) {
				List<List<Integer>> path=shortestPath(network.getNodeList().get(i), network);
				int[] sigmas=shortestPathNum(network.getNodeList().get(i), network);
				for(int j=i+1;j<network.getNodeCount();j++) {
					if(path.get(j).contains(s)&&path.get(j).contains(t)) {
						num+=sigmas[j];
					}
				}
			}
			network.getRow(edge).set(columnName, (double)num/n);
		}

	}

	public void analyseNeiCon(CyNetwork network) {
		// TODO Auto-generated method stub
		String columnName="NeighborsConnectivity";
		if(network.getTable(CyNode.class, CyNetwork.LOCAL_ATTRS).getColumn(columnName)!=null) return;
		network.getTable(CyNode.class, CyNetwork.LOCAL_ATTRS).createColumn(columnName, Double.class, false);
		for(CyNode node:network.getNodeList()) {
			List<CyNode> neighbors=network.getNeighborList(node, CyEdge.Type.ANY);
			Set<CyNode> nSet=new HashSet<>(neighbors) ;
			int d=neighbors.size();
			int max=0;
			for(CyNode neighbor:neighbors) {
				Set<CyNode> eSet=new HashSet<>(network.getNeighborList(neighbor, CyEdge.Type.ANY)) ;
				eSet.retainAll(nSet);
				max+=eSet.size();
			}
			max/=2;
			network.getRow(node).set(columnName, (double)max/d);
		}
	}

	public void analyseSelfLoop(CyNetwork network) {
		// TODO Auto-generated method stub
		String columnName="SelfLoop";
		if(network.getTable(CyNode.class, CyNetwork.LOCAL_ATTRS).getColumn(columnName)!=null) return;
		network.getTable(CyNode.class, CyNetwork.LOCAL_ATTRS).createColumn(columnName, Integer.class, false);
		for(CyNode node:network.getNodeList()) {
			List<CyNode> neighbors=network.getNeighborList(node, CyEdge.Type.ANY);
			if(!neighbors.contains(node)) {
				network.getRow(node).set(columnName, 0);
			}else {
				int c=0;
				for(CyNode neighbor:neighbors) {
					if(node.equals(neighbor)) c++;
				}
				network.getRow(node).set(columnName, c);
			}
		}
	}

	public void analyseBetCen(CyNetwork network) {
		// TODO Auto-generated method stub
		String columnName="BetweennessCentrality";
		if(network.getTable(CyNode.class, CyNetwork.LOCAL_ATTRS).getColumn(columnName)!=null) return;
		network.getTable(CyNode.class, CyNetwork.LOCAL_ATTRS).createColumn(columnName, Double.class, false);
		for(CyNode node:network.getNodeList()) {
			double bc=0;
			for(CyNode sNode:network.getNodeList()) {
				
				if(!node.equals(sNode)) {
					int [] sigma=shortestPathNum(sNode, network);
					List<List<Integer>> paths=shortestPath(sNode, network);
					for(int t=0;t<sigma.length;t++) {
						int d1=0;
						int d2=0;
						CyNode tNode=network.getNodeList().get(t);
						if(!sNode.equals(tNode) && !node.equals(tNode)) {
							d1+=sigma[t];
							List<Integer> path=paths.get(network.getNodeList().indexOf(tNode));
							if(path.contains(network.getNodeList().indexOf(node))) d2+=sigma[t];
						}
						if(d1!=0) bc=bc+d2/d1;
					}
				}
			}
			
			network.getRow(node).set(columnName, new Double(bc));
		}
		
	}

	public void analyseAverPath(CyNetwork network) {
		// TODO Auto-generated method stub
		String columnName="AverageShortestPathLength";
		if(network.getTable(CyNode.class, CyNetwork.LOCAL_ATTRS).getColumn(columnName)!=null) return;
		network.getTable(CyNode.class, CyNetwork.LOCAL_ATTRS).createColumn(columnName, Double.class, false);
		int n=network.getNodeCount()-1;
		for(CyNode node:network.getNodeList()) {
			int d=shortestPathLen(node, network);
			double c=0;
			if(d==Integer.MAX_VALUE) c=(double)Integer.MAX_VALUE;
			else c=d/n;
			network.getRow(node).set(columnName, c);
		}
	}

	public void analyseCloCen(CyNetwork network) {
		// TODO Auto-generated method stub
		String columnName="ClosenessCentrality";
		if(network.getTable(CyNode.class, CyNetwork.LOCAL_ATTRS).getColumn(columnName)!=null) return;
		network.getTable(CyNode.class, CyNetwork.LOCAL_ATTRS).createColumn(columnName, Double.class, false);
		for(CyNode node:network.getNodeList()) {
			int d=shortestPathLen(node, network);
			double c=0;
			if(d==Integer.MAX_VALUE) c=(double)0;
			else c=(double)1/d;
			network.getRow(node).set(columnName, new Double(c));
		}
		
	}

	public void analyseDegCen(CyNetwork network) {
		// TODO Auto-generated method stub
		String columnName="DegreeCentrality";
		if(network.getTable(CyNode.class, CyNetwork.LOCAL_ATTRS).getColumn(columnName)!=null) return;
		network.getTable(CyNode.class, CyNetwork.LOCAL_ATTRS).createColumn(columnName, Double.class, false);
		int n=network.getNodeCount();
		for(CyNode node:network.getNodeList()) {
			List<CyNode> neighbors=network.getNeighborList(node, CyEdge.Type.ANY);
			int i=0;
			for(CyNode neighbor:neighbors) {
				if(!node.equals(neighbor)) i++;
			}
			network.getRow(node).set(columnName,(double) i/n);
		}
		
	}

	public void analyseSingle(CyNetwork network) {
		// TODO Auto-generated method stub
		String columnName="SingleNode";
		if(network.getTable(CyNode.class, CyNetwork.LOCAL_ATTRS).getColumn(columnName)!=null) return;
		network.getTable(CyNode.class, CyNetwork.LOCAL_ATTRS).createColumn(columnName, Boolean.class, false);
		for(CyNode node:network.getNodeList()) {
			List<CyNode> neighbors=network.getNeighborList(node, CyEdge.Type.ANY);
			if(neighbors.size()==0) {
				network.getRow(node).set(columnName, true);
			}else {
				network.getRow(node).set(columnName, false);
			}
		}
		
	}

	public void analyseCC(CyNetwork network) {
		// TODO Auto-generated method stub
		String columnName="Clusteringcoefficient";
		if(network.getTable(CyNode.class, CyNetwork.LOCAL_ATTRS).getColumn(columnName)!=null) return;
		network.getTable(CyNode.class, CyNetwork.LOCAL_ATTRS).createColumn(columnName, Double.class, false);
		for(CyNode node:network.getNodeList()) {
			List<CyNode> neighbors=network.getNeighborList(node, CyEdge.Type.ANY);
			Set<CyNode> nSet=new HashSet<>(neighbors) ;
			
			int d=neighbors.size();
			int max=0;
			for(CyNode neighbor:neighbors) {
				Set<CyNode> eSet=new HashSet<>(network.getNeighborList(neighbor, CyEdge.Type.ANY)) ;
				eSet.retainAll(nSet);
				max+=eSet.size();
			}
			max/=2;
			double CC=max/(d*(d-1)/2);
			network.getRow(node).set(columnName, new Double(CC));
			
		}
	}

	public void analyseDegree(CyNetwork network) {
		if(npar.getDirected()==false) {
			String columnName="Degree";
			//network.getDefaultNodeTable().createColumn(columnName, Integer.class, true);
			if(network.getTable(CyNode.class, CyNetwork.LOCAL_ATTRS).getColumn(columnName)!=null) return;
			network.getTable(CyNode.class, CyNetwork.LOCAL_ATTRS).createColumn(columnName, Integer.class, false);
			for(CyNode node:network.getNodeList()) {
				int d=network.getNeighborList(node, CyEdge.Type.ANY).size();
				//network.getDefaultNodeTable().getRow(node).set(columnName,new Integer(d));
				//network.getDefaultNodeTable().getRow(node).set(columnName, new Integer(d));
				network.getRow(node).set(columnName, new Integer(d));
			}
		}else {
			String columnName1="InDgree";
			String columnName2="OutDgree";
			if(network.getTable(CyNode.class, CyNetwork.LOCAL_ATTRS).getColumn(columnName1)!=null) return;
			network.getTable(CyNode.class, CyNetwork.LOCAL_ATTRS).createColumn(columnName1, Integer.class, false);
			network.getTable(CyNode.class, CyNetwork.LOCAL_ATTRS).createColumn(columnName2, Integer.class, false);
			for(CyNode node:network.getNodeList()) {
				int int_d=network.getNeighborList(node, CyEdge.Type.INCOMING).size();
				int out_d=network.getNeighborList(node, CyEdge.Type.OUTGOING).size();
				//network.getDefaultNodeTable().getRow(node).set(columnName1, new Integer(int_d));
				//network.getDefaultNodeTable().getRow(node).set(columnName2, new Integer(out_d));
				network.getRow(node).set(columnName1, new Integer(int_d));
				network.getRow(node).set(columnName2, new Integer(out_d));
			}
		}
		
	}
	
	public List<List<Integer>> shortestPath(CyNode node,CyNetwork network) {
		Queue<CyNode> queue=new LinkedList<>();
		
		int [] dis=new int [network.getNodeCount()];
		List<List<Integer>> path=new ArrayList<>();
		for(int i=0;i<network.getNodeCount();i++) {
			dis[i]=Integer.MAX_VALUE;
			path.add(i,new ArrayList<>());
		}
		
		dis[network.getNodeList().indexOf(node)]=0;
		queue.offer(node);
		if(npar.getDirected()==false) {
			while(!queue.isEmpty()) {
				CyNode node1=queue.poll();
				for(CyEdge edge:network.getAdjacentEdgeList(node1, CyEdge.Type.ANY)) {
					CyNode node2;
					if(edge.getSource().equals(node1)) {
						node2=edge.getTarget();
					}else {
						node2=edge.getSource();
					}
					if(dis[network.getNodeList().indexOf(node2)]==Integer.MAX_VALUE) {
						dis[network.getNodeList().indexOf(node2)]=dis[network.getNodeList().indexOf(node1)]+1;
						path.get(network.getNodeList().indexOf(node2)).clear();
						path.get(network.getNodeList().indexOf(node2)).addAll(path.get(network.getNodeList().indexOf(node1)));
						//path.set(network.getNodeList().indexOf(node2),path.get(network.getNodeList().indexOf(node1)));
						path.get(network.getNodeList().indexOf(node2)).add(network.getNodeList().indexOf(node1));
						queue.offer(node2);
					}else if(dis[network.getNodeList().indexOf(node2)]==(dis[network.getNodeList().indexOf(node1)]+1)){
						path.get(network.getNodeList().indexOf(node2)).add(network.getNodeList().indexOf(node1));
					}
				}
			}
		}
		else {
			while(!queue.isEmpty()) {
				CyNode node1=queue.poll();
				for(CyEdge edge:network.getAdjacentEdgeList(node1, CyEdge.Type.OUTGOING)) {
					CyNode node2=edge.getSource();
					
					if(dis[network.getNodeList().indexOf(node2)]==Integer.MAX_VALUE) {
						dis[network.getNodeList().indexOf(node2)]=dis[network.getNodeList().indexOf(node1)]+1;
						path.set(network.getNodeList().indexOf(node2),path.get(network.getNodeList().indexOf(node1)));
						path.get(network.getNodeList().indexOf(node2)).add(network.getNodeList().indexOf(node1));
						queue.offer(node2);
					}else if(dis[network.getNodeList().indexOf(node2)]==(dis[network.getNodeList().indexOf(node1)]+1)){
						path.get(network.getNodeList().indexOf(node2)).add(network.getNodeList().indexOf(node1));
					}
				}
			}
		}
	
		return path;
	}
	public int shortestPathLen(CyNode node,CyNetwork network) {
		Queue<CyNode> queue=new LinkedList<>();
		
		int [] dis=new int [network.getNodeCount()];
		for(int i=0;i<network.getNodeCount();i++) {
			dis[i]=Integer.MAX_VALUE;
		}
		
		dis[network.getNodeList().indexOf(node)]=0;
		queue.offer(node);
		if(npar.getDirected()==false) {
			while(!queue.isEmpty()) {
				CyNode node1=queue.poll();
				for(CyEdge edge:network.getAdjacentEdgeList(node1, CyEdge.Type.ANY)) {
					CyNode node2;
					if(edge.getSource().equals(node1)) {
						node2=edge.getTarget();
					}else {
						node2=edge.getSource();
					}
					if(dis[network.getNodeList().indexOf(node2)]==Integer.MAX_VALUE) {
						dis[network.getNodeList().indexOf(node2)]=dis[network.getNodeList().indexOf(node1)]+1;
						queue.offer(node2);
					}
				}
			}
		}
		else {
			while(!queue.isEmpty()) {
				CyNode node1=queue.poll();
				for(CyEdge edge:network.getAdjacentEdgeList(node1, CyEdge.Type.OUTGOING)) {
					CyNode node2=edge.getSource();
					
					if(dis[network.getNodeList().indexOf(node2)]==Integer.MAX_VALUE) {
						dis[network.getNodeList().indexOf(node2)]=dis[network.getNodeList().indexOf(node1)]+1;
						queue.offer(node2);
					}
				}
			}
		}
		
		int d=0;
		for(int i=0;i<network.getNodeCount();i++) {
			
			if(dis[i]==Integer.MAX_VALUE) {
				d=Integer.MAX_VALUE;
				break;
			}else {
				d=d+dis[i];
			}
		}
		
		return d;
	}
	
	public int[] shortestPathNum(CyNode node,CyNetwork network) {
		Queue<CyNode> queue=new LinkedList<>();
		
		int [] dis=new int [network.getNodeCount()];
		int [] sigma=new int[network.getNodeCount()];
		//List<CyNode> pre=new ArrayList<>();
		for(int i=0;i<network.getNodeCount();i++) {
			dis[i]=Integer.MAX_VALUE;
			sigma[i]=0;
		}
		
		dis[network.getNodeList().indexOf(node)]=0;
		sigma[network.getNodeList().indexOf(node)]=1;
		queue.offer(node);
		if(npar.getDirected()==false) {
			while(!queue.isEmpty()) {
				CyNode node1=queue.poll();
				for(CyEdge edge:network.getAdjacentEdgeList(node1, CyEdge.Type.ANY)) {
					CyNode node2;
					if(edge.getSource().equals(node1)) {
						node2=edge.getTarget();
					}else {
						node2=edge.getSource();
					}
					if(dis[network.getNodeList().indexOf(node2)]==Integer.MAX_VALUE) {
						dis[network.getNodeList().indexOf(node2)]=dis[network.getNodeList().indexOf(node1)]+1;
						sigma[network.getNodeList().indexOf(node2)]=sigma[network.getNodeList().indexOf(node1)];
						queue.offer(node2);
					}else if(dis[network.getNodeList().indexOf(node2)]==(dis[network.getNodeList().indexOf(node1)]+1)){
						sigma[network.getNodeList().indexOf(node2)]++;
					}
				}
			}
		}
		else {
			while(!queue.isEmpty()) {
				CyNode node1=queue.poll();
				for(CyEdge edge:network.getAdjacentEdgeList(node1, CyEdge.Type.OUTGOING)) {
					CyNode node2=edge.getTarget();
					
					if(dis[network.getNodeList().indexOf(node2)]==Integer.MAX_VALUE) {
						dis[network.getNodeList().indexOf(node2)]=dis[network.getNodeList().indexOf(node1)]+1;
						
						sigma[network.getNodeList().indexOf(node2)]=sigma[network.getNodeList().indexOf(node1)];
						queue.offer(node2);
					}else if(dis[network.getNodeList().indexOf(node2)]==(dis[network.getNodeList().indexOf(node1)]+1)){
						sigma[network.getNodeList().indexOf(node2)]++;
					}
				}
			}
		}
		
		
		return sigma;
	}
}

