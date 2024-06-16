### API get index price kline data

GET https://api.bybit.com/v5/market/index-price-kline?symbol=BTCUSDZ22&start=1670608800000&interval=1&end=1670608740000&category=inverse&limit=2

candleTime open high low close

```json
{
  "retCode": 0,
  "retMsg": "OK",
  "result": {
    "symbol": "BTCUSDZ22",
    "category": "inverse",
    "list": [
      [
        "1670608800000",
        "17167",
        "17167",
        "17161.9",
        "17163.07"
      ],
      [
        "1670608740000",
        "17166.54",
        "17167.69",
        "17165.42",
        "17167"
      ]
    ]
  },
  "retExtInfo": {},
  "time": 1718545801213
}
```