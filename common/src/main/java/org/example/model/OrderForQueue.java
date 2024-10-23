package org.example.model;

import lombok.Data;
import org.apache.commons.lang3.StringUtils;
import org.example.enums.OrderStatus;

import java.sql.Date;

@Data
public class OrderForQueue {
    private Long id;
    private OrderStatus status;
    private String errorMessage;
    private Date updatedAt;
    private String createdBy;

    private final Order order;

    public OrderForQueue(Order order) {
        this.order = order;
    }

    public boolean hasErrorMessage() {
        return StringUtils.isNotBlank(this.errorMessage);
    }

}
