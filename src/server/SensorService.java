package server;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.HashMap;

import org.json.simple.JSONObject;

import client.ClientInterface;;

public interface SensorService extends Remote {
	public boolean authenticate(ClientInterface service,String password) throws RemoteException;
	public String getSensor(String alarmId) throws RemoteException;
	public void clientCount(int count) throws RemoteException;
	public void sensorList(ArrayList<String> sensors ) throws RemoteException;
	public void sensorReadings(HashMap<String,JSONObject> readings) throws RemoteException;
	
}
