package your.org.myapp.internal.alignnetworks;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import org.cytoscape.app.swing.CySwingAppAdapter;
import org.cytoscape.application.swing.AbstractCyAction;
import org.cytoscape.application.swing.CyAction;
import org.cytoscape.application.swing.CytoPanel;
import org.cytoscape.application.swing.CytoPanelComponent;
import org.cytoscape.application.swing.CytoPanelName;
import org.cytoscape.application.swing.CytoPanelState;
import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.presentation.property.BasicVisualLexicon;
import org.cytoscape.view.vizmap.VisualStyle;
import org.cytoscape.view.vizmap.mappings.ContinuousMapping;
import org.cytoscape.view.vizmap.mappings.DiscreteMapping;
import org.cytoscape.view.vizmap.mappings.PassthroughMapping;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.TaskMonitor;
import org.ujmp.core.Matrix;

public class CreateDyNetTask extends AbstractTask{
	
	private final CySwingAppAdapter appAdapter;
	private List<CyNetwork> networks;
	private final double alpha;
	private final double beta;
	
	public CreateDyNetTask(CySwingAppAdapter appAdapter, List<CyNetwork> networks, double alpha, double beta) {
		// TODO Auto-generated constructor stub
		this.appAdapter=appAdapter;
		this.networks=networks;
		this.alpha=alpha;
		this.beta=beta;
	}

