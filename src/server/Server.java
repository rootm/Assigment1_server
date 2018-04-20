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
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import javax.print.attribute.standard.DateTimeAtCompleted;

import java.text.DateFormat;

import org.apache.commons.codec.binary.Base64;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

import client.ClientInterface;

public class Server extends UnicastRemoteObject implements SensorService {

	private static final long serialVersionUID = 1L;
	private static HashMap<Integer, ClientInterface> monitorClients = new HashMap<Integer, ClientInterface>();
	private String password = "admin";

	public Server() throws RemoteException {
		super();
	}

	private static HashMap<String, Object[]> alarmList = new HashMap<String, Object[]>();
	private static HashMap<String, String> tokenList = new HashMap<String, String>();
	private static HashMap<String, String> reportStatus = new HashMap<String, String>();

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

			
			Naming.unbind("SensorService");
		} catch (MalformedURLException mue) {
			System.err.println(mue.getMessage());
		}

		Runnable reportsRunnable = new Runnable() {

			@Override
			public void run() {
				// System.out.println("sending Reading sensors " +authState);

				try {
					DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
					synchronized (reportStatus) {
						for (String alarm : reportStatus.keySet()) {
							if (reportStatus.get(alarm).toString() == "pending") {
								Date date = new Date();
								;
								for (ClientInterface client : monitorClients.values()) {

									client.alerts(date + ":\tReport From " + alarm + "not recieved");
								}
							} else {
								reportStatus.replace(alarm, "pending");
							}
						}
					}
				} catch (Exception e) {
					System.out.println("server error reports runnable "+e.getMessage());
				}

			}

		};

		System.out.println("\nReport Services Running\n");
		ScheduledExecutorService readerService = Executors.newSingleThreadScheduledExecutor();
		
		Calendar rightNow = Calendar.getInstance();
		//run the reportsRunnable every 65 minutes
		readerService.scheduleWithFixedDelay(reportsRunnable, 65-rightNow.get(Calendar.MINUTE), 65, TimeUnit.MINUTES);

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
		private String recieved = "recieved";
		private String pending = "pending";
		// End Variables

		public Handler(Socket socket) {
			this.socket = socket;
		}

		public void run() {
			try {

				//get printWriter and a buffered reader to output and read incoming messages
				PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
				BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

				System.out.println("client Connecting");
				
				//loop responsible for reading auth initialize message
				while (true) {

					response = in.readLine();

					if (!(response == null)) {
						synchronized (alarmList) {
							//decode the message from base64 to a JSON object
							JSONObject json = (JSONObject) JSONValue.parse(new String(Base64.decodeBase64(response)));
							//check if the alarm id already exists
							if (!alarmList.containsKey(json.get("id").toString())) {
								alarmId = json.get("id").toString();
								alarmList.put(alarmId, new Object[] { out, in });
								System.out.println("alarm id added " + json.get("id").toString());
								try {
									//send the message with cipher challenge token
									authString = auth.AuthChallangeToken();
									out.println(parser.authChallage(authString));
								} catch (Exception e) {
									// TODO: handle exception
								}

								break;
							} else {
								//if alarm already registered send a error response
								JSONObject request = new JSONObject();
								request.put("header", parser.AlarmExists());

								out.println(Base64.encodeBase64String(request.toJSONString().getBytes()));
								
							}
						}
					}
				}

				//loop listening to cipher challenge messages and send responses accordingly
				while (true) {
					response = in.readLine();

					if (!(response == null)) {

						JSONObject json = (JSONObject) JSONValue.parse(new String(Base64.decodeBase64(response)));
						//check the auth reply message for cipher challenge answer from the fire alarm
						if (parser.getResponseType(response).equals("authReply")) {
							try {
								//check if the fire alarm send the correct answer
								if (Integer.parseInt(auth.decrypt(key, parser.getauthChallangeReply(response)))
										- 1 == Integer.parseInt(auth.decrypt(key, authString))) {
									System.out.println("auth sucessful");
									//set the session token for this alarm
									SessionToken = auth.SessionToken();
									//send the session token to the fire alarm
									out.println(parser.authSuccess(SessionToken));
									synchronized (tokenList) {
										//register generated session token
										tokenList.put(alarmId, SessionToken);
									}
									//register alarm for checking hourly report
									synchronized (reportStatus) {
										reportStatus.put(alarmId, pending);
									}

									HashMap<String, JSONArray> readings = new HashMap<String, JSONArray>();

									readings.put(parser.getJSON(response).get("id").toString(), new JSONArray());
									synchronized (monitorClients) {
										//send the registered fire alarm to every connected client
										for (ClientInterface client : monitorClients.values()) {
											client.sensorReadings(readings);
										}
									}

									break;

								} else {
									//authentication failed
									System.out.println("auth failed");
									synchronized (alarmList) {
										//remove it from the list if authentication failed
										alarmList.remove(alarmId);
									}
									out.println(parser.authFail());
									//close the socket
									socket.close();
									break;

								}
							} catch (Exception e) {
								//System.out.println(e);
							}

						}

					}

				}
				System.out.println("\n listning to messages\n");
				//loop listening to messages after authentication
				while (true) {
					response = in.readLine();

					if (!(response == null)) {
						//get the hourly alarm messages
						if (parser.getResponseType(response).toString().equals("sensorReading[]")) {
							
							try {
								String senderToken = parser.getJSON(response).get("token").toString();
								String senderId = parser.getJSON(response).get("id").toString();
								//verify the token with the registered token
								if (tokenList.get(senderId).toString().equals(senderToken)) {

									HashMap<String, JSONArray> readings = new HashMap<String, JSONArray>();
									
									readings.put(parser.getJSON(response).get("id").toString(),
											(JSONArray) (parser.getJSON(response).get("readings")));
									synchronized (monitorClients) {
										//Send the readings to every registered client stations
										for (ClientInterface client : monitorClients.values()) {
											client.sensorReadings(readings);
										}
										//mark as report arrived for the alarm
										synchronized (readings) {
											reportStatus.replace(alarmId, recieved);
										}

									}

								}

							} catch (Exception e) {
								
							}
							//get the monitor station requestered message from alarm
						}else if (parser.getResponseType(response).toString().equals("sensorReading")) {
							try {
								String senderToken = parser.getJSON(response).get("token").toString();
								String senderId = parser.getJSON(response).get("id").toString();
								
								if (tokenList.get(senderId).toString().equals(senderToken)) {

										synchronized (monitorClients) {
											//get the client interface to send and send it to the client that client only
											int clientId=Integer.parseInt(parser.getClientId(response));
											if(clientId!=-1) {
											ClientInterface client=monitorClients.get(clientId);
											client.sensorReading(response);
											}

										

									}

								}

							} catch (Exception e) {
								
							}
							//get alert messages send from fire alarms
						}else if (parser.getResponseType(response).toString().equals("alert")) {
							try {
								String senderToken = parser.getJSON(response).get("token").toString();
								String senderId = parser.getJSON(response).get("id").toString();
								//verify the token of the message
								if (tokenList.get(senderId).toString().equals(senderToken)) {
									//send the alert to every monitor stations
									synchronized (monitorClients) {
										for (ClientInterface client : monitorClients.values()) {
											client.alerts(parser.getJSON(response).get("message").toString());
										}

										

									}

								}

							} catch (Exception e) {
								
							}
						}

					}
					

				}

			} catch (IOException e) {
				System.out.println(e);
			} finally {

				try {
					socket.close();

					synchronized (alarmList) {
						alarmList.remove(alarmId);
					}
					synchronized (tokenList) {
						tokenList.remove(alarmId);
					}
					synchronized (reportStatus) {
						reportStatus.remove(alarmId);
					}

					for (ClientInterface client : monitorClients.values()) {
						client.sensorDisconnected(alarmId);
						;
					}
				} catch (IOException e) {
				}
			}
		}
	}

	private void clientCountUpdate() {
		try {
			synchronized (monitorClients) {
				for (ClientInterface clientInt : monitorClients.values()) {
					clientInt.clientCount(monitorClients.size());
				}

			}
		} catch (Exception e) {
			
		}

	}

	@Override
	public int authenticate(ClientInterface service, String password) throws RemoteException {

		if (this.password.equals(password)) {
			synchronized (monitorClients) {
				int id = new SecureRandom().nextInt(100000);
				System.out.println("Monitoring client connected " + id);
				monitorClients.put(id, service);
				service.setSensorList(alarmList.keySet().toArray(new String[alarmList.size()]));

				clientCountUpdate();

				return id;
			}

		}
		return -1;

	}



	@Override
	public boolean disconnectFromServer(int monitorId) throws RemoteException {
		try {
			

			if (monitorClients.containsKey(monitorId)) {
				System.out.println("Monitoring client disconnet " + monitorId);
				synchronized (monitorClients) {
					monitorClients.remove(monitorId);
				}
				clientCountUpdate();
				return true;
			} else {
				
				return false;
			}

			
		} catch (Exception e) {
			System.out.println(e.getMessage());
			return false;
		}

	}

	@Override
	public void getSensorReading(String id,String clientId) throws RemoteException {
		try {
			RequestParserServer parser=new RequestParserServer();
			Object[] array=	alarmList.get(id);
			PrintWriter writer=(PrintWriter) array[0];
			System.out.println(new String(Base64.decodeBase64(parser.sensorReadingsRequest(id,clientId).getBytes())));
			
			writer.println(parser.sensorReadingsRequest(id,clientId));
		} catch (Exception e) {
			
		}
		
	}

}
