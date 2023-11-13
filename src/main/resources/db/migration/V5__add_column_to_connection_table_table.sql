ALTER TABLE database_sync_connections_tables
ADD
    time_interval int,
    is_running bit,
    custom_query varchar(5000),
    is_active bit
GO