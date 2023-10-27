package edu.gatech.chai.VRDR.model;

import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;

public class Lazy<T> {
    private final Supplier<T> supplier;
    private volatile T value;

    public Lazy(Supplier<T> supplier) {
        this.supplier = supplier;
    }

    public T getValue() {
        if (value == null) {
            synchronized (this) {
                if (value == null) {
                    value = supplier.get();
                }
            }
        }
        return value;
    }
}

