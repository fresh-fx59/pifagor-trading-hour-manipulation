package org.example.model;

public record OrderManipulationResponseRecord<T> (
    String retCode,
    String retMsg,
    T result,
    Object retExtInfo,
    String time
) { }
