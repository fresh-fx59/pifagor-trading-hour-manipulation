**Description**
This project is intended to use one hour manipulation strategy to place and cancel orders on crypto exchange in automatic mode.

**Strategy Logic**
Symplified logic looks as follows:
- we are looking for two candles one after another with 2 conditions met
    - the lowest point of the second candle is upper than 50% fiba level of the first candle
    - the highest point of the 2nd candle is above the highest point of the 1st candle
- after that we place limit order with StopLoss at fiba level 1,  TakeProfit at 0.382 and limit price fiba 0.5
- if next candle(s) exceed the highest point, than we move limit order price and TakeProfit level to new values according to new fiba levels. StopLoss remains the same
- when the price touching the 0.5 fiba level our limit order should be filled, than we are waiting for the 0.382 or 1 fiba level reached
- after the order is filled by StopLoss or TakeProfit price we resume following the price for searching of entering points

**Integration Description**

The application is written in Java programming language. It receives data via websocket exchange connection, it places orders via REST requests to exchange, it stores received candles in Clickhouse DB. History is logged and could be found in log files.

**Caveats**
- websocket connection could be closed at any time so we should reconnect immediately
- we could miss some *seconds* candle while reconnecting, so it is important to check data consistency
- before placing new order we should check that previous order is filled. For that purpose we should save orders data to DB

**Prerequisites**
You should have Bybit test account https://testnet.bybit.com/en and API enabled. To enable API you should enable two-factor authentification first.

**For launch**
Set enviroment variables
_BYBIT_API_KEY_=
_BYBIT_API_SECRET_=
_BYBIT_API_URL_=https://api-testnet.bybit.com - used to place orders

By default, the app connects to ByBit testnet.

Flow Chart
https://drive.google.com/file/d/1DsGdlFlUIqehI0Sz7yMcLbCjXnku1Awi/view?usp=sharing

Multiple docker files in project
https://stackoverflow.com/questions/27409761/docker-multiple-dockerfiles-in-project?rq=2

Multi module project Dockerfile
https://stackoverflow.com/questions/51679363/multi-module-maven-project-on-dockers

