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

package io.github.lxgaming.agent.mixin.sonatype.nexus.coreui;

import io.github.lxgaming.agent.asm.annotation.Setting;
import io.github.lxgaming.agent.asm.annotation.Visit;
import io.github.lxgaming.agent.asm.annotation.VisitMethod;
import io.github.lxgaming.agent.util.ASMUtils;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;

@Setting(value = "nexus")
@Visit(name = "org/sonatype/nexus/coreui/AssetXO")
public class AssetXOMixin {
    
    @Setting(value = "anonymize-name")
    private String name = "anonymous";
    
    @Setting(value = "anonymize-ip")
    private String ip = "127.0.0.1";
    
    @Setting(value = "anonymize")
    @VisitMethod(
            name = "getCreatedBy",
            descriptor = "()Ljava/lang/String;"
    )
    private void onGetCreatedBy(ClassNode classNode, MethodNode methodNode) {
        ASMUtils.clear(methodNode);
        methodNode.visitLdcInsn(name);
        methodNode.visitInsn(Opcodes.ARETURN);
        methodNode.visitMaxs(-1, -1);
    }
    
    @Setting(value = "anonymize")
    @VisitMethod(
            name = "setCreatedBy",
            descriptor = "(Ljava/lang/String;)V"
    )
    private void onSetCreatedBy(ClassNode classNode, MethodNode methodNode) {
        ASMUtils.clear(methodNode);
        methodNode.visitVarInsn(Opcodes.ALOAD, 0);
        methodNode.visitLdcInsn(name);
        methodNode.visitFieldInsn(Opcodes.PUTFIELD, "org/sonatype/nexus/coreui/AssetXO", "createdBy", "Ljava/lang/String;");
        methodNode.visitInsn(Opcodes.RETURN);
        methodNode.visitMaxs(-1, -1);
    }
    
    @Setting(value = "anonymize")
    @VisitMethod(
            name = "getCreatedByIp",
            descriptor = "()Ljava/lang/String;"
    )
    private void onGetCreatedByIp(ClassNode classNode, MethodNode methodNode) {
        ASMUtils.clear(methodNode);
        methodNode.visitLdcInsn(ip);
        methodNode.visitInsn(Opcodes.ARETURN);
        methodNode.visitMaxs(-1, -1);
    }
    
    @Setting(value = "anonymize")
    @VisitMethod(
            name = "setCreatedByIp",
            descriptor = "(Ljava/lang/String;)V"
    )
    private void onSetCreatedByIp(ClassNode classNode, MethodNode methodNode) {
        ASMUtils.clear(methodNode);
        methodNode.visitVarInsn(Opcodes.ALOAD, 0);
        methodNode.visitLdcInsn(ip);
        methodNode.visitFieldInsn(Opcodes.PUTFIELD, "org/sonatype/nexus/coreui/AssetXO", "createdByIp", "Ljava/lang/String;");
        methodNode.visitInsn(Opcodes.RETURN);
        methodNode.visitMaxs(-1, -1);
    }
}