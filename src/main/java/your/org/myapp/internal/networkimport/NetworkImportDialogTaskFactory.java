package your.org.myapp.internal.networkimport;

import org.cytoscape.app.swing.CySwingAppAdapter;
import org.cytoscape.work.AbstractTaskFactory;
import org.cytoscape.work.TaskIterator;

public class NetworkImportDialogTaskFactory extends AbstractTaskFactory{

	private CySwingAppAdapter appAdapter;
	
	public NetworkImportDialogTaskFactory(CySwingAppAdapter appAdapter){
		this.appAdapter = appAdapter;
	}
	
	
	@Override
	public TaskIterator createTaskIterator() {
		return new TaskIterator(new NetworkImportDialogTask(appAdapter));
	}

}
