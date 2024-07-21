package service;

import org.example.model.KlineCandle;
import org.example.model.Order;
import org.example.service.OrderServiceImpl;
import org.example.service.UniversalKlineCandleProcessorImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;

import static org.assertj.core.api.Assertions.assertThat;
import static org.example.CsvReader.getCandlesFromFile;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class KlineProcessorImplTest {
    private static final BigDecimal initialBalance = new BigDecimal("31000");
    private final static BigDecimal quantityThreshold = new BigDecimal("0.05");

    @Mock
    private OrderServiceImpl orderService;

    private final UniversalKlineCandleProcessorImpl universalKlineCandleProcessor =
            new UniversalKlineCandleProcessorImpl(new LinkedBlockingQueue<>(), initialBalance, quantityThreshold, orderService);



    @Test
    public void universalCandleProcessor() {
        //given
        String filePath = "src/test/resources/1720344410_klineCandles_1709251200000-1711929540000.csv";
        BigDecimal expectedResult = new BigDecimal("2342.227");
        List<KlineCandle> candlesToProcess = getCandlesFromFile(filePath);
        candlesToProcess.forEach(candle -> candle.setIsKlineClosed(true));
        MathContext mc = new MathContext(7, RoundingMode.DOWN);

        //when
        when(orderService.createOrder(any())).then(new Order());
        candlesToProcess.forEach(universalKlineCandleProcessor::processCandleData);
        BigDecimal actualResult = universalKlineCandleProcessor.getBalance()
                .subtract(initialBalance, mc);

        //then
        assertThat(actualResult).isEqualTo(expectedResult);
    }

}
