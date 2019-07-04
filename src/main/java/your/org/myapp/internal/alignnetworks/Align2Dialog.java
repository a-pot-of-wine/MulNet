package your.org.myapp.internal.alignnetworks;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

import javax.swing.JDialog;
import javax.swing.SwingUtilities;

import org.cytoscape.app.swing.CySwingAppAdapter;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.work.TaskIterator;

public class Align2Dialog extends JDialog{
	
	private final CySwingAppAdapter appAdapter;
	private List<CyNetwork> networks;
	private final boolean directed;
	
	private javax.swing.JButton jButton1;
	private javax.swing.JButton jButton2;
	private javax.swing.JLabel jLabel1;
	private javax.swing.JLabel jLabel2;
	private javax.swing.JLabel jLabel3;
	private javax.swing.JLabel jLabel4;
	private javax.swing.JSlider jSlider1;
	private javax.swing.JSpinner jSpinner1;
	private javax.swing.JSpinner jSpinner2;
	
	public Align2Dialog(CySwingAppAdapter appAdapter,List<CyNetwork> networks,boolean directed) {
		// TODO Auto-generated constructor stub
		this.appAdapter=appAdapter;
		this.networks=networks;
		this.directed=directed;
		
		setModalityType(ModalityType.APPLICATION_MODAL);
		setTitle("SNF setting");
		
		initComponents();
		
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				setMinimumSize(getPreferredSize());
				pack();
				setLocationRelativeTo(appAdapter.getCySwingApplication().getJFrame());
				setVisible(true);
			}
		});
		
        jSlider1.addChangeListener(changeEvent -> {
        	jLabel3.setText("0."+(jSlider1.getValue()));
        	
        });
        
        jButton1.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
            
				int numK=(int) jSpinner1.getValue();
				int k=(int) jSpinner2.getValue();
				float r=jSlider1.getValue()/10f;
				appAdapter.getTaskManager().execute(new TaskIterator(
							new CreateSNFTask(appAdapter,networks,directed,k,numK,r)));
				dispose();
				
            }
        });
        
        jButton2.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
            	dispose();
            }
        });
	}
	
	 private void initComponents() {

	        jLabel1 = new javax.swing.JLabel();
	        jSpinner1 = new javax.swing.JSpinner();
	        jLabel2 = new javax.swing.JLabel();
	        jLabel3 = new javax.swing.JLabel();
	        jSlider1 = new javax.swing.JSlider();
	        jButton1 = new javax.swing.JButton();
	        jButton2 = new javax.swing.JButton();
	        jLabel4 = new javax.swing.JLabel();
	        jSpinner2 = new javax.swing.JSpinner();

	        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);

	        jLabel1.setText("The number of nearst neighbors:");
	        jSpinner1.setValue(3);
	        
	        jLabel2.setText("The threshold mju:");
	        jSlider1.setMaximum(8);
	        jSlider1.setMinimum(3);
	        jSlider1.setValue(5);
	        jLabel3.setText("0.5");

	        jButton1.setText("OK");
	       

	        jButton2.setText("Cancel");
	      

	        jLabel4.setText("The number of Clusters");
	        jSpinner2.setValue(3);

	        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
	        getContentPane().setLayout(layout);
	        layout.setHorizontalGroup(
	            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
	            .addGroup(layout.createSequentialGroup()
	                .addGap(61, 61, 61)
	                .addComponent(jButton1)
	                .addGap(32, 32, 32)
	                .addComponent(jButton2)
	                .addGap(0, 0, Short.MAX_VALUE))
	            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
	                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
	                .addComponent(jSlider1, javax.swing.GroupLayout.PREFERRED_SIZE, 237, javax.swing.GroupLayout.PREFERRED_SIZE)
	                .addGap(23, 23, 23))
	            .addGroup(layout.createSequentialGroup()
	                .addContainerGap()
	                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
	                    .addComponent(jLabel1)
	                    .addComponent(jLabel2)
	                    .addComponent(jLabel4))
	                .addGap(48, 48, 48)
	                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
	                    .addComponent(jSpinner2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
	                    .addComponent(jLabel3)
	                    .addComponent(jSpinner1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
	                .addContainerGap(19, Short.MAX_VALUE))
	        );

	        layout.linkSize(javax.swing.SwingConstants.HORIZONTAL, new java.awt.Component[] {jButton1, jButton2});

	        layout.setVerticalGroup(
	            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
	            .addGroup(layout.createSequentialGroup()
	                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
	                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
	                    .addComponent(jLabel4)
	                    .addComponent(jSpinner2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
	                .addGap(18, 18, 18)
	                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
	                    .addGroup(layout.createSequentialGroup()
	                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
	                            .addComponent(jSpinner1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
	                            .addComponent(jLabel1))
	                        .addGap(18, 18, 18)
	                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
	                            .addComponent(jLabel2)
	                            .addComponent(jLabel3))
	                        .addGap(18, 18, 18)
	                        .addComponent(jSlider1, javax.swing.GroupLayout.PREFERRED_SIZE, 17, javax.swing.GroupLayout.PREFERRED_SIZE)
	                        .addGap(73, 73, 73))
	                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
	                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
	                            .addComponent(jButton1)
	                            .addComponent(jButton2))
	                        .addGap(23, 23, 23))))
	        );

	   //     pack();
	    }
	    
}
