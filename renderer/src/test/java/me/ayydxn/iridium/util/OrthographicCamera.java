package me.ayydxn.iridium.util;

import org.joml.Matrix4f;
import org.lwjgl.BufferUtils;

import java.nio.ByteBuffer;

public class OrthographicCamera
{
    private final Matrix4f viewMatrix;
    private final Matrix4f projectionMatrix;
    private final Matrix4f viewProjectionMatrix;

    public OrthographicCamera(float left, float right, float bottom, float top)
    {
        this.viewMatrix = new Matrix4f();

        this.projectionMatrix = new Matrix4f();
        this.projectionMatrix.identity();
        this.projectionMatrix.ortho(left, right, bottom, top, -1.0f, 1.0f);

        this.viewProjectionMatrix = this.projectionMatrix.mul(this.viewMatrix);
    }

    public ByteBuffer getViewProjectionMatrixBuffer()
    {
        ByteBuffer viewProjectionMatrixBuffer = BufferUtils.createByteBuffer(64);

        this.viewProjectionMatrix.get(viewProjectionMatrixBuffer);

        return viewProjectionMatrixBuffer;
    }
}
