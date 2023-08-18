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

        int equalDistributedArraySize = 11000;
        int[] data_ifElse = new int[equalDistributedArraySize];
        int[] data_switchCase = new int[equalDistributedArraySize];
        int[] data_switchCaseWithFastDiv = new int[equalDistributedArraySize];
        int[] data_arrayIndexWithFastDiv = new int[equalDistributedArraySize];
        initEqualDistributedData(data_ifElse, random);
        initEqualDistributedData(data_switchCase, random);
        initEqualDistributedData_switchCaseWithFastDiv(data_switchCaseWithFastDiv, random);
        initEqualDistributedData_arrayIndexWithFastDiv(data_arrayIndexWithFastDiv, random);
        for (int i = 0; i < data_ifElse.length; i++) {
            HttpStatusClass rs = HttpStatusClass.valueOf(data_ifElse[i]);
            bh.consume(rs);
        }
        for (int i = 0; i < data_switchCase.length; i++) {
            HttpStatusClass rs = HttpStatusClass.valueOfSwitchCase(data_switchCase[i]);
            bh.consume(rs);
        }
        for (int i = 0; i < data_switchCaseWithFastDiv.length; i++) {
            HttpStatusClass rs = HttpStatusClass.valueOfSwitchCaseWithFastDiv(data_switchCaseWithFastDiv[i]);
            bh.consume(rs);
        }
        for (int i = 0; i < data_arrayIndexWithFastDiv.length; i++) {
            HttpStatusClass rs = HttpStatusClass.valueOfArrayIndexWithFastDiv(data_arrayIndexWithFastDiv[i]);
            bh.consume(rs);
        }

        data = new int[size];
        result = new HttpStatusClass[size];
        initBenchmarkDistributedData(data, random);
    }

    @SuppressJava6Requirement(reason = "suppress")
    private void initEqualDistributedData_arrayIndexWithFastDiv(int[] setUpData, SplittableRandom random) {
        int INFORMATIONAL_count = 0, SUCCESS_count = 0, REDIRECTION_count = 0, CLIENT_ERROR_count = 0,
                SERVER_ERROR_count = 0, UNKNOWN_count = 0;
        for (int i = 0; i < setUpData.length; i++) {
            setUpData[i] = random.nextInt(0, 2);
        }
        for (int i = 0; i < setUpData.length; i++) {
            // Code needs to be UNKNOWN
            if (setUpData[i] == 0) {
                // The code random range: [Integer.MIN_VALUE, 100) and [600, Integer.MAX_VALUE)
                int code = random.nextInt();
                // while not UNKNOWN, keep loop
                while (!HttpStatusClass.UNKNOWN.contains(code)) {
                    // until code is UNKNOWN
                    code = random.nextInt();
                }
                setUpData[i] = code;
                ++ UNKNOWN_count;
                continue;
            }
            // The code random range: [100, 600)
            int code = random.nextInt(100, 600);
            setUpData[i] = code;
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
        }
        // Print the percentage of each code type:
        System.out.println("\ninitEqualDistributedData_arrayIndexWithFastDiv===>"
                +"INFORMATIONAL:" + df.format((INFORMATIONAL_count * 100.0f) / setUpData.length)
                + "%, SUCCESS:" + df.format((SUCCESS_count * 100.0f) / setUpData.length)
                + "%, REDIRECTION:" + df.format((REDIRECTION_count * 100.0f) / setUpData.length)
                + "%, CLIENT_ERROR:" + df.format((CLIENT_ERROR_count * 100.0f) / setUpData.length)
                + "%, SERVER_ERROR:" + df.format((SERVER_ERROR_count * 100.0f) / setUpData.length)
                + "%, UNKNOWN:" + df.format((UNKNOWN_count * 100.0f) / setUpData.length)
        );
    }

    @SuppressJava6Requirement(reason = "suppress")
    private void initEqualDistributedData_switchCaseWithFastDiv(int[] setUpData, SplittableRandom random) {
        int INFORMATIONAL_count = 0, SUCCESS_count = 0, REDIRECTION_count = 0, CLIENT_ERROR_count = 0,
                SERVER_ERROR_count = 0, UNKNOWN_count = 0, NEGATIVE_count = 0;
        for (int i = 0; i < setUpData.length; i++) {
            setUpData[i] = random.nextInt(0, 2);
        }
        for (int i = 0; i < setUpData.length; i++) {
            // Code needs to be negative
            if (setUpData[i] == 0) {
                // code range: [Integer.MIN_VALUE, 0)
                setUpData[i] = random.nextInt(Integer.MIN_VALUE, 0);
                ++ NEGATIVE_count;
                continue;
            }
            // The code random range: [100, 700)
            int code = random.nextInt(100, 700);
            // If code is 'UNKNOWN'
            if (code >= 600) {
                int unknownCode;
                // The 'UNKNOWN' code random range: [0, 100) and [600, Integer.MAX_VALUE)
                do {
                    unknownCode = random.nextInt(0, Integer.MAX_VALUE);
                } while (unknownCode >= 100 && unknownCode < 600);
                code = unknownCode;
            }
            setUpData[i] = code;
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
        System.out.println("\ninitEqualDistributedData_switchCaseWithFastDiv===>"
                +"INFORMATIONAL:" + df.format((INFORMATIONAL_count * 100.0f) / setUpData.length)
                + "%, SUCCESS:" + df.format((SUCCESS_count * 100.0f) / setUpData.length)
                + "%, REDIRECTION:" + df.format((REDIRECTION_count * 100.0f) / setUpData.length)
                + "%, CLIENT_ERROR:" + df.format((CLIENT_ERROR_count * 100.0f) / setUpData.length)
                + "%, SERVER_ERROR:" + df.format((SERVER_ERROR_count * 100.0f) / setUpData.length)
                + "%, UNKNOWN:" + df.format((UNKNOWN_count * 100.0f) / setUpData.length)
                + "%, Negative:" + df.format((NEGATIVE_count * 100.0f) / setUpData.length)
        );
    }

    @SuppressJava6Requirement(reason = "suppress")
    private void initEqualDistributedData(int[] setUpData, SplittableRandom random) {
        int INFORMATIONAL_count = 0, SUCCESS_count = 0, REDIRECTION_count = 0, CLIENT_ERROR_count = 0,
                SERVER_ERROR_count = 0, UNKNOWN_count = 0;
        for (int i = 0; i < setUpData.length; i++) {
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
            setUpData[i] = code;
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
        System.out.println("\ninitEqualDistributedData===>"
                +"INFORMATIONAL:" + df.format((INFORMATIONAL_count * 100.0f) / setUpData.length)
                + "%, SUCCESS:" + df.format((SUCCESS_count * 100.0f) / setUpData.length)
                + "%, REDIRECTION:" + df.format((REDIRECTION_count * 100.0f) / setUpData.length)
                + "%, CLIENT_ERROR:" + df.format((CLIENT_ERROR_count * 100.0f) / setUpData.length)
                + "%, SERVER_ERROR:" + df.format((SERVER_ERROR_count * 100.0f) / setUpData.length)
                + "%, UNKNOWN:" + df.format((UNKNOWN_count * 100.0f) / setUpData.length));
    }

    /**
     * Http code distribution:
     * INFORMATIONAL:35%, SUCCESS:24%, REDIRECTION:19%, CLIENT_ERROR:14%, SERVER_ERROR:6%, UNKNOWN:2%
     * This distribution is optimized for 'if else' code of 4.1 branch.
     */
    @SuppressJava6Requirement(reason = "suppress")
    private void initBenchmarkDistributedData(int[] setUpData, SplittableRandom random) {
        int INFORMATIONAL_count = 0, SUCCESS_count = 0, REDIRECTION_count = 0, CLIENT_ERROR_count = 0,
                SERVER_ERROR_count = 0, UNKNOWN_count = 0;

        int informational_CodeCount = (int) (setUpData.length * 0.35);
        int success_CodeCount = (int) (setUpData.length * 0.24);
        int redirection_CodeCount = (int) (setUpData.length * 0.19);
        int clientError_CodeCount = (int) (setUpData.length * 0.14);
        int serverError_CodeCount = (int) (setUpData.length * 0.06);
        int unknown_CodeCount = (int) (setUpData.length * 0.02);

        int i = 0;
        while (i < setUpData.length &&
                (informational_CodeCount + success_CodeCount + redirection_CodeCount +
                clientError_CodeCount + serverError_CodeCount + unknown_CodeCount) > 0) {
            int code = random.nextInt(100, 700);
            if (HttpStatusClass.INFORMATIONAL.contains(code) && informational_CodeCount-- > 0) {
                setUpData[i++] = code;
                ++INFORMATIONAL_count;
                continue;
            }
            if (HttpStatusClass.SUCCESS.contains(code) && success_CodeCount-- > 0) {
                setUpData[i++] = code;
                ++SUCCESS_count;
                continue;
            }
            if (HttpStatusClass.REDIRECTION.contains(code) && redirection_CodeCount-- > 0) {
                setUpData[i++] = code;
                ++REDIRECTION_count;
                continue;
            }
            if (HttpStatusClass.CLIENT_ERROR.contains(code) && clientError_CodeCount-- > 0) {
                setUpData[i++] = code;
                ++CLIENT_ERROR_count;
            }
            if (HttpStatusClass.SERVER_ERROR.contains(code) && serverError_CodeCount-- > 0) {
                setUpData[i++] = code;
                ++SERVER_ERROR_count;
                continue;
            }
            if (HttpStatusClass.UNKNOWN.contains(code) && unknown_CodeCount-- > 0) {
                setUpData[i++] = code;
                ++UNKNOWN_count;
            }
        }
        while (i < setUpData.length) {
            // Fill INFORMATIONAL
            int code = random.nextInt(100, 200);
            setUpData[i++] = code;
            ++ INFORMATIONAL_count;
        }

        for (int j = 0; j < setUpData.length; j++) {
            // The code random range: [100, 700)
            int code = setUpData[j];
            // If code is 'UNKNOWN'
            if (code >= 600) {
                // The 'UNKNOWN' code random range: [Integer.MIN_VALUE, 100) and [600, Integer.MAX_VALUE]
                do {
                    code = random.nextInt();
                } while (code >= 100 && code < 600);
            }
            setUpData[j] = code;
        }
        // Print the percentage of each code type:
        System.out.println("\ninitBenchmarkDistributedData===>"
                +"INFORMATIONAL:" + df.format((INFORMATIONAL_count * 100.0f) / setUpData.length)
                + "%, SUCCESS:" + df.format((SUCCESS_count * 100.0f) / setUpData.length)
                + "%, REDIRECTION:" + df.format((REDIRECTION_count * 100.0f) / setUpData.length)
                + "%, CLIENT_ERROR:" + df.format((CLIENT_ERROR_count * 100.0f) / setUpData.length)
                + "%, SERVER_ERROR:" + df.format((SERVER_ERROR_count * 100.0f) / setUpData.length)
                + "%, UNKNOWN:" + df.format((UNKNOWN_count * 100.0f) / setUpData.length));
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
