package your.org.myapp.internal.alignnetworks;

import java.util.List;

import org.cytoscape.app.swing.CySwingAppAdapter;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.Task;
import org.cytoscape.work.TaskMonitor;

public class EdgesPanelTask extends AbstractTask {
	
	private CySwingAppAdapter swingAdapter;
	private List<CyNetwork> networks;
	private String edgeName;
	

	public EdgesPanelTask(CySwingAppAdapter swingAdapter, List<CyNetwork> networks, String edgeName) {
		// TODO Auto-generated constructor stub
		this.swingAdapter=swingAdapter;
		this.networks=networks;
		this.edgeName=edgeName;
	}

	@Override
	public void run(TaskMonitor taskMonitor) throws Exception {
		// TODO Auto-generated method stub
		if(networks.size() < 2) {
            throw new RuntimeException("At least two networks needed for dynamic analyse.");
		}
		
		EdgeAttrDiag edgePanel=new EdgeAttrDiag(swingAdapter,networks,edgeName);
	}

}
