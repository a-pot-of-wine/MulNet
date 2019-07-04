package your.org.myapp.internal.networkimport;

import java.util.ArrayList;
import java.util.Collection;

import org.cytoscape.app.CyAppAdapter;
import org.cytoscape.io.read.CyNetworkReader;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNetworkManager;
import org.cytoscape.model.subnetwork.CyRootNetwork;
import org.cytoscape.model.subnetwork.CySubNetwork;
import org.cytoscape.session.CyNetworkNaming;
import org.cytoscape.view.layout.CyLayoutAlgorithm;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.CyNetworkViewFactory;
import org.cytoscape.view.model.CyNetworkViewManager;
import org.cytoscape.view.presentation.property.BasicVisualLexicon;
import org.cytoscape.view.vizmap.VisualMappingManager;
import org.cytoscape.view.vizmap.VisualStyle;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.ObservableTask;
import org.cytoscape.work.SynchronousTaskManager;
import org.cytoscape.work.TaskMonitor;

public class GenerateNetworkViewsTask extends AbstractTask implements ObservableTask {
	
	private final CyAppAdapter appAdapter;
	private final String name;
	private final CyNetworkReader viewReader;
	private final CyNetworkManager networkManager;
	private final CyNetworkViewManager networkViewManager;
	private final CyNetworkNaming namingUtil;
	private final VisualMappingManager vmm;
	private final CyNetworkViewFactory nullNetworkViewFactory;
	private	Collection<CyNetworkView> results;

	public GenerateNetworkViewsTask(
			final CyAppAdapter appAdapter, final String name,
			final CyNetworkReader viewReader,
			final CyNetworkManager networkManager,
			final CyNetworkViewManager networkViewManager,
			final CyNetworkNaming namingUtil,
			final VisualMappingManager vmm,
			final CyNetworkViewFactory nullNetworkViewFactory
	) {
		this.appAdapter=appAdapter;
		this.name = name;
		this.viewReader = viewReader;
		this.networkManager = networkManager;
		this.networkViewManager = networkViewManager;
		this.namingUtil = namingUtil;
		this.vmm = vmm;
		this.nullNetworkViewFactory = nullNetworkViewFactory;
	}

	@Override
	public void run(final TaskMonitor taskMonitor) throws Exception {
		taskMonitor.setProgress(0.0);

		final CyNetwork[] networks = viewReader.getNetworks();
		double numNets = (double)(networks.length);
		int i = 0;
		results = new ArrayList<CyNetworkView>();
		
		
		for (CyNetwork network : networks) {
			// Use original name if exists
			String networkName = network.getRow(network).get(CyNetwork.NAME, String.class);
			
			if (networkName == null || networkName.trim().length() == 0) {
				networkName = name;
				
				if (networkName == null)
					networkName = "? (Name is missing)";
				
				network.getRow(network).set(CyNetwork.NAME, namingUtil.getSuggestedNetworkTitle(networkName));
			}
			
			networkManager.addNetwork(network);
			
			createNetworkView(network);
			
			taskMonitor.setProgress((double)(++i)/numNets);
		}

		// If this is a subnetwork, and there is only one subnetwork in the root, check the name of the root network
		// If there is no name yet for the root network, set it the same as its base subnetwork
		if (networks.length == 1){
			if (networks[0] instanceof CySubNetwork){
				CySubNetwork subnet = (CySubNetwork) networks[0];
				final CyRootNetwork rootNet = subnet.getRootNetwork();
				String rootNetName = rootNet.getRow(rootNet).get(CyNetwork.NAME, String.class);
				
				if (rootNetName == null || rootNetName.trim().length() == 0){
					// The root network does not have a name yet, set it the same as the base subnetwork
					rootNet.getRow(rootNet).set(
							CyNetwork.NAME, networks[0].getRow(networks[0]).get(CyNetwork.NAME, String.class));
				}
			}
		}
		
		// Make sure rootNetwork has a name
		for (CyNetwork net : networks) {
			if (net instanceof CySubNetwork){
				CySubNetwork subNet = (CySubNetwork) net;
				CyRootNetwork rootNet = subNet.getRootNetwork();
				String networkName = rootNet.getRow(rootNet).get(CyNetwork.NAME, String.class);
				
				if (networkName == null || networkName.trim().length() == 0) {
					networkName = name;
					
					if (networkName == null)
						networkName = "? (Name is missing)";
					
					rootNet.getRow(rootNet).set(CyNetwork.NAME, namingUtil.getSuggestedNetworkTitle(networkName));
				}
			}			
		}
		
	}

	@Override
	public Object getResults(Class expectedType) {
		if (expectedType.equals(String.class))
			return getStringResults();
	
		return results;
	}

	private Object getStringResults() {
		String strRes = "";
		
		for (CyNetworkView view: results)
			strRes += (view.toString() + "\n");
		
		return strRes.isEmpty() ? null : strRes.substring(0, strRes.length()-1);
	}
	
	private void createNetworkView(final CyNetwork network) {
		final VisualStyle curStyle = vmm.getCurrentVisualStyle(); // get the current style before registering the views!
		
		final CyNetworkView view = viewReader.buildCyNetworkView(network);
		final VisualStyle viewStyle = vmm.getVisualStyle(view);
		networkViewManager.addNetworkView(view);
		
		// Only set current style when no style (or usually the default one) is already set for this view.
		// This allows the CyNetworkReader implementation to set the desired style itself.
		if (viewStyle != null && !viewStyle.equals(vmm.getDefaultVisualStyle())) {
			viewStyle.apply(view);
		} else {
			CyLayoutAlgorithm layoutAlgorithm = appAdapter.getCyLayoutAlgorithmManager().getLayout("force-directed");
			SynchronousTaskManager<?> synTaskMan = appAdapter.getCyServiceRegistrar().getService(SynchronousTaskManager.class);
			synTaskMan.execute(layoutAlgorithm.createTaskIterator(view, layoutAlgorithm.createLayoutContext(), CyLayoutAlgorithm.ALL_NODE_VIEWS, null));
			vmm.setVisualStyle(curStyle, view);
			curStyle.apply(view);
		}
		
		if (!view.isSet(BasicVisualLexicon.NETWORK_CENTER_X_LOCATION)
				&& !view.isSet(BasicVisualLexicon.NETWORK_CENTER_Y_LOCATION)
				&& !view.isSet(BasicVisualLexicon.NETWORK_CENTER_Z_LOCATION))
			view.fitContent();
		
		results.add(view);
	}
}
