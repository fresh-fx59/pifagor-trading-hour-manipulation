# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

This is a cryptocurrency trading system that implements Fibonacci-based trading strategies for Bybit. The system uses a state machine architecture to process real-time kline (candlestick) data from WebSockets, calculate Fibonacci retracement levels, and manage trading orders with leverage support.

## Multi-Module Maven Architecture

This is a multi-module Maven project with 15+ modules organized around distinct concerns:

**Core Trading Modules:**
- `trading-app` - Main application entry point, WebSocket connection, data pipeline orchestration
- `trading-logic` - Core trading strategy implementation with state machines (FibaProcessor, CandleProcessor)
- `trading-api` - API interfaces for order management and trading operations
- `common` - Shared models, enums, utilities, and configuration

**Data Processing:**
- `bybit-data-grabber` - Bybit API integration for historical data retrieval
- `csv-processor` - CSV data import/export utilities
- `trading-data-generator` - Test data generation
- `websocket-server-jakarta` - WebSocket server for testing

**Database Migration:**
- `postgres-flyway` - PostgreSQL schema migrations for trading data
- `postgres-auth-flyway` - PostgreSQL schema migrations for authentication
- `postgres-token-flyway` - PostgreSQL schema migrations for token management
- `clickhouse-flyway` - ClickHouse schema migrations for kline data storage

**Other:**
- `fibonacci-levels` - Fibonacci calculation utilities
- `old-user-auth` - Legacy authentication service (Spring Boot with JWT)
- `old-user-exchange-token` - Legacy token exchange service

## Build and Development Commands

### Building the Project

```bash
# Build entire project from root
mvn clean install

# Build specific module (from root)
mvn clean install -pl trading-app -am

# Package trading-app as executable JAR
cd trading-app && mvn clean package
```

### Running the Application

```bash
# Run trading-app (main application)
cd trading-app
java -jar target/trading-app-1.0-SNAPSHOT.jar [PROD|TEST]

# Run from source with profile
cd trading-app
mvn exec:java -Dexec.mainClass="org.example.Main" -Dexec.args="PROD"
```

### Database Migrations

```bash
# Migrate ClickHouse
cd clickhouse-flyway
mvn flyway:migrate

# Migrate PostgreSQL (trading data)
cd postgres-flyway
mvn flyway:migrate -Dflyway.configFiles=flyway.conf

# Migrate PostgreSQL (auth)
cd postgres-auth-flyway
mvn flyway:migrate -Dflyway.configFiles=flyway.conf
```

### Running Tests

```bash
# Run all tests
mvn test

# Run tests for specific module
mvn test -pl trading-logic

# Run single test class
cd trading-logic
mvn test -Dtest=KlineProcessorImplTest

# Run single test method
mvn test -Dtest=KlineProcessorImplTest#specificTestMethod
```

### Docker Environment

```bash
# Start infrastructure (ClickHouse, PostgreSQL, ClickHouse Keeper)
docker-compose up -d

# Stop infrastructure
docker-compose down

# View logs
docker-compose logs -f pthm-clickhouse-server
docker-compose logs -f pthm-postgres
```

## Core Architecture

### Pipeline Architecture

The application uses a pipeline of BlockingQueues to process data through stages:

1. **WebSocket Reader** → `websocketDbQueue`
2. **Preprocessor** → `preprocessedWebsocketDbQueue`
3. **Converter** → `klineDataDbBlockingQueue` + `klineCandleQueue`
4. **Database Writer** (persists to ClickHouse)
5. **Candle Processor** → `orderQueue`
6. **Order Updater** (persists to PostgreSQL)

See `BybitProcessFactoryImpl` (trading-app/src/main/java/org/example/BybitProcessFactoryImpl.java) for pipeline setup.

### State Machine Architecture

The system uses two coordinated state machines:

**FibaProcessor State Machine** (`trading-logic/src/main/java/org/example/processor/fiba/impl/FibaProcessorImpl.java`):
- Tracks Fibonacci level calculation state based on hour candles
- States: NO_HOUR_CANDLES → ONE_HOUR_CANDLE → MORE_THAN_ONE_HOUR_CANDLE → CLEAN_UP_FIBA_DATA
- Each state has an implementation in `trading-logic/src/main/java/org/example/processor/fiba/state/impl/`

