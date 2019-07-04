package your.org.myapp.internal.alignnetworks;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JSlider;
import javax.swing.JSpinner;
import javax.swing.SwingUtilities;

import org.cytoscape.app.swing.CySwingAppAdapter;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.work.TaskIterator;

public class Align1Dialog extends JDialog{
	
	private final CySwingAppAdapter appAdapter;
	private List<CyNetwork> networks;
	private final boolean directed;
	
	private JButton jButton1;
	private JButton jButton2;
	
	private JLabel jLabel2;
	private JLabel jLabel3;
	private JLabel jLabel4;

	private JSlider jSlider2;

	private JSpinner jSpinner2;

	
	public Align1Dialog(CySwingAppAdapter appAdapter,List<CyNetwork> networks,boolean directed) {
		// TODO Auto-generated constructor stub
		this.appAdapter=appAdapter;
		this.networks=networks;
		this.directed=directed;
		
		setModalityType(ModalityType.APPLICATION_MODAL);
		setTitle("SCML setting");
		
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
		
        jSlider2.addChangeListener(changeEvent -> {
        	if(jSlider2.getValue()<100) jLabel4.setText("0."+(jSlider2.getValue()));
        	else jLabel4.setText("1.0");
        });
        
        jButton1.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
            
				
				int numC=(int) jSpinner2.getValue();
				float r=jSlider2.getValue()/100f;
				appAdapter.getTaskManager().execute(new TaskIterator(
							new CreateConserveTask(appAdapter,networks,directed,numC,r)));
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

	        jLabel2 = new JLabel();
	        jSpinner2 = new JSpinner();
	        jLabel3 = new JLabel();
	        jSlider2 = new JSlider();
	        jLabel4 = new JLabel();
	        jButton1 = new JButton();
	        jButton2 = new JButton();

	        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);

	       

	        jLabel2.setText("Number of communities:");

	        jSpinner2.setValue(3);

	        jLabel3.setText("threshold value:");

	        jSlider2.setMajorTickSpacing(10);
	        jSlider2.setMinorTickSpacing(1);
	        jSlider2.setSnapToTicks(true);
	        jSlider2.setToolTipText("50");
	        jSlider2.setValue(50);
	        jSlider2.setAutoscrolls(true);

	        jLabel4.setText("0.50");

	        jButton1.setText("OK");
	        
	        jButton2.setText("Cancel");
	        
	        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
	        getContentPane().setLayout(layout);
	        layout.setHorizontalGroup(
	            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
	            .addGroup(layout.createSequentialGroup()
	                .addGap(18, 18, 18)
	                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
	                    .addGroup(layout.createSequentialGroup()
	                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
	                            
	                            .addComponent(jLabel2))
	                        .addGap(18, 18, 18)
	                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
	                            .addComponent(jSpinner2, javax.swing.GroupLayout.DEFAULT_SIZE, 38, Short.MAX_VALUE)
	                            )
	                        .addContainerGap(71, Short.MAX_VALUE))
	                    .addGroup(layout.createSequentialGroup()
	                        .addComponent(jLabel3)
	                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
	                        .addComponent(jLabel4)
	                        .addGap(20, 20, 20))))
	            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
	                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
	                .addComponent(jSlider2, javax.swing.GroupLayout.PREFERRED_SIZE, 165, javax.swing.GroupLayout.PREFERRED_SIZE)
	                .addGap(45, 45, 45))
	            .addGroup(layout.createSequentialGroup()
	                .addGap(67, 67, 67)
	                .addComponent(jButton1, javax.swing.GroupLayout.PREFERRED_SIZE, 66, javax.swing.GroupLayout.PREFERRED_SIZE)
	                .addGap(35, 35, 35)
	                .addComponent(jButton2)
	                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
	        );

	        layout.linkSize(javax.swing.SwingConstants.HORIZONTAL, new java.awt.Component[] {jButton1, jButton2});

	        layout.setVerticalGroup(
	            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
	            .addGroup(layout.createSequentialGroup()
	                .addContainerGap()
	                .addGap(18, 18, 18)
	                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
	                    .addComponent(jLabel2)
	                    .addComponent(jSpinner2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
	                .addGap(18, 18, 18)
	                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
	                    .addComponent(jLabel3)
	                    .addComponent(jLabel4))
	                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
	                .addComponent(jSlider2, javax.swing.GroupLayout.PREFERRED_SIZE, 18, javax.swing.GroupLayout.PREFERRED_SIZE)
	                .addGap(29, 29, 29)
	                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
	                    .addComponent(jButton1)
	                    .addComponent(jButton2))
	                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
	        );

	   //     pack();
	    }
	    
}
