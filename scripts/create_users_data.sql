CREATE TABLE `users_data` (
  `data_id` int(11) NOT NULL AUTO_INCREMENT,
  `user_id` int(11) NOT NULL,
  `device_platform` varchar(45) NOT NULL,
  `data_credits_num` int(11),
  `data_jockers_num` int(11),
  `data_lives_num` int(11),
  `data_lives_max_num` int(11),
  PRIMARY KEY (`data_id`),
  UNIQUE KEY `data_id_UNIQUE` (`data_id`),
  UNIQUE KEY `user_id_UNIQUE` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1