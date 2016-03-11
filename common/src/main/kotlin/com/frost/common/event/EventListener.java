package com.frost.common.event;

@FunctionalInterface
public interface EventListener<E extends Event> {
    void onEvent(E e);
}
