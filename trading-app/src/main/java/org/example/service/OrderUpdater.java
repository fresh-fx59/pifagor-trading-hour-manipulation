package org.example.service;

import lombok.extern.slf4j.Slf4j;
import org.example.dao.OrderDAO;
import org.example.enums.OrderStatus;
import org.example.model.OrderForQueue;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;

@Slf4j
public class OrderUpdater implements Runnable, AutoCloseable {
    private final AtomicBoolean shouldStop = new AtomicBoolean();

    private final BlockingQueue<OrderForQueue> orderQueue;
    private final OrderDAO orderDAO;

    public OrderUpdater(BlockingQueue<OrderForQueue> orderQueue,
                        OrderDAO orderDAO) {
        this.orderQueue = orderQueue;
        this.orderDAO = orderDAO;
    }

    @Override
    public void run() {
        try {
            log.info("Starting OrderUpdaterImpl...");
            shouldStop.set(false);
            processOrderFlow();
        } catch (InterruptedException e) {
            log.error("Failed to processOrderFlow", e);
        }
    }

    private void processOrderFlow() throws InterruptedException {
        while (!shouldStop.get()) {
            final OrderForQueue orderForQueue = orderQueue.take();
            final OrderStatus orderStatus = orderForQueue.getStatus();

            //understand if it is update or create
            switch (orderStatus) {
                case NEW -> orderDAO.saveOrder(orderForQueue);
                case UPDATED, CANCELLED -> orderDAO.updateOrder(orderForQueue);
                default -> log.error("Can't process order {} with status {} to DB",
                        orderForQueue.getOrder().getOrderId(), orderStatus);
            }
        }
    }

    @Override
    public void close() throws Exception {
        shouldStop.set(true);
    }
}
