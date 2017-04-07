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

public class CRequestExecutorUpdateUserGameData extends CBackendRequestExecutor
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
					.table("users_gamedata")
					.where("user_id=" + parameters.getUserId());
			
			if (jsonData.has(CRequestKeys.mKeyUserDataMagnetOn))
			{
				final int deviceMagnetOn = jsonData.getBoolean(CRequestKeys.mKeyUserDataMagnetOn) ? 1 : 0;
				sqlBuilderUpdate.set("data_magnet_on", Integer.toString(deviceMagnetOn));
			}
			
			if (jsonData.has(CRequestKeys.mKeyUserDataLeftHandedOn))
			{
				final int deviceLeftHandedOn = jsonData.getBoolean(CRequestKeys.mKeyUserDataLeftHandedOn) ? 1 : 0;
				sqlBuilderUpdate.set("data_left_handed_on", Integer.toString(deviceLeftHandedOn));
			}
			
			if (jsonData.has(CRequestKeys.mKeyUserDataMusicOn))
			{
				final int deviceMusicOn = jsonData.getBoolean(CRequestKeys.mKeyUserDataMusicOn) ? 1 : 0;
				sqlBuilderUpdate.set("data_music_on", Integer.toString(deviceMusicOn));
			}
			
			if (jsonData.has(CRequestKeys.mKeyUserDataSfxOn))
			{
				final int deviceSfxOn = jsonData.getBoolean(CRequestKeys.mKeyUserDataSfxOn) ? 1 : 0;
				sqlBuilderUpdate.set("data_sfx_on", Integer.toString(deviceSfxOn));
			}
			
			if (jsonData.has(CRequestKeys.mKeyUserDataCredits))
			{
				final int deviceCredits = jsonData.getInt(CRequestKeys.mKeyUserDataCredits);
				sqlBuilderUpdate.set("data_credits", Integer.toString(deviceCredits));
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
