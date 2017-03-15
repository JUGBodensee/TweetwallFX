/*
 * The MIT License
 *
 * Copyright 2014-2016 TweetWallFX
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
package org.tweetwallfx.controls.steps;

import org.tweetwallfx.controls.WordleSkin;
import org.tweetwallfx.controls.dataprovider.TweetDataProvider;
import org.tweetwallfx.controls.stepengine.AbstractStep;
import org.tweetwallfx.controls.stepengine.StepEngine.MachineContext;

/**
 *
 * @author Sven
 */
public class NextTweetStep extends AbstractStep {

    @Override
    public long preferredStepDuration(MachineContext context) {
        return 0;
    }

    @Override
    public void doStep(MachineContext context) {
        WordleSkin skin = (WordleSkin) context.get("WordleSkin");
        skin.getSkinnable().getDataProvider(TweetDataProvider.class).nextTweet();
        context.proceed();
    }
}
