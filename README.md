# README

## Project Overview
This project is a microservices-based cryptocurrency trading platform. It includes various services such as User Service, Coin Service, Wallet Service, Asset Management Service, Trading Service, Watchlist Service, Withdrawal Service, Payment Service, and Security & Compliance Service.

## Key Features
- **Microservice Architecture**: Utilized microservices to separate functionalities and manage scalability.
- **Role-Based Authorization**: Implemented secure access control based on user roles.
- **Crypto Trading**: Supports real-time buy/sell order placement.
- **Market Data**: Fetch real-time cryptocurrency data from CoinGecko and Binance.
- **Asset & Wallet Management**: Track balances and update asset values.
- **Admin Functions**: Manage users, coins on the exchange (CRUD), monitor transactions, and handle withdrawal requests.
- **Payment & Withdrawal Processing**: Integrated with SePay for transaction verification.
- **Watchlist**: Save and monitor price fluctuations of favorite coins.
- **Notification System**: Send alerts for upcoming events and important market updates.
- **Authentication**: Users can register, log in, and reset passwords with email verification for security.
- **AI Chatbot Integration**: Integrated with Gemini AI to provide real-time market insights, trading assistance, and user support.

## Technologies Used
- **Spring Boot**
- **Spring Cloud**
- **Spring Boot Starter Data JPA**
- **Spring Boot Starter Web**
- **Spring Cloud Netflix Eureka**
- **Spring Boot DevTools**
- **MySQL**
- **Lombok**
- **OpenFeign**
- **Redis**

## Microservices
### 1. User Service
- Manages user authentication and authorization.
- Allows Google and Facebook sign-up.
- Stores user details securely.

### 2. Coin Service
- Fetches cryptocurrency data.
- Stores market data such as price, market cap, and volume.

### 3. Wallet Service
- Manages user wallets.
- Allows deposits and withdrawals.
- Handles transactions securely.

### 4. Asset Management Service
- Tracks users' assets.
- Updates asset values based on transactions.

### 5. Trading Service
- Handles buying and selling of cryptocurrencies.
- Updates user balances after transactions.

### 6. Watchlist Service
- Allows users to track favorite coins.
- Provides price alerts.

### 7. Withdrawal Service
- Manages withdrawal requests.
- Ensures approval before processing.

### 8. Payment Service
- Handles payment requests and QR code generation.
- Verifies transactions every 3 seconds.

## Deployment
This project uses an API Gateway for routing and service discovery using Eureka Server. Configuration settings are managed via a Config Server.

## Installation & Setup
```sh
# Clone the repository
git clone https://github.com/leduc432003/cryptoTradingPlatform.git

# Navigate to the project directory
cd crypto-trading-platform

# Start the Eureka Server
cd eureka-server
mvn spring-boot:run

# Start each microservice
cd user-service
mvn spring-boot:run
```
Repeat for other services.

Access the API Gateway at `http://localhost:5000`.
