package your.org.myapp.internal.alignnetworks;

import java.awt.Dialog.ModalityType;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

import javax.swing.JDialog;
import javax.swing.SwingUtilities;

import org.cytoscape.app.swing.CySwingAppAdapter;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.work.TaskIterator;

public class NodeAttrDiag extends JDialog{
	
	private CySwingAppAdapter swingAdapter;
	private List<CyNetwork> networks;
	private String nodeName;
	
	private javax.swing.ButtonGroup buttonGroup1;
	private javax.swing.JButton jButton1;
	private javax.swing.JButton jButton2;
	private javax.swing.JRadioButton jRadioButton1;
	private javax.swing.JRadioButton jRadioButton2;
	private javax.swing.JRadioButton jRadioButton3;
	private javax.swing.JRadioButton jRadioButton4;	
	private javax.swing.JRadioButton jRadioButton5;
	private javax.swing.JRadioButton jRadioButton6;
	private javax.swing.JRadioButton jRadioButton7;
	private javax.swing.JRadioButton jRadioButton8;

	public NodeAttrDiag(CySwingAppAdapter swingAdapter, List<CyNetwork> networks, String nodeName) {
		// TODO Auto-generated constructor stub
		this.swingAdapter=swingAdapter;
		this.networks=networks;
		this.nodeName=nodeName;
		
		setModalityType(ModalityType.APPLICATION_MODAL);
		setTitle("NodeAttr setting");
		
		initComponents();
		
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				setMinimumSize(getPreferredSize());
				pack();
				setLocationRelativeTo(swingAdapter.getCySwingApplication().getJFrame());
				setVisible(true);
			}
		});
		
		jButton1.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				// TODO Auto-generated method stub
				String nodeAttr = null;
				
				if(jRadioButton1.isSelected()) nodeAttr=jRadioButton1.getText();
				if(jRadioButton2.isSelected()) nodeAttr=jRadioButton2.getText();
				if(jRadioButton3.isSelected()) nodeAttr=jRadioButton3.getText();
				if(jRadioButton4.isSelected()) nodeAttr=jRadioButton4.getText();
				if(jRadioButton5.isSelected()) nodeAttr=jRadioButton5.getText();
				if(jRadioButton6.isSelected()) nodeAttr=jRadioButton6.getText();
				if(jRadioButton7.isSelected()) nodeAttr=jRadioButton7.getText();
				if(jRadioButton8.isSelected()) nodeAttr=jRadioButton8.getText();
				swingAdapter.getTaskManager().execute(new TaskIterator(
						new CreateNodeAttrTask(swingAdapter,networks,nodeName,nodeAttr)));
				dispose();
			}
		});
		
		jButton2.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
            	dispose();
            }
        });
	}
	
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">                          
    private void initComponents() {

        buttonGroup1 = new javax.swing.ButtonGroup();
        jRadioButton1 = new javax.swing.JRadioButton();
        jRadioButton2 = new javax.swing.JRadioButton();
        jRadioButton3 = new javax.swing.JRadioButton();
        jRadioButton4 = new javax.swing.JRadioButton();
        jRadioButton5 = new javax.swing.JRadioButton();
        jRadioButton6 = new javax.swing.JRadioButton();
        jRadioButton7 = new javax.swing.JRadioButton();
        jRadioButton8 = new javax.swing.JRadioButton();
        jButton1 = new javax.swing.JButton();
        jButton2 = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);

        buttonGroup1.add(jRadioButton1);
        jRadioButton1.setText("Degree");
        jRadioButton1.setSelected(true);
        
        buttonGroup1.add(jRadioButton2);
        jRadioButton2.setText("Clusteringcoefficient");

        buttonGroup1.add(jRadioButton3);
        jRadioButton3.setText("SelfLoop");

        buttonGroup1.add(jRadioButton4);
        jRadioButton4.setText("AverageShortestPathLength");

        buttonGroup1.add(jRadioButton5);
        jRadioButton5.setText("DegreeCentrality");

        buttonGroup1.add(jRadioButton6);
        jRadioButton6.setText("ClosenessCentrality");

        buttonGroup1.add(jRadioButton7);
        jRadioButton7.setText("NeighborsConnectivity");

        buttonGroup1.add(jRadioButton8);
        jRadioButton8.setText("BetweennessCentrality");
        

        jButton1.setText("OK");

        jButton2.setText("Cancel");

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jRadioButton1)
                    .addComponent(jRadioButton3)
                    .addComponent(jRadioButton5)
                    .addComponent(jRadioButton7))
                .addGap(27, 27, Short.MAX_VALUE)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jRadioButton2)
                    .addComponent(jRadioButton4)
                    .addComponent(jRadioButton6)
                    .addComponent(jRadioButton8))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
            .addGroup(layout.createSequentialGroup()
                .addGap(77, 77, 77)
                .addComponent(jButton1)
                .addGap(42, 42, 42)
                .addComponent(jButton2)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        layout.linkSize(javax.swing.SwingConstants.HORIZONTAL, new java.awt.Component[] {jButton1, jButton2});

        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(14, 14, 14)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jRadioButton1)
                    .addComponent(jRadioButton2))
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jRadioButton3)
                    .addComponent(jRadioButton4))
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jRadioButton5)
                    .addComponent(jRadioButton6))
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jRadioButton7)
                    .addComponent(jRadioButton8))
                .addGap(37, 37, 37)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jButton1)
                    .addComponent(jButton2))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

       // pack();
    }
}
