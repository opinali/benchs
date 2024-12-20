/**
 * @author Osvaldo
 */
public final class SyncBench {
  /**
   * Benchmark startup.
   */
  public static void main(String[] args) {
    Thinker.thinkTimeLoops = -1;
    int[] threadNumbers = null;

    // Parameters

    for (int i = 0; i < args.length; ++i) {
      if (args[i].startsWith("-thinktime:"))
        Thinker.thinkTimeGoal = Integer.parseInt(args[i].substring(11));
      else if (args[i].startsWith("-thinkloops:"))
        Thinker.thinkTimeLoops = Integer.parseInt(args[i].substring(12));
      else if (args[i].equals("-threads")) {
        threadNumbers = new int[args.length - ++i];

        for (int j = 0; i < args.length; ++j, ++i)
          threadNumbers[j] = Integer.parseInt(args[i]);
      }
    }

    // Default values and startup that depends on parameters

    if (threadNumbers == null) threadNumbers = new int[] {1, 2, 4, 8, 16, 32, 64, 128};

    Benchmark.minOps = 1000;
    Benchmark.minTime = 2000;
    Benchmark.cycles = 1;
    Thinker.warmUp(100000);
    if (Thinker.thinkTimeLoops == -1) Thinker.calibrate();
    Benchmark.bench(args, threadNumbers, new BenchSync(true), new BenchSync(), new BenchLock());
  }
}
