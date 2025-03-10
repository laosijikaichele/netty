package io.netty.microbench.buffer;

import io.netty.microbench.util.AbstractMicrobenchmark;
import io.netty.util.internal.PlatformDependent;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;
import java.util.Queue;

@State(Scope.Benchmark)
@Warmup(iterations = 10, time = 1)
@Measurement(iterations = 10, time = 1)
@Fork(2)
public class QueuePlainNewBenchmark extends AbstractMicrobenchmark {

    private static final Queue<Object> bufferQueue = PlatformDependent.newFixedMpmcQueue(1024);

    public QueuePlainNewBenchmark() {
        super(true, true);
    }

    @Setup
    public void setup() {
        // Fill half of the queue .
        for (int i = 0; i < bufferQueue.size() / 2; i++) {
            bufferQueue.offer(new Object());
        }
    }

    @Benchmark
    @Threads(1)
    public void plainNew(Blackhole bk) {
        // Create from plain-new
        Object o = new Object();
        bk.consume(o);
        // Simulate recycle:
        // No explicit recycle code needed.
    }

    @Benchmark
    @Threads(1)
    public void queue(Blackhole bk) {
        // Most objects will be poll-ed from the queue.
        Object o = bufferQueue.poll();
        if (o == null) {
            // Only very few/none will reach here.
            o = new Object();
        }
        bk.consume(o);
        // Simulate recycle:
        bufferQueue.offer(o);
    }

}
