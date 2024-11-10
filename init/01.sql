CREATE DATABASE IF NOT EXISTS kltn_user_service;
CREATE DATABASE IF NOT EXISTS kltn_coin_service;
CREATE DATABASE IF NOT EXISTS kltn_asset_service;
CREATE DATABASE IF NOT EXISTS kltn_trading_service;
CREATE DATABASE IF NOT EXISTS kltn_wallet_service;
CREATE DATABASE IF NOT EXISTS kltn_withdrawal_service;
CREATE DATABASE IF NOT EXISTS kltn_watchlist_service;

CREATE USER 'root'@'localhost' IDENTIFIED BY 'local';
GRANT ALL PRIVILEGES ON *.* TO 'root'@'%';