package org.example.model;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

@Data
public class Kline implements Serializable {
    private long startTime;
    private BigDecimal openPrice;
    private BigDecimal highPrice;
    private BigDecimal lowPrice;
    private BigDecimal closePrice;
    private String volume;
    private String turnover;
}
