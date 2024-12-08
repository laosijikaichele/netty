package io.netty.microbench.buffer;

import io.netty.buffer.AdaptiveByteBufAllocator;
import io.netty.buffer.ByteBuf;
import io.netty.microbench.util.AbstractMicrobenchmark;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Param;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.Threads;
import org.openjdk.jmh.annotations.Warmup;
import org.openjdk.jmh.infra.Blackhole;

@State(Scope.Benchmark)
@Warmup(iterations = 10, time = 1)
@Measurement(iterations = 10, time = 1)
@Fork(2)
public class AdaptiveByteBufAllocatorConcurrentNoCacheBenchmark2 extends AbstractMicrobenchmark {

    private static final AdaptiveByteBufAllocator adaptiveAllocator =
            new AdaptiveByteBufAllocator(true, false);

    @Param({
            "123",
            "1234",
            "12345",
            "123456",
            "1234567",
    })
    public int size;

    @Param({
            "0",
            "5",
            "10",
            "100",
    })
    public long tokens;

    public AdaptiveByteBufAllocatorConcurrentNoCacheBenchmark2() {
        super(false, true);
    }

    @Benchmark
    @Threads(32)
    public void allocateReleaseHeapAdaptive(Blackhole blackhole) {
        ByteBuf buf = adaptiveAllocator.heapBuffer(size);
        if (tokens > 0) {
            Blackhole.consumeCPU(tokens);
        }
        blackhole.consume(buf.release());
    }
}
