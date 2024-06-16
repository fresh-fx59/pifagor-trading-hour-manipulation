package org.example.model;

import com.bybit.api.client.domain.market.request.MarketDataRequest;
import lombok.*;
import org.apache.commons.collections4.CollectionUtils;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class MarketData implements Serializable {
    private String symbol;
    private String category;
    private String marketInterval;
    private List<Kline> list;

    public MarketData(MarketDataRequest request) {
        this.symbol = request.getSymbol();
        this.category = request.getCategory().getCategoryTypeId();
        this.marketInterval = request.getMarketInterval().getIntervalId();
        this.list = new ArrayList<Kline>();
    }

    public String toString() {
        StringBuilder listData = new StringBuilder();

        if (CollectionUtils.isNotEmpty(list)) {
            Iterator<Kline> iter = list.iterator();
            while (iter.hasNext()) {
                listData.append(iter.next());
                listData.append("\n");
            }
        }

        return symbol + " " + category + " " + marketInterval + "\n" + listData
//                +
//                list.stream()
//                        .map(Kline::getStartTime)
//                        .reduce(0L, Long::sum
//                        )
        ;
    }
}
