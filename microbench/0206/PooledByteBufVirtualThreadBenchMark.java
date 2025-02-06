/*
 * Copyright 2025 The Netty Project
 *
 * The Netty Project licenses this file to you under the Apache License,
 * version 2.0 (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at:
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */
package io.netty.microbench.buffer;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.PooledByteBufAllocator;
import java.util.concurrent.CountDownLatch;

public class PooledByteBufVirtualThreadBenchMark {

    private static final int THREAD_COUNT = 1000000;
    private static final int WARMUP_ITERATIONS = 5;
    private static final int BENCH_ITERATIONS = 5;
    private static final int TOTAL_ITERATIONS = WARMUP_ITERATIONS + BENCH_ITERATIONS;

    private static final PooledByteBufAllocator DEFAULT = PooledByteBufAllocator.DEFAULT;
    private static final PooledByteBufAllocator DEFAULT_NO_TL = PooledByteBufAllocator.DEFAULT_NO_TL;

    public static void main(String[] args) throws InterruptedException {
        for (int i = 0; i < 5; i++) {
            noThreadLocalBench();
            defaultBench();
            System.out.println("---------------------------------------");
        }
    }

    private static void noThreadLocalBench() throws InterruptedException {
        long[] durations = new long[TOTAL_ITERATIONS];
        for (int i = 0; i < TOTAL_ITERATIONS; i++) {
            durations[i] = doBench(DEFAULT_NO_TL);
        }
        long sum = 0;
        // Only calculate BENCH_ITERATIONS.
        for (int j = WARMUP_ITERATIONS; j < TOTAL_ITERATIONS; j++) {
            sum += durations[j];
        }
        System.out.println("NO-ThreadLocal alloc avg duration: " + sum / BENCH_ITERATIONS);
    }

    private static void defaultBench() throws InterruptedException {
        long[] durations = new long[TOTAL_ITERATIONS];
        for (int i = 0; i < TOTAL_ITERATIONS; i++) {
            durations[i] = doBench(DEFAULT);
        }
        long sum = 0;
        // Only calculate BENCH_ITERATIONS.
        for (int j = WARMUP_ITERATIONS; j < TOTAL_ITERATIONS; j++) {
            sum += durations[j];
        }
        System.out.println("Default alloc avg duration: " + sum / BENCH_ITERATIONS);
    }

    private static long doBench(PooledByteBufAllocator allocator) throws InterruptedException {
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch endLatch = new CountDownLatch(THREAD_COUNT);
        for (int i = 0; i < THREAD_COUNT; i++) {
            Thread.startVirtualThread(() -> {
                ByteBuf bf1 = null;
                ByteBuf bf2 = null;
                ByteBuf bf3 = null;
                try {
                    startLatch.await();
                    bf1 = allocator.heapBuffer(123);
                    bf2 = allocator.heapBuffer(1234);
                    bf3 = allocator.heapBuffer(12345);
                } catch (Throwable t) {
                    System.err.println(t);
                    System.exit(0);
                } finally {
                    if (bf3 != null) {
                        bf3.release();
                    }
                    if (bf1 != null) {
                        bf1.release();
                    }
                    if (bf2 != null) {
                        bf2.release();
                    }
                    endLatch.countDown();
                }
            });
        }
        long startTime = System.currentTimeMillis();
        startLatch.countDown();
        endLatch.await();
        return System.currentTimeMillis() - startTime;
    }
}
