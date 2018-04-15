package server;

import java.io.Console;
import java.security.SecureRandom;
import java.util.logging.Logger;

import org.apache.commons.codec.binary.Base64;
import org.json.simple.*;

import server.AuthServer;
import server.ResponseException;

public class RequestParserServer implements RequestParserInterfaceServer {
	private int responseOk = 20;
	private int authOk = 21;
	private int readingRequest = 30;
	private int alarmExists = 40;
	private int invalidResponse = -1;
	private String pass="IT16107274";
	@Override
	public String auhtInit(String alarmId) {
		JSONObject json=new JSONObject();
		json.put("id", alarmId);
		json.put("requestType", "authInit");
		System.out.println(json.toJSONString());
		return Base64.encodeBase64String((json.toJSONString().getBytes()));
	}

	@Override
	public String getResponseType(String response) {
		
		
		try {
			JSONObject json=(JSONObject)JSONValue.parse(Base64.decodeBase64(response).toString());
			if (Response(response) == this.responseOk) {
				
				if(json.containsKey("type") ) {
					//return the token by decoding base64 string
					return new String (json.get("type").toString());
				}
				
				return "-1";
				
			}
			
		} catch (Exception e) {
			
		}
		
		return "-1";
	}
	
	@Override
	public int Response(String response) {
		
		// return the response code in the response message send by the server
		// if their is an error in the response message this will return -1 as an error
		// code
		try {
			//create a JSON object from the response parameter by decoding it and parsing
			JSONObject json=(JSONObject)JSONValue.parse(Base64.decodeBase64(response).toString());
			
			if (json.containsKey("header")) {
				//extract the response code and return it
				return Integer.parseInt(json.get("header").toString());
			} 
			//when a header section not found return -1 as an error code
			return -1;
		} catch (Exception e) {
			//return error code -1 if error occur while parsing the response
			return -1;
		}

	}

	@Override
	public String exchangeCypher(String encrypted,String pass) {
		try {
			AuthServer auth=new AuthServer();
			String decrypt= auth.decrypt(pass, encrypted);
			int authNext=Integer.parseInt(decrypt)+1;
			return Base64.encodeBase64String(auth.encrypt(pass,String.valueOf(authNext)).getBytes());
		} catch (Exception e) {
			// TODO: handle exception
			System.out.println("Error in server auth reply...\n");
			return "-1";
		}
		
	
	}

	
		@Override
	public String sensorReadingsRequest(String token) {
		return null;
	}

	@Override
	public String AuthChallangeToken() throws ResponseException {
		SecureRandom secure=new SecureRandom();
		try {
				
				AuthServer auth=new AuthServer();
				String ecrypt= auth.encrypt(pass,Integer.toString(secure.nextInt(1000000)));
				int authInt=Integer.parseInt(ecrypt)+1;
				return String.valueOf(auth.encrypt(pass,String.valueOf(authInt)));
			} catch (Exception e) {
				// TODO: handle exception
				
				return "-1";
			}
		
	}

	@Override
	public String getauthChallangeReply(String response) throws Exception {
		try {
			//create a json object from the response parameter
			JSONObject json=(JSONObject)JSONValue.parse(Base64.decodeBase64(response).toString());
			if (Response(response) == this.responseOk) {
				//check if there is token field in the json and it is a base64 encoded one
				if(json.containsKey("authRepToken") &  Base64.isBase64(json.get("authToken").toString())) {
					//return the token by decoding base64 string
					return new String (Base64.decodeBase64(json.get("authRepToken").toString()));
				}
				
				//if no tokens found throw a custom exception
				throw new  ResponseException("No Auth Tokens Found");
			}
			
			
		} catch (Exception e) {
			throw new  ResponseException("Error Parsing Response");
		}
		//if error occur parsing throw an exception
		throw new  ResponseException("Error Parsing Response");
	}

	@Override
	public String SessionToken(String token) {
		SecureRandom secure=new SecureRandom();
		try {
				
				AuthServer auth=new AuthServer();
				String ecrypt= auth.encrypt(pass,token);
				int authInt=Integer.parseInt(ecrypt)+1;
				return String.valueOf(auth.encrypt(pass,String.valueOf(authInt)));
			} catch (Exception e) {
				// TODO: handle exception
				
				return "-1";
			}
		
	}

	

	/*
	 * Request response states getter methods
	 */

	/**
	 * @return the responseOk
	 */
	public int ResponseOk() {
		return responseOk;
	}

	/**
	 * @return the authOk
	 */
	public int AuthOk() {
		return authOk;
	}

	/**
	 * @return the readingRequest
	 */
	public int ReadingRequest() {
		return readingRequest;
	}

	/**
	 * @return the alarmExists
	 */
	public int AlarmExists() {
		return alarmExists;
	}

	/**
	 * @return the invalidResponse
	 */
	public int InvalidResponse() {
		return invalidResponse;
	}


	

	

}
