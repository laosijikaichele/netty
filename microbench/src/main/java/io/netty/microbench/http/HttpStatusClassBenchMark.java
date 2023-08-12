package io.netty.microbench.http;
import io.netty.microbench.util.AbstractMicrobenchmark;
import io.netty.util.internal.SuppressJava6Requirement;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Level;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Param;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.Threads;
import org.openjdk.jmh.annotations.Warmup;
import io.netty.handler.codec.http.*;
import java.util.SplittableRandom;
import java.util.concurrent.TimeUnit;
//@State(Scope.Benchmark)
@OutputTimeUnit(TimeUnit.MICROSECONDS)
@Fork(2)
@Threads(1)
@Warmup(iterations = 2, time = 1)
@Measurement(iterations = 2, time = 1)
public class HttpStatusClassBenchMark extends AbstractMicrobenchmark {

    private int[] data;

    private HttpStatusClass[] result;

    @Param({ "7", "23", "47", "97"})
    public int size;

    @Setup(Level.Invocation)
    @SuppressJava6Requirement(reason = "using SplittableRandom to reliably produce data")
    public void setup() {
        final SplittableRandom random = new SplittableRandom();
        data = new int[size];
        result = new HttpStatusClass[size];

        for (int j = 0; j < size; j++) {
            data[j] = random.nextInt(100, 700);
        }
    }

    private int getData(int i) {
        return data[i];
    }

    @Benchmark
    public HttpStatusClass[] ofValue() {
        for (int i = 0; i < size; ++i) {
            result[i] = HttpStatusClass.valueOf(getData(i));
        }
        return result;
    }

}
