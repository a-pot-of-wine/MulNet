package your.org.myapp.internal.topologyanalysis;

import java.rmi.activation.Activator;

import org.cytoscape.app.swing.CySwingAppAdapter;
import org.cytoscape.work.AbstractTaskFactory;
import org.cytoscape.work.TaskIterator;

import your.org.myapp.internal.CyActivator;


public class ShowTopoTaskFactory extends AbstractTaskFactory {
	private final CySwingAppAdapter appAdapter;
	private final CyActivator activator;
	
	public ShowTopoTaskFactory(CySwingAppAdapter appAdapter, CyActivator activator){
		this.appAdapter = appAdapter;
		this.activator = activator;
	}

	@Override
	public TaskIterator createTaskIterator() {
		return new TaskIterator(new ShowTopoTask(appAdapter, activator));
	}

	
	
}