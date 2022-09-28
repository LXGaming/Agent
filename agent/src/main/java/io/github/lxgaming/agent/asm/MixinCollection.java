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
import io.github.lxgaming.agent.asm.annotation.Setting;
import io.github.lxgaming.agent.asm.annotation.Visit;
import io.github.lxgaming.agent.asm.annotation.VisitFieldInsn;
import io.github.lxgaming.agent.asm.annotation.VisitMethod;
import io.github.lxgaming.agent.asm.annotation.VisitMethodInsn;
import io.github.lxgaming.agent.asm.exception.MixinException;
import io.github.lxgaming.agent.util.ASMUtils;
import io.github.lxgaming.agent.util.ConfigUtils;
import io.github.lxgaming.agent.util.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.annotation.Annotation;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;

public class MixinCollection {
    
    protected static final MethodHandles.Lookup LOOKUP;
    protected static final Map<Class<? extends Annotation>, Class<?>[]> MAPPINGS;
    protected static final Map<Class<? extends Annotation>, Class<?>[]> INSN_MAPPINGS;
    
    static {
        LOOKUP = MethodHandles.lookup();
        
        MAPPINGS = new LinkedHashMap<>();
        MAPPINGS.put(VisitMethod.class, new Class[]{ClassNode.class, MethodNode.class});
        
        INSN_MAPPINGS = new LinkedHashMap<>();
        INSN_MAPPINGS.put(VisitFieldInsn.class, new Class[]{ClassNode.class, MethodNode.class, FieldInsnNode.class});
        INSN_MAPPINGS.put(VisitMethodInsn.class, new Class[]{ClassNode.class, MethodNode.class, MethodInsnNode.class});
    }
    
    protected final Collection<Class<?>> classes;
    protected final Collection<MixinDescriptor> descriptors;
    protected final Logger logger;
    protected Config config;
    
    public MixinCollection(@Nullable Config config) {
        this(new HashSet<>(), new LinkedHashSet<>());
        this.config = config;
    }
    
    protected MixinCollection(@NotNull Collection<Class<?>> classes, @NotNull Collection<MixinDescriptor> descriptors) {
        this.classes = classes;
        this.descriptors = descriptors;
        this.logger = LoggerFactory.getLogger(getClass());
    }
    
    /**
     * Clears the mixins from this {@link MixinCollection}.
     */
    public void clear() {
        descriptors.clear();
    }
    
    /**
     * Creates a {@link MixinTransformer} containing the mixins from this {@link MixinCollection}.
     *
     * @return the default {@link MixinTransformer} implementation
     */
    public @NotNull MixinTransformer buildMixinTransformer() {
        return new MixinTransformer(new LinkedHashSet<>(descriptors), config);
    }
    
    /**
     * Registers mixins of the class specified in {@code mixinClass},
     * which must be annotated with {@link Visit}.
     *
     * @param mixinClass The mixin class
     * @return {@code true} if the specified mixin class is successfully registered, otherwise {@code false}
     */
    public boolean register(@NotNull Class<?> mixinClass) {
        try {
            add(mixinClass);
            logger.debug("Registered {}", mixinClass);
            return true;
        } catch (Throwable t) {
            logger.error("Encountered an error while registering {}", mixinClass, t);
            return false;
        }
    }
    
    /**
     * Adds mixins of the class specified in {@code mixinClass},
     * which must be annotated with {@link Visit}.
     *
     * @param mixinClass The mixin class
     * @return this {@link MixinCollection} for chaining
     * @throws Exception if an exception was encountered while adding
     */
    public MixinCollection add(Class<?> mixinClass) throws Exception {
        if (classes.contains(mixinClass)) {
            throw new IllegalArgumentException(String.format(
                    "%s is already registered",
                    mixinClass
            ));
        }
        
        classes.add(mixinClass);
        
        if (!mixinClass.isAnnotationPresent(Visit.class)) {
            throw new MixinException(String.format(
                    "Invalid class %s! Missing visitor annotation",
                    mixinClass.getName()
            ));
        }
        
        Object instance = createInstance(mixinClass);
        
        Collection<MixinDescriptor> descriptors = getDescriptors(mixinClass, instance);
        
        setFields(mixinClass, instance);
        
        this.descriptors.addAll(descriptors);
        return this;
    }
    
    protected @Nullable Object createInstance(@NotNull Class<?> mixinClass) throws ReflectiveOperationException {
        if (Modifier.isAbstract(mixinClass.getModifiers()) || Modifier.isInterface(mixinClass.getModifiers())) {
            return null;
        }
        
        Constructor<?> constructor = mixinClass.getDeclaredConstructor();
        constructor.setAccessible(true);
        return constructor.newInstance();
    }
    
