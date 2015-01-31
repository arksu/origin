package com.a4server.gameserver.model;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Created by arksu on 05.01.2015.
 */
public class PcAppearance
{
    /**
     * представление игрока в мире. то как он выглядит
     */

    private boolean _sex; // true = female; male = false;

    private byte _hairColor;
    private byte _hairStyle;
    private byte _face;

    public PcAppearance(ResultSet set) {
        try
        {
            _face = set.getByte("face");
            _hairColor = set.getByte("hairColor");
            _hairStyle = set.getByte("hairStyle");
            _sex = set.getInt("sex") != 0;
        }
        catch (SQLException e)
        {
            e.printStackTrace();
        }
    }
}
