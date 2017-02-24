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
	private final DataSource 								mSqlDataSource;
	private final String 									mEncriptionCode; 
	private final ConcurrentMap<String, CBackendSession> 	mActiveSessions;
	
	public CBackendSessionManager(DataSource sqlDataSource, String encriptionCode) throws Exception
	{
		mSqlDataSource = sqlDataSource;
		mEncriptionCode = encriptionCode;
		mActiveSessions = new ConcurrentHashMap<>();
		if (mSqlDataSource == null || mEncriptionCode == null)
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
		CBackendSession session =  mActiveSessions.get(sessionKey);		
		if (session == null)
		{
			// try to load it from dtabase
			long sessionId = 0;
			try 
			{
				final CBackendAdvancedEncryptionStandard encripter = new CBackendAdvancedEncryptionStandard(mEncriptionCode, "AES");
				sessionId = Long.parseLong(encripter.decrypt(sessionKey));
			} 
			catch (Exception e) 
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
				return null;
			}
						
			Connection sqlConnection = null;
			PreparedStatement preparedStatementSelect = null;
			try 
			{
				sqlConnection = mSqlDataSource.getConnection();
				sqlConnection.setAutoCommit(false);
				
				// find the session in the database first
				String strQuerySelect = "select session_id, user_id, device_id session_expire_date from sessions where session_id=? order by session_id desc limit 1;";
				preparedStatementSelect = sqlConnection.prepareStatement(strQuerySelect);
				preparedStatementSelect.setLong(1, sessionId);
				
				ResultSet response = preparedStatementSelect.executeQuery();			
				if (response != null && response.next())
				{
					final int userId = response.getInt(2);
					final long deviceId = response.getLong(3);
					final long timestampExpired = response.getTimestamp(4).getTime();
					final long timestampNow = System.currentTimeMillis();
					
					preparedStatementSelect.close();
					preparedStatementSelect = null;
					
					CBackendSession newSession = new CBackendSession(sessionId, userId, deviceId, sessionKey, timestampExpired, timestampNow);
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
					}  
					catch (SQLException e)  
					{ 
						e.printStackTrace(); 
					}
				}			
				
				try 
				{
					sqlConnection.close();
				} 
				catch (SQLException e) 
				{
					e.printStackTrace();
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
		CBackendSession session =  mActiveSessions.get(sessionKey);		
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
			
			// find the session in the database first
			String strQuerySelect = "select session_id, session_expire_date from sessions where device_id=? and user_id=? order by session_id desc limit 1;";
			preparedStatementSelect = sqlConnection.prepareStatement(strQuerySelect);
			preparedStatementSelect.setLong(1, deviceId);
			preparedStatementSelect.setInt(2, userId);
			
			ResultSet response = preparedStatementSelect.executeQuery();			
			long sessionId = 0;
			long timestampNow = System.currentTimeMillis();
			long timestampExpired = 0;
			if (response != null && response.next())
			{
				sessionId = response.getLong(1);
				timestampExpired = response.getTimestamp(2).getTime();
			
				preparedStatementSelect.close();
				preparedStatementSelect = null;
				
				final CBackendAdvancedEncryptionStandard encripter = new CBackendAdvancedEncryptionStandard(mEncriptionCode, "AES");
				final String sessionKey = encripter.encrypt(String.valueOf(sessionId));
				
				if (timestampNow < timestampExpired)
				{
					CBackendSession newSession = new CBackendSession(sessionId, userId, deviceId, sessionKey, timestampExpired, timestampNow);
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
				}  
				catch (SQLException e)  
				{ 
					e.printStackTrace(); 
				}
			}			
			
			try 
			{
				sqlConnection.close();
			} 
			catch (SQLException e) 
			{
				e.printStackTrace();
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
			
			// there is no session stored in the backend or the session was expired
			// create new session data
			
			// create session key
			String strQueryInsert = "insert into sessions ( user_id, device_id, session_creation_date, session_expire_date, session_ip ) values (?,?,?,?,?);";
			preparedStatementInsert = sqlConnection.prepareStatement(strQueryInsert, PreparedStatement.RETURN_GENERATED_KEYS);
			preparedStatementInsert.setInt(1, userId);
			preparedStatementInsert.setLong(2, deviceId);
			preparedStatementInsert.setTimestamp(3, new Timestamp(milisCurrent));
			preparedStatementInsert.setTimestamp(4, new Timestamp(milisLastOfTheDay));
			preparedStatementInsert.setString(5, remoteAddress);
			
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
			preparedStatementInsert.close();
			preparedStatementInsert = null;
			
			final CBackendAdvancedEncryptionStandard encripter = new CBackendAdvancedEncryptionStandard(mEncriptionCode, "AES");
			final String newSessionKey = encripter.encrypt(String.valueOf(sessionId));
			CBackendSession newSession = new CBackendSession(sessionId, userId, deviceId, newSessionKey, milisCurrent, milisLastOfTheDay);
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
				}  
				catch (SQLException e)  
				{ 
					e.printStackTrace(); 
				}
			}
			
			try 
			{
				sqlConnection.close();
			} 
			catch (SQLException e) 
			{
				e.printStackTrace();
			}
		}
		return null;
	}
	
}
