CREATE TABLE `game_results` (
  `game_results_id` bigint(20) NOT NULL AUTO_INCREMENT,
  `game_results_user_id` int(11) NOT NULL,
  `game_results_session_id` bigint(20) NOT NULL,
  `game_results_creation_date` timestamp NOT NULL DEFAULT '1970-01-01 00:00:01',
  `game_results_type` int(11) NOT NULL,
  `game_results_seed` varchar(45) NOT NULL,
  `game_results_seed_source` int(11) NOT NULL,
  `game_results_duration` int(11) NOT NULL,
  `game_results_complete_result` int(11) NOT NULL,
  `game_results_used_actions_num` int(11) NOT NULL,
  `game_results_used_hints_num` int(11) NOT NULL,
  `game_results_used_jockers_num` int(11) NOT NULL,
  PRIMARY KEY (`game_results_id`),
  UNIQUE KEY `game_results_id_UNIQUE` (`game_results_id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1
