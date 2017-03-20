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
import com.mozaicgames.utils.CSqlBuilderInsert;
import com.mozaicgames.utils.CSqlBuilderUpdate;

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
		PreparedStatement preparedStatementInsertUser = null;
		PreparedStatement preparedStatementUpdateDevice = null;
		PreparedStatement preparedStatementInsertUserData = null;
		
		try 
		{
			sqlConnection = parameters.getSqlDataSource().getConnection();
			sqlConnection.setAutoCommit(false);
		
			final CSqlBuilderInsert sqlBuilderInsertNewUser = new CSqlBuilderInsert()
					.into("users")
					.value("user_creation_date", new Timestamp(System.currentTimeMillis()).toString());
			
			final String strQueryInsertUser = sqlBuilderInsertNewUser.toString();
			preparedStatementInsertUser = sqlConnection.prepareStatement(strQueryInsertUser, PreparedStatement.RETURN_GENERATED_KEYS);
			int affectedRows = preparedStatementInsertUser.executeUpdate();
			if (affectedRows == 0)
			{
				throw new Exception("Nothing updated in database!");
			}
			
			int newUserId = 0;
			ResultSet restultLastInsert = preparedStatementInsertUser.getGeneratedKeys();
			if (restultLastInsert.next())
			{
				newUserId = restultLastInsert.getInt(1);
			}
			restultLastInsert.close();
			
			// update user id in device
			final CSqlBuilderUpdate sqlBuilderUpdate = new CSqlBuilderUpdate()
					.table("devices")
					.set("device_user_id", Integer.toString(newUserId))
					.where("device_id=" + deviceId);
			
			final String strQueryUpdate = sqlBuilderUpdate.toString();
			preparedStatementUpdateDevice =  sqlConnection.prepareStatement(strQueryUpdate);
			
			affectedRows = preparedStatementUpdateDevice.executeUpdate();
			if (affectedRows == 0)
			{
				throw new  Exception("Nothing updated in database!");
			}
			
			final String defaultValueMagnetOn = "1";
			final String defaultValueLeftHandedOn = "0";
			final String defaultValueMusicOn = "1";
			final String defaultValueSfxOn = "1";
			final String defaultValueCredits = "100";
			
			
			final CSqlBuilderInsert sqlBuilderInsertNewUserData = new CSqlBuilderInsert()
					.into("usersgamedata")
					.value("user_id", Integer.toString(newUserId))
					.value("data_magnet_on", defaultValueMagnetOn)
					.value("data_left_handed_on", defaultValueLeftHandedOn)
					.value("data_music_on", defaultValueMusicOn)
					.value("data_sfx_on", defaultValueSfxOn)
					.value("data_credits", defaultValueCredits);
			
			final String strQueryInsertUserData = sqlBuilderInsertNewUserData.toString();
			preparedStatementInsertUserData = sqlConnection.prepareStatement(strQueryInsertUserData, PreparedStatement.RETURN_GENERATED_KEYS);
			affectedRows = preparedStatementInsertUserData.executeUpdate();
			if (affectedRows == 0)
			{
				throw new Exception("Nothing updated in database!");
			}			

			JSONObject responseUserData = new JSONObject();

			responseUserData.put(CRequestKeys.mKeyUserDataMagnetOn, defaultValueMagnetOn);
			responseUserData.put(CRequestKeys.mKeyUserDataLeftHandedOn, defaultValueLeftHandedOn);
			responseUserData.put(CRequestKeys.mKeyUserDataMusicOn, defaultValueMusicOn);
			responseUserData.put(CRequestKeys.mKeyUserDataSfxOn, defaultValueSfxOn);
			responseUserData.put(CRequestKeys.mKeyUserDataCredits, defaultValueCredits);
			
			String userToken = encripter.encrypt(String.valueOf(newUserId));			
			JSONObject jsonResponse = new JSONObject();
			jsonResponse.put(CRequestKeys.mKeyClientUserToken, userToken);
			jsonResponse.put(CRequestKeys.mKeyClientUserData, responseUserData);
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
			if (preparedStatementInsertUser != null)
			{
				try  
				{ 
					preparedStatementInsertUser.close(); 
					preparedStatementInsertUser = null;
					}  
				catch (SQLException e)  
				{ 
					e.printStackTrace(); 
				}
			}
			
			if (preparedStatementUpdateDevice != null)
			{
				try  
				{ 
					preparedStatementUpdateDevice.close();
					preparedStatementUpdateDevice = null;
				}  
				catch (SQLException e)  
				{ 
					e.printStackTrace(); 
				}
			}
			
			if (preparedStatementInsertUser != null)
			{
				try  
				{ 
					preparedStatementInsertUser.close();
					preparedStatementInsertUser = null;
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
