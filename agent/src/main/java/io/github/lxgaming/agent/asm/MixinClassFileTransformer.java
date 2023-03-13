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

import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.instrument.ClassFileTransformer;
import java.security.ProtectionDomain;

public class MixinClassFileTransformer implements ClassFileTransformer {
    
    protected final Logger logger;
    protected final MixinTransformer transformer;
    
    public MixinClassFileTransformer(@NotNull MixinTransformer transformer) {
        this.logger = LoggerFactory.getLogger(getClass());
        this.transformer = transformer;
    }
    
    @Override
    public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) {
        if (loader == null || className == null) {
            return null;
        }
        
        try {
            byte[] modifiedBytes = transformer.transform(loader, className, classfileBuffer);
            if (modifiedBytes != null) {
                logger.debug("Transformed {}", className);
            }
            
            return modifiedBytes;
        } catch (Throwable t) {
            logger.error("Encountered an error while transforming {}", className, t);
            return null;
        }
    }
}