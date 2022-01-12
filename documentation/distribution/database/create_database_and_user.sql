-- Create a database for the application to store the data.
create database spring-boot-blueprint_prod;

-- Create a database user which the application will connect as and grant the required permissions.
create user 'spring-boot-blueprint_user'@'localhost' identified by 'spring-boot-blueprint_password';
grant CREATE, DROP, DELETE, INSERT, SELECT, UPDATE, ALTER, REFERENCES on spring-boot-blueprint_prod.* to 'spring-boot-blueprint_user'@'localhost';
