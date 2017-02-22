package com.mozaicgames.utils;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.sql.DataSource;

import com.mozaicgames.backend.EBackendResponsStatusCode;

public class CBackendQueryValidateDevice implements IBackendQueryExecuter {
	
	private final DataSource		mDataSource;
	private final long				mDeviceId;
	
	public CBackendQueryValidateDevice(DataSource dataSource, long deviceId)
	{
		mDataSource = dataSource;
		mDeviceId = deviceId;
	}
	
	public CBackendQueryResponse execute()
	{
		EBackendResponsStatusCode intResponseCode = EBackendResponsStatusCode.STATUS_OK;
		String strResponseBody = "";
		
		PreparedStatement preparedStatementSelectDevice = null;
		Connection sqlConnection = null;
		
		try 
		{
			sqlConnection = mDataSource.getConnection();
			String strQuerySelectDevice = "select device_blocked from devices where device_id=?";
			preparedStatementSelectDevice = sqlConnection.prepareStatement(strQuerySelectDevice);
			preparedStatementSelectDevice.setLong(1, mDeviceId);
			ResultSet resultSetDeviceQuery = preparedStatementSelectDevice.executeQuery();
			if (resultSetDeviceQuery == null || !resultSetDeviceQuery.next())
			{
				// the device is invalid
				intResponseCode = EBackendResponsStatusCode.INVALID_TOKEN_DEVICE;
				strResponseBody =  "Invalid device token!";				
			}
			else
			{
				if (resultSetDeviceQuery.getShort(1) == 1)
				{
					// device is invalid
					intResponseCode = EBackendResponsStatusCode.CLIENT_REJECTED;
					strResponseBody ="Client is rejected!";
				}
			}
			preparedStatementSelectDevice.close();
			preparedStatementSelectDevice = null;
		}
		catch (Exception e) 
		{
			// error processing statement
			// return statement error - status error
			intResponseCode = EBackendResponsStatusCode.INTERNAL_ERROR;
			strResponseBody = "Unable to retrive device data!";
		}
		finally
		{
			if (preparedStatementSelectDevice != null)
			{
				try  
				{ 
					preparedStatementSelectDevice.close(); 
				}  
				catch (SQLException e)  
				{ 
					e.printStackTrace(); 
				}
				preparedStatementSelectDevice = null;
			}
			
			if (sqlConnection != null)
			{
				try  
				{ 
					sqlConnection.close();
				}  
				catch (SQLException e)  
				{ 
					e.printStackTrace(); 
				}
				sqlConnection = null;
			}
		}
		
		return new CBackendQueryResponse(intResponseCode, strResponseBody);
	}
}
