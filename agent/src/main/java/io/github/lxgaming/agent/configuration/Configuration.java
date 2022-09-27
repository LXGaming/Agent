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
import java.util.Properties;

public class Configuration {
    
    protected final Path configPath;
    protected Properties config;
    
    public Configuration(Path path) {
        this.configPath = path.resolve("agent.properties");
    }
    
    public void loadConfiguration() throws IOException {
        this.config = deserializeFile(configPath);
    }
    
    public void saveConfiguration() throws IOException {
        serializeFileAsync(configPath, config);
    }
    
    public @Nullable Properties getConfig() {
        return config;
    }
    
    protected @NotNull Properties deserializeFile(@NotNull Path path) throws IOException {
        Properties properties = new Properties();
        
        InputStream inputStream = ClassLoader.getSystemResourceAsStream(path.getFileName().toString());
        if (inputStream != null) {
            try (Reader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
                properties.load(reader);
            }
        }
        
        if (Files.exists(path)) {
            try (Reader reader = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
                properties.load(reader);
            }
        }
        
        return properties;
    }
    
    protected void serializeFileAsync(@NotNull Path path, @NotNull Properties properties) throws IOException {
        Path parent = path.getParent();
        if (parent != null && !Files.exists(parent)) {
            Files.createDirectories(parent);
        }
        
        try (Writer writer = Files.newBufferedWriter(path, StandardCharsets.UTF_8)) {
            properties.store(writer, null);
        }
    }
}