package com.a4server.gameserver.model.objects;

import com.google.gson.annotations.SerializedName;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * шаблон инвентаря
 * Created by arksu on 24.02.15.
 */
public class InventoryTemplate
{
    private static final Logger _log = LoggerFactory.getLogger(InventoryTemplate.class.getName());

    @SerializedName ("width")
    private int _width = 2;

    @SerializedName ("height")
    private int _height = 2;

    public InventoryTemplate(int width, int height)
    {
        _width = width;
        _height = height;
    }

    public int getWidth()
    {
        return _width;
    }

    public int getHeight()
    {
        return _height;
    }

    @Override
    public String toString()
    {
        return "(inventory " + _width + "x" + _height + ")";
    }
}
