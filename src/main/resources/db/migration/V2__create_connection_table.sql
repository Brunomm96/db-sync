CREATE TABLE database_sync_connections (
  id uniqueidentifier NOT NULL,
   created_at datetime NOT NULL,
   updated_at datetime,
   driver_classname varchar(255),
   name varchar(255),
   url varchar(255),
   username varchar(255),
   password varchar(255),
   CONSTRAINT pk_database_sync_connections PRIMARY KEY (id)
)
GO