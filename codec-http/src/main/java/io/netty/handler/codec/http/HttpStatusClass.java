/*
 * Copyright 2014 The Netty Project
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

import io.netty.util.AsciiString;

/**
 * The class of HTTP status.
 */
public enum HttpStatusClass {
    /**
     * The informational class (1xx)
     */
    INFORMATIONAL(100, 200, "Informational"),
    /**
     * The success class (2xx)
     */
    SUCCESS(200, 300, "Success"),
    /**
     * The redirection class (3xx)
     */
    REDIRECTION(300, 400, "Redirection"),
    /**
     * The client error class (4xx)
     */
    CLIENT_ERROR(400, 500, "Client Error"),
    /**
     * The server error class (5xx)
     */
    SERVER_ERROR(500, 600, "Server Error"),
    /**
     * The unknown class
     */
    UNKNOWN(0, 0, "Unknown Status") {
        @Override
        public boolean contains(int code) {
            return code < 100 || code >= 600;
        }
    };

     private static final HttpStatusClass[] statusArray = new HttpStatusClass[6];

     static {
         statusArray[1] = INFORMATIONAL;
         statusArray[2] = SUCCESS;
         statusArray[3] = REDIRECTION;
         statusArray[4] = CLIENT_ERROR;
         statusArray[5] = SERVER_ERROR;
     }

    /**
     * Returns the class of the specified HTTP status code.
     */
    public static HttpStatusClass valueOf(int code) {
        if (INFORMATIONAL.contains(code)) {
            return INFORMATIONAL;
        }
        if (SUCCESS.contains(code)) {
            return SUCCESS;
        }
        if (REDIRECTION.contains(code)) {
            return REDIRECTION;
        }
        if (CLIENT_ERROR.contains(code)) {
            return CLIENT_ERROR;
        }
        if (SERVER_ERROR.contains(code)) {
            return SERVER_ERROR;
        }
        return UNKNOWN;
    }

    /**
     * This method is for benchmark comparison, will be removed after test done.
     */
    public static HttpStatusClass valueOfSwitchCase(int code) {
        switch (code / 100) {
            // 1xx
            case 1: return INFORMATIONAL;
            // 2xx
            case 2: return SUCCESS;
            // 3xx
            case 3: return REDIRECTION;
            // 4xx
            case 4: return CLIENT_ERROR;
            // 5xx
            case 5: return SERVER_ERROR;
            // others
            default: return UNKNOWN;
        }
    }

    /**
     * This method is for benchmark comparison, will be removed after test done.
     */
    public static HttpStatusClass valueOfSwitchCaseWithFastDiv(int code) {
        if (code < 0) return UNKNOWN;
        switch (fastDiv100(code)) {
            // 1xx
            case 1: return INFORMATIONAL;
            // 2xx
            case 2: return SUCCESS;
            // 3xx
            case 3: return REDIRECTION;
            // 4xx
            case 4: return CLIENT_ERROR;
            // 5xx
            case 5: return SERVER_ERROR;
            // others
            default: return UNKNOWN;
        }
    }

    /**
     * This method is for benchmark comparison, will be removed after test done.
     */
    public static HttpStatusClass valueOfArrayIndexWithFastDiv(int code) {
        if (UNKNOWN.contains(code)) {
            return UNKNOWN;
        }
        return statusArray[fastDiv100(code)];
    }

    public static HttpStatusClass valueOfArrayIndexWithFastDivForceInline(int code) {
        if (code < 100 || code >= 600) {
            return UNKNOWN;
        }
        return statusArray[fastDiv100(code)];
    }

    /**
     * @param code MUST >= 0
     * @return
     */
    private static int fastDiv100(int code) {
//        assert code >= 0;
        // 0x51eb851f is hex of 1374389535L
        return (int)((code * 1374389535L) >> 37);
    }

    /**
     * Returns the class of the specified HTTP status code.
     * @param code Just the numeric portion of the http status code.
     */
    public static HttpStatusClass valueOf(CharSequence code) {
        if (code != null && code.length() == 3) {
            char c0 = code.charAt(0);
            return isDigit(c0) && isDigit(code.charAt(1)) && isDigit(code.charAt(2)) ? valueOf(digit(c0) * 100)
                                                                                     : UNKNOWN;
        }
        return UNKNOWN;
    }

    private static int digit(char c) {
        return c - '0';
    }

    private static boolean isDigit(char c) {
        return c >= '0' && c <= '9';
    }

    private final int min;
    private final int max;
    private final AsciiString defaultReasonPhrase;

    HttpStatusClass(int min, int max, String defaultReasonPhrase) {
        this.min = min;
        this.max = max;
        this.defaultReasonPhrase = AsciiString.cached(defaultReasonPhrase);
    }

    /**
     * Returns {@code true} if and only if the specified HTTP status code falls into this class.
     */
    public boolean contains(int code) {
        return code >= min && code < max;
    }

    /**
     * Returns the default reason phrase of this HTTP status class.
     */
    AsciiString defaultReasonPhrase() {
        return defaultReasonPhrase;
    }
}
