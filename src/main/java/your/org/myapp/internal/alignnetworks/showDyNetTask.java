package your.org.myapp.internal.alignnetworks;

import java.util.List;

import org.cytoscape.app.swing.CySwingAppAdapter;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.TaskMonitor;

public class showDyNetTask extends AbstractTask{
	private final CySwingAppAdapter appAdapter;
	private List<CyNetwork> networks;
	private final boolean directed;
	public showDyNetTask(CySwingAppAdapter swingAdapter,List<CyNetwork> networks, boolean directed) {
		// TODO Auto-generated constructor stub
		this.appAdapter=swingAdapter;
		this.networks=networks;
		this.directed=directed;
	}
	@Override
	public void run(TaskMonitor taskMonitor) throws Exception {
		
		 if(networks.size() < 2) {
	            throw new RuntimeException("At least two networks needed for dynamic analyse.");
	     }
		if(directed==true) {
			throw new RuntimeException("Networks must be treated as undirected network for dynamic analyse.");
		}
		taskMonitor.setTitle("DyNetwork");
		taskMonitor.setStatusMessage("Showing initialisation dialog");
		
		
		DyNetDialog setDialog = new DyNetDialog(appAdapter,networks);
		
	}
	
	
}
