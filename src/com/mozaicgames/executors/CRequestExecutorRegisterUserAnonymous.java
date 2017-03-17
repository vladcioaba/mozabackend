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
import com.mozaicgames.utils.CBackendQueryResponse;
import com.mozaicgames.utils.CBackendQueryValidateDevice;

public class CRequestExecutorRegisterUserAnonymous extends CBackendRequestExecutor
{
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
			return new CBackendRequestExecutorResult(EBackendResponsStatusCode.INVALID_DATA, "Bad input data!");
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
		PreparedStatement preparedStatementInsert = null;
		PreparedStatement preparedStatementUpdate = null;
		
		try 
		{
			sqlConnection = parameters.getSqlDataSource().getConnection();
			sqlConnection.setAutoCommit(false);
		
			String strQueryInsert = "insert into users ( user_creation_date ) values ( ? );";
			preparedStatementInsert = sqlConnection.prepareStatement(strQueryInsert, PreparedStatement.RETURN_GENERATED_KEYS);
			preparedStatementInsert.setTimestamp(1, new Timestamp(System.currentTimeMillis()));
			int affectedRows = preparedStatementInsert.executeUpdate();
			if (affectedRows == 0)
			{
				throw new Exception("Nothing updated in database!");
			}
			
			int newUserId = 0;
			ResultSet restultLastInsert = preparedStatementInsert.getGeneratedKeys();
			if (restultLastInsert.next())
			{
				newUserId = restultLastInsert.getInt(1);
			}
			restultLastInsert.close();
			
			// update user id in device
			String strQueryUpdate = "update devices set device_user_id=? where device_id=?;";
			preparedStatementUpdate =  sqlConnection.prepareStatement(strQueryUpdate);
			preparedStatementUpdate.setInt(1, newUserId);
			preparedStatementUpdate.setLong(2, deviceId);
			
			affectedRows = preparedStatementUpdate.executeUpdate();
			if (affectedRows == 0)
			{
				throw new  Exception("Nothing updated in database!");
			}
			
			String userToken = encripter.encrypt(String.valueOf(newUserId));			
			JSONObject jsonResponse = new JSONObject();
			jsonResponse.put(CRequestKeys.mKeyClientUserToken, userToken);
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
			
			if (preparedStatementUpdate != null)
			{
				try  
				{ 
					preparedStatementUpdate.close();
					preparedStatementUpdate = null;
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
