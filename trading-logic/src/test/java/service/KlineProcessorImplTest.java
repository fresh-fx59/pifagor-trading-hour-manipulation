package service;

import org.example.dao.FibaDAOImpl;
import org.example.enums.Profile;
import org.example.model.KlineCandle;
import org.example.service.OrderServiceImpl;
import org.example.service.UniversalKlineCandleProcessorImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
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
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class KlineProcessorImplTest {
    private final BigDecimal initialBalance = new BigDecimal("31000");
    private final BigDecimal quantityThreshold = new BigDecimal("0.05");
    private final LinkedBlockingQueue<KlineCandle> linkedBlockingQueue = new LinkedBlockingQueue<>();

    @Mock
    private OrderServiceImpl orderService;
    @Mock
    private FibaDAOImpl fibaDAO;

    private UniversalKlineCandleProcessorImpl universalKlineCandleProcessor;

    @BeforeEach
    public void init() {
        universalKlineCandleProcessor = new UniversalKlineCandleProcessorImpl(linkedBlockingQueue, initialBalance, quantityThreshold, orderService, Profile.PROD, fibaDAO);
    }

    @ParameterizedTest
    @CsvSource({
            "src/test/resources/1720344410_klineCandles_1709251200000-1711929540000.csv,1610.631",
            "src/test/resources/1722895422_klineCandles_1691257112000-1722879512000.csv,7525.294"
    })
    public void universalCandleProcessor(String filePath, String expectedResultString) {
        //given
        final BigDecimal expectedResult = new BigDecimal(expectedResultString);
        final List<KlineCandle> candlesToProcess = getCandlesFromFile(filePath);
        candlesToProcess.forEach(candle -> candle.setIsKlineClosed(true));
        final MathContext mc = new MathContext(7, RoundingMode.DOWN);

        //when
        doNothing().when(fibaDAO).save(any());
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
