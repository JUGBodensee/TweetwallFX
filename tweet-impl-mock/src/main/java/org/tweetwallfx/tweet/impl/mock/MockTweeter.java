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

import net.datafaker.Faker;
import net.datafaker.providers.base.Number;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tweetwallfx.config.Configuration;
import org.tweetwallfx.tweet.api.Tweet;
import org.tweetwallfx.tweet.api.TweetFilterQuery;
import org.tweetwallfx.tweet.api.TweetQuery;
import org.tweetwallfx.tweet.api.TweetStream;
import org.tweetwallfx.tweet.api.Tweeter;
import org.tweetwallfx.tweet.api.User;
import org.tweetwallfx.tweet.impl.mock.config.MockSettings;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringJoiner;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;
import java.util.function.LongPredicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.tweetwallfx.tweet.impl.mock.config.MockSettings.CONFIG_KEY;

public class MockTweeter implements Tweeter {
    private static final Logger LOGGER = LoggerFactory.getLogger(MockTweeter.class);
    private static final Faker FAKER = new Faker();
    private static final AtomicLong ID = new AtomicLong();

    private final MockSettings settings;
    private final ScheduledExecutorService executor;
    private final List<Consumer<Tweet>> postConsumers;
    private final Set<PostEntry> posts;
    private final Map<Integer, User> users;

    private ScheduledFuture<?> postTask;

    /**
     * Used from service loader.
     */
    public MockTweeter() {
        this(Configuration.getInstance().getConfigTyped(CONFIG_KEY, MockSettings.class),
                Executors.newSingleThreadScheduledExecutor());
    }

    MockTweeter(MockSettings settings, ScheduledExecutorService executor) {
        LOGGER.debug("Initializing with configuration: {}", settings);
        this.settings = settings;
        this.executor = executor;
        this.postConsumers = new CopyOnWriteArrayList<>();
        this.users = new ConcurrentHashMap<>();
        this.posts = Collections.synchronizedSet(new TreeSet<>());
        initializePosts();
    }

    private void initializePosts() {
        int initialPosts = settings.initialPosts();
        while (posts.size() < initialPosts) {
            simulatePost();
        }
    }

    private synchronized void initializePostsTask() {
        if (postTask == null) {
            long time = settings.postInterval().longValue();
            LOGGER.debug("Starting post task with {} second interval", time);
            postTask = executor.scheduleWithFixedDelay(this::simulatePost, time, time, TimeUnit.SECONDS);
        }
    }

    private void simulatePost() {
        final Tweet post = createPost();
        LOGGER.debug("Simulate post {}", post);
        posts.add(new PostEntry(post));
        postConsumers.forEach(postConsumer -> postConsumer.accept(post));
    }
    static int limitUserId(int userId) {
        if (userId > 199) {
            userId = userId % 200;
        }
        return userId;
    }

    static User createUser(int userId) {
        userId = limitUserId(userId);
        final String lastName = FAKER.name().lastName();
        final String profileUrl;
        final String biggerProfileUrl;
        final String firstName;
        if (userId < 100) {
            firstName = FAKER.resolve("name.female_first_name");
            profileUrl = "https://randomuser.me/api/portraits/med/women/%d.jpg".formatted(userId);
            biggerProfileUrl = "https://randomuser.me/api/portraits/women/%d.jpg".formatted(userId);
        } else {
            int profileNumber = userId - 100;
            firstName = FAKER.resolve("name.male_first_name");
            profileUrl = "https://randomuser.me/api/portraits/med/men/%d.jpg".formatted(profileNumber);
            biggerProfileUrl = "https://randomuser.me/api/portraits/men/%d.jpg".formatted(profileNumber);
        }
        return new MockUser(userId, "en", "@%s%s".formatted(firstName, lastName),
                "%s %s".formatted(firstName, lastName), FAKER.number().randomDigit(), FAKER.bool().bool(),
                profileUrl, biggerProfileUrl);
    }

    private Tweet createPost() {
        return createPost(ID.incrementAndGet());
    }

    private Tweet createPost(long postId) {
        var userId = limitUserId(FAKER.number().numberBetween(0, 199));
        return createPost(postId, users.computeIfAbsent(userId, MockTweeter::createUser));
    }

    private Tweet createPost(long postId, User user) {
        final Number number = FAKER.number();
        final StringJoiner text = new StringJoiner(" ");
        text.add(FAKER.lorem().sentence(number.numberBetween(7, 20)));
        settings.users().stream().map("@%s"::formatted).forEach(text::add);
        settings.hashtags().stream().map("#%s"::formatted).forEach(text::add);
        return new MockPost(postId, text.toString(), user, LocalDateTime.now(ZoneId.of("UTC")),
                null, number.randomDigit(), number.randomDigit(),
                new MockPostMedia(number.numberBetween(0, 500),
                        number.numberBetween(300, 1600),
                        number.numberBetween(200, 1200)));
    }

    @Override
    public boolean isEnabled() {
        return settings.enabled();
    }

    @Override
    public TweetStream createTweetStream(TweetFilterQuery filterQuery) {
        LOGGER.debug("createTweetStream({})", filterQuery);
        initializePostsTask();
        final MockPostStream statusStream = new MockPostStream();
        final Map<TrackType, List<String>> trackTypeListMap = Stream.of(filterQuery.getTrack())
                .collect(Collectors.groupingBy(this::trackType, () -> new EnumMap<>(TrackType.class), Collectors.toList()));
        trackTypeListMap.forEach((trackType, values) -> {
            switch (trackType) {
                case HASHTAG -> handleHashtags(statusStream, values);
                case USER -> handleUsers(statusStream, values);
                case UNKNOWN -> LOGGER.error("Track names not supported: {}", values);
            }
        });
        return statusStream;
    }

    private void handleHashtags(MockPostStream statusStream, List<String> hashtags) {
        postConsumers.add(post -> {
            if (hashtags.stream().anyMatch(post.getText()::contains)) {
                LOGGER.debug("Post {} contains hash tag", post);
                statusStream.accept(post);
            } else {
                LOGGER.debug("Skipped Post {} as is does no contain any of those hash tags: {} ", post, hashtags);
            }
        });
    }

    private void handleUsers(MockPostStream statusStream, List<String> users) {
        postConsumers.add(post -> {
            if (users.stream().anyMatch(post.getUser().getName()::contains)) {
                LOGGER.debug("Post {} contains user", post);
                statusStream.accept(post);
            } else {
                LOGGER.debug("Skipped Post {} as is does no contain any of those users: {} ", post, users);
            }
        });
    }

    enum TrackType {
        UNKNOWN, HASHTAG, USER
    }

    private TrackType trackType(String trackName) {
        if (trackName.startsWith("#")) {
            return TrackType.HASHTAG;
        } else if (trackName.startsWith("@")) {
            return TrackType.USER;
        } else {
            return TrackType.UNKNOWN;
        }
    }

    @Override
    public Tweet getTweet(long postId) {
        LOGGER.debug("getTweet({})", postId);
        return posts.stream()
                .filter(postEntry -> postEntry.test(postId))
                .map(PostEntry::tweet)
                .findFirst().orElseGet(() -> createPost(postId));
    }

    @Override
    public User getUser(String userId) {
        LOGGER.debug("getUser({})", userId);
        return users.computeIfAbsent(limitUserId(Integer.parseInt(userId)), MockTweeter::createUser);
    }

    @Override
    public Stream<User> getFriends(User user) {
        LOGGER.debug("getFriends({})", user);
        return Stream.empty();
    }

    @Override
    public Stream<User> getFriends(String userScreenName) {
        LOGGER.debug("getFriends({})", userScreenName);
        return Stream.empty();
    }

    @Override
    public Stream<User> getFriends(long userId) {
        LOGGER.debug("getFriends({})", userId);
        return Stream.empty();
    }

    @Override
    public Stream<User> getFollowers(User user) {
        LOGGER.debug("getFollowers({})", user);
        return Stream.empty();
    }

    @Override
    public Stream<User> getFollowers(String userScreenName) {
        LOGGER.debug("getFollowers({})", userScreenName);
        return Stream.empty();
    }

    @Override
    public Stream<User> getFollowers(long userId) {
        LOGGER.debug("getFollowers({})", userId);
        return Stream.empty();
    }

    @Override
    public Stream<Tweet> search(TweetQuery tweetQuery) {
        LOGGER.debug("search({})", tweetQuery);
        final String query = tweetQuery.getQuery();
        System.out.println(query); //TODO
        return posts.stream()
                .map(PostEntry::tweet);
    }

    @Override
    public Stream<Tweet> searchPaged(TweetQuery tweetQuery, int numberOfPages) {
        LOGGER.debug("searchPaged({}, {})", tweetQuery, numberOfPages);
        final String query = tweetQuery.getQuery();
        System.out.println(query); //TODO
        return posts.stream()
                .map(PostEntry::tweet)
                .limit(numberOfPages);
    }

    @Override
    public void shutdown() {
        LOGGER.debug("shutdown()");
        try {
            postTask = null;
            if (!executor.isTerminated()) {
                executor.shutdown();
                if (!executor.awaitTermination(2, TimeUnit.SECONDS)) {
                    executor.shutdownNow();
                }
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            LOGGER.error("Termination interrupted", e);
        }
    }

    private record PostEntry(Tweet tweet) implements Comparable<PostEntry>, LongPredicate {
        @Override
        public int compareTo(PostEntry other) {
            int rc = tweet.getCreatedAt().compareTo(other.tweet.getCreatedAt());
            if (rc == 0) {
                return Long.compare(tweet.getId(), other.tweet.getId());
            }
            return rc;
        }

        @Override
        public boolean test(long value) {
            return tweet.getId() == value;
        }
    }
}
