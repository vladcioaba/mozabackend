package com.mozaicgames.core;

import org.json.JSONException;
import org.json.JSONObject;

public abstract class CBackendRequestExecutor
{
	private final static String 				mKeyResponseStatus			= "status";
	private final static String 				mKeyResponseBody			= "body";
	
	public CBackendRequestExecutor()
	{
		
	}
	
	public boolean isSessionTokenValidationNeeded() 
	{ 
		return false; 
		}
	
	abstract public JSONObject execute(JSONObject jsonData, CBackendRequestExecutorParameters parameters) throws CBackendRequestException;
	
	public static JSONObject toJSONObject(final EBackendResponsStatusCode code, final Object body)
	{
		JSONObject jsonObjectReturn = new JSONObject();
		
		try 
		{
			jsonObjectReturn.put(mKeyResponseStatus, code.getValue());
			jsonObjectReturn.put(mKeyResponseBody, body);
		}
		catch (JSONException ex)
		{
			// internal error
			System.err.println("Internall error: " + ex.getMessage());
		}
		
		return jsonObjectReturn;
	}
}
