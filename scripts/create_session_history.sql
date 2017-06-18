CREATE TABLE sessions_history (
  `session_history_id` INT NOT NULL AUTO_INCREMENT,
  `session_id` INT NOT NULL,
  `session_token` VARCHAR(45) NOT NULL,
  `session_creation_date` DATETIME NOT NULL,
  `session_ip` VARCHAR(45) NOT NULL,
  `session_platform` VARCHAR(45) NOT NULL,
  PRIMARY KEY (`session_history_id`)
 ) ENGINE=InnoDB DEFAULT CHARSET=latin1;