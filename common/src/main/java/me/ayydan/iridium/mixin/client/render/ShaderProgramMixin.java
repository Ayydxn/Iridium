package me.ayydan.iridium.mixin.client.render;

import com.mojang.blaze3d.glfw.Window;
import com.mojang.blaze3d.shader.GlUniform;
import com.mojang.blaze3d.shader.ShaderStage;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.ShaderProgram;
import net.minecraft.resource.ResourceFactory;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ShaderProgram.class)
public class ShaderProgramMixin
{
    @Shadow @Final @Nullable public GlUniform modelViewMat;
    @Shadow @Final @Nullable public GlUniform projectionMat;
    @Shadow @Final @Nullable public GlUniform colorModulator;
    @Shadow @Final @Nullable public GlUniform inverseViewRotationMat;
    @Shadow @Final @Nullable public GlUniform glintAlpha;
    @Shadow @Final @Nullable public GlUniform fogStart;
    @Shadow @Final @Nullable public GlUniform fogEnd;
    @Shadow @Final @Nullable public GlUniform fogColor;
    @Shadow @Final @Nullable public GlUniform fogShape;
    @Shadow @Final @Nullable public GlUniform textureMat;
    @Shadow @Final @Nullable public GlUniform gameTime;
    @Shadow @Final @Nullable public GlUniform screenSize;
    @Shadow @Final @Nullable public GlUniform lineWidth;

    @Inject(method = "<init>", at = @At("RETURN"))
    public void createShader(ResourceFactory factory, String name, VertexFormat format, CallbackInfo ci)
    {
        // TODO: (Ayydan) Initialize and create the graphics pipeline here.
    }

    @Redirect(method = "<init>", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/shader/GlUniform;bindAttribLocation(IILjava/lang/CharSequence;)V"))
    public void cancelBindAttribLocation(int program, int index, CharSequence name)
    {
    }

    @Inject(method = "loadProgram", at = @At("HEAD"), cancellable = true)
    private static void loadShaderProgram(ResourceFactory factory, ShaderStage.Type type, String name, CallbackInfoReturnable<ShaderStage> cir)
    {
        cir.setReturnValue(null);
        cir.cancel();
    }

    @Inject(method = "bind", at = @At("HEAD"), cancellable = true)
    public void bind(CallbackInfo ci)
    {
        if (this.modelViewMat != null)
            this.modelViewMat.setMat4x4(RenderSystem.getModelViewMatrix());

        if (this.projectionMat != null)
            this.projectionMat.setMat4x4(RenderSystem.getProjectionMatrix());

        if (this.colorModulator != null)
            this.colorModulator.setFloats(RenderSystem.getShaderColor());

        if (this.inverseViewRotationMat != null)
            this.inverseViewRotationMat.setMat3x3(RenderSystem.getInverseViewRotationMatrix());

        if (this.glintAlpha != null)
            this.glintAlpha.setFloat(RenderSystem.getShaderGlintAlpha());

        if (this.fogStart != null)
            this.fogStart.setFloat(RenderSystem.getShaderFogStart());

        if (this.fogEnd != null)
            this.fogEnd.setFloat(RenderSystem.getShaderFogEnd());

        if (this.fogColor != null)
            this.fogColor.setFloats(RenderSystem.getShaderFogColor());

        if (this.fogShape != null)
            this.fogShape.setInt(RenderSystem.getShaderFogShape().getShapeId());

        if (this.textureMat != null)
            this.textureMat.setMat4x4(RenderSystem.getTextureMatrix());

        if (this.gameTime != null)
            this.gameTime.setFloat(RenderSystem.getShaderGameTime());

        if (this.screenSize != null)
        {
            Window window = MinecraftClient.getInstance().getWindow();

            this.screenSize.setVec2((float) window.getWidth(), (float) window.getHeight());
        }

        if (this.lineWidth != null)
            this.lineWidth.setFloat(RenderSystem.getShaderLineWidth());

        ci.cancel();
    }

    @Inject(method = "unbind", at = @At("HEAD"), cancellable = true)
    public void unbind(CallbackInfo ci)
    {
        ci.cancel();
    }

    @Inject(method = "close", at = @At("HEAD"), cancellable = true)
    public void destroyGraphicsPipelines(CallbackInfo ci)
    {
        // TODO: (Ayydan) Destroy graphics pipeline here.

        ci.cancel();
    }
}
