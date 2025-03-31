package com.ayydxn.iridium.mixin.core.client.renderer;

import com.google.common.collect.Lists;
import net.minecraft.client.renderer.ShaderManager;
import net.minecraft.client.renderer.ShaderProgram;
import net.minecraft.server.packs.resources.ResourceProvider;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(ShaderManager.class)
public class ShaderManagerMixin
{
    @Inject(method = "preloadForStartup", at = @At("HEAD"), cancellable = true)
    public void a(ResourceProvider resourceProvider, ShaderProgram[] programs, CallbackInfo ci)
    {
        List<ShaderProgram> shaderPrograms = Lists.newArrayList(programs);
        shaderPrograms.forEach(shaderProgram -> System.out.println(shaderProgram.configId().getPath()));

        ci.cancel();
    }
}
