/*
 * Copyright 2019 GridGain Systems, Inc. and Contributors.
 *
 * Licensed under the GridGain Community Edition License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.gridgain.com/products/software/community-edition/gridgain-community-edition-license
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.ignite.development.utils.arguments;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import org.apache.ignite.IgniteException;
import org.apache.ignite.internal.util.GridStringBuilder;

import static java.util.stream.Collectors.toSet;

/**
 * Parser for command line arguments.
 */
public class CLIArgumentParser {
    /** */
    private final Map<String, CLIArgument> argConfiguration = new LinkedHashMap<>();

    /** */
    private final Map<String, Object> parsedArgs = new HashMap<>();

    /** */
    public CLIArgumentParser(List<CLIArgument> argConfiguration) {
        for (CLIArgument cliArgument : argConfiguration)
            this.argConfiguration.put(cliArgument.name(), cliArgument);
    }

    /**
     * Parses arguments using iterator. Parsed argument value are available through {@link #get(CLIArgument)}
     * and {@link #get(String)}.
     *
     * @param argsIter Iterator.
     */
    public void parse(Iterator<String> argsIter) {
        Set<String> obligatoryArgs =
            argConfiguration.values().stream().filter(a -> !a.optional()).map(CLIArgument::name).collect(toSet());

        while (argsIter.hasNext()) {
            String arg = argsIter.next();

            CLIArgument cliArg = argConfiguration.get(arg);

            if (cliArg == null)
                throw new IgniteException("Unexpected argument: " + arg);

            if (cliArg.type().equals(Boolean.class))
                parsedArgs.put(cliArg.name(), true);
            else {
                if (!argsIter.hasNext())
                    throw new IgniteException("Please specify a value for argument: " + arg);

                String strVal = argsIter.next();

                parsedArgs.put(cliArg.name(), parseVal(strVal, cliArg.type()));
            }

            obligatoryArgs.remove(cliArg.name());
        }

        if (!obligatoryArgs.isEmpty())
            throw new IgniteException("Mandatory argument(s) missing: " + obligatoryArgs);
    }

    /** */
    private Object parseVal(String val, Class type) {
        switch (type.getSimpleName()) {
            case "String": return val;

            case "String[]": return val.split(",");

            case "Integer": return Integer.parseInt(val);

            case "Long": return Long.parseLong(val);

            case "UUID": return UUID.fromString(val);

            default: throw new IgniteException("Unsupported argument type: " + type.getName());
        }
    }

    /**
     * Get parsed argument value.
     *
     * @param arg Argument configuration.
     * @param <T> Value type.
     * @return Value.
     */
    public <T> T get(CLIArgument arg) {
        Object val = parsedArgs.get(arg.name());

        if (val == null)
            return (T)arg.defaultValueSupplier().get();
        else
            return (T)val;
    }

    /**
     * Get parsed argument value.
     *
     * @param name Argument name.
     * @param <T> Value type.
     * @return Value.
     */
    public <T> T get(String name) {
        CLIArgument arg = argConfiguration.get(name);

        if (arg == null)
            throw new IgniteException("No such argument: " + name);

        return get(arg);
    }

    /**
     * Returns usage description.
     *
     * @return Usage.
     */
    public String usage() {
        GridStringBuilder sb = new GridStringBuilder("Usage: ");

        for (CLIArgument arg : argConfiguration.values())
            sb.a(argNameForUsage(arg)).a(" ");

        for (CLIArgument arg : argConfiguration.values()) {
            Object dfltVal = null;

            try {
                dfltVal = arg.defaultValueSupplier().get();
            }
            catch (Exception ignored) {
                /* No op. */
            }

            sb.a("\n\n").a(arg.name()).a(": ").a(arg.usage());

            if (arg.optional())
                sb.a(" Default value: ").a(dfltVal);
        }

        return sb.toString();
    }

    /** */
    private String argNameForUsage(CLIArgument arg) {
        if (arg.optional())
            return "[" + arg.name() + "]";
        else
            return arg.name();
    }
}
