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

package io.github.lxgaming.agent;

import io.github.lxgaming.agent.api.AgentService;
import io.github.lxgaming.agent.asm.MixinCollection;
import io.github.lxgaming.agent.mixin.h2.util.JdbcUtilsMixin;
import io.github.lxgaming.agent.mixin.log4j.core.lookup.JndiLookupMixin;
import io.github.lxgaming.agent.mixin.log4j.core.pattern.MessagePatternConverterMixin;
import org.jetbrains.annotations.NotNull;

public class AgentServiceImpl implements AgentService {
    
    @Override
    public void initialize(@NotNull MixinCollection mixins) {
        mixins.register(JdbcUtilsMixin.class);
        mixins.register(JndiLookupMixin.class);
        mixins.register(MessagePatternConverterMixin.class);
    }
}