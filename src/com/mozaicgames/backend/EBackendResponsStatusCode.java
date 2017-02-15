package com.mozaicgames.backend;

public enum EBackendResponsStatusCode 
{
		STATUS_OK					(200),
		INVALID_REQUEST				(4001),
		INVALID_DATA				(4002),
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
