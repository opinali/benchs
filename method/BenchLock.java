import java.util.concurrent.locks.ReentrantLock;

/**
 * Tests ReentrantLock.
 */
final class BenchLock extends Benchmark implements Runnable {
  public static final ReentrantLock sync = new ReentrantLock();

  public BenchLock(boolean isRef) {
    super(isRef);
  }

  public BenchLock() {
  }

  @Override
  public void run() {
    for (int i = operations; i > 0; --i) {
      try {
        if (!isRef) sync.lock();
        count ^= Thinker.think(count);
      } finally {
        if (!isRef) sync.unlock();
      }

      count ^= Thinker.think(count);
    }

    Benchmark.consume(count);
  }
}
