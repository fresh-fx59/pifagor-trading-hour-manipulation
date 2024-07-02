package service;

import org.example.model.KlineCandle;
import org.example.service.KlineCandleProcessorImpl;
import org.example.service.UniversalKlineCandleProcessorImpl;
import org.example.utils.FibaHelper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.example.CsvReader.getCandlesFromFile;

@ExtendWith(MockitoExtension.class)
public class KlineProcessorImplTest {
    @Spy
    private final FibaHelper fibaHelper = new FibaHelper();
    @Spy
    private final List<KlineCandle> importantCandles = new ArrayList<>();
    @InjectMocks
    private KlineCandleProcessorImpl klineCandleProcessor;
    @InjectMocks
    private UniversalKlineCandleProcessorImpl universalKlineCandleProcessor;

    @Test
    void processCandleDataNoCandlesTest() {
        //given
        KlineCandle candle = new KlineCandle(
                LocalDateTime.of(2024, 5, 30, 0, 0, 0),
                "BTCUSD",
                "1",
                new BigDecimal("1200"),
                new BigDecimal("2000"),
                new BigDecimal("1000"),
                new BigDecimal("1800")
        );

        //when
        klineCandleProcessor.processCandleData(candle);

        //then
//        assertThat(klineCandleProcessor.getImportantCandlesCount()).isEqualTo(1);
//        verify(importantCandles).add(any());
    }

    @Test
    public void oneMinuteCandleProcessor() {
        //given
        String filePath = "src/test/resources/1719869337_klineCandles_1709240400000-1711918740000.csv";
        BigDecimal expectedResult = new BigDecimal("1869.5610");
        List<KlineCandle> candlesToProcess = getCandlesFromFile(filePath);

        //when
        candlesToProcess.forEach(klineCandleProcessor::processCandleData);
        BigDecimal actualResult = klineCandleProcessor.getBalance();

        //then
        assertThat(actualResult).isEqualTo(expectedResult);
    }

    @Test
    public void universalCandleProcessor() {
        //given
        String filePath = "src/test/resources/1719869337_klineCandles_1709240400000-1711918740000.csv";
        BigDecimal expectedResult = new BigDecimal("1869.5610");
        List<KlineCandle> candlesToProcess = getCandlesFromFile(filePath);
        candlesToProcess.forEach(candle -> candle.setIsKlineClosed(true));

        //when
        candlesToProcess.forEach(universalKlineCandleProcessor::processCandleData);
        BigDecimal actualResult = universalKlineCandleProcessor.getBalance();

        //then
        assertThat(actualResult).isEqualTo(expectedResult);
    }

}
