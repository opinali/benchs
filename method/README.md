# Method Call Microbenchmarks

Benchmarks for method call overhead and synchronization in the JVM.

Sample run (Ryzen 9 9900X, JDK 23):

```
C:\Users\opinali\source\benchs\method>run
*** Testing (Hard) ***
Interface: 00.000  TrulyPoly: 01.268  Poly: 00.176  Virtual: 00.190  Final: 00.187  Static: 00.187
Interface: 00.000  TrulyPoly: 01.182  Poly: 00.176  Virtual: 00.187  Final: 00.187  Static: 00.189
Interface: 00.000  TrulyPoly: 01.015  Poly: 00.176  Virtual: 00.187  Final: 00.187  Static: 00.187
Interface: 00.000  TrulyPoly: 00.995  Poly: 00.176  Virtual: 00.187  Final: 00.187  Static: 00.187
Interface: 00.000  TrulyPoly: 00.995  Poly: 00.178  Virtual: 00.187  Final: 00.190  Static: 00.187
*** Testing (Easy) ***
Interface: 00.177  TrulyPoly: 00.000  Poly: 00.000  Virtual: 00.000  Final: 00.000  Static: 00.000
Interface: 00.177  TrulyPoly: 00.000  Poly: 00.000  Virtual: 00.000  Final: 00.000  Static: 00.000
Interface: 00.176  TrulyPoly: 00.000  Poly: 00.000  Virtual: 00.000  Final: 00.000  Static: 00.000
Interface: 00.176  TrulyPoly: 00.000  Poly: 00.000  Virtual: 00.000  Final: 00.000  Static: 00.000
Interface: 00.176  TrulyPoly: 00.000  Poly: 00.000  Virtual: 00.000  Final: 00.000  Static: 00.000
Threads REF     Sync    Lock
1       282.240 02.838  05.369
1       283.358 03.195  04.605
2       148.421 45.142  206.724
4       76.581  146.112 345.493
8       48.584  174.189 360.253
16      24.896  199.186 382.661
32      16.854  207.491 392.969
64      17.669  206.314 386.164
128     16.935  206.289 397.778
```

Note: I make no resresentations of the quality of these benchmarks today... they were good circa 2003
when I wrote them :) but since then, JVMs have evolved. The scores are in nanoseconds, all zeros mean
a completely broken benchmark (the code is all optimized out).

The synchronization bench at the end is still looking OK.
