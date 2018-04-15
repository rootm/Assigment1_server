package server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;

import org.apache.commons.codec.binary.Base64;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

public class Server {

	private static HashMap<String, PrintWriter> alarmList = new HashMap<String, PrintWriter>();
	private static HashMap<String, String> tokenList = new HashMap<String, String>();

	public static void main(String[] args) throws Exception {
		System.out.println("The Fire Alarm Monitor server is running.");

		System.out.println("Initiating Socket Thread...");
		Runnable runnable = new Runnable() {
			@Override
			public void run() {
				ServerSocket Alarmlistener;
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
		private String id;
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
								id = json.get("id").toString();
								alarmList.put(id, out);
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
										tokenList.put(id, SessionToken);
									}
								} else {
									System.out.println("auth failed");
									synchronized (alarmList) {
										alarmList.remove(id);
									}
									out.println(parser.authFail());
									socket.close();
									return;

								}
							} catch (Exception e) {
								System.out.println(e);
							}

						}

					}

				}

			} catch (IOException e) {
				System.out.println(e);
			} finally {

				try {
					socket.close();
				} catch (IOException e) {
				}
			}
		}
	}

}
