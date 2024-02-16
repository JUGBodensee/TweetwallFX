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
package org.tweetwallfx.google.vision;

import java.io.IOException;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tweetwallfx.filterchain.FilterChainSettings;
import org.tweetwallfx.filterchain.FilterStep;
import org.tweetwallfx.google.GoogleLikelihood;
import org.tweetwallfx.tweet.api.Tweet;
import org.tweetwallfx.tweet.api.entry.MediaTweetEntry;
import org.tweetwallfx.tweet.api.entry.MediaTweetEntryType;

/**
 * A {@link FilterStep} handling {@link Tweet}s by checking that photos comply
 * to some safe search criteria. If it does the {@link Tweet} is rejected.
 *
 * In case no safe search violations have occurred with the configured limits it
 * is terminally rejected with {@link Result#REJECTED}. Otherwise it is
 * evaluated as {@link Result#NOTHING_DEFINITE}.
 */
public class ImageContentFilterStep implements FilterStep<Tweet> {

    private static final Logger LOG = LoggerFactory.getLogger(ImageContentFilterStep.class);
    private static final Map<Integer, Function<MediaTweetEntry, String>> MTE_SIZE_TO_URL_FUNCTIONS = Map.of(
            0, mte -> mte.getMediaUrl(MediaTweetEntry.SizeHint.THUMB),
            1, mte -> mte.getMediaUrl(MediaTweetEntry.SizeHint.SMALL),
            2, mte -> mte.getMediaUrl(MediaTweetEntry.SizeHint.MEDIUM),
            3, mte -> mte.getMediaUrl(MediaTweetEntry.SizeHint.LARGE)
    );

    private final Config config;
    private final ImageContentAnalysis.SafeSearch requiredSafeSearch;

    private ImageContentFilterStep(final Config config) {
        this.config = config;
        requiredSafeSearch = new ImageContentAnalysis.SafeSearch(
                config.adult().acceptableLikelyhood(),
                config.medical().acceptableLikelyhood(),
                config.racy().acceptableLikelyhood(),
                config.spoof().acceptableLikelyhood(),
                config.violence().acceptableLikelyhood()
        );
    }

    @Override
    public Result check(final Tweet tweet) {
        Tweet t = tweet;

        do {
            Result r = checkImages(tweet, t);

            if (r.isTerminal()) {
                return r;
            } else {
                t = t.getRetweetedTweet();
            }
        } while (config.checkRetweeted() && null != t);

        LOG.debug("Tweet(id:{}): No terminal decision found -> NOTHING_DEFINITE",
                tweet.getId());
        return Result.NOTHING_DEFINITE;
    }

    private Result checkImages(final Tweet tweet, final Tweet t) {
        final List<MediaTweetEntry> mtes = Stream.of(t.getMediaEntries())
                .filter(MediaTweetEntryType.photo::isType)
                .toList();

        if (mtes.isEmpty()) {
            LOG.debug("Tweet(id:{}): Tweet(id:{}) has no photos -> NOTHING_DEFINITE",
                    tweet.getId(),
                    t.getId());
            return Result.NOTHING_DEFINITE;
        }

        final List<String> imageUrlStrings = mtes.stream().map(this::getImageUrlString).toList();
        final Map<String, ImageContentAnalysis> visionAnalysis;

        try {
            visionAnalysis = GoogleVisionCache.INSTANCE.getCachedOrLoad(imageUrlStrings.stream());
        } catch (final IOException ex) {
            LOG.warn("Tweet(id:{}): Tweet(id:{}) failed analysation of its photos -> REJECTED",
                    tweet.getId(),
                    t.getId(),
                    ex);
            return Result.REJECTED;
        }

        for (final Map.Entry<String, ImageContentAnalysis> entry : visionAnalysis.entrySet()) {
            if (null == entry.getValue()) {
                continue;
            }

            if (null != entry.getValue().analysisError()) {
                LOG.info("Tweet(id:{}): Tweet(id:{}) photo \"{}\" failed analysation \"{}\" -> REJECTED",
                        tweet.getId(),
                        t.getId(),
                        entry.getValue().analysisError(),
                        entry.getKey());
                return Result.REJECTED;
            }

            final String analysisResult = diff(entry.getValue().safeSearch(), requiredSafeSearch);

            if (!analysisResult.isEmpty()) {
                LOG.info("Tweet(id:{}): Tweet(id:{}) photo \"{}\" is not compliant to configuration {} -> REJECTED",
                        tweet.getId(),
                        t.getId(),
                        entry.getKey(),
                        analysisResult);
                return Result.REJECTED;
            }
        }

        return Result.NOTHING_DEFINITE;
    }

