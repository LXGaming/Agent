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

package io.github.lxgaming.agent.asm;

import org.jetbrains.annotations.NotNull;
import org.objectweb.asm.ClassWriter;

public class ClassWriterImpl extends ClassWriter {
    
    protected final ClassLoader classLoader;
    
    public ClassWriterImpl(int flags, @NotNull ClassLoader classLoader) {
        super(flags);
        this.classLoader = classLoader;
    }
    
    @Override
    protected @NotNull ClassLoader getClassLoader() {
        return classLoader;
    }
}