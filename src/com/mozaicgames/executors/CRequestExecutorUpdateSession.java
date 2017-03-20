package com.mozaicgames.executors;

import org.json.JSONException;
import org.json.JSONObject;

import com.mozaicgames.core.CBackendRequestExecutor;
import com.mozaicgames.core.CBackendRequestExecutorParameters;
import com.mozaicgames.core.CBackendRequestExecutorResult;
import com.mozaicgames.core.EBackendResponsStatusCode;
import com.mozaicgames.utils.CBackendQueryResponse;
import com.mozaicgames.utils.CBackendQueryValidateDevice;
import com.mozaicgames.utils.CBackendSession;
import com.mozaicgames.utils.CBackendSessionManager;

public class CRequestExecutorUpdateSession extends CBackendRequestExecutor
{
	@Override
	public CBackendRequestExecutorResult execute(JSONObject jsonData, CBackendRequestExecutorParameters parameters) 
	{
		String deviceToken = null;
		try 
		{
			deviceToken = jsonData.getString(CRequestKeys.mKeyClientSessionToken);
		}		
		catch (JSONException e)
		{
			// bad input
			// return database connection error - status retry
			return new CBackendRequestExecutorResult(EBackendResponsStatusCode.INVALID_DATA, "Invalid input data!");
		}
		
		long deviceId = 0;
		int userId = 0;
		boolean createNewSession = false;
		
		final CBackendSessionManager sessionManager = parameters.getSessionManager();		
		final String remoteAddress = parameters.getRemoteAddress();
		CBackendSession activeSession = sessionManager.getActiveSessionFor(deviceToken);
		if (activeSession == null) 
		{
			CBackendSession lastKnownSession = sessionManager.getLastKnownSessionFor(deviceToken);
			if (lastKnownSession == null)
			{
				return new CBackendRequestExecutorResult(EBackendResponsStatusCode.INVALID_TOKEN_SESSION_KEY, "Unknown session token!");
			}
			
			deviceId = lastKnownSession.getDeviceId();
			userId = lastKnownSession.getUserId();
			createNewSession = true;
		}
		else
		{
			deviceId = activeSession.getDeviceId();
			userId = activeSession.getUserId();
			createNewSession = activeSession.getIp().equals(parameters.getRemoteAddress()) == false;			
		}
		
		final CBackendQueryValidateDevice validatorDevice = new CBackendQueryValidateDevice(parameters.getSqlDataSource(), deviceId);
		final CBackendQueryResponse validatorResponse = validatorDevice.execute();		
		if (validatorResponse.getCode() != EBackendResponsStatusCode.STATUS_OK)
		{
			return new CBackendRequestExecutorResult(validatorResponse.getCode(), validatorResponse.getBody());
		}
		
		if (createNewSession)
		{
			activeSession = sessionManager.createSession(deviceId, userId, remoteAddress);
		}
		
		if (activeSession != null)
		{
			try 
			{
				JSONObject jsonResponse = new JSONObject();
				jsonResponse.put(CRequestKeys.mKeyClientSessionToken, activeSession.getKey());
				return new CBackendRequestExecutorResult(EBackendResponsStatusCode.STATUS_OK, jsonResponse.toString());
			} 
			catch (JSONException e)
			{
				System.err.println(e.getMessage());
			}
		}
		
		// error processing statement
		// return statement error - status error
		return new CBackendRequestExecutorResult(EBackendResponsStatusCode.INTERNAL_ERROR, "Unable to retrive active session!");
	}
}