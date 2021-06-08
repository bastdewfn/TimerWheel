package timerwheel;

@FunctionalInterface
public interface TimeTaskConsumer<T> {
   void exec(T sourceDada);
}
