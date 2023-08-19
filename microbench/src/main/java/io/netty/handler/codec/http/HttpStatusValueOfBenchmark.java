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
    @Param({"519", "1023", "2059", "3027"})
    public int size;
    public HttpStatusValueOfBenchmark() {
        // disable assertion
        super(true);
    }

    @Setup(Level.Iteration)
    @SuppressJava6Requirement(reason = "suppress")
    public void setup(Blackhole bh) {
        final SplittableRandom random = new SplittableRandom();

        // Equal the branch predictor.
        int equalDistributedArraySize = 16000;
        int[] dataIfElse = new int[equalDistributedArraySize];
        int[] dataSwitchCase = new int[equalDistributedArraySize];
        int[] dataSwitchCaseWithFastDiv = new int[equalDistributedArraySize];
        int[] dataArrayIndexWithFastDiv = new int[equalDistributedArraySize];
        initEqualDistributedData(dataIfElse, random);
        initEqualDistributedData(dataSwitchCase, random);
        initEqualDistributedDataSwitchCaseWithFastDiv(dataSwitchCaseWithFastDiv, random);
        initEqualDistributedDataArrayIndexWithFastDiv(dataArrayIndexWithFastDiv, random);
        for (int i = 0; i < dataIfElse.length; i++) {
            HttpStatusClass rs = HttpStatusClass.valueOf(dataIfElse[i]);
            bh.consume(rs);
        }
        for (int i = 0; i < dataSwitchCase.length; i++) {
            HttpStatusClass rs = HttpStatusClass.valueOfSwitchCase(dataSwitchCase[i]);
            bh.consume(rs);
        }
        for (int i = 0; i < dataSwitchCaseWithFastDiv.length; i++) {
            HttpStatusClass rs = HttpStatusClass.valueOfSwitchCaseWithFastDiv(dataSwitchCaseWithFastDiv[i]);
            bh.consume(rs);
            HttpStatusClass rs2 = HttpStatusClass.valueOfSwitchCaseWithIfLess0(dataSwitchCaseWithFastDiv[i]);
            bh.consume(rs2);
        }
        for (int i = 0; i < dataArrayIndexWithFastDiv.length; i++) {
            HttpStatusClass rs = HttpStatusClass.valueOfArrayIndex(dataArrayIndexWithFastDiv[i]);
            bh.consume(rs);
            HttpStatusClass rs1 = HttpStatusClass.valueOfArrayIndexWithFastDiv(dataArrayIndexWithFastDiv[i]);
            bh.consume(rs1);
        }

        // Generate bench mark data.
        data = new int[size];
        result = new HttpStatusClass[size];
//        initBenchmarkDistributedDataReverse(data, random);
        initBenchmarkDistributedData(data, random);
    }

    @SuppressJava6Requirement(reason = "suppress")
    private void initEqualDistributedDataArrayIndexWithFastDiv(int[] setUpData, SplittableRandom random) {
        int informationalCount = 0, successCount = 0, redirectionCount = 0, clientErrorCount = 0,
            serverErrorCount = 0, unknownCount = 0;
        for (int i = 0; i < setUpData.length; i++) {
            setUpData[i] = random.nextInt(0, 6);
        }
        for (int i = 0; i < setUpData.length; i++) {
            // If the code needs to be 'UNKNOWN'
            int code;
            if (setUpData[i] == 0) {
                do {
                    code = random.nextInt();
                } while (!HttpStatusClass.UNKNOWN.contains(code));
                // The code random range: [Integer.MIN_VALUE, 100) and [600, Integer.MAX_VALUE)
                setUpData[i] = code;
                ++unknownCount;
                continue;
            }
            // The code random range: [100, 600)
            code = random.nextInt(100, 600);
            setUpData[i] = code;
            if (HttpStatusClass.INFORMATIONAL.contains(code)) {
                ++informationalCount;
            }
            if (HttpStatusClass.SUCCESS.contains(code)) {
                ++successCount;
            }
            if (HttpStatusClass.REDIRECTION.contains(code)) {
                ++redirectionCount;
            }
            if (HttpStatusClass.CLIENT_ERROR.contains(code)) {
                ++clientErrorCount;
            }
            if (HttpStatusClass.SERVER_ERROR.contains(code)) {
                ++serverErrorCount;
            }
        }
        // Print the percentage of each code type:
        DecimalFormat df = new DecimalFormat("#.00");
        System.out.println("\ninitEqualDistributedDataArrayIndexWithFastDiv===>"
                +"INFORMATIONAL:" + df.format((informationalCount * 100.0f) / setUpData.length)
                + "%, SUCCESS:" + df.format((successCount * 100.0f) / setUpData.length)
                + "%, REDIRECTION:" + df.format((redirectionCount * 100.0f) / setUpData.length)
                + "%, CLIENT_ERROR:" + df.format((clientErrorCount * 100.0f) / setUpData.length)
                + "%, SERVER_ERROR:" + df.format((serverErrorCount * 100.0f) / setUpData.length)
                + "%, UNKNOWN:" + df.format((unknownCount * 100.0f) / setUpData.length)
                + "%");
    }

    @SuppressJava6Requirement(reason = "suppress")
    private void initEqualDistributedDataSwitchCaseWithFastDiv(int[] setUpData, SplittableRandom random) {
        int informationalCount = 0, successCount = 0, redirectionCount = 0, clientErrorCount = 0,
            serverErrorCount = 0, unknownCount = 0, negativeCount = 0;
        for (int i = 0; i < setUpData.length; i++) {
            setUpData[i] = random.nextInt(0, 7);
        }
        for (int i = 0; i < setUpData.length; i++) {
            // Code needs to be negative
            if (setUpData[i] == 0) {
                // code range: [Integer.MIN_VALUE, 0)
                setUpData[i] = random.nextInt(Integer.MIN_VALUE, 0);
                ++negativeCount;
                continue;
            }
            // The code random range: [100, 700)
            int code = random.nextInt(100, 700);
            // If code is 'UNKNOWN'
            if (code >= 600) {
                // The 'UNKNOWN' code random range: [0, 100) and [600, Integer.MAX_VALUE)
                do {
                    code = random.nextInt(0, Integer.MAX_VALUE);
                } while (code >= 100 && code < 600);
            }
            setUpData[i] = code;
            if (HttpStatusClass.INFORMATIONAL.contains(code)) {
                ++informationalCount;
            }
            if (HttpStatusClass.SUCCESS.contains(code)) {
                ++successCount;
            }
            if (HttpStatusClass.REDIRECTION.contains(code)) {
                ++redirectionCount;
            }
            if (HttpStatusClass.CLIENT_ERROR.contains(code)) {
                ++clientErrorCount;
            }
            if (HttpStatusClass.SERVER_ERROR.contains(code)) {
                ++serverErrorCount;
            }
            if (HttpStatusClass.UNKNOWN.contains(code)) {
                ++unknownCount;
            }
        }
        // Print the percentage of each code type:
        DecimalFormat df = new DecimalFormat("#.00");
        System.out.println("\ninitEqualDistributedDataSwitchCaseWithFastDiv===>"
                +"INFORMATIONAL:" + df.format((informationalCount * 100.0f) / setUpData.length)
                + "%, SUCCESS:" + df.format((successCount * 100.0f) / setUpData.length)
                + "%, REDIRECTION:" + df.format((redirectionCount * 100.0f) / setUpData.length)
                + "%, CLIENT_ERROR:" + df.format((clientErrorCount * 100.0f) / setUpData.length)
                + "%, SERVER_ERROR:" + df.format((serverErrorCount * 100.0f) / setUpData.length)
                + "%, UNKNOWN:" + df.format((unknownCount * 100.0f) / setUpData.length)
                + "%, Negative:" + df.format((negativeCount * 100.0f) / setUpData.length)
                + "%");
    }

    @SuppressJava6Requirement(reason = "suppress")
    private void initEqualDistributedData(int[] setUpData, SplittableRandom random) {
        int informationalCount = 0, successCount = 0, redirectionCount = 0, clientErrorCount = 0,
            serverErrorCount = 0, unknownCount = 0;
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
                ++informationalCount;
            }
            if (HttpStatusClass.SUCCESS.contains(code)) {
                ++successCount;
            }
            if (HttpStatusClass.REDIRECTION.contains(code)) {
                ++redirectionCount;
            }
            if (HttpStatusClass.CLIENT_ERROR.contains(code)) {
                ++clientErrorCount;
            }
            if (HttpStatusClass.SERVER_ERROR.contains(code)) {
                ++serverErrorCount;
            }
            if (HttpStatusClass.UNKNOWN.contains(code)) {
                ++unknownCount;
            }
        }
        // Print the percentage of each code type:
        DecimalFormat df = new DecimalFormat("#.00");
        System.out.println("\ninitEqualDistributedData===>"
                +"INFORMATIONAL:" + df.format((informationalCount * 100.0f) / setUpData.length)
                + "%, SUCCESS:" + df.format((successCount * 100.0f) / setUpData.length)
                + "%, REDIRECTION:" + df.format((redirectionCount * 100.0f) / setUpData.length)
                + "%, CLIENT_ERROR:" + df.format((clientErrorCount * 100.0f) / setUpData.length)
                + "%, SERVER_ERROR:" + df.format((serverErrorCount * 100.0f) / setUpData.length)
                + "%, UNKNOWN:" + df.format((unknownCount * 100.0f) / setUpData.length)
                + "%");
    }

    /**
     * Http code distribution:
     * INFORMATIONAL:35%, SUCCESS:24%, REDIRECTION:19%, CLIENT_ERROR:14%, SERVER_ERROR:6%, UNKNOWN:2%
     * This distribution is optimized for 'if else' code of 4.1 branch.
     */
    @SuppressJava6Requirement(reason = "suppress")
    private void initBenchmarkDistributedData(int[] setUpData, SplittableRandom random) {
        int informationalCount = 0, successCount = 0, redirectionCount = 0, clientErrorCount = 0,
            serverErrorCount = 0, unknownCount = 0;

        int totalCount = 0;
        int informationalDistributeCount = (int) (setUpData.length * 0.35);
        totalCount += informationalDistributeCount;
        int successDistributeCount = (int) (setUpData.length * 0.24);
        totalCount += successDistributeCount;
        int redirectionDistributeCount = (int) (setUpData.length * 0.19);
        totalCount += redirectionDistributeCount;
        int clientErrorDistributeCount = (int) (setUpData.length * 0.14);
        totalCount += clientErrorDistributeCount;
        int serverErrorDistributeCount = (int) (setUpData.length * 0.06);
        totalCount += serverErrorDistributeCount;
        int unknownDistributeCount = (int) (setUpData.length * 0.02);
        totalCount += unknownDistributeCount;

        if (totalCount < setUpData.length) {
            informationalDistributeCount += setUpData.length - totalCount;
        }

        for (int i = 0; i < setUpData.length;) {
            int code = random.nextInt(100, 700);
            if (HttpStatusClass.INFORMATIONAL.contains(code) && informationalDistributeCount-- > 0) {
                setUpData[i++] = code;
                ++informationalCount;
                continue;
            }
            if (HttpStatusClass.SUCCESS.contains(code) && successDistributeCount-- > 0) {
                setUpData[i++] = code;
                ++successCount;
                continue;
            }
            if (HttpStatusClass.REDIRECTION.contains(code) && redirectionDistributeCount-- > 0) {
                setUpData[i++] = code;
                ++redirectionCount;
                continue;
            }
            if (HttpStatusClass.CLIENT_ERROR.contains(code) && clientErrorDistributeCount-- > 0) {
                setUpData[i++] = code;
                ++clientErrorCount;
            }
            if (HttpStatusClass.SERVER_ERROR.contains(code) && serverErrorDistributeCount-- > 0) {
                setUpData[i++] = code;
                ++serverErrorCount;
                continue;
            }
            if (HttpStatusClass.UNKNOWN.contains(code) && unknownDistributeCount-- > 0) {
                setUpData[i++] = code;
                ++unknownCount;
            }
        }

        for (int j = 0; j < setUpData.length; j++) {
            // The code random range: [100, 700)
            int code = setUpData[j];
            // If code is 'UNKNOWN'
            if (code >= 600) {
                do {
                    // The 'UNKNOWN' code random range: [Integer.MIN_VALUE, 100) and [600, Integer.MAX_VALUE]
                    code = random.nextInt();
                } while (code >= 100 && code < 600);
            }
            setUpData[j] = code;
        }
        // Print the percentage of each code type:
        DecimalFormat df = new DecimalFormat("#.00");
        System.out.println("\ninitBenchmarkDistributedData===>"
                +"INFORMATIONAL:" + df.format((informationalCount * 100.0f) / setUpData.length)
                + "%, SUCCESS:" + df.format((successCount * 100.0f) / setUpData.length)
                + "%, REDIRECTION:" + df.format((redirectionCount * 100.0f) / setUpData.length)
                + "%, CLIENT_ERROR:" + df.format((clientErrorCount * 100.0f) / setUpData.length)
                + "%, SERVER_ERROR:" + df.format((serverErrorCount * 100.0f) / setUpData.length)
                + "%, UNKNOWN:" + df.format((unknownCount * 100.0f) / setUpData.length)
                + "%");
    }

    /**
     * Http code distribution:
     * INFORMATIONAL:2%, SUCCESS:6%, REDIRECTION:14%, CLIENT_ERROR:19%, SERVER_ERROR:24%, UNKNOWN:35%
     * This distribution is reversed for 'if else' code of 4.1 branch.
     */
    @SuppressJava6Requirement(reason = "suppress")
    private void initBenchmarkDistributedDataReverse(int[] setUpData, SplittableRandom random) {
        int informationalCount = 0, successCount = 0, redirectionCount = 0, clientErrorCount = 0,
                serverErrorCount = 0, unknownCount = 0;

        int totalCount = 0;
        int informationalDistributeCount = (int) (setUpData.length * 0.02);
        totalCount += informationalDistributeCount;
        int successDistributeCount = (int) (setUpData.length * 0.06);
        totalCount += successDistributeCount;
        int redirectionDistributeCount = (int) (setUpData.length * 0.14);
        totalCount += redirectionDistributeCount;
        int clientErrorDistributeCount = (int) (setUpData.length * 0.19);
        totalCount += clientErrorDistributeCount;
        int serverErrorDistributeCount = (int) (setUpData.length * 0.24);
        totalCount += serverErrorDistributeCount;
        int unknownDistributeCount = (int) (setUpData.length * 0.35);
        totalCount += unknownDistributeCount;

        if (totalCount < setUpData.length) {
            serverErrorDistributeCount += setUpData.length - totalCount;
        }

        for (int i = 0; i < setUpData.length;) {
            int code = random.nextInt(100, 700);
            if (HttpStatusClass.INFORMATIONAL.contains(code) && informationalDistributeCount-- > 0) {
                setUpData[i++] = code;
                ++informationalCount;
                continue;
            }
            if (HttpStatusClass.SUCCESS.contains(code) && successDistributeCount-- > 0) {
                setUpData[i++] = code;
                ++successCount;
                continue;
            }
            if (HttpStatusClass.REDIRECTION.contains(code) && redirectionDistributeCount-- > 0) {
                setUpData[i++] = code;
                ++redirectionCount;
                continue;
            }
            if (HttpStatusClass.CLIENT_ERROR.contains(code) && clientErrorDistributeCount-- > 0) {
                setUpData[i++] = code;
                ++clientErrorCount;
            }
            if (HttpStatusClass.SERVER_ERROR.contains(code) && serverErrorDistributeCount-- > 0) {
                setUpData[i++] = code;
                ++serverErrorCount;
                continue;
            }
            if (HttpStatusClass.UNKNOWN.contains(code) && unknownDistributeCount-- > 0) {
                setUpData[i++] = code;
                ++unknownCount;
            }
        }

        for (int j = 0; j < setUpData.length; j++) {
            // The code random range: [100, 700)
            int code = setUpData[j];
            // If code is 'UNKNOWN'
            if (code >= 600) {
                do {
                    // The 'UNKNOWN' code random range: [Integer.MIN_VALUE, 100) and [600, Integer.MAX_VALUE]
                    code = random.nextInt();
                } while (code >= 100 && code < 600);
            }
            setUpData[j] = code;
        }
        // Print the percentage of each code type:
        DecimalFormat df = new DecimalFormat("#.00");
        System.out.println("\ninitBenchmarkDistributedDataReverse===>"
                +"INFORMATIONAL:" + df.format((informationalCount * 100.0f) / setUpData.length)
                + "%, SUCCESS:" + df.format((successCount * 100.0f) / setUpData.length)
                + "%, REDIRECTION:" + df.format((redirectionCount * 100.0f) / setUpData.length)
                + "%, CLIENT_ERROR:" + df.format((clientErrorCount * 100.0f) / setUpData.length)
                + "%, SERVER_ERROR:" + df.format((serverErrorCount * 100.0f) / setUpData.length)
                + "%, UNKNOWN:" + df.format((unknownCount * 100.0f) / setUpData.length)
                + "%");
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
    public HttpStatusClass[] valueOfSwitchCaseWithIfLess0() {
        for (int i = 0; i < size; ++i) {
            result[i] = HttpStatusClass.valueOfSwitchCaseWithIfLess0(data[i]);
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
    public HttpStatusClass[] valueOfArrayIndex() {
        for (int i = 0; i < size; ++i) {
            result[i] = HttpStatusClass.valueOfArrayIndex(data[i]);
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
