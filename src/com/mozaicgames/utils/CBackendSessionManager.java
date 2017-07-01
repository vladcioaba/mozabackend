package com.mozaicgames.utils;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;

import javax.sql.DataSource;

import com.mozaicgames.executors.CDatabaseKeys;

public class CBackendSessionManager 
{
	private final DataSource mSqlDataSource;
	private final ConcurrentMap<String, CBackendSession> mActiveSessions;
	private final CBackendAdvancedEncryptionStandard mEncripter;

	public CBackendSessionManager(final DataSource sqlDataSource, final CBackendAdvancedEncryptionStandard encripter)
			throws Exception 
	{
		mSqlDataSource = sqlDataSource;
		mEncripter = encripter;
		mActiveSessions = new ConcurrentHashMap<>();
		if (mSqlDataSource == null || mEncripter == null) 
		{
			throw new Exception("Invalid argument");
		}
	}

	public static final int TIME_TO_LIVE = 600000;

	public synchronized void cleanInvalidSessions() 
	{
		long now = System.currentTimeMillis();
		for (CBackendSession session : mActiveSessions.values()) 
		{
			if ((now - session.getCreationTime()) > TIME_TO_LIVE || now < session.getExpireTime()) 
			{
				mActiveSessions.remove(session.getKey());
			}
		}
	}

	public synchronized boolean isSessionValid(CBackendSession session) 
	{
		long now = System.currentTimeMillis();
		if (session != null) 
		{
			return now < session.getExpireTime();
		}
		return false;
	}

	public synchronized CBackendSession getActiveSessionFor(String sessionKey) 
	{
		CBackendSession session = mActiveSessions.get(sessionKey);
		if (session == null) 
		{
			return getLastKnownSessionFor(sessionKey);
		} 
		else
		{
			if (false == isSessionValid(session)) 
			{
				mActiveSessions.remove(session.getKey());
			}

			return session;
		}
	}
	
	public synchronized CBackendSession getLastKnownSessionFor(String sessionKey) 
	{
		// find cached session
		CBackendSession session = mActiveSessions.get(sessionKey);
		if (session != null) 
		{
			if (false == isSessionValid(session)) 
			{
				mActiveSessions.remove(sessionKey);
			}
			
			return session;
		}
				
		Connection sqlConnection = null;
		PreparedStatement preparedStatementSelect = null;
		try {
			sqlConnection = mSqlDataSource.getConnection();
			sqlConnection.setAutoCommit(false);

			CSqlBuilderSelect sqlBuilderSelect = new CSqlBuilderSelect()
					.column(CDatabaseKeys.mKeyTableSessionSessionId)
					.column(CDatabaseKeys.mKeyTableSessionUserId)
					.column(CDatabaseKeys.mKeyTableSessionDeviceId)
					.column(CDatabaseKeys.mKeyTableSessionExpireDate)
					.column(CDatabaseKeys.mKeyTableSessionIp)
					.column(CDatabaseKeys.mKeyTableSessionPlatform)
					.from(CDatabaseKeys.mKeyTableSessionTableName)
					.where(CDatabaseKeys.mKeyTableSessionSessionToken + "='" + sessionKey+"'");

			// find the session in the database first
			final String strQuerySelect = sqlBuilderSelect.toString();
			preparedStatementSelect = sqlConnection.prepareStatement(strQuerySelect);

			ResultSet response = preparedStatementSelect.executeQuery();
			if (response != null && response.next()) 
			{
				final long sessionId = response.getLong(1);
				final int userId = response.getInt(2);
				final long deviceId = response.getLong(3);
				final long timestampExpired = response.getTimestamp(4).getTime();
				final long timestampNow = System.currentTimeMillis();
				final String sessionIp = response.getString(5);
				final String sessionPlatform = response.getString(6);

				CBackendSession newSession = new CBackendSession(sessionId, userId, deviceId, sessionKey, timestampExpired, timestampNow, sessionIp, sessionPlatform);
				if (timestampNow < timestampExpired) 
				{
					mActiveSessions.put(sessionKey, newSession);
				}
				return newSession;
			}
		} 
		catch (Exception e) 
		{
			// could not get a connection
			// return database connection error - status retry
			System.err.println("Register handler Null pointer exception: " + e.getMessage());
		} 
		finally
		{
			if (preparedStatementSelect != null) 
			{
				try 
				{
					preparedStatementSelect.close();
					preparedStatementSelect = null;
				} 
				catch (SQLException e) 
				{
					System.err.println("Ex: " + e.getMessage());
				}
			}

			try 
			{
				sqlConnection.close();
				sqlConnection = null;
			} 
			catch (SQLException e) 
			{
				System.err.println("Ex: " + e.getMessage());
			}
		}
		
		return null;
	}

