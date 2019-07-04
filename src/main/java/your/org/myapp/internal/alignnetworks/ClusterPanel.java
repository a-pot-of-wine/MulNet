package your.org.myapp.internal.alignnetworks;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.DefaultListModel;
import javax.swing.GroupLayout;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.LayoutStyle;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.cytoscape.app.swing.CySwingAppAdapter;
import org.cytoscape.application.swing.CytoPanel;
import org.cytoscape.application.swing.CytoPanelComponent;
import org.cytoscape.application.swing.CytoPanelName;
import org.cytoscape.application.swing.CytoPanelState;
import org.cytoscape.application.swing.events.CytoPanelStateChangedEvent;
import org.cytoscape.application.swing.events.CytoPanelStateChangedListener;
import org.cytoscape.model.CyDisposable;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.CyTableUtil;


public class ClusterPanel extends JPanel implements CytoPanelComponent, CytoPanelStateChangedListener {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -2719000764102304002L;
	private CySwingAppAdapter appAdapter;
	private List<CyNetwork> networks;
	private List<List<List<Integer>>> cd;
	private CytoPanelState oldState = null;
	private  DefaultListModel<String> allListModel;
	private  DefaultListModel<String> timeListModel;
   
	
	private javax.swing.ButtonGroup buttonGroup1;
	private javax.swing.JList<String> jList1;
	private javax.swing.JPanel jPanel1;
	private javax.swing.JRadioButton jRadioButton1;
	private javax.swing.JRadioButton jRadioButton2;
	private javax.swing.JScrollPane jScrollPane1;
	private JButton jButton;
	public ClusterPanel(CySwingAppAdapter appAdapter, List<CyNetwork> networks, List<List<List<Integer>>> cd) {
		// TODO Auto-generated constructor stub
		this.appAdapter=appAdapter;
		this.networks=networks;
		this.cd=cd;
		
		allListModel = new DefaultListModel<>();
		
		for(int i=0;i<cd.size();i++) {
			int num=0;
			for(int j=0;j<cd.get(i).size();j++) {
				if(cd.get(i).get(j).size()!=0) num++;
			}
			allListModel.addElement("Cluster Id: "+(i+1)+"\n       Existed in "+num+" time snapshots");
		}	
		
		initComponents();
		jRadioButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jList1.setModel(allListModel);
            }
        });
		
		jRadioButton2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
            	timeListModel = new DefaultListModel<>();
            	CyNetwork network=appAdapter.getCyApplicationManager().getCurrentNetwork();
        		int i=0;
        		for(i=0;i<networks.size();i++) {
        			if(network.equals(networks.get(i))) break;
        		}
        		
        		for(int z=0;z<cd.size();z++) {
        			if(cd.get(z).get(i).size()!=0) {
        				timeListModel.addElement("Cluster Id: "+(z)+"\n       Node nums: "+cd.get(z).get(i).size());
        			}else {
        				timeListModel.addElement("");
        			}
        		}
            	 jList1.setModel(timeListModel);
            }
        });
		
		jButton.addActionListener(new ActionListener() {
			
			public void actionPerformed(ActionEvent e) {
				// TODO Auto-generated method stub
				panelClosing();
			}
		});
		
		jList1.addListSelectionListener(new ListSelectionListener() {
			
			@Override
			public void valueChanged(ListSelectionEvent e) {
				// TODO Auto-generated method stub
				int z=jList1.getSelectedIndex();
				String s=jList1.getSelectedValue();
				if(s.equals("")) {
					return ;
				}
				
				CyNetwork network=appAdapter.getCyApplicationManager().getCurrentNetwork();
        		int i=0;
        		for(i=0;i<networks.size();i++) {
        			if(network.equals(networks.get(i))) break;
        		}
				for(CyNetwork net:networks) {
					List<CyNode> nodes = CyTableUtil.getNodesInState(net,"selected",true);
					for(CyNode node:nodes) {
						net.getRow(node).set("selected", false);
					}
				}
				
				
				if(jRadioButton2.isSelected()) {
					List<CyNode> nodeList=network.getNodeList();
					List<CyNode> selectList=new ArrayList<>();
					for(int k=0;k<cd.get(z).get(i).size();k++) {
						int c=cd.get(z).get(i).get(k);
						selectList.add(nodeList.get(c));
					}
					for(CyNode node:selectList) {
						network.getRow(node).set("selected", true);
					}
				}else if(jRadioButton1.isSelected()){
					int j=-1;
					for(CyNetwork net:networks) {
						j++;
						List<CyNode> nodeList=net.getNodeList();
						List<CyNode> selectList=new ArrayList<>();
						for(int k=0;k<cd.get(z).get(j).size();k++) {
							int c=cd.get(z).get(i).get(k);
							selectList.add(nodeList.get(c));
						}
						for(CyNode node:selectList) {
							net.getRow(node).set("selected", true);
						}
					}
				}
			}
		});
		final CytoPanel cytoPanel = appAdapter.getCySwingApplication().getCytoPanel(CytoPanelName.EAST);
		oldState = cytoPanel.getState();
	}
	
	private void initComponents() {

        buttonGroup1 = new javax.swing.ButtonGroup();
        jRadioButton1 = new javax.swing.JRadioButton();
        jRadioButton2 = new javax.swing.JRadioButton();
        jPanel1 = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        jList1 = new javax.swing.JList<>();
        jButton=new JButton("close");
        buttonGroup1.add(jRadioButton1);
        jRadioButton1.setSelected(true);
        jRadioButton1.setText("List all clustres");
        

        buttonGroup1.add(jRadioButton2);
        jRadioButton2.setText("List clusters at thie time");
        

        jPanel1.setBorder(javax.swing.BorderFactory.createTitledBorder("Clusters brows 5"));

        jList1=new JList<>(allListModel);
        jScrollPane1.setViewportView(jList1);

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane1,GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addGroup(jPanel1Layout.createSequentialGroup()
            	.addGap(49,49,49)
            	.addComponent(jButton)
            	.addContainerGap(GroupLayout.DEFAULT_SIZE,Short.MAX_VALUE))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
            	.addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 218, Short.MAX_VALUE)
            	.addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
            	.addComponent(jButton))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jRadioButton1)
                    .addComponent(jRadioButton2))
                .addGap(0, 9, Short.MAX_VALUE))
            .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jRadioButton1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jRadioButton2)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
    }
	
	@Override
	public Component getComponent() {
		// TODO Auto-generated method stub
		return this;
	}

	@Override
	public CytoPanelName getCytoPanelName() {
		// TODO Auto-generated method stub
		return CytoPanelName.EAST;
	}

	@Override
	public String getTitle() {
		// TODO Auto-generated method stub
		return "DyNet Clusters Results";
	}

	@Override
	public Icon getIcon() {
		// TODO Auto-generated method stub
		return null;
	}
	private void panelClosing() {
		// TODO Auto-generated method stub
		final CytoPanel cytoPanel = appAdapter.getCySwingApplication().getCytoPanel(CytoPanelName.EAST);
		int panelCount = cytoPanel.getCytoPanelComponentCount();
		for(int i=0; i<panelCount; i++) {
			Component panel = cytoPanel.getComponentAt(i);
			ClusterPanel c=this;
			if(panel.equals(c)) {
				if(c!=null)
					appAdapter.getCyServiceRegistrar().unregisterAllServices(c);
				c = null;
				break;
			}
		}
	}

	@Override
	public void handleEvent(CytoPanelStateChangedEvent e) {
		// TODO Auto-generated method stub
		if( oldState != CytoPanelState.DOCK && e.getNewState() == CytoPanelState.DOCK )
		{
			final CytoPanel cytoPanel = appAdapter.getCySwingApplication().getCytoPanel(CytoPanelName.EAST);
			JPanel thisPanel = (JPanel)cytoPanel.getThisComponent();
			int x = 5;
		}

		//If the oldState was already FLOAT, or the new state is not FLOAT, no need to do anything.
		if( oldState == CytoPanelState.FLOAT || e.getNewState() != CytoPanelState.FLOAT )
		{
			//Don't do anything, but before we leave, record the new state for future reference.
			oldState = e.getNewState();
			return;
		}
		//Record the new state for future reference.
	    oldState = e.getNewState();

		//Get the EAST cytoPanel, which we are going to resize below.
		final CytoPanel cytoPanel = appAdapter.getCySwingApplication().getCytoPanel(CytoPanelName.EAST);

		//Check that the resultPanel is currently selected. If it is not, ignore the event.
		if(!cytoPanel.getSelectedComponent().equals(this))
			return;

		JPanel thisPanel = (JPanel)cytoPanel.getThisComponent();
		JDialog dialog = (JDialog) thisPanel.getTopLevelAncestor();
		dialog.setSize(680,540);
	}


}
