/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2019 TweetWallFX
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
package org.tweetwallfx.stepengine.steps.visual;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.Collection;
import java.util.Objects;
import org.tweetwallfx.stepengine.api.ActionStep;
import org.tweetwallfx.stepengine.api.DataProvider;
import org.tweetwallfx.stepengine.api.Step;
import org.tweetwallfx.stepengine.api.StepEngine;
import org.tweetwallfx.stepengine.api.Visualization;
import org.tweetwallfx.stepengine.api.Visualization.Showable;
import org.tweetwallfx.stepengine.api.VisualizationRegistry;
import org.tweetwallfx.stepengine.api.config.StepEngineSettings;
import static org.tweetwallfx.util.ToString.createToString;
import static org.tweetwallfx.util.ToString.map;

/**
 * A special Step in the {@link StepEngine}. It represents an action triggered against an existing
 * visualization implementing {@link Visualization.Showable}.
 */
public class ShowAction extends ActionStep<Visualization.Showable> {

    private final Config config;

    public ShowAction(Config config, Showable showable) {
        super(showable);
        this.config = config;
    }

    @Override
    public void perform(Visualization.Context context) {
        getVisualization().doShow(context);
    }

    @Override
    public Duration preferredStepDuration(StepEngine.MachineContext context) {
        return super.preferredStepDuration(context);
    }

    @Override
    public String toString() {
        return "ShowAction for " + config.getVisualizationIdentifier();
    };

    /**
     * Implementation of {@link Step.Factory} as Service implementation creating
     * {@link ShowAction}.
     */
    public static final class FactoryImpl implements Step.Factory {

        @Override
        public Step create(final StepEngineSettings.StepDefinition stepDefinition) {
            final Config config = stepDefinition.getConfig(Config.class);
            return new ShowAction(config, VisualizationRegistry.INSTANCE.getVisualization(config.getVisualizationIdentifier(), Visualization.Showable.class));
        }

        @Override
        public Collection<Class<? extends DataProvider>> getRequiredDataProviders(StepEngineSettings.StepDefinition stepDefinition) {
            String visualizationIdentifier = stepDefinition.getConfig(Config.class).getVisualizationIdentifier();
            return VisualizationRegistry.INSTANCE.getRequiredDataProviders(visualizationIdentifier);
        }

        @Override
        public Class<ShowAction> getStepClass() {
            return ShowAction.class;
        }
    }

    public static class Config {

        private ChronoUnit unit = ChronoUnit.SECONDS;
        private long amount = 5;
        private String visualizationIdentifier = "Undefined";

        public ChronoUnit getUnit() {
            return unit;
        }

        public void setUnit(final ChronoUnit unit) {
            this.unit = Objects.requireNonNull(unit, "unit must not be null!");
        }

        public long getAmount() {
            return amount;
        }

        public void setAmount(final long amount) {
            this.amount = amount;
        }

        public String getVisualizationIdentifier() {
            return this.visualizationIdentifier;
        }

        public void setVisualizationIdentifier(String visualizationIdentifier) {
            this.visualizationIdentifier = visualizationIdentifier;
        }

        @Override
        public String toString() {
            return createToString(this, map(
                    "visualizationIdentifier", getVisualizationIdentifier(),
                    "amount", getAmount(),
                    "unit", getUnit()
            )) + " extends " + super.toString();
        }
    }
}
