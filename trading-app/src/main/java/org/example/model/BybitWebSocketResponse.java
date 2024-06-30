package org.example.model;

import java.util.List;

public record BybitWebSocketResponse<T> (
        String topic,
        List<T> data,
        Long ts,
        String type
) {
}
