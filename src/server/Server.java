package server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.MalformedURLException;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.channels.AlreadyBoundException;
import java.rmi.Naming;
import java.rmi.RMISecurityManager;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.server.UnicastRemoteObject;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.HashMap;

import org.apache.commons.codec.binary.Base64;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

import client.ClientInterface;

public class Server extends UnicastRemoteObject implements SensorService {

	private static final long serialVersionUID = 1L;
	private static HashMap< Integer,ClientInterface> monitorClients = new HashMap< Integer,ClientInterface>();
	private String password="admin";
	public Server() throws RemoteException {
		super();
	}

	private static HashMap<String, Object[]> alarmList = new HashMap<String, Object[]>();
	private static HashMap<String, String> tokenList = new HashMap<String, String>();

	static ServerSocket Alarmlistener;

	public static void main(String[] args) throws Exception {
		System.out.println("The Fire Alarm Monitor server is running.");

		System.out.println("Initiating Socket Thread...");
		Runnable runnable = new Runnable() {
			@Override
			public void run() {

				try {
					Alarmlistener = new ServerSocket(3001);
					try {

						while (true) {
							new Handler(Alarmlistener.accept()).start();
						}
					} finally {
						Alarmlistener.close();
					}
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

			}
		};

		System.out.println("Creating Socket Thread...");
		Thread thread = new Thread(runnable);

		System.out.println("Starting Thread...");
		thread.start();
		System.out.println("Socket connetions running seperatley...");

		if (System.getSecurityManager() == null)
			System.setSecurityManager(new SecurityManager());
		try {
			LocateRegistry.createRegistry(2099);
			Server server = new Server();
			Naming.rebind("SensorService", server);
			System.out.println("RMI Service started....");
		} catch (RemoteException re) {
			System.err.println(re.getMessage());

		} catch (AlreadyBoundException abe) {

			System.err.println(abe.getMessage());
			Naming.unbind("SensorService");
		} catch (MalformedURLException mue) {
			System.err.println(mue.getMessage());
		}

	}

	private static class Handler extends Thread {
		// Variables
		private AuthServer auth = new AuthServer();
		private Socket socket;
		private String response = "";
		private RequestParserServer parser = new RequestParserServer();
		private String key = "IT16107274";
		private String authString;
		private String SessionToken;
		private String alarmId;
		// End Variables

		public Handler(Socket socket) {
			this.socket = socket;
		}

		public void run() {
			try {

				PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
				BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

				System.out.println("client Connecting");

				while (true) {

					response = in.readLine();

					if (!(response == null)) {
						synchronized (alarmList) {
							JSONObject json = (JSONObject) JSONValue.parse(new String(Base64.decodeBase64(response)));
							System.out.println(json.toJSONString());
							if (!alarmList.containsKey(json.get("id").toString())) {
								alarmId = json.get("id").toString();
								alarmList.put(alarmId, new Object[] { out, in });
								System.out.println("alarm id added " + json.get("id").toString());
								try {
									authString = auth.AuthChallangeToken();
									out.println(parser.authChallage(authString));
								} catch (Exception e) {
									// TODO: handle exception
								}

								break;
							} else {
								JSONObject request = new JSONObject();
								request.put("header", parser.AlarmExists());

								out.println(Base64.encodeBase64String(request.toJSONString().getBytes()));
								System.out.println(request.toJSONString());
							}
						}
					}
				}

				while (true) {
					response = in.readLine();

					if (!(response == null)) {

						JSONObject json = (JSONObject) JSONValue.parse(new String(Base64.decodeBase64(response)));
						System.out.println(json.toJSONString());
						System.out.println(parser.getResponseType(response));
						if (parser.getResponseType(response).equals("authReply")) {
							try {

								if (Integer.parseInt(auth.decrypt(key, parser.getauthChallangeReply(response)))
										- 1 == Integer.parseInt(auth.decrypt(key, authString))) {
									System.out.println("auth suceswful");
									SessionToken = auth.SessionToken();
									out.println(parser.authSuccess(SessionToken));
									synchronized (tokenList) {
										tokenList.put(alarmId, SessionToken);
									}
									
									HashMap<String, JSONArray> readings = new HashMap<String, JSONArray>();

									readings.put(parser.getJSON(response).get("id").toString(),
											new JSONArray());
									for (ClientInterface client : monitorClients.values()) {
										client.sensorReadings(readings);
									}
									
									break;
									
								} else {
									System.out.println("auth failed");
									synchronized (alarmList) {
										alarmList.remove(alarmId);
									}
									out.println(parser.authFail());
									socket.close();
									break;

								}
							} catch (Exception e) {
								System.out.println(e);
							}

						}

					}

				}
				System.out.println("\n listning to messages\n");
				while (true) {
					response = in.readLine();

					if (!(response == null)) {
						System.out.println(parser.getResponseType(response).toString().equals("sensorReading[]") );
						if (parser.getResponseType(response).toString().equals("sensorReading[]")) {
						
							try {
								String senderToken = parser.getJSON(response).get("token").toString();
								String senderId = parser.getJSON(response).get("id").toString();
								System.out.println(senderToken+" "+senderId );
								if (tokenList.get(senderId).toString().equals(senderToken)) {

									HashMap<String, JSONArray> readings = new HashMap<String, JSONArray>();

									readings.put(parser.getJSON(response).get("id").toString(),
											(JSONArray) (parser.getJSON(response).get("readings")));
									for (ClientInterface client : monitorClients.values()) {
										client.sensorReadings(readings);
									}
								}
								
							} catch (Exception e) {
								System.out.println(e.toString()+ " server x1");
							}

						}

					}
					JSONObject json = (JSONObject) JSONValue.parse(new String(Base64.decodeBase64(response)));
					System.out.println(json.toJSONString());

				}

			} catch (IOException e) {
				System.out.println(e);
			} finally {

				try {
					socket.close();
					alarmList.remove(alarmId);
					tokenList.remove(alarmId);
					for (ClientInterface client : monitorClients.values()) {
						client.sensorDisconnected(alarmId);;
					}
				} catch (IOException e) {
				}
			}
		}
	}

	@Override
	public int authenticate(ClientInterface service, String password) throws RemoteException {
		
		if(this.password.equals(password)) {
			synchronized (monitorClients) {
				int id=new SecureRandom().nextInt(100000);
				monitorClients.put(id,service);
				System.out.println("Monitoring client connected " +id);
				return id;
			}
			
		}
		return -1;
		
	}

	@Override
	public String getSensor(String alarmId) throws RemoteException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void clientCount(int count) throws RemoteException {
		// TODO Auto-generated method stub

	}

	@Override
	public void sensorList(ArrayList<String> sensors) throws RemoteException {
		// TODO Auto-generated method stub

	}

	@Override
	public void sensorReadings(HashMap<String, JSONObject> readings) throws RemoteException {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean disconnectFromServer(int monitorId) throws RemoteException {
		try {
			System.out.println("Monitoring client disconnet here " +monitorId);
			//synchronized (monitorClients) {
			
			if(monitorClients.containsKey(monitorId)) {
				System.out.println("Monitoring client disconnet " +monitorId);
				monitorClients.remove(monitorId);
				return true;
			}else {
				System.out.println("Monitoring client not found " +monitorId+" "+monitorClients.size());
				return false;
			}
			
		//}
		} catch (Exception e) {
			System.out.println(e.getMessage());
			return false;
		}
		
		
		
	}

}
