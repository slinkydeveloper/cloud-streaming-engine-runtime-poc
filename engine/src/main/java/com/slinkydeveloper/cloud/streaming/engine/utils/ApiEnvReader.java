package com.slinkydeveloper.cloud.streaming.engine.utils;

import com.slinkydeveloper.cloud.streaming.api.InputStream;
import com.slinkydeveloper.cloud.streaming.api.OutputStream;
import com.slinkydeveloper.cloud.streaming.api.StateStream;
import com.slinkydeveloper.cloud.streaming.api.StreamProcessor;

import java.time.Duration;
import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;
import java.util.function.Function;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class ApiEnvReader {

    public static StreamProcessor readStreamProcessorFromEnv() {
        return new StreamProcessor(
            Arrays
                .stream(getEnv("INPUT_STREAMS", s -> s.split(Pattern.quote(","))).get())
                .map(name -> {
                    if (name.contains(":")) {
                        String[] splitted = name.split(Pattern.quote(":"));
                        if (splitted.length == 2) {
                            return new InputStream(splitted[0], splitted[1], null);
                        } else {
                            return new InputStream(splitted[0], splitted[1], splitted[2]);
                        }
                    }
                    return new InputStream(name, null, null);
                })
                .collect(Collectors.toSet()),
            getEnv("OUTPUT_STREAMS", s -> s.split(Pattern.quote(",")))
                .map(outputs ->
                    Arrays.stream(outputs)
                        .map(name -> {
                            if (name.contains(":")) {
                                String[] splitted = name.split(Pattern.quote(":"));
                                if (splitted.length == 2) {
                                    return new OutputStream(splitted[0], splitted[1], null);
                                } else {
                                    return new OutputStream(splitted[0], splitted[1], splitted[2]);
                                }
                            }
                            return new OutputStream(name, null, null);
                        })
                        .collect(Collectors.toSet())
                )
                .orElse(Collections.emptySet()),
            getEnv("TIMEOUT", Duration::parse).orElse(null),
            null,
            getEnv("STATE_STREAM").map(name -> {
                if (name.contains(":")) {
                    String[] splitted = name.split(Pattern.quote(":"));
                    if (splitted.length == 2) {
                        return new StateStream(splitted[0], splitted[1], null);
                    } else {
                        return new StateStream(splitted[0], splitted[1], splitted[2]);
                    }
                }
                return new StateStream(name, null, null);
            }).orElse(null)
        );
    }

    public static Optional<String> getEnv(String key) {
        return getEnv(key, Function.identity());
    }

    public static <T> Optional<T> getEnv(String key, Function<String, T> mapper) {
        return Optional.ofNullable(System.getenv(key)).map(mapper);
    }

}
