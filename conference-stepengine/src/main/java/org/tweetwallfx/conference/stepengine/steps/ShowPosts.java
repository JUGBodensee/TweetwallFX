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
package org.tweetwallfx.conference.stepengine.steps;

import java.util.Arrays;
import java.util.Collection;

import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tweetwallfx.controls.WordleSkin;
import org.tweetwallfx.emoji.control.EmojiFlow;
import org.tweetwallfx.stepengine.api.DataProvider;
import org.tweetwallfx.stepengine.api.Step;
import org.tweetwallfx.stepengine.api.StepEngine.MachineContext;
import org.tweetwallfx.stepengine.api.config.AbstractConfig;
import org.tweetwallfx.stepengine.api.config.StepEngineSettings;
import org.tweetwallfx.stepengine.dataproviders.TweetStreamDataProvider;
import org.tweetwallfx.stepengine.dataproviders.TweetUserProfileImageDataProvider;
import org.tweetwallfx.tweet.api.Tweet;

import javafx.animation.FadeTransition;
import javafx.geometry.Insets;
import javafx.scene.CacheHint;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

public class ShowPosts implements Step {
    private static final Logger LOGGER = LoggerFactory.getLogger(ShowPosts.class);
    private final Config config;

    private ShowPosts(Config config) {
        this.config = config;
    }

    @Override
    public void doStep(MachineContext context) {
        final var wordleSkin = (WordleSkin) context.get("WordleSkin");
        final var dataProvider = context.getDataProvider(TweetStreamDataProvider.class);

        var existingPostsNode = (Pane)wordleSkin.getNode().lookup("#postsNode");
        if (null == existingPostsNode) {
            LOGGER.debug("Initializing posts node");

            var postsNode = new Pane();
            postsNode.getStyleClass().add("posts");
            postsNode.setId("postsNode");
            postsNode.setOpacity(0);

            var title = new Label("Latest Posts");

            title.setPrefWidth(config.width);
            title.getStyleClass().add("title");
            title.setPrefHeight(config.titleHeight);
            title.setAlignment(Pos.CENTER);

            postsNode.getChildren().add(title);

            final var fadeIn = new FadeTransition(Duration.millis(500), postsNode);
            fadeIn.setFromValue(0);
            fadeIn.setToValue(1);
            fadeIn.setOnFinished(e -> {
                LOGGER.info("Calling proceed from ShowPosts");
                context.proceed();
            });
            postsNode.setLayoutX(config.layoutX);
            postsNode.setLayoutY(config.layoutY);
            postsNode.setMinWidth(config.width);
            postsNode.setMaxWidth(config.width);
            postsNode.setPrefWidth(config.width);
            postsNode.setCacheHint(CacheHint.SPEED);
            postsNode.setCache(true);
            int col = 0;
            int row = 0;

            for (Tweet post : dataProvider.getTweets()) {
                Pane postPane = createPostNode(context, post);
                double postWidth = (config.width - (config.columns - 1) * config.postHGap) / config.columns;
                postPane.setMinWidth(postWidth);
                postPane.setMaxWidth(postWidth);
                postPane.setPrefWidth(postWidth);
                postPane.setMinHeight(config.postHeight);
                postPane.setMaxHeight(config.postHeight);
                postPane.setPrefHeight(config.postHeight);
                postPane.setLayoutX(col * (postWidth + config.postHGap));
                postPane.setLayoutY(config.titleHeight + config.postVGap + (config.postHeight + config.postVGap) * row);
                postsNode.getChildren().add(postPane);
                col++;
                if (col == config.columns) {
                    row++;
                    col = 0;
                }
                if (row >= config.rows) {
                    break;
                }
            }

            Platform.runLater(() -> {
                wordleSkin.getPane().getChildren().add(postsNode);
                fadeIn.play();
            });
        }
    }

    private Pane createPostNode(MachineContext context, Tweet post) {
        final var userImageProvider = context.getDataProvider(TweetUserProfileImageDataProvider.class);
        final var bpPostPane = new BorderPane();
        bpPostPane.getStyleClass().add("postDisplay");
        bpPostPane.setCenter(createSinglePostDisplay(post, userImageProvider));
        return bpPostPane;
    }

