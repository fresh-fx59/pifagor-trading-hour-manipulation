package org.example.mapper;

import com.bybit.api.client.domain.CategoryType;
import com.bybit.api.client.domain.TradeOrderType;
import com.bybit.api.client.domain.trade.Side;
import com.bybit.api.client.domain.trade.request.TradeOrderRequest;
import org.example.enums.OrderCategory;
import org.example.enums.OrderSide;
import org.example.enums.OrderType;
import org.example.enums.Ticker;
import org.example.model.Order;
import org.jetbrains.annotations.NotNull;
import org.mapstruct.*;

import java.util.Arrays;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE)
public abstract class OrderMapper {
    @Mapping(target = "category", source = "category", qualifiedByName = "convertOrderCategoryToCategoryType")
    @Mapping(target = "symbol", source = "ticker", qualifiedByName = "convertTickerToSymbol")
    @Mapping(target = "side", source = "orderSide", qualifiedByName = "convertOrderSideToSide")
    @Mapping(target = "orderType", source = "type", qualifiedByName = "convertOrderTypeToTradeOrderType")
    @Mapping(target = "qty", source = "quantity")
    @Mapping(target = "orderLinkId", source = "customOrderId")
    public abstract TradeOrderRequest toTradeOrderRequest(Order order);

    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "category", source = "category", qualifiedByName = "convertOrderCategoryToCategoryType")
    @Mapping(target = "symbol", source = "ticker", qualifiedByName = "convertTickerToSymbol")
    @Mapping(target = "orderId", source = "orderId")
    @Mapping(target = "price", source = "price")
    @Mapping(target = "takeProfit", source = "takeProfit")
    public abstract TradeOrderRequest toTradeOrderRequestAmend(Order order);

    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "category", source = "category", qualifiedByName = "convertOrderCategoryToCategoryType")
    @Mapping(target = "symbol", source = "ticker", qualifiedByName = "convertTickerToSymbol")
    @Mapping(target = "orderId", source = "orderId")
    public abstract TradeOrderRequest toTradeOrderRequestCancel(Order order);

    @Named("convertOrderCategoryToCategoryType")
    protected CategoryType convertOrderCategoryToCategoryType(@NotNull OrderCategory inboundCategory) {
        return Arrays.stream(CategoryType.values())
                .filter(category -> category.getCategoryTypeId().equals(inboundCategory.getBybitValue()))
                .findAny()
                .orElseThrow();
    }

    @Named("convertTickerToSymbol")
    protected String convertTickerToSymbol(@NotNull Ticker ticker) {
        return ticker.getBybitValue();
    }

    @Named("convertOrderSideToSide")
    protected Side convertOrderSideToSide(@NotNull OrderSide orderSide) {
        return Arrays.stream(Side.values())
                .filter(side -> side.getTransactionSide().equals(orderSide.getBybitValue()))
                .findAny()
                .orElseThrow();
    }

    @Named("convertOrderTypeToTradeOrderType")
    protected TradeOrderType convertOrderTypeToTradeOrderType(@NotNull OrderType orderType) {
        return Arrays.stream(TradeOrderType.values())
                .filter(tradeOrderType -> tradeOrderType.getOType().equals(orderType.getBybitValue()))
                .findAny()
                .orElseThrow();
    }
}
