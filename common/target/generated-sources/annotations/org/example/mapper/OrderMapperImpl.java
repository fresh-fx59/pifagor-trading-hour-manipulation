package org.example.mapper;

import com.bybit.api.client.domain.trade.request.TradeOrderRequest;
import javax.annotation.processing.Generated;
import org.example.model.Order;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2024-07-08T00:29:36+0300",
    comments = "version: 1.5.3.Final, compiler: javac, environment: Java 17.0.11 (Eclipse Adoptium)"
)
public class OrderMapperImpl extends OrderMapper {

    @Override
    public TradeOrderRequest toTradeOrderRequest(Order order) {
        if ( order == null ) {
            return null;
        }

        TradeOrderRequest.TradeOrderRequestBuilder tradeOrderRequest = TradeOrderRequest.builder();

        tradeOrderRequest.category( convertOrderCategoryToCategoryType( order.getCategory() ) );
        tradeOrderRequest.symbol( convertTickerToSymbol( order.getTicker() ) );
        tradeOrderRequest.side( convertOrderSideToSide( order.getOrderSide() ) );
        tradeOrderRequest.orderType( convertOrderTypeToTradeOrderType( order.getType() ) );
        tradeOrderRequest.qty( order.getQuantity() );
        tradeOrderRequest.orderLinkId( order.getCustomOrderId() );
        tradeOrderRequest.price( order.getPrice() );
        tradeOrderRequest.orderId( order.getOrderId() );
        tradeOrderRequest.takeProfit( order.getTakeProfit() );
        tradeOrderRequest.stopLoss( order.getStopLoss() );

        return tradeOrderRequest.build();
    }

    @Override
    public TradeOrderRequest toTradeOrderRequestAmend(Order order) {
        if ( order == null ) {
            return null;
        }

        TradeOrderRequest.TradeOrderRequestBuilder tradeOrderRequest = TradeOrderRequest.builder();

        tradeOrderRequest.category( convertOrderCategoryToCategoryType( order.getCategory() ) );
        tradeOrderRequest.symbol( convertTickerToSymbol( order.getTicker() ) );
        tradeOrderRequest.orderId( order.getOrderId() );
        tradeOrderRequest.price( order.getPrice() );
        tradeOrderRequest.takeProfit( order.getTakeProfit() );

        return tradeOrderRequest.build();
    }

    @Override
    public TradeOrderRequest toTradeOrderRequestCancel(Order order) {
        if ( order == null ) {
            return null;
        }

        TradeOrderRequest.TradeOrderRequestBuilder tradeOrderRequest = TradeOrderRequest.builder();

        tradeOrderRequest.category( convertOrderCategoryToCategoryType( order.getCategory() ) );
        tradeOrderRequest.symbol( convertTickerToSymbol( order.getTicker() ) );
        tradeOrderRequest.orderId( order.getOrderId() );

        return tradeOrderRequest.build();
    }
}
