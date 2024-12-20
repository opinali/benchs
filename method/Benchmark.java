import java.text.DecimalFormat;

/**
 * Base class and controller for all benchmarks.
 */
public abstract class Benchmark {
  /**
   * Number of threads.
   */
  protected static int nThreads;

  /**
   * All options for thread numbers.
   */
  protected static int[] threadNumbers;

  /**
   * Initial number of operations.
   */
  public static int minOps;

  /**
   * Maximum number of operations.
   */
  public int maxOps;

  /**
   * Constant for the benchmark controller's loop.
   */
  public static int cycles;

  /**
   * Start time for the benchmark cycle.
   */
  private long start;

  /**
   * Total time for the benchmark cycle.
   */
  private long span;

  /**
   * Number of operations.
   */
  public int operations;

  /**
   * Minimum runtime per benchmark (ms).
   */
  public static long minTime;

  /**
   * Reference benchmark, if any.
   */
  protected static Benchmark reference;

  /**
   * Score (ns).
   */
  protected double score;

  /**
   * Flag telling if this is a reference benchmark.
   */
  protected final boolean isRef;

  /**
   * Accumulator for think-time results.
   */
  protected int count;

  public Benchmark(boolean isRef) {
    operations = minOps;
    maxOps = Integer.MAX_VALUE;
    this.isRef = isRef;
  }

  public Benchmark() {
    this(false);
  }

  /**
   * Generic benchmark main procedure: command-line processing and controller.
   */
  public static void bench(String[] args, int[] threadNumbers, Benchmark... bench) {
    String only = null;
    Benchmark.threadNumbers = threadNumbers;

    // Parameters

    for (int i = 0; i < args.length; ++i) {
      if (args[i].startsWith("-cycles:"))
        cycles = Integer.parseInt(args[i].substring(8));
      else if (args[i].startsWith("-minops:"))
        minOps = Integer.parseInt(args[i].substring(8));
      else if (args[i].startsWith("-mintime:"))
        minTime = Long.parseLong(args[i].substring(9));
      else if (args[i].startsWith("-only:")) only = args[i].substring(6);
    }

    boolean singleThreaded = (threadNumbers.length == 1 && threadNumbers[0] == 1);

    // Print header

    if (!singleThreaded) System.out.print("Threads\t");

    for (int i = 0; i < bench.length; ++i) {
      if (i > 0) System.out.print("\t");
      System.out.print(bench[i].getName());
    }

    System.out.println();

    // Benchmark main loop

    for (int i = 0; i < threadNumbers.length; ++i) {
      // Resets operations and maxOps according to the thread#.

      if (i > 0) for (int k = 0; k < bench.length; ++k) {
        bench[k].maxOps = Math.max(minOps, bench[k].operations / nThreads * threadNumbers[i]);
        bench[k].operations = minOps;
      }

      // More preparation for this thread#. When running multiple thread#s,
      // we force an extra executions of 1 thread for additional warming up.

      nThreads = threadNumbers[i];
      int loopCycles = (singleThreaded || i != 0) ? cycles : Math.max(cycles, 2);

      // Now the coe to call the benchmark for this thread#. Most complication
      // here is for formatting and deciding whether to call the benchmark.

      for (int j = 0; j < loopCycles; ++j) {
        if (!singleThreaded) System.out.print(nThreads + "\t");

        boolean first = true;

        for (int k = 0; k < bench.length; ++k) {
          if (bench[k].isRef || only == null || bench[k].getName().equalsIgnoreCase(only)) {
            if (!first) System.out.print("\t");
            bench[k].execute();
            first = false;
          }
        }

        System.out.println();
      }
    }
  }

  public String getName() {
    if (isRef) return "REF";

    String className = getClass().getName();
    return className.substring(className.lastIndexOf("Bench") + 5);
  }

  /**
   * The inner bechmark executor.
   */
  public final void execute() {
    try {
      // Initialize the operation counter. The ideal value is big enough
      // to converge quickly to the minimum runtime, but small enough to
      // not exceed this runtime too much in the first attempt. We work
      // with a worst-case assumption that execution times will grow
      // quadratically with increased thread#.

      operations = minOps + (operations - minOps) / 4;

      if (!isRef && reference != null && reference.operations > operations)
        operations += (reference.operations - operations) / 4;

      // Do the benchmark until reaching the minimum runtime.

      do {
        start = System.currentTimeMillis();
        bench();
      } while (!checkEnd());

      // Produces and processes score

      consume(Helper.x);
      score = span * 1e6d / operations / nThreads;

      if (isRef)
        reference = this;
      else if (reference != null) score -= reference.score;

      System.out.print(format(score));

      // Adjusts the operations counter for an ideal value, because we
      // probably exceeded the minimum time. This ideal value will be
      // useful for the initialization of the next thread#.

      operations = (int) (((double) minTime / span) * operations);
    } catch (Throwable e) {
      System.out.print("N/A");
    }
  }

  /**
   * Checks if the benchmark can stop (enough precision obtained).
   */
  private boolean checkEnd() {
    span = System.currentTimeMillis() - start;

    if (span >= minTime || operations == maxOps) return true;

    operations = grow(operations, span, minTime, false);
    return false;
  }

  /**
   * Formats timings.
   */
  private static String format(double n) {
    return new DecimalFormat("00.000").format(n);
  }

  /**
   * Consumes a value produced by the benchmark, so its calculation cannot be eliminated as dead
   * code (but it might still be computed in compilation time).
   */
  public static void consume(int value) {
    StringBuilder s = new StringBuilder(format(value));
    s.setLength(0);
    System.out.print(s.toString());
  }

  /**
   * Benchmark code. This default implementation is a hook for the multithreaded benchmarks: these
   * will not override bench(), but rather provide an entry point for each thread. Single-threaded
   * benchmarks, on the other hand, will just override this method.
   */
  public void bench() throws Exception {
    Thread[] threads = new Thread[nThreads];

    for (int i = 0; i < nThreads; ++i)
      threads[i] = new Thread((Runnable) this);

    startAndJoin(threads);
  }

  /**
   * Grows an interation counter, trying to converge to a goal timespan.
   */
  public static int grow(int value, long span, long goal, boolean fine) {
    double growth;

    if (span == 0) {
      // Execution was too fast, measuring zero time. Let's give a large
      // boost to the counter, unless explicitly told not to do so.

      if (fine) return value + 1;

      span = 10;
    }

    // Find a growth factor in the 5% to 500% range.

    growth = (double) goal / span * 1.05;
    growth = Math.min(growth, 5);

    // Increase the counter, making sure it grows in at least 1 unit.

    int newValue = (int) (value * growth);
    if (newValue == value) ++newValue;
    return newValue;
  }

  /**
   * Launch worker threads for the multithreaded benchmark and wait all to end.
   */
  public static void startAndJoin(Thread[] threads) {
    for (int i = 0; i < threads.length; ++i)
      threads[i].start();

    for (int i = 0; i < threads.length; ++i)
      try {
        threads[i].join();
      } catch (Exception e) {
        System.out.println(e);
      }
  }
}
