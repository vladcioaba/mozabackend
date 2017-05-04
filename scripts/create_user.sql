CREATE TABLE `users` (
  `user_id` int(11) NOT NULL AUTO_INCREMENT,
  `user_creation_date` timestamp NOT NULL DEFAULT '1970-01-01 00:00:01',
  `user_level` int(11) NOT NULL DEFAULT 1,
  `user_xp` int(11) NOT NULL DEFAULT 0,
  `user_trophies` int(11) NOT NULL DEFAULT 1000,
  PRIMARY KEY (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1