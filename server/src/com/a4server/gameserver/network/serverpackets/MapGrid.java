package com.a4server.gameserver.network.serverpackets;

import com.a4server.gameserver.model.Grid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by arksu on 08.01.2015.
 */
public class MapGrid extends GameServerPacket
{
    protected static final Logger _log = LoggerFactory.getLogger(MapGrid.class.getName());

    Grid _grid;
    int  _px, _py;

    public MapGrid(Grid grid, int px, int py)
    {
        _grid = grid;
        _px = px;
        _py = py;
    }

    @Override
    protected void write()
    {
        writeC(0x0B);
        writeD(_px);
        writeD(_py);
        writeD(_grid.getCoordX());
        writeD(_grid.getCoordY());
        writeB(_grid.getTilesBlob());
    }
}