    protected void setFields(@NotNull Class<?> clazz, @Nullable Object instance) throws ReflectiveOperationException {
        Setting classSetting = clazz.getDeclaredAnnotation(Setting.class);
        
        for (Field field : clazz.getDeclaredFields()) {
            Setting fieldSetting = field.getDeclaredAnnotation(Setting.class);
            if (fieldSetting == null) {
                continue;
            }
            
            if (!String.class.isAssignableFrom(field.getType())) {
                throw new MixinException(String.format(
                        "Invalid descriptor %s.%s! Expected %s but found %s",
                        clazz.getName(),
                        field.getName(),
                        Type.getDescriptor(String.class),
                        Type.getDescriptor(field.getType())
                ));
            }
            
            if (!Modifier.isStatic(field.getModifiers()) && instance == null) {
                throw new MixinException(String.format(
                        "Invalid field %s.%s! Expected static but found instance",
                        clazz.getName(),
                        field.getName()
                ));
            }
            
            if (Modifier.isFinal(field.getModifiers())) {
                if (Modifier.isStatic(field.getModifiers())) {
                    throw new MixinException(String.format(
                            "Invalid field %s.%s! Compile-time constant",
                            clazz.getName(),
                            field.getName()
                    ));
                }
                
                Field modifiersField = Field.class.getDeclaredField("modifiers");
                modifiersField.setAccessible(true);
                modifiersField.setInt(field, field.getModifiers() & ~Modifier.FINAL);
            }
            
            String setting = ConfigUtils.getString(config, classSetting, fieldSetting);
            if (StringUtils.isBlank(setting)) {
                continue;
            }
            
            field.setAccessible(true);
            field.set(instance, setting);
        }
    }
    
    protected @NotNull Collection<MixinDescriptor> getDescriptors(@NotNull Class<?> clazz, @Nullable Object instance) throws IllegalAccessException {
        Visit visit = clazz.getDeclaredAnnotation(Visit.class);
        if (visit == null) {
            throw new MixinException(String.format(
                    "Invalid class %s! Missing visitor annotation",
                    clazz.getName()
            ));
        }
        
        Collection<MixinDescriptor> descriptors = new ArrayList<>();
        for (Method method : clazz.getDeclaredMethods()) {
            for (VisitMethod visitMethod : method.getDeclaredAnnotationsByType(VisitMethod.class)) {
                boolean hasVisitor = false;
                for (Map.Entry<Class<? extends Annotation>, Class<?>[]> entry : INSN_MAPPINGS.entrySet()) {
                    Annotation visitInsnAnnotation = method.getDeclaredAnnotation(entry.getKey());
                    if (visitInsnAnnotation == null) {
                        continue;
                    }
                    
                    if (hasVisitor) {
                        throw new MixinException(String.format(
                                "Invalid annotations on %s.%s! Cannot have multiple instruction visitors",
                                clazz.getName(),
                                method.getName()
                        ));
                    }
                    
                    descriptors.add(createMixinDescriptor(clazz, method, visit, visitMethod, visitInsnAnnotation, instance));
                    hasVisitor = true;
                }
                
                if (!hasVisitor) {
                    descriptors.add(createMixinDescriptor(clazz, method, visit, visitMethod, null, instance));
                }
            }
        }
        
        return descriptors;
    }
    
    protected @NotNull MixinDescriptor createMixinDescriptor(@NotNull Class<?> clazz,
                                                             @NotNull Method method,
                                                             @NotNull Visit visit,
                                                             @NotNull VisitMethod visitMethod,
                                                             @Nullable Annotation visitInsnAnnotation,
                                                             @Nullable Object instance) throws IllegalAccessException {
        if (Modifier.isAbstract(method.getModifiers())) {
            throw new MixinException(String.format(
                    "Invalid method %s.%s! Cannot be abstract",
                    clazz.getName(),
                    method.getName()
            ));
        }
        
        if (!Modifier.isStatic(method.getModifiers()) && instance == null) {
            throw new MixinException(String.format(
                    "Invalid method %s.%s! Expected static but found instance",
                    clazz.getName(),
                    method.getName()
            ));
        }
        
        Class<?>[] parameterTypes;
        if (visitInsnAnnotation != null) {
            parameterTypes = INSN_MAPPINGS.get(visitInsnAnnotation.annotationType());
        } else {
            parameterTypes = MAPPINGS.get(visitMethod.annotationType());
        }
        
        if (!Arrays.equals(method.getParameterTypes(), parameterTypes) || method.getReturnType() != Void.TYPE) {
            throw new MixinException(String.format(
                    "Invalid descriptor on %s.%s! Expected %s but found %s",
                    clazz.getName(),
                    method.getName(),
                    Type.getMethodDescriptor(Type.VOID_TYPE, ASMUtils.getTypes(parameterTypes)),
                    Type.getMethodDescriptor(method)
            ));
        }
        
        Boolean setting = ConfigUtils.getBoolean(config,
                clazz.getDeclaredAnnotation(Setting.class),
                method.getDeclaredAnnotation(Setting.class)
        );
        
        method.setAccessible(true);
        MethodHandle methodHandle = LOOKUP.unreflect(method);
        if (Modifier.isStatic(method.getModifiers())) {
            return new MixinDescriptor(clazz, method, setting, visit, visitMethod, methodHandle, visitInsnAnnotation, null);
        }
        
        return new MixinDescriptor(clazz, method, setting, visit, visitMethod, methodHandle, visitInsnAnnotation, instance);
    }
}