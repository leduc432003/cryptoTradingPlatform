create database if not exists 'kltn_user_service'
create database if not exists 'kltn_coin_service'
create database if not exists 'kltn_asset_service'
create database if not exists 'kltn_trading_service'
create database if not exists 'kltn_wallet_service'


CREATE USER 'root'@'localhost' IDENTIFIED BY 'local';
GRANT ALL PRIVILEGES ON *.* TO 'root'@'%';