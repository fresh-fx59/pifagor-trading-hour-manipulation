package service;

import org.example.model.enums.FibaLevel;
import org.example.utils.FibaHelper;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.example.model.enums.FibaLevel.*;

public class FibaServiceTest {
    private final FibaHelper fibaHelper = new FibaHelper();

    @Test
    void  calculateValueForLevelLowHighTest() {
        //given
        BigDecimal low = new BigDecimal("100");
        BigDecimal high = new BigDecimal("200");
        Map<FibaLevel, BigDecimal> expectedResult = new LinkedHashMap<>(){
            {
                put(ZERO, new BigDecimal("200"));
//                put(TWOTHREESIX, new BigDecimal("176.400"));
                put(THREEEIGHTTWO, new BigDecimal("161.800"));
                put(FIVE, new BigDecimal("150.0"));
//                put(SIXONEEIGHT, new BigDecimal("138.200"));
//                put(SEVENEIGHTSIX, new BigDecimal("121.400"));
                put(ONE, new BigDecimal("100"));
//                put(ONESIXONEEIGHT, new BigDecimal("38.200"));
//                put(TWO, new BigDecimal("0"));
            }};

        //when
        Map<FibaLevel, BigDecimal> actualResult = FibaHelper.calculateValueForLevel(low, high);

        //then
        assertThat(actualResult).isEqualTo(expectedResult);
    }

    @Test
    void  calculateValueForLevelHighLowTest() {
        //given
        BigDecimal low = new BigDecimal("200");
        BigDecimal high = new BigDecimal("100");
        Map<FibaLevel, BigDecimal> expectedResult = new LinkedHashMap<>(){
            {
                put(ZERO, new BigDecimal("100"));
//                put(TWOTHREESIX, new BigDecimal("123.600"));
                put(THREEEIGHTTWO, new BigDecimal("138.200"));
                put(FIVE, new BigDecimal("150.0"));
//                put(SIXONEEIGHT, new BigDecimal("161.800"));
//                put(SEVENEIGHTSIX, new BigDecimal("178.600"));
                put(ONE, new BigDecimal("200"));
//                put(ONESIXONEEIGHT, new BigDecimal("261.800"));
//                put(TWO, new BigDecimal("300"));
            }};

        //when
        Map<FibaLevel, BigDecimal> actualResult = FibaHelper.calculateValueForLevel(low, high);

        //then
        assertThat(actualResult).isEqualTo(expectedResult);
    }
}
