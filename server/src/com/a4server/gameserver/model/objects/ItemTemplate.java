package com.a4server.gameserver.model.objects;

import com.google.gson.annotations.SerializedName;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * шаблон для описания предмета инвентаря
 * Created by arksu on 24.02.15.
 */
public class ItemTemplate
{
    private static final Logger _log = LoggerFactory.getLogger(ItemTemplate.class.getName());

    @SerializedName ("width")
    private int _width = 1;

    @SerializedName ("height")
    private int _height = 1;
}
