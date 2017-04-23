CREATE TABLE `users_data` (
  `user_data_id` int(11) NOT NULL AUTO_INCREMENT,
  `user_id` int(11) NOT NULL,
  `data_magnet_on` tinyint(4),
  `data_left_handed_on` tinyint(4),
  `data_music_on` tinyint(4),
  `data_sfx_on` tinyint(4),
  `data_credits_num` int(11),
  `data_jockers_num` int(11),
  `data_lives_num` int(11),
  `data_lives_max_num` int(11),
  PRIMARY KEY (`user_data_id`),
  UNIQUE KEY `user_data_id_UNIQUE` (`user_data_id`),
  UNIQUE KEY `user_id_UNIQUE` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1