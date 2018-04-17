package client;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
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

public class MonitorClient {

	private static final long serialVersionUID = 1L;
	JFrame frame = new JFrame("Monitor");
	JLabel numSensors = new JLabel();
	JLabel numClients = new JLabel();
	JTextArea alertsData = new JTextArea(10, 60);
	JTextArea sensorData = new JTextArea(10, 60);
	JList sensorList = new JList();
	private static int monitorId;
	// JTextField textField = new JTextField(40);

	JButton request = new JButton("Request Sensor Data");

	public MonitorClient() {
		// Layout GUI
		GridBagConstraints gbc = new GridBagConstraints();
		GridBagLayout layout = new GridBagLayout();
		frame.setLayout(layout);
		gbc.fill = GridBagConstraints.HORIZONTAL;
		numSensors.setText("Sensor Count: ");
		numClients.setText("Client Count: ");
		gbc.gridwidth = 1;
		gbc.gridx = 0;
		gbc.gridy = 0;
		frame.add(numSensors, gbc);
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.gridwidth = 1;
		gbc.gridx = 1;
		gbc.gridy = 0;
		frame.add(numClients, gbc);

		gbc.gridwidth = 1;
		gbc.gridx = 0;
		gbc.gridy = 1;
		JScrollPane paneA = new JScrollPane(sensorList);
		paneA.setPreferredSize(new Dimension(paneA.getPreferredSize().height, 163));

		frame.getContentPane().add(paneA, gbc);
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.gridwidth = 1;
		gbc.gridx = 1;
		gbc.gridy = 1;
		frame.getContentPane().add(new JScrollPane(sensorData), gbc);
		gbc.fill = GridBagConstraints.HORIZONTAL;

		gbc.gridx = 1;
		gbc.gridy = 2;
		frame.add(request, gbc);
		// gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.gridwidth = 2;
		gbc.gridx = 0;
		gbc.gridy = 3;
		frame.getContentPane().add(new JScrollPane(alertsData), gbc);

sensorList.addListSelectionListener(new ListSelectionListener() {
			
			@Override
			public void valueChanged(ListSelectionEvent e) {
				
				try {
					messenger msg=new messenger();
					JSONArray data=new JSONArray();
					if(msg.contains(sensorList.getSelectedValue().toString())) {
						data=msg.getSensorData(sensorList.getSelectedValue().toString());
						if (data!=null) {
							sensorData.setText(data.toJSONString());
						}else {
						
						}
						
					}
				} catch (RemoteException e1) {
					// TODO Auto-generated catch block
					System.out.println(e1.getMessage()+" monitor x1" );
				}catch(Exception ex) {
					System.out.println(ex.getMessage()+" monitor x2 null" );
				}

			}
		});
frame.addWindowListener(new WindowListener() {
			
		
			
		
			
			@Override
			public void windowClosing(WindowEvent e) {
				try {
					System.out.println("closing");
					messenger msg=new messenger();
					msg.disconnect(monitorId);
				
				} catch (Exception e2) {
					System.out.println(e2.getMessage());
				}
			 
				
				
			}

			@Override
			public void windowActivated(WindowEvent e) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void windowClosed(WindowEvent e) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void windowDeactivated(WindowEvent e) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void windowDeiconified(WindowEvent e) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void windowIconified(WindowEvent e) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void windowOpened(WindowEvent e) {
				// TODO Auto-generated method stub
				
			}
			
		
			
		
		});

		frame.pack();
		frame.setResizable(false);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		frame.setVisible(true);
		frame.setResizable(false);
	}

	private String getPassword() {
		return JOptionPane.showInputDialog(frame, "Enter Password", JOptionPane.PLAIN_MESSAGE);
	}

	public void runn(SensorService sensor) {
		String line = "fck;fck;fck";

		sensorList.setListData(line.split(";"));
		try {
			// sensor.authenticate(this, "pass");
		} catch (Exception e) {
			System.out.println(e);
		}

	}

	private void restrictPanelWidth(JScrollPane panel) {
		int panelCurrentWidth = panel.getWidth();
		int panelPreferredHeight = (int) panel.getPreferredSize().getHeight();
		int panelMaximumHeight = (int) panel.getMaximumSize().getHeight();
		panel.setPreferredSize(new Dimension(panelCurrentWidth, panelPreferredHeight));
		panel.setMaximumSize(new Dimension(panelCurrentWidth, panelMaximumHeight));
	}

	public static void main(String[] args) {
		MonitorClient client;
		try {
			client = new MonitorClient();
			
			if (System.getSecurityManager() == null) {
				System.setSecurityManager(new SecurityManager());
			}

			SensorService service = (SensorService) Naming.lookup("//localhost/SensorService");
			ClientInterface cli = new messenger(service, client);

			String pass = client.getPassword();

			if (pass != null) {
				monitorId=service.authenticate(cli, pass);
				while (monitorId==-1) {
					pass = client.getPassword();
					monitorId=service.authenticate(cli, pass);
				}
			}

			// service.authenticate(client, "pass");

		

			client.frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
			client.frame.setVisible(true);
			client.frame.setResizable(false);

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

}
