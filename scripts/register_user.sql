CREATE TABLE `users` (
  `user_id` int(11) NOT NULL DEFAULT '0',
  `user_email` varchar(45) DEFAULT 'none',
  `user_password` varchar(45) DEFAULT 'none',
  `user_fb_token` varchar(45) DEFAULT 'none',
  `user_creation_date` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `user_app_version` varchar(45) NOT NULL,
  PRIMARY KEY (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1