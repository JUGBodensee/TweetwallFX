/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2018-2025 TweetWallFX
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
package org.tweetwallfx.stepengine.dataproviders;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tweetwallfx.cache.URLContent;
import org.tweetwallfx.cache.URLContentCacheBase;
import org.tweetwallfx.tweet.api.Tweet;
import org.tweetwallfx.tweet.api.entry.MediaTweetEntry;

import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * Cache used to provide images for photos of e.g. a {@link Tweet}.
 */
public final class PhotoImageCache extends URLContentCacheBase {

    private static final Logger LOG = LoggerFactory.getLogger(PhotoImageCache.class);
    private static final Map<Integer, Function<MediaTweetEntry, String>> MTE_SIZE_TO_URL_FUNCTIONS;

    static {
        final Map<Integer, Function<MediaTweetEntry, String>> tmp = new HashMap<>();

        tmp.put(0, mte -> mte.getMediaUrl(MediaTweetEntry.SizeHint.THUMB));
        tmp.put(1, mte -> mte.getMediaUrl(MediaTweetEntry.SizeHint.SMALL));
        tmp.put(2, mte -> mte.getMediaUrl(MediaTweetEntry.SizeHint.MEDIUM));
        tmp.put(3, mte -> mte.getMediaUrl(MediaTweetEntry.SizeHint.LARGE));

        MTE_SIZE_TO_URL_FUNCTIONS = Collections.unmodifiableMap(tmp);
    }

    /**
     * Cache instance.
     */
    public static final PhotoImageCache INSTANCE = new PhotoImageCache();

    private PhotoImageCache() {
        super("photoImage");
    }

    public void addToCacheAsync(final MediaTweetEntry mte) {
        getCachedOrLoad(
                mte,
                this::handleLoadedContent);
    }

    public URLContent getCached(final MediaTweetEntry mte) {
        return getCachedOrLoad(getImageUrlString(mte));
    }

    public void getCachedOrLoad(final MediaTweetEntry mte, final Consumer<URLContent> consumer) {
        getCachedOrLoad(
                getImageUrlString(mte),
                consumer);
    }

    private String getImageUrlString(final MediaTweetEntry mte) {
        final String urlString = mte.getSizes().keySet().stream()
                .max(Comparator.naturalOrder())
                .map(MTE_SIZE_TO_URL_FUNCTIONS::get)
                .orElseThrow(() -> new IllegalArgumentException("Illegal value"))
                .apply(mte);

        LOG.info("MediaTweetEntry({}): {}", mte.getId(), urlString);
        return urlString;
    }

    private void handleLoadedContent(final URLContent urlc) {
        // do nothing
    }
}
