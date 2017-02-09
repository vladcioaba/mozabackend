CREATE TABLE `devices` (
  `device_id` int(11) NOT NULL AUTO_INCREMENT,
  `device_token` varchar(45) NOT NULL,
  `device_model` varchar(45) NOT NULL,
  `device_os_version` varchar(45) NOT NULL,
  `device_platform` varchar(45) NOT NULL,
  `device_creation_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`device_id`),
  UNIQUE KEY `device_id_UNIQUE` (`device_id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1

insert into  devices ( device_token, device_model , device_os_version , device_platform ) values ( HEX(AES_ENCRYPT( LAST_INSERT_ID() + 1, 'mozadev')), 'iphone' , '7.1' , 'ios'  );