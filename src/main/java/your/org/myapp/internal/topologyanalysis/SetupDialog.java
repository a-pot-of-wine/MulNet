package your.org.myapp.internal.topologyanalysis;

import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;

import javax.swing.ButtonGroup;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.cytoscape.app.swing.CySwingAppAdapter;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.work.TaskIterator;

import your.org.myapp.internal.CyActivator;


public class SetupDialog extends JDialog {
	
	
	private JButton cancelButton;
	private JButton okButton;
	private AdvancedOptionsPanel advancedOptionsPanel;
	private JList<CyNetwork> networkList;
	private JRadioButton directedRadioButton;
	private JRadioButton undirectedRadioButton;
	
	
	//the node attributes that will be used for identifying corresponding nodes and edges
	//Example: if the attribute "Interaction Type" is selected, then edges that connect the same two nodes but
	//have different interaction types in different networks will not be considered as a single edge in the union network.
	private List<String> selectedNodeAttributes; 
	private List<String> selectedEdgeAttributes;
	
	private List<CyNetwork> selectedNetworks;
	private boolean treatAsDirected;
	
	
	private JScrollPane networkListScrollPane;
	private JLabel treatNetworkAsLabel;
	private JLabel selectIncludedNetworksLabel;
	

	
	//the previous collapsed state of the advancedOptionsPanel, so that it can stay expanded 
	//when user chooses different networks
	private boolean previousCollapsedState = true;
	
	
	
