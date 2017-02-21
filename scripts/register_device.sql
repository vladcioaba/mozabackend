CREATE TABLE `devices` (
  `device_id` bigint(20) NOT NULL AUTO_INCREMENT,
  `device_user_id` int(11) DEFAULT '0',
  `device_model` varchar(45) NOT NULL,
  `device_os_version` varchar(45) NOT NULL,
  `device_platform` varchar(45) NOT NULL,
  `device_app_version` varchar(45) NOT NULL,
  `device_ip` varchar(45) NOT NULL, 
  `device_creation_time` timestamp NOT NULL DEFAULT '1970-01-01 00:00:01',
  `device_blocked` tinyint(4) NOT NULL DEFAULT '0',
  PRIMARY KEY (`device_id`),
  UNIQUE KEY `device_id_UNIQUE` (`device_id`)
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=latin1