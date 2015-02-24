package com.a4server.gameserver.model.craft;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * механизм крафта вещей
 * Created by arksu on 24.02.15.
 */
public class Craft
{
    private static final Logger _log = LoggerFactory.getLogger(Craft.class.getName());

    public static List<CraftTemplate> _crafts = new ArrayList<>();
}
