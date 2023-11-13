CREATE TABLE database_sync_events (
  id uniqueidentifier NOT NULL,
   created_at datetime NOT NULL,
   updated_at datetime,
   name varchar(255),
   success bit,
   log varchar(255),
   CONSTRAINT pk_database_sync_events PRIMARY KEY (id)
)
GO