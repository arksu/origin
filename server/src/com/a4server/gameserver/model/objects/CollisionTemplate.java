package com.a4server.gameserver.model.objects;

import com.google.gson.annotations.SerializedName;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by arksu on 23.02.15.
 */
public class CollisionTemplate
{
    private static final Logger _log = LoggerFactory.getLogger(CollisionTemplate.class.getName());

    @SerializedName ("all")
    private boolean _allYes = true;

    @SerializedName ("exclude")
    private List<String> _exclude = new ArrayList<>();
}
