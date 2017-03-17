package com.mozaicgames.executors;

import org.json.JSONObject;

import com.mozaicgames.core.CBackendRequestExecutor;
import com.mozaicgames.core.CBackendRequestExecutorParameters;
import com.mozaicgames.core.CBackendRequestExecutorResult;
import com.mozaicgames.core.EBackendResponsStatusCode;

public class CRequestExecutorCheckServer extends CBackendRequestExecutor
{
	@Override
	public CBackendRequestExecutorResult execute(JSONObject jsonData, CBackendRequestExecutorParameters parameters) 
	{	
		return new CBackendRequestExecutorResult(EBackendResponsStatusCode.STATUS_OK, "Hello!");
	}
}
