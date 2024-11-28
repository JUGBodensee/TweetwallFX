/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2015-2023 TweetWallFX
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
package org.tweetwallfx.controls;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javafx.beans.binding.Bindings;
import javafx.scene.control.SkinBase;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tweetwallfx.stepengine.api.StepEngine;

public final class WordleSkin extends SkinBase<Wordle> {

    private static final Logger LOG = LoggerFactory.getLogger("org.tweetwallfx.startup");
    private static final Logger LOGGER = LoggerFactory.getLogger(WordleSkin.class);
    public final Map<Word, Text> word2TextMap = new HashMap<>();
    // used for Tweet Display
    public final List<TweetLayout.TweetWordNode> tweetWordList = new ArrayList<>();
    private final Pane pane;
    private final Pane stackPane;
    private Pane infoBox;
    private Pane mediaBox;

    private int displayCloudTags = 25;

    private ImageView logo;
    private ImageView secondLogo;
    private ImageView backgroundImage;
    private final Font font;
    private final int fontSizeMin;
    private final int fontSizeMax;
    private final int tweetFontSize;
    private final Boolean favIconsVisible;
    private final DateFormat df = new SimpleDateFormat("HH:mm:ss");

    public ImageView getSecondLogo() {
        return secondLogo;
    }

    public ImageView getLogo() {
        return logo;
    }

    public Pane getPane() {
        return pane;
    }

    public int getDisplayCloudTags() {
        return displayCloudTags;
    }

    public Pane getMediaBox() {
        return mediaBox;
    }

    public void setMediaBox(Pane mediaBox) {
        this.mediaBox = mediaBox;
    }

    public Pane getInfoBox() {
        return infoBox;
    }

    public void setInfoBox(Pane infoBox) {
        this.infoBox = infoBox;
    }

    public Boolean getFavIconsVisible() {
        return favIconsVisible;
    }

    public DateFormat getDf() {
        return df;
    }

    public Font getFont() {
        return font;
    }

    public int getFontSizeMin() {
        return fontSizeMin;
    }

    public int getFontSizeMax() {
        return fontSizeMax;
    }

    public int getTweetFontSize() {
        return tweetFontSize;
    }

    public WordleSkin(Wordle wordle) {
        super(wordle);
        //create panes
        stackPane = new StackPane();
        pane = new Pane();
        pane.prefWidthProperty().bind(stackPane.widthProperty());
        pane.prefHeightProperty().bind(stackPane.heightProperty());
        //assemble panes
        stackPane.getChildren().addAll(pane);
        this.getChildren().add(stackPane);
        //assign style
        stackPane.getStylesheets().add(this.getClass().getResource("wordle.css").toExternalForm());

        pane.heightProperty().addListener(observable
                -> updateLogoPosition());
        pane.widthProperty().addListener(observable
                -> updateLogoPosition());
        getSkinnable().logoProperty().addListener((obs, oldValue, newValue)
                -> updateLogo(newValue));

        pane.heightProperty().addListener(observable
                -> updateSecondLogoPosition());
        pane.widthProperty().addListener(observable
                -> updateSecondLogoPosition());
        getSkinnable().secondLogoProperty().addListener((obs, oldValue, newValue)
                -> updateSecondLogo(newValue));

        getSkinnable().backgroundGraphicProperty().addListener((obs, oldValue, newValue)
                -> updateBackgroundGraphic(newValue));

        updateBackgroundGraphic(getSkinnable().backgroundGraphicProperty().getValue());
        updateLogo(getSkinnable().logoProperty().getValue());
        updateSecondLogo(getSkinnable().secondLogoProperty().getValue());

        favIconsVisible = wordle.favIconsVisibleProperty().get();
        displayCloudTags = wordle.displayedNumberOfTagsProperty().get();
        font = wordle.fontProperty().get();
        fontSizeMin = wordle.fontSizeMinProperty().get();
        fontSizeMax = wordle.fontSizeMaxProperty().get();
        tweetFontSize = wordle.tweetFontSizeProperty().get();
        prepareStepMachine();
    }

    private void updateLogo(final String newLogo) {
        if (null != logo) {
            pane.getChildren().remove(logo);
            logo = null;
        }
        LOGGER.trace("Logo: {}", newLogo);
        if (null != newLogo && !newLogo.isEmpty()) {
            logo = new ImageView(newLogo);
            logo.getStyleClass().add("logo");
            pane.getChildren().add(logo);
            updateLogoPosition();
        }
    }

    private void updateLogoPosition() {
        LOGGER.trace("Updating logo position");
        if (null != logo) {
            logo.setLayoutX(0);
            logo.setLayoutY(pane.getHeight() - logo.getImage().getHeight());
        }
    }

    private void updateSecondLogo(final String newLogo) {
        if (null != secondLogo) {
            pane.getChildren().remove(secondLogo);
            secondLogo = null;
        }
        LOGGER.trace("SecondLogo: {}", newLogo);
        if (null != newLogo && !newLogo.isEmpty()) {
            secondLogo = new ImageView(newLogo);
            secondLogo.getStyleClass().add("secondlogo");
            pane.getChildren().add(secondLogo);
            updateSecondLogoPosition();
        }
    }

    private void updateSecondLogoPosition() {
        LOGGER.trace("Updating secondLogo position");
        if (null != secondLogo) {
            secondLogo.setLayoutX(pane.getWidth() - (secondLogo.getImage().getWidth() * 0.8));
            secondLogo.setLayoutY(pane.getHeight() - (secondLogo.getImage().getHeight() * 0.8));
        }
    }

    private void updateBackgroundGraphic(final String newBackgroundGraphic) {
        if (null != backgroundImage) {
            stackPane.getChildren().remove(backgroundImage);
            backgroundImage = null;
        }
        if (null != newBackgroundGraphic && !newBackgroundGraphic.isEmpty()) {
            backgroundImage = new ImageView(newBackgroundGraphic) {
                @Override
                public double minHeight(double width) {
                    return 10;
                }

                @Override
                public double minWidth(double height) {
                    return 10;
                }
            };

            backgroundImage.getStyleClass().add("bg-image");
            backgroundImage.yProperty().bind(Bindings.divide(Bindings.subtract(stackPane.heightProperty(), backgroundImage.fitHeightProperty()), 0.5));
            backgroundImage.fitWidthProperty().bind(stackPane.widthProperty());
            backgroundImage.fitHeightProperty().bind(stackPane.heightProperty());
            backgroundImage.setPreserveRatio(true);
            backgroundImage.setCache(true);
            backgroundImage.setSmooth(true);

            stackPane.getChildren().add(0, backgroundImage);
        }
    }

    private void prepareStepMachine() {
        LOG.info("Prepare StepMachine");

        final StepEngine s = new StepEngine();
        s.getContext().put("WordleSkin", this);
        s.getContext().put("WallPane", this.getPane());
        s.getContext().put("WallNode", this.getNode());
        LOG.info("Prepare StepMachine done");
        s.go();
    }
}
