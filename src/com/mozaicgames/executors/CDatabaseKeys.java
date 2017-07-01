package com.mozaicgames.executors;

public class CDatabaseKeys 
{
	public static final String mKeyTableUsersTableName					= "users";
	public static final String mKeyTableUsersUserId						= "user_id";
	public static final String mKeyTableUsersUserCreationDate			= "user_creation_date";
	public static final String mKeyTableUsersUserLevel					= "user_level";
	public static final String mKeyTableUsersUserXp						= "user_xp";
	public static final String mKeyTableUsersUserTrophies				= "user_trophies";
	
	public static final String mKeyTableUsersSettingsTableName			= "users_settings";
	public static final String mKeyTableUsersSettingsId 				= "settings_id";	
	public static final String mKeyTableUsersSettingsUserId     		= "user_id";	
	public static final String mKeyTableUsersSettingsMagnetOn			= "settings_magnet_on";	
	public static final String mKeyTableUsersSettingsLeftHandedOn		= "settings_left_handed_on";
	public static final String mKeyTableUsersSettingsMusicOn			= "settings_music_on";
	public static final String mKeyTableUsersSettingsSfxOn				= "settings_sfx_on";
	
	public static final String mKeyTableUsersWalletDataTableName		= "users_wallet";
	public static final String mKeyTableUsersWalletDataId				= "wallet_id";
	public static final String mKeyTableUsersWalletDataUserId			= "user_id";
	public static final String mKeyTableUsersWalletDataDevicePlatform	= "device_platform";
	public static final String mKeyTableUsersWalletDataCreditsNum		= "wallet_credits_num";
	public static final String mKeyTableUsersWalletDataJokersNum		= "wallet_jokers_num";
	public static final String mKeyTableUsersWalletDataTokensNum		= "wallet_tokens_num";
	
	public static final String mKeyTableDevicesTableName				= "devices";
	public static final String mKeyTableDevicesDeviceId					= "device_id";
	public static final String mKeyTableDevicesUserId					= "user_id";
	public static final String mKeyTableDevicesModel					= "device_model";
	public static final String mKeyTableDevicesOsVersion 				= "device_os_version";
	public static final String mKeyTableDevicesPlatform					= "device_platform";
	public static final String mKeyTableDevicesUsageTime				= "device_usage_time";
	public static final String mKeyTableDevicesClientCoreVersion  		= "device_core_version";
	public static final String mKeyTableDevicesClientAppVersion   		= "device_app_version";
	public static final String mKeyTableDevicesFirstIp			   		= "device_first_ip";
	public static final String mKeyTableDevicesCreationDate		   		= "device_creation_date";
	public static final String mKeyTableDevicesUpdateDate				= "device_update_date";
	public static final String mKeyTableDevicesDeviceBlocked			= "device_blocked";
	
	public static final String mKeyTableGameResultsTableName 			= "game_results";
	public static final String mKeyTableGameResultsSessionToken 		= "session_token";
	public static final String mKeyTableGameResultsUserId	 			= "user_id";
	public static final String mKeyTableGameResultsCreationDate			= "creation_date";
	public static final	String mKeyTableGameResultsPlayerPlace			= "player_place";
	public static final	String mKeyTableGameResultsPlayerScore			= "player_score";
	public static final	String mKeyTableGameResultsPlayerStars			= "player_stars";
	public static final String mKeyTableGameResultsType		 			= "type";
	public static final String mKeyTableGameResultsSeed		 			= "seed";
	public static final String mKeyTableGameResultsSeedSource 			= "seed_source";
	public static final String mKeyTableGameResultsDuration	 			= "duration";
	public static final String mKeyTableGameResultsCompleteResult		= "complete_result";
	public static final String mKeyTableGameResultsDeckRefreshNum		= "deck_refresh_num";
	public static final String mKeyTableGameResultsUsedActionsNum		= "used_actions_num";
	public static final String mKeyTableGameResultsUsedHintsNum			= "used_hints_num";
	public static final String mKeyTableGameResultsUsedJokersNum		= "used_jokers_num";
	public static final String mKeyTableGameResultsFoundationRank1		= "foundation_rank_1";
	public static final String mKeyTableGameResultsFoundationRank2		= "foundation_rank_2";
	public static final String mKeyTableGameResultsFoundationRank3		= "foundation_rank_3";
	public static final String mKeyTableGameResultsFoundationRank4		= "foundation_rank_4";
	
	public static final String mKeyTableSessionTableName				= "sessions";
	public static final String mKeyTableSessionSessionId				= "session_id";
	public static final String mKeyTableSessionUserId					= "user_id";
	public static final String mKeyTableSessionDeviceId					= "device_id";
	public static final String mKeyTableSessionSessionToken				= "session_token";
	public static final String mKeyTableSessionCreationDate				= "session_creation_date";
	public static final String mKeyTableSessionExpireDate				= "session_expire_date";
	public static final String mKeyTableSessionIp						= "session_ip";
	public static final String mKeyTableSessionPlatform					= "session_platform";
	
	public static final String mKeyTableSessionHistoryTableName			= "sessions_history";
	public static final String mKeyTableSessionHistorySessionId			= "session_id";
	public static final String mKeyTableSessionHistorySessionToken		= "session_token";
	public static final String mKeyTableSessionHistoryCreationDate		= "session_creation_date";
	public static final String mKeyTableSessionHistoryIp				= "session_ip";
	public static final String mKeyTableSessionHistoryPlatform			= "session_platform";
}
