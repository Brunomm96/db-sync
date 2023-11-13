CREATE TABLE database_sync_tables (
  id uniqueidentifier NOT NULL,
   created_at datetime NOT NULL,
   updated_at datetime,
   label varchar(255) NOT NULL,
   name varchar(255),
   primary_keys varchar(255),
   sync_field varchar(255),
   CONSTRAINT pk_database_sync_tables PRIMARY KEY (id)
)
GO

ALTER TABLE database_sync_tables ADD CONSTRAINT uc_database_sync_tables_label UNIQUE (label)
GO