    private HBox createSinglePostDisplay(final Tweet post,
                                         final TweetUserProfileImageDataProvider userImageProvider) {
        var postFlow = new EmojiFlow();
        postFlow.setText(post.getDisplayEnhancedText());
        postFlow.setEmojiFitWidth(config.postFontSize);
        postFlow.setEmojiFitHeight(config.postFontSize);
        postFlow.getStyleClass().add("postFlow");
        postFlow.setMaxHeight(Double.MAX_VALUE);

        var postUser = new EmojiFlow();
        postUser.setText(post.getUser().getName());
        postUser.setEmojiFitWidth(config.postUserFontSize);
        postUser.setEmojiFitHeight(config.postFontSize);
        postUser.getStyleClass().add("postUserName");
        postUser.setMaxHeight(Double.MAX_VALUE);

        VBox imageBox = new VBox(createPostUserImage(userImageProvider, post));
        imageBox.setPadding(new Insets(10, 0, 10, 10));

        VBox postContentBox = new VBox(postUser, postFlow);
        postContentBox.setSpacing(10);
        postContentBox.setPadding(new Insets(10, 10, 10, 0));

        HBox postBox = new HBox(imageBox, postContentBox);
        postBox.setCacheHint(CacheHint.QUALITY);
        postBox.setSpacing(10);

        return postBox;
    }

    private Node createPostUserImage(TweetUserProfileImageDataProvider postImageProvider, Tweet post) {
        var image = postImageProvider.getImageBig(post.getUser());
        var postUserImage = new ImageView(image);
        postUserImage.getStyleClass().add("postUserImage");
        postUserImage.setPreserveRatio(true);
        if (image.getWidth() > image.getHeight()) {
            postUserImage.setFitHeight(config.avatarSize);
        } else {
            postUserImage.setFitWidth(config.avatarSize);
        }

        // avatar image clipping
        if (config.circularAvatar) {
            Circle circle = new Circle(config.avatarSize / 2f, config.avatarSize / 2f, config.avatarSize / 2f);
            postUserImage.setClip(circle);
        } else {
            Rectangle clip = new Rectangle(config.avatarSize, config.avatarSize);
            clip.setArcWidth(config.avatarArcSize);
            clip.setArcHeight(config.avatarArcSize);
            postUserImage.setClip(clip);
        }
        return postUserImage;
    }

    static String fixup(String source) {
        return source.replaceAll("[\n\r]+", "|").replaceAll("[\u200D]","");
    }

    @Override
    public boolean requiresPlatformThread() {
        return false;
    }

    @Override
    public java.time.Duration preferredStepDuration(final MachineContext context) {
        return java.time.Duration.ofMillis(config.getStepDuration());
    }

    /**
     * Implementation of {@link Step.Factory} as Service implementation creating
     * {@link ShowPosts}.
     */
    public static final class FactoryImpl implements Step.Factory {
        @Override
        public Step create(final StepEngineSettings.StepDefinition stepDefinition) {
            return new ShowPosts(stepDefinition.getConfig(ShowPosts.Config.class));
        }

        @Override
        public Class<ShowPosts> getStepClass() {
            return ShowPosts.class;
        }

        @Override
        public Collection<Class<? extends DataProvider>> getRequiredDataProviders(final StepEngineSettings.StepDefinition stepSettings) {
            return Arrays.asList(TweetStreamDataProvider.class, TweetUserProfileImageDataProvider.class);
        }
    }

    public static class Config extends AbstractConfig {
        public double layoutX = 0;
        public double layoutY = 0;
        public double width = 800;
        public double titleHeight = 60;
        public double postVGap = 10;
        public double postHGap = 10;
        public double postHeight = 150;
        public int postFontSize = 13;
        public int postUserFontSize = 13;
        public int columns = 1;
        public int rows = 1;
        public int avatarSize = 64;
        public int avatarArcSize = 20;
        public boolean circularAvatar = true;
    }
}
