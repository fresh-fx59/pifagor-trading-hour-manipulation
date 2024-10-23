package service;

import io.zonky.test.db.postgres.embedded.EmbeddedPostgres;
import lombok.extern.slf4j.Slf4j;
import org.example.enums.*;
import org.example.model.Order;
import org.example.model.OrderForQueue;
import org.example.service.OrderUpdater;
import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Map;
import java.util.concurrent.LinkedBlockingQueue;

import static org.example.config.ConfigPostgresDataSource.getPostgresDataSource;
import static org.example.enums.Profile.TEST;

@Slf4j
public class OrderUpdaterTest {
    private EmbeddedPostgres postgres;
    private DataSource dataSource;
    LinkedBlockingQueue<OrderForQueue> queue = new LinkedBlockingQueue<>();

    @BeforeEach
    public void setUp() throws Exception {
        // Start the embedded PostgreSQL server
        postgres = EmbeddedPostgres.builder().setPort(50650).start();

        // Get DataSource from the embedded PostgreSQL instance
        dataSource = getPostgresDataSource(TEST);

        // Configure Flyway to use this DataSource and apply migrations
        Flyway flyway = Flyway.configure()
                .dataSource(dataSource)
                .placeholders(Map.of("mytimezone", "UTC", "schemaName", "public"))
                .locations("classpath:db/migration") // Path to migration scripts
                .load();

        System.out.println("Flyway version " + flyway.info());

        // Start the migration process
        flyway.migrate();
    }

    @AfterEach
    public void tearDown() throws Exception {
        // Stop the embedded PostgreSQL server
        if (postgres != null) {
            postgres.close();
        }
    }

    @Test
    public void testConnection() throws Exception {
        try (Connection connection = dataSource.getConnection();
             Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery("SELECT 1")) {

            Assertions.assertTrue(resultSet.next());
            Assertions.assertEquals(1, resultSet.getInt(1));
        }
    }

    @Test
    public void createOrder() throws Exception {
        // given
        final String orderId = "12345";
        final Order order = Order.builder()
                .category(OrderCategory.SPOT)
                .ticker(Ticker.BTCUSDT)  // Assuming Ticker has a constructor that takes a string.
                .orderSide(OrderSide.BUY)
                .type(OrderType.MARKET)
                .quantity("10")
                .price("150.00")
                .orderId(orderId)
                .customOrderId("custom-001")
                .stopLoss("145.00")
                .takeProfit("155.00")
                .build();
        final OrderForQueue orderForQueue = new OrderForQueue(order);
        orderForQueue.setId(1L);
        orderForQueue.setCreatedBy("me");
        orderForQueue.setStatus(OrderStatus.NEW);
        final String sql = "SELECT * FROM order_flow";

        // when
        Thread runnableThread = new Thread(new OrderUpdater(queue, TEST));
        runnableThread.start();

        queue.put(orderForQueue);

        while (!queue.isEmpty()) {
            Thread.sleep(100);
            log.info("waiting for thread to be empty...");
        }

        runnableThread.interrupt();

        while (!runnableThread.isInterrupted()) {
            Thread.sleep(100);
            log.info("waiting for thread to be interrupted...");
        }

        // then
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            ResultSet resultSet = statement.executeQuery();
            Assertions.assertTrue(resultSet.next());
            Assertions.assertEquals(1, resultSet.getInt(1));
            Assertions.assertEquals(orderId, resultSet.getString("order_id"));
        }
    }

    @Test
    public void updateOrder() throws Exception {
        // given
        final String orderId = "12345";
        final Order orderForCreate = Order.builder()
                .category(OrderCategory.SPOT)
                .ticker(Ticker.BTCUSDT)  // Assuming Ticker has a constructor that takes a string.
                .orderSide(OrderSide.BUY)
                .type(OrderType.MARKET)
                .quantity("10")
                .price("150.00")
                .orderId(orderId)
                .customOrderId("custom-001")
                .stopLoss("145.00")
                .takeProfit("155.00")
                .build();
        final OrderForQueue orderForCreateForQueue = new OrderForQueue(orderForCreate);
        orderForCreateForQueue.setCreatedBy("me");
        orderForCreateForQueue.setStatus(OrderStatus.NEW);

        final String newQuantity = "11";
        final Order orderForUpdate = Order.builder()
                .category(OrderCategory.SPOT)
                .ticker(Ticker.BTCUSDT)  // Assuming Ticker has a constructor that takes a string.
                .orderSide(OrderSide.BUY)
                .type(OrderType.MARKET)
                .quantity(newQuantity)
                .price("150.00")
                .orderId(orderId)
                .customOrderId("custom-001")
                .stopLoss("145.00")
                .takeProfit("155.00")
                .build();
        final OrderForQueue orderForUpdateForQueue = new OrderForQueue(orderForUpdate);
        orderForUpdateForQueue.setCreatedBy("me");
        orderForUpdateForQueue.setStatus(OrderStatus.UPDATED);

        final String sql = "SELECT * FROM order_flow";

        // when
        Thread runnableThread = new Thread(new OrderUpdater(queue, TEST));
        runnableThread.start();

        queue.put(orderForCreateForQueue);
        queue.put(orderForUpdateForQueue);

        while (!queue.isEmpty()) {
            Thread.sleep(100);
            log.info("waiting for thread to be empty...");
        }

        runnableThread.interrupt();

        while (!runnableThread.isInterrupted()) {
            Thread.sleep(100);
            log.info("waiting for thread to be interrupted...");
        }

        // then
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            ResultSet resultSet = statement.executeQuery();
            Assertions.assertTrue(resultSet.next());
            Assertions.assertEquals(1, resultSet.getInt(1));
            Assertions.assertEquals(orderId, resultSet.getString("order_id"));
            Assertions.assertEquals(newQuantity, resultSet.getString("quantity"));
        }
    }
}
