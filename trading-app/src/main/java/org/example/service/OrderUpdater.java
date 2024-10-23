package org.example.service;

import lombok.extern.slf4j.Slf4j;
import org.example.config.ConfigPostgresDataSource;
import org.example.enums.OrderStatus;
import org.example.enums.Profile;
import org.example.model.OrderForQueue;

import javax.sql.DataSource;
import java.sql.*;
import java.time.LocalDate;
import java.util.Optional;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;

@Slf4j
public class OrderUpdater implements Runnable, AutoCloseable {
    private volatile AtomicBoolean shouldStop = new AtomicBoolean();
    private static final String tableName = "order_flow";

    private final BlockingQueue<OrderForQueue> orderQueue;
    private final DataSource dataSource;

    public OrderUpdater(BlockingQueue<OrderForQueue> orderQueue, Profile profile) {
        this.orderQueue = orderQueue;
        dataSource = ConfigPostgresDataSource.getPostgresDataSource(profile);
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
                case NEW -> saveOrder(orderForQueue);
                case UPDATED, CANCELLED -> updateOrder(orderForQueue);
                default -> log.error("Can't process order {} with status {} to DB",
                        orderForQueue.getOrder().getOrderId(), orderStatus);
            }
        }
    }

    private void saveOrder(OrderForQueue orderForQueue) {
        try (Connection conn = dataSource.getConnection();
             PreparedStatement createStatement = getCreateStatement(conn)) {
            fillInValuesOnCreate(createStatement, orderForQueue);
            createStatement.executeUpdate();
            conn.commit();
            log.info("Order id {} was saved successfully", orderForQueue.getOrder().getOrderId());
        } catch (SQLException e) {
            log.error("Failed to save order", e);
        }
    }

    private void fillInValuesOnCreate(PreparedStatement statement, OrderForQueue order) throws SQLException {
        statement.setString(1, order.getOrder().getCategory().toString());
        statement.setString(2, order.getOrder().getTicker().toString());
        statement.setString(3, order.getOrder().getOrderSide().toString());
        statement.setString(4, order.getOrder().getType().toString());
        statement.setString(5, order.getOrder().getQuantity());
        statement.setString(6, order.getOrder().getPrice());
        statement.setString(7, order.getOrder().getOrderId());
        statement.setString(8, order.getOrder().getCustomOrderId());
        statement.setString(9, order.getOrder().getStopLoss());
        statement.setString(10, order.getOrder().getTakeProfit());
        statement.setString(11, order.getStatus().toString());
        statement.setString(12, order.getErrorMessage());
        statement.setString(13, order.getCreatedBy());
    }

    private PreparedStatement getCreateStatement(Connection conn) throws SQLException {
        final String sql = String.format("""
                    INSERT INTO %s (
                    category,
                    ticker,
                    order_side,
                    type,
                    quantity,
                    price,
                    order_id,
                    custom_order_id,
                    stopLoss,
                    take_profit,
                    status,
                    error_message,
                    created_by
                    ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                    """, tableName);
        return conn.prepareStatement(sql);
    }

    private void updateOrder(OrderForQueue orderForQueue) {
        Optional<Long> orderId = getOrderByOrderId(orderForQueue);

        if (orderId.isPresent()) {
            try (Connection conn = dataSource.getConnection();
                 PreparedStatement createStatement = getUpdateStatement(conn)) {
                fillInValuesOnUpdate(createStatement, orderForQueue, orderId.get());
                createStatement.execute();
                conn.commit();
                log.info("Order id {} was updated successfully", orderForQueue.getOrder().getOrderId());
            } catch (SQLException e) {
                log.error("Failed to update order", e);
            }
        } else {
            log.error("Order id {} wasn't updated. Some error to find order by order_id.", orderForQueue.getOrder().getOrderId());
        }
    }

    private Optional<Long> getOrderByOrderId(OrderForQueue orderForQueue) {
        try (Connection conn = dataSource.getConnection();
             PreparedStatement findByOrderIdStatement = getFindByOrderIdStatement(conn)) {
            findByOrderIdStatement.setString(1, orderForQueue.getOrder().getOrderId());
            ResultSet rs = findByOrderIdStatement.executeQuery();
            if (rs.next())
                return Optional.of(rs.getLong("id"));
        } catch (SQLException e) {
            log.error("Failed to get order by its id", e);
        }

        return Optional.empty();
    }

    private PreparedStatement getFindByOrderIdStatement(Connection conn) throws SQLException {
        final String sql = String.format("""
                SELECT *
                FROM %s
                WHERE order_id=?
                """, tableName);
        return conn.prepareStatement(sql);
    }

    private void fillInValuesOnUpdate(PreparedStatement statement, OrderForQueue order, Long id) throws SQLException {
        fillInValuesOnCreate(statement, order);
        statement.setDate(14, Date.valueOf(LocalDate.now()));
        statement.setLong(15, id);

    }

    private PreparedStatement getUpdateStatement(Connection conn) throws SQLException {
        final String sql = String.format("""
                UPDATE %s SET
                            category=?,
                            ticker=?,
                            order_side=?,
                            type=?,
                            quantity=?,
                            price=?,
                            order_id=?,
                            custom_order_id=?,
                            stopLoss=?,
                            take_profit=?,
                            status=?,
                            error_message=?,
                            created_by=?,
                            updated_at=?
                            WHERE id=?
                    """, tableName);
        return conn.prepareStatement(sql);
    }

    @Override
    public void close() throws Exception {
        shouldStop.set(true);
    }
}
