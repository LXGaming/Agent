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
import org.jetbrains.annotations.Nullable;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.ClassNode;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class ClassWriterImpl extends ClassWriter {
    
    protected static final Map<String, String> CLASS_PARENTS = new ConcurrentHashMap<>();
    protected static final Map<String, Set<String>> CLASS_HIERARCHIES = new ConcurrentHashMap<>();
    protected static final Map<String, Boolean> IS_INTERFACE = new ConcurrentHashMap<>();
    
    protected final ClassLoader classLoader;
    protected final ClassNode classNode;
    protected boolean computed;
    
    public ClassWriterImpl(int flags, @NotNull ClassLoader classLoader, @NotNull ClassNode classNode) {
        super(flags);
        this.classLoader = classLoader;
        this.classNode = classNode;
    }
    
    @Override
    protected String getCommonSuperClass(String type1, String type2) {
        if (!computed) {
            computeHierarchy(classNode);
            this.computed = true;
        }
        
        if (getSupers(type2).contains(type1)) {
            return type1;
        }
        
        if (getSupers(type1).contains(type2)) {
            return type2;
        }
        
        if (isInterface(type1) || isInterface(type2)) {
            return "java/lang/Object";
        }
        
        String type = type1;
        do {
            type = getSuper(type);
        } while (!getSupers(type2).contains(type));
        
        return type;
    }
    
    @Override
    protected ClassLoader getClassLoader() {
        return classLoader;
    }
    
    protected @NotNull String getSuper(@NotNull String typeName) {
        computeHierarchy(typeName);
        return CLASS_PARENTS.get(typeName);
    }
    
    protected @NotNull Set<String> getSupers(@NotNull String typeName) {
        computeHierarchy(typeName);
        return CLASS_HIERARCHIES.get(typeName);
    }
    
    protected boolean isInterface(@NotNull String typeName) {
        return IS_INTERFACE.get(typeName);
    }
    
    protected void computeHierarchy(@NotNull ClassNode classNode) {
        if (CLASS_HIERARCHIES.containsKey(classNode.name)) {
            return;
        }
        
        computeHierarchy(classNode.access, classNode.name, classNode.superName, classNode.interfaces.toArray(new String[0]));
    }
    
    protected void computeHierarchy(@NotNull String typeName) {
        if (CLASS_HIERARCHIES.containsKey(typeName)) {
            return;
        }
        
        ClassReader classReader;
        try (InputStream inputStream = getResourceAsStream(typeName + ".class")) {
            classReader = new ClassReader(inputStream);
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
        
        computeHierarchy(classReader.getAccess(), typeName, classReader.getSuperName(), classReader.getInterfaces());
    }
    
    protected void computeHierarchy(int access, @NotNull String name, @Nullable String superName, @NotNull String[] interfaces) {
        Set<String> hierarchies = new HashSet<>();
        if (superName != null) {
            CLASS_PARENTS.put(name, superName);
            computeHierarchy(superName);
            hierarchies.add(name);
            hierarchies.addAll(CLASS_HIERARCHIES.get(superName));
        } else {
            hierarchies.add("java/lang/Object");
        }
        
        IS_INTERFACE.put(name, (access & Opcodes.ACC_INTERFACE) != 0);
        for (String interfaceName : interfaces) {
            computeHierarchy(interfaceName);
            hierarchies.add(interfaceName);
            hierarchies.addAll(CLASS_HIERARCHIES.get(interfaceName));
        }
        
        CLASS_HIERARCHIES.put(name, hierarchies);
    }
    
    protected @NotNull InputStream getResourceAsStream(@NotNull String name) throws IOException {
        InputStream resource = getClassLoader().getResourceAsStream(name);
        if (resource != null) {
            return resource;
        }
        
        InputStream systemResource = ClassLoader.getSystemResourceAsStream(name);
        if (systemResource != null) {
            return systemResource;
        }
        
        throw new IOException(String.format("Resource %s not found", name));
    }
}