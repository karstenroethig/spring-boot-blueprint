# How-to backup the database

This guide describes how to create backups of the database.


## Create one simple backup of the database

* Create one simple backup of the database with the following command

	mysqldump -u root -p[PASSWORD] --single-transaction spring-boot-blueprint_prod > ~/mysql__spring-boot-blueprint_prod__dump.sql


## Restore database from backup

* Empty database

	mysql -u root -p
	use spring-boot-blueprint_test;
	source ~/drop_all_tables.sql;
	exit;

* Restore backup

	mysql -u root -p spring-boot-blueprint_test < ~/backup.sql