    private String getImageUrlString(final MediaTweetEntry mte) {
        return MTE_SIZE_TO_URL_FUNCTIONS
                .getOrDefault(
                        mte.getSizes().keySet().stream().max(Comparator.naturalOrder()).orElse(Integer.MAX_VALUE),
                        this::unsupportedSize)
                .apply(mte);
    }

    private String unsupportedSize(final MediaTweetEntry mte) {
        throw new IllegalArgumentException("Illegal value");
    }

    private String diff(final ImageContentAnalysis.SafeSearch actual, final ImageContentAnalysis.SafeSearch required) {
        return Map.<String, Function<ImageContentAnalysis.SafeSearch, GoogleLikelihood>>of(
                "adult", ImageContentAnalysis.SafeSearch::adult,
                "medical", ImageContentAnalysis.SafeSearch::medical,
                "racy", ImageContentAnalysis.SafeSearch::racy,
                "spoof", ImageContentAnalysis.SafeSearch::spoof,
                "violence", ImageContentAnalysis.SafeSearch::violence)
                .entrySet().stream()
                .filter(e -> GoogleLikelihood.UNKNOWN != e.getValue().apply(actual))
                .filter(e -> e.getValue().apply(actual).compareTo(e.getValue().apply(required)) > 0)
                .map(e
                        -> String.format(
                        "%s: %s > %s",
                        e.getKey(),
                        e.getValue().apply(actual),
                        e.getValue().apply(required)))
                .collect(Collectors.joining("; "));
    }

    /**
     * Implementation of {@link FilterStep.Factory} creating
     * {@link ImageContentFilterStep}.
     */
    public static final class FactoryImpl implements FilterStep.Factory {

        @Override
        public Class<Tweet> getDomainObjectClass() {
            return Tweet.class;
        }

        @Override
        public Class<ImageContentFilterStep> getFilterStepClass() {
            return ImageContentFilterStep.class;
        }

        @Override
        public ImageContentFilterStep create(final FilterChainSettings.FilterStepDefinition filterStepDefinition) {
            return new ImageContentFilterStep(filterStepDefinition.getConfig(Config.class));
        }
    }

    /**
     * POJO used to configure {@link ImageContentFilterStep}.
     *
     * <p>
     * Param {@code checkRetweeted} a boolean flag controlling whether for a
     * retweet the retweeted Tweet is also checked
     */
    public static record Config(
            Boolean checkRetweeted,
            SafeTypeConfig adult,
            SafeTypeConfig medical,
            SafeTypeConfig racy,
            SafeTypeConfig spoof,
            SafeTypeConfig violence) {

        public Config(
                final Boolean checkRetweeted,
                final SafeTypeConfig adult,
                final SafeTypeConfig medical,
                final SafeTypeConfig racy,
                final SafeTypeConfig spoof,
                final SafeTypeConfig violence) {
            this.checkRetweeted = Objects.requireNonNullElse(checkRetweeted, false);
            this.adult = Objects.requireNonNullElse(adult, new SafeTypeConfig(GoogleLikelihood.VERY_UNLIKELY));
            this.medical = Objects.requireNonNullElse(medical, new SafeTypeConfig(GoogleLikelihood.VERY_UNLIKELY));
            this.racy = Objects.requireNonNullElse(racy, new SafeTypeConfig(GoogleLikelihood.VERY_UNLIKELY));
            this.spoof = Objects.requireNonNullElse(spoof, new SafeTypeConfig(GoogleLikelihood.UNLIKELY));
            this.violence = Objects.requireNonNullElse(violence, new SafeTypeConfig(GoogleLikelihood.VERY_UNLIKELY));
        }
    }

    public static record SafeTypeConfig(
            GoogleLikelihood acceptableLikelyhood) {

        public SafeTypeConfig(final GoogleLikelihood acceptableLikelyhood) {
            this.acceptableLikelyhood = Objects.requireNonNullElse(acceptableLikelyhood, GoogleLikelihood.VERY_UNLIKELY);
        }
    }
}
