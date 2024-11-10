import org.example.model.enums.FibaLevel;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.math.BigDecimal;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.example.model.enums.FibaLevel.FIVE;
import static org.example.model.enums.FibaLevel.ONE;
import static org.example.model.enums.FibaLevel.THREEEIGHTTWO;
import static org.example.processor.candle.utils.CandleProcessorHelper.calculateLeverageLong;

public class CandleProcessorHelperTest {
    @ParameterizedTest
    @CsvSource({
            "200,126",
            "50, 50",
            "126,126"
    })
    public void calculateLeverageLongBigMaxLeverageTest(int maxLeverage, BigDecimal expectedResult) {
        //given
        final int percentOfDepositToLoose = 60;
        final Map<FibaLevel, BigDecimal> levelPrice =
                Map.of(
                        THREEEIGHTTWO, new BigDecimal("61624.721"),
                        FIVE, new BigDecimal("61555.750"),
                        ONE, new BigDecimal("61263.500")
                );

        //when
        final BigDecimal actualResult =  calculateLeverageLong(percentOfDepositToLoose, maxLeverage, levelPrice);

        //then
        assertThat(actualResult).isEqualTo(expectedResult);
    }

}
