package io.netty.microbench.buffer;

import io.netty.buffer.AdaptiveByteBufAllocator;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.buffer.PooledByteBufAllocator;
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
import org.openjdk.jmh.runner.options.ChainedOptionsBuilder;

@State(Scope.Benchmark)
@Warmup(iterations = 5, time = 1)
@Measurement(iterations = 5, time = 1)
@Fork(1)
public class BufAllocatorConcurrentCompareMultiLess12345Benchmark_32_threads extends AbstractMicrobenchmark {

    private final AdaptiveByteBufAllocator adaptiveAllocatorNoCache =
            new AdaptiveByteBufAllocator(true, false);

//    private final ByteBufAllocator defaultPooledAllocator = PooledByteBufAllocator.DEFAULT;

//    private final AdaptiveByteBufAllocator adaptiveAllocatorCache =
//            new AdaptiveByteBufAllocator(true, true);

//    @Param({"12345", "123456", "1234567"})
    @Param({"123"})
    private int size;

    private final String[] sizeParams = new String[13345 / 123];

    public BufAllocatorConcurrentCompareMultiLess12345Benchmark_32_threads() {
        // Set the second param to 'false' when using 'adaptiveAllocatorCache'.
        super(false, true);
        sizeParams[0] = "123";
        for (int i = 1; i < sizeParams.length; i++) {
            sizeParams[i] = String.valueOf(Integer.parseInt(sizeParams[i-1]) + 123);
        }
    }

    @Override
    protected ChainedOptionsBuilder newOptionsBuilder() throws Exception {
        return super.newOptionsBuilder().param("size", sizeParams);
    }

    @Benchmark
    @Threads(32)
    public void allocateReleaseHeap(Blackhole blackhole) {
        ByteBuf buf = adaptiveAllocatorNoCache.heapBuffer(size);
        blackhole.consume(buf);
        blackhole.consume(buf.release());
    }

//    @Benchmark
//    @Threads(8)
//    public void allocateReleaseHeap(Blackhole blackhole) {
//        ByteBuf buf = defaultPooledAllocator.heapBuffer(size);
//        blackhole.consume(buf);
//        blackhole.consume(buf.release());
//    }

//    @Benchmark
//    @Threads(8)
//    public void allocateReleaseHeap(Blackhole blackhole) {
//        ByteBuf buf = adaptiveAllocatorCache.heapBuffer(size);
//        blackhole.consume(buf);
//        blackhole.consume(buf.release());
//    }

}
