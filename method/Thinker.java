/**
 * Contains dummy computation for benchmarks that need think-time.
 */
public class Thinker {
  /**
   * Desired think time (ns).
   */
  public static int thinkTimeGoal = 100;

  /**
   * Loop counter to obtain the desired think time.
   */
  public static int thinkTimeLoops;

  /**
   * This is the thinking method. Based on a pseudo-random number generator (from
   * http://www.snippets.org), but the only important properties are: 1) Uses some CPU 2) Cannot be
   * easy to inline (it's important to have a loop here) 3) Cannot be too slow, to allow calibration
   * with decent precision 4) Returns a result that depends on the input
   */
  public static int think(int x) {
    for (int i = 0; i < thinkTimeLoops; ++i) {
      int lo = 16807 * (x & 0xFFFF);
      int hi = 16807 * (x >>> 16);
      lo += (hi & 0x7FFF) << 16;
      lo += hi >>> 15;
      x = lo;
    }

    return x;
  }

  /**
   * Warm-up for think(), forces it to be optimized by lazy JITs.
   */
  public static void warmUp(int iters) {
    thinkTimeLoops = 100;
    int value = 0;

    for (int i = 0; i < iters / 2; ++i) {
      value ^= think(value);
      if (i % 333 == 0) Benchmark.consume(value);
      value ^= think(value);
      if (i % 555 == 0) Benchmark.consume(value);
    }

    // Allows idle-time JIT to work. This is only a paranoic feature;
    // it's better to trust on options like HotSpot's -Xbatch.

    try {
      Thread.sleep(100);
    } catch (InterruptedException e) {
    }
  }

  /**
   * Calibration of think(). Tries to find an ideal value for thinmkTimeLoops: the minimum value
   * that produces a runtime equal or greater to thinkTimeGoal.
   */
  public static void calibrate() {
    thinkTimeLoops = 1; // initial loop count
    int value = 0; // accumulator for think()
    long span = 0; // time for current loop count
    long lastSpan; // time for previous loop count
    boolean growing = true; // mode: growing/ungrowing

    System.out.print("Calibrate: loops=" + thinkTimeLoops);

    for (int attempt = 0;; ++attempt) {
      // Runs think() a lot of times, to measure it with enough precision.

      long start = System.currentTimeMillis();

      for (int i = 0; i < 1000000; ++i)
        value ^= think(value);

      lastSpan = span;
      span = System.currentTimeMillis() - start;
      Benchmark.consume(value);

      // Force the first 3 iterations with loops=1, and discard these
      // iterations. This is good to warm-up the calibration code itself.
      if (attempt < 3) continue;

      if (growing) {
        // Growing mode: first we increase loops, possibility very fast,
        // until the runtime exceeds our goal. When this happens, we
        // switch to the "ungrowing" mode.

        if (span >= thinkTimeGoal) {
          growing = false;
          --thinkTimeLoops;
        } else
          thinkTimeLoops = Benchmark.grow(thinkTimeLoops, span, thinkTimeGoal, true);
      } else {
        // "Ungrowing" mode: after the goal was exceeded, we reduce the
        // number of loops very slowly (steps of 1) until we find the
        // first value that doesn't exceed the goal; then, we stop with
        // the but-last value, which is the smallest one that hits the goal.

        if (span <= thinkTimeGoal) {
          ++thinkTimeLoops;
          break;
        }

        --thinkTimeLoops;
      }

      System.out.print("," + thinkTimeLoops);
    }

    System.out.println("=" + lastSpan + "ns");
  }
}
