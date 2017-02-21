CREATE TABLE `users` (
  `user_id` int(11) NOT NULL AUTO_INCREMENT,
  `user_email` varchar(45) DEFAULT 'none',
  `user_password` varchar(45) DEFAULT 'none',
  `user_fb_token` varchar(45) DEFAULT 'none',
  `user_game_credits` int(11) NOT NULL DEFAULT 100,
  `user_creation_date` timestamp NOT NULL DEFAULT '1970-01-01 00:00:01',
  PRIMARY KEY (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1