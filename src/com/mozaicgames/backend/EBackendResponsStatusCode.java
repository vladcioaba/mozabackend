package com.mozaicgames.backend;

public enum EBackendResponsStatusCode 
{
		STATUS_OK					(200),
		INVALID_REQUEST				(300),
		INVALID_DATA				(400),
		INTERNAL_ERROR				(500),
		RETRY_LATER					(900),
		CLIENT_OUT_OF_DATE			(800),
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
