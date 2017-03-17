package com.mozaicgames.core;

import org.json.JSONException;
import org.json.JSONObject;

public class CBackendRequestExecutorResult 
{
	private final EBackendResponsStatusCode		mCodeStatus;
	private	final String						mStrBody;								
	
	private final static String 				mKeyResponseStatus			= "status";
	private final static String 				mKeyResponseBody			= "body";
	
	public CBackendRequestExecutorResult(EBackendResponsStatusCode status, String body)
	{
		mCodeStatus = status;
		mStrBody = body;
	}
	
	public JSONObject toJSONObject()
	{
		return toJSONObject(mCodeStatus, mStrBody);
	}
	
	public String toString()
	{
		return toJSONObject().toString();
	}
	
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
	
	public static JSONObject toJSONObject(final String requestName, final EBackendResponsStatusCode code, final Object body)
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
