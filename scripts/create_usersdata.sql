CREATE TABLE `usersdata` (
  `userdata_id` int(11) NOT NULL AUTO_INCREMENT,
  `user_id` int(11) NOT NULL,
  `data_magnet_on` tinyint(4),
  `data_left_handed_on` tinyint(4),
  `data_music_on` tinyint(4),
  `data_sfx_on` tinyint(4),
  `data_credits` int(11),
  PRIMARY KEY (`userdata_id`),
  UNIQUE KEY `userdata_id_UNIQUE` (`userdata_id`),
  UNIQUE KEY `user_id_UNIQUE` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1