CREATE TABLE `sessions` (
  `session_id` bigint(20) NOT NULL AUTO_INCREMENT,
  `user_id` int(11) NOT NULL,
  `device_id` bigint(20) NOT NULL,
  `session_creation_date` timestamp NOT NULL DEFAULT '1970-01-01 00:00:01',
  `session_expire_date` timestamp NOT NULL DEFAULT '1970-01-01 00:00:01',
  `session_ip` varchar(45) NOT NULL,
  `session_platform` varchar(45) NOT NULL,
  PRIMARY KEY (`session_id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;