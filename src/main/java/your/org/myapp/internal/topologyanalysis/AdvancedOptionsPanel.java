package your.org.myapp.internal.topologyanalysis;

import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.LineBorder;

import org.cytoscape.model.CyColumn;
import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.util.swing.BasicCollapsiblePanel;

public class AdvancedOptionsPanel extends BasicCollapsiblePanel {
	
	
	private JPanel nodeCheckBoxPanel;
	private JPanel edgeCheckBoxPanel;
	
	private List<String> validNodeAttributes;
	private List<String> validEdgeAttributes;
	
	private List<String> selectedNodeAttributes;
	private List<String> selectedEdgeAttributes;
	
	private SetupDialog parentDialog;
	
	
	public AdvancedOptionsPanel(final List<CyNetwork> selectedNetworks, final SetupDialog parentDialog,
			List<String> previousSelectedNodeAttributes, List<String> previousSelectedEdgeAttributes) {
		super("Advanced options");
		this.parentDialog = parentDialog;
		
		extractValidAttributes();
		selectedNodeAttributes = new ArrayList<String>();
		selectedEdgeAttributes = new ArrayList<String>();
		
		
		nodeCheckBoxPanel = new JPanel();
		nodeCheckBoxPanel.setBorder(new LineBorder(new Color(0, 0, 0)));
		nodeCheckBoxPanel.setLayout(new BoxLayout(nodeCheckBoxPanel, BoxLayout.Y_AXIS));
		for (String nodeAttribute : validNodeAttributes){
			JCheckBox attributeCheckBox = new JCheckBox(nodeAttribute);
			attributeCheckBox.addItemListener(new ItemListener() {
				@Override
				public void itemStateChanged(ItemEvent e) {
					if (e.getStateChange() == ItemEvent.SELECTED){
						selectedNodeAttributes.add(((JCheckBox)e.getSource()).getText());
					}else if (e.getStateChange() == ItemEvent.DESELECTED){
						selectedNodeAttributes.remove(((JCheckBox)e.getSource()).getText());
					}
					parentDialog.setSelectedNodeAttributes(selectedNodeAttributes);
				}
			});
			
			if (previousSelectedNodeAttributes != null){
				if (previousSelectedNodeAttributes.contains(nodeAttribute)){
					attributeCheckBox.setSelected(true);
				}else{
					attributeCheckBox.setSelected(false);
				}
			}else{
				if (nodeAttribute.equals("Degree")) attributeCheckBox.setSelected(true);
			}
			
			nodeCheckBoxPanel.add(attributeCheckBox);
		}
		
		
		
		edgeCheckBoxPanel = new JPanel();
		edgeCheckBoxPanel.setBorder(new LineBorder(new Color(0, 0, 0)));
		edgeCheckBoxPanel.setLayout(new BoxLayout(edgeCheckBoxPanel, BoxLayout.Y_AXIS));
		for (String edgeAttribute : validEdgeAttributes){
			JCheckBox attributeCheckBox = new JCheckBox(edgeAttribute);
			attributeCheckBox.addItemListener(new ItemListener() {
				@Override
				public void itemStateChanged(ItemEvent e) {
					if (e.getStateChange() == ItemEvent.SELECTED){
						selectedEdgeAttributes.add(((JCheckBox)e.getSource()).getText());
					}else if (e.getStateChange() == ItemEvent.DESELECTED){
						selectedEdgeAttributes.remove(((JCheckBox)e.getSource()).getText());
					}
					parentDialog.setSelectedEdgeAttributes(selectedEdgeAttributes);
				}
			});
			if (previousSelectedEdgeAttributes != null){
				if (previousSelectedEdgeAttributes.contains(edgeAttribute)){
					attributeCheckBox.setSelected(true);
				}else{
					attributeCheckBox.setSelected(false);
				}
			}else{
				if (edgeAttribute.equals("EdgeBetweenness")) attributeCheckBox.setSelected(true);
			}
			edgeCheckBoxPanel.add(attributeCheckBox);
		}
		
		
		GridBagLayout gridBagLayout = new GridBagLayout();
		gridBagLayout.columnWidths = new int[]{10, 150, 10, 150, 10, 0};
		gridBagLayout.rowHeights = new int[]{10, 0, 60, 10, 0};
		gridBagLayout.columnWeights = new double[]{0.0, 1.0, 0.0, 1.0, 0.0, Double.MIN_VALUE};
		gridBagLayout.rowWeights = new double[]{0.0, 0.0, 0.0, 0.0, Double.MIN_VALUE};
		getContentPane().setLayout(gridBagLayout);
		
		JLabel groupNodesLabel = new JLabel("Analyse nodes with props:");
		GridBagConstraints gbc_groupNodesLabel = new GridBagConstraints();
		gbc_groupNodesLabel.anchor = GridBagConstraints.WEST;
		gbc_groupNodesLabel.insets = new Insets(0, 0, 5, 5);
		gbc_groupNodesLabel.gridx = 1;
		gbc_groupNodesLabel.gridy = 1;
		add(groupNodesLabel, gbc_groupNodesLabel);
		
		JLabel groupEdgesLabel = new JLabel("Analyse edges with props:");
		GridBagConstraints gbc_groupEdgesLabel = new GridBagConstraints();
		gbc_groupEdgesLabel.anchor = GridBagConstraints.WEST;
		gbc_groupEdgesLabel.insets = new Insets(0, 0, 5, 5);
		gbc_groupEdgesLabel.gridx = 3;
		gbc_groupEdgesLabel.gridy = 1;
		add(groupEdgesLabel, gbc_groupEdgesLabel);
		
		GridBagConstraints gbc_nodeCheckBoxPanel = new GridBagConstraints();
		gbc_nodeCheckBoxPanel.insets = new Insets(0, 0, 5, 5);
		gbc_nodeCheckBoxPanel.fill = GridBagConstraints.BOTH;
		gbc_nodeCheckBoxPanel.gridx = 1;
		gbc_nodeCheckBoxPanel.gridy = 2;
		add(nodeCheckBoxPanel, gbc_nodeCheckBoxPanel);
		
		GridBagConstraints gbc_edgeCheckBoxPanel = new GridBagConstraints();
		gbc_edgeCheckBoxPanel.insets = new Insets(0, 0, 5, 5);
		gbc_edgeCheckBoxPanel.fill = GridBagConstraints.BOTH;
		gbc_edgeCheckBoxPanel.gridx = 3;
		gbc_edgeCheckBoxPanel.gridy = 2;
		add(edgeCheckBoxPanel, gbc_edgeCheckBoxPanel);
	}
	
	@Override
	public void setCollapsed(boolean collapse) {
		super.setCollapsed(collapse);
		if (parentDialog != null){
			parentDialog.collapseStateChanged();
		}
	}

	public List<String> getSelectedNodeAttributes(){
		return selectedNodeAttributes;
	}
	
	public List<String> getSelectedEdgeAttributes(){
		return selectedEdgeAttributes;
	}

	
	//valid attributes are the ones that are present and have the same type in all included networks
	private void extractValidAttributes(){
		
		validNodeAttributes = new ArrayList<String>();
		validNodeAttributes.add("Degree");
		validNodeAttributes.add("Clusteringcoefficient");
		validNodeAttributes.add("DegreeCentrality");
		validNodeAttributes.add("ClosenessCentrality");
		validNodeAttributes.add("SelfLoop");
		validNodeAttributes.add("NeighborsConnectivity");
		validNodeAttributes.add("BetweennessCentrality");
		validNodeAttributes.add("AverageShortestPathLength");
		validNodeAttributes.add("IsSingleNode");
		
		validEdgeAttributes = new ArrayList<String>();
		validEdgeAttributes.add("EdgeBetweenness");
		validEdgeAttributes.add("EdgeClusteringcoefficient");
	
	}
}
