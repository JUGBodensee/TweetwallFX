/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2024-2025 TweetWallFX
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
package org.tweetwallfx.tweet.impl.mock;

import org.tweetwallfx.tweet.api.entry.MediaTweetEntry;
import org.tweetwallfx.tweet.api.entry.MediaTweetEntryType;

import java.util.Map;

public record MockPostMedia(long id, int with, int height) implements MediaTweetEntry {
    @Override
    public long getId() {
        return id;
    }

    @Override
    public String getMediaUrl(SizeHint sizeHint) {
        return "https://picsum.photos/id/%s/%s/%s".formatted(id, height, with);
    }

    @Override
    public Map<Integer, Size> getSizes() {
        return Map.of(0,
                MediaTweetEntry.createSize(
                        with,
                        height,
                        1));
    }

    @Override
    public MediaTweetEntryType getType() {
        return MediaTweetEntryType.photo;
    }

    @Override
    public String getText() {
        return getMediaUrl(null);
    }

    @Override
    public int getStart() {
        return 0;
    }

    @Override
    public int getEnd() {
        return 0;
    }
}
