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

package io.github.lxgaming.agent.mixin.log4j.core.pattern;

import io.github.lxgaming.agent.asm.annotation.Setting;
import io.github.lxgaming.agent.asm.annotation.Visit;
import io.github.lxgaming.agent.asm.annotation.VisitFieldInsn;
import io.github.lxgaming.agent.asm.annotation.VisitMethod;
import io.github.lxgaming.agent.util.ASMUtils;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.LdcInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.VarInsnNode;

/**
 * log4j-core v2.7 - v2.14.1
 */
@Setting(value = "log4j.jndi")
@Visit(name = "org/apache/logging/log4j/core/pattern/MessagePatternConverter")
public class MessagePatternConverterMixin {

    @VisitMethod(
        name = "<init>"
    )
    @VisitFieldInsn(
        opcode = Opcodes.PUTFIELD,
        owner = "org/apache/logging/log4j/core/pattern/MessagePatternConverter",
        name = "noLookups",
        descriptor = "Z"
    )
    private void onInit(ClassNode classNode, MethodNode methodNode, FieldInsnNode fieldInsnNode) {
        ASMUtils.insert(methodNode.instructions, fieldInsnNode,
            new VarInsnNode(Opcodes.ALOAD, 0),
            new LdcInsnNode(true),
            new FieldInsnNode(
                fieldInsnNode.getOpcode(),
                fieldInsnNode.owner,
                fieldInsnNode.name,
                fieldInsnNode.desc
            )
        );
        methodNode.visitMaxs(-1, -1);
    }
}