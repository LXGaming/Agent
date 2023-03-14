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
import org.objectweb.asm.Label;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.LabelNode;
import org.objectweb.asm.tree.MethodNode;

public class ASMUtils {
    
    public static void clear(@NotNull MethodNode methodNode) {
        if (methodNode.instructions != null) methodNode.instructions.clear();
        if (methodNode.tryCatchBlocks != null) methodNode.tryCatchBlocks.clear();
        methodNode.maxStack = -1;
        methodNode.maxLocals = -1;
        if (methodNode.localVariables != null) methodNode.localVariables.clear();
        if (methodNode.visibleLocalVariableAnnotations != null) methodNode.visibleLocalVariableAnnotations.clear();
        if (methodNode.invisibleLocalVariableAnnotations != null) methodNode.invisibleLocalVariableAnnotations.clear();
    }
    
    public static @NotNull String getInternalName(@NotNull String name) {
        return name.replace('.', '/');
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
}