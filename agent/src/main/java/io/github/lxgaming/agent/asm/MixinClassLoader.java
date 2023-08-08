/*
 * Copyright 2023 Alex Thomson
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

import io.github.lxgaming.agent.util.ASMUtils;
import io.github.lxgaming.agent.util.IOUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.function.Predicate;

public class MixinClassLoader extends ClassLoader {

    static {
        ClassLoader.registerAsParallelCapable();
    }

    protected final Logger logger;
    protected final ClassLoader platformClassLoader;
    protected final MixinTransformer transformer;
    protected Predicate<String> transformerFilter;

    public MixinClassLoader(@NotNull MixinTransformer transformer) {
        this(getSystemClassLoader(), transformer);
    }

    public MixinClassLoader(@Nullable ClassLoader parent, @NotNull MixinTransformer transformer) {
        super(parent);
        this.logger = LoggerFactory.getLogger(getClass());
        this.platformClassLoader = ClassLoader.getSystemClassLoader().getParent();
        this.transformer = transformer;
        this.transformerFilter = name -> true;
    }

    public void addTransformerFilter(@NotNull Predicate<String> filter) {
        this.transformerFilter = transformerFilter.and(filter);
    }

    @Override
    protected Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
        synchronized (getClassLoadingLock(name)) {
            Class<?> c = findLoadedClass(name);
            if (c == null) {
                try {
                    c = platformClassLoader.loadClass(name);
                } catch (ClassNotFoundException ex) {
                    // no-op
                }
            }

            if (c == null) {
                if (transformerFilter.test(name)) {
                    String internalName = ASMUtils.getInternalName(name);
                    byte[] bytes = transform(internalName);
                    c = defineClass(name, bytes, 0, bytes.length);
                } else {
                    c = super.loadClass(name, resolve);
                }
            }

            if (resolve) {
                resolveClass(c);
            }

            return c;
        }
    }

    protected byte[] transform(@NotNull String name) throws ClassNotFoundException {
        byte[] bytes;
        try (InputStream inputStream = getResourceAsStream(name + ".class")) {
            if (inputStream == null) {
                throw new IOException(String.format("Resource %s not found", name));
            }

            bytes = IOUtils.readAllBytes(inputStream);
        } catch (IOException ex) {
            logger.error("Encountered an error while reading {}", name, ex);
            throw new ClassNotFoundException(name);
        }

        try {
            byte[] modifiedBytes = transformer.transform(this, name, bytes);
            if (modifiedBytes != null) {
                logger.debug("Transformed {}", name);
                return modifiedBytes;
            }
        } catch (Throwable t) {
            logger.error("Encountered an error while transforming {}", name, t);
        }

        return bytes;
    }
}