package com.mozaicgames.utils;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.sql.DataSource;

import com.mozaicgames.backend.EBackendResponsStatusCode;
import com.sun.net.httpserver.HttpExchange;

public class CBackendQuerryValidateDevice extends CBackendResponseWriter {

	private final DataSource 			mDataSource;
	private final HttpExchange 			mExchange;
	
	public CBackendQuerryValidateDevice(DataSource dataSource, HttpExchange exchange)
	{
		mDataSource = dataSource;
		mExchange = exchange;
	}
	
	public boolean validateDeviceFromToken(long deviceId)
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
			preparedStatementSelectDevice.setLong(1, deviceId);
			ResultSet resultSetDeviceQuery = preparedStatementSelectDevice.executeQuery();
			if (resultSetDeviceQuery == null || !resultSetDeviceQuery.next())
			{
				// the device is invalid
				intResponseCode = EBackendResponsStatusCode.INVALID_TOKEN_DEVICE;
				strResponseBody = "Invalid device token!";
				outputResponse(mExchange, intResponseCode, strResponseBody);
				return false;
			}
			else
			{
				if (resultSetDeviceQuery.getShort(1) == 1)
				{
					// device is invalid
					intResponseCode = EBackendResponsStatusCode.CLIENT_REJECTED;
					strResponseBody = "Client is rejected!";
					outputResponse(mExchange, intResponseCode, strResponseBody);
					return false;
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
			outputResponse(mExchange, intResponseCode, strResponseBody);
			return false;
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
		
		return true;
	}
}
