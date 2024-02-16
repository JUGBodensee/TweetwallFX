/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2015-2025 TweetWallFX
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
package org.tweetwallfx.tweet.api.entry;

import java.util.Map;

public interface MediaTweetEntry extends TweetEntry {

    /**
     * Returns the id of the media.
     *
     * @return the id of the media
     */
    long getId();

    /**
     * Returns the media URL.
     *
     * @param sizeHint optional size hint
     * @return the media URL
     */
    String getMediaUrl(SizeHint sizeHint);

    /**
     * Returns size variations of the media.
     *
     * @return size variations of the media
     */
    Map<Integer, Size> getSizes();

    enum SizeHint {
        THUMB, SMALL, MEDIUM, LARGE
    }

    interface Size extends java.io.Serializable {

        Integer THUMB = 0;
        Integer SMALL = 1;
        Integer MEDIUM = 2;
        Integer LARGE = 3;
        int FIT = 100;
        int CROP = 101;

        int getWidth();

        int getHeight();

        int getResize();
    }

    /**
     * Returns the media type.
     *
     * @return the media type
     */
    MediaTweetEntryType getType();

    static Size createSize(final int width, final int height, final int resize) {
        return new MediaTweetEntrySize(width, height, resize);
    }
}
