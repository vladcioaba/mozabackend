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
import com.mozaicgames.utils.CBackendQueryGetUserWalletData;
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
		PreparedStatement preparedStatementUpdateData = null;
		PreparedStatement preparedStatementUpdateWallet = null;
		
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
			
			final JSONObject jsonCurrentUserWallet = CBackendQueryGetUserWalletData.getUserGameData(parameters.getUserId(), parameters.getDevicePlatform(), parameters.getSqlDataSource());
			int currentWalletTokens = jsonCurrentUserWallet.getInt(CRequestKeys.mKeyUserWalletDataTokensNum);
			int currentWalletJokers = jsonCurrentUserWallet.getInt(CRequestKeys.mKeyUserWalletDataJokersNum);
			int currentWalletCredits = jsonCurrentUserWallet.getInt(CRequestKeys.mKeyUserWalletDataCreditsNum);
			
			
			JSONArray gamesRewardsVector = new JSONArray(); 
			
			for (int i = 0; i < jsonGamesDataArrayLength; i++) 
			{
				JSONObject jsonGameData = jsonGamesDataArray.getJSONObject(i);
				
				final Timestamp creationTime = new Timestamp(System.currentTimeMillis());
				final int gameType = jsonGameData.getInt(CRequestKeys.mKeyGameType);
				final String gameSeed = jsonGameData.getString(CRequestKeys.mKeyGameSeed);
				final int gameSeedSource = jsonGameData.getInt(CRequestKeys.mKeyGameSeedSource);
				int playerPlace = jsonGameData.getInt(CRequestKeys.mKeyGamePlayerPlace);
				final int playerScore = jsonGameData.getInt(CRequestKeys.mKeyGamePlayerScore);
				final int playerStars = jsonGameData.getInt(CRequestKeys.mKeyGamePlayerStars);
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
				final int gainedXp = 1 + foundationRank1 + foundationRank2 + foundationRank3 + foundationRank4 + Math.min(gameDuration, 360) / 10;
				int gainedTrophies = 0;
				int gainedTokens = 0;
				int gainedJokers = 0;
				int gainedCredits = 1;
				int gainedLevel = 0;
				boolean isFoundation4k = foundationRank1 == 14 && foundationRank2 == 14 && foundationRank3 == 14 && foundationRank4 == 14;
				
				CSqlBuilderInsert sqlBuilderInsert = new CSqlBuilderInsert()
						.into(CDatabaseKeys.mKeyTableGameResultsTableName)
						.value(CDatabaseKeys.mKeyTableGameResultsSessionToken, parameters.getSessionToken())
						.value(CDatabaseKeys.mKeyTableGameResultsUserId, Integer.toString(parameters.getUserId()))
						.value(CDatabaseKeys.mKeyTableGameResultsCreationDate, creationTime.toString())
						.value(CDatabaseKeys.mKeyTableGameResultsType, Integer.toString(gameType))
						.value(CDatabaseKeys.mKeyTableGameResultsSeed, gameSeed)
						.value(CDatabaseKeys.mKeyTableGameResultsPlayerPlace, Integer.toString(playerPlace))
						.value(CDatabaseKeys.mKeyTableGameResultsPlayerScore, Integer.toString(playerScore))
						.value(CDatabaseKeys.mKeyTableGameResultsPlayerStars, Integer.toString(playerStars))
						.value(CDatabaseKeys.mKeyTableGameResultsSeedSource, Integer.toString(gameSeedSource))
						.value(CDatabaseKeys.mKeyTableGameResultsDuration, Integer.toString(gameDuration))
						.value(CDatabaseKeys.mKeyTableGameResultsCompleteResult, Integer.toString(gameReuslt))
						.value(CDatabaseKeys.mKeyTableGameResultsDeckRefreshNum, Integer.toString(numDeckRefreshed))
						.value(CDatabaseKeys.mKeyTableGameResultsUsedActionsNum, Integer.toString(numUsedActions))
						.value(CDatabaseKeys.mKeyTableGameResultsUsedHintsNum, Integer.toString(numUsedHints))
						.value(CDatabaseKeys.mKeyTableGameResultsUsedJokersNum, Integer.toString(numUsedJockers))
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
						gainedTrophies = -12;
						playerPlace = 4;
						break;

					case 2: // user finished
						
						if (playerPlace == 1)
						{
							// user finished first
							gainedTrophies = 11;
							gainedTokens = 1;
							double randNumber = Math.random();
							if (randNumber < 0.5)
							{
								gainedTokens ++;
								if (randNumber < 0.2)
								{
									gainedCredits ++;
								}
							}
						}
						else  if (playerPlace == 2)
						{
							// user finished second
							gainedTrophies = 6;
							gainedTokens = 1;
						}
						else
						{
							// user lost
							gainedTrophies = -12;
						}
						
						if (isFoundation4k)
						{
							gainedTrophies += 10;
							gainedCredits ++;
							
							double randNumber = Math.random();
							if (randNumber < 0.5)
							{
								gainedCredits ++;
								
								if (numUsedJockers > 3)
								{
									gainedJokers ++;
								}
								
								if (randNumber < 0.2)
								{
									gainedCredits ++;
									gainedJokers ++;
									
									if (randNumber < 0.1)
									{
										gainedJokers ++;
										
										if (randNumber < 0.05)
										{
											gainedJokers ++;
										}
									}
								}
							}
						}
						
						break;						
				}
				
				currentUserTrophies += gainedTrophies;
				currentUserXp += gainedXp;
				
				currentWalletTokens += gainedTokens;
				currentWalletJokers += gainedJokers;
				currentWalletCredits += gainedCredits;
				
				currentUserTrophies = Math.max(currentUserTrophies, 0);
				
				// calculate new user level
				int maxXpForThisLevel = (int) (100 * Math.pow(currentUserLevel, 2) - 1);
				while (currentUserXp > maxXpForThisLevel)
				{
					gainedLevel ++;
					currentUserXp -= maxXpForThisLevel;
					maxXpForThisLevel = (int) (100 * Math.pow(currentUserLevel + gainedLevel, 2) - 1);
				}
				
				currentUserLevel += gainedLevel;
				
				// jsonResponse.put(CRequestKeys.mKeyUserDataLevel, currentUserLevel);
				JSONObject jsonGameResponse = new JSONObject();
				jsonGameResponse.put(CRequestKeys.mKeyGameRewardsGainedXp, gainedXp);
				jsonGameResponse.put(CRequestKeys.mKeyGameRewardsGainedLevel, gainedLevel);
				jsonGameResponse.put(CRequestKeys.mKeyGameRewardsGainedLevelMaxXp, maxXpForThisLevel);
				jsonGameResponse.put(CRequestKeys.mKeyGameRewardsGainedTrophie, gainedTrophies);			
				jsonGameResponse.put(CRequestKeys.mKeyGameRewardsGainedCredits, gainedCredits);
				jsonGameResponse.put(CRequestKeys.mKeyGameRewardsGainedJokers, gainedJokers);
				jsonGameResponse.put(CRequestKeys.mKeyGameRewardsGainedTokens, gainedTokens);
				jsonGameResponse.put(CRequestKeys.mKeyGameRewardsGainedPlace, playerPlace);
				jsonGameResponse.put(CRequestKeys.mKeyGameRewardsGainedScore, playerScore);
				jsonGameResponse.put(CRequestKeys.mKeyGameRewardsGainedStars, playerStars);
				
				gamesRewardsVector.put(jsonGameResponse);
			}
			
			CSqlBuilderUpdate sqlBuilderUpdateUserData = new CSqlBuilderUpdate()
							.table(CDatabaseKeys.mKeyTableUsersTableName)
							.set(CDatabaseKeys.mKeyTableUsersUserLevel, Integer.toString(currentUserLevel))
							.set(CDatabaseKeys.mKeyTableUsersUserXp, Integer.toString(currentUserXp))
							.set(CDatabaseKeys.mKeyTableUsersUserTrophies, Integer.toString(currentUserTrophies))
							.where(CDatabaseKeys.mKeyTableUsersUserId + "=" + parameters.getUserId());
			
			final String strQueryUpdateUsers = sqlBuilderUpdateUserData.toString(); 
			preparedStatementUpdateData = sqlConnection.prepareStatement(strQueryUpdateUsers, PreparedStatement.RETURN_GENERATED_KEYS);
			
			final int affectedRowsUpdateData = preparedStatementUpdateData.executeUpdate();
			if (affectedRowsUpdateData == 0)
			{
				throw new SQLException("Nothing updated in database!");
			}
			
			CSqlBuilderUpdate sqlBuilderUpdateUserWallet = new CSqlBuilderUpdate()
					.table(CDatabaseKeys.mKeyTableUsersWalletDataTableName)
					.set(CDatabaseKeys.mKeyTableUsersWalletDataTokensNum, Integer.toString(currentWalletTokens))
					.set(CDatabaseKeys.mKeyTableUsersWalletDataJokersNum, Integer.toString(currentWalletJokers))
					.set(CDatabaseKeys.mKeyTableUsersWalletDataCreditsNum, Integer.toString(currentWalletCredits))
					.where(CDatabaseKeys.mKeyTableUsersWalletDataUserId + "=" + parameters.getUserId());
			
			final String strQueryUdpdateWallet = sqlBuilderUpdateUserWallet.toString();
			preparedStatementUpdateWallet = sqlConnection.prepareStatement(strQueryUdpdateWallet, PreparedStatement.RETURN_GENERATED_KEYS);
			final int affectedRowsUpdateWallet = preparedStatementUpdateWallet.executeUpdate();
			if (affectedRowsUpdateWallet == 0)
			{
				throw new SQLException("Nothing updated in database!");
			}
			
			JSONObject jsonResponse = new JSONObject();
			jsonResponse.put(CRequestKeys.mKeyGameRewardsVector, gamesRewardsVector);
			
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
			if (preparedStatementUpdateData != null)
			{
				try 
				{
					preparedStatementUpdateData.close();
				} 
				catch (SQLException e) 
				{
					System.err.println(e.getMessage());
				}
			}
			if (preparedStatementUpdateWallet != null)
			{
				try 
				{
					preparedStatementUpdateWallet.close();
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
