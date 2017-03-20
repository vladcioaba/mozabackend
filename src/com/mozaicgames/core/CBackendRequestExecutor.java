package com.mozaicgames.core;

import org.json.JSONObject;

public abstract class CBackendRequestExecutor
{
	public CBackendRequestExecutor()
	{
		
	}
	
	public boolean isSessionTokenValidationNeeded() { return false; }
	
	abstract public CBackendRequestExecutorResult execute(JSONObject jsonData, CBackendRequestExecutorParameters parameters);
}
