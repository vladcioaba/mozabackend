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

public class CRequestExecutorUpdateDevice extends CBackendRequestExecutor
{
	@Override
	public CBackendRequestExecutorResult execute(JSONObject jsonData, CBackendRequestExecutorParameters parameters) 
	{	
		String deviceModel = null;
		String deviceOsVerrsion = null;
		String devicePlatform = null;
		String deviceToken = null;
		try 
		{
			deviceModel = jsonData.getString(CRequestKeys.mKeyDeviceModel);
			deviceOsVerrsion = jsonData.getString(CRequestKeys.mKeyDeviceOsVersion);
			devicePlatform = jsonData.getString(CRequestKeys.mKeyDevicePlatform);	
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
			
			String strQueryInsert = "update devices set device_model=?, device_os_version=?, device_platform=?, device_core_version=?, device_app_version=?, device_update_time=? where device_id=?;";
			preparedStatementUpdate = sqlConnection.prepareStatement(strQueryInsert, PreparedStatement.RETURN_GENERATED_KEYS);
			preparedStatementUpdate.setString(1, deviceModel);
			preparedStatementUpdate.setString(2, deviceOsVerrsion);
			preparedStatementUpdate.setString(3, devicePlatform);
			preparedStatementUpdate.setString(4, parameters.getClientCoreVersion());
			preparedStatementUpdate.setString(5, parameters.getClientAppVersion());
			final Timestamp creationTime = new Timestamp(System.currentTimeMillis());
			preparedStatementUpdate.setTimestamp(6, creationTime);
			preparedStatementUpdate.setLong(7, deviceId);
			
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
