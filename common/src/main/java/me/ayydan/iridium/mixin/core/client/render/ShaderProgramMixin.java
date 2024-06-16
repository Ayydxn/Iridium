package me.ayydan.iridium.mixin.core.client.render;

import com.mojang.blaze3d.platform.Window;
import com.mojang.blaze3d.shaders.Program;
import com.mojang.blaze3d.shaders.Uniform;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.VertexFormat;
import me.ayydan.iridium.render.shader.IridiumShader;
import me.ayydan.iridium.render.vulkan.VulkanGraphicsPipeline;
import me.ayydan.iridium.render.vulkan.VulkanPipeline;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.server.packs.resources.ResourceProvider;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ShaderInstance.class)
public class ShaderProgramMixin
{
    @Shadow @Final @Nullable public Uniform MODEL_VIEW_MATRIX;
    @Shadow @Final @Nullable public Uniform PROJECTION_MATRIX;
    @Shadow @Final @Nullable public Uniform COLOR_MODULATOR;
    @Shadow @Final @Nullable public Uniform INVERSE_VIEW_ROTATION_MATRIX;
    @Shadow @Final @Nullable public Uniform GLINT_ALPHA;
    @Shadow @Final @Nullable public Uniform FOG_START;
    @Shadow @Final @Nullable public Uniform FOG_END;
    @Shadow @Final @Nullable public Uniform FOG_COLOR;
    @Shadow @Final @Nullable public Uniform FOG_SHAPE;
    @Shadow @Final @Nullable public Uniform TEXTURE_MATRIX;
    @Shadow @Final @Nullable public Uniform GAME_TIME;
    @Shadow @Final @Nullable public Uniform SCREEN_SIZE;
    @Shadow @Final @Nullable public Uniform LINE_WIDTH;

    @Unique
    private VulkanGraphicsPipeline shaderGraphicsPipeline;

    @Inject(method = "<init>", at = @At("RETURN"))
    public void createShader(ResourceProvider resourceProvider, String name, VertexFormat vertexFormat, CallbackInfo ci)
    {
        this.shaderGraphicsPipeline = (VulkanGraphicsPipeline) new VulkanPipeline.Builder()
                .type(VulkanPipeline.Type.Graphics)
                .shader(new IridiumShader("minecraft/core/" + name))
                .vertexFormat(vertexFormat)
                .build();

        this.shaderGraphicsPipeline.create();
    }

    @Redirect(method = "<init>", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/shaders/Uniform;glBindAttribLocation(IILjava/lang/CharSequence;)V"))
    public void cancelBindAttribLocation(int program, int index, CharSequence name)
    {
    }

    @Inject(method = "getOrCreate", at = @At("HEAD"), cancellable = true)
    private static void loadShaderProgram(ResourceProvider resourceProvider, Program.Type programType, String name, CallbackInfoReturnable<Program> cir)
    {
        cir.setReturnValue(null);
        cir.cancel();
    }

    @Inject(method = "apply", at = @At("HEAD"), cancellable = true)
    public void bind(CallbackInfo ci)
    {
        if (this.MODEL_VIEW_MATRIX != null)
            this.MODEL_VIEW_MATRIX.set(RenderSystem.getModelViewMatrix());

        if (this.PROJECTION_MATRIX != null)
            this.PROJECTION_MATRIX.set(RenderSystem.getProjectionMatrix());

        if (this.COLOR_MODULATOR != null)
            this.COLOR_MODULATOR.set(RenderSystem.getShaderColor());

        if (this.INVERSE_VIEW_ROTATION_MATRIX != null)
            this.INVERSE_VIEW_ROTATION_MATRIX.set(RenderSystem.getInverseViewRotationMatrix());

        if (this.GLINT_ALPHA != null)
            this.GLINT_ALPHA.set(RenderSystem.getShaderGlintAlpha());

        if (this.FOG_START != null)
            this.FOG_START.set(RenderSystem.getShaderFogStart());

        if (this.FOG_END != null)
            this.FOG_END.set(RenderSystem.getShaderFogEnd());

        if (this.FOG_COLOR != null)
            this.FOG_COLOR.set(RenderSystem.getShaderFogColor());

        if (this.FOG_SHAPE != null)
            this.FOG_SHAPE.set(RenderSystem.getShaderFogShape().getIndex());

        if (this.TEXTURE_MATRIX != null)
            this.TEXTURE_MATRIX.set(RenderSystem.getTextureMatrix());

        if (this.GAME_TIME != null)
            this.GAME_TIME.set(RenderSystem.getShaderGameTime());

        if (this.SCREEN_SIZE != null)
        {
            Window window = Minecraft.getInstance().getWindow();

            this.SCREEN_SIZE.set((float) window.getWidth(), (float) window.getHeight());
        }

        if (this.LINE_WIDTH != null)
            this.LINE_WIDTH.set(RenderSystem.getShaderLineWidth());

        ci.cancel();
    }

    @Inject(method = "clear", at = @At("HEAD"), cancellable = true)
    public void unbind(CallbackInfo ci)
    {
        ci.cancel();
    }

    @Inject(method = "close", at = @At("HEAD"), cancellable = true)
    public void destroyGraphicsPipelines(CallbackInfo ci)
    {
        this.shaderGraphicsPipeline.destroy();

        ci.cancel();
    }
}
