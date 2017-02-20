package com.mozaicgames.backend;

public class CBackendSession 
{
	
	private final long 			mId;
	private final int			mUserId;
	private final long			mDeviceId;
	private final long			mCreationTime;
	private final String		mKey;
	
	public CBackendSession(long id, int userId, long deviceId, String key, long creationTime)
	{
		mId = id;
		mUserId = userId;
		mDeviceId = deviceId;
		mKey = key;
		mCreationTime = creationTime;
	}
	
	public long getId()
	{
		return mId;
	}
	
	public int getUserId()
	{
		return mUserId;
	}
	
	public long getDeviceId()
	{
		return mDeviceId;
	}

	public String getKey()
	{
		return mKey;
	}
	
	public long getCreationTime()
	{
		return mCreationTime;
	}

}
