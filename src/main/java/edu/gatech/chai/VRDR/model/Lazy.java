package edu.gatech.chai.VRDR.model;

import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;

public class Lazy<T> {
    private final AtomicReference<T> instance = new AtomicReference<>();

    public T get(Supplier<T> supplier) {
        T value = instance.get();
        if (value == null) {
            value = supplier.get();
            if (!instance.compareAndSet(null, value)) {
                value = instance.get();
            }
        }
        return value;
    }
}

