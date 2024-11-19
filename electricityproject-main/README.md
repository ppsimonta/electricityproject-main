# ElectricityProject

## Getting started

Currently the database must be created locally, so the first step is to forward engineer this database schema to phpMyAdmin:

```
-- MySQL Workbench Forward Engineering

SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0;
SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0;
SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='ONLY_FULL_GROUP_BY,STRICT_TRANS_TABLES,NO_ZERO_IN_DATE,NO_ZERO_DATE,ERROR_FOR_DIVISION_BY_ZERO,NO_ENGINE_SUBSTITUTION';

-- -----------------------------------------------------
-- Schema pricechecker_database
-- -----------------------------------------------------

-- -----------------------------------------------------
-- Schema pricechecker_database
-- -----------------------------------------------------
CREATE SCHEMA IF NOT EXISTS `pricechecker_database` DEFAULT CHARACTER SET utf8 ;
USE `pricechecker_database` ;

-- -----------------------------------------------------
-- Table `pricechecker_database`.`time_and_price`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `pricechecker_database`.`time_and_price` (
  `id` INT NOT NULL AUTO_INCREMENT,
  `time` DATETIME NOT NULL UNIQUE,
  `value` FLOAT NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE = InnoDB;

SET SQL_MODE=@OLD_SQL_MODE;
SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS;
SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS;
```