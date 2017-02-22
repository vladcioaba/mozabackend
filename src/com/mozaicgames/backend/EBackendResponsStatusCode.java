package com.mozaicgames.backend;

public enum EBackendResponsStatusCode 
{
		BAD_REQUEST					(0),
		STATUS_OK					(2000),
		INVALID_REQUEST				(4001),
		INVALID_DATA				(4002),
		INVALID_TOKEN_DEVICE		(4010),
		INVALID_TOKEN_USER			(4020),
		INVALID_TOKEN_SESSION_KEY	(4030),
		INTERNAL_ERROR				(5000),
		RETRY_LATER					(8000),
		CLIENT_OUT_OF_DATE			(9000),
		CLIENT_REJECTED				(1000);
	
		private final int mValue;
	
		EBackendResponsStatusCode(int val) 
		{
			mValue = val;
		}
		
		public int getValue()
		{
			return mValue;
		}
	
}