	public synchronized CBackendSession getLastKnownSessionFor(long deviceId, int userId) 
	{
		// find cached session
		for (CBackendSession session : mActiveSessions.values()) 
		{
			if (session.getUserId() == userId && session.getDeviceId() == deviceId) 
			{
				if (false == isSessionValid(session)) 
				{
					mActiveSessions.remove(session.getKey());
				} 
				
				return session;
			}
		}

		Connection sqlConnection = null;
		PreparedStatement preparedStatementSelect = null;
		try 
		{
			sqlConnection = mSqlDataSource.getConnection();
			sqlConnection.setAutoCommit(false);

			CSqlBuilder sqlBuilderSelect = new CSqlBuilderSelect()
					.column(CDatabaseKeys.mKeyTableSessionSessionId)
					.column(CDatabaseKeys.mKeyTableSessionSessionToken)
					.column(CDatabaseKeys.mKeyTableSessionExpireDate)
					.column(CDatabaseKeys.mKeyTableSessionIp)
					.column(CDatabaseKeys.mKeyTableSessionPlatform)
					.from(CDatabaseKeys.mKeyTableSessionTableName)
					.where(CDatabaseKeys.mKeyTableSessionDeviceId + "=" + deviceId)
					.where(CDatabaseKeys.mKeyTableSessionUserId + "=" + userId);

			// find the session in the database first
			final String strQuerySelect = sqlBuilderSelect.toString();
			preparedStatementSelect = sqlConnection.prepareStatement(strQuerySelect);

			ResultSet response = preparedStatementSelect.executeQuery();
			if (response != null && response.next())
			{
				final long sessionId = response.getLong(1);
				final String sessionKey = response.getString(2);
				final long timestampNow = System.currentTimeMillis();
				final long timestampExpired = response.getTimestamp(3).getTime();
				final String sessionIp = response.getString(4);
				final String sessionPlatform = response.getString(5);
				
				CBackendSession newSession = new CBackendSession(sessionId, userId, deviceId, sessionKey, timestampExpired, timestampNow, sessionIp, sessionPlatform);
				if (timestampNow < timestampExpired) 
				{
					mActiveSessions.put(sessionKey, newSession);
				}

				return newSession;
			}
		} 
		catch (Exception e) 
		{
			// could not get a connection
			// return database connection error - status retry
			System.err.println("Register handler Null pointer exception: " + e.getMessage());
		} 
		finally 
		{
			if (preparedStatementSelect != null) 
			{
				try 
				{
					preparedStatementSelect.close();
					preparedStatementSelect = null;
				} 
				catch (SQLException e) 
				{
					System.err.println(e.getMessage());
				}
			}

			try 
			{
				sqlConnection.close();
				sqlConnection = null;
			} 
			catch (SQLException e) 
			{
				System.err.println(e.getMessage());
			}
		}
		return null;
	}

