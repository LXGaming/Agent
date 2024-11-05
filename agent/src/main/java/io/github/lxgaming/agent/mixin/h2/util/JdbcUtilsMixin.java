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

package io.github.lxgaming.agent.mixin.h2.util;

import io.github.lxgaming.agent.asm.annotation.Setting;
import io.github.lxgaming.agent.asm.annotation.Visit;
import io.github.lxgaming.agent.asm.annotation.VisitMethod;
import io.github.lxgaming.agent.asm.annotation.VisitMethodInsn;
import io.github.lxgaming.agent.util.ASMUtils;
import org.objectweb.asm.Label;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.JumpInsnNode;
import org.objectweb.asm.tree.LdcInsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.VarInsnNode;

@Setting(value = "h2.jndi")
@Visit(name = "org/h2/util/JdbcUtils")
public class JdbcUtilsMixin {

    @VisitMethod(
        name = "getConnection",
        descriptor = "(Ljava/lang/String;Ljava/lang/String;Ljava/util/Properties;)Ljava/sql/Connection;"
    )
    @VisitMethodInsn(
        owner = "javax/naming/Context",
        name = "lookup",
        descriptor = "(Ljava/lang/String;)Ljava/lang/Object;"
    )
    private void onLookup(ClassNode classNode, MethodNode methodNode, MethodInsnNode methodInsnNode) {
        Label label = new Label();

        ASMUtils.insertBefore(methodNode.instructions, methodInsnNode,
            new VarInsnNode(Opcodes.ALOAD, 1),
            new LdcInsnNode("java:"),
            new MethodInsnNode(Opcodes.INVOKEVIRTUAL, "java/lang/String", "startsWith", "(Ljava/lang/String;)Z", false),
            new JumpInsnNode(Opcodes.IFEQ, ASMUtils.getLabelNode(label))
        );

        methodNode.visitLabel(label);
        methodNode.visitTypeInsn(Opcodes.NEW, "java/sql/SQLException");
        methodNode.visitInsn(Opcodes.DUP);
        methodNode.visitLdcInsn("Only java scheme is supported for JNDI lookups");
        methodNode.visitLdcInsn("08001");
        methodNode.visitMethodInsn(Opcodes.INVOKESPECIAL, "java/sql/SQLException", "<init>", "(Ljava/lang/String;Ljava/lang/String;)V", false);
        methodNode.visitInsn(Opcodes.ATHROW);
        methodNode.visitMaxs(-1, -1);
    }
}