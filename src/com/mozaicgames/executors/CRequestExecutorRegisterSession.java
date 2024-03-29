package com.mozaicgames.executors;

import org.json.JSONException;
import org.json.JSONObject;

import com.mozaicgames.core.CBackendRequestException;
import com.mozaicgames.core.CBackendRequestExecutor;
import com.mozaicgames.core.CBackendRequestExecutorParameters;
import com.mozaicgames.core.EBackendResponsStatusCode;
import com.mozaicgames.utils.CBackendAdvancedEncryptionStandard;
import com.mozaicgames.utils.CBackendQueryGetUserGameData;
import com.mozaicgames.utils.CBackendQueryGetUserWalletData;
import com.mozaicgames.utils.CBackendQueryGetUserSettingsData;
import com.mozaicgames.utils.CBackendQueryResponse;
import com.mozaicgames.utils.CBackendQueryValidateDevice;
import com.mozaicgames.utils.CBackendSession;
import com.mozaicgames.utils.CBackendUpdateUserLastLoginDate;

public class CRequestExecutorRegisterSession extends CBackendRequestExecutor
{
	public JSONObject execute(JSONObject jsonData, CBackendRequestExecutorParameters parameters) throws CBackendRequestException
	{
		String deviceToken = null;
		String userToken = null;
		try 
		{
			deviceToken = jsonData.getString(CRequestKeys.mKeyClientDeviceToken);
			userToken = jsonData.getString(CRequestKeys.mKeyClientUserToken);
		}		
		catch (JSONException e)
		{
			// bad input
			// return database connection error - status retry
			throw new CBackendRequestException(EBackendResponsStatusCode.INVALID_DATA, "Invalid input data!");
		}
		
		final CBackendAdvancedEncryptionStandard encripter = parameters.getEncriptionStandard();
		// decrypt device id from token
		long deviceId = 0;
		int userId = 0;
		try
		{
			deviceId = Long.parseLong(encripter.decrypt(deviceToken));
			userId = Integer.parseInt(encripter.decrypt(userToken));
		} 
		catch (Exception e) 
		{
			// error processing statement
			// return statement error - status error
			throw new CBackendRequestException(EBackendResponsStatusCode.INTERNAL_ERROR, "Unable to validate tokens!");
		}
		
		CBackendQueryValidateDevice validatorDevice = new CBackendQueryValidateDevice(parameters.getSqlDataSource(), deviceId);
		CBackendQueryResponse validatorResponse = validatorDevice.execute();
		
		if (validatorResponse.getCode() != EBackendResponsStatusCode.STATUS_OK)
		{
			throw new CBackendRequestException(validatorResponse.getCode(), validatorResponse.getBody());
		}
	
		final String remoteAddress = parameters.getRemoteAddress();
		final String devicePlatform = validatorResponse.getBody();
		CBackendSession activeSession = parameters.getSessionManager().getLastKnownSessionFor(deviceId, userId);
		if (activeSession == null) 
		{
			activeSession = parameters.getSessionManager().createSession(deviceId, userId, remoteAddress, devicePlatform);
		}
		else if (parameters.getSessionManager().isSessionValid(activeSession) == false ||
			    	activeSession.getIp().equals(parameters.getRemoteAddress()) == false)
		{
			activeSession = parameters.getSessionManager().updateSession(activeSession.getId(), deviceId, userId, activeSession.getKey(), remoteAddress, devicePlatform);
		}
		
		// update user last login date
		// update user last login date
				final CBackendUpdateUserLastLoginDate updateLastLoginQuerry = new CBackendUpdateUserLastLoginDate(parameters.getSqlDataSource(), userId);		
				final CBackendQueryResponse updateLoginDateResponse = updateLastLoginQuerry.execute();
				if (updateLoginDateResponse.getCode() != EBackendResponsStatusCode.STATUS_OK)
				{
					throw new CBackendRequestException(validatorResponse.getCode(), validatorResponse.getBody());
				}
				
		if (activeSession != null)
		{
			try 
			{
				JSONObject jsonResponse = new JSONObject();
				jsonResponse.put(CRequestKeys.mKeyClientSessionToken, activeSession.getKey());
				jsonResponse.put(CRequestKeys.mKeyClientUserSettingsData, CBackendQueryGetUserSettingsData.getUserGameData(userId, parameters.getSqlDataSource()));
				jsonResponse.put(CRequestKeys.mKeyClientUserWalletData, CBackendQueryGetUserWalletData.getUserGameData(userId, activeSession.getPlatform(), parameters.getSqlDataSource()));
				jsonResponse.put(CRequestKeys.mKeyClientUserGameData, CBackendQueryGetUserGameData.getUserData(userId, parameters.getSqlDataSource()));
				return toJSONObject(EBackendResponsStatusCode.STATUS_OK, jsonResponse);
			} 
			catch (JSONException e)
			{
				System.err.println(e.getMessage());
			}
		}
		
		// error processing statement
		// return statement error - status error
		throw new CBackendRequestException(EBackendResponsStatusCode.INTERNAL_ERROR, "Unable to retrive active session!");
	}
}
