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

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.SplittableRandom;
import java.util.concurrent.TimeUnit;

@BenchmarkMode(Mode.Throughput)
@Warmup(iterations = 1, time = 1)
@Measurement(iterations = 1, time = 1)
@OutputTimeUnit(TimeUnit.MICROSECONDS)
public class HttpStatusValueOfBenchmark extends AbstractMicrobenchmark {
    private int[] data;
    private int[] data5Codes;
    private HttpStatusClass[] result;
    @Param({"519", "1023", "2059", "3027"})
    private int size;
    private final BigDecimal bdZero = new BigDecimal("0.00");
    private final BigDecimal bdOne = new BigDecimal("1.00");
    private final BigDecimal bdLowest = new BigDecimal("0.01");
    private final DecimalFormat df = new DecimalFormat("##.##%");
    public HttpStatusValueOfBenchmark() {
        // disable assertion
        super(true);
    }

    @Setup(Level.Iteration)
    @SuppressJava6Requirement(reason = "suppress")
    public void setup(Blackhole bh) {
        if (size < 100) {
            throw new IllegalArgumentException("The size MUST > 100");
        }
        final SplittableRandom random = new SplittableRandom();
        // Equal the branch predictor.
        int equalDistributedArraySize = 16000;
        int[] dataIfElse = new int[equalDistributedArraySize];
        int[] dataSwitchCase = new int[equalDistributedArraySize];
        int[] dataSwitchCaseWithFastDiv = new int[equalDistributedArraySize];
        int[] dataArrayIndexOnly5Codes = new int[equalDistributedArraySize];
        int[] dataArrayIndexWithFastDiv = new int[equalDistributedArraySize];

        initDistributedData("dataIfElse", dataIfElse, random, 0.166, 0.166, 0.166,
                0.166, 0.166, 0.166, 0.0);

        initDistributedData("dataSwitchCase", dataSwitchCase, random, 0.166, 0.166, 0.166,
                0.166, 0.166, 0.166, 0.0);

        initDistributedData("dataSwitchCaseWithFastDiv", dataSwitchCaseWithFastDiv, random, 0.142, 0.142, 0.142,
                0.142, 0.142, 0.142, 0.142);

        initDistributedData("dataArrayIndexOnly5Codes", dataArrayIndexOnly5Codes, random, 0.2, 0.2,
                0.2, 0.2, 0.2, 0.0, 0.0);

        initDistributedData("dataArrayIndexWithFastDiv", dataArrayIndexWithFastDiv, random, 0.166, 0.166, 0.166,
                0.166, 0.166, 0.166, 0.0);
        for (int i = 0; i < equalDistributedArraySize; i++) {
            HttpStatusClass rs1 = HttpStatusClass.valueOf(dataIfElse[i]);
            bh.consume(rs1);
            HttpStatusClass rs2 = HttpStatusClass.valueOfSwitchCase(dataSwitchCase[i]);
            bh.consume(rs2);
            HttpStatusClass rs3 = HttpStatusClass.valueOfSwitchCaseWithFastDiv(dataSwitchCaseWithFastDiv[i]);
            bh.consume(rs3);
            HttpStatusClass rs4 = HttpStatusClass.valueOfSwitchCaseWithIfLess0(dataSwitchCaseWithFastDiv[i]);
            bh.consume(rs4);
            HttpStatusClass rs5 = HttpStatusClass.valueOfArrayIndex(dataArrayIndexWithFastDiv[i]);
            bh.consume(rs5);
            HttpStatusClass rs6 = HttpStatusClass.valueOfArrayIndexOnly5Codes(dataArrayIndexOnly5Codes[i]);
            bh.consume(rs6);
            HttpStatusClass rs7 = HttpStatusClass.valueOfArrayIndexWithFastDiv(dataArrayIndexWithFastDiv[i]);
            bh.consume(rs7);
        }

        data = new int[size];
        data5Codes = new int[size];
        result = new HttpStatusClass[size];
        // Generate bench mark data.

        initDistributedData("data", data, random, 0.38, 0.30, 0.15,
                0.10, 0.05, 0.02, 0.0);

        initDistributedData("data5Codes", data5Codes, random, 0.38, 0.32, 0.15,
                0.10, 0.05, 0.0, 0.0);
    }

