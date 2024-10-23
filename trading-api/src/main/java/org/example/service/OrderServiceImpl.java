package org.example.service;

import com.bybit.api.client.domain.trade.request.TradeOrderRequest;
import com.bybit.api.client.domain.trade.response.OrderResponse;
import com.fasterxml.jackson.core.type.TypeReference;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.config.MyBybitApiTradeRestClient;
import org.example.enums.OrderStatus;
import org.example.mapper.OrderMapper;
import org.example.model.Order;
import org.example.model.OrderForQueue;
import org.example.model.OrderManipulationResponseRecord;
import org.mapstruct.factory.Mappers;

import java.sql.Date;
import java.time.LocalDate;
import java.util.concurrent.BlockingQueue;

import static org.example.enums.OrderStatus.*;
import static org.example.mapper.JsonMapper.getMapper;

@RequiredArgsConstructor
@Slf4j
public class OrderServiceImpl implements OrderService {
    private final static String CREATED_BY = "OrderService";
    private final static String SUCCESS_RET_CODE = "0";
    private final OrderMapper orderMapper = Mappers.getMapper(OrderMapper.class);

    private final Boolean testModeEnabled;
    private final BlockingQueue<OrderForQueue> orderQueue;


    @Override
    public Order createOrder(Order order) {
        if (testModeEnabled)
            return order;

        TradeOrderRequest tradeOrderRequest = orderMapper.toTradeOrderRequest(order);
        Object orderResponseObject = MyBybitApiTradeRestClient.getBybitApiTradeRestClient().createOrder(tradeOrderRequest);

        processOrderResponse(orderResponseObject, order, ERROR_ON_CREATION, NEW);

        return order;
    }

    @Override
    public Order amendOrder(Order order) {
        if (testModeEnabled)
            return order;

        TradeOrderRequest tradeOrderRequest = orderMapper.toTradeOrderRequestAmend(order);
        Object orderResponseObject = MyBybitApiTradeRestClient.getBybitApiTradeRestClient().amendOrder(tradeOrderRequest);

        processOrderResponse(orderResponseObject, order, ERROR_ON_UPDATE, UPDATED);

        return order;
    }

    @Override
    public void cancelOrder(Order order) {
        if (testModeEnabled)
            return;

        TradeOrderRequest tradeOrderRequest = orderMapper.toTradeOrderRequestCancel(order);
        Object orderResponseObject = MyBybitApiTradeRestClient.getBybitApiTradeRestClient().cancelOrder(tradeOrderRequest);

        processOrderResponse(orderResponseObject, order, ERROR_ON_CANCELLATION, CANCELLED);
    }

    @Override
    public OrderStatus getOrderStatus(Order order) {
        return OrderStatus.FILLED;
    }

    private void processOrderResponse(
            Object orderResponseObject,
            Order order,
            OrderStatus errorStatus,
            OrderStatus successStatus
    ) {
        final OrderForQueue orderForQueue = new OrderForQueue(order);

        log.info("response received {}", orderResponseObject);
        OrderManipulationResponseRecord<OrderResponse> orderManipulationResponse =
                getMapper().convertValue(orderResponseObject, new TypeReference<>() {});
        if (SUCCESS_RET_CODE.equals(orderManipulationResponse.retCode())) {
            order.setOrderId(orderManipulationResponse.result().getOrderId());
            log.debug("order with system ID = {} processed", order.getOrderId());
        } else {
            orderForQueue.setErrorMessage(orderManipulationResponse.retMsg());
            log.error("Something went wrong processing the order: {} \n response {} \n order {}",
                    orderManipulationResponse.retMsg(),
                    orderManipulationResponse,
                    order);
        }

        if (orderForQueue.hasErrorMessage()) {
            orderForQueue.setStatus(errorStatus);
        } else {
            orderForQueue.setStatus(successStatus);
            orderForQueue.setUpdatedAt(Date.valueOf(LocalDate.now()));
        }

        orderForQueue.setCreatedBy(CREATED_BY);

        try {
            orderQueue.put(orderForQueue);
        } catch (Exception e) {
            log.error("failed to put orderForQueue in queue");
        }
    }
}
