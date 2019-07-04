package your.org.myapp.internal.alignnetworks;

import java.awt.Dialog.ModalityType;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

import org.cytoscape.app.swing.CySwingAppAdapter;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.work.TaskIterator;

public class DyNetDialog extends JDialog {

	private final CySwingAppAdapter appAdapter;
	private List<CyNetwork> networks;
	private javax.swing.JButton jButton1;
	private javax.swing.JButton jButton2;
	private javax.swing.JLabel jLabel1;
	private javax.swing.JLabel jLabel2;
	private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
	private javax.swing.JSlider jSlider1;
	private javax.swing.JSlider jSlider2;
	
	public DyNetDialog(CySwingAppAdapter appAdapter,List<CyNetwork> networks) {
		// TODO Auto-generated constructor stub
		this.appAdapter=appAdapter;
		this.networks=networks;
		
		
		setModalityType(ModalityType.APPLICATION_MODAL);
		setTitle("Dynetworks setting");
		
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
	        	if(jSlider1.getValue()<100) jLabel3.setText("0."+(jSlider1.getValue()));
	        	else jLabel3.setText("1.0");
	        });
	        
	        jSlider2.addChangeListener(changeEvent -> {
	        	if(jSlider2.getValue()<100) jLabel4.setText("0."+(jSlider2.getValue()));
	        	else jLabel4.setText("1.0");
	        });
	        
	        jButton1.addActionListener(new ActionListener() {
	            public void actionPerformed(ActionEvent evt) {
	            	if (networks == null || networks.size() < 2){
						JOptionPane.showMessageDialog(DyNetDialog.this, "Please select at least 2 networks");
					}else{
						double alpha=jSlider1.getValue()/100f;
						double beta=jSlider2.getValue()/100f;
						appAdapter.getTaskManager().execute(new TaskIterator(
								new CreateDyNetTask(appAdapter,networks,alpha,beta)));
						dispose();
					}
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
	        jLabel2 = new javax.swing.JLabel();
	        jLabel3 = new javax.swing.JLabel();
	        jLabel4 = new javax.swing.JLabel();
	        jSlider1 = new javax.swing.JSlider();
	        jSlider2 = new javax.swing.JSlider();
	        jButton1 = new javax.swing.JButton();
	        jButton2 = new javax.swing.JButton();

	        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);

	        jLabel1.setText("snapshot cost:");

	        jLabel2.setText("temporal cost:");
	        
	        jSlider2.setMajorTickSpacing(10);
	        jSlider2.setMinorTickSpacing(1);
	        jSlider2.setSnapToTicks(true);
	        jSlider2.setToolTipText("50");
	        jSlider2.setValue(50);
	        jSlider2.setAutoscrolls(true);
	        
	        jSlider1.setMajorTickSpacing(10);
	        jSlider1.setMinorTickSpacing(1);
	        jSlider1.setSnapToTicks(true);
	        jSlider1.setToolTipText("80");
	        jSlider1.setValue(80);
	        jSlider1.setAutoscrolls(true);

	        jLabel3.setText("0.80");

	        jLabel4.setText("0.50");

	        jButton1.setText("OK");
	       

	        jButton2.setText("Cancel");
	       

	        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
	        getContentPane().setLayout(layout);
	        layout.setHorizontalGroup(
	            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
	            .addGroup(layout.createSequentialGroup()
	                .addGap(20, 20, 20)
	                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
	                    .addGroup(layout.createSequentialGroup()
	                        .addComponent(jLabel2)
	                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
	                        .addComponent(jLabel4))
	                    .addGroup(layout.createSequentialGroup()
	                        .addComponent(jLabel1)
	                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
	                        .addComponent(jLabel3)))
	                .addGap(18, 18, 18))
	            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
	                .addContainerGap(83, Short.MAX_VALUE)
	                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
	                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
	                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
	                            .addComponent(jSlider1, javax.swing.GroupLayout.PREFERRED_SIZE, 189, javax.swing.GroupLayout.PREFERRED_SIZE)
	                            .addComponent(jSlider2, javax.swing.GroupLayout.PREFERRED_SIZE, 190, javax.swing.GroupLayout.PREFERRED_SIZE))
	                        .addGap(40, 40, 40))
	                    .addGroup(layout.createSequentialGroup()
	                        .addComponent(jButton1)
	                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
	                        .addComponent(jButton2)
	                        .addGap(73, 73, 73))))
	        );

	        layout.linkSize(javax.swing.SwingConstants.HORIZONTAL, new java.awt.Component[] {jSlider1, jSlider2});

	        layout.linkSize(javax.swing.SwingConstants.HORIZONTAL, new java.awt.Component[] {jButton1, jButton2});

	        layout.setVerticalGroup(
	            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
	            .addGroup(layout.createSequentialGroup()
	                .addGap(26, 26, 26)
	                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
	                    .addComponent(jLabel1)
	                    .addComponent(jLabel3))
	                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
	                .addComponent(jSlider1, javax.swing.GroupLayout.PREFERRED_SIZE, 16, javax.swing.GroupLayout.PREFERRED_SIZE)
	                .addGap(18, 18, 18)
	                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
	                    .addComponent(jLabel2)
	                    .addComponent(jLabel4))
	                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
	                .addComponent(jSlider2, javax.swing.GroupLayout.PREFERRED_SIZE, 16, javax.swing.GroupLayout.PREFERRED_SIZE)
	                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 30, Short.MAX_VALUE)
	                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
	                    .addComponent(jButton1)
	                    .addComponent(jButton2))
	                .addContainerGap())
	        );

	        layout.linkSize(javax.swing.SwingConstants.VERTICAL, new java.awt.Component[] {jSlider1, jSlider2});

	      //  pack();
	    }// </editor-fold>                        

}
