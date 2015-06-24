/*
 * The MIT License
 *
 * Copyright 2014-2015 TweetWallFX
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
package org.tweetwallfx.cmdargs;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import java.util.Collection;
import java.util.Map;
import java.util.TreeMap;
import java.util.function.Function;
import org.openide.util.Lookup;

/**
 * Helper utility used for parsing the command line arguments.
 *
 * @author martin
 */
public final class CommandLineArgumentParser {

    @Parameter(names = "-help", help = true, description = "Display this help")
    private boolean helpAndExit;
    @Parameter(names = "--helpMe", help = true, hidden = true, description = "Display this help")
    private boolean help;
    private static final CommandLineArgumentParser HELPER = new CommandLineArgumentParser();

    private CommandLineArgumentParser() {
    }

    /**
     * Parse the {@code args} and process the Parameters.
     *
     * @param args the command line arguments to parse
     */
    public static void parseArguments(final Collection<String> args) {
        if (null == args || args.isEmpty()) {
            return;
        }

        parseArguments(args.toArray(new String[args.size()]));
    }

    /**
     * Parse the {@code args} and process the Parameters.
     *
     * @param args the command line arguments to parse
     */
    public static void parseArguments(final String... args) {
        if (null == args || 0 == args.length) {
            return;
        }

        final JCommander jCommander = new JCommander();
        final Map<String, JCommander> commands = new TreeMap<>();
        final Function<String, JCommander> mapAdder = s -> {
            jCommander.addCommand(s);
            return commands.get(s);
        };

        Lookup.getDefault()
                .lookupAll(ParametersObject.class).stream()
                .peek(po -> {
                    if (null != po.getCommand()) {
                        commands.computeIfAbsent(po.getCommand(), mapAdder).addObject(po);
                    }
                })
                .forEach(po -> {
                    if (null == po.getCommand()) {
                        jCommander.addObject(po);
                    }
                });

        jCommander.addObject(HELPER);
        jCommander.parse(args);

        if (HELPER.help) {
            jCommander.usage();
        } else if (HELPER.helpAndExit) {
            jCommander.usage();
            System.exit(0);
        }
    }

    /**
     * Marker interface used for retrieving commandline arguments from instance.
     */
    public interface ParametersObject {

        default String getCommand() {
            return null;
        }
    }
}
