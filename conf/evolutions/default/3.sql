# --- !Ups
ALTER TABLE `TASKS` ADD (`DEADLINE` TIMESTAMP);

# --- !Downs
ALTER TABLE `TASKS` DROP `DEADLINE`;