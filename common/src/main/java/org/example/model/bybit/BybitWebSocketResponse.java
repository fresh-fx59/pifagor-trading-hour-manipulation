package org.example.model.bybit;

import com.fasterxml.jackson.annotation.JsonCreator;
import lombok.Builder;
import org.example.enums.LoadType;
import org.example.enums.Ticker;
import org.example.enums.TickerInterval;

import java.util.List;

import static org.example.enums.TickerInterval.getTickerIntervalFromBybitValue;

@Builder
public record BybitWebSocketResponse<T> (
        String topic,
        List<T> data,
        Long ts,
        String type,
        String ret_msg,
        String op,
        String success,
        String conn_id,
        String req_id,
        // not from websocket parameter
        LoadType loadType
) {
    @JsonCreator
    public BybitWebSocketResponse(String topic, List<T> data, Long ts, String type, String ret_msg, String op, String success, String conn_id, String req_id, LoadType loadType) {
        this.topic = topic;
        this.data = data;
        this.ts = ts;
        this.type = type;
        this.ret_msg = ret_msg;
        this.op = op;
        this.success = success;
        this.conn_id = conn_id;
        this.req_id = req_id;
        this.loadType = loadType == null ? LoadType.WEBSOCKET : loadType;
    }

    public BybitWebSocketResponse(String topic, List<T> data, Long timestamp, LoadType loadType) {
        this(topic, data, timestamp, null, null, null, null, null, null, loadType);
    }

    public BybitWebSocketResponse<T> copy(
            List<T> data,
            LoadType loadType
    ) {
        return new BybitWebSocketResponse<>(
                this.topic,
                data != null ? data : this.data,
                this.ts,
                this.type,
                this.ret_msg,
                this.op,
                this.success,
                this.conn_id,
                this.req_id,
                loadType != null ? loadType : this.loadType
        );
    }

    public Ticker getTicker() {
        String[] splittedTopic = topic.split("\\.");
        return Ticker.getTickerFromBybitValue(splittedTopic[2]);
    }

    public TickerInterval getTickerInterval() {
        String[] splittedTopic = topic().split("\\.");
        return getTickerIntervalFromBybitValue(splittedTopic[1]);
    }
}