	@Override
	public void run(TaskMonitor taskMonitor) throws Exception {
		// TODO Auto-generated method stub
		int m=networks.size();
		ArrayList<Matrix> adjacencyMatrices=new ArrayList<>(m);
		List<String> nameList=new ArrayList<>();
		
		//CyNetwork network=networks[p];
		
		for(CyNetwork network:networks) {
			for(CyNode node:network.getNodeList()) {
				String name=network.getRow(node).get("name", String.class);
				if(!nameList.contains(name)) nameList.add(name);
			}
		}
		
		int i=0;
		for(CyNetwork network:networks) {
			int r=network.getNodeCount();
			Matrix V=Matrix.Factory.zeros(r, r);
			for(CyEdge edge:network.getEdgeList()) {
				String source=network.getRow(edge.getSource()).get("name", String.class);
				String target=network.getRow(edge.getTarget()).get("name", String.class);
				int s=nameList.indexOf(source);
				int t=nameList.indexOf(target);
				V.setAsInt(1, s,t);
				V.setAsInt(1, t,s);
			}
			adjacencyMatrices.add(i, V);
			i++;
		}
		ArrayList<Matrix> results=ESPRA.ESPRA_Algorithm(adjacencyMatrices, alpha, beta);
		
		List<List<List<Integer>>> cc=new ArrayList<>();
		List<List<Integer>> ct=new ArrayList<>();
		List<List<List<Integer>>> cd=new ArrayList<>();
		
		List<Integer> ma=new ArrayList<>();
		int mx=0;
		for(i=0;i<results.size();i++) {
			mx=0;
			cc.add(i, new ArrayList<>());
			ct.add(i, new ArrayList<>());
			for(int j=0;j<results.get(i).getRowCount();j++) {
				if(mx<results.get(i).getAsInt(j,1)) {
					mx=results.get(i).getAsInt(j,1);
				}
			}
			ma.add(mx);
			for(int k=0;k<=mx;k++) {
				cc.get(i).add(new ArrayList<>());
				ct.get(i).add(0);
			}
		}
		
		for(i=0;i<results.size();i++) {
			for(int j=0;j<results.get(i).getRowCount();j++) {
				int o=results.get(i).getAsInt(j,1);
				cc.get(i).get(o).add(j);
			}
		}
		
		

		for(i=0;i<results.size();i++) {
			
			for(int z=0;z<cc.get(i).size();z++) {
				int j=i+1;
				if(ct.get(i).get(z)==1) continue;
				cd.add(new ArrayList<>());
				ct.get(i).set(z,1);
				int s3=cd.size()-1;
					
				for(int p=0;p<i;p++) cd.get(s3).add(p,new ArrayList<>());
					
				cd.get(s3).add(i, cc.get(i).get(z));
				while(true) {
					if(j>=results.size()) break;
					boolean flag=false;
					for(int k=0;k<cc.get(j).size();k++) {
						Set<Integer> c1=new HashSet<>(cc.get(i).get(z));
						Set<Integer> c2=new HashSet<>(cc.get(j).get(k));
						int s1=c1.size();
						int s2=c2.size();
						System.out.println(c1);	
						System.out.println(c2);	
						c1.retainAll(c2);
						if(c1.size()>s1/2||c1.size()>s2/2) {
							ct.get(j).set(k,1);
							cd.get(s3).add(j, cc.get(j).get(k));
							flag=true;
							break;
						}
					}
					if(!flag) cd.get(s3).add(j,new ArrayList<>());
				
					j++;
				}
			}
		}
		
		for(i=0;i<m;i++) {
			if(networks.get(i).getTable(CyNode.class, CyNetwork.LOCAL_ATTRS).getColumn("Membership")!=null) return;
			networks.get(i).getTable(CyNode.class, CyNetwork.LOCAL_ATTRS).createColumn("Membership", Integer.class, false);
			int j=0;
			
			for(CyNode node:networks.get(i).getNodeList()) {
				//int c=results.get(i).getAsInt(j,1);
				boolean flag=false;
				for(int z=0;z<cd.size();z++) {
					if(flag==true) break;
					for(int k=0;k<cd.get(z).get(i).size();k++) {
						if(j==cd.get(z).get(i).get(k)) {
							networks.get(i).getRow(node).set("Membership", z);
							flag=true;
							break;
						}
					}
				}
				//if(flag==false) 
				
				j++;
			}
		}
		
		//List<List<Integer>> Cluster
		
		List<Color> colors=Arrays.asList(new Color(220,20,60), new Color(255,20,147), new Color(255,182,193), new Color(255,0,255), new Color(219,112,147), new Color(148,0,211),
					new Color(75,0,130), new Color(65,105,225), new Color(0,0,205), new Color(0,191,255), new Color(119,136,153), new Color(225,255,255), new Color(0,255,255),
					new Color(0,206,209), new Color(64,224,208), new Color(245,255,250), new Color(50,205,50), new Color(0,255,0), new Color(0,100, 0), new Color(173,255, 47),
					new Color( 107, 142, 35), new Color( 255, 255, 224), new Color( 255, 255, 0), new Color(189, 183, 107), new Color( 255, 215, 0), new Color( 255, 250, 240), 
					new Color(210,180,140), new Color(210,105,30), new Color(0,0,0), new Color(192,192,192)) ;
		CyNetwork network=networks.get(0);
		appAdapter.getCyApplicationManager().setCurrentNetwork(network);
		CyNetworkView view=appAdapter.getCyApplicationManager().getCurrentNetworkView();
		VisualStyle vs=appAdapter.getVisualStyleFactory().createVisualStyle("DyNet");
		appAdapter.getVisualMappingManager().addVisualStyle(vs);
		PassthroughMapping pMapping = (PassthroughMapping) appAdapter.getVisualMappingFunctionPassthroughFactory().createVisualMappingFunction("name", String.class, BasicVisualLexicon.NODE_LABEL);
		DiscreteMapping cMapping=(DiscreteMapping) appAdapter.getVisualMappingFunctionDiscreteFactory().createVisualMappingFunction("Membership", Integer.class, BasicVisualLexicon.NODE_FILL_COLOR);
		List<Integer> c_list=new ArrayList<>();
		for(CyNode node:network.getNodeList()) {
			
			int c=network.getRow(node).get("Membership", Integer.class);
			if(c_list.indexOf(c)<0) {
				c_list.add(c);
			}
			int j=c_list.indexOf(c);
			cMapping.putMapValue(c, colors.get(j));
		}
		vs.addVisualMappingFunction(pMapping);
		vs.addVisualMappingFunction(cMapping);
		appAdapter.getVisualMappingManager().setVisualStyle(vs, view);
		view.updateView();
		
		
		ClusterPanel clusterPanel=new ClusterPanel(appAdapter,networks,cd);
//		AbstractCyAction loadAlignPanelAction = new AbstractCyAction("") {
//			public void actionPerformed(ActionEvent actionEvent) {
			appAdapter.getCyServiceRegistrar().registerService(clusterPanel, CytoPanelComponent.class, new Properties());
	        CytoPanel cytoPanelWest=appAdapter.getCySwingApplication().getCytoPanel(CytoPanelName.EAST);
	        if (cytoPanelWest.getState() == CytoPanelState.HIDE) {
				cytoPanelWest.setState(CytoPanelState.DOCK);
			}
	        int index = cytoPanelWest.indexOfComponent(clusterPanel);
			if (index == -1) {
				return;
			}
			cytoPanelWest.setSelectedIndex(index);
			
			
//			}
//		};
//		appAdapter.getCyServiceRegistrar().registerService(loadAlignPanelAction, CyAction.class, new Properties());
//	
	}

}
