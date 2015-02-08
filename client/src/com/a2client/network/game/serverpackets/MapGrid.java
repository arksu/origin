package com.a2client.network.game.serverpackets;

import com.a2client.model.Grid;
import com.a2client.MapCache;
import com.a2client.network.game.GamePacketHandler;
import com.a2client.util.Vec2i;

public class MapGrid extends GameServerPacket
{
    static
    {
        GamePacketHandler.AddPacketType(0x0B, MapGrid.class);
    }

    int _px, _py;
    Vec2i _gc;
    byte[] _data;

    @Override
    public void readImpl()
    {
        _px = readD();
        _py = readD();
        _gc = new Vec2i(readD(), readD());
        _data = readB(MapCache.GRID_SIZE_BYTES);
    }

    @Override
    public void run()
    {
        MapCache.removeOutsideGrids(_px, _py);

        boolean f = false;
        for (Grid g : MapCache.grids)
        {
            if (g.getGC().equals(_gc))
            {
                g.setData(_data);
                f = true;
            }
        }
        if (!f)
        {
            MapCache.grids.add(new Grid(_gc, _data));
        }

    }
}
