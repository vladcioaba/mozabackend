package com.mozaicgames.executors;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;

import org.json.JSONException;
import org.json.JSONObject;

import com.mozaicgames.core.CBackendRequestExecutor;
import com.mozaicgames.core.CBackendRequestExecutorParameters;
import com.mozaicgames.core.CBackendRequestExecutorResult;
import com.mozaicgames.core.EBackendResponsStatusCode;
import com.mozaicgames.utils.CBackendAdvancedEncryptionStandard;
import com.mozaicgames.utils.CSqlBuilderInsert;

public class CRequestExecutorRegisterDevice extends CBackendRequestExecutor 
{
	
	@Override
	public CBackendRequestExecutorResult execute(JSONObject jsonData, CBackendRequestExecutorParameters parameters) 
	{	
		String deviceModel = null;
		String deviceOsVerrsion = null;
		String devicePlatform = null;
		String deviceClientCoreVersion = null;
		String deviceClientAppVersion = null;
		try 
		{
			deviceModel = jsonData.getString(CRequestKeys.mKeyDeviceModel);
			deviceOsVerrsion= jsonData.getString(CRequestKeys.mKeyDeviceOsVersion);
			devicePlatform = jsonData.getString(CRequestKeys.mKeyDevicePlatform);
			deviceClientCoreVersion = jsonData.getString(CRequestKeys.mKeyDeviceClientCoreVersion);
			deviceClientAppVersion = jsonData.getString(CRequestKeys.mKeyDeviceClientAppVersion);
		}		
		catch (JSONException e)
		{
			// bad input
			// return database connection error - status retry
			return new CBackendRequestExecutorResult(EBackendResponsStatusCode.INVALID_DATA, "Invalid input data!");
		}
		
		Connection sqlConnection = null;
		PreparedStatement preparedStatementInsert = null;
		
		try 
		{
			sqlConnection = parameters.getSqlDataSource().getConnection();
			sqlConnection.setAutoCommit(false);
			final String remoteAddress = parameters.getRemoteAddress();
			
			final Timestamp creationTime = new Timestamp(System.currentTimeMillis());
			final CSqlBuilderInsert sqlBuilderInsert = new CSqlBuilderInsert()
					.into("devices")
					.value("device_model", deviceModel)
					.value("device_os_version", deviceOsVerrsion)
					.value("device_platform", devicePlatform)
					.value("device_core_version", deviceClientCoreVersion)
					.value("device_app_version", deviceClientAppVersion)
					.value("device_first_ip", remoteAddress)
					.value("device_creation_time", creationTime.toString())
					.value("device_update_time", creationTime.toString());
			
			final String strQueryInsert = sqlBuilderInsert.toString();
			preparedStatementInsert = sqlConnection.prepareStatement(strQueryInsert, PreparedStatement.RETURN_GENERATED_KEYS);
			
			int affectedRows = preparedStatementInsert.executeUpdate();
			if (affectedRows == 0)
			{
				throw new Exception("Nothing updated in database!");
			}
			
			// get last user id
			long newDeviceId = 0;
			ResultSet restultLastInsert = preparedStatementInsert.getGeneratedKeys();
			if (restultLastInsert.next())
			{
				newDeviceId = restultLastInsert.getLong(1);
			}			
			
			final CBackendAdvancedEncryptionStandard encripter = parameters.getEncriptionStandard();
			final String newUUID = encripter.encrypt(String.valueOf(newDeviceId));
			
			JSONObject jsonResponse = new JSONObject();
			jsonResponse.put(CRequestKeys.mKeyClientDeviceToken, newUUID);
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
			if (preparedStatementInsert != null)
			{
				try 
				{
					preparedStatementInsert.close();
					preparedStatementInsert = null;
					} 
				catch (SQLException e) 
				{
					e.printStackTrace();
				}
			}
			try 
			{
				sqlConnection.close();
				sqlConnection = null;
			} 
			catch (Exception e) 
			{
				e.printStackTrace();
			}
		}
	}

}
