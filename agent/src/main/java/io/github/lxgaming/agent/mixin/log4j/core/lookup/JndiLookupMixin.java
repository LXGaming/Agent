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

package io.github.lxgaming.agent.mixin.log4j.core.lookup;

import io.github.lxgaming.agent.asm.annotation.Setting;
import io.github.lxgaming.agent.asm.annotation.Visit;
import io.github.lxgaming.agent.asm.annotation.VisitMethod;
import io.github.lxgaming.agent.util.ASMUtils;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;

@Setting(value = "log4j.jndi")
@Visit(name = "org/apache/logging/log4j/core/lookup/JndiLookup")
public class JndiLookupMixin {
    
    @VisitMethod(
            name = "lookup",
            descriptor = "(Lorg/apache/logging/log4j/core/LogEvent;Ljava/lang/String;)Ljava/lang/String;"
    )
    private void onLookup(ClassNode classNode, MethodNode methodNode) {
        ASMUtils.clear(methodNode);
        methodNode.visitInsn(Opcodes.ACONST_NULL);
        methodNode.visitInsn(Opcodes.ARETURN);
        methodNode.visitMaxs(-1, -1);
    }
}