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

import org.jetbrains.annotations.NotNull;

import java.nio.file.Path;
import java.nio.file.Paths;

public class PathUtils {

    public static @NotNull Path getAbsolutePath(@NotNull String first, @NotNull String... more) {
        return Paths.get(first, more).toAbsolutePath().normalize();
    }

    public static @NotNull Path getWorkingDirectory() {
        String agentDirectory = System.getProperty("agent.dir");
        if (StringUtils.isNotBlank(agentDirectory)) {
            return getAbsolutePath(agentDirectory);
        }

        String userDirectory = System.getProperty("user.dir");
        if (StringUtils.isNotBlank(userDirectory)) {
            return getAbsolutePath(userDirectory);
        }

        return getAbsolutePath(".");
    }
}