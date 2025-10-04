package me.ayydxn.iridium;

import me.ayydxn.iridium.buffers.IndexBuffer;
import me.ayydxn.iridium.buffers.UniformBuffer;
import me.ayydxn.iridium.buffers.VertexBuffer;
import me.ayydxn.iridium.layer.Layer;
import me.ayydxn.iridium.renderer.GraphicsPipeline;
import me.ayydxn.iridium.renderer.SwapChain;
import me.ayydxn.iridium.shaders.IridiumShader;
import me.ayydxn.iridium.shaders.ShaderDataTypes;
import me.ayydxn.iridium.util.OrthographicCamera;
import me.ayydxn.iridium.vertex.VertexBufferElement;
import me.ayydxn.iridium.vertex.VertexBufferLayout;
import org.joml.Vector3f;
import org.lwjgl.BufferUtils;
import org.lwjgl.system.MemoryUtil;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadLocalRandom;

public class GraphicsTestLayer extends Layer
{
    private final Vector3f selectedQuadColor = new Vector3f(1.0f, 1.0f, 1.0f);
    private final SwapChain swapChain;
    private final Timer timer = new Timer();

    private GraphicsPipeline graphicsPipeline;
    private VertexBuffer vertexBuffer;
    private IndexBuffer indexBuffer;
    private UniformBuffer uniformBuffer;

    public GraphicsTestLayer(SwapChain swapChain)
    {
        super("Graphics Test");

        this.swapChain = swapChain;
    }

    @Override
    public void onAttach()
    {
        VertexBufferLayout vertexBufferLayout = new VertexBufferLayout(List.of(
                new VertexBufferElement("Positions", ShaderDataTypes.Float3),
                new VertexBufferElement("Colors", ShaderDataTypes.Float3)
        ));

        OrthographicCamera camera = new OrthographicCamera(-1.6f, 1.6f, -0.9f, 0.9f);

        float[] vertices = {
                // Positions            // Colors
                -0.5f, -0.5f, 0.0f,     1.0f, 0.0f, 0.0f,
                 0.5f, -0.5f, 0.0f,     0.0f, 1.0f, 0.0f,
                 0.5f, 0.5f, 0.0f,      0.0f, 0.0f, 1.0f,
                -0.5f, 0.5f, 0.0f,      1.0f, 0.0f, 0.0f,
        };

        int[] indices = {
                0, 1, 2, 2, 3, 0
        };

        FloatBuffer vertexData = BufferUtils.createFloatBuffer(vertices.length);
        vertexData.put(vertices).flip();

        IntBuffer indexData = BufferUtils.createIntBuffer(indices.length);
        indexData.put(indices).flip();

        this.graphicsPipeline = new GraphicsPipeline(new IridiumShader("shaders/default_shader"), vertexBufferLayout, swapChain);
        graphicsPipeline.create();

        this.vertexBuffer = new VertexBuffer(MemoryUtil.memByteBuffer(vertexData));
        vertexBuffer.create();

        this.indexBuffer = new IndexBuffer(MemoryUtil.memByteBuffer(indexData));
        indexBuffer.create();

        this.uniformBuffer = new UniformBuffer(camera.getViewProjectionMatrixBuffer());
        uniformBuffer.create();

        this.timer.scheduleAtFixedRate(new TimerTask()
        {
            @Override
            public void run()
            {
                ThreadLocalRandom threadLocalRandom = ThreadLocalRandom.current();
                Vector3f randomColor = new Vector3f(threadLocalRandom.nextFloat(1.0f), threadLocalRandom.nextFloat(1.0f),
                        threadLocalRandom.nextFloat(1.0f));

                GraphicsTestLayer.this.selectedQuadColor.set(randomColor);
            }
        }, 0L, 3000L);
    }

    @Override
    public void onUpdate()
    {
    }

    @Override
    public void onRender()
    {
        ByteBuffer frameDataPushConstant = ByteBuffer.allocateDirect(12)
                .order(ByteOrder.nativeOrder());
        frameDataPushConstant.putFloat(this.selectedQuadColor.x);
        frameDataPushConstant.putFloat(this.selectedQuadColor.y);
        frameDataPushConstant.putFloat(this.selectedQuadColor.z);
        frameDataPushConstant.flip();

        this.graphicsPipeline.bindUniformBuffer("u_Camera", uniformBuffer);
        this.graphicsPipeline.setPushConstant("u_FrameData", frameDataPushConstant);

        IridiumRenderer.getInstance().draw(graphicsPipeline, vertexBuffer, indexBuffer);
    }

    @Override
    public void onDetach()
    {
        this.vertexBuffer.destroy();
        this.indexBuffer.destroy();
        this.uniformBuffer.destroy();
        this.graphicsPipeline.destroy();

        this.timer.cancel();
    }
}
