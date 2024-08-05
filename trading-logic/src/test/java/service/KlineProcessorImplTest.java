package service;

import org.example.model.KlineCandle;
import org.example.service.OrderServiceImpl;
import org.example.service.UniversalKlineCandleProcessorImpl;
import org.junit.jupiter.api.BeforeEach;
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
import static org.example.enums.OrderStatus.FILLED;
import static org.mockito.AdditionalAnswers.returnsFirstArg;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class KlineProcessorImplTest {
    private final BigDecimal initialBalance = new BigDecimal("31000");
    private final BigDecimal quantityThreshold = new BigDecimal("0.05");
    private final LinkedBlockingQueue<KlineCandle> linkedBlockingQueue = new LinkedBlockingQueue<>();

    @Mock
    private OrderServiceImpl orderService;

    private UniversalKlineCandleProcessorImpl universalKlineCandleProcessor;


    @BeforeEach
    public void init() {
        universalKlineCandleProcessor = new UniversalKlineCandleProcessorImpl(linkedBlockingQueue, initialBalance, quantityThreshold, orderService, true);
    }

    @Test
    public void universalCandleProcessor() {
        //given
        String filePath = "src/test/resources/1720344410_klineCandles_1709251200000-1711929540000.csv";
        BigDecimal expectedResult = new BigDecimal("2342.304");
        List<KlineCandle> candlesToProcess = getCandlesFromFile(filePath);
        candlesToProcess.forEach(candle -> candle.setIsKlineClosed(true));
        MathContext mc = new MathContext(7, RoundingMode.DOWN);

        //when
        when(orderService.createOrder(any())).then(returnsFirstArg());
        when(orderService.amendOrder(any())).then(returnsFirstArg());
        doReturn(FILLED).when(orderService).getOrderStatus(any());
        candlesToProcess.forEach(universalKlineCandleProcessor::processCandleData);
        BigDecimal actualResult = universalKlineCandleProcessor.getBalance()
                .subtract(initialBalance, mc);

        //then
        assertThat(actualResult).isEqualTo(expectedResult);
    }

    @Test
    public void universalCandleProcessorCustomFile() {
        //given
        String filePath = "src/test/resources/universal_kline_candle_1721433600-1721519940-closed-minutes-1721596603823.csv";
        BigDecimal expectedResult = new BigDecimal("0");
        List<KlineCandle> candlesToProcess = getCandlesFromFile(filePath);
        candlesToProcess.forEach(candle -> candle.setIsKlineClosed(true));
        MathContext mc = new MathContext(7, RoundingMode.DOWN);

        //when
        when(orderService.createOrder(any())).then(returnsFirstArg());
        when(orderService.amendOrder(any())).then(returnsFirstArg());
        doReturn(FILLED).when(orderService).getOrderStatus(any());
        candlesToProcess.forEach(universalKlineCandleProcessor::processCandleData);
        BigDecimal actualResult = universalKlineCandleProcessor.getBalance()
                .subtract(initialBalance, mc);

        //then
        assertThat(actualResult).isEqualTo(expectedResult);
    }

}