    @SuppressJava6Requirement(reason = "suppress")
    private void initDistributedData(String desc, int[] setUpData, SplittableRandom random, double informationalRatio,
                                     double successRatio, double redirectionRatio, double clientErrorRatio,
                                     double serverErrorRatio, double unknownRatio, double negativeRatio) {
        BigDecimal[] bdArray = {
                BigDecimal.valueOf(informationalRatio),
                BigDecimal.valueOf(successRatio),
                BigDecimal.valueOf(redirectionRatio),
                BigDecimal.valueOf(clientErrorRatio),
                BigDecimal.valueOf(serverErrorRatio),
                BigDecimal.valueOf(unknownRatio),
                BigDecimal.valueOf(negativeRatio)
        };
        BigDecimal bdSum = new BigDecimal("0.00");
        for (BigDecimal bdParam : bdArray) {
            if (bdParam.compareTo(bdZero) < 0) {
                throw new IllegalArgumentException("Ratio MUST NOT negative");
            }
            if (bdParam.compareTo(bdZero) > 0 && bdParam.compareTo(bdLowest) < 0) {
                throw new IllegalArgumentException("If ratio != 0, then the ratio MUST >= 0.01");
            }
            bdSum = bdSum.add(bdParam);
        }
        if (bdSum.compareTo(bdOne) > 0) {
            throw new IllegalArgumentException("Sum of ratios MUST <= 1");
        }

        int totalCount = 0;
        int informationalCount = (int) (setUpData.length * informationalRatio);
        totalCount += informationalCount;
        int successCount = (int) (setUpData.length * successRatio);
        totalCount += successCount;
        int redirectionCount = (int) (setUpData.length * redirectionRatio);
        totalCount += redirectionCount;
        int clientErrorCount = (int) (setUpData.length * clientErrorRatio);
        totalCount += clientErrorCount;
        int serverErrorCount = (int) (setUpData.length * serverErrorRatio);
        totalCount += serverErrorCount;
        int unknownCount = (int) (setUpData.length * unknownRatio);
        totalCount += unknownCount;
        int negativeCount = (int) (setUpData.length * negativeRatio);
        totalCount += negativeCount;

        double c1x = 0, c2x = 0, c3x = 0, c4x = 0, c5x = 0, c6x = 0, c7x = 0;
        for (int i = 0; i < totalCount;) {
            // INFORMATIONAL:[100,200); SUCCESS:[200,300); REDIRECTION:[300,400);
            // CLIENT_ERROR:[400,500); SERVER_ERROR:[500,600); UNKNOWN:[600,700); Negative:[700,800)
            int code = random.nextInt(100, 800);
            if (HttpStatusClass.INFORMATIONAL.contains(code) && informationalCount-- > 0) {
                setUpData[i++] = code;
                ++c1x;
            }
            if (HttpStatusClass.SUCCESS.contains(code) && successCount-- > 0) {
                setUpData[i++] = code;
                ++c2x;
            }
            if (HttpStatusClass.REDIRECTION.contains(code) && redirectionCount-- > 0) {
                setUpData[i++] = code;
                ++c3x;
            }
            if (HttpStatusClass.CLIENT_ERROR.contains(code) && clientErrorCount-- > 0) {
                setUpData[i++] = code;
                ++c4x;
            }
            if (HttpStatusClass.SERVER_ERROR.contains(code) && serverErrorCount-- > 0) {
                setUpData[i++] = code;
                ++c5x;
            }
            // UNKNOWN code:
            if (code >= 600 && code < 700 && unknownCount-- > 0) {
                int origin = BigDecimal.valueOf(negativeRatio).compareTo(bdZero) > 0 ? 0 : Integer.MIN_VALUE;
                // Generate 'UNKNOWN' code.
                do {
                    code = random.nextInt(origin, Integer.MAX_VALUE);
                } while (code >= 100 && code < 600);
                setUpData[i++] = code;
                ++c6x;
            }
            // Negative code:
            if (code >= 700 && negativeCount-- > 0) {
                code = random.nextInt(Integer.MIN_VALUE, 0);
                setUpData[i++] = code;
                ++c7x;
            }
        }

        for (int i = (totalCount - 1); i < setUpData.length; i++) {
            // Generate gap elements from 1xx to 5xx
            int code = random.nextInt(100, 600);
            setUpData[i] = code;
            if (HttpStatusClass.INFORMATIONAL.contains(code)) {
                ++c1x;
            }
            if (HttpStatusClass.SUCCESS.contains(code)) {
                ++c2x;
            }
            if (HttpStatusClass.REDIRECTION.contains(code)) {
                ++c3x;
            }
            if (HttpStatusClass.CLIENT_ERROR.contains(code)) {
                ++c4x;
            }
            if (HttpStatusClass.SERVER_ERROR.contains(code)) {
                ++c5x;
            }
        }

        // Print the percentage of each code type:
        System.out.println("\n" + desc + "===>"
                +"INFORMATIONAL:" + df.format(c1x / setUpData.length)
                + ", SUCCESS:" + df.format(c2x / setUpData.length)
                + ", REDIRECTION:" + df.format(c3x / setUpData.length)
                + ", CLIENT_ERROR:" + df.format(c4x / setUpData.length)
                + ", SERVER_ERROR:" + df.format(c5x / setUpData.length)
                + ", UNKNOWN:" + df.format(c6x / setUpData.length)
                + ", NEGATIVE:" + df.format(c7x / setUpData.length)
                );
    }