	public SetupDialog(final CySwingAppAdapter appAdapter, final CyActivator activator) {
		
		setModalityType(ModalityType.APPLICATION_MODAL);
		setTitle("Analyse networks setting");
		GridBagLayout gridBagLayout = new GridBagLayout();
		gridBagLayout.columnWidths = new int[]{10, 160, 20, 0, 80, 80, 10, 0};
		gridBagLayout.rowHeights = new int[]{10, 0, 50, 0, 0, 50, 0, 0, 0, 10, 0};
		gridBagLayout.columnWeights = new double[]{0.0, 1.0, 1.0, 1.0, 0.0, 0.0, 0.0, Double.MIN_VALUE};
		gridBagLayout.rowWeights = new double[]{0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, Double.MIN_VALUE};
		getContentPane().setLayout(gridBagLayout);
		
		
		
		
		networkList = new JList<CyNetwork>(){
			@Override
			public boolean getScrollableTracksViewportHeight() {
				return false;
			}
		};
		DefaultListModel<CyNetwork> listModel = new DefaultListModel<CyNetwork>();
		for (CyNetwork network : appAdapter.getCyNetworkManager().getNetworkSet()){
			listModel.addElement(network);
		}
		networkList.setModel(listModel);
		networkList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		networkList.addListSelectionListener(new ListSelectionListener() {
			@Override
			public void valueChanged(ListSelectionEvent e) {
				if (e.getValueIsAdjusting() == false){
					selectedNetworks = ((JList<CyNetwork>)e.getSource()).getSelectedValuesList();
					resetAdvancedOptionsPanel();
				}
			}
		});
		networkList.setSelectionInterval(0, listModel.size() - 1);
		
		MouseAdapter reorderListener = new MouseAdapter() {
			private int pressIndex = 0;
			private int releaseIndex = 0;
			
			@Override
			public void mousePressed(MouseEvent e) {
				pressIndex = ((JList)e.getSource()).locationToIndex(e.getPoint());
			}
	
			@Override
			public void mouseReleased(MouseEvent e) {
				releaseIndex = ((JList)e.getSource()).locationToIndex(e.getPoint());
				if (releaseIndex != pressIndex && releaseIndex != -1) {
					DefaultListModel model = (DefaultListModel) ((JList)e.getSource()).getModel();
					Object item = model.elementAt(pressIndex);
					model.removeElementAt(pressIndex);
					model.insertElementAt(item, releaseIndex);
				}
			}
	
			@Override
			public void mouseDragged(MouseEvent e) {
				mouseReleased(e);
				pressIndex = releaseIndex;
			}
		};
		networkList.addMouseListener(reorderListener);
		networkList.addMouseMotionListener(reorderListener);
		
		
		//provides selection of the initial layout to use
		
		directedRadioButton = new JRadioButton("Directed networks");
		directedRadioButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				treatAsDirected = true;
			}
		});
		
		
		undirectedRadioButton = new JRadioButton("Undirected networks");
		undirectedRadioButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				treatAsDirected = false;
			}
		});
		
		
		ButtonGroup group = new ButtonGroup();
		group.add(directedRadioButton);
		group.add(undirectedRadioButton);
		undirectedRadioButton.setSelected(true);
		

		
		
		cancelButton = new JButton("Cancel");
		cancelButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				dispose();
			}
		});
		
		
		
		okButton = new JButton("OK");
		okButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (selectedNetworks == null || selectedNetworks.size() < 1){
					JOptionPane.showMessageDialog(SetupDialog.this, "Please select at least 1 networks");
				}else{
					NetworksParameters nPar=new NetworksParameters(treatAsDirected, selectedNodeAttributes, selectedEdgeAttributes);
					appAdapter.getTaskManager().execute(new TaskIterator(
							new TopoAnalyseTask(appAdapter, selectedNetworks, nPar)));
					dispose();
				}
			}
		});
		
			
		selectIncludedNetworksLabel = new JLabel("Select included networks:");
		GridBagConstraints gbc_selectIncludedNetworksLabel = new GridBagConstraints();
		gbc_selectIncludedNetworksLabel.anchor = GridBagConstraints.SOUTHWEST;
		gbc_selectIncludedNetworksLabel.insets = new Insets(0, 0, 5, 5);
		gbc_selectIncludedNetworksLabel.gridx = 1;
		gbc_selectIncludedNetworksLabel.gridy = 1;
		getContentPane().add(selectIncludedNetworksLabel, gbc_selectIncludedNetworksLabel);
		
	
		
		networkListScrollPane = new JScrollPane();
		networkListScrollPane.getViewport().setBackground(Color.WHITE);
		GridBagConstraints gbc_networkListScrollPane = new GridBagConstraints();
		gbc_networkListScrollPane.gridheight = 4;
		gbc_networkListScrollPane.insets = new Insets(0, 0, 5, 5);
		gbc_networkListScrollPane.fill = GridBagConstraints.BOTH;
		gbc_networkListScrollPane.gridx = 1;
		gbc_networkListScrollPane.gridy = 2;
		getContentPane().add(networkListScrollPane, gbc_networkListScrollPane);
		networkListScrollPane.setViewportView(networkList);
		
	
	
		treatNetworkAsLabel = new JLabel("Treat networks as:");
		GridBagConstraints gbc_treatNetworkAsLabel = new GridBagConstraints();
		gbc_treatNetworkAsLabel.anchor = GridBagConstraints.SOUTHWEST;
		gbc_treatNetworkAsLabel.gridwidth = 3;
		gbc_treatNetworkAsLabel.insets = new Insets(0, 0, 5, 5);
		gbc_treatNetworkAsLabel.gridx = 3;
		gbc_treatNetworkAsLabel.gridy = 3;
		getContentPane().add(treatNetworkAsLabel, gbc_treatNetworkAsLabel);
		
		
		GridBagConstraints gbc_directedCheckBox = new GridBagConstraints();
		gbc_directedCheckBox.anchor = GridBagConstraints.NORTHWEST;
		gbc_directedCheckBox.gridwidth = 3;
		gbc_directedCheckBox.insets = new Insets(0, 0, 5, 5);
		gbc_directedCheckBox.gridx = 3;
		gbc_directedCheckBox.gridy = 4;
		getContentPane().add(directedRadioButton, gbc_directedCheckBox);
		
		
		GridBagConstraints gbc_undirectedCheckBox = new GridBagConstraints();
		gbc_undirectedCheckBox.anchor = GridBagConstraints.NORTHWEST;
		gbc_undirectedCheckBox.gridwidth = 3;
		gbc_undirectedCheckBox.insets = new Insets(0, 0, 5, 5);
		gbc_undirectedCheckBox.gridx = 3;
		gbc_undirectedCheckBox.gridy = 5;
		getContentPane().add(undirectedRadioButton, gbc_undirectedCheckBox);
		
		GridBagConstraints gbc_cancelButton = new GridBagConstraints();
		gbc_cancelButton.fill = GridBagConstraints.HORIZONTAL;
		gbc_cancelButton.insets = new Insets(0, 0, 5, 5);
		gbc_cancelButton.gridx = 4;
		gbc_cancelButton.gridy = 7;
		getContentPane().add(cancelButton, gbc_cancelButton);
		
		
		GridBagConstraints gbc_okButton = new GridBagConstraints();
		gbc_okButton.fill = GridBagConstraints.HORIZONTAL;
		gbc_okButton.insets = new Insets(0, 0, 5, 5);
		gbc_okButton.gridx = 5;
		gbc_okButton.gridy = 7;
		getContentPane().add(okButton, gbc_okButton);
		
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				setMinimumSize(getPreferredSize());
				pack();
				setLocationRelativeTo(appAdapter.getCySwingApplication().getJFrame());
				setVisible(true);
			}
		});
	}

	private void resetAdvancedOptionsPanel(){
		if (advancedOptionsPanel != null){
			getContentPane().remove(advancedOptionsPanel);
		}
		
		if (selectedNetworks != null && selectedNetworks.size() >= 1){
			advancedOptionsPanel = new AdvancedOptionsPanel(selectedNetworks, SetupDialog.this, selectedNodeAttributes, selectedEdgeAttributes);
			GridBagConstraints gbc_advancedOptionsPanel = new GridBagConstraints();
			gbc_advancedOptionsPanel.gridwidth = 5;
			gbc_advancedOptionsPanel.insets = new Insets(0, 0, 5, 5);
			gbc_advancedOptionsPanel.fill = GridBagConstraints.BOTH;
			gbc_advancedOptionsPanel.gridx = 1;
			gbc_advancedOptionsPanel.gridy = 6;
			getContentPane().add(advancedOptionsPanel, gbc_advancedOptionsPanel);
			advancedOptionsPanel.setCollapsed(previousCollapsedState);
		}else{
			previousCollapsedState = true;
		}
		
		setMinimumSize(getPreferredSize());
		pack();
		revalidate();
		repaint();
	}

	public void collapseStateChanged() {
		previousCollapsedState = advancedOptionsPanel.isCollapsed();
		setMinimumSize(getPreferredSize());
		pack();
		revalidate();
		repaint();
	}
	
	public void setSelectedNodeAttributes(List<String> selectedNodeAttributes){
		this.selectedNodeAttributes = selectedNodeAttributes;
	}
	
	public void setSelectedEdgeAttributes(List<String> selectedEdgeAttributes){
		this.selectedEdgeAttributes = selectedEdgeAttributes;
	}
}