**CandleProcessor State Machine** (`trading-logic/src/main/java/org/example/processor/candle/impl/CandleProcessorImpl.java`):
- Manages order lifecycle based on candle patterns and Fibonacci levels
- States: WAITING_FOR_TWO_CANDLES → ORDER_CREATED → ORDER_AMENDED → ORDER_FREEZED → ORDER_FILLED_TAKE_PROFIT/ORDER_FILLED_STOP_LOSS
- Each state has an implementation in `trading-logic/src/main/java/org/example/processor/candle/state/impl/`

Both state machines enforce strict state transition rules and will throw `StateMachineIllegalStateException` for invalid transitions.

### Key Data Models

- `KlineCandle` - Represents a candlestick with OHLCV data (common module)
- `FibaCandlesData` - Stores accumulated hour candles and calculated Fibonacci levels
- `OrdersData` - Tracks current order state and parameters
- `CandleEnvironment` / `FibaEnvironment` - Context objects passed to state machines

## Key Configuration

### Environment Variables

All configuration is loaded from `.env` file:
- Database connections: PostgreSQL (port 5435), ClickHouse (port 8125)
- Bybit API credentials (testnet): BYBIT_API_KEY, BYBIT_API_SECRET
- Profile selection: PROD vs TEST mode

### Process Factory Settings

Trading parameters are configured in `Main.java` via `ProcessFactorySettings`:
- `QUANTITY_THRESHOLD` - Minimum order quantity
- `INITIAL_BALANCE` - Starting balance for simulation
- `TICKER` - Trading pair (e.g., BTCUSDT)
- `TICKER_INTERVAL` - Candle interval (e.g., 1 minute)
- `PERCENT_TO_LOOSE` - Risk percentage for stop loss calculation
- `MAX_LEVERAGE` - Maximum leverage for orders

## Database Schema

### PostgreSQL (Trading Data)
- `orders` table - Stores all trading orders with state transitions
- Migrations in `postgres-flyway/src/main/resources/db/migration/`

### ClickHouse (Time Series Data)
- `universal_kline_candle` table - Stores kline/candle data for high-performance querying
- Configured for time-series optimization with partitioning
- Migrations in `clickhouse-flyway/src/main/resources/db/migration/`

### PostgreSQL (Auth - Legacy)
- User authentication tables for old-user-auth module
- Migrations in `postgres-auth-flyway/src/main/resources/db/migration/`

## Testing Strategy

- Tests use embedded PostgreSQL (`io.zonky.test:embedded-postgres`)
- Flyway migrations run automatically in test setup
- CSV test data available via `csv-processor` module
- Some integration tests require `trading-data-generator` for sample data

## Important Implementation Details

### Annotation Processing
Both `trading-app` and `trading-logic` modules use Lombok + MapStruct annotation processors. The `maven-compiler-plugin` configuration includes all three annotation processors in the correct order:
1. Lombok
2. lombok-mapstruct-binding
3. MapStruct processor

### Shaded JARs
`trading-app` and `trading-logic` use `maven-shade-plugin` to create fat JARs with all dependencies. Main class: `org.example.Main`

### Cold Start
The system supports "cold start" mode which fetches historical data before processing live WebSocket data. Configured via `DAYS_TO_RETREIVE_DATA` setting.

### WebSocket Testing
For local testing without Bybit connection, use `websocket-server-jakarta` module to simulate WebSocket data feed.

## Common Patterns

### State Machine Extension
When adding new states:
1. Create state implementation class extending `FibaState` or `CandleState`
2. Add enum value to `FibaProcessorState` or `CandleProcessorState`
3. Update state transition validation in processor's `updateState()` method
4. Implement `getNext()` method with transition logic

### Adding New Order Parameters
When modifying order structure:
1. Update `Order` model in common module
2. Update `OrderMapper` (MapStruct interface)
3. Update database migration in postgres-flyway
4. Regenerate MapStruct implementation: `mvn clean compile`

### Bybit API Integration
- API service implementations in `bybit-data-grabber` module
- WebSocket message models in `common` module under `model.bybit` package
- Use `BybitApiConfig` for endpoint URLs (testnet vs mainnet)
