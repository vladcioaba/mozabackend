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
import com.mozaicgames.utils.CBackendQueryGetUserGameData;
import com.mozaicgames.utils.CSqlBuilderInsert;
import com.mozaicgames.utils.CSqlBuilderUpdate;

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
		PreparedStatement preparedStatementUpdate = null;
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
			
			final JSONObject jsonCurrentUserData = CBackendQueryGetUserGameData.getUserData(parameters.getUserId(), parameters.getSqlDataSource()); 
			int currentUserLevel = jsonCurrentUserData.getInt(CRequestKeys.mKeyUserDataLevel);
			int currentUserXp = jsonCurrentUserData.getInt(CRequestKeys.mKeyUserDataXp);
			int currentUserTrophies = jsonCurrentUserData.getInt(CRequestKeys.mKeyUserDataTrophies);
			
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
				final int numUsedJockers = jsonGameData.getInt(CRequestKeys.mKeyGameJokersUsedNum);
				final int foundationRank1 = jsonGameData.getInt(CRequestKeys.mKeyGameFoundationRank1);
				final int foundationRank2 = jsonGameData.getInt(CRequestKeys.mKeyGameFoundationRank2);
				final int foundationRank3 = jsonGameData.getInt(CRequestKeys.mKeyGameFoundationRank3);
				final int foundationRank4 = jsonGameData.getInt(CRequestKeys.mKeyGameFoundationRank4);
				final int gainedXp = foundationRank1 + foundationRank2 + foundationRank3 + foundationRank4 + gameDuration / 10;
				
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
						.value(CDatabaseKeys.mKeyTableGameResultsUsedJokersNum, Integer.toString(numUsedJockers))
						.value(CDatabaseKeys.mKeyTableGameResultsGainedXp, Integer.toString(gainedXp))
						.value(CDatabaseKeys.mKeyTableGameResultsFoundationRank1, Integer.toString(foundationRank1))
						.value(CDatabaseKeys.mKeyTableGameResultsFoundationRank2, Integer.toString(foundationRank2))
						.value(CDatabaseKeys.mKeyTableGameResultsFoundationRank3, Integer.toString(foundationRank3))
						.value(CDatabaseKeys.mKeyTableGameResultsFoundationRank4, Integer.toString(foundationRank4));
				
				final String strQueryInsert = sqlBuilderInsert.toString(); 
				preparedStatementInsert = sqlConnection.prepareStatement(strQueryInsert, PreparedStatement.RETURN_GENERATED_KEYS);
				
				int affectedRows = preparedStatementInsert.executeUpdate();
				if (affectedRows == 0)
				{
					throw new SQLException("Nothing updated in database!");
				}
				
				switch (gameReuslt)
				{
					default:
					case 0: // application closed
					case 1: // user quit
						currentUserTrophies -= 2;
						break;
					case 2: // user timeout
						currentUserTrophies -= 1;
						break;
					case 3: // user won
						currentUserTrophies += 1;
						break;
					case 4: // user won 4k
						currentUserTrophies += 2;
						break;
					case 5: // user lost
						currentUserTrophies += -1;
						break;
				}
				
				currentUserXp += gainedXp;
			}
			
			
			// calculate new user level

			
			CSqlBuilderUpdate sqlBuilderUpdateUserData = new CSqlBuilderUpdate()
							.table(CDatabaseKeys.mKeyTableUsersTableName)
							.set(CDatabaseKeys.mKeyTableUsersUserLevel, Integer.toString(currentUserLevel))
							.set(CDatabaseKeys.mKeyTableUsersUserXp, Integer.toString(currentUserXp))
							.set(CDatabaseKeys.mKeyTableUsersUserTrophies, Integer.toString(currentUserTrophies))
							.where(CDatabaseKeys.mKeyTableUsersUserId + "=" + parameters.getUserId());
			
			final String strQueryUpdateUsers = sqlBuilderUpdateUserData.toString(); 
			preparedStatementUpdate = sqlConnection.prepareStatement(strQueryUpdateUsers, PreparedStatement.RETURN_GENERATED_KEYS);
			
			int affectedRowsUpdate = preparedStatementUpdate.executeUpdate();
			if (affectedRowsUpdate == 0)
			{
				throw new SQLException("Nothing updated in database!");
			}
			
			// calculate new rewards
			
			int currentCreditsNum = 0;
			int currentJockersNum = 0;
			int currentLivesNum = 0;
			
			JSONObject responseWalletData = new JSONObject();			
			responseWalletData.put(CRequestKeys.mKeyUserWalletDataCreditsNum, currentCreditsNum);
			responseWalletData.put(CRequestKeys.mKeyUserWalletDataJokersNum, currentJockersNum);
			responseWalletData.put(CRequestKeys.mKeyUserWalletDataLivesNum, currentLivesNum);
			
			JSONObject responseGameData = new JSONObject();
			responseGameData.put(CRequestKeys.mKeyUserDataLevel, currentUserLevel);
			responseGameData.put(CRequestKeys.mKeyUserDataXp, currentUserXp);
			responseGameData.put(CRequestKeys.mKeyUserDataTrophies, currentUserTrophies);
			
			JSONObject jsonResponse = new JSONObject();
			jsonResponse.put(CRequestKeys.mKeyClientUserWalletData, responseWalletData);
			jsonResponse.put(CRequestKeys.mKeyClientUserGameData, responseGameData);
			
			sqlConnection.commit();
			return toJSONObject(EBackendResponsStatusCode.STATUS_OK, jsonResponse);
		}
		catch (JSONException e)
		{
			throw new CBackendRequestException(EBackendResponsStatusCode.INVALID_DATA, "Invalid data!");
		} 
		catch (SQLException e) 
		{
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
