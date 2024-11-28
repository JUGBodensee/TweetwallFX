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
package org.tweetwallfx.stepengine.api;

import java.util.Collection;
import java.util.Collections;
import javafx.scene.layout.Pane;
import org.tweetwallfx.stepengine.api.StepEngine.MachineContext;
import org.tweetwallfx.stepengine.api.config.StepEngineSettings;

/**
 * A Visualization, which can be orchestrated/directed using {@link ActionStep}
 */
public interface Visualization {

    /**
     * A Visualization which can be shown using an {@link ActionStep}.
     */
    public static interface Showable extends Visualization {

        /**
         * Shows the visualization. This method will not be called on the
         * FXApplication Thread.
         *
         * @param context the Visualization.Context
         */
        public void doShow(Context context);
    }

    /**
     * A Visualization which can be hidden using an {@link ActionStep}.
     */
    public static interface Hideable extends Visualization {

        /**
         * Hides the visualization. This method will not be called on the
         * FXApplication Thread.
         *
         * @param context the Visualization.Context
         */
        public void doHide(Context context);
    }

    /**
     * A context passed to state changing methods of a visualization as part of
     * the step engine triggers. The actual implementation delegates to the
     * associated MachineContext from the StepEngine, but this may change in the future
     */
    public static final class Context {

        private final MachineContext machineContext;

        /**
         * Creates a new context using the step engine {@link MachineContext}.
         * @param machineContext the context to delgate to
         * @return the new context
         */
        static final Context create(MachineContext machineContext) {
            return new Context(machineContext);
        }

        private Context(MachineContext machineContext) {
            this.machineContext = machineContext;
        }

        /**
         * Returns a {@link DataProvider} as per requested class.
         * @param <T> genric type of the {@link DataProvider}
         * @param klazz class describing the {@link DataProvider}
         * @return a {@link DataProvider} instance
         */
        public <T extends DataProvider> T getDataProvider(final Class<T> klazz) {
            return machineContext.getDataProvider(klazz);
        }

        /**
         * Returns the {@link Pane} to interact with for showing {@link Visualization}s
         * @return the pane to interact with
         */
        public Pane getWallPane() {
            return machineContext.get("WallPane", Pane.class);
        }
    }

    /**
     * A Factory creating a {@link Visualization}.
     */
    interface Factory {

        /**
         * Returns the class of the Visualization this factory will create via
         * {@link #create(org.tweetwallfx.stepengine.api.config.StepEngineSettings.VisualizationSetting)}.
         *
         * @return the class of the Visualization this factory will create
         */
        Class<? extends Visualization> getVisualizationClass();

        /**
         * Creates a Visualization.
         *
         * @param visualizationSetting the settings object for the
         * {@link Visualization} about to be created.
         *
         * @return the created Visualization
         */
        Visualization create(final StepEngineSettings.VisualizationSetting visualizationSetting);

        /**
         * Retrieves the classes of {@link DataProvider DataProviders} required
         * by the created {@link Visualization}.
         * <p>
         *
         * <b>Important: Any {@link DataProvider} that is used must be contained
         * within the returned Collection in order to be certain that the
         * DataProvider is instantiated. If it is not declared to be required it
         * is highly likely that it will not be instantiated and thus produce a
         * {@link NullPointerException} downstream.</b>
         *
         * @param visualizationSetting the settings object for which the
         * Visualization is to be created
         *
         * @return a Collection containing the classes of
         * {@link DataProvider DataProviders} required by the created
         * {@link Visualization}
         */
        default Collection<Class<? extends DataProvider>> getRequiredDataProviders(final StepEngineSettings.VisualizationSetting visualizationSetting) {
            return Collections.emptyList();
        }
    }
}
