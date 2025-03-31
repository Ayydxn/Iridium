package com.ayydxn.iridium.render.exceptions;

public class NotImplementedByIridiumException extends IridiumRendererException
{
    public NotImplementedByIridiumException(String functionName)
    {
        super(functionName + " is not implemented by Iridium!");
    }
}
