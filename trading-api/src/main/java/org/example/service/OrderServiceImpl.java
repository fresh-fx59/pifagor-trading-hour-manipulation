package org.example.service;

import com.bybit.api.client.domain.trade.request.TradeOrderRequest;
import com.bybit.api.client.domain.trade.response.OrderResponse;
import com.fasterxml.jackson.core.type.TypeReference;
import lombok.extern.slf4j.Slf4j;
import org.example.config.MyBybitApiTradeRestClient;
import org.example.enums.OrderStatus;
import org.example.mapper.OrderMapper;
import org.example.model.Order;
import org.example.model.OrderManipulationResponseRecord;
import org.mapstruct.factory.Mappers;

import static org.example.mapper.JsonMapper.getMapper;

@Slf4j
public class OrderServiceImpl implements OrderService {
    private final static String SUCCESS_RET_CODE = "0";
    private final OrderMapper orderMapper = Mappers.getMapper(OrderMapper.class);


    @Override
    public Order createOrder(Order order) {
        TradeOrderRequest tradeOrderRequest = orderMapper.toTradeOrderRequest(order);
        Object orderResponseObject = MyBybitApiTradeRestClient.getBybitApiTradeRestClient().createOrder(tradeOrderRequest);

        processOrderResponse(orderResponseObject, order);

        return order;
    }

    @Override
    public Order amendOrder(Order order) {
        TradeOrderRequest tradeOrderRequest = orderMapper.toTradeOrderRequestAmend(order);
        Object orderResponseObject = MyBybitApiTradeRestClient.getBybitApiTradeRestClient().amendOrder(tradeOrderRequest);

        processOrderResponse(orderResponseObject, order);

        return order;
    }

    private void processOrderResponse(Object orderResponseObject, Order order) {
        log.info("response received {}", orderResponseObject);
        OrderManipulationResponseRecord<OrderResponse> orderManipulationResponse =
                getMapper().convertValue(orderResponseObject, new TypeReference<>() {});
        if (SUCCESS_RET_CODE.equals(orderManipulationResponse.retCode())) {
            order.setOrderId(orderManipulationResponse.result().getOrderId());
            log.debug("order with system ID = {} processed", order.getOrderId());
        } else {
            log.error("Something went wrong processing the order: {}", orderManipulationResponse.retMsg());
        }
    }

    @Override
    public void cancelOrder(Order order) {
        TradeOrderRequest tradeOrderRequest = orderMapper.toTradeOrderRequestCancel(order);
        Object orderResponseObject = MyBybitApiTradeRestClient.getBybitApiTradeRestClient().cancelOrder(tradeOrderRequest);

        processOrderResponse(orderResponseObject, order);
    }

    @Override
    public OrderStatus getOrderStatus(Order order) {
        return OrderStatus.FILLED;
    }
}
