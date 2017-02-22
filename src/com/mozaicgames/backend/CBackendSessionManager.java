package com.mozaicgames.backend;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import javax.sql.DataSource;

import com.mozaicgames.utils.AdvancedEncryptionStandard;

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
	
	public synchronized boolean isSessionValid(String sessionKey)
	{
		return isSessionValid(getActiveSession(sessionKey));
	}
	
	private synchronized boolean isSessionValid(CBackendSession session)
	{
		long now = System.currentTimeMillis();
		if (session != null)
		{
			return now < session.getExpireTime();
		}
		return false;
	}
	
	public synchronized CBackendSession getActiveSession(String sessionKey)
	{
		return mActiveSessions.get(sessionKey);
	}
	
	public synchronized CBackendSession getSessionFor(long deviceId, int userId)
	{
		final AdvancedEncryptionStandard encripter = new AdvancedEncryptionStandard(mEncriptionCode, "AES");
		final String sessionPhrase = String.valueOf(deviceId) + "_" + String.valueOf(userId);
		String sessionKey = null;
		try {
			sessionKey = encripter.encrypt(sessionPhrase);
			if (isSessionValid(sessionKey))
			{
				return mActiveSessions.get(sessionKey);
			}
		} catch (Exception e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
			return null;
		}
		
		
		Connection sqlConnection = null;
		PreparedStatement preparedStatementSelect = null;
		try 
		{
			sqlConnection = mSqlDataSource.getConnection();
			sqlConnection.setAutoCommit(false);
			
			// find the session in the database first
			String strQuerySelect = "select session_id, session_expire_date from sessions where device_id=? and user_id=? order by session_id desc limit 1;";
			preparedStatementSelect = sqlConnection.prepareStatement(strQuerySelect, Statement.RETURN_GENERATED_KEYS);
			preparedStatementSelect.setLong(1, deviceId);
			preparedStatementSelect.setInt(2, userId);
			
			ResultSet response = preparedStatementSelect.executeQuery();			
			long sessionId = 0;
			long timestampNow = System.currentTimeMillis();
			long timestampExpired = 0;
			if (response != null && response.next())
			{
				sessionId = response.getLong(1);
				timestampExpired = response.getTimestamp(1).getTime();
			
				preparedStatementSelect.close();
				preparedStatementSelect = null;
				
				if (timestampNow < timestampExpired)
				{
					CBackendSession newSession = new CBackendSession(sessionId, userId, deviceId, sessionKey, timestampExpired, timestampNow);
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
			
			// there is no session stored in the backend or the session was expired
			// create new session data
			long sessionId = 0;
			final long timeToExpire = 300000;
			final long timestampCreated = System.currentTimeMillis();
			final long timestampExpired = timestampCreated + timeToExpire;
			
			// create session key
			String strQueryInsert = "insert into sessions ( user_id, device_id, session_creation_date, session_expire_date, session_ip ) values (?,?,?,?,?);";
			preparedStatementInsert = sqlConnection.prepareStatement(strQueryInsert, PreparedStatement.RETURN_GENERATED_KEYS);
			preparedStatementInsert.setInt(1, userId);
			preparedStatementInsert.setLong(2, deviceId);
			preparedStatementInsert.setTimestamp(3, new Timestamp(timestampCreated));
			preparedStatementInsert.setTimestamp(4, new Timestamp(timestampExpired));
			preparedStatementInsert.setString(5, remoteAddress);
			
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
			
			final AdvancedEncryptionStandard encripter = new AdvancedEncryptionStandard(mEncriptionCode, "AES");
			final String sessionPhrase = String.valueOf(deviceId) + "_" + String.valueOf(userId);
			final String newSessionKey = encripter.encrypt(sessionPhrase);
			CBackendSession newSession = new CBackendSession(sessionId, userId, deviceId, newSessionKey, timestampExpired, timestampCreated);
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
