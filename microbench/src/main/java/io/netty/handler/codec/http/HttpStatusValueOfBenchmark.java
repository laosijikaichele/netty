/*
 * Copyright 2019 The Netty Project
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
package io.netty.handler.codec.http;
import io.netty.microbench.util.AbstractMicrobenchmark;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Level;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Param;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.Warmup;
import org.openjdk.jmh.infra.Blackhole;
import io.netty.util.internal.SuppressJava6Requirement;
import java.text.DecimalFormat;
import java.util.SplittableRandom;
import java.util.concurrent.TimeUnit;

@BenchmarkMode(Mode.Throughput)
@Warmup(iterations = 2, time = 1)
@Measurement(iterations = 2, time = 1)
@OutputTimeUnit(TimeUnit.MICROSECONDS)
public class HttpStatusValueOfBenchmark extends AbstractMicrobenchmark {
    private int[] data;
    private HttpStatusClass[] result;
    @Param({"32", "63", "121", "247", "519", "1021"})
    public int size;
    private final DecimalFormat df = new DecimalFormat("#.00");

    @Setup(Level.Iteration)
    @SuppressJava6Requirement(reason = "suppress")
    public void setup(Blackhole bh) {
        final SplittableRandom random = new SplittableRandom();
        data = new int[size];
        result = new HttpStatusClass[size];

        int INFORMATIONAL_count = 0, SUCCESS_count = 0, REDIRECTION_count = 0, CLIENT_ERROR_count = 0,
                SERVER_ERROR_count = 0, UNKNOWN_count = 0;

        for (int i = 0; i < size; i++) {
            // The code random range: [100, 700)
            int code = random.nextInt(100, 700);
            // If code is 'UNKNOWN'
            if (code >= 600) {
                int unknownCode;
                // The 'UNKNOWN' code random range: [Integer.MIN_VALUE, 100) and [600, Integer.MAX_VALUE]
                do {
                    unknownCode = random.nextInt();
                } while (unknownCode >= 100 && unknownCode < 600);
                code = unknownCode;
            }
            data[i] = code;

            if (HttpStatusClass.INFORMATIONAL.contains(code)) {
                ++ INFORMATIONAL_count;
            }
            if (HttpStatusClass.SUCCESS.contains(code)) {
                ++ SUCCESS_count;
            }
            if (HttpStatusClass.REDIRECTION.contains(code)) {
                ++ REDIRECTION_count;
            }
            if (HttpStatusClass.CLIENT_ERROR.contains(code)) {
                ++ CLIENT_ERROR_count;
            }
            if (HttpStatusClass.SERVER_ERROR.contains(code)) {
                ++ SERVER_ERROR_count;
            }
            if (HttpStatusClass.UNKNOWN.contains(code)) {
                ++ UNKNOWN_count;
            }
        }

        // Print the percentage of each code type:
        System.out.println("\nCode distribution===>"
                +"INFORMATIONAL:" + df.format((INFORMATIONAL_count * 100.0f) / size)
                + "%, SUCCESS:" + df.format((SUCCESS_count * 100.0f) / size)
                + "%, REDIRECTION:" + df.format((REDIRECTION_count * 100.0f) / size)
                + "%, CLIENT_ERROR:" + df.format((CLIENT_ERROR_count * 100.0f) / size)
                + "%, SERVER_ERROR:" + df.format((SERVER_ERROR_count * 100.0f) / size)
                + "%, UNKNOWN:" + df.format((UNKNOWN_count * 100.0f) / size));

        // Confuse JIT prediction.
        HttpStatusClass setupResult;
        for (int j = 0; j < 210000; j++) {
            setupResult = HttpStatusClass.valueOf(random.nextInt());
            bh.consume(setupResult);
            setupResult = HttpStatusClass.valueOfSwitchCase(random.nextInt());
            bh.consume(setupResult);
            setupResult = HttpStatusClass.valueOfSwitchCaseWithFastDiv(random.nextInt());
            bh.consume(setupResult);
            setupResult = HttpStatusClass.valueOfArrayIndexWithFastDiv(random.nextInt());
            bh.consume(setupResult);
        }
    }

    @Benchmark
    public HttpStatusClass[] valueOf() {
        for (int i = 0; i < size; ++i) {
            result[i] = HttpStatusClass.valueOf(data[i]);
        }
        return result;
    }

    @Benchmark
    public HttpStatusClass[] valueOfSwitchCase() {
        for (int i = 0; i < size; ++i) {
            result[i] = HttpStatusClass.valueOfSwitchCase(data[i]);
        }
        return result;
    }

    @Benchmark
    public HttpStatusClass[] valueOfSwitchCaseWithFastDiv() {
        for (int i = 0; i < size; ++i) {
            result[i] = HttpStatusClass.valueOfSwitchCaseWithFastDiv(data[i]);
        }
        return result;
    }

    @Benchmark
    public HttpStatusClass[] valueOfArrayIndexWithFastDiv() {
        for (int i = 0; i < size; ++i) {
            result[i] = HttpStatusClass.valueOfArrayIndexWithFastDiv(data[i]);
        }
        return result;
    }
}
