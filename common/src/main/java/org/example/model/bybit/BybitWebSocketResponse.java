package org.example.model.bybit;

import java.util.List;

public record BybitWebSocketResponse<T> (
        String topic,
        List<T> data,
        Long ts,
        String type
) {
}
