package io.netty.microbench.buffer;

import io.netty.buffer.AdaptiveByteBufAllocator;
import io.netty.buffer.ByteBufAllocator;
import io.netty.microbench.util.AbstractMicrobenchmark;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Param;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.Threads;
import org.openjdk.jmh.annotations.Warmup;

@State(Scope.Benchmark)
@Warmup(iterations = 10, time = 1)
@Measurement(iterations = 10, time = 1)
@Threads(8)
public class ByteBufAllocatorConcurrentBenchmark extends AbstractMicrobenchmark {

    private static final ByteBufAllocator adaptiveAllocator = new AdaptiveByteBufAllocator();

    @Param({
            "00064",
            "00256",
            "01024",
            "04096",
    })
    public int size;

    public ByteBufAllocatorConcurrentBenchmark() {
        super(true, true);
    }

    @Benchmark
    public boolean allocateReleaseAdaptive() {
        return adaptiveAllocator.directBuffer(size).release();
    }
}
