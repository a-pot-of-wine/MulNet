package your.org.myapp.internal.alignnetworks;

import java.util.ArrayList;
import java.util.List;

import org.cytoscape.app.swing.CySwingAppAdapter;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyRow;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.TaskMonitor;




public class CreateNodeAttrTask extends AbstractTask {
	
	private CySwingAppAdapter swingAdapter;
	private List<CyNetwork> networks;
	private String nodeName;
	private String nodeAttr;

	public CreateNodeAttrTask(CySwingAppAdapter swingAdapter, List<CyNetwork> networks, String nodeName,
			String nodeAttr) {
		// TODO Auto-generated constructor stub
		this.swingAdapter=swingAdapter;
		this.networks=networks;
		this.nodeName=nodeName;
		this.nodeAttr=nodeAttr;
	}

	@Override
	public void run(TaskMonitor taskMonitor) throws Exception {
		// TODO Auto-generated method stub
		List<Object> nums=new ArrayList<>();
		
		
		for(CyNetwork network:networks) {
			if(network.getDefaultNodeTable().getMatchingRows("name", nodeName)==null) {
				if(nodeAttr.equalsIgnoreCase("Degree")||nodeAttr.equalsIgnoreCase("SelfLoop")) {
					nums.add(0);
				}else {
					nums.add(0.0);
				}
			}else {
				CyRow row=network.getDefaultNodeTable().getMatchingRows("name", nodeName).iterator().next();
				if(nodeAttr.equalsIgnoreCase("Degree")||nodeAttr.equalsIgnoreCase("SelfLoop")) {
					nums.add(row.get(nodeAttr, Integer.class));
				}else {
					nums.add(row.get(nodeAttr, Double.class));
				}
			
			}
		}
		org.jfree.data.category.DefaultCategoryDataset dateset = new org.jfree.data.category.DefaultCategoryDataset();
		for(int i=0;i<nums.size();i++) {
			dateset.setValue((Number) nums.get(i), "a", ""+i);
		}
		org.jfree.chart.JFreeChart chart=org.jfree.chart.ChartFactory.createLineChart(
				nodeAttr+" show", 
				"time", 
				nodeAttr, 
				dateset, 
				org.jfree.chart.plot.PlotOrientation.VERTICAL, 
				true, 
				false, 
				false 
				);
		org.jfree.chart.ChartPanel frame1=new org.jfree.chart.ChartPanel(chart,true);
		ShowDiag nodeDiag=new ShowDiag(swingAdapter,frame1);
//		swingAdapter.getCyServiceRegistrar().registerService(frame1, CytoPanelComponent.class, new Properties());
//        CytoPanel cytoPanelWest=swingAdapter.getCySwingApplication().getCytoPanel(CytoPanelName.WEST);
//        if (cytoPanelWest.getState() == CytoPanelState.HIDE) {
//			cytoPanelWest.setState(CytoPanelState.DOCK);
//		}
//        int index = cytoPanelWest.indexOfComponent(frame1);
//		if (index == -1) {
//			return;
//		}
//		cytoPanelWest.setSelectedIndex(index);
	}

}
