package your.org.myapp.internal.alignnetworks;

import java.util.List;

import org.cytoscape.app.swing.CySwingAppAdapter;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.Task;
import org.cytoscape.work.TaskMonitor;

public class NodesPanelTask extends AbstractTask {
	
	private CySwingAppAdapter swingAdapter;
	private List<CyNetwork> networks;
	private String nodeName;
	

	public NodesPanelTask(CySwingAppAdapter swingAdapter, List<CyNetwork> networks, String nodeName) {
		// TODO Auto-generated constructor stub
		this.swingAdapter=swingAdapter;
		this.networks=networks;
		this.nodeName=nodeName;
	}

	@Override
	public void run(TaskMonitor taskMonitor) throws Exception {
		// TODO Auto-generated method stub
		if(networks.size() < 2) {
            throw new RuntimeException("At least two networks needed for dynamic analyse.");
		}
		
		NodeAttrDiag nodePanel=new NodeAttrDiag(swingAdapter,networks,nodeName);
	}

}
