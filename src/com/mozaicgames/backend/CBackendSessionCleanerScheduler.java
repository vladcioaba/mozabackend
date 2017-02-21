package com.mozaicgames.backend;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class CBackendSessionCleanerScheduler implements Runnable
{
	private final CBackendSessionManager 	mSessionManager;

    public CBackendSessionCleanerScheduler(CBackendSessionManager sessionManager) {
        this.mSessionManager = sessionManager;
    }

    public void startService() {
        ScheduledExecutorService service = Executors.newSingleThreadScheduledExecutor();
        service.scheduleAtFixedRate(this, CBackendSessionManager.TIME_TO_LIVE, CBackendSessionManager.TIME_TO_LIVE, TimeUnit.MILLISECONDS);
    }

    @Override
    public void run() {
    	mSessionManager.cleanInvalidSessions();
    }
    
}
