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

	public synchronized CBackendSession getLastKnownSessionFor(String sessionKey) 
	{
		CBackendSession session = mActiveSessions.get(sessionKey);
		if (session == null) 
		{
			// try to load it from dtabase
			long sessionId = 0;
			try 
			{
				sessionId = Long.parseLong(mEncripter.decrypt(sessionKey));
			} 
			catch (Exception e) 
			{
				// TODO Auto-generated catch block
				System.err.println(e.getMessage());
				return null;
			}

			Connection sqlConnection = null;
			PreparedStatement preparedStatementSelect = null;
			try {
				sqlConnection = mSqlDataSource.getConnection();
				sqlConnection.setAutoCommit(false);

				CSqlBuilderSelect sqlBuilderSelect = new CSqlBuilderSelect().column("user_id")
						.column("device_id")
						.column("session_expire_date")
						.column("session_ip")
						.from("sessions")
						.where("session_id=" + sessionId)
						.orderBy("session_id")
						.orderType("desc").limit(1);

				// find the session in the database first
				final String strQuerySelect = sqlBuilderSelect.toString();
				preparedStatementSelect = sqlConnection.prepareStatement(strQuerySelect);

				ResultSet response = preparedStatementSelect.executeQuery();
				if (response != null && response.next()) 
				{
					final int userId = response.getInt(1);
					final long deviceId = response.getLong(2);
					final long timestampExpired = response.getTimestamp(3).getTime();
					final long timestampNow = System.currentTimeMillis();
					final String sessionIp = response.getString(4);

					CBackendSession newSession = new CBackendSession(sessionId, userId, deviceId, sessionKey,
							timestampExpired, timestampNow, sessionIp);
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
		} 
		else 
		{
			return session;
		}

		return null;
	}

	public synchronized CBackendSession getActiveSessionFor(String sessionKey) 
	{
		CBackendSession session = mActiveSessions.get(sessionKey);
		if (session == null) 
		{
			session = getLastKnownSessionFor(sessionKey);
			if (session != null && isSessionValid(session)) 
			{
				return session;
			} 
			else 
			{
				return null;
			}
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

	public synchronized CBackendSession getSessionFor(long deviceId, int userId) 
	{
		// find cached session
		for (CBackendSession session : mActiveSessions.values()) 
		{
			if (session.getUserId() == userId && session.getDeviceId() == deviceId) 
			{
				if (isSessionValid(session)) 
				{
					return session;
				} 
				else 
				{
					mActiveSessions.remove(session.getKey());
					break;
				}
			}
		}

		Connection sqlConnection = null;
		PreparedStatement preparedStatementSelect = null;
		try 
		{
			sqlConnection = mSqlDataSource.getConnection();
			sqlConnection.setAutoCommit(false);

			CSqlBuilder sqlBuilderSelect = new CSqlBuilderSelect()
					.column("session_id")
					.column("session_expire_date")
					.column("session_ip")
					.from("sessions")
					.where("device_id=" + deviceId)
					.where("user_id=" + userId)
					.orderBy("session_id")
					.orderType("desc").limit(1);

			// find the session in the database first
			final String strQuerySelect = sqlBuilderSelect.toString();
			preparedStatementSelect = sqlConnection.prepareStatement(strQuerySelect);

			ResultSet response = preparedStatementSelect.executeQuery();
			if (response != null && response.next()) 
			{
				final long sessionId = response.getLong(1);
				long timestampNow = System.currentTimeMillis();
				final long timestampExpired = response.getTimestamp(2).getTime();
				final String sessionIp = response.getString(3);
				final String sessionKey = mEncripter.encrypt(String.valueOf(sessionId));

				if (timestampNow < timestampExpired) 
				{
					CBackendSession newSession = new CBackendSession(sessionId, userId, deviceId, sessionKey,
							timestampExpired, timestampNow, sessionIp);
					mActiveSessions.put(sessionKey, newSession);
					return newSession;
				}
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

	public synchronized CBackendSession createSession(long deviceId, int userId, String remoteAddress) 
	{
		Connection sqlConnection = null;
		PreparedStatement preparedStatementInsert = null;
		try 
		{
			sqlConnection = mSqlDataSource.getConnection();
			sqlConnection.setAutoCommit(false);

			// calculate milliseconds to end of the day.
			final long milisCurrent = System.currentTimeMillis();
			final long milisInDay = TimeUnit.DAYS.toMillis(1);
			final long numOfDaysSinceEpoch = milisCurrent / milisInDay;
			final long milisFirstOfTheDay = numOfDaysSinceEpoch * milisInDay;
			final long milisLastOfTheDay = milisFirstOfTheDay + milisInDay - 1000;

			// there is no session stored in the backend or the session was
			// expired
			// create new session data

			// create session key
			final CSqlBuilderInsert sqlBuilderInsert = new CSqlBuilderInsert()
					.into("sessions")
					.value("user_id", Integer.toString(userId))
					.value("device_id", Long.toString(deviceId))
					.value("session_creation_date", new Timestamp(milisCurrent).toString())
					.value("session_expire_date", new Timestamp(milisLastOfTheDay).toString())
					.value("session_ip", remoteAddress);			
			
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

			final String newSessionKey = mEncripter.encrypt(String.valueOf(sessionId));
			CBackendSession newSession = new CBackendSession(sessionId, userId, deviceId, newSessionKey, milisCurrent,
					milisLastOfTheDay, remoteAddress);
			mActiveSessions.put(newSessionKey, newSession);

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
