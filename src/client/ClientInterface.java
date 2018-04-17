package client;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.HashMap;

import org.json.simple.*;
public interface ClientInterface extends Remote {
	
	 public void clientCount(int count) throws RemoteException;
	 public void sensorList(ArrayList<String> sensors ) throws RemoteException;
	 public void sensorReadings(HashMap<String,JSONArray> readings) throws RemoteException;
	
}
