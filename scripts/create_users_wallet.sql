CREATE TABLE `users_wallet` (
  `wallet_id` int(11) NOT NULL AUTO_INCREMENT,
  `user_id` int(11) NOT NULL,
  `device_platform` varchar(45) NOT NULL,
  `wallet_credits_num` int(11),
  `wallet_jokers_num` int(11),
  `wallet_lives_num` int(11),
  `wallet_lives_max_num` int(11),
  PRIMARY KEY (`wallet_id`),
  UNIQUE KEY `wallet_id_UNIQUE` (`wallet_id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1