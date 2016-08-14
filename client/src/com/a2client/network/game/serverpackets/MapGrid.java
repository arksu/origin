package com.a2client.network.game.serverpackets;

import com.a2client.Terrain;
import com.a2client.model.Grid;
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
		_data = readB(Terrain.GRID_SIZE_BYTES);
	}

	@Override
	public void run()
	{
		// удалим лишные гриды
		if (_px != -1 && _py != -1)
		{
			Terrain.removeOutsideGrids(_px, _py);
		}

		Grid fg = null;
		// ищем грид в списке
		for (Grid g : Terrain.grids)
		{
			if (g.getGC().equals(_gc))
			{
				g.setData(_data);
				g.fillChunks(true);
				fg = g;
				break;
			}
		}
		// если не нашли - добавим
		if (fg == null)
		{
			Grid grid = new Grid(_gc, _data);
			Terrain.addGrid(grid);
		}
		else
		{
			// иначе перестроим остальные гриды, найденный перестроится выше
			for (Grid g : Terrain.grids)
			{
				if (g != fg)
				{
					g.fillChunks(false);
				}
			}
		}
	}
}