	public synchronized CBackendSession createSession(long deviceId, int userId, String remoteAddress, String sessionPlatform) 
	{
		Connection sqlConnection = null;
		PreparedStatement preparedStatementInsert = null;
		PreparedStatement preparedStatementInsertHistory = null;
		try 
		{
			sqlConnection = mSqlDataSource.getConnection();
			sqlConnection.setAutoCommit(false);

			// calculate milliseconds to end of the day.
			final String strUserId = Integer.toString(userId);
			final String stdDeviceId = Long.toString(deviceId);
			final long milisCurrent = System.currentTimeMillis();
			final long milisInDay = TimeUnit.DAYS.toMillis(1);
			final long numOfDaysSinceEpoch = milisCurrent / milisInDay;
			final long milisFirstOfTheDay = numOfDaysSinceEpoch * milisInDay;
			final long milisLastOfTheDay = milisFirstOfTheDay + milisInDay - 1000;
			final String newSessionKey = mEncripter.encrypt(strUserId + "-" + stdDeviceId + "-" + String.valueOf(milisCurrent));
			
			// there is no session stored in the backend or the session was
			// expired
			// create new session data

			final String strTimeCurrentDate = new Timestamp(milisCurrent).toString();
			final String strTimeExpireDate = new Timestamp(milisLastOfTheDay).toString();
			
			// create session key
			final CSqlBuilderInsert sqlBuilderInsert = new CSqlBuilderInsert()
					.into(CDatabaseKeys.mKeyTableSessionTableName)
					.value(CDatabaseKeys.mKeyTableSessionUserId, strUserId)
					.value(CDatabaseKeys.mKeyTableSessionDeviceId, stdDeviceId)
					.value(CDatabaseKeys.mKeyTableSessionSessionToken, newSessionKey)
					.value(CDatabaseKeys.mKeyTableSessionCreationDate, strTimeCurrentDate)
					.value(CDatabaseKeys.mKeyTableSessionExpireDate, strTimeExpireDate)
					.value(CDatabaseKeys.mKeyTableSessionIp, remoteAddress)
					.value(CDatabaseKeys.mKeyTableSessionPlatform, sessionPlatform);
			
			final String strQueryInsert = sqlBuilderInsert.toString();
			preparedStatementInsert = sqlConnection.prepareStatement(strQueryInsert, PreparedStatement.RETURN_GENERATED_KEYS);

			long sessionId = 0;
			int affectedRows = preparedStatementInsert.executeUpdate();
			ResultSet restultLastInsert = preparedStatementInsert.getGeneratedKeys();
			if (affectedRows >= 1 && restultLastInsert.next()) 
			{
				sessionId = restultLastInsert.getLong(1);
			} 
			else 
			{
				return null;
			}

			CBackendSession newSession = new CBackendSession(sessionId, userId, deviceId, newSessionKey, milisLastOfTheDay, milisCurrent, remoteAddress, sessionPlatform);
			mActiveSessions.put(newSessionKey, newSession);

			// insert into history
			final CSqlBuilderInsert strBuilderInsertHistory = new CSqlBuilderInsert()
					.into(CDatabaseKeys.mKeyTableSessionHistoryTableName)
					.value(CDatabaseKeys.mKeyTableSessionHistorySessionId, Long.toString(sessionId))
					.value(CDatabaseKeys.mKeyTableSessionHistorySessionToken, newSessionKey)
					.value(CDatabaseKeys.mKeyTableSessionHistoryCreationDate, strTimeCurrentDate)
					.value(CDatabaseKeys.mKeyTableSessionHistoryIp, remoteAddress)
					.value(CDatabaseKeys.mKeyTableSessionHistoryPlatform, sessionPlatform);
			
			final String strQueryInsertHistory = strBuilderInsertHistory.toString();
			preparedStatementInsertHistory = sqlConnection.prepareStatement(strQueryInsertHistory, PreparedStatement.RETURN_GENERATED_KEYS);
			affectedRows = preparedStatementInsertHistory.executeUpdate();
			if (affectedRows == 0)
			{
				return null;
			}
			
			sqlConnection.commit();
			return newSession;
		} 
		catch (Exception e) 
		{
			// could not get a connection
			// return database connection error - status retry
			System.err.println("Register handler Null pointer exception: " + e.getMessage());
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
					System.err.println(e.getMessage());
				}
			}
			
			if (preparedStatementInsertHistory != null) 
			{
				try 
				{
					preparedStatementInsertHistory.close();
					preparedStatementInsertHistory = null;
				} 
				catch (SQLException e) 
				{
					System.err.println(e.getMessage());
				}
			}

