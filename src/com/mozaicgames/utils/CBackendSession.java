package com.mozaicgames.utils;

public class CBackendSession 
{
	
	private final long 			mId;
	private final int			mUserId;
	private final long			mDeviceId;
	private final long			mExpireTime;
	private final long			mCreationTime;
	private final String		mIp;
	private final String		mKey;
	
	public CBackendSession(long id, int userId, long deviceId, String key, long expireTime, long creationTime, String ip)
	{
		mId = id;
		mUserId = userId;
		mDeviceId = deviceId;
		mKey = key;
		mExpireTime = expireTime;
		mCreationTime = creationTime;
		mIp = ip;
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
	
	public long getExpireTime()
	{
		return mExpireTime;
	}
	
	public long getCreationTime()
	{
		return mCreationTime;
	}

	public String getIp()
	{
		return mIp;
	}
}
