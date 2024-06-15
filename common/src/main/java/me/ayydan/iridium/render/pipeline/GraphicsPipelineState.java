package me.ayydan.iridium.render.pipeline;

import com.mojang.blaze3d.platform.GlStateManager;
import me.ayydan.iridium.render.IridiumRenderSystem;

import java.util.Objects;

import static org.lwjgl.opengl.GL14.*;
import static org.lwjgl.vulkan.VK10.*;

public class GraphicsPipelineState
{
    public static final BlendState DEFAULT_BLEND_STATE = defaultBlendInfo().createBlendState();
    public static final DepthState DEFAULT_DEPTH_STATE = defaultDepthState();
    public static final LogicOpState DEFAULT_LOGICOP_STATE = new LogicOpState(false, 0);
    public static final ColorMask DEFAULT_COLORMASK = new ColorMask(true, true, true, true);

    public static GraphicsPipelineState.BlendInfo blendInfo = GraphicsPipelineState.defaultBlendInfo();
    public static GraphicsPipelineState.BlendState currentblendState = blendInfo.createBlendState();
    public static GraphicsPipelineState.DepthState currentDepthState = GraphicsPipelineState.DEFAULT_DEPTH_STATE;
    public static GraphicsPipelineState.LogicOpState currentLogicOpState = GraphicsPipelineState.DEFAULT_LOGICOP_STATE;
    public static GraphicsPipelineState.ColorMask currentColorMask = GraphicsPipelineState.DEFAULT_COLORMASK;

    public final BlendState blendState;
    public final DepthState depthState;
    public final ColorMask colorMask;
    public final LogicOpState logicOpState;
    public final boolean cullState;

    public GraphicsPipelineState(BlendState blendState, DepthState depthState, LogicOpState logicOpState, ColorMask colorMask)
    {
        this.blendState = blendState;
        this.depthState = depthState;
        this.logicOpState = logicOpState;
        this.colorMask = colorMask;
        this.cullState = IridiumRenderSystem.isCullingEnabled;
    }

    @Override
    public boolean equals(Object otherObject)
    {
        if (this == otherObject)
            return true;

        if (otherObject == null || getClass() != otherObject.getClass())
            return false;

        GraphicsPipelineState otherGraphicsPipelineState = (GraphicsPipelineState) otherObject;

        return blendState.equals(otherGraphicsPipelineState.blendState) &&
                depthState.equals(otherGraphicsPipelineState.depthState) &&
                logicOpState.equals(otherGraphicsPipelineState.logicOpState) &&
                (cullState == otherGraphicsPipelineState.cullState) && colorMask.equals(otherGraphicsPipelineState.colorMask);
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(blendState, depthState, logicOpState, cullState);
    }

    public static BlendInfo defaultBlendInfo()
    {
        return new BlendInfo(true, VK_BLEND_FACTOR_SRC_ALPHA, VK_BLEND_FACTOR_ONE_MINUS_SRC_ALPHA,
                VK_BLEND_FACTOR_ONE, VK_BLEND_FACTOR_ZERO, VK_BLEND_OP_ADD);
    }

    public static DepthState defaultDepthState()
    {
        return new DepthState(true, true, 515);
    }

    public static ColorMask defaultColorMask()
    {
        return new ColorMask(true, true, true, true);
    }

    public static class BlendInfo
    {
        public boolean enabled;
        public int srcRgbFactor;
        public int dstRgbFactor;
        public int srcAlphaFactor;
        public int dstAlphaFactor;
        public int blendOp;

        public BlendInfo(boolean enabled, int srcRgbFactor, int dstRgbFactor, int srcAlphaFactor, int dstAlphaFactor, int blendOp)
        {
            this.enabled = enabled;
            this.srcRgbFactor = srcRgbFactor;
            this.dstRgbFactor = dstRgbFactor;
            this.srcAlphaFactor = srcAlphaFactor;
            this.dstAlphaFactor = dstAlphaFactor;
            this.blendOp = blendOp;
        }

