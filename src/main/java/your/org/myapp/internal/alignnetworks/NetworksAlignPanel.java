package your.org.myapp.internal.alignnetworks;

import java.awt.Color;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.Timer;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

import javax.swing.DefaultListModel;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;

import org.cytoscape.app.swing.CySwingAppAdapter;
import org.cytoscape.application.swing.CytoPanelComponent;
import org.cytoscape.application.swing.CytoPanelName;
import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNetworkManager;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.CyTableUtil;
import org.cytoscape.model.events.NetworkAboutToBeDestroyedEvent;
import org.cytoscape.model.events.NetworkAboutToBeDestroyedListener;
import org.cytoscape.model.events.NetworkAddedEvent;
import org.cytoscape.model.events.NetworkAddedListener;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.presentation.property.BasicVisualLexicon;
import org.cytoscape.view.vizmap.VisualStyle;
import org.cytoscape.view.vizmap.mappings.DiscreteMapping;
import org.cytoscape.view.vizmap.mappings.PassthroughMapping;
import org.cytoscape.work.TaskIterator;
import org.cytoscape.work.TaskManager;

public class NetworksAlignPanel extends JPanel implements CytoPanelComponent,NetworkAddedListener,NetworkAboutToBeDestroyedListener{
	
	private static final long serialVersionUID = 8292806967891823933L;
	private static final int DEFAULT_NONIMPROVING = 20;
    private static final int DEFAULT_PERTURBATION = 20;
    
    private final CySwingAppAdapter swingAdapter;
    private  CyNetworkManager networkManager;
    private TaskManager taskManager;
//    private CyNetworkFactory networkFactory;
//    private CyServiceRegistrar service;
//    private CyNetworkViewManager viewManager;
//    private CyNetworkViewFactory viewFactory;
    
    private final DefaultListModel<CyNetwork> availableListModel;
    private final DefaultListModel<CyNetwork> selectedListModel;
    private final SpinnerNumberModel exceptionsSpinnerModel;
    private JSpinner exceptionsSpinner;
    private ArrayList<String> nodesAttrs;
    private ArrayList<String> edgesAttrs;
    
    private javax.swing.ButtonGroup buttonGroup1;
    private javax.swing.ButtonGroup buttonGroup2;
    private javax.swing.JButton jButton1;
    private javax.swing.JButton jButton2;
    private javax.swing.JButton jButton3;
    private javax.swing.JButton jButton4;
    private javax.swing.JButton jButton5;
    private javax.swing.JButton jButton6;
    private javax.swing.JButton jButton7;
    private javax.swing.JButton jButton8;
    private javax.swing.JButton jButton9;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JList<CyNetwork> jList1;
    private javax.swing.JList<CyNetwork> jList2;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JRadioButton jRadioButton1;
    private javax.swing.JRadioButton jRadioButton2;
    private javax.swing.JRadioButton jRadioButton3;
    private javax.swing.JRadioButton jRadioButton4;
    private javax.swing.JRadioButton jRadioButton5;
    private javax.swing.JRadioButton jRadioButton6;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JSlider jSlider1;
    List<Color> colors=Arrays.asList(new Color(220,20,60), new Color(255,20,147), new Color(255,182,193), new Color(255,0,255), new Color(219,112,147), new Color(148,0,211),
			new Color(75,0,130), new Color(65,105,225), new Color(0,0,205), new Color(0,191,255), new Color(119,136,153), new Color(225,255,255), new Color(0,255,255),
			new Color(0,206,209), new Color(64,224,208), new Color(245,255,250), new Color(50,205,50), new Color(0,255,0), new Color(0,100, 0), new Color(173,255, 47),
			new Color( 107, 142, 35), new Color( 255, 255, 224), new Color( 255, 255, 0), new Color(189, 183, 107), new Color( 255, 215, 0), new Color( 255, 250, 240), 
			new Color(210,180,140), new Color(210,105,30), new Color(0,0,0), new Color(192,192,192)) ;
    private static final ExecutorService exec=Executors.newSingleThreadExecutor(new SwingThreadFactory());
	private static volatile Thread swingThread;
	private volatile boolean cancelled=false;
	private static class SwingThreadFactory implements ThreadFactory{

