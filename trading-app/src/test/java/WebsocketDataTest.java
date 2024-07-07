import com.bybit.api.client.domain.websocket_message.public_channel.KlineData;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import org.example.model.bybit.BybitWebSocketResponse;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.example.mapper.JsonMapper.getMapper;

public class WebsocketDataTest {
    @Test
    public void convertResponseToClassTest() throws JsonProcessingException {
        //given
        String message = """
                {"topic":"kline.1.BTCUSDT","data":[{"start":1719667560000,"end":1719667619999,"interval":"1","open":"61030","close":"61029.9","high":"61030","low":"61015","volume":"10.798","turnover":"658980.4245","confirm":false,"timestamp":1719667601380}],"ts":1719667601380,"type":"snapshot"}
                """;
        KlineData klineData = new KlineData();
        klineData.setStart(1719667560000L);
        klineData.setEnd(1719667619999L);
        klineData.setInterval("1");
        klineData.setOpen("61030");
        klineData.setClose("61029.9");
        klineData.setHigh("61030");
        klineData.setLow("61015");
        klineData.setVolume("10.798");
        klineData.setTurnover("658980.4245");
        klineData.setConfirm(false);
        klineData.setTimestamp(1719667601380L);
        BybitWebSocketResponse<KlineData> expectedResponse = new BybitWebSocketResponse<>("kline.1.BTCUSDT",
                List.of(klineData), 1719667601380L, "snapshot");

        //when
        BybitWebSocketResponse<KlineData> actualResponse = getMapper().readValue(message, new TypeReference<>(){});

        //then
        assertThat(actualResponse).isEqualTo(expectedResponse);
    }
}
