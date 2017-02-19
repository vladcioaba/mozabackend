CREATE TABLE `sessions` (
  `session_id` bigint(20) NOT NULL AUTO_INCREMENT,
  `user_id` int(11) NOT NULL,
  `device_id` bigint(20) NOT NULL,
  `session_creation_date` datetime NOT NULL ,
  `session_expire_date` datetime NOT NULL,
  `session_ip` varchar(45) NOT NULL,
  PRIMARY KEY (`session_id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;