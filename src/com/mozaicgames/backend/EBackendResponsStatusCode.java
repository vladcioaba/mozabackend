package com.mozaicgames.backend;

public enum EBackendResponsStatusCode {

		STATUS_OK					(200),
		INVALID_REQUEST				(300),
		INVALID_DATA				(301),
		INTERNAL_ERROR				(302),
		INTERNAL_SQL_TIMEOUT		(303),
		INTERNAL_SQL_ERROR			(304),
		RETRY_LATER					(500),
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
