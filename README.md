write commands:
docker cp backup.sql balance_db_container:/tmp/backup.sql 
docker exec -i balance_db_container sh -c 'mysql -u root -pdenis123 balance_db < /tmp/backup.sql'
