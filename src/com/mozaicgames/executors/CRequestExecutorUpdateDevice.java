package com.mozaicgames.executors;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;

import org.json.JSONObject;

import com.mozaicgames.core.CBackendRequestExecutor;
import com.mozaicgames.core.CBackendRequestExecutorParameters;
import com.mozaicgames.core.CBackendRequestExecutorResult;
import com.mozaicgames.core.EBackendResponsStatusCode;
import com.mozaicgames.utils.CSqlBuilderUpdate;

public class CRequestExecutorUpdateDevice extends CBackendRequestExecutor
{
	@Override
	public boolean isSessionTokenValidationNeeded() 
	{ 
		return true; 
	}
	
	@Override
	public CBackendRequestExecutorResult execute(JSONObject jsonData, CBackendRequestExecutorParameters parameters) 
	{	
		Connection sqlConnection = null;
		PreparedStatement preparedStatementUpdate = null;
		
		try 
		{
			sqlConnection = parameters.getSqlDataSource().getConnection();
			sqlConnection.setAutoCommit(false);
			
			CSqlBuilderUpdate sqlBuilderUpdate = new CSqlBuilderUpdate()
					.table("devices")
					.set("device_update_time", new Timestamp(System.currentTimeMillis()).toString())
					.where("device_id=" + parameters.getDeviceId());
			
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
			
			if (jsonData.has(CRequestKeys.mKeyDeviceClientAppVersion))
			{
				final String deviceAppVersion = jsonData.getString(CRequestKeys.mKeyDeviceClientAppVersion);
				sqlBuilderUpdate.set("device_app_version", deviceAppVersion);
			}
			
			if (jsonData.has(CRequestKeys.mKeyDeviceClientCoreVersion))
			{
				final String deviceCoreVersion = jsonData.getString(CRequestKeys.mKeyDeviceClientCoreVersion);
				sqlBuilderUpdate.set("device_core_version", deviceCoreVersion);
			}
			
			final String strQueryUpdate = sqlBuilderUpdate.toString(); 
			preparedStatementUpdate = sqlConnection.prepareStatement(strQueryUpdate, PreparedStatement.RETURN_GENERATED_KEYS);
			
			int affectedRows = preparedStatementUpdate.executeUpdate();
			if (affectedRows == 0)
			{
				throw new Exception("Nothing updated in database!");
			}		
			
			JSONObject jsonResponse = new JSONObject();
			final String newDeviceToken = parameters.getEncriptionStandard().encrypt(Long.toString(parameters.getDeviceId()));
			jsonResponse.put(CRequestKeys.mKeyClientDeviceToken, newDeviceToken);
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
