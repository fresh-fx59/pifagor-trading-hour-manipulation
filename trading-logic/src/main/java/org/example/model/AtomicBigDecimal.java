package org.example.model;

import java.math.BigDecimal;
import java.util.concurrent.atomic.AtomicReference;

public class AtomicBigDecimal {
    private final AtomicReference<BigDecimal> value;

    public AtomicBigDecimal(BigDecimal initialValue) {
        this.value = new AtomicReference<>(initialValue);
    }

    public BigDecimal get() {
        return value.get();
    }

    public void add(BigDecimal delta) {
        while (true) {
            final BigDecimal current = value.get();
            final BigDecimal newValue = current.add(delta);
            if (value.compareAndSet(current, newValue)) {
                break; // Successfully updated
            }
            // If another thread updated the value, retry
        }
    }

    public void substruct(BigDecimal delta) {
        while (true) {
            final BigDecimal current = value.get();
            final BigDecimal newValue = current.subtract(delta);
            if (value.compareAndSet(current, newValue)) {
                break; // Successfully updated
            }
            // If another thread updated the value, retry
        }
    }
}
