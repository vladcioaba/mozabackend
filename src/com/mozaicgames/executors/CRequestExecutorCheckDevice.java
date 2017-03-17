package com.mozaicgames.executors;

import org.json.JSONException;
import org.json.JSONObject;

import com.mozaicgames.core.CBackendRequestExecutor;
import com.mozaicgames.core.CBackendRequestExecutorParameters;
import com.mozaicgames.core.CBackendRequestExecutorResult;
import com.mozaicgames.core.EBackendResponsStatusCode;
import com.mozaicgames.utils.CBackendAdvancedEncryptionStandard;
import com.mozaicgames.utils.CBackendQueryResponse;
import com.mozaicgames.utils.CBackendQueryValidateDevice;

public class CRequestExecutorCheckDevice extends CBackendRequestExecutor
{
	@Override
	public CBackendRequestExecutorResult execute(JSONObject jsonData, CBackendRequestExecutorParameters parameters) 
	{	
		String deviceToken = null;
		try 
		{
			deviceToken = jsonData.getString(CRequestKeys.mKeyClientDeviceToken);
		}		
		catch (JSONException e)
		{
			// bad input
			// return database connection error - status retry
			return new CBackendRequestExecutorResult(EBackendResponsStatusCode.INVALID_DATA, "Invalid input data!");
		}
		
		final CBackendAdvancedEncryptionStandard encripter = parameters.getEncriptionStandard();
		long deviceId = 0;
		try 
		{
			// decrypt device id from token
			deviceId = Long.parseLong(encripter.decrypt(deviceToken));
		}
		catch (Exception ex)
		{
			// error processing statement
			// return statement error - status error
			return new CBackendRequestExecutorResult(EBackendResponsStatusCode.INTERNAL_ERROR, "Unable to validate tokens!");
		}
		
		CBackendQueryValidateDevice validatorDevice = new CBackendQueryValidateDevice(parameters.getSqlDataSource(), deviceId);
		CBackendQueryResponse validatorResponse = validatorDevice.execute();		
		return new CBackendRequestExecutorResult(validatorResponse.getCode(), validatorResponse.getBody());
	}
}
