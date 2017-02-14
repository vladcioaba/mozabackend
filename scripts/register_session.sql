CREATE TABLE `sessions` (
  `session_id` bigint(20) NOT NULL DEFAULT '0',
  `user_id` int(11) NOT NULL DEFAULT '0',
  `device_id` bigint(20) NOT NULL DEFAULT '0',
  `session_creation_date` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `session_expire_date` datetime NOT NULL,
  `session_ip` varchar(45) NOT NULL,
  PRIMARY KEY (`session_id`),
  KEY `user_id_idx` (`user_id`),
  KEY `device_id_idx` (`device_id`),
  CONSTRAINT `device_id` FOREIGN KEY (`device_id`) REFERENCES `devices` (`device_id`) ON DELETE NO ACTION ON UPDATE NO ACTION,
  CONSTRAINT `user_id` FOREIGN KEY (`user_id`) REFERENCES `users` (`user_id`) ON DELETE NO ACTION ON UPDATE NO ACTION
) ENGINE=InnoDB DEFAULT CHARSET=latin1