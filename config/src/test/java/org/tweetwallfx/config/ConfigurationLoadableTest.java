/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2015-2022 TweetWallFX
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
package org.tweetwallfx.config;

import java.util.Map;
import java.util.ServiceLoader;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.assertj.core.api.Assertions;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;

/**
 * Testing if Configuration is safely retrievable.
 */
public class ConfigurationLoadableTest {

    @Rule
    public TestName testName = new TestName();

    @Test
    public void testConfigurationLoadable() {
        final Configuration conf = Configuration.getInstance();
        System.out.println(conf);
        Assertions.assertThat(conf).as("conf is not null").isNotNull();
    }

    @Before
    public void before() {
        System.out.println("#################### START: " + testName.getMethodName() + " ####################");
    }

    @After
    public void after() {
        System.out.println("####################   END: " + testName.getMethodName() + " ####################");
    }

    @Test
    public void testCustomizedConfigurationLoaded() {
        final Configuration configuration = Configuration.getInstance();
        Assertions.assertThat(configuration)
                .as("configuration is not null").isNotNull();

        // key test is only available if file 'src/test/resources/myCustomConfig.json' was loaded by Configuration
        final Object testConfig = configuration.getConfig("test");

        Assertions.assertThat(testConfig)
                .isNotNull()
                .isInstanceOf(Map.class);

        @SuppressWarnings("unchecked")
        final Map<String, Object> testConfigMap = (Map<String, Object>) testConfig;

        Assertions.assertThat(testConfigMap)
                .isNotNull()
                .containsOnlyKeys("fileName")
                .containsEntry("fileName", "myCustomConfig.json");

        for (final ConfigurationConverter o : ServiceLoader.load(ConfigurationConverter.class)) {
            System.out.println("Config Key " + o.getResponsibleKey() + " with type " + o.getDataClass());

            try {
                System.out.println(configuration.getConfigTyped(o.getResponsibleKey(), o.getDataClass()));
            } catch (RuntimeException npe) {
                System.out.println("value not found");
            }
        }
    }
}
