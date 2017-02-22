package com.mozaicgames.utils;

import com.mozaicgames.backend.EBackendResponsStatusCode;

public class CBackendQueryResponse {

	private final EBackendResponsStatusCode 	mStatus;
	private final String 						mBody;
	
	public CBackendQueryResponse(EBackendResponsStatusCode status, String body)
	{
		mStatus = status;
		mBody = body;
	}
	
	public EBackendResponsStatusCode getCode()
	{
		return mStatus;
	}
	
	public String getBody()
	{
		return mBody;
	}
	
}
