import io.netty.buffer.AdaptiveByteBufAllocator;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.buffer.PooledByteBufAllocator;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Param;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.Threads;
import org.openjdk.jmh.annotations.Warmup;
import org.openjdk.jmh.infra.Blackhole;
import org.openjdk.jmh.results.format.ResultFormatType;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

@State(Scope.Benchmark)
@Warmup(iterations = 1, time = 1)
@Measurement(iterations = 1, time = 1)
@Fork(1)
public class BufAllocatorOOMBenchmark {

    private final AdaptiveByteBufAllocator adaptiveAllocatorNoCache =
            new AdaptiveByteBufAllocator(true, false);

    private final ByteBufAllocator defaultPooledAllocator = PooledByteBufAllocator.DEFAULT;

    @Param("659225")
    private int size;

    public static void main(String[] args) throws RunnerException {
        Options opt = new OptionsBuilder()
                .include(BufAllocatorOOMBenchmark.class.getSimpleName())
                .resultFormat(ResultFormatType.JSON)
                .jvmArgs("-Xms168m", "-Xmx168m", "-XX:MaxDirectMemorySize=168m")
                .build();
        new Runner(opt).run();
    }

    @Benchmark
    @Threads(4)
    public void allocateReleaseHeapAdaptive(Blackhole blackhole) {
        ByteBuf buf = adaptiveAllocatorNoCache.heapBuffer(size);
        blackhole.consume(buf);
        blackhole.consume(buf.release());
    }

    @Benchmark
    @Threads(4)
    public void allocateReleaseHeapDefault(Blackhole blackhole) {
        ByteBuf buf = defaultPooledAllocator.heapBuffer(size);
        blackhole.consume(buf);
        blackhole.consume(buf.release());
    }

}
