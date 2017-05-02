CREATE TABLE `users_settings` (
  `settings_id` int(11) NOT NULL AUTO_INCREMENT,
  `user_id` int(11) NOT NULL,
  `settings_magnet_on` tinyint(4),
  `settings_left_handed_on` tinyint(4),
  `settings_music_on` tinyint(4),
  `settings_sfx_on` tinyint(4),
  PRIMARY KEY (`settings_id`),
  UNIQUE KEY `settings_id_UNIQUE` (`settings_id`),
  UNIQUE KEY `user_id_UNIQUE` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1