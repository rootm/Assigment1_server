package server;

import org.json.simple.JSONObject;

import server.ResponseException;

public interface RequestParserInterfaceServer {
	public String getResponseType(String response);
	public String auhtInit(String alarmId);
	public int Response(String response);
	public JSONObject getJSON(String response);
	public String sensorReadingsRequest(String alarmId,String clientId);	
	public String authChallage(String token)throws ResponseException;
	public String getauthChallangeReply(String response) throws Exception ;
	public String authSuccess(String sessionToken);
	public String authFail();
	public String getClientId(String response);
	
}
