package org.example.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * <a href="https://bybit-exchange.github.io/docs/v5/enum#orderstatus">docs</a>
 * open status
 * <p>
 *     New - order has been placed successfully
 *     PartiallyFilled
 *     Untriggered - Conditional orders are created
 * <p>
 * closed status
 * <p>
 *     Rejected
 *     PartiallyFilledCanceled - Only spot has this order status
 *     Filled
 *     Cancelled - In derivatives, orders with this status may have an executed qty
 *     Triggered - instantaneous state for conditional orders from Untriggered to New
 *     Deactivated - UTA: Spot tp/sl order, conditional order, OCO order are cancelled before they are triggered
 */
@RequiredArgsConstructor
@Getter
public enum OrderStatus {
    FILLED("Filled"),
    NEW("New"),
    UPDATED(""),
    ERROR_ON_CREATION(""),
    ERROR_ON_UPDATE(""),
    ERROR_ON_CANCELLATION(""),
    CANCELLED("Cancelled"),
    ;

    private final String bybitStatus;
}
