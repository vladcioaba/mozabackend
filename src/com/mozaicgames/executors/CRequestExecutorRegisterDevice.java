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

public class CRequestExecutorRegisterDevice extends CBackendRequestExecutor 
{
	
	@Override
	public CBackendRequestExecutorResult execute(JSONObject jsonData, CBackendRequestExecutorParameters parameters) 
	{	
		String deviceModel = null;
		String deviceOsVerrsion = null;
		String devicePlatform = null;
		try 
		{
			deviceModel = jsonData.getString(CRequestKeys.mKeyDeviceModel);
			deviceOsVerrsion= jsonData.getString(CRequestKeys.mKeyDeviceOsVersion);
			devicePlatform = jsonData.getString(CRequestKeys.mKeyDevicePlatform);			
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
			
			String strQueryInsert = "insert into devices (device_model, device_os_version, device_platform, device_core_version, device_app_version, device_first_ip, device_creation_time, device_update_time) values (?,?,?,?,?,?,?,?);";
			preparedStatementInsert = sqlConnection.prepareStatement(strQueryInsert, PreparedStatement.RETURN_GENERATED_KEYS);
			preparedStatementInsert.setString(1, deviceModel);
			preparedStatementInsert.setString(2, deviceOsVerrsion);
			preparedStatementInsert.setString(3, devicePlatform);
			preparedStatementInsert.setString(4, parameters.getClientCoreVersion());
			preparedStatementInsert.setString(5, parameters.getClientAppVersion());
			preparedStatementInsert.setString(6, remoteAddress);
			final Timestamp creationTime = new Timestamp(System.currentTimeMillis());
			preparedStatementInsert.setTimestamp(7, creationTime);
			preparedStatementInsert.setTimestamp(8, creationTime);
			
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
