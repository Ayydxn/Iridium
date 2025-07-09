package me.ayydxn.moonblast.utils;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.Validate;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Files;

public class ByteBufferUtils
{
    public static void writeToFile(ByteBuffer byteBuffer, String filepath) throws IOException
    {
        File byteBufferFile = new  File(filepath);
        if (!byteBufferFile.exists())
            Validate.isTrue(byteBufferFile.createNewFile());

        byteBuffer.rewind();

        try (FileOutputStream fileOutputStream = new FileOutputStream(byteBufferFile))
        {
            FileChannel fileChannel = fileOutputStream.getChannel();
            Validate.isTrue(fileChannel.write(byteBuffer) != 0);
        }
    }

    public static ByteBuffer readFromFile(String filepath) throws IOException
    {
        File byteBufferFile = new File(filepath);
        if (!byteBufferFile.exists())
            throw new RuntimeException(String.format("The file '%s' doesn't exist!", filepath));

        byte[] byteBufferData = Files.readAllBytes(byteBufferFile.toPath());

        ByteBuffer buffer = ByteBuffer.allocateDirect(byteBufferData.length);
        buffer.put(byteBufferData);
        buffer.flip();

        return buffer;
    }
}
