package your.org.myapp.internal.alignnetworks;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import org.cytoscape.app.swing.CySwingAppAdapter;
import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.view.layout.CyLayoutAlgorithm;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.presentation.property.BasicVisualLexicon;
import org.cytoscape.view.vizmap.VisualStyle;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.SynchronousTaskManager;
import org.cytoscape.work.TaskMonitor;

public class CreateSNFTask extends AbstractTask {

	private final CySwingAppAdapter appAdapter;
	private List<CyNetwork> networks;
	private boolean directed;
	private int k;
	private int numK;
	private float r;
	
	public CreateSNFTask(CySwingAppAdapter appAdapter, List<CyNetwork> networks, boolean directed, int k,int numK,float r) {
		// TODO Auto-generated constructor stub
		this.appAdapter=appAdapter;
		this.networks=networks;
		this.directed=directed;
		this.k=k;
		this.numK=numK;
		this.r=r;
	}

	@Override
	public void run(TaskMonitor taskMonitor) throws Exception {
		// TODO Auto-generated method stub
		if(networks.size() < 2) {
            throw new RuntimeException("At least two networks needed for SNF.");
        }
		
		int m=networks.size();
		int n=0;
		int i=-1;
		List<String> nameList=new ArrayList<>();
		for(CyNetwork network:networks) {
			for(CyNode node:network.getNodeList()) {
				String name=network.getRow(node).get("name", String.class);
				if(!nameList.contains(name)) nameList.add(name);
			}
		}
		n=nameList.size();
		
		if(numK>=n) {
			 throw new RuntimeException("Numbers of nearest neighbors must less than nodes");
		}
		if(k>=n) {
			 throw new RuntimeException("Numbers of Clusters must less than nodes");
		}
		
		ArrayList<double [][]> data=new ArrayList<>();
		
		
		i=-1;
		if(directed==false) {
			for(CyNetwork network:networks) {
				i++;
				data.add(new double [n][n]);
				for(CyEdge edge:network.getEdgeList()) {
					String source=network.getRow(edge.getSource()).get("name", String.class);
					String target=network.getRow(edge.getTarget()).get("name", String.class);
					int s=nameList.indexOf(source);
					int t=nameList.indexOf(target);
					data.get(i)[s][t]=1;
					data.get(i)[t][s]=1;
				}
			}
		}else {
			for(CyNetwork network:networks) {
				i++;
				data.add(new double [n][n]);
				for(CyEdge edge:network.getEdgeList()) {
					String source=network.getRow(edge.getSource()).get("name", String.class);
					String target=network.getRow(edge.getTarget()).get("name", String.class);
					int s=nameList.indexOf(source);
					int t=nameList.indexOf(target);
					data.get(i)[s][t]=1;
				}
			}
		}
		
		SNF snf=new SNF(r,1e-6,numK,n,data);
		
		int [] C=snf.spectralCluster(k);
		//int [] C= {0,0,0,1,1,1,2,2,2,2};
		List<List<Integer>> N=new ArrayList<>();
		for(i=0;i<k;i++) {
			N.add(new ArrayList<Integer>());
		}
		for(i=0;i<n;i++) {
			N.get(C[i]).add(i);
			
		}
		
		//Create Aligned CyNetwork
		CyNetwork net=appAdapter.getCyNetworkFactory().createNetwork();
		
		//Unique CyNetwork Name
		HashSet<String> existName=new HashSet<>();
		for(CyNetwork network:appAdapter.getCyNetworkManager().getNetworkSet()) {
			existName.add(network.getRow(network).get(CyNetwork.NAME, String.class));
		}
		String name="Aligned Network";
		String netName=name;
		int count=1;
		while(existName.contains(netName)) {
			netName=name+"_"+count;
			count++;
		}
		net.getRow(net).set(CyNetwork.NAME, netName);
		
		List<CyNode> nodeList=new ArrayList<>();
		//CyTable edgeTable=net.getDefaultEdgeTable();

		
		for(i=0;i<n;i++) {
//			CyNode node=net.addNode();
//			net.getRow(node).set("name", nameList.get(i));
			nodeList.add(null);
		}
//		CyTable nodeTable=net.getDefaultNodeTable();
//		for(CyNetwork network : networks) {
//            String nname = network.getRow(network).get(CyNetwork.NAME, String.class);
//            nodeTable.createColumn(nname, String.class, true);
//        }
		
		int [][] u=new int [n][n];
		
		
		for(i=0;i<k;i++) {
			
			if(N.get(i).size()<=1) continue;
			for(int j=0;j<N.get(i).size()-1;j++) {
				int s=N.get(i).get(j);
				for(int z=j+1;z<N.get(i).size();z++) {
					int t=N.get(i).get(z);
					if(u[s][t]==1||u[t][s]==1) {
						continue;
					}
					for(int v=0;v<m;v++) {
						if(data.get(v)[s][t]==1) {
							if(u[s][t]==1||u[t][s]==1) {
								break;
							}
							u[s][t]=u[t][s]=1;
							CyNode source=nodeList.get(s);
							if (source==null) {
								source=net.addNode();
								nodeList.set(s, source);
								net.getRow(source).set("name", nameList.get(s));
							}
							
							CyNode target=nodeList.get(t);
							if (target==null) {
								target=net.addNode();
								nodeList.set(t, target);
								net.getRow(target).set("name", nameList.get(t));
							}
								
							CyEdge edge=net.addEdge(source, target, directed);
							net.getRow(edge).set("name",nameList.get(s)+"(interacts with)"+nameList.get(t));
							
							
						}
					}
				}
			}
		}

		appAdapter.getCyNetworkManager().addNetwork(net);
		createNetworkView(net);
	}
	private void createNetworkView(final CyNetwork net) {
		final VisualStyle curStyle = appAdapter.getVisualMappingManager().getCurrentVisualStyle(); // get the current style before registering the views!
		
		final CyNetworkView view =appAdapter.getCyNetworkViewFactory().createNetworkView(net);
		final VisualStyle viewStyle = appAdapter.getVisualMappingManager().getVisualStyle(view);
		appAdapter.getCyNetworkViewManager().addNetworkView(view);
		
		// Only set current style when no style (or usually the default one) is already set for this view.
		// This allows the CyNetworkReader implementation to set the desired style itself.
		if (viewStyle != null && !viewStyle.equals(appAdapter.getVisualMappingManager().getDefaultVisualStyle())) {
			viewStyle.apply(view);
		} else {
			CyLayoutAlgorithm layoutAlgorithm = appAdapter.getCyLayoutAlgorithmManager().getLayout("force-directed");
			SynchronousTaskManager<?> synTaskMan = appAdapter.getCyServiceRegistrar().getService(SynchronousTaskManager.class);
			synTaskMan.execute(layoutAlgorithm.createTaskIterator(view, layoutAlgorithm.createLayoutContext(), CyLayoutAlgorithm.ALL_NODE_VIEWS, null));
			appAdapter.getVisualMappingManager().setVisualStyle(curStyle, view);
			curStyle.apply(view);
		}
		
		if (!view.isSet(BasicVisualLexicon.NETWORK_CENTER_X_LOCATION)
				&& !view.isSet(BasicVisualLexicon.NETWORK_CENTER_Y_LOCATION)
				&& !view.isSet(BasicVisualLexicon.NETWORK_CENTER_Z_LOCATION))
			view.fitContent();
		
	}
	

}
