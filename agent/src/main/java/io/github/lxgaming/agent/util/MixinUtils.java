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

import io.github.lxgaming.agent.asm.annotation.Visit;
import io.github.lxgaming.agent.asm.annotation.VisitFieldInsn;
import io.github.lxgaming.agent.asm.annotation.VisitMethod;
import io.github.lxgaming.agent.asm.annotation.VisitMethodInsn;
import org.jetbrains.annotations.NotNull;
import org.objectweb.asm.Label;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.LabelNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;

import java.lang.annotation.Annotation;

public class MixinUtils {
    
    public static void clear(@NotNull MethodNode methodNode) {
        methodNode.instructions.clear();
        methodNode.tryCatchBlocks.clear();
        methodNode.maxStack = -1;
        methodNode.maxLocals = -1;
        methodNode.localVariables.clear();
        methodNode.visibleLocalVariableAnnotations.clear();
        methodNode.invisibleLocalVariableAnnotations.clear();
    }
    
    public static @NotNull LabelNode getLabelNode(@NotNull Label label) {
        if (!(label.info instanceof LabelNode)) {
            label.info = new LabelNode();
        }
        
        return (LabelNode) label.info;
    }
    
    public static void insert(@NotNull InsnList insnList, @NotNull AbstractInsnNode previousInsn, @NotNull AbstractInsnNode... insnNodes) {
        for (AbstractInsnNode insnNode : insnNodes) {
            insnList.insert(previousInsn, insnNode);
            previousInsn = insnNode;
        }
    }
    
    public static void insertBefore(@NotNull InsnList insnList, @NotNull AbstractInsnNode nextInsn, @NotNull AbstractInsnNode... insnNodes) {
        for (AbstractInsnNode insnNode : insnNodes) {
            insnList.insertBefore(nextInsn, insnNode);
        }
    }
    
    public static Type[] getTypes(@NotNull Class<?>... classes) {
        Type[] types = new Type[classes.length];
        for (int index = 0; index < classes.length; index++) {
            types[index] = Type.getType(classes[index]);
        }
        
        return types;
    }
    
    public static boolean canVisit(@NotNull String className, @NotNull String visitName) {
        if (StringUtils.isBlank(visitName)) {
            return true;
        }
        
        return visitName.charAt(visitName.length() - 1) == '/'
                ? className.startsWith(visitName) // Package
                : className.equals(visitName); // Class
    }
    
    public static boolean canVisit(@NotNull String className, @NotNull Visit visit) {
        return canVisit(className, visit.name());
    }
    
    public static boolean canVisit(@NotNull ClassNode classNode, @NotNull Visit visit) {
        return (visit.version() == -1 || visit.version() == classNode.version)
                && (visit.access() == -1 || visit.access() == classNode.access)
                && canVisit(classNode.name, visit.name())
                && (visit.signature().equals("") || visit.signature().equals(classNode.signature))
                && (visit.superName().equals("") || visit.superName().equals(classNode.superName))
                && (visit.interfaces().length == 0 || StringUtils.containsAll(classNode.interfaces, visit.interfaces()));
    }
    
    public static boolean canVisit(@NotNull MethodNode methodNode, @NotNull VisitMethod visitMethod) {
        return (visitMethod.access() == -1 || visitMethod.access() == methodNode.access)
                && (visitMethod.name().equals("") || visitMethod.name().equals(methodNode.name))
                && (visitMethod.descriptor().equals("") || visitMethod.descriptor().equals(methodNode.desc))
                && (visitMethod.signature().equals("") || visitMethod.signature().equals(methodNode.signature))
                && (visitMethod.exceptions().length == 0 || StringUtils.containsAll(methodNode.exceptions, visitMethod.exceptions()));
    }
    
    @SuppressWarnings("DuplicatedCode")
    public static boolean canVisit(@NotNull AbstractInsnNode abstractInsnNode, @NotNull Annotation visitInsnAnnotation) {
        if (visitInsnAnnotation instanceof VisitFieldInsn && abstractInsnNode instanceof FieldInsnNode) {
            VisitFieldInsn visitInsn = (VisitFieldInsn) visitInsnAnnotation;
            FieldInsnNode insnNode = (FieldInsnNode) abstractInsnNode;
            return (visitInsn.opcode() == -1 || visitInsn.opcode() == insnNode.getOpcode())
                    && canVisit(insnNode.owner, visitInsn.owner())
                    && (visitInsn.name().equals("") || visitInsn.name().equals(insnNode.name))
                    && (visitInsn.descriptor().equals("") || visitInsn.descriptor().equals(insnNode.desc));
        }
        
        if (visitInsnAnnotation instanceof VisitMethodInsn && abstractInsnNode instanceof MethodInsnNode) {
            VisitMethodInsn visitInsn = (VisitMethodInsn) visitInsnAnnotation;
            MethodInsnNode insnNode = (MethodInsnNode) abstractInsnNode;
            return (visitInsn.opcode() == -1 || visitInsn.opcode() == insnNode.getOpcode())
                    && canVisit(insnNode.owner, visitInsn.owner())
                    && (visitInsn.name().equals("") || visitInsn.name().equals(insnNode.name))
                    && (visitInsn.descriptor().equals("") || visitInsn.descriptor().equals(insnNode.desc));
        }
        
        return false;
    }
}