        public void setBlendFunction(GlStateManager.SourceFactor sourceFactor, GlStateManager.DestFactor destFactor)
        {
            this.srcRgbFactor = convertOpenGLBlendFactorToVulkan(sourceFactor.value);
            this.srcAlphaFactor = convertOpenGLBlendFactorToVulkan(sourceFactor.value);
            this.dstRgbFactor = convertOpenGLBlendFactorToVulkan(destFactor.value);
            this.dstAlphaFactor = convertOpenGLBlendFactorToVulkan(destFactor.value);
        }

        public void setBlendFuncSeparate(GlStateManager.SourceFactor srcRgb, GlStateManager.DestFactor dstRgb, GlStateManager.SourceFactor srcAlpha,
                                         GlStateManager.DestFactor dstAlpha)
        {
            this.srcRgbFactor = convertOpenGLBlendFactorToVulkan(srcRgb.value);
            this.srcAlphaFactor = convertOpenGLBlendFactorToVulkan(srcAlpha.value);
            this.dstRgbFactor = convertOpenGLBlendFactorToVulkan(dstRgb.value);
            this.dstAlphaFactor = convertOpenGLBlendFactorToVulkan(dstAlpha.value);
        }

        public void setBlendFunction(int sourceFactor, int destFactor)
        {
            this.srcRgbFactor = convertOpenGLBlendFactorToVulkan(sourceFactor);
            this.srcAlphaFactor = convertOpenGLBlendFactorToVulkan(sourceFactor);
            this.dstRgbFactor = convertOpenGLBlendFactorToVulkan(destFactor);
            this.dstAlphaFactor = convertOpenGLBlendFactorToVulkan(destFactor);
        }

        public void setBlendFuncSeparate(int srcRgb, int dstRgb, int srcAlpha, int dstAlpha)
        {
            this.srcRgbFactor = convertOpenGLBlendFactorToVulkan(srcRgb);
            this.srcAlphaFactor = convertOpenGLBlendFactorToVulkan(srcAlpha);
            this.dstRgbFactor = convertOpenGLBlendFactorToVulkan(dstRgb);
            this.dstAlphaFactor = convertOpenGLBlendFactorToVulkan(dstAlpha);
        }

        public void setBlendOp(int blendOp)
        {
            this.blendOp = convertOpenGLBlendOpToVulkan(blendOp);
        }

        public BlendState createBlendState()
        {
            return new BlendState(this.enabled, this.srcRgbFactor, this.dstRgbFactor, this.srcAlphaFactor, this.dstAlphaFactor, this.blendOp);
        }

        private static int convertOpenGLBlendOpToVulkan(int glBlendOp)
        {
            return switch (glBlendOp)
            {
                case GL_FUNC_ADD -> VK_BLEND_OP_ADD;
                case GL_MIN -> VK_BLEND_OP_MIN;
                case GL_MAX -> VK_BLEND_OP_MAX;
                case GL_FUNC_SUBTRACT -> VK_BLEND_OP_SUBTRACT;
                case GL_FUNC_REVERSE_SUBTRACT -> VK_BLEND_OP_REVERSE_SUBTRACT;
                default -> throw new IllegalArgumentException(String.format("Failed to convert OpenGL blend factor '%d' to Vulkan!", glBlendOp));
            };
        }

