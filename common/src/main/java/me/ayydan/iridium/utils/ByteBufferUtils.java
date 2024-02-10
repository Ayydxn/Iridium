package me.ayydan.iridium.utils;

import org.apache.commons.io.FileUtils;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.attribute.FileAttribute;

public class ByteBufferUtils
{
    public static void writeToFile(ByteBuffer byteBuffer, String filepath)
    {
        File byteBufferFile = new File(filepath);

        try
        {
            if (!byteBufferFile.exists())
                byteBufferFile.createNewFile();
        }
        catch (IOException exception)
        {
            exception.printStackTrace();
        }

        try (FileOutputStream fileOutputStream = new FileOutputStream(byteBufferFile))
        {
            FileChannel fileChannel = fileOutputStream.getChannel();
            fileChannel.write(byteBuffer);
        }
        catch (IOException exception)
        {
            exception.printStackTrace();
        }
    }

    public static ByteBuffer readFromFile(String filepath)
    {
        File byteBufferFile = new File(filepath);
        if (!byteBufferFile.exists())
            throw new RuntimeException(String.format("The file '%s' doesn't exist!", filepath));

        try (FileInputStream fileInputStream = new FileInputStream(byteBufferFile))
        {
            ByteBuffer result = ByteBuffer.allocate((int) FileUtils.sizeOf(byteBufferFile));

            FileChannel fileChannel = fileInputStream.getChannel();
            while (fileChannel.read(result) > 0)
            {
                result.flip();
                result.clear();
            }

            fileChannel.close();

            return result;
        }
        catch (IOException exception)
        {
            exception.printStackTrace();
        }

        return null;
    }
}
