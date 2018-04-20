package server;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.HashMap;

import org.json.simple.JSONObject;

import client.ClientInterface;;

public interface SensorService extends Remote {
	public int authenticate(ClientInterface service,String password) throws RemoteException;
	public void getSensorReading(String id,String clientId) throws RemoteException;
	public boolean disconnectFromServer(int monitorId)throws RemoteException;
}
