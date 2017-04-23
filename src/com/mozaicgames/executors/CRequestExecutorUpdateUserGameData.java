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
					.table(CDatabaseKeys.mKeyTableUsersdata)
					.where(CDatabaseKeys.mKeyTableUsersdataUserId + "=" + parameters.getUserId());
			
			if (jsonData.has(CRequestKeys.mKeyUserSettingsDataMagnetOn))
			{
				final int deviceMagnetOn = jsonData.getBoolean(CRequestKeys.mKeyUserSettingsDataMagnetOn) ? 1 : 0;
				sqlBuilderUpdate.set(CDatabaseKeys.mKeyTableUsersdataDataMagnetOn, Integer.toString(deviceMagnetOn));
			}
			
			if (jsonData.has(CRequestKeys.mKeyUserSettingsDataLeftHandedOn))
			{
				final int deviceLeftHandedOn = jsonData.getBoolean(CRequestKeys.mKeyUserSettingsDataLeftHandedOn) ? 1 : 0;
				sqlBuilderUpdate.set(CDatabaseKeys.mKeyTableUsersdataDatLeftHandedOn, Integer.toString(deviceLeftHandedOn));
			}
			
			if (jsonData.has(CRequestKeys.mKeyUserSettingsDataMusicOn))
			{
				final int deviceMusicOn = jsonData.getBoolean(CRequestKeys.mKeyUserSettingsDataMusicOn) ? 1 : 0;
				sqlBuilderUpdate.set(CDatabaseKeys.mKeyTableUsersdataDataMusicOn, Integer.toString(deviceMusicOn));
			}
			
			if (jsonData.has(CRequestKeys.mKeyUserSettingsDataSfxOn))
			{
				final int deviceSfxOn = jsonData.getBoolean(CRequestKeys.mKeyUserSettingsDataSfxOn) ? 1 : 0;
				sqlBuilderUpdate.set(CDatabaseKeys.mKeyTableUsersdataDataSfxOn, Integer.toString(deviceSfxOn));
			}
			
			if (jsonData.has(CRequestKeys.mKeyUserGameDataCreditsNum))
			{
				final int deviceCreditsNum = jsonData.getInt(CRequestKeys.mKeyUserGameDataCreditsNum);
				sqlBuilderUpdate.set(CDatabaseKeys.mKeyTableUsersdataDataCreditsNum, Integer.toString(deviceCreditsNum));
			}
			
			if (jsonData.has(CRequestKeys.mKeyUserGameDataJockersNum))
			{
				final int deviceJockersNum = jsonData.getInt(CRequestKeys.mKeyUserGameDataJockersNum);
				sqlBuilderUpdate.set(CDatabaseKeys.mKeyTableUsersdataDataJockersNum, Integer.toString(deviceJockersNum));
			}
			
			if (jsonData.has(CRequestKeys.mKeyUserGameDataLivesNum))
			{
				final int deviceLivesNum = jsonData.getInt(CRequestKeys.mKeyUserGameDataLivesNum);
				sqlBuilderUpdate.set(CDatabaseKeys.mKeyTableUsersdataDataLivesNum, Integer.toString(deviceLivesNum));
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
