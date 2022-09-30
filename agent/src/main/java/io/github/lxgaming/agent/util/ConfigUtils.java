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

package io.github.lxgaming.agent.util;

import com.typesafe.config.Config;
import io.github.lxgaming.agent.asm.annotation.Setting;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.nio.file.Path;
import java.util.StringJoiner;

public class ConfigUtils {
    
    public static boolean getBoolean(@Nullable Config config, @Nullable Setting... settings) {
        if (config == null) {
            return false;
        }
        
        String path = getSetting(settings);
        return StringUtils.isNotBlank(path) && getBoolean(config, path);
    }
    
    public static boolean getBoolean(@Nullable Config config, @NotNull String path) {
        return config != null && config.getBoolean(path);
    }
    
    public static @Nullable Path getPath(@Nullable Config config, @NotNull String path) {
        String value = getString(config, path);
        return StringUtils.isNotBlank(value) ? PathUtils.getWorkingDirectory().resolve(value) : null;
    }
    
    public static @Nullable String getString(@Nullable Config config, @Nullable Setting... settings) {
        if (config == null) {
            return null;
        }
        
        String path = getSetting(settings);
        return StringUtils.isNotBlank(path) ? getString(config, path) : null;
    }
    
    public static @Nullable String getString(@Nullable Config config, @NotNull String path) {
        return config != null ? config.getString(path) : null;
    }
    
    public static @Nullable String getSetting(@Nullable Setting... settings) {
        if (settings == null || settings.length == 0) {
            return null;
        }
        
        StringJoiner stringJoiner = new StringJoiner(".");
        for (Setting setting : settings) {
            if (setting != null && StringUtils.isNotBlank(setting.value())) {
                stringJoiner.add(setting.value());
            }
        }
        
        String value = stringJoiner.toString();
        return StringUtils.isNotBlank(value) ? "mixin." + value : null;
    }
}