        private static int convertOpenGLBlendFactorToVulkan(int glBlendFactor)
        {
            return switch (glBlendFactor)
            {
                case GL_CONSTANT_ALPHA -> VK_BLEND_FACTOR_CONSTANT_ALPHA;
                case GL_CONSTANT_COLOR -> VK_BLEND_FACTOR_CONSTANT_COLOR;
                case GL_DST_ALPHA -> VK_BLEND_FACTOR_DST_ALPHA;
                case GL_DST_COLOR -> VK_BLEND_FACTOR_DST_COLOR;
                case GL_ONE -> VK_BLEND_FACTOR_ONE;
                case GL_ONE_MINUS_CONSTANT_ALPHA -> VK_BLEND_FACTOR_ONE_MINUS_CONSTANT_ALPHA;
                case GL_ONE_MINUS_CONSTANT_COLOR -> VK_BLEND_FACTOR_ONE_MINUS_CONSTANT_COLOR;
                case GL_ONE_MINUS_DST_ALPHA -> VK_BLEND_FACTOR_ONE_MINUS_DST_ALPHA;
                case GL_ONE_MINUS_DST_COLOR -> VK_BLEND_FACTOR_ONE_MINUS_DST_COLOR;
                case GL_ONE_MINUS_SRC_ALPHA -> VK_BLEND_FACTOR_ONE_MINUS_SRC_ALPHA;
                case GL_ONE_MINUS_SRC_COLOR -> VK_BLEND_FACTOR_ONE_MINUS_SRC_COLOR;
                case GL_SRC_ALPHA -> VK_BLEND_FACTOR_SRC_ALPHA;
                case GL_SRC_ALPHA_SATURATE -> VK_BLEND_FACTOR_SRC_ALPHA_SATURATE;
                case GL_SRC_COLOR -> VK_BLEND_FACTOR_SRC_COLOR;
                case GL_ZERO -> VK_BLEND_FACTOR_ZERO;
                default -> throw new IllegalArgumentException(String.format("Failed to convert OpenGL blend factor '%d' to Vulkan!", glBlendFactor));
            };
        }
    }

    public static class BlendState
    {
        public final boolean enabled;
        public final int srcRgbFactor;
        public final int dstRgbFactor;
        public final int srcAlphaFactor;
        public final int dstAlphaFactor;
        public final int blendOp;

        protected BlendState(boolean enabled, int srcRgb, int dstRgb, int srcAlpha, int dstAlpha, int blendOp)
        {
            this.enabled = enabled;
            this.srcRgbFactor = srcRgb;
            this.dstRgbFactor = dstRgb;
            this.srcAlphaFactor = srcAlpha;
            this.dstAlphaFactor = dstAlpha;
            this.blendOp = blendOp;
        }

        @Override
        public boolean equals(Object otherObject)
        {
            if (this == otherObject)
                return true;

            if (otherObject == null || getClass() != otherObject.getClass())
                return false;

            return this.equals((BlendState) otherObject);
        }

        public boolean equals(BlendState blendState)
        {
            if (!this.enabled && !blendState.enabled)
                return true;

            if (this.enabled != blendState.enabled)
                return false;

            return srcRgbFactor == blendState.srcRgbFactor &&
                    dstRgbFactor == blendState.dstRgbFactor &&
                    srcAlphaFactor == blendState.srcAlphaFactor &&
                    dstAlphaFactor == blendState.dstAlphaFactor && blendOp == blendState.blendOp;
        }

        @Override
        public int hashCode()
        {
            return Objects.hash(srcRgbFactor, dstRgbFactor, srcAlphaFactor, dstAlphaFactor, blendOp);
        }
    }

    public static class LogicOpState
    {
        public final boolean enabled;
        private int logicOp;

        public LogicOpState(boolean enable, int op)
        {
            this.enabled = enable;
            this.logicOp = op;
        }

        public void setLogicOp(GlStateManager.LogicOp logicOp)
        {
            switch (logicOp)
            {
                case AND -> this.setLogicOp(VK_LOGIC_OP_AND);
                case AND_INVERTED -> this.setLogicOp(VK_LOGIC_OP_AND_INVERTED);
                case AND_REVERSE -> this.setLogicOp(VK_LOGIC_OP_AND_REVERSE);
                case CLEAR -> this.setLogicOp(VK_LOGIC_OP_CLEAR);
                case COPY -> this.setLogicOp(VK_LOGIC_OP_COPY);
                case COPY_INVERTED -> this.setLogicOp(VK_LOGIC_OP_COPY_INVERTED);
                case EQUIV -> this.setLogicOp(VK_LOGIC_OP_EQUIVALENT);
                case INVERT -> this.setLogicOp(VK_LOGIC_OP_INVERT);
                case NAND -> this.setLogicOp(VK_LOGIC_OP_NAND);
                case NOOP -> this.setLogicOp(VK_LOGIC_OP_NO_OP);
                case NOR -> this.setLogicOp(VK_LOGIC_OP_NOR);
                case OR -> this.setLogicOp(VK_LOGIC_OP_OR);
                case OR_INVERTED -> this.setLogicOp(VK_LOGIC_OP_OR_INVERTED);
                case OR_REVERSE -> this.setLogicOp(VK_LOGIC_OP_OR_REVERSE);
                case SET -> this.setLogicOp(VK_LOGIC_OP_SET);
                case XOR -> this.setLogicOp(VK_LOGIC_OP_XOR);
            }

        }

