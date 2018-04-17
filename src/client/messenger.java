package client;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.HashMap;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import server.SensorService;

public class messenger extends UnicastRemoteObject implements ClientInterface {

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
	private static final long serialVersionUID = 1L;

	@Override
	public void clientCount(int count) throws RemoteException {
		System.out.println("here");
		monitor.numClients.setText(String.valueOf(count));
		System.out.println("here1");
	}

	@Override
	public void sensorList(ArrayList<String> sensors) throws RemoteException {
		// TODO Auto-generated method stub

	}

	@Override
	public synchronized void sensorReadings(HashMap<String, JSONArray> readings) throws RemoteException {
		// this.sensorDataList=readings;
		System.out.println("xxx");
		try {
			
		for (String key : readings.keySet()) {
			if (this.sensorDataList.containsKey(key)) {
				this.sensorDataList.replace(key, readings.get(key));
			} else {
				this.sensorDataList.put(key, readings.get(key));
			}
		}

		monitor.sensorList.setListData(this.sensorDataList.keySet().toArray());
		monitor.numSensors.setText("Sensor Count: " + sensorDataList.size());
		} catch (Exception e) {
			System.out.println(e.getMessage()+" messenger x1" );
		}

	}

	@Override
	public void sensorDisconnected(String alarmId) throws RemoteException {

		if (this.sensorDataList.containsKey(alarmId)) {
			this.sensorDataList.remove(alarmId);

		}

		monitor.sensorList.setListData(this.sensorDataList.keySet().toArray());
		monitor.numSensors.setText("Sensor Count: " + sensorDataList.size());

	}

	public boolean contains(String alarmId) {
		if (this.sensorDataList.containsKey(alarmId)) {
			return true;

		}
		return false;
	}
	
	public JSONArray getSensorData(String alarmId) {
		if (this.sensorDataList.containsKey(alarmId)) {
			return this.sensorDataList.get(alarmId);

		}
		return null;
	}

	
	public void disconnect(int id) {
		try {
			System.out.println("disconnect messenger");
			System.out.println(server.disconnectFromServer(id)+" disconnect response");
		} catch (Exception e) {
			System.out.println(e.getMessage());
		}
		
	}
}
