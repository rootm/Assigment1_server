package client;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RMISecurityManager;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import javax.sound.midi.Sequencer;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import server.SensorService;

public class MonitorClient extends javax.swing.JFrame {

	private static final long serialVersionUID = 1L;
	static DefaultListModel<String> listModel = new DefaultListModel<>();


	private static int monitorId;

	static MonitorClient client;

	private void initComponents() {

		numSensors = new javax.swing.JLabel();
		numClients = new javax.swing.JLabel();
		jScrollPane1 = new javax.swing.JScrollPane();
		sensorList = new javax.swing.JList<>();
		jScrollPane2 = new javax.swing.JScrollPane();
		sensorData = new javax.swing.JTextArea();
		request = new javax.swing.JButton();
		jScrollPane3 = new javax.swing.JScrollPane();
		alertsData = new javax.swing.JTextArea();
		jLabel3 = new javax.swing.JLabel();

		setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

		numSensors.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N
		numSensors.setText("Sensor Count:");

		numClients.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N
		numClients.setText("Client Count:");

		sensorList.setModel(listModel);
		sensorList.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
		sensorList.addListSelectionListener(new javax.swing.event.ListSelectionListener() {
			public void valueChanged(javax.swing.event.ListSelectionEvent evt) {
				sensorListValueChanged(evt);
			}
		});
		jScrollPane1.setViewportView(sensorList);

		sensorData.setColumns(20);
		sensorData.setRows(5);
		jScrollPane2.setViewportView(sensorData);

		request.setFont(new java.awt.Font("Tahoma", 0, 12)); // NOI18N
		request.setText("Request Sensor Data");

		alertsData.setColumns(20);
		alertsData.setRows(5);
		jScrollPane3.setViewportView(alertsData);

		jLabel3.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N
		jLabel3.setText("Alerts From Sensors");

		addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(WindowEvent evt) {
            	try {
        			System.out.println("closing");
        			messenger msg = new messenger();
        			msg.disconnect(monitorId);

        		} catch (Exception e2) {
        			System.out.println(e2.getMessage());
        		}
            }
        });
		
		request.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				
				if(sensorList.getSelectedIndex()!=-1) {
					sensorRequest(sensorList.getSelectedValue());
				}
				

			}
		});
		
		
		javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
		getContentPane().setLayout(layout);
		layout.setHorizontalGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addGroup(layout
				.createSequentialGroup().addContainerGap()
				.addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
						.addGroup(layout.createSequentialGroup().addGap(0, 0, Short.MAX_VALUE)
								.addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
										.addGroup(layout.createSequentialGroup()
												.addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 131,
														javax.swing.GroupLayout.PREFERRED_SIZE)
												.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
												.addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 521,
														javax.swing.GroupLayout.PREFERRED_SIZE))
										.addGroup(layout.createSequentialGroup().addComponent(numSensors)
												.addGap(139, 139, 139).addComponent(numClients))))
						.addGroup(layout.createSequentialGroup().addComponent(jLabel3)
								.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED,
										javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
								.addComponent(request, javax.swing.GroupLayout.PREFERRED_SIZE, 339,
										javax.swing.GroupLayout.PREFERRED_SIZE))
						.addComponent(jScrollPane3, javax.swing.GroupLayout.Alignment.LEADING))
				.addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)));
		layout.setVerticalGroup(
				layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
						.addGroup(layout.createSequentialGroup().addContainerGap()
								.addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
										.addComponent(numSensors).addComponent(numClients))
								.addGap(18, 18, 18)
								.addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
										.addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 234,
												Short.MAX_VALUE)
										.addComponent(jScrollPane2))
								.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
								.addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
										.addComponent(request, javax.swing.GroupLayout.PREFERRED_SIZE, 32,
												javax.swing.GroupLayout.PREFERRED_SIZE)
										.addComponent(jLabel3))
								.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
								.addComponent(jScrollPane3, javax.swing.GroupLayout.PREFERRED_SIZE, 184,
										javax.swing.GroupLayout.PREFERRED_SIZE)
								.addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)));

		pack();
	}

	public MonitorClient() {
		// Layout GUI
		initComponents();

	}

	private String getPassword() {
		return JOptionPane.showInputDialog(this, "Enter Password", JOptionPane.PLAIN_MESSAGE);
	}

	//display the selected sensors current reading send by the server
	void sensorListValueChanged(javax.swing.event.ListSelectionEvent evt) {
		try {
			if (!evt.getValueIsAdjusting()) {

				messenger msg = new messenger();
				JSONArray data = new JSONArray();
				String selected = String.valueOf(client.sensorList.getSelectedValue());
				if (selected != null) {
					if (msg.contains(selected)) {

						data = msg.getSensorData(selected);

						if (data != null) {
							Iterator i = data.iterator();
							sensorData.setText("");
							while (i.hasNext()) {
								JSONObject js = (JSONObject) i.next();
								
								sensorData.setText(sensorData.getText() + "Time: " + js.get("time")
										+ "\n\tTemperature: " + js.get("temp") + "\n\tBattery Level: "
										+ js.get("battery") + "\n\tSmoke Level: " + js.get("smoke") + "\n\tCo2 Level: "
										+ js.get("co2") + "\n\n");
								
							}
sensorData.setCaretPosition(0);
							

						} else {

						}
					}
				}
			}
		} catch (RemoteException e1) {
			// TODO Auto-generated catch block
			
		} catch (Exception ex) {
			
		}

	}

	void sensorRequest(String id) {
		try {
			messenger msg=new messenger();
			msg.getServer().getSensorReading(id,String.valueOf(monitorId));
			
		} catch (Exception e) {
			
		}
		
		
	}
	
	public static void main(String[] args) {

		try {
			client = new MonitorClient();

			if (System.getSecurityManager() == null) {
				System.setSecurityManager(new SecurityManager());
			}

			SensorService service = (SensorService) Naming.lookup("//localhost/SensorService");
			ClientInterface cli = new messenger(service, client);

			String pass = client.getPassword();

			if (pass != null) {
				monitorId = service.authenticate(cli, pass);
				while (monitorId == -1) {
					pass = client.getPassword();
					monitorId = service.authenticate(cli, pass);
				}
			}

			
			

			client.setVisible(true);

		} catch (NotBoundException ex) {
			System.err.println(ex.getMessage());
		} catch (MalformedURLException ex) {
			System.err.println(ex.getMessage());
		} catch (RemoteException ex) {
			System.err.println(ex.getMessage());
		} catch (Exception ex) {
			System.err.println(ex.getMessage());
		}

	}

	// Variables declaration - do not modify
	protected javax.swing.JTextArea alertsData;
	private javax.swing.JLabel jLabel3;
	private javax.swing.JScrollPane jScrollPane1;
	private javax.swing.JScrollPane jScrollPane2;
	private javax.swing.JScrollPane jScrollPane3;
	protected javax.swing.JTextArea sensorData;
	protected javax.swing.JLabel numClients;
	protected javax.swing.JLabel numSensors;
	protected javax.swing.JButton request;
	protected javax.swing.JList<String> sensorList;
	// End of variables declaration

}
