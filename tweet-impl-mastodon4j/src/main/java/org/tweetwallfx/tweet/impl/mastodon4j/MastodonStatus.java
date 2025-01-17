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
package org.tweetwallfx.tweet.impl.mastodon4j;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.nodes.TextNode;
import org.jsoup.safety.Cleaner;
import org.jsoup.safety.Safelist;
import org.jsoup.select.NodeTraversor;
import org.jsoup.select.NodeVisitor;
import org.mastodon4j.core.api.entities.Status;
import org.tweetwallfx.tweet.api.Tweet;
import org.tweetwallfx.tweet.api.User;
import org.tweetwallfx.tweet.api.entry.HashtagTweetEntry;
import org.tweetwallfx.tweet.api.entry.MediaTweetEntry;
import org.tweetwallfx.tweet.api.entry.SymbolTweetEntry;
import org.tweetwallfx.tweet.api.entry.UrlTweetEntry;
import org.tweetwallfx.tweet.api.entry.UserMentionTweetEntry;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.StringJoiner;

final class MastodonStatus implements Tweet {
    private final Status status;
    private final String text;
    private final List<HashtagTweetEntry> hashtagTweetEntryList;

    public MastodonStatus(Status status) {
        this.status = status;
        Cleaner cleaner = new Cleaner(Safelist.none().addTags("img", "movie", "a"));
        Document document = cleaner.clean(Jsoup.parse(status.content()));
        hashtagTweetEntryList = new ArrayList<>();
        NodeTraversor.traverse(new NodeVisitor() {
            @Override
            public void head(Node node, int i) {
                if (node instanceof TextNode textNode) {
                    //String wholeText = textNode.getWholeText();
                    hashtagTweetEntryList.add(null);
                } else if (node instanceof Element element) {
                    switch (element.nodeName()) {
                        case "a" -> {
                        }
                        case "img" -> {
                        }
                        case "movie" -> {
                        }
                        default -> {
                        }
                    }
                }
            }
        }, document);
        this.text = document.text();
    }

    @Override
    public LocalDateTime getCreatedAt() {
        return status.created_at().toLocalDateTime();
    }

    @Override
    public int getFavoriteCount() {
        return status.favourites_count();
    }

    @Override
    public long getId() {
        return Long.parseLong(status.id());
    }

    @Override
    public long getInReplyToTweetId() {
        final String inReplyToId = status.in_reply_to_id();
        if (inReplyToId == null) {
            return 0;
        }
        return Long.parseLong(inReplyToId);
    }

    @Override
    public long getInReplyToUserId() {
        final String inReplyToAccountId = status.in_reply_to_account_id();
        if (inReplyToAccountId == null) {
            return 0;
        }
        return Long.parseLong(inReplyToAccountId);
    }

    @Override
    public String getInReplyToScreenName() {
        return null;
    }

    @Override
    public String getLang() {
        return status.language();
    }

    @Override
    public int getRetweetCount() {
        return status.reblogs_count();
    }

    @Override
    public Tweet getRetweetedTweet() {
        return Optional.ofNullable(status.reblog()).map(MastodonStatus::new).orElse(null);
    }

    @Override
    public Tweet getOriginTweet() {
        return Optional.ofNullable(status.reblog()).map(MastodonStatus::new).orElse(this);
    }

    @Override
    public String getText() {
        return text;
    }

    @Override
    public User getUser() {
        return new MastodonAccount(status.account());
    }

    @Override
    public boolean isRetweet() {
        return Boolean.TRUE.equals(status.reblogged());
    }

    @Override
    public boolean isTruncated() {
        return false;
    }

    @Override
    public HashtagTweetEntry[] getHashtagEntries() {
        return new HashtagTweetEntry[0];
    }

    @Override
    public MediaTweetEntry[] getMediaEntries() {
        return new MediaTweetEntry[0];
    }

    @Override
    public SymbolTweetEntry[] getSymbolEntries() {
        return new SymbolTweetEntry[0];
    }

    @Override
    public UrlTweetEntry[] getUrlEntries() {
        return new UrlTweetEntry[0];
    }

    @Override
    public UserMentionTweetEntry[] getUserMentionEntries() {
        return new UserMentionTweetEntry[0];
    }

    @Override
    public int hashCode() {
        return status.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof MastodonStatus mastodonStatus) {
            return status.equals(mastodonStatus.status);
        }
        return false;
    }
}
