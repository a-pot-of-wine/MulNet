package your.org.myapp.internal.networkimport;

import java.io.File;
import java.util.HashSet;
import java.util.Set;

import org.cytoscape.app.CyAppAdapter;
import org.cytoscape.io.read.AbstractCyNetworkReader;
import org.cytoscape.io.read.CyNetworkReader;
import org.cytoscape.io.read.CyNetworkReaderManager;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNetworkManager;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.session.CyNetworkNaming;
import org.cytoscape.view.model.CyNetworkViewFactory;
import org.cytoscape.view.model.CyNetworkViewManager;
import org.cytoscape.view.vizmap.VisualMappingManager;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.TaskMonitor;



public class NetworkImportTask extends AbstractTask{

	private static final String CREATE_NEW_COLLECTION_STRING = "-- Create new network collection --";
	
	private final CyAppAdapter appAdapter;
	private final CyServiceRegistrar serviceRegistrar;//you should only use CyServiceRegistrar if you need to register services outside of AbstractCyActivator's start method.
	private final CyNetworkReaderManager networkReaderManager;
	private final CyNetworkManager networkManager;
	private final CyNetworkViewManager networkViewManager;
	private final CyNetworkNaming namingUtil;
	private final VisualMappingManager vmm;
	private final CyNetworkViewFactory networkViewFactory;
	
	private final File[] files;
	
	private Set<String> existingNetworkNames;
	
	
	
	public NetworkImportTask(CyAppAdapter appAdapter, File[] files){
		this.appAdapter = appAdapter;
		serviceRegistrar = appAdapter.getCyServiceRegistrar();
		networkReaderManager = serviceRegistrar.getService(CyNetworkReaderManager.class);
		networkManager = appAdapter.getCyNetworkManager();
		networkViewManager = appAdapter.getCyNetworkViewManager();
		namingUtil = serviceRegistrar.getService(CyNetworkNaming.class);
		vmm = appAdapter.getVisualMappingManager();
		networkViewFactory = appAdapter.getCyNetworkViewFactory();
		
		this.files = files;
	}
	
	
	@Override
	public void run(TaskMonitor taskMonitor) throws Exception {
		
		//gather the names of all existing networks to avoid clash
		existingNetworkNames = new HashSet<String>();
		for (CyNetwork network : networkManager.getNetworkSet()){
			existingNetworkNames.add(network.getRow(network).get(CyNetwork.NAME, String.class));
		}

		
		for (File file : files){
			if (cancelled) throw new Exception("Task cancelled.");
			
			CyNetworkReader reader = networkReaderManager.getReader(file.toURI(), file.getName());
			
			if (!(reader instanceof AbstractCyNetworkReader)){
				throw new Exception("File format not supported: " + file.getName());
			}
			
			((AbstractCyNetworkReader)reader).getRootNetworkList().setSelectedValue(CREATE_NEW_COLLECTION_STRING);;

			
			reader.run(taskMonitor);
			
			for (CyNetwork network : reader.getNetworks()){
				String networkName = network.getRow(network).get(CyNetwork.NAME, String.class);
				if (existingNetworkNames.contains(networkName)){
					networkName = createUniqueName(networkName, existingNetworkNames);
					network.getRow(network).set(CyNetwork.NAME, networkName);
				}
				existingNetworkNames.add(networkName);
			}
			
			new GenerateNetworkViewsTask(appAdapter,file.getName(), reader, networkManager, networkViewManager, namingUtil, vmm, networkViewFactory).run(taskMonitor);
		}
	}

	private static String createUniqueName(String name, Set<String> existingNames){
		int counter = 1;
		String newName = name;
		
		while (existingNames.contains(newName)){
			newName = name + "_" + counter;
			counter++;
		}
		
		return newName;
	}
}

