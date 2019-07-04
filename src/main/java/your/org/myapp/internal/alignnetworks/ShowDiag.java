package your.org.myapp.internal.alignnetworks;

import java.awt.Dialog.ModalityType;

import javax.swing.JDialog;
import javax.swing.SwingUtilities;

import org.cytoscape.app.swing.CySwingAppAdapter;



public class ShowDiag extends JDialog{
	private CySwingAppAdapter swingAdapter;
	private org.jfree.chart.ChartPanel frame1;
	public ShowDiag(CySwingAppAdapter swingAdapter, org.jfree.chart.ChartPanel frame1) {
		
		// TODO Auto-generated constructor stub
		this.swingAdapter=swingAdapter;
		this.frame1=frame1;
		setModalityType(ModalityType.APPLICATION_MODAL);
		setTitle("Node/EdgeAttr Show");
		add(frame1);
		
		
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				setMinimumSize(getPreferredSize());
				pack();
				setLocationRelativeTo(swingAdapter.getCySwingApplication().getJFrame());
				setVisible(true);
			}
		});
	}

}
