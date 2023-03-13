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

import com.typesafe.config.Config;
import io.github.lxgaming.agent.util.ConfigUtils;
import io.github.lxgaming.agent.util.MixinUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;

public class MixinTransformer {
    
    protected final Collection<MixinDescriptor> descriptors;
    protected final Logger logger;
    protected Config config;
    
    public MixinTransformer(@NotNull Collection<MixinDescriptor> descriptors, @Nullable Config config) {
        this(descriptors);
        this.config = config;
    }
    
    public MixinTransformer(@NotNull Collection<MixinDescriptor> descriptors) {
        this.descriptors = descriptors;
        this.logger = LoggerFactory.getLogger(getClass());
    }
    
    public byte[] transform(@NotNull ClassLoader classLoader, @NotNull String name, byte[] bytes) throws Throwable {
        ClassNode classNode = null;
        boolean modified = false;
        for (MixinDescriptor descriptor : descriptors) {
            if (descriptor.getSetting() != Boolean.TRUE) {
                continue;
            }
            
            if (!MixinUtils.canVisit(name, descriptor.getVisit())) {
                continue;
            }
            
            if (classNode == null) {
                classNode = new ClassNode();
                ClassReader classReader = new ClassReader(bytes);
                classReader.accept(classNode, ClassReader.SKIP_DEBUG);
            }
            
            if (!MixinUtils.canVisit(classNode, descriptor.getVisit())) {
                continue;
            }
            
            for (MethodNode methodNode : classNode.methods) {
                if (!MixinUtils.canVisit(methodNode, descriptor.getVisitMethod())) {
                    continue;
                }
                
                if (descriptor.getVisitInsnAnnotation() == null) {
                    descriptor.visit(classNode, methodNode);
                    modified = true;
                    continue;
                }
                
                for (AbstractInsnNode insnNode : methodNode.instructions) {
                    if (MixinUtils.canVisit(insnNode, descriptor.getVisitInsnAnnotation())) {
                        descriptor.visit(classNode, methodNode, insnNode);
                        modified = true;
                    }
                }
            }
        }
        
        if (classNode == null || !modified) {
            return null;
        }
        
        ClassWriter classWriter = new ClassWriterImpl(ClassWriter.COMPUTE_FRAMES, classLoader, classNode);
        classNode.accept(classWriter);
        
        byte[] modifiedBytes = classWriter.toByteArray();
        export(name, modifiedBytes);
        return modifiedBytes;
    }
    
    protected void export(@NotNull String name, byte[] bytes) {
        try {
            Path exportPath = ConfigUtils.getPath(config, "debug.export-dir");
            if (exportPath == null || ConfigUtils.getBoolean(config, "debug.export") != Boolean.TRUE) {
                return;
            }
            
            Path path = exportPath.resolve(name + ".class");
            Path parent = path.getParent();
            if (parent != null && !Files.exists(parent)) {
                Files.createDirectories(parent);
            }
            
            Files.write(path, bytes);
            logger.info("Exported {}", path);
        } catch (IOException ex) {
            logger.error("Encountered an error while exporting {}", name, ex);
        }
    }
}