package com.a4server.gameserver.model.objects;

import com.google.gson.annotations.SerializedName;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by arksu on 24.02.15.
 */
public class InventoryTemplate
{
    private static final Logger _log = LoggerFactory.getLogger(InventoryTemplate.class.getName());

    @SerializedName ("width")
    private int _width = 2;

    @SerializedName ("height")
    private int _height = 2;


}
