package com.mozaicgames.core;

public class CBackendRequestException extends Exception
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private final EBackendResponsStatusCode		mCodeStatus;
	private	final String						mStrBody;							
	
	public CBackendRequestException(EBackendResponsStatusCode status, String body)
	{
		mCodeStatus = status;
		mStrBody = body;
	}
	
	public EBackendResponsStatusCode getStatus()
	{
		return mCodeStatus;
	}
	
	public String getBody()
	{
		return mStrBody;
	}
}
