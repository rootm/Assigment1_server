package client;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.HashMap;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import server.SensorService;

public class messenger extends UnicastRemoteObject implements ClientInterface {
	
	private SensorService server;
	private MonitorClient monitor;
	JSONObject json=new JSONObject();
    HashMap<String,JSONArray> sensorDataList =new HashMap<>();
    
    
	protected messenger(SensorService service,MonitorClient client) throws RemoteException {
		super();
		this.server=service;
		this.monitor=client;
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
	public void sensorReadings(HashMap<String, JSONArray> readings) throws RemoteException {
		//this.sensorDataList=readings;
		System.out.println("xxx");
		for (String key : readings.keySet()) {
			if (this.sensorDataList.containsKey(key)) {
				this.sensorDataList.replace(key, readings.get(key));
			}else {
				this.sensorDataList.put(key, readings.get(key));
			}
		}
		
		
		
		monitor.sensorList.setListData(this.sensorDataList.keySet().toArray());
		monitor.numSensors.setText("Sensor Count: "+sensorDataList.size() );
		
	}
	
	
	
	
}
