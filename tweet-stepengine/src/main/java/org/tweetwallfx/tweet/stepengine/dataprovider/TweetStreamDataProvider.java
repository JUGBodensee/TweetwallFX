/*
 * The MIT License
 *
 * Copyright 2018 TweetWallFX
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
package org.tweetwallfx.tweet.stepengine.dataprovider;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Deque;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.stream.Collectors;
import javafx.scene.image.Image;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.tweetwallfx.config.Configuration;
import org.tweetwallfx.config.TweetwallSettings;
import org.tweetwallfx.stepengine.api.DataProvider;
import org.tweetwallfx.stepengine.api.config.StepEngineSettings;
import org.tweetwallfx.tweet.api.Tweet;
import org.tweetwallfx.tweet.api.TweetQuery;
import org.tweetwallfx.tweet.api.Tweeter;
import org.tweetwallfx.tweet.api.entry.MediaTweetEntryType;

/**
 * Provides an always current list of tweets based on the configured query. The
 * history length is not yet configurable.
 *
 * @author Sven Reimers
 */
public class TweetStreamDataProvider implements DataProvider.NewTweetAware {

    private static final Logger LOGGER = LogManager.getLogger(TweetStreamDataProvider.class);
    private final ReadWriteLock tweetListLock = new ReentrantReadWriteLock();
    private final String searchText = Configuration.getInstance()
            .getConfigTyped(TweetwallSettings.CONFIG_KEY, TweetwallSettings.class)
            .getQuery();
    private volatile Image latestTweetedImage;
    private volatile Deque<Tweet> tweets = new ArrayDeque<>();
    private final Config config;

    private TweetStreamDataProvider(final Config config) {
        this.config = config;

        LOGGER.info("Initialize tweet stream provider");
        List<Tweet> history = getLatestHistory();
        tweetListLock.writeLock().lock();
        try {
            history.forEach(this::addTweet);
        } finally {
            tweetListLock.writeLock().unlock();
        }
    }

    @Override
    public void processNewTweet(final Tweet tweet) {
        LOGGER.info("New tweet received");
        addTweet(tweet);
    }

    private void updateImage(final Tweet tweet) {
        Arrays.stream(tweet.getMediaEntries())
                .filter(MediaTweetEntryType.photo::isType)
                .findFirst()
                .ifPresent(me -> {
                    String url;
                    switch (me.getSizes().keySet().stream().max(Comparator.naturalOrder())
                            .get()) {
                        case 0:
                            url = me.getMediaUrl() + ":thumb";
                            break;
                        case 1:
                            url = me.getMediaUrl() + ":small";
                            break;
                        case 2:
                            url = me.getMediaUrl() + ":medium";
                            break;
                        case 3:
                            url = me.getMediaUrl() + ":large";
                            break;
                        default:
                            throw new IllegalArgumentException("Illegal value");
                    }
                    latestTweetedImage = new Image(url);
                });
    }

    private void addTweet(final Tweet tweet) {
        LOGGER.info("Add tweet {}", tweet.getId());
        if (tweet.getUser().getFollowersCount() > config.getMinFollowersCount()) {
            tweetListLock.writeLock().lock();
            try {
                if (tweet.isRetweet()) {
                    Tweet originalTweet = tweet.getRetweetedTweet();
                    if (tweets.stream().noneMatch(twt -> originalTweet.getId() == twt.getId())) {
                        tweets.addFirst(originalTweet);
                        updateImage(originalTweet);
                    }
                } else {
                    tweets.addFirst(tweet);
                    updateImage(tweet);
                }
                if (tweets.size() > config.getMaxTweets()) {
                    tweets.removeLast();
                }
            } finally {
                tweetListLock.writeLock().unlock();
            }
        }
    }

    public Optional<Image> getLatestImage() {
        return Optional.ofNullable(latestTweetedImage);
    }

    public List<Tweet> getTweets() {
        tweetListLock.readLock().lock();

        try {
            return new ArrayList<>(this.tweets);
        } finally {
            tweetListLock.readLock().unlock();
        }
    }

    private List<Tweet> getLatestHistory() {
        LOGGER.info("Reinit the history");
        return Tweeter.getInstance()
                .search(new TweetQuery()
                        .query(searchText)
                        .count(config.getHistorySize()))
                .collect(Collectors.toList());
    }

    public static class FactoryImpl implements DataProvider.Factory {

        @Override
        public TweetStreamDataProvider create(final StepEngineSettings.DataProviderSetting dataProviderSetting) {
            return new TweetStreamDataProvider(dataProviderSetting.getConfig(Config.class));
        }

        @Override
        public Class<TweetStreamDataProvider> getDataProviderClass() {
            return TweetStreamDataProvider.class;
        }
    }

    /**
     * POJO used to configure {@link TweetStreamDataProvider}.
     */
    public static final class Config {

        /**
         * The number of the tweets to request from query in order to fill up
         * {@link TweetStreamDataProvider} upon initialization. Defaults to
         * {@code 25}.
         */
        private int historySize = 25;

        /**
         * The number of tweet to produce upon request via
         * {@link TweetStreamDataProvider#getTweets()}. Defaults to {@code 4}.
         */
        private int maxTweets = 4;

        /**
         * The number of followers required of a tweets user in order to be
         * considerable for {@link TweetStreamDataProvider}. Defaults to
         * {@code 0}.
         */
        private int minFollowersCount = 0;

        /**
         * DisplayNames of twitter users blocked from this
         * {@link TweetStreamDataProvider}.
         */
        private Set<String> blockedTwitterUserScreenNames;

        public Set<String> getBlockedTwitterUserScreenNames() {
            return Optional.ofNullable(blockedTwitterUserScreenNames)
                    .orElse(Collections.emptySet());
        }

        public void setBlockedTwitterUserScreenNames(final Set<String> blockedTwitterUserScreenNames) {
            this.blockedTwitterUserScreenNames = blockedTwitterUserScreenNames;
        }

        public int getHistorySize() {
            return historySize;
        }

        public void setHistorySize(int historySize) {
            if (historySize < 0) {
                throw new IllegalArgumentException("property 'historySize' must not be a negative number");
            }

            this.historySize = historySize;
        }

        public int getMaxTweets() {
            return maxTweets;
        }

        public void setMaxTweets(final int maxTweets) {
            if (maxTweets < 0) {
                throw new IllegalArgumentException("property 'maxTweets' must not be a negative number");
            }

            this.maxTweets = maxTweets;
        }

        public int getMinFollowersCount() {
            return minFollowersCount;
        }

        public void setMinFollowersCount(final int minFollowersCount) {
            if (minFollowersCount < 0) {
                throw new IllegalArgumentException("property 'minFollowersCount' must not be a negative number");
            }

            this.minFollowersCount = minFollowersCount;
        }
    }
}