		@Override
		public Thread newThread(Runnable r) {
			// TODO Auto-generated method stub
			swingThread=new Thread(r);
			return swingThread;
		}
		
	}
    public NetworksAlignPanel(CySwingAppAdapter swingAdapter) {
    	
    	this.swingAdapter=swingAdapter;
//    	service=swingAdapter.getCyServiceRegistrar();
//    	networkFactory=swingAdapter.getCyNetworkFactory();
    	this.networkManager=swingAdapter.getCyNetworkManager();
    	taskManager=swingAdapter.getTaskManager();
//    	viewFactory=swingAdapter.getCyNetworkViewFactory();
//   	viewManager=swingAdapter.getCyNetworkViewManager();
    	availableListModel = new DefaultListModel<>();
        selectedListModel = new DefaultListModel<>();
        nodesAttrs=new ArrayList<String>();
        edgesAttrs=new ArrayList<String>();
        for (CyNetwork network : networkManager.getNetworkSet()) {
            availableListModel.addElement(network);
        }

        exceptionsSpinnerModel = new SpinnerNumberModel(0, 0, 0, 1);

        createGUI();
        
        jButton1.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
            	List<CyNetwork> selected = jList1.getSelectedValuesList();
                for (CyNetwork network : selected) {
                    availableListModel.removeElement(network);
                    selectedListModel.addElement(network);
                }
                exceptionsSpinnerModel.setMaximum(numSelected());
            }
        });
        
        jButton2.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
            	List<CyNetwork> selected = jList2.getSelectedValuesList();
                for (CyNetwork network : selected) {
                    selectedListModel.removeElement(network);
                    availableListModel.addElement(network);
                }
                exceptionsSpinnerModel.setMaximum(numSelected());
                if((Integer)exceptionsSpinner.getValue() > (Integer)exceptionsSpinnerModel.getMaximum()) {
                    exceptionsSpinnerModel.setValue(exceptionsSpinnerModel.getMaximum());
                }
            }
        });
     
        
        jButton3.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
            	CyNetwork network=swingAdapter.getCyApplicationManager().getCurrentNetwork();
            	if(network==null) {
            		throw new RuntimeException("Please select one node");
            	}
            	List<CyNode> nodes=CyTableUtil.getNodesInState(network,"selected",true);
                
                if(nodes.size()!=1) {
                	JOptionPane.showMessageDialog(null, "Please select one node.  "+nodes.size());
                	return;
                }
                else {
                	 
                	List<CyNetwork> networks = new ArrayList<>();
                	Enumeration<CyNetwork> networks_enum = selectedListModel.elements();
                	while (networks_enum.hasMoreElements()) networks.add(networks_enum.nextElement());
                	String nodeName=network.getRow(nodes.get(0)).get("name", String.class);
                	taskManager.execute(new TaskIterator(new NodesPanelTask(swingAdapter,networks,nodeName)));
                }
           }
        });
         
        jButton4.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
            	List<CyNetwork> networks = new ArrayList<>();
            	Enumeration<CyNetwork> networks_enum = selectedListModel.elements();
                while (networks_enum.hasMoreElements()) networks.add(networks_enum.nextElement());
                jSlider1.setMaximum(networks.size()-1);
                if(networks.size()>=2) {
                	jLabel9.setText("0 (total time-course is: "+(networks.size()-1)+")");
                	jButton7.setEnabled(true);
                 	jButton8.setEnabled(true);
                 	jButton9.setEnabled(true);
                }
               
            	boolean directed=jRadioButton2.isSelected();
            	
                taskManager.execute(new TaskIterator(new showDyNetTask(swingAdapter, networks, directed)));
            }
        });
        jButton5.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				 CyNetwork network=swingAdapter.getCyApplicationManager().getCurrentNetwork();
				 if(network==null) {
	            		throw new RuntimeException("Please select one edge");
	            }
				 List<CyEdge> edges=CyTableUtil.getEdgesInState(network,"selected",true);
	             if (edges.size()!=1) {
	            	 JOptionPane.showMessageDialog(null, "Please select one edge.  "+edges.size());
	                	return;
	             }
	             else {
					List<CyNetwork> networks = new ArrayList<>();
	            	Enumeration<CyNetwork> networks_enum = selectedListModel.elements();
	                while (networks_enum.hasMoreElements()) networks.add(networks_enum.nextElement());
                	String edgeName=network.getRow(edges.get(0)).get("name", String.class);
                	taskManager.execute(new TaskIterator(new EdgesPanelTask(swingAdapter,networks,edgeName)));
                }
				
			}
		});
        jButton6.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
            	List<CyNetwork> networks = new ArrayList<>();
            	Enumeration<CyNetwork> networks_enum = selectedListModel.elements();
                while (networks_enum.hasMoreElements()) networks.add(networks_enum.nextElement());
                jButton7.setEnabled(false);
            	jButton8.setEnabled(false);
            	jButton9.setEnabled(false);
                
            	boolean directed=jRadioButton2.isSelected();
            	String algorithm = null;
            	if(jRadioButton3.isSelected()) algorithm=jRadioButton3.getText();
            	else if(jRadioButton4.isSelected()) algorithm=jRadioButton4.getText();
            	else if(jRadioButton5.isSelected()) algorithm=jRadioButton5.getText();
            	else if(jRadioButton6.isSelected()) algorithm=jRadioButton6.getText();
        
                taskManager.execute(new TaskIterator(new showAlignTask(swingAdapter,networks,directed,algorithm)));
                //taskManager.execute(new TaskIterator(initialTasks));
            }
        });
        
        jButton7.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
            	List<CyNetwork> networks = new ArrayList<>();
            	Enumeration<CyNetwork> networks_enum = selectedListModel.elements();
                while (networks_enum.hasMoreElements()) networks.add(networks_enum.nextElement());
                
                exec.execute(new Runnable() {
					public void run() {
						cancelled=false;
						
						jButton9.setEnabled(false);
						int i=jSlider1.getValue();
						while(!cancelled&&i>jSlider1.getMinimum()){
							jButton7.setEnabled(false);
							i--;
							jSlider1.setValue(i);
							jLabel9.setText(i+" (total time-course is:  "+(networks.size()-1)+")");
							CyNetwork network=networks.get(i);
        					
		        			swingAdapter.getCyApplicationManager().setCurrentNetwork(network);
		        			
		        			CyNetworkView view=swingAdapter.getCyApplicationManager().getCurrentNetworkView();
		        			Iterator it = swingAdapter.getVisualMappingManager().getAllVisualStyles().iterator();
		        			VisualStyle vs=null;
		        			while (it.hasNext()){
		        				vs = (VisualStyle)it.next();
		        				if (vs.getTitle().equals("DyNet"))
		        					break;
		        			}
		        			swingAdapter.getVisualMappingManager().setVisualStyle(vs, view);
		        			view.updateView();
							try {
								Thread.currentThread();
								Thread.sleep(2000);
								jButton7.setEnabled(true);
							} catch (InterruptedException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
							
						}
						jButton7.setEnabled(true);
						jButton9.setEnabled(true);
					}
				});
            }
        });
        
        jButton8.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
            	jButton7.setEnabled(true);
				jButton9.setEnabled(true);
				cancelled=true;
            }
        });
        
        jButton9.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
            	List<CyNetwork> networks = new ArrayList<>();
            	Enumeration<CyNetwork> networks_enum = selectedListModel.elements();
                while (networks_enum.hasMoreElements()) networks.add(networks_enum.nextElement());
                
                exec.execute(new Runnable() {
					public void run() {
						cancelled=false;
						jButton7.setEnabled(false);
						
						int i=jSlider1.getValue();
						while(!cancelled&&i<jSlider1.getMaximum()){
							jButton9.setEnabled(false);
							i++;
							jSlider1.setValue(i);
							jLabel9.setText(i+" (total time-course is:  "+(networks.size()-1)+")");
							CyNetwork network=networks.get(i);
        					
		        			swingAdapter.getCyApplicationManager().setCurrentNetwork(network);
		        			
		        			CyNetworkView view=swingAdapter.getCyApplicationManager().getCurrentNetworkView();
		        			Iterator it = swingAdapter.getVisualMappingManager().getAllVisualStyles().iterator();
		        			VisualStyle vs=null;
		        			while (it.hasNext()){
		        				vs = (VisualStyle)it.next();
		        				if (vs.getTitle().equals("DyNet"))
		        					break;
		        			}
		        			swingAdapter.getVisualMappingManager().setVisualStyle(vs, view);
		        			view.updateView();
							try {
								Thread.currentThread();
								Thread.sleep(2000);
								
							} catch (InterruptedException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
						}
						jButton7.setEnabled(true);
						jButton9.setEnabled(true);
					}
				});
            }
        });
    }
    
    @Override
    public void handleEvent(NetworkAddedEvent e) {
		availableListModel.addElement(e.getNetwork());
	}

	@Override
	public void handleEvent(NetworkAboutToBeDestroyedEvent e) {
		availableListModel.removeElement(e.getNetwork());
        selectedListModel.removeElement(e.getNetwork());
	}
    
	@Override
	public Component getComponent() {
		// TODO Auto-generated method stub
		return this;
	}

	@Override
	public CytoPanelName getCytoPanelName() {
		// TODO Auto-generated method stub
		return CytoPanelName.WEST;
	}

	@Override
	public String getTitle() {
		// TODO Auto-generated method stub
		return "Aligning Networks";
	}

	@Override
	public Icon getIcon() {
		// TODO Auto-generated method stub
		return null;
	}

	
	private void createGUI() {
		buttonGroup1 = new javax.swing.ButtonGroup();
        buttonGroup2 = new javax.swing.ButtonGroup();
        jPanel2 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        jList1 = new javax.swing.JList<>();
        jScrollPane2 = new javax.swing.JScrollPane();
        jList2 = new javax.swing.JList<>();
        jButton1 = new javax.swing.JButton();
        jButton2 = new javax.swing.JButton();
        jLabel4 = new javax.swing.JLabel();
        jRadioButton1 = new javax.swing.JRadioButton();
        jRadioButton2 = new javax.swing.JRadioButton();
        jPanel3 = new javax.swing.JPanel();
        jLabel6 = new javax.swing.JLabel();
        jButton3 = new javax.swing.JButton();
        jLabel7 = new javax.swing.JLabel();
        jButton5 = new javax.swing.JButton();
        jPanel4 = new javax.swing.JPanel();
        jLabel3 = new javax.swing.JLabel();
        jRadioButton3 = new javax.swing.JRadioButton();
        jRadioButton4 = new javax.swing.JRadioButton();
        jRadioButton5 = new javax.swing.JRadioButton();
        jRadioButton6 = new javax.swing.JRadioButton();
        jButton6 = new javax.swing.JButton();
        jPanel1 = new javax.swing.JPanel();
        jButton4 = new javax.swing.JButton();
        jSlider1 = new javax.swing.JSlider();
        jLabel8 = new javax.swing.JLabel();
        jLabel9 = new javax.swing.JLabel();
        jButton7 = new javax.swing.JButton();
        jButton9 = new javax.swing.JButton();
        jButton8= new javax.swing.JButton();

        setBackground(new java.awt.Color(240, 240, 242));

        jPanel2.setBorder(javax.swing.BorderFactory.createTitledBorder("Select networks to align or dy-analyse"));

        jLabel1.setText("Available");

        jLabel2.setText("Selected");

        jList1=new JList<>(availableListModel);
        jScrollPane1.setViewportView(jList1);

        jList2=new JList<>(selectedListModel);
        jScrollPane2.setViewportView(jList2);

        jButton1.setText(">");
      
        jButton2.setText("<");
       
        jLabel4.setText("Treat networks as:");

        buttonGroup1.add(jRadioButton1);
        jRadioButton1.setSelected(true);
        jRadioButton1.setText("Undirected");
       
        buttonGroup1.add(jRadioButton2);
        jRadioButton2.setText("Directed");
       

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel4)
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel1)
                            .addGroup(jPanel2Layout.createSequentialGroup()
                                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 137, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(18, 18, 18)
                                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(jButton2, javax.swing.GroupLayout.Alignment.TRAILING)
                                    .addComponent(jButton1, javax.swing.GroupLayout.Alignment.TRAILING))))
                        .addGap(18, 18, Short.MAX_VALUE)
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel2)
                            .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 139, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jRadioButton2)))
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addGap(55, 55, 55)
                        .addComponent(jRadioButton1)))
                .addContainerGap())
        );

        jPanel2Layout.linkSize(javax.swing.SwingConstants.HORIZONTAL, new java.awt.Component[] {jScrollPane1, jScrollPane2});

        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel2)
                    .addComponent(jLabel1, javax.swing.GroupLayout.Alignment.TRAILING))
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 124, Short.MAX_VALUE)
                            .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)))
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addGap(33, 33, 33)
                        .addComponent(jButton1)
                        .addGap(18, 18, 18)
                        .addComponent(jButton2)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jLabel4)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jRadioButton1)
                    .addComponent(jRadioButton2))
                .addContainerGap())
        );

        jPanel3.setBorder(javax.swing.BorderFactory.createTitledBorder("Networks align"));

        jLabel6.setText("Analyse selected node attr:");

        jButton3.setText("analyse");

        jLabel7.setText("Analyse selected edge attr:");

        jButton5.setText("analyse");

        jPanel4.setBorder(javax.swing.BorderFactory.createTitledBorder("Show conserve modules:"));

        jLabel3.setText("Algorithm:");

        buttonGroup2.add(jRadioButton3);
        jRadioButton3.setText("SCML");
        
        
        
        buttonGroup2.add(jRadioButton4);
        jRadioButton4.setText("SNF");

        buttonGroup2.add(jRadioButton5);
        jRadioButton5.setText("ConMod");

        buttonGroup2.add(jRadioButton6);
        jRadioButton6.setText("jRadioButton6");

        jButton6.setText("show");

        javax.swing.GroupLayout jPanel4Layout = new javax.swing.GroupLayout(jPanel4);
        jPanel4.setLayout(jPanel4Layout);
        jPanel4Layout.setHorizontalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel4Layout.createSequentialGroup()
                        .addComponent(jLabel3)
                        .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addGroup(jPanel4Layout.createSequentialGroup()
                        .addGap(10, 10, 10)
                        .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jRadioButton5)
                            .addComponent(jRadioButton3))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jRadioButton4)
                            .addComponent(jRadioButton6))
                        .addGap(28, 28, 28))))
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel4Layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jButton6)
                .addGap(140, 140, 140))
        );
        jPanel4Layout.setVerticalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel3)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jRadioButton3)
                    .addComponent(jRadioButton4))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jRadioButton5)
                    .addComponent(jRadioButton6))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jButton6)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addComponent(jPanel4, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addContainerGap())
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel6)
                            .addComponent(jLabel7))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jButton3)
                            .addComponent(jButton5))
                        .addGap(42, 42, 42))))
        );

        jPanel3Layout.linkSize(javax.swing.SwingConstants.HORIZONTAL, new java.awt.Component[] {jButton3, jButton5});

        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel6)
                    .addComponent(jButton3, javax.swing.GroupLayout.PREFERRED_SIZE, 19, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(10, 10, 10)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel7)
                    .addComponent(jButton5))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jPanel4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
        );

        jPanel3Layout.linkSize(javax.swing.SwingConstants.VERTICAL, new java.awt.Component[] {jButton3, jButton5});

        jPanel1.setBorder(javax.swing.BorderFactory.createTitledBorder("Dynamic analysis"));

        jButton4.setText("Analyse");
        
        jSlider1.setPaintLabels(true);
        jSlider1.setPaintTicks(true);
        jSlider1.setSnapToTicks(true);
        jSlider1.setToolTipText("");
        jSlider1.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
        jSlider1.setValue(0);
        jSlider1.setMaximum(100);
        jSlider1.setMinimum(0);
        
        jLabel8.setText("Current time =");

        jLabel9.setText("0 (total time-course is:  )");

        jButton8.setText("Stop");

        jButton7.setText("<< Play");

        jButton9.setText("Play >>");
        
        jSlider1.setEnabled(false);
        jButton7.setEnabled(false);
        jButton8.setEnabled(false);
        jButton9.setEnabled(false);


        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                    .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(jPanel1Layout.createSequentialGroup()
                            .addGap(13, 13, 13)
                            .addComponent(jLabel8)
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jLabel9))
                        .addGroup(jPanel1Layout.createSequentialGroup()
                            .addContainerGap()
                            .addComponent(jSlider1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                        .addGroup(jPanel1Layout.createSequentialGroup()
                            .addGap(140, 140, 140)
                            .addComponent(jButton4)
                            .addGap(0, 0, Short.MAX_VALUE)))
                    .addContainerGap())
                .addGroup(jPanel1Layout.createSequentialGroup()
                    .addGap(78, 78, 78)
                    .addComponent(jButton7)
                    .addGap(0, 0, 0)
                    .addComponent(jButton8)
                    .addGap(0, 0, 0)
                    .addComponent(jButton9)
                    .addContainerGap(78, Short.MAX_VALUE))
            );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
            		.addContainerGap()
                    .addComponent(jButton4)
                    .addGap(19, 19, 19)
                    .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(jLabel8)
                        .addComponent(jLabel9))
                    .addGap(8, 8, 8)
                    .addComponent(jSlider1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                    .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(jButton8)
                        .addComponent(jButton7)
                        .addComponent(jButton9))
                    .addContainerGap(27, Short.MAX_VALUE))
            );


        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jPanel3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(6, 6, 6)
                .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, 227, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );
    }// </editor-fold>                                                                 
	
	

	
	private int numSelected() {
        int count = 0;
        Enumeration<CyNetwork> e = selectedListModel.elements();
        while(e.hasMoreElements()) {
            e.nextElement();
            count++;
        }
        return count;
    }
}
