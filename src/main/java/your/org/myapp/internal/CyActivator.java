package your.org.myapp.internal;

import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.Properties;

import org.cytoscape.app.swing.CySwingAppAdapter;
import org.cytoscape.application.swing.AbstractCyAction;
import org.cytoscape.application.swing.CyAction;
import org.cytoscape.application.swing.CytoPanel;
import org.cytoscape.application.swing.CytoPanelComponent;
import org.cytoscape.application.swing.CytoPanelName;
import org.cytoscape.application.swing.CytoPanelState;
import org.cytoscape.model.events.NetworkAboutToBeDestroyedListener;
import org.cytoscape.model.events.NetworkAddedListener;
import org.cytoscape.service.util.AbstractCyActivator;
import org.cytoscape.work.TaskFactory;
import org.osgi.framework.BundleContext;

import your.org.myapp.internal.alignnetworks.LoadAnalyseAction;
import your.org.myapp.internal.alignnetworks.NetworksAlignPanel;
import your.org.myapp.internal.networkimport.NetworkImportDialogTaskFactory;
import your.org.myapp.internal.topologyanalysis.ShowTopoTaskFactory;





public class CyActivator extends AbstractCyActivator {
	private CySwingAppAdapter swingAdapter;
	private ArrayList<Object> services;
	
	
	public void start(BundleContext context) throws Exception {
		swingAdapter = getService(context, CySwingAppAdapter.class);
		
		
		NetworkImportDialogTaskFactory networkImportTaskFactory = new NetworkImportDialogTaskFactory(swingAdapter);
		Properties networkImportTaskProperty = new Properties();
		networkImportTaskProperty.setProperty("preferredMenu", "Apps.MulNetwork");
		networkImportTaskProperty.setProperty("title", "MulNetwork Importer");
		registerService(context, networkImportTaskFactory, TaskFactory.class, networkImportTaskProperty);
		
		ShowTopoTaskFactory topoTaskFactory=new ShowTopoTaskFactory(swingAdapter, this);
		Properties topoProps=new  Properties();
		topoProps.put("title", "Networks Analyse");
		topoProps.put("preferredMenu", "Apps.MulNetwork");
		
		registerService(context, topoTaskFactory, TaskFactory.class, topoProps);
		
		
		NetworksAlignPanel alignPanel=new NetworksAlignPanel(swingAdapter);
		AbstractCyAction loadAlignPanelAction = new AbstractCyAction("Networks Align") {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                registerService(context, alignPanel, CytoPanelComponent.class, new Properties());
                
                CytoPanel cytoPanelWest=swingAdapter.getCySwingApplication().getCytoPanel(CytoPanelName.WEST);
                if (cytoPanelWest.getState() == CytoPanelState.HIDE) {
        			cytoPanelWest.setState(CytoPanelState.DOCK);
        		}	

        		// Select my panel
        		int index = cytoPanelWest.indexOfComponent(alignPanel);
        		if (index == -1) {
        			return;
        		}
        		cytoPanelWest.setSelectedIndex(index);
            }
        };
        loadAlignPanelAction.setPreferredMenu("Apps.MulNetwork");
        registerService(context, loadAlignPanelAction, CyAction.class, new Properties());
        registerService(context, alignPanel, NetworkAddedListener.class, new Properties());
        registerService(context, alignPanel, NetworkAboutToBeDestroyedListener.class, new Properties());
		

		
		//registerAllServices(context, this, new Properties());
		
	}
	
}

