			try 
			{
				sqlConnection.close();
				sqlConnection = null;
			} 
			catch (SQLException e) 
			{
				System.err.println(e.getMessage());
			}
		}
		return null;
	}
	
	public synchronized CBackendSession updateSession(long sessionId, long deviceId, int userId, String oldSessionKey, String remoteAddress, String sessionPlatform) 
	{
		Connection sqlConnection = null;
		PreparedStatement preparedStatementUpdate = null;
		PreparedStatement preparedStatementInsertHistory = null;
		try 
		{
			sqlConnection = mSqlDataSource.getConnection();
			sqlConnection.setAutoCommit(false);

			// calculate milliseconds to end of the day.
			final String strSessionId = Long.toString(sessionId);
			final String strUserId = Integer.toString(userId);
			final String stdDeviceId = Long.toString(deviceId);
			final long milisCurrent = System.currentTimeMillis();
			final long milisInDay = TimeUnit.DAYS.toMillis(1);
			final long numOfDaysSinceEpoch = milisCurrent / milisInDay;
			final long milisFirstOfTheDay = numOfDaysSinceEpoch * milisInDay;
			final long milisLastOfTheDay = milisFirstOfTheDay + milisInDay - 1000;
			final String newSessionKey = mEncripter.encrypt(strUserId + "-" + stdDeviceId + "-" + String.valueOf(milisCurrent));
			
			// there is no session stored in the backend or the session was
			// expired
			// create new session data

			final String strTimeCurrentDate = new Timestamp(milisCurrent).toString();
			final String strTimeExpireDate = new Timestamp(milisLastOfTheDay).toString();
			
			// create session key
			final CSqlBuilderUpdate sqlBuilderUpdate = new CSqlBuilderUpdate()
					.table(CDatabaseKeys.mKeyTableSessionTableName)
					.set(CDatabaseKeys.mKeyTableSessionSessionToken, newSessionKey)
					.set(CDatabaseKeys.mKeyTableSessionCreationDate, strTimeCurrentDate)
					.set(CDatabaseKeys.mKeyTableSessionExpireDate, strTimeExpireDate)
					.set(CDatabaseKeys.mKeyTableSessionIp, remoteAddress)
					.where(CDatabaseKeys.mKeyTableSessionUserId + "=" + strUserId)
					.where(CDatabaseKeys.mKeyTableSessionDeviceId + "=" + stdDeviceId)
					.where(CDatabaseKeys.mKeyTableSessionPlatform + "='" + sessionPlatform+"'");					
			
			final String strQueryUpdate = sqlBuilderUpdate.toString();
			preparedStatementUpdate = sqlConnection.prepareStatement(strQueryUpdate, PreparedStatement.RETURN_GENERATED_KEYS);

			int affectedRows = preparedStatementUpdate.executeUpdate();
			if (affectedRows == 0) 
			{
				return null;
			}

			// insert into history
			final CSqlBuilderInsert strBuilderInsertHistory = new CSqlBuilderInsert()
					.into(CDatabaseKeys.mKeyTableSessionHistoryTableName)
					.value(CDatabaseKeys.mKeyTableSessionHistorySessionId, strSessionId)
					.value(CDatabaseKeys.mKeyTableSessionHistorySessionToken, newSessionKey)
					.value(CDatabaseKeys.mKeyTableSessionHistoryCreationDate, strTimeCurrentDate)
					.value(CDatabaseKeys.mKeyTableSessionHistoryIp, remoteAddress)
					.value(CDatabaseKeys.mKeyTableSessionHistoryPlatform, sessionPlatform);
			
			final String strQueryInsertHistory = strBuilderInsertHistory.toString();
			preparedStatementInsertHistory = sqlConnection.prepareStatement(strQueryInsertHistory, PreparedStatement.RETURN_GENERATED_KEYS);
			affectedRows = preparedStatementInsertHistory.executeUpdate();
			if (affectedRows == 0)
			{
				return null;
			}
			
			sqlConnection.commit();

			mActiveSessions.remove(oldSessionKey);
			
			CBackendSession newSession = new CBackendSession(sessionId, userId, deviceId, newSessionKey, milisLastOfTheDay, milisCurrent, remoteAddress, sessionPlatform);
			mActiveSessions.put(newSessionKey, newSession);

			return newSession;
		} 
		catch (Exception e) 
		{
			// could not get a connection
			// return database connection error - status retry
			System.err.println("Register handler Null pointer exception: " + e.getMessage());
		} 
		finally 
		{
			if (preparedStatementUpdate != null) 
			{
				try 
				{
					preparedStatementUpdate.close();
					preparedStatementUpdate = null;
				} 
				catch (SQLException e) 
				{
					System.err.println(e.getMessage());
				}
			}
			
			if (preparedStatementInsertHistory != null) 
			{
				try 
				{
					preparedStatementInsertHistory.close();
					preparedStatementInsertHistory = null;
				} 
				catch (SQLException e) 
				{
					System.err.println(e.getMessage());
				}
			}

			try 
			{
				sqlConnection.close();
				sqlConnection = null;
			} 
			catch (SQLException e) 
			{
				System.err.println(e.getMessage());
			}
		}
		return null;
	}

}
