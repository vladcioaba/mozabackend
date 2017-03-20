package com.mozaicgames.executors;

import org.json.JSONObject;

import com.mozaicgames.core.CBackendRequestException;
import com.mozaicgames.core.CBackendRequestExecutor;
import com.mozaicgames.core.CBackendRequestExecutorParameters;
import com.mozaicgames.core.EBackendResponsStatusCode;

public class CRequestExecutorCheckServer extends CBackendRequestExecutor
{
	@Override
	public JSONObject execute(JSONObject jsonData, CBackendRequestExecutorParameters parameters) throws CBackendRequestException
	{	
		return toJSONObject(EBackendResponsStatusCode.STATUS_OK, "Hello!");
	}
}
