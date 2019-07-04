package your.org.myapp.internal.alignnetworks;

import java.util.List;

import org.cytoscape.app.swing.CySwingAppAdapter;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.Task;
import org.cytoscape.work.TaskMonitor;




public class showAlignTask extends AbstractTask {
	
	private final CySwingAppAdapter appAdapter;
	private List<CyNetwork> networks;
	private final boolean directed;
	private final String algorithm;
	
	public showAlignTask(CySwingAppAdapter swingAdapter,List<CyNetwork> networks, boolean directed, String algorithm) {
		// TODO Auto-generated constructor stub
		this.appAdapter=swingAdapter;
		this.networks=networks;
		this.directed=directed;
		this.algorithm=algorithm;
	}

	public void run(TaskMonitor taskMonitor) throws Exception {
		// TODO Auto-generated method stub
		if(networks.size() < 2) {
            throw new RuntimeException("At least two networks needed for finding MCS.");
        }
		taskMonitor.setTitle("Align");
		taskMonitor.setStatusMessage("Showing initialisation dialog");
		
		if(algorithm.equalsIgnoreCase("SCML")) {
			Align1Dialog setDialog = new Align1Dialog(appAdapter,networks,directed);
		}
		
		else if(algorithm.equalsIgnoreCase("SNF")) {
			Align2Dialog set2Dialog = new Align2Dialog(appAdapter,networks,directed);
		}
		
		else if(algorithm.equalsIgnoreCase("ConMod")) {
			Align3Dialog set3Dialog = new Align3Dialog(appAdapter,networks,directed);
		}
		
//		else if(algorithm.equalsIgnoreCase("jRadioButton3") {
//			Align1Dialog setDialog = new Align4Dialog(appAdapter);
//		}
	}


}
