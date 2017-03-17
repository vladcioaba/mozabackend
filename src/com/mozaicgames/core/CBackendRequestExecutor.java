package com.mozaicgames.core;

import org.json.JSONObject;

public abstract class CBackendRequestExecutor
{
	public CBackendRequestExecutor()
	{
		
	}
	
	abstract public CBackendRequestExecutorResult execute(JSONObject jsonData, CBackendRequestExecutorParameters parameters);
}
