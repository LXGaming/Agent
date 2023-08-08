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

import io.github.lxgaming.agent.asm.annotation.Visit;
import io.github.lxgaming.agent.asm.annotation.VisitMethod;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;

import java.lang.annotation.Annotation;
import java.lang.invoke.MethodHandle;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Objects;

public class MixinDescriptor {

    protected final Class<?> clazz;
    protected final Method method;
    protected final Boolean setting;
    protected final Visit visit;
    protected final VisitMethod visitMethod;
    protected final MethodHandle methodHandle;
    protected Annotation visitInsnAnnotation;
    protected Object instance;

    public MixinDescriptor(@NotNull Class<?> clazz, @NotNull Method method, @NotNull Boolean setting,
                           @NotNull Visit visit, @NotNull VisitMethod visitMethod, @NotNull MethodHandle methodHandle,
                           @Nullable Annotation visitInsnAnnotation, @Nullable Object instance) {
        this(clazz, method, setting, visit, visitMethod, methodHandle);
        this.visitInsnAnnotation = visitInsnAnnotation;
        this.instance = instance;
    }

    public MixinDescriptor(@NotNull Class<?> clazz, @NotNull Method method, @NotNull Boolean setting,
                           @NotNull Visit visit, @NotNull VisitMethod visitMethod, @NotNull MethodHandle methodHandle) {
        this.clazz = clazz;
        this.method = method;
        this.setting = setting;
        this.visit = visit;
        this.visitMethod = visitMethod;
        this.methodHandle = methodHandle;
    }

    public void visit(@NotNull ClassNode classNode, @NotNull MethodNode methodNode) throws Throwable {
        if (Modifier.isStatic(method.getModifiers())) {
            methodHandle.invoke(classNode, methodNode);
        } else {
            methodHandle.invoke(instance, classNode, methodNode);
        }
    }

    public void visit(@NotNull ClassNode classNode, @NotNull MethodNode methodNode, @NotNull AbstractInsnNode abstractInsnNode) throws Throwable {
        if (Modifier.isStatic(method.getModifiers())) {
            methodHandle.invoke(classNode, methodNode, abstractInsnNode);
        } else {
            methodHandle.invoke(instance, classNode, methodNode, abstractInsnNode);
        }
    }

    public @NotNull Class<?> getClazz() {
        return clazz;
    }

    public @NotNull Method getMethod() {
        return method;
    }

    public @NotNull Boolean getSetting() {
        return setting;
    }

    public @NotNull Visit getVisit() {
        return visit;
    }

    public @NotNull VisitMethod getVisitMethod() {
        return visitMethod;
    }

    public @NotNull MethodHandle getMethodHandle() {
        return methodHandle;
    }

    public @Nullable Annotation getVisitInsnAnnotation() {
        return visitInsnAnnotation;
    }

    public @Nullable Object getInstance() {
        return instance;
    }

    @Override
    public int hashCode() {
        return Objects.hash(clazz, method, setting, visit, visitMethod, methodHandle, visitInsnAnnotation, instance);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }

        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }

        MixinDescriptor descriptor = (MixinDescriptor) obj;
        return Objects.equals(clazz, descriptor.clazz)
                && Objects.equals(method, descriptor.method)
                && Objects.equals(setting, descriptor.setting)
                && Objects.equals(visit, descriptor.visit)
                && Objects.equals(visitMethod, descriptor.visitMethod)
                && Objects.equals(methodHandle, descriptor.methodHandle)
                && Objects.equals(visitInsnAnnotation, descriptor.visitInsnAnnotation)
                && Objects.equals(instance, descriptor.instance);
    }

    @Override
    public String toString() {
        return String.format("%s.%s%s", clazz.getName(), method.getName(), Type.getMethodDescriptor(method));
    }
}