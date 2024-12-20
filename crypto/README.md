# Win32 Secure Random benchmark

Microbenchmark for producing random numbers on Win32, hopefully with crypto-secure entropy sourced from the TPM module.

Sample run, Ryzen 9 9900X:

```
> CryptoBench.exe
Benchmarking RNG:
Block = 1, score = 55 Mb/s.
Block = 16, score = 534 Mb/s.
Block = 256, score = 1978 Mb/s.
Block = 4096, score = 7119 Mb/s.
Block = 65536, score = 8497 Mb/s.
Block = 1048576, score = 8449 Mb/s.
Block = 2097152, score = 8426 Mb/s.
Block = 4194304, score = 8400 Mb/s.
Block = 8388608, score = 8136 Mb/s.
Block = 16777216, score = 7580 Mb/s.
Benchmarking FIPS186DSARNG:
Block = 1, score = 55 Mb/s.
Block = 16, score = 558 Mb/s.
Block = 256, score = 2012 Mb/s.
Block = 4096, score = 7120 Mb/s.
Block = 65536, score = 8407 Mb/s.
Block = 1048576, score = 8466 Mb/s.
Block = 2097152, score = 8433 Mb/s.
Block = 4194304, score = 8303 Mb/s.
Block = 8388608, score = 8135 Mb/s.
Block = 16777216, score = 7487 Mb/s.
```

