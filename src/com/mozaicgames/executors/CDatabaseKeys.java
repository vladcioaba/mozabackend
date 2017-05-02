package com.mozaicgames.executors;

public class CDatabaseKeys 
{
	public static final String mKeyTableUsersTableName				= "users";
	public static final String mKeyTableUsersUserCreationDate		= "user_creation_date";	
	
	public static final String mKeyTableUsersSettingsTableName		= "users_settings";
	public static final String mKeyTableUsersSettingsId 			= "settings_id";	
	public static final String mKeyTableUsersSettingsUserId     	= "user_id";	
	public static final String mKeyTableUsersSettingsMagnetOn		= "settings_magnet_on";	
	public static final String mKeyTableUsersSettingsLeftHandedOn	= "settings_left_handed_on";
	public static final String mKeyTableUsersSettingsMusicOn		= "settings_music_on";
	public static final String mKeyTableUsersSettingsSfxOn			= "settings_sfx_on";
	
	public static final String mKeyTableUsersDataTableName		    = "users_data";
	public static final String mKeyTableUsersDataId					= "data_id";
	public static final String mKeyTableUsersDataUserId				= "user_id";
	public static final String mKeyTableUsersDataDevicePlatform		= "device_platform";
	public static final String mKeyTableUsersDataCreditsNum			= "data_credits_num";
	public static final String mKeyTableUsersDataJockersNum			= "data_jockers_num";
	public static final String mKeyTableUsersDataLivesNum			= "data_lives_num";
	public static final String mKeyTableUsersDataLivesMaxNum		= "data_lives_max_num";
	
	public static final String mKeyTableDevicesTableName			= "devices";
	public static final String mKeyTableDevicesUserId				= "user_id";
	public static final String mKeyTableDevicesModel				= "device_model";
	public static final String mKeyTableDevicesOsVersion 			= "device_os_version";
	public static final String mKeyTableDevicesPlatform				= "device_platform";
	public static final String mKeyTableDevicesUsageTime			= "device_usage_time";
	public static final String mKeyTableDevicesClientCoreVersion  	= "device_core_version";
	public static final String mKeyTableDevicesClientAppVersion   	= "device_app_version";
	public static final String mKeyTableDevicesFirstIp			   	= "device_first_ip";
	public static final String mKeyTableDevicesCreationDate		   	= "device_creation_date";
	public static final String mKeyTableDevicesUpdateDate			= "device_update_date";
	public static final String mKeyTableDevicesDeviceId				= "device_id";
	public static final String mKeyTableDevicesDeviceBlocked		= "device_blocked";
	
	public static final String mKeyTableGameResultsTableName 		= "game_results";
	public static final String mKeyTableGameResultsSessionId 		= "session_id";
	public static final String mKeyTableGameResultsUserId	 		= "user_id";
	public static final String mKeyTableGameResultsCreationDate		= "creation_date";
	public static final String mKeyTableGameResultsType		 		= "type";
	public static final String mKeyTableGameResultsSeed		 		= "seed";
	public static final String mKeyTableGameResultsSeedSource 		= "seed_source";
	public static final String mKeyTableGameResultsDuration	 		= "duration";
	public static final String mKeyTableGameResultsCompleteResult	= "complete_result";
	public static final String mKeyTableGameResultsDeckRefreshNum	= "deck_refresh_num";
	public static final String mKeyTableGameResultsUsedActionsNum	= "used_actions_num";
	public static final String mKeyTableGameResultsUsedHintsNum		= "used_hints_num";
	public static final String mKeyTableGameResultsUsedJockersNum	= "used_jockers_num";
	
	public static final String mKeyTableSessionTableName			= "sessions";
	public static final String mKeyTableSessionUserId				= "user_id";
	public static final String mKeyTableSessionDeviceId				= "device_id";
	public static final String mKeyTableSessionCreationData			= "session_creation_date";
	public static final String mKeyTableSessionExpireData			= "session_expire_date";
	public static final String mKeyTableSessionIp					= "session_ip";
	public static final String mKeyTableSessionPlatform				= "session_platform";
}
