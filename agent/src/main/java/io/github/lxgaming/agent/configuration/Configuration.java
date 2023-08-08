/*
 * Copyright 2022 Alex Thomson
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.github.lxgaming.agent.configuration;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import com.typesafe.config.ConfigParseOptions;
import com.typesafe.config.ConfigRenderOptions;
import com.typesafe.config.ConfigResolveOptions;
import io.github.lxgaming.agent.util.PathUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

public class Configuration {

    protected static final ConfigParseOptions DEFAULT_PARSE_OPTIONS = ConfigParseOptions.defaults();
    protected static final ConfigRenderOptions DEFAULT_RENDER_OPTIONS = ConfigRenderOptions.defaults()
            .setOriginComments(false)
            .setJson(false);
    protected static final ConfigResolveOptions DEFAULT_RESOLVE_OPTIONS = ConfigResolveOptions.defaults();

    protected final Path configPath;
    protected Config config;
    protected Config overrideConfig;

    public Configuration() {
        this(PathUtils.getWorkingDirectory().resolve("agent.conf"));
    }

    public Configuration(Path configPath) {
        this.configPath = configPath;
    }

    public void loadConfiguration() throws IOException {
        Config config = deserializeFile(configPath);
        this.overrideConfig = ConfigFactory.defaultOverrides().withFallback(config).resolve(DEFAULT_RESOLVE_OPTIONS);
        this.config = config.resolveWith(overrideConfig, DEFAULT_RESOLVE_OPTIONS);
    }

    public void saveConfiguration() throws IOException {
        serializeFileAsync(configPath, config);
    }

    public @Nullable Config getConfig() {
        return config;
    }

    public @Nullable Config getOverrideConfig() {
        return overrideConfig;
    }

    protected @NotNull Config deserializeFile(@NotNull Path path) throws IOException {
        Config config = ConfigFactory.empty();

        if (Files.exists(path)) {
            try (Reader reader = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
                config = config.withFallback(ConfigFactory.parseReader(reader, DEFAULT_PARSE_OPTIONS));
            }
        }

        InputStream inputStream = ClassLoader.getSystemResourceAsStream(path.getFileName().toString());
        if (inputStream != null) {
            try (Reader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
                config = config.withFallback(ConfigFactory.parseReader(reader, DEFAULT_PARSE_OPTIONS));
            }
        }

        return config;
    }

    protected void serializeFileAsync(@NotNull Path path, @NotNull Config config) throws IOException {
        Path parent = path.getParent();
        if (parent != null && !Files.exists(parent)) {
            Files.createDirectories(parent);
        }

        try (Writer writer = Files.newBufferedWriter(path, StandardCharsets.UTF_8)) {
            writer.write(config.root().render(DEFAULT_RENDER_OPTIONS));
        }
    }
}