package com.mozaicgames.executors;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;

import org.json.JSONObject;

import com.mozaicgames.core.CBackendRequestException;
import com.mozaicgames.core.CBackendRequestExecutor;
import com.mozaicgames.core.CBackendRequestExecutorParameters;
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
	public JSONObject execute(JSONObject jsonData, CBackendRequestExecutorParameters parameters) throws CBackendRequestException
	{	
		Connection sqlConnection = null;
		PreparedStatement preparedStatementUpdate = null;
		
		try 
		{
			sqlConnection = parameters.getSqlDataSource().getConnection();
			sqlConnection.setAutoCommit(false);
			
			CSqlBuilderUpdate sqlBuilderUpdate = new CSqlBuilderUpdate()
					.table(CDatabaseKeys.mKeyTableDevicesTableName)
					.set(CDatabaseKeys.mKeyTableDevicesUpdateDate, new Timestamp(System.currentTimeMillis()).toString())
					.where(CDatabaseKeys.mKeyTableDevicesDeviceId + "=" + parameters.getDeviceId());
			
			if (jsonData.has(CRequestKeys.mKeyDeviceModel))
			{
				final String deviceModel = jsonData.getString(CRequestKeys.mKeyDeviceModel);
				sqlBuilderUpdate.set(CDatabaseKeys.mKeyTableDevicesModel, deviceModel);
			}
			
			if (jsonData.has(CRequestKeys.mKeyDeviceOsVersion))
			{
				final String deviceOsVerrsion = jsonData.getString(CRequestKeys.mKeyDeviceOsVersion);
				sqlBuilderUpdate.set(CDatabaseKeys.mKeyTableDevicesOsVersion, deviceOsVerrsion);
			}
			
			if (jsonData.has(CRequestKeys.mKeyDevicePlatform))
			{
				final String devicePlatform = jsonData.getString(CRequestKeys.mKeyDevicePlatform);
				sqlBuilderUpdate.set(CDatabaseKeys.mKeyTableDevicesPlatform, devicePlatform);
			}
			
			if (jsonData.has(CRequestKeys.mKeyDeviceUsageTime))
			{
				final int deviceUsageTime = jsonData.getInt(CRequestKeys.mKeyDeviceUsageTime);
				sqlBuilderUpdate.set(CDatabaseKeys.mKeyTableDevicesUsageTime, Integer.toString(deviceUsageTime));
			}
			
			if (jsonData.has(CRequestKeys.mKeyDeviceClientAppVersion))
			{
				final String deviceAppVersion = jsonData.getString(CRequestKeys.mKeyDeviceClientAppVersion);
				sqlBuilderUpdate.set(CDatabaseKeys.mKeyTableDevicesClientAppVersion, deviceAppVersion);
			}
			
			if (jsonData.has(CRequestKeys.mKeyDeviceClientCoreVersion))
			{
				final String deviceCoreVersion = jsonData.getString(CRequestKeys.mKeyDeviceClientCoreVersion);
				sqlBuilderUpdate.set(CDatabaseKeys.mKeyTableDevicesClientCoreVersion, deviceCoreVersion);
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
			return toJSONObject(EBackendResponsStatusCode.STATUS_OK, jsonResponse);
		}
		catch (Exception e)
		{
			// error processing statement
			// return statement error - status error
			throw new CBackendRequestException(EBackendResponsStatusCode.INTERNAL_ERROR, e.getMessage());
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
					System.err.println(e.getMessage());
				}
			}
			try 
			{
				sqlConnection.close();
			} 
			catch (Exception e) 
			{
				System.err.println(e.getMessage());
			}
		}
	}
	
}
