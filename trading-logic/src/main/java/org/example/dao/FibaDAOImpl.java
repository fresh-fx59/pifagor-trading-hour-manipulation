package org.example.dao;

import lombok.extern.slf4j.Slf4j;
import org.example.config.PostgresDataSourceConf;
import org.example.enums.Profile;
import org.example.model.FibaEntity;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

@Slf4j
public class FibaDAOImpl implements FibaDAO {
    private static final String fibaEntityTable = "fiba_entity";
    private final DataSource dataSource;
    private final Profile profile;

    public FibaDAOImpl(Profile profile) {
        this.profile = profile;
//        if (Profile.NO_DATASOURCE.equals(profile)) {
//            this.dataSource = null;
//        } else {
            this.dataSource = PostgresDataSourceConf.getPostgresDataSource(profile);
//        }
    }

    @Override
    public void save(FibaEntity fibaEntity) {
//        if (Profile.NO_DATASOURCE.equals(profile))
//            return;

        final String sql = String.format("""
                    INSERT INTO %s (
                        current_state, 
                        next_state, 
                        candles_count, 
                        fiba0, 
                        fiba0382, 
                        fiba05, 
                        fiba1, 
                        incoming_candle_open_at, 
                        incoming_candle_open, 
                        incoming_candle_high, 
                        incoming_candle_low, 
                        incoming_candle_close, 
                        incoming_candle_is_kline_closed, 
                        incoming_candle_close_at, 
                        incoming_candle_ticker_interval, 
                        incoming_candle_ticker, 
                        incoming_candle_event_time, 
                        incoming_candle_load_type, 
                        created_at, 
                        created_by
                    ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, NOW(), ?)
                """, fibaEntityTable);

        try (Connection connection = dataSource.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {

            preparedStatement.setString(1, fibaEntity.getCurrentState().toString());
            preparedStatement.setString(2, fibaEntity.getNextState().toString());
            preparedStatement.setInt(3, fibaEntity.getCandlesCount());
            preparedStatement.setBigDecimal(4, fibaEntity.getFiba0());
            preparedStatement.setBigDecimal(5, fibaEntity.getFiba0382());
            preparedStatement.setBigDecimal(6, fibaEntity.getFiba05());
            preparedStatement.setBigDecimal(7, fibaEntity.getFiba1());
            preparedStatement.setObject(8, fibaEntity.getIncomingCandleOpenAt());
            preparedStatement.setBigDecimal(9, fibaEntity.getIncomingCandleOpen());
            preparedStatement.setBigDecimal(10, fibaEntity.getIncomingCandleHigh());
            preparedStatement.setBigDecimal(11, fibaEntity.getIncomingCandleLow());
            preparedStatement.setBigDecimal(12, fibaEntity.getIncomingCandleClose());
            preparedStatement.setBoolean(13, fibaEntity.getIncomingCandleIsKlineClosed());
            preparedStatement.setObject(14, fibaEntity.getIncomingCandleCloseAt());
            preparedStatement.setString(15, fibaEntity.getIncomingCandleTickerInterval().toString());
            preparedStatement.setString(16, fibaEntity.getIncomingCandleTicker().toString());
            preparedStatement.setObject(17, fibaEntity.getIncomingCandleEventTime());
            preparedStatement.setString(18, fibaEntity.getIncomingCandleLoadType().toString());
            preparedStatement.setString(19, fibaEntity.getCreatedBy());

            int rowsInserted = preparedStatement.executeUpdate();
            connection.commit();
            if (rowsInserted > 0) {
                log.info("FibaEntity created successfully!");
            }
        } catch (SQLException e) {
            log.error("failed to save FibaEntity", e);
        }
    }
}
