package client;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.HashMap;

import org.apache.commons.codec.binary.Base64;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.json.simple.parser.JSONParser;

import server.SensorService;

public class messenger extends UnicastRemoteObject implements ClientInterface {
	private static final long serialVersionUID = 1L;
	private static SensorService server;
	private MonitorClient monitor;
	JSONObject json = new JSONObject();
	private static HashMap<String, JSONArray> sensorDataList = new HashMap<>();

	protected messenger() throws RemoteException {
		super();

		// TODO Auto-generated constructor stub
	}

	protected messenger(SensorService service, MonitorClient client) throws RemoteException {
		super();
		this.server = service;
		this.monitor = client;
		// TODO Auto-generated constructor stub
	}

	/**
	 * 
	 */
	
	//method used to update the connected clients count
	@Override
	public void clientCount(int count) throws RemoteException {
		
		monitor.numClients.setText("Client Count: "+String.valueOf(count));
		
	}
	//method used get the currently connected fire alarm list from server
	@Override
	public void setSensorList(String[] sensors) throws RemoteException {
		
		try {
			JSONArray array=new JSONArray();
			synchronized (sensorDataList) {
			for(String alarm:sensors) {
				
					if(!this.sensorDataList.containsKey(alarm)) {
						this.sensorDataList.put(alarm, array);
					}					
					
				}
				
				
			}
			
			monitor.listModel.clear();
			for (String alarm:sensorDataList.keySet()) {
				monitor.listModel.addElement(alarm);;  
			}
			
		} catch (Exception e) {
			System.out.println(e.getMessage()+" messenger x4" );
		}
		

	}

	//method used to get the periodic update from the fire alarms from server
	@Override
	public synchronized void sensorReadings(HashMap<String, JSONArray> readings) throws RemoteException {
		
		try {
			
		for (String key : readings.keySet()) {
			if (this.sensorDataList.containsKey(key)) {
				this.sensorDataList.replace(key, readings.get(key));
			} else {
				this.sensorDataList.put(key, readings.get(key));
			}
		}

		monitor.listModel.clear();
		for (String alarm:sensorDataList.keySet()) {
			monitor.listModel.addElement(alarm);;  
		}
		
		monitor.numSensors.setText("Sensor Count: " + sensorDataList.size());
		} catch (Exception e) {
			System.out.println(e.getMessage()+" messenger x1" );
		}

	}

	//update sensor list when a fire alarm disconnect from the server
	@Override
	public void sensorDisconnected(String alarmId) throws RemoteException {

		if (this.sensorDataList.containsKey(alarmId)) {
			synchronized (sensorDataList) {
			this.sensorDataList.remove(alarmId);
			
			monitor.listModel.clear();
			for (String alarm:sensorDataList.keySet()) {
				monitor.listModel.addElement(alarm);;  
			}
				monitor.numSensors.setText("Sensor Count: " + sensorDataList.size());
			}
			
		}

		

	}

	//check if the sensor already registered in monitor client
	public boolean contains(String alarmId) {
		if (this.sensorDataList.containsKey(alarmId)) {
			return true;

		}
		return false;
	}
	
	//return the interface of the server
	public static SensorService getServer() {
		return server;
	}

		
	public JSONArray getSensorData(String alarmId) {
		if (this.sensorDataList.containsKey(alarmId)) {
			return this.sensorDataList.get(alarmId);

		}
		return null;
	}

	//disconnect from server. called upon exit
	public void disconnect(int id) {
		try {
			System.out.println("disconnect messenger");
			System.out.println(server.disconnectFromServer(id));
		} catch (Exception e) {
			System.out.println(e.getMessage());
		}
		
	}

	//get alert data from sensor anomalies and failed periodic update from a fire alarm
	@Override
	public void alerts(String message) throws RemoteException {
		monitor.alertsData.setText(monitor.alertsData.getText()+message+"\n");
		
	}

	//Hourly sensor reading parsing method
	@Override
	public void sensorReading(String reading) throws RemoteException {
		System.out.println("got readings");
		JSONObject js=(JSONObject)JSONValue.parse(new String(Base64.decodeBase64(reading)) );
		monitor.sensorData.setText("");
		monitor.sensorData.setText( "Time: " + js.get("time")
		+ "\n\tTemperature: " + js.get("temp") + "\n\tBattery Level: "
		+ js.get("battery") + "\n\tSmoke Level: " + js.get("smoke") + "\n\tCo2 Level: "
		+ js.get("co2") + "\n\n");
		monitor.sensorData.setCaretPosition(0);
		
	
		
	}
}
