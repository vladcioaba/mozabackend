package com.mozaicgames.executors;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import org.json.JSONObject;

import com.mozaicgames.core.CBackendRequestException;
import com.mozaicgames.core.CBackendRequestExecutor;
import com.mozaicgames.core.CBackendRequestExecutorParameters;
import com.mozaicgames.core.EBackendResponsStatusCode;
import com.mozaicgames.utils.CSqlBuilderUpdate;

public class CRequestExecutorUpdateUserSettings extends CBackendRequestExecutor
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
					.table(CDatabaseKeys.mKeyTableUsersSettingsTableName)
					.where(CDatabaseKeys.mKeyTableUsersSettingsUserId + "=" + parameters.getUserId());
			
			if (jsonData.has(CRequestKeys.mKeyUserSettingsMagnetOn))
			{
				final int deviceMagnetOn = jsonData.getBoolean(CRequestKeys.mKeyUserSettingsMagnetOn) ? 1 : 0;
				sqlBuilderUpdate.set(CDatabaseKeys.mKeyTableUsersSettingsMagnetOn, Integer.toString(deviceMagnetOn));
			}
			
			if (jsonData.has(CRequestKeys.mKeyUserSettingsLeftHandedOn))
			{
				final int deviceLeftHandedOn = jsonData.getBoolean(CRequestKeys.mKeyUserSettingsLeftHandedOn) ? 1 : 0;
				sqlBuilderUpdate.set(CDatabaseKeys.mKeyTableUsersSettingsLeftHandedOn, Integer.toString(deviceLeftHandedOn));
			}
			
			if (jsonData.has(CRequestKeys.mKeyUserSettingsMusicOn))
			{
				final int deviceMusicOn = jsonData.getBoolean(CRequestKeys.mKeyUserSettingsMusicOn) ? 1 : 0;
				sqlBuilderUpdate.set(CDatabaseKeys.mKeyTableUsersSettingsMusicOn, Integer.toString(deviceMusicOn));
			}
			
			if (jsonData.has(CRequestKeys.mKeyUserSettingsSfxOn))
			{
				final int deviceSfxOn = jsonData.getBoolean(CRequestKeys.mKeyUserSettingsSfxOn) ? 1 : 0;
				sqlBuilderUpdate.set(CDatabaseKeys.mKeyTableUsersSettingsSfxOn, Integer.toString(deviceSfxOn));
			}
			
			final String strQueryUpdate = sqlBuilderUpdate.toString(); 
			preparedStatementUpdate = sqlConnection.prepareStatement(strQueryUpdate, PreparedStatement.RETURN_GENERATED_KEYS);
			
			int affectedRows = preparedStatementUpdate.executeUpdate();
			if (affectedRows == 0)
			{
				throw new Exception("Nothing updated in database!");
			}		
			
			JSONObject jsonResponse = new JSONObject();
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
