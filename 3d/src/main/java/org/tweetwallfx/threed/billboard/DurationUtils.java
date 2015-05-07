/*
 * The MIT License
 *
 * Copyright 2014-2015 TweetWallFX
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package org.tweetwallfx.threed.billboard;

import javafx.util.Duration;

/**
 * Some Duration conversions
 *
 * @author Jason Pollastrini aka jdub1581
 */
public class DurationUtils {

    public static Duration toNanos(double val) {
        return Duration.millis(
                (val / 1000000) * 1000
        );
    }

    public static Duration toNanos(Duration dur) {
        double milVal = dur.toMillis();
        double nanos = milVal / 1000000;
        return Duration.millis(nanos);
    }

    public static Duration fpsToMillis(long fps) {
        return Duration.millis(((1.0 / fps) * 1000));
    }

    public static Duration fpsToNanos(long fps) {
        return Duration.millis((((1.0 / fps) * 1000) / 1000000));
    }
}
