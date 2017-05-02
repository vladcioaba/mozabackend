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
						.into(CDatabaseKeys.mKeyTableGameResultsTableName)
						.value(CDatabaseKeys.mKeyTableGameResultsSessionId, Long.toString(parameters.getSessionId()))
						.value(CDatabaseKeys.mKeyTableGameResultsUserId, Integer.toString(parameters.getUserId()))
						.value(CDatabaseKeys.mKeyTableGameResultsCreationDate, creationTime.toString())
						.value(CDatabaseKeys.mKeyTableGameResultsType, Integer.toString(gameType))
						.value(CDatabaseKeys.mKeyTableGameResultsSeed, gameSeed)
						.value(CDatabaseKeys.mKeyTableGameResultsSeedSource, Integer.toString(gameSeedSource))
						.value(CDatabaseKeys.mKeyTableGameResultsDuration, Integer.toString(gameDuration))
						.value(CDatabaseKeys.mKeyTableGameResultsCompleteResult, Integer.toString(gameReuslt))
						.value(CDatabaseKeys.mKeyTableGameResultsDeckRefreshNum, Integer.toString(numDeckRefreshed))
						.value(CDatabaseKeys.mKeyTableGameResultsUsedActionsNum, Integer.toString(numUsedActions))
						.value(CDatabaseKeys.mKeyTableGameResultsUsedHintsNum, Integer.toString(numUsedHints))
						.value(CDatabaseKeys.mKeyTableGameResultsUsedJockersNum, Integer.toString(numUsedJockers));
				
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