    @SuppressJava6Requirement(reason = "suppress")
//    private void initEqualDistributedDataArrayIndexWithFastDiv(int[] setUpData, SplittableRandom random) {
//        int informationalCount = 0, successCount = 0, redirectionCount = 0, clientErrorCount = 0,
//            serverErrorCount = 0, unknownCount = 0;
//        for (int i = 0; i < setUpData.length; i++) {
//            setUpData[i] = random.nextInt(0, 6);
//        }
//        for (int i = 0; i < setUpData.length; i++) {
//            // If the code needs to be 'UNKNOWN'
//            int code;
//            if (setUpData[i] == 0) {
//                do {
//                    code = random.nextInt();
//                } while (!HttpStatusClass.UNKNOWN.contains(code));
//                // The code random range: [Integer.MIN_VALUE, 100) and [600, Integer.MAX_VALUE)
//                setUpData[i] = code;
//                ++unknownCount;
//                continue;
//            }
//            // The code random range: [100, 600)
//            code = random.nextInt(100, 600);
//            setUpData[i] = code;
//            if (HttpStatusClass.INFORMATIONAL.contains(code)) {
//                ++informationalCount;
//            }
//            if (HttpStatusClass.SUCCESS.contains(code)) {
//                ++successCount;
//            }
//            if (HttpStatusClass.REDIRECTION.contains(code)) {
//                ++redirectionCount;
//            }
//            if (HttpStatusClass.CLIENT_ERROR.contains(code)) {
//                ++clientErrorCount;
//            }
//            if (HttpStatusClass.SERVER_ERROR.contains(code)) {
//                ++serverErrorCount;
//            }
//        }
//        // Print the percentage of each code type:
//        DecimalFormat df = new DecimalFormat("#.00");
//        System.out.println("\ninitEqualDistributedDataArrayIndexWithFastDiv===>"
//                +"INFORMATIONAL:" + df.format((informationalCount * 100.0f) / setUpData.length)
//                + "%, SUCCESS:" + df.format((successCount * 100.0f) / setUpData.length)
//                + "%, REDIRECTION:" + df.format((redirectionCount * 100.0f) / setUpData.length)
//                + "%, CLIENT_ERROR:" + df.format((clientErrorCount * 100.0f) / setUpData.length)
//                + "%, SERVER_ERROR:" + df.format((serverErrorCount * 100.0f) / setUpData.length)
//                + "%, UNKNOWN:" + df.format((unknownCount * 100.0f) / setUpData.length)
//                + "%");
//    }
//
//    @SuppressJava6Requirement(reason = "suppress")
//    private void initEqualDistributedDataArrayIndexOnly5Codes(int[] setUpData, SplittableRandom random) {
//        int informationalCount = 0, successCount = 0, redirectionCount = 0, clientErrorCount = 0,
//                serverErrorCount = 0, unknownCount = 0;
//        for (int i = 0; i < setUpData.length; i++) {
//            // The code random range: [100, 600)
//            int code = random.nextInt(100, 600);
//            setUpData[i] = code;
//            if (HttpStatusClass.INFORMATIONAL.contains(code)) {
//                ++informationalCount;
//            }
//            if (HttpStatusClass.SUCCESS.contains(code)) {
//                ++successCount;
//            }
//            if (HttpStatusClass.REDIRECTION.contains(code)) {
//                ++redirectionCount;
//            }
//            if (HttpStatusClass.CLIENT_ERROR.contains(code)) {
//                ++clientErrorCount;
//            }
//            if (HttpStatusClass.SERVER_ERROR.contains(code)) {
//                ++serverErrorCount;
//            }
//        }
//        // Print the percentage of each code type:
//        DecimalFormat df = new DecimalFormat("#.00");
//        System.out.println("\ninitEqualDistributedDataArrayIndexOnly5Codes===>"
//                +"INFORMATIONAL:" + df.format((informationalCount * 100.0f) / setUpData.length)
//                + "%, SUCCESS:" + df.format((successCount * 100.0f) / setUpData.length)
//                + "%, REDIRECTION:" + df.format((redirectionCount * 100.0f) / setUpData.length)
//                + "%, CLIENT_ERROR:" + df.format((clientErrorCount * 100.0f) / setUpData.length)
//                + "%, SERVER_ERROR:" + df.format((serverErrorCount * 100.0f) / setUpData.length)
//                + "%, UNKNOWN:" + df.format((unknownCount * 100.0f) / setUpData.length)
//                + "%");
//    }
//
//    @SuppressJava6Requirement(reason = "suppress")
//    private void initEqualDistributedDataSwitchCaseWithFastDiv(int[] setUpData, SplittableRandom random) {
//        int informationalCount = 0, successCount = 0, redirectionCount = 0, clientErrorCount = 0,
//            serverErrorCount = 0, unknownCount = 0, negativeCount = 0;
//        for (int i = 0; i < setUpData.length; i++) {
//            setUpData[i] = random.nextInt(0, 7);
//        }
//        for (int i = 0; i < setUpData.length; i++) {
//            // Code needs to be negative
//            if (setUpData[i] == 0) {
//                // code range: [Integer.MIN_VALUE, 0)
//                setUpData[i] = random.nextInt(Integer.MIN_VALUE, 0);
//                ++negativeCount;
//                continue;
//            }
//            // The code random range: [100, 700)
//            int code = random.nextInt(100, 700);
//            // If code is 'UNKNOWN'
//            if (code >= 600) {
//                // The 'UNKNOWN' code random range: [0, 100) and [600, Integer.MAX_VALUE)
//                do {
//                    code = random.nextInt(0, Integer.MAX_VALUE);
//                } while (code >= 100 && code < 600);
//            }
//            setUpData[i] = code;
//            if (HttpStatusClass.INFORMATIONAL.contains(code)) {
//                ++informationalCount;
//            }
//            if (HttpStatusClass.SUCCESS.contains(code)) {
//                ++successCount;
//            }
//            if (HttpStatusClass.REDIRECTION.contains(code)) {
//                ++redirectionCount;
//            }
//            if (HttpStatusClass.CLIENT_ERROR.contains(code)) {
//                ++clientErrorCount;
//            }
//            if (HttpStatusClass.SERVER_ERROR.contains(code)) {
//                ++serverErrorCount;
//            }
//            if (HttpStatusClass.UNKNOWN.contains(code)) {
//                ++unknownCount;
//            }
//        }
//        // Print the percentage of each code type:
//        DecimalFormat df = new DecimalFormat("#.00");
//        System.out.println("\ninitEqualDistributedDataSwitchCaseWithFastDiv===>"
//                +"INFORMATIONAL:" + df.format((informationalCount * 100.0f) / setUpData.length)
//                + "%, SUCCESS:" + df.format((successCount * 100.0f) / setUpData.length)
//                + "%, REDIRECTION:" + df.format((redirectionCount * 100.0f) / setUpData.length)
//                + "%, CLIENT_ERROR:" + df.format((clientErrorCount * 100.0f) / setUpData.length)
//                + "%, SERVER_ERROR:" + df.format((serverErrorCount * 100.0f) / setUpData.length)
//                + "%, UNKNOWN:" + df.format((unknownCount * 100.0f) / setUpData.length)
//                + "%, Negative:" + df.format((negativeCount * 100.0f) / setUpData.length)
//                + "%");
//    }
//
//    @SuppressJava6Requirement(reason = "suppress")
//    private void initEqualDistributedData(int[] setUpData, SplittableRandom random) {
//        int informationalCount = 0, successCount = 0, redirectionCount = 0, clientErrorCount = 0,
//            serverErrorCount = 0, unknownCount = 0;
//        for (int i = 0; i < setUpData.length; i++) {
//            // The code random range: [100, 700)
//            int code = random.nextInt(100, 700);
//            // If code is 'UNKNOWN'
//            if (code >= 600) {
//                int unknownCode;
//                // The 'UNKNOWN' code random range: [Integer.MIN_VALUE, 100) and [600, Integer.MAX_VALUE]
//                do {
//                    unknownCode = random.nextInt();
//                } while (unknownCode >= 100 && unknownCode < 600);
//                code = unknownCode;
//            }
//            setUpData[i] = code;
//            if (HttpStatusClass.INFORMATIONAL.contains(code)) {
//                ++informationalCount;
//            }
//            if (HttpStatusClass.SUCCESS.contains(code)) {
//                ++successCount;
//            }
//            if (HttpStatusClass.REDIRECTION.contains(code)) {
//                ++redirectionCount;
//            }
//            if (HttpStatusClass.CLIENT_ERROR.contains(code)) {
//                ++clientErrorCount;
//            }
//            if (HttpStatusClass.SERVER_ERROR.contains(code)) {
//                ++serverErrorCount;
//            }
//            if (HttpStatusClass.UNKNOWN.contains(code)) {
//                ++unknownCount;
//            }
//        }
//        // Print the percentage of each code type:
//        DecimalFormat df = new DecimalFormat("#.00");
//        System.out.println("\ninitEqualDistributedData===>"
//                +"INFORMATIONAL:" + df.format((informationalCount * 100.0f) / setUpData.length)
//                + "%, SUCCESS:" + df.format((successCount * 100.0f) / setUpData.length)
//                + "%, REDIRECTION:" + df.format((redirectionCount * 100.0f) / setUpData.length)
//                + "%, CLIENT_ERROR:" + df.format((clientErrorCount * 100.0f) / setUpData.length)
//                + "%, SERVER_ERROR:" + df.format((serverErrorCount * 100.0f) / setUpData.length)
//                + "%, UNKNOWN:" + df.format((unknownCount * 100.0f) / setUpData.length)
//                + "%");
//    }
//
//    /**
//     * Http code distribution:
//     * INFORMATIONAL:35%, SUCCESS:24%, REDIRECTION:19%, CLIENT_ERROR:14%, SERVER_ERROR:6%, UNKNOWN:2%
//     * This distribution is optimized for 'if else' code of 4.1 branch.
//     */
//    @SuppressJava6Requirement(reason = "suppress")
//    private void initBenchmarkDistributedData(int[] setUpData, SplittableRandom random) {
//        int informationalCount = 0, successCount = 0, redirectionCount = 0, clientErrorCount = 0,
//            serverErrorCount = 0, unknownCount = 0;
//
//        int totalCount = 0;
//        int informationalDistributeCount = (int) (setUpData.length * 0.35d);
//        totalCount += informationalDistributeCount;
//        int successDistributeCount = (int) (setUpData.length * 0.24);
//        totalCount += successDistributeCount;
//        int redirectionDistributeCount = (int) (setUpData.length * 0.19);
//        totalCount += redirectionDistributeCount;
//        int clientErrorDistributeCount = (int) (setUpData.length * 0.14);
//        totalCount += clientErrorDistributeCount;
//        int serverErrorDistributeCount = (int) (setUpData.length * 0.06);
//        totalCount += serverErrorDistributeCount;
//        int unknownDistributeCount = (int) (setUpData.length * 0.02);
//        totalCount += unknownDistributeCount;
//
//        if (totalCount < setUpData.length) {
//            informationalDistributeCount += setUpData.length - totalCount;
//        }
//
//        for (int i = 0; i < setUpData.length;) {
//            int code = random.nextInt(100, 700);
//            if (HttpStatusClass.INFORMATIONAL.contains(code) && informationalDistributeCount-- > 0) {
//                setUpData[i++] = code;
//                ++informationalCount;
//                continue;
//            }
//            if (HttpStatusClass.SUCCESS.contains(code) && successDistributeCount-- > 0) {
//                setUpData[i++] = code;
//                ++successCount;
//                continue;
//            }
//            if (HttpStatusClass.REDIRECTION.contains(code) && redirectionDistributeCount-- > 0) {
//                setUpData[i++] = code;
//                ++redirectionCount;
//                continue;
//            }
//            if (HttpStatusClass.CLIENT_ERROR.contains(code) && clientErrorDistributeCount-- > 0) {
//                setUpData[i++] = code;
//                ++clientErrorCount;
//            }
//            if (HttpStatusClass.SERVER_ERROR.contains(code) && serverErrorDistributeCount-- > 0) {
//                setUpData[i++] = code;
//                ++serverErrorCount;
//                continue;
//            }
//            if (HttpStatusClass.UNKNOWN.contains(code) && unknownDistributeCount-- > 0) {
//                setUpData[i++] = code;
//                ++unknownCount;
//            }
//        }
//
//        for (int j = 0; j < setUpData.length; j++) {
//            // The code random range: [100, 700)
//            int code = setUpData[j];
//            // If code is 'UNKNOWN'
//            if (code >= 600) {
//                do {
//                    // The 'UNKNOWN' code random range: [Integer.MIN_VALUE, 100) and [600, Integer.MAX_VALUE]
//                    code = random.nextInt();
//                } while (code >= 100 && code < 600);
//            }
//            setUpData[j] = code;
//        }
//        // Print the percentage of each code type:
//        DecimalFormat df = new DecimalFormat("#.00");
//        System.out.println("\ninitBenchmarkDistributedData===>"
//                +"INFORMATIONAL:" + df.format((informationalCount * 100.0f) / setUpData.length)
//                + "%, SUCCESS:" + df.format((successCount * 100.0f) / setUpData.length)
//                + "%, REDIRECTION:" + df.format((redirectionCount * 100.0f) / setUpData.length)
//                + "%, CLIENT_ERROR:" + df.format((clientErrorCount * 100.0f) / setUpData.length)
//                + "%, SERVER_ERROR:" + df.format((serverErrorCount * 100.0f) / setUpData.length)
//                + "%, UNKNOWN:" + df.format((unknownCount * 100.0f) / setUpData.length)
//                + "%");
//    }
//
//    @SuppressJava6Requirement(reason = "suppress")
//    private void initBenchmarkDistributedDataFor5Codes(int[] setUpData, SplittableRandom random) {
//        int informationalCount = 0, successCount = 0, redirectionCount = 0, clientErrorCount = 0,
//                serverErrorCount = 0, unknownCount = 0;
//
//        int totalCount = 0;
//        int informationalDistributeCount = (int) (setUpData.length * 0.35);
//        totalCount += informationalDistributeCount;
//        int successDistributeCount = (int) (setUpData.length * 0.24);
//        totalCount += successDistributeCount;
//        int redirectionDistributeCount = (int) (setUpData.length * 0.19);
//        totalCount += redirectionDistributeCount;
//        int clientErrorDistributeCount = (int) (setUpData.length * 0.14);
//        totalCount += clientErrorDistributeCount;
//        int serverErrorDistributeCount = (int) (setUpData.length * 0.08);
//        totalCount += serverErrorDistributeCount;
//
//        if (totalCount < setUpData.length) {
//            informationalDistributeCount += setUpData.length - totalCount;
//        }
//
//        for (int i = 0; i < setUpData.length;) {
//            int code = random.nextInt(100, 600);
//            if (HttpStatusClass.INFORMATIONAL.contains(code) && informationalDistributeCount-- > 0) {
//                setUpData[i++] = code;
//                ++informationalCount;
//                continue;
//            }
//            if (HttpStatusClass.SUCCESS.contains(code) && successDistributeCount-- > 0) {
//                setUpData[i++] = code;
//                ++successCount;
//                continue;
//            }
//            if (HttpStatusClass.REDIRECTION.contains(code) && redirectionDistributeCount-- > 0) {
//                setUpData[i++] = code;
//                ++redirectionCount;
//                continue;
//            }
//            if (HttpStatusClass.CLIENT_ERROR.contains(code) && clientErrorDistributeCount-- > 0) {
//                setUpData[i++] = code;
//                ++clientErrorCount;
//            }
//            if (HttpStatusClass.SERVER_ERROR.contains(code) && serverErrorDistributeCount-- > 0) {
//                setUpData[i++] = code;
//                ++serverErrorCount;
//            }
//        }
//
//        // Print the percentage of each code type:
//        DecimalFormat df = new DecimalFormat("#.00");
//        System.out.println("\ninitBenchmarkDistributedDataFor5Codes===>"
//                +"INFORMATIONAL:" + df.format((informationalCount * 100.0f) / setUpData.length)
//                + "%, SUCCESS:" + df.format((successCount * 100.0f) / setUpData.length)
//                + "%, REDIRECTION:" + df.format((redirectionCount * 100.0f) / setUpData.length)
//                + "%, CLIENT_ERROR:" + df.format((clientErrorCount * 100.0f) / setUpData.length)
//                + "%, SERVER_ERROR:" + df.format((serverErrorCount * 100.0f) / setUpData.length)
//                + "%, UNKNOWN:" + df.format((unknownCount * 100.0f) / setUpData.length)
//                + "%");
//    }
//
//    /**
//     * Http code distribution:
//     * INFORMATIONAL:2%, SUCCESS:6%, REDIRECTION:14%, CLIENT_ERROR:19%, SERVER_ERROR:24%, UNKNOWN:35%
//     * This distribution is reversed for 'if else' code of 4.1 branch.
//     */
//    @SuppressJava6Requirement(reason = "suppress")
//    private void initBenchmarkDistributedDataReverse(int[] setUpData, SplittableRandom random) {
//        int informationalCount = 0, successCount = 0, redirectionCount = 0, clientErrorCount = 0,
//                serverErrorCount = 0, unknownCount = 0;
//
//        int totalCount = 0;
//        int informationalDistributeCount = (int) (setUpData.length * 0.02);
//        totalCount += informationalDistributeCount;
//        int successDistributeCount = (int) (setUpData.length * 0.06);
//        totalCount += successDistributeCount;
//        int redirectionDistributeCount = (int) (setUpData.length * 0.14);
//        totalCount += redirectionDistributeCount;
//        int clientErrorDistributeCount = (int) (setUpData.length * 0.19);
//        totalCount += clientErrorDistributeCount;
//        int serverErrorDistributeCount = (int) (setUpData.length * 0.24);
//        totalCount += serverErrorDistributeCount;
//        int unknownDistributeCount = (int) (setUpData.length * 0.35);
//        totalCount += unknownDistributeCount;
//
//        if (totalCount < setUpData.length) {
//            serverErrorDistributeCount += setUpData.length - totalCount;
//        }
//
//        for (int i = 0; i < setUpData.length;) {
//            int code = random.nextInt(100, 700);
//            if (HttpStatusClass.INFORMATIONAL.contains(code) && informationalDistributeCount-- > 0) {
//                setUpData[i++] = code;
//                ++informationalCount;
//                continue;
//            }
//            if (HttpStatusClass.SUCCESS.contains(code) && successDistributeCount-- > 0) {
//                setUpData[i++] = code;
//                ++successCount;
//                continue;
//            }
//            if (HttpStatusClass.REDIRECTION.contains(code) && redirectionDistributeCount-- > 0) {
//                setUpData[i++] = code;
//                ++redirectionCount;
//                continue;
//            }
//            if (HttpStatusClass.CLIENT_ERROR.contains(code) && clientErrorDistributeCount-- > 0) {
//                setUpData[i++] = code;
//                ++clientErrorCount;
//            }
//            if (HttpStatusClass.SERVER_ERROR.contains(code) && serverErrorDistributeCount-- > 0) {
//                setUpData[i++] = code;
//                ++serverErrorCount;
//                continue;
//            }
//            if (HttpStatusClass.UNKNOWN.contains(code) && unknownDistributeCount-- > 0) {
//                setUpData[i++] = code;
//                ++unknownCount;
//            }
//        }
//
//        for (int j = 0; j < setUpData.length; j++) {
//            // The code random range: [100, 700)
//            int code = setUpData[j];
//            // If code is 'UNKNOWN'
//            if (code >= 600) {
//                do {
//                    // The 'UNKNOWN' code random range: [Integer.MIN_VALUE, 100) and [600, Integer.MAX_VALUE]
//                    code = random.nextInt();
//                } while (code >= 100 && code < 600);
//            }
//            setUpData[j] = code;
//        }
//        // Print the percentage of each code type:
//        DecimalFormat df = new DecimalFormat("#.00");
//        System.out.println("\ninitBenchmarkDistributedDataReverse===>"
//                +"INFORMATIONAL:" + df.format((informationalCount * 100.0f) / setUpData.length)
//                + "%, SUCCESS:" + df.format((successCount * 100.0f) / setUpData.length)
//                + "%, REDIRECTION:" + df.format((redirectionCount * 100.0f) / setUpData.length)
//                + "%, CLIENT_ERROR:" + df.format((clientErrorCount * 100.0f) / setUpData.length)
//                + "%, SERVER_ERROR:" + df.format((serverErrorCount * 100.0f) / setUpData.length)
//                + "%, UNKNOWN:" + df.format((unknownCount * 100.0f) / setUpData.length)
//                + "%");
//    }

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
    public HttpStatusClass[] valueOfArrayIndexOnly5Codes() {
        for (int i = 0; i < size; ++i) {
            result[i] = HttpStatusClass.valueOfArrayIndexOnly5Codes(data5Codes[i]);
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
