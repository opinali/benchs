// clang-format off
#include <windows.h>
// clang-format on

#include <bcrypt.h>

#include <cstdint>
#include <cstdio>
#include <string>
#include <vector>

std::string StatusToStringFallback(NTSTATUS status) {
  char buf[256];
  snprintf(buf, 255, "Unknown status: %lu", status);
  return buf;
}

std::string StatusToString(NTSTATUS status) {
  HMODULE ntdll = GetModuleHandleA("ntdll.dll");
  if (ntdll == NULL) return StatusToStringFallback(status);

  LPSTR buf = nullptr;
  size_t size = FormatMessageA(
      FORMAT_MESSAGE_ALLOCATE_BUFFER | FORMAT_MESSAGE_FROM_HMODULE |
          FORMAT_MESSAGE_FROM_SYSTEM | FORMAT_MESSAGE_IGNORE_INSERTS,
      ntdll, status, MAKELANGID(LANG_NEUTRAL, SUBLANG_DEFAULT), (LPSTR)&buf, 0,
      NULL);

  if (size == 0) return StatusToStringFallback(status);

  std::string msg(buf, size);
  LocalFree(buf);
  msg.erase(std::remove(msg.begin(), msg.end(), '\n'), msg.end());
  msg.erase(std::remove(msg.begin(), msg.end(), '\r'), msg.end());
  return msg;
}

BCRYPT_ALG_HANDLE OpenProvider(LPCWSTR rng_algo) {
  BCRYPT_ALG_HANDLE h_alg = 0;
  // Note: MS_PLATFORM_CRYPTO_PROVIDER (to force raw TPM entropy) doesn't work
  // with RNG algos. But the Primitive Provider should use the Windows entropy
  // pool, which includes entropy from the TPM if available.
  NTSTATUS status =
      BCryptOpenAlgorithmProvider(&h_alg, rng_algo, MS_PRIMITIVE_PROVIDER, 0);
  if (FAILED(status)) {
    printf("BCryptOpenAlgorithmProvider failed: %s\n",
           StatusToString(status).c_str());
    exit(1);
  }
  return h_alg;
}

void CloseProvider(BCRYPT_ALG_HANDLE h_alg) {
  NTSTATUS status = BCryptCloseAlgorithmProvider(h_alg, 0);
  if (FAILED(status)) {
    printf("BCryptCloseAlgorithmProvider failed: %s\n",
           StatusToString(status).c_str());
    exit(1);
  }
}

uint64_t BenchKernel(BCRYPT_ALG_HANDLE h_alg, int blocksize, int iters) {
  std::vector<UCHAR> buffer;
  buffer.resize(blocksize);
  LARGE_INTEGER frequency;
  QueryPerformanceFrequency(&frequency);
  LARGE_INTEGER t_start, t_end;
  QueryPerformanceCounter(&t_start);
  NTSTATUS status = 0;  // STATUS_SUCCESS

  for (int i = 0; i < iters && !FAILED(status); ++i) {
    status = BCryptGenRandom(h_alg, &buffer[0], blocksize, /*ignored*/ 0);
  }

  if (FAILED(status)) {
    printf("BCryptGenRandom(%d) failed: %s\n", blocksize,
           StatusToString(status).c_str());
    CloseProvider(h_alg);
    exit(1);
  }

  // Return score in bytes-per-secondelapsed time in microseconds
  QueryPerformanceCounter(&t_end);
  auto seconds = double(t_end.QuadPart - t_start.QuadPart) / frequency.QuadPart;
  return static_cast<uint64_t>((blocksize * iters) / seconds);
}

void RunBench(LPCWSTR rng_algo) {
  printf("Benchmarking %ls:\n", rng_algo);
  BCRYPT_ALG_HANDLE h_alg = OpenProvider(rng_algo);

  for (int blocksize_exp = 0; blocksize_exp <= 24;
       blocksize_exp += (blocksize_exp >= 20 ? 1 : 4)) {
    auto blocksize = 1 << blocksize_exp;
    auto iters = 1 << (24 - blocksize_exp);
    auto score = BenchKernel(h_alg, blocksize, iters) / (1 << 20);
    printf("Block = %d, score = %llu Mb/s.\n", blocksize, score);
  }

  CloseProvider(h_alg);
}

int main(int argc, char **argv) {
  // Note: BCRYPT_RNG_DUAL_EC_ALGORITHM is deprecated
  RunBench(BCRYPT_RNG_ALGORITHM);
  RunBench(BCRYPT_RNG_FIPS186_DSA_ALGORITHM);
  return 0;
}
