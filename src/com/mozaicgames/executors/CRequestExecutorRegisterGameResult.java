package com.mozaicgames.executors;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.mozaicgames.core.CBackendRequestException;
import com.mozaicgames.core.CBackendRequestExecutor;
import com.mozaicgames.core.CBackendRequestExecutorParameters;
import com.mozaicgames.core.EBackendResponsStatusCode;
import com.mozaicgames.utils.CSqlBuilderInsert;

public class CRequestExecutorRegisterGameResult extends CBackendRequestExecutor 
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
		PreparedStatement preparedStatementInsert = null;
		try 
		{
			sqlConnection = parameters.getSqlDataSource().getConnection();
			sqlConnection.setAutoCommit(false);
			
			
			JSONArray jsonGamesDataArray = jsonData.getJSONArray(CRequestKeys.mKeyGameDataVector);			
			final int jsonGamesDataArrayLength = jsonGamesDataArray.length();
			
			if (jsonGamesDataArrayLength == 0)
			{
				throw new JSONException("Invalid array data!");
			}
			
			for (int i = 0; i < jsonGamesDataArrayLength; i++) 
			{
				JSONObject jsonGameData = jsonGamesDataArray.getJSONObject(i);
				
				final Timestamp creationTime = new Timestamp(System.currentTimeMillis());
				final int gameType = jsonGameData.getInt(CRequestKeys.mKeyGameType);
				final String gameSeed = jsonGameData.getString(CRequestKeys.mKeyGameSeed);
				final int gameSeedSource = jsonGameData.getInt(CRequestKeys.mKeyGameSeedSource);
				final int gameDuration = jsonGameData.getInt(CRequestKeys.mKeyGameDuration);
				final int gameReuslt = jsonGameData.getInt(CRequestKeys.mKeyGameFinishResult);
				final int numDeckRefreshed = jsonGameData.getInt(CRequestKeys.mKeyGameDeckRefreshNum);
				final int numUsedActions = jsonGameData.getInt(CRequestKeys.mKeyGameActionsUsedNum);
				final int numUsedHints = jsonGameData.getInt(CRequestKeys.mKeyGameHintsUsedNum);
				final int numUsedJockers = jsonGameData.getInt(CRequestKeys.mKeyGameJockersUsedNum);
				
				CSqlBuilderInsert sqlBuilderInsert = new CSqlBuilderInsert()
						.into("game_results")
						.value("session_id", Long.toString(parameters.getSessionId()))
						.value("user_id", Integer.toString(parameters.getUserId()))
						.value("creation_date", creationTime.toString())
						.value("type", Integer.toString(gameType))
						.value("seed", gameSeed)
						.value("seed_source", Integer.toString(gameSeedSource))
						.value("duration", Integer.toString(gameDuration))
						.value("complete_result", Integer.toString(gameReuslt))
						.value("deck_refresh_num", Integer.toString(numDeckRefreshed))
						.value("used_actions_num", Integer.toString(numUsedActions))
						.value("used_hints_num", Integer.toString(numUsedHints))
						.value("used_jockers_num", Integer.toString(numUsedJockers));
				
				final String strQuerInsert = sqlBuilderInsert.toString(); 
				preparedStatementInsert = sqlConnection.prepareStatement(strQuerInsert, PreparedStatement.RETURN_GENERATED_KEYS);
				
				int affectedRows = preparedStatementInsert.executeUpdate();
				if (affectedRows == 0)
				{
					throw new SQLException("Nothing updated in database!");
				}
			}
			
			JSONObject jsonResponse = new JSONObject();
			sqlConnection.commit();			
			return toJSONObject(EBackendResponsStatusCode.STATUS_OK, jsonResponse);
		}
		catch (JSONException e)
		{
			throw new CBackendRequestException(EBackendResponsStatusCode.INVALID_DATA, "Invalid data!");
		} 
		catch (SQLException e) 
		{
			// TODO Auto-generated catch block
			throw new CBackendRequestException(EBackendResponsStatusCode.INTERNAL_ERROR, e.getMessage());
		}
		finally
		{
			if (preparedStatementInsert != null)
			{
				try 
				{
					preparedStatementInsert.close();
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
