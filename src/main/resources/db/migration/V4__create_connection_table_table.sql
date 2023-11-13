CREATE TABLE database_sync_connections_tables (
  last_sync_date datetime,
   connection_id uniqueidentifier NOT NULL,
   table_id uniqueidentifier NOT NULL,
   CONSTRAINT pk_database_sync_connections_tables PRIMARY KEY (connection_id, table_id)
)
GO

ALTER TABLE database_sync_connections_tables ADD CONSTRAINT FK_DATABASE_SYNC_CONNECTIONS_TABLES_ON_CONNECTION FOREIGN KEY (connection_id) REFERENCES database_sync_connections (id)
GO

ALTER TABLE database_sync_connections_tables ADD CONSTRAINT FK_DATABASE_SYNC_CONNECTIONS_TABLES_ON_TABLE FOREIGN KEY (table_id) REFERENCES database_sync_tables (id)
GO