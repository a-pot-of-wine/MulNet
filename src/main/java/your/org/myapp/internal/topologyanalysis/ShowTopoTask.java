package your.org.myapp.internal.topologyanalysis;

import org.cytoscape.app.swing.CySwingAppAdapter;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.TaskMonitor;

import your.org.myapp.internal.CyActivator;

public class ShowTopoTask extends AbstractTask {
	private final CySwingAppAdapter appAdapter;
	private final CyActivator activator;
	
	public ShowTopoTask (CySwingAppAdapter appAdapter, CyActivator activator){
		this.appAdapter = appAdapter;
		this.activator = activator;
	}
	
	public void run(final TaskMonitor taskMonitor) throws Exception{
		taskMonitor.setTitle("Topology");
		taskMonitor.setStatusMessage("Showing initialisation dialog");
		
		SetupDialog setupDialog = new SetupDialog(appAdapter, activator);
	}
	
}
