# --- !Ups
CREATE TABLE `USER` (
  `ID`      INT  NOT NULL PRIMARY KEY AUTO_INCREMENT,
  `NAME`        VARCHAR(255) UNIQUE,
  `PROVIDER_ID`  TEXT NOT NULL,
  `PROVIDER_KEY` TEXT NOT NULL
);

CREATE TABLE `PASSWORD`(
  `PROVIDER_KEY` VARCHAR(245) NOT NULL PRIMARY KEY,
  `HASHER` TEXT NOT NULL,
  `HASH` TEXT NOT NULL,
  `SALT` TEXT
);

# --- !Downs

DROP TABLE `USER`;
DROP TABLE `PASSWORD`;
