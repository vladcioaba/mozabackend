CREATE TABLE `game_results` (
  `result_id` bigint(20) NOT NULL AUTO_INCREMENT,
  `user_id` int(11) NOT NULL,
  `session_id` bigint(20) NOT NULL,
  `creation_date` timestamp NOT NULL DEFAULT '1970-01-01 00:00:01',
  `type` int(11) NOT NULL,
  `seed` varchar(45) NOT NULL,
  `seed_source` int(11) NOT NULL,
  `duration` int(11) NOT NULL,
  `complete_result` int(11) NOT NULL,
  `deck_refresh_num` int(11) NOT NULL,
  `used_actions_num` int(11) NOT NULL,
  `used_hints_num` int(11) NOT NULL,
  `used_jockers_num` int(11) NOT NULL,
  PRIMARY KEY (`result_id`),
  UNIQUE KEY `result_id_UNIQUE` (`result_id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1