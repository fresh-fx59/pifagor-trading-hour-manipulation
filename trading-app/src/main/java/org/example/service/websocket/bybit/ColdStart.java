package org.example.service.websocket.bybit;

import com.bybit.api.client.domain.websocket_message.public_channel.KlineData;
import org.example.model.bybit.BybitWebSocketResponse;

public interface ColdStart {
    BybitWebSocketResponse<KlineData> getData();
}
