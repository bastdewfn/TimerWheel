package com.dewfn.timerwheel;

@FunctionalInterface
public interface TimeTaskConsumer<T> {
   void exec(T sourceData);
}
