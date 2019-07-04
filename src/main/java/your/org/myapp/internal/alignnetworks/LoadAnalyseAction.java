package your.org.myapp.internal.alignnetworks;

import java.awt.event.ActionEvent;
import java.util.Properties;

import org.cytoscape.app.swing.CySwingAppAdapter;
import org.cytoscape.application.swing.AbstractCyAction;
import org.cytoscape.application.swing.CySwingApplication;
import org.cytoscape.application.swing.CytoPanel;
import org.cytoscape.application.swing.CytoPanelComponent;
import org.cytoscape.application.swing.CytoPanelName;
import org.cytoscape.application.swing.CytoPanelState;
import org.cytoscape.model.events.NetworkAboutToBeDestroyedListener;
import org.cytoscape.model.events.NetworkAddedListener;


public class LoadAnalyseAction extends AbstractCyAction{
	
	private static final long serialVersionUID = 2L;
	private final CySwingAppAdapter swingAdapter;
	private CySwingApplication swingApp;
	private final CytoPanel cytoPanelWest;
	private NetworksAlignPanel alignPanel;
	
	public LoadAnalyseAction(CySwingAppAdapter swingAdapter,NetworksAlignPanel alignPanel){
		// Add a menu item -- Apps->sample02
		super("Networks Aligning");
		setPreferredMenu("Apps.MulNetwork");

		this.swingAdapter = swingAdapter;
		this.swingApp=swingAdapter.getCySwingApplication();
		//Note: myControlPanel is bean we defined and registered as a service
		this.cytoPanelWest = this.swingApp.getCytoPanel(CytoPanelName.WEST);
		this.alignPanel = alignPanel;
		
	}
	
	/**
	 *  DOCUMENT ME!
	 *
	 * @param e DOCUMENT ME!
	 */
	public void actionPerformed(ActionEvent e) {
		swingAdapter.getCyServiceRegistrar().registerService(alignPanel, CytoPanelComponent.class, new Properties());
		swingAdapter.getCyServiceRegistrar().registerService(alignPanel, NetworkAddedListener.class, new Properties());
		swingAdapter.getCyServiceRegistrar().registerService(alignPanel, NetworkAboutToBeDestroyedListener.class, new Properties());
		// If the state of the cytoPanelWest is HIDE, show it
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

}