        public void setLogicOp(int logicOp)
        {
            this.logicOp = logicOp;
        }

        public int getLogicOp()
        {
            return this.logicOp;
        }

        @Override
        public boolean equals(Object otherObject)
        {
            if (this == otherObject)
                return true;

            if (otherObject == null || getClass() != otherObject.getClass())
                return false;

            LogicOpState otherLogicOpState = (LogicOpState) otherObject;

            if (this.enabled != otherLogicOpState.enabled)
                return false;

            return logicOp == otherLogicOpState.logicOp;
        }

        public int hashCode()
        {
            return Objects.hash(enabled, logicOp);
        }
    }

    public static class ColorMask
    {
        public final int colorMask;

        public ColorMask(boolean r, boolean g, boolean b, boolean a)
        {
            this.colorMask = (r ? VK_COLOR_COMPONENT_R_BIT : 0) | (g ? VK_COLOR_COMPONENT_G_BIT : 0) | (b ? VK_COLOR_COMPONENT_B_BIT : 0) | (a ? VK_COLOR_COMPONENT_A_BIT : 0);
        }

        public ColorMask(int mask)
        {
            this.colorMask = mask;
        }

        public static int getColorMask(boolean r, boolean g, boolean b, boolean a)
        {
            return (r ? VK_COLOR_COMPONENT_R_BIT : 0) | (g ? VK_COLOR_COMPONENT_G_BIT : 0) | (b ? VK_COLOR_COMPONENT_B_BIT : 0) | (a ? VK_COLOR_COMPONENT_A_BIT : 0);
        }

        @Override
        public boolean equals(Object otherObject)
        {
            if (this == otherObject)
                return true;

            if (otherObject == null || getClass() != otherObject.getClass())
                return false;

            ColorMask otherColorMask = (ColorMask) otherObject;
            return this.colorMask == otherColorMask.colorMask;
        }
    }

    public static class DepthState
    {
        public final boolean depthTest;
        public final boolean depthMask;
        public final int function;

        public DepthState(boolean depthTest, boolean depthMask, int function)
        {
            this.depthTest = depthTest;
            this.depthMask = depthMask;
            this.function = convertOpenGLCompareOpToVulkan(function);
        }

        private static int convertOpenGLCompareOpToVulkan(int openGLCompareOp)
        {
            return switch (openGLCompareOp)
            {
                case GL_NEVER -> VK_COMPARE_OP_NEVER;
                case GL_LESS -> VK_COMPARE_OP_LESS;
                case GL_EQUAL -> VK_COMPARE_OP_EQUAL;
                case GL_LEQUAL -> VK_COMPARE_OP_LESS_OR_EQUAL;
                case GL_GREATER -> VK_COMPARE_OP_GREATER;
                case GL_NOTEQUAL -> VK_COMPARE_OP_NOT_EQUAL;
                case GL_GEQUAL -> VK_COMPARE_OP_GREATER_OR_EQUAL;
                case GL_ALWAYS -> VK_COMPARE_OP_ALWAYS;
                default -> throw new IllegalArgumentException(String.format("Failed to convert OpenGL compare op '%d' to Vulkan!", openGLCompareOp));
            };
        }

        @Override
        public boolean equals(Object otherObject)
        {
            if (this == otherObject)
                return true;

            if (otherObject == null || getClass() != otherObject.getClass())
                return false;

            DepthState otherDepthState = (DepthState) otherObject;
            return depthTest == otherDepthState.depthTest && depthMask == otherDepthState.depthMask && function == otherDepthState.function;
        }

        @Override
        public int hashCode()
        {
            return Objects.hash(depthTest, depthMask, function);
        }
    }
}