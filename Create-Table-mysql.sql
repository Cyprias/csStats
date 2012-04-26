CREATE TABLE `%1s` (
`id` INT NOT NULL AUTO_INCREMENT PRIMARY KEY ,
`owner` VARCHAR( 32 ) NOT NULL ,
`TypeId` INT NOT NULL ,
`Durability` INT NOT NULL ,
`amount` INT NOT NULL ,
`world` VARCHAR( 32 ) NOT NULL ,
`X` INT NOT NULL ,
`Y` INT NOT NULL ,
`Z` INT NOT NULL ,
`enchantments` INT NOT NULL ,
`unixtime` INT NOT NULL
) ENGINE = MYISAM ;