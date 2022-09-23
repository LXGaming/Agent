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

import io.github.lxgaming.agent.asm.annotation.Setting;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Properties;
import java.util.StringJoiner;

public class PropertiesUtils {
    
    public static @NotNull Boolean getBoolean(@Nullable Properties properties, @Nullable Setting... settings) {
        return getBoolean(properties, getSetting(settings));
    }
    
    public static boolean getBoolean(@Nullable Properties properties, @NotNull String key) {
        String value = getString(properties, key);
        return Boolean.parseBoolean(value);
    }
    
    public static @Nullable String getString(@Nullable Properties properties, @Nullable Setting... settings) {
        return getString(properties, getSetting(settings));
    }
    
    public static @Nullable String getString(@Nullable Properties properties, @NotNull String key) {
        if (properties == null) {
            return null;
        }
        
        return properties.getProperty(key);
    }
    
    public static @NotNull String getSetting(@Nullable Setting... settings) {
        StringJoiner stringJoiner = new StringJoiner(".");
        stringJoiner.add("mixin");
        for (Setting setting : settings) {
            if (setting != null && StringUtils.isNotBlank(setting.value())) {
                stringJoiner.add(setting.value());
            }
        }
        
        return stringJoiner.toString();
    }
}