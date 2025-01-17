/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2023-2025 TweetWallFX
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
package org.tweetwallfx.tweet.impl.mastodon4j;

import org.junit.jupiter.api.Test;
import org.mastodon4j.core.api.entities.Status;

import java.time.ZoneId;
import java.time.ZonedDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class MastodonStatusTest {
    public static final String STATUS_CONTENT = """
            <p>the status #message html to @TweetwallFX, a new line:
            some nice image <img src="https://somehost.somedomain/image.jpg alt="some image">
            and a <a href="http://somehost.somedomain/">#special link</a> to some content for <b>@reinhapa</b></p>""";
    ZonedDateTime createdAt = ZonedDateTime.now(ZoneId.systemDefault());
    MastodonStatus status = new MastodonStatus(new Status("42", null, createdAt, null,
            STATUS_CONTENT,  null, null, null, null,
            null, null, null, null, 33, 22, null,
            null, null, null, null, null, null, "german",
            null, null, null, true, null, null, null, null));

    MastodonStatus statusWithoutOptionals = new MastodonStatus(new Status("43", null, createdAt, null,
            "", null, null, null, null, null, null,
            null, null, 34, 23, null, null, null,
            null, null, null, null, "english", null, null,
            null, null, null, null, null, null));

    @Test
    void getCreatedAt() {
        assertThat(status.getCreatedAt()).isEqualTo(createdAt.toLocalDateTime());
        assertThat(statusWithoutOptionals.getCreatedAt()).isEqualTo(createdAt.toLocalDateTime());
    }

    @Test
    void getFavoriteCount() {
        assertThat(status.getFavoriteCount()).isEqualTo(22);
        assertThat(statusWithoutOptionals.getFavoriteCount()).isEqualTo(23);
    }

    @Test
    void getId() {
        assertThat(status.getId()).isEqualTo(42L);
        assertThat(statusWithoutOptionals.getId()).isEqualTo(43L);
    }

    @Test
    void getInReplyToTweetId() {
        assertThat(status.getInReplyToTweetId()).isEqualTo(0);
        assertThat(statusWithoutOptionals.getInReplyToTweetId()).isEqualTo(0);
    }

    @Test
    void getInReplyToUserId() {
        assertThat(status.getInReplyToUserId()).isEqualTo(0);
        assertThat(statusWithoutOptionals.getInReplyToUserId()).isEqualTo(0);
    }

    @Test
    void getInReplyToScreenName() {
        assertThat(status.getInReplyToScreenName()).isNull();
        assertThat(statusWithoutOptionals.getInReplyToScreenName()).isNull();
    }

    @Test
    void getLang() {
        assertThat(status.getLang()).isEqualTo("german");
        assertThat(statusWithoutOptionals.getLang()).isEqualTo("english");
    }

    @Test
    void getRetweetCount() {
        assertThat(status.getRetweetCount()).isEqualTo(33);
        assertThat(statusWithoutOptionals.getRetweetCount()).isEqualTo(34);
    }

    @Test
    void getRetweetedTweet() {
        assertThat(status.getRetweetedTweet()).isNull();
        assertThat(statusWithoutOptionals.getRetweetedTweet()).isNull();
    }

    @Test
    void getOriginTweet() {
        assertThat(status.getOriginTweet()).isSameAs(status);
        assertThat(statusWithoutOptionals.getOriginTweet()).isSameAs(statusWithoutOptionals);
    }

    @Test
    void getText() {
        assertThat(status.getText()).isEqualTo("""
                the status #message html to @TweetwallFX, a new line: \
                some nice image and a #special link to some content for @reinhapa""");
        assertThat(statusWithoutOptionals.getText()).isEmpty();
    }

    @Test
    void getUser() {
        assertThat(status.getUser()).isInstanceOf(MastodonAccount.class);
        assertThat(statusWithoutOptionals.getUser()).isInstanceOf(MastodonAccount.class);
    }

    @Test
    void isRetweet() {
        assertThat(status.isRetweet()).isTrue();
        assertThat(statusWithoutOptionals.isRetweet()).isFalse();
    }

    @Test
    void isTruncated() {
        assertThat(status.isTruncated()).isFalse();
        assertThat(statusWithoutOptionals.isTruncated()).isFalse();
    }

    @Test
    void getHashtagEntries() {
        assertThat(status.getHashtagEntries()).isNotNull().isEmpty();
        assertThat(statusWithoutOptionals.getHashtagEntries()).isNotNull().isEmpty();
    }

    @Test
    void getMediaEntries() {
        assertThat(status.getMediaEntries()).isNotNull().isEmpty();
        assertThat(statusWithoutOptionals.getMediaEntries()).isNotNull().isEmpty();
    }

    @Test
    void getSymbolEntries() {
        assertThat(status.getSymbolEntries()).isNotNull().isEmpty();
        assertThat(statusWithoutOptionals.getSymbolEntries()).isNotNull().isEmpty();
    }

    @Test
    void getUrlEntries() {
        assertThat(status.getUrlEntries()).isNotNull().isEmpty();
        assertThat(statusWithoutOptionals.getUrlEntries()).isNotNull().isEmpty();
    }

    @Test
    void getUserMentionEntries() {
        assertThat(status.getUserMentionEntries()).isNotNull().isEmpty();
        assertThat(statusWithoutOptionals.getUserMentionEntries()).isNotNull().isEmpty();
    }
}
