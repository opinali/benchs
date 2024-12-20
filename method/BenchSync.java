/**
 * Tests synchronized.
 */
final class BenchSync extends Benchmark implements Runnable {
  public static final Object sync = new Object();

  public BenchSync(boolean doSync) {
    super(doSync);
  }

  public BenchSync() {
  }

  @Override
  public void run() {
    for (int i = operations; i > 0; --i) {
      if (!isRef) synchronized (sync) {
        count ^= Thinker.think(count);
      } else
        count ^= Thinker.think(count);

      count ^= Thinker.think(count);
    }

    Benchmark.consume(count);
  }
}
