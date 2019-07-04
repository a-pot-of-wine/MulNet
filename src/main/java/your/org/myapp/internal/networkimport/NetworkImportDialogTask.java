package your.org.myapp.internal.networkimport;

import javax.swing.JFileChooser;
import javax.swing.SwingUtilities;

import org.cytoscape.app.swing.CySwingAppAdapter;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.TaskIterator;
import org.cytoscape.work.TaskMonitor;



public class NetworkImportDialogTask extends AbstractTask{

	private final CySwingAppAdapter appAdapter;
	
	
	
	public NetworkImportDialogTask(CySwingAppAdapter appAdapter){
		this.appAdapter = appAdapter;
	}
	
	@Override
	public void run(final TaskMonitor taskMonitor) throws Exception {
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {				
				JFileChooser fileChooser = new JFileChooser();
				fileChooser.setMultiSelectionEnabled(true);
				
				int returnVal = fileChooser.showOpenDialog(appAdapter.getCySwingApplication().getJFrame());
				if (returnVal == JFileChooser.APPROVE_OPTION){
					appAdapter.getTaskManager().execute(new TaskIterator(
							new NetworkImportTask(appAdapter, fileChooser.getSelectedFiles())));
				}
			}
		});
	}
}
