package com.mozaicgames.executors;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;

import org.json.JSONException;
import org.json.JSONObject;

import com.mozaicgames.core.CBackendRequestExecutor;
import com.mozaicgames.core.CBackendRequestExecutorParameters;
import com.mozaicgames.core.CBackendRequestExecutorResult;
import com.mozaicgames.core.EBackendResponsStatusCode;
import com.mozaicgames.utils.CBackendAdvancedEncryptionStandard;
import com.mozaicgames.utils.CBackendQueryResponse;
import com.mozaicgames.utils.CBackendQueryValidateDevice;
import com.mozaicgames.utils.CSqlBuilderUpdate;

public class CRequestExecutorUpdateDevice extends CBackendRequestExecutor
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
		
		final CBackendQueryValidateDevice validatorDevice = new CBackendQueryValidateDevice(parameters.getSqlDataSource(), deviceId);
		final CBackendQueryResponse validatorResponse = validatorDevice.execute();
		
		if (validatorResponse.getCode() != EBackendResponsStatusCode.STATUS_OK)
		{
			return new CBackendRequestExecutorResult(validatorResponse.getCode(), validatorResponse.getBody());
		}
		
		Connection sqlConnection = null;
		PreparedStatement preparedStatementUpdate = null;
		
		try 
		{
			sqlConnection = parameters.getSqlDataSource().getConnection();
			sqlConnection.setAutoCommit(false);
			
			CSqlBuilderUpdate sqlBuilderUpdate = new CSqlBuilderUpdate()
					.table("devices")
					.set("device_core_version", parameters.getClientCoreVersion())
					.set("device_app_version", parameters.getClientAppVersion())
					.set("device_update_time", new Timestamp(System.currentTimeMillis()).toString())
					.where("device_id="+deviceId);
			
			if (jsonData.has(CRequestKeys.mKeyDeviceModel))
			{
				final String deviceModel = jsonData.getString(CRequestKeys.mKeyDeviceModel);
				sqlBuilderUpdate.set("device_model", deviceModel);
			}
			
			if (jsonData.has(CRequestKeys.mKeyDeviceOsVersion))
			{
				final String deviceOsVerrsion = jsonData.getString(CRequestKeys.mKeyDeviceOsVersion);
				sqlBuilderUpdate.set("device_os_version", deviceOsVerrsion);
			}
			
			if (jsonData.has(CRequestKeys.mKeyDevicePlatform))
			{
				final String devicePlatform = jsonData.getString(CRequestKeys.mKeyDevicePlatform);
				sqlBuilderUpdate.set("device_platform", devicePlatform);
			}	
			
			final String strQueryUpdate = sqlBuilderUpdate.toString(); 
			preparedStatementUpdate = sqlConnection.prepareStatement(strQueryUpdate, PreparedStatement.RETURN_GENERATED_KEYS);
			
			int affectedRows = preparedStatementUpdate.executeUpdate();
			if (affectedRows == 0)
			{
				throw new Exception("Nothing updated in database!");
			}		
			
			JSONObject jsonResponse = new JSONObject();
			jsonResponse.put(CRequestKeys.mKeyClientDeviceToken, deviceToken);
			sqlConnection.commit();			
			return new CBackendRequestExecutorResult(EBackendResponsStatusCode.STATUS_OK, jsonResponse.toString());
		}
		catch (Exception e)
		{
			// error processing statement
			// return statement error - status error
			return new CBackendRequestExecutorResult(EBackendResponsStatusCode.INTERNAL_ERROR, e.getMessage());
		}
		finally
		{
			if (preparedStatementUpdate != null)
			{
				try 
				{
					preparedStatementUpdate.close();
				} 
				catch (SQLException e) 
				{
					e.printStackTrace();
				}
			}
			try 
			{
				sqlConnection.close();
			} 
			catch (Exception e) 
			{
				e.printStackTrace();
			}
		}
	}
	
}
