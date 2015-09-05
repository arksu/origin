package com.a4server.map;

import com.a4server.Database;
import com.a4server.util.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.sql.Blob;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import static com.a4server.gameserver.model.Grid.*;
import static com.a4server.gameserver.model.Tile.TileType.*;

/**
 * Created by arksu on 06.09.15.
 */
public class Importer
{
	private static final Logger _log = LoggerFactory.getLogger(Importer.class.getName());

	public static final String CLEAR_SUPERGRID = "TRUNCATE sg_0";
	public static final String SET_GRID_DATA = "INSERT INTO sg_0 (id,data,last_tick) VALUES (?,?,?)";

	public static Map<Integer, Integer> tiles = new HashMap<>();

	public static void main(String[] args)
	{
		String mapFile = "";
		int sg = -1;
		int i = 0;
		while (i < args.length)
		{
			String arg = args[i];
			if (arg.equals("-m"))
			{
				i++;
				mapFile = args[i];
			}
			if (arg.equals("-sg"))
			{
				i++;
				sg = Integer.parseInt(args[i]);
			}
			i++;
		}

		if (!Utils.isEmpty(mapFile) && sg >= 0)
		{
			_log.debug("file: " + mapFile + " sg: " + sg);
			_log.debug("start db...");
			Database.getInstance();

			try
			{
				_log.debug("load image");
				BufferedImage image = ImageIO.read(new File(mapFile));
				_log.debug("image " + image.getWidth() + " x " + image.getHeight());

				final int sgSize = GRID_SIZE * SUPERGRID_SIZE;
				if (image.getHeight() == sgSize && image.getWidth() == sgSize)
				{
					_log.debug("its full supergrid, start work");
					_log.debug("clear table: sg_" + sg);
					clearSupergrid(sg);

					fillConfig();
					final int gridsCount = SUPERGRID_SIZE * SUPERGRID_SIZE;
					_log.debug("fill " + gridsCount + " grids...");
					// обработаем все гриды
					for (int gridn = 0; gridn < gridsCount; gridn++)
					{
						// подготовим бинарные данные
						byte[] data = new byte[GRID_BLOB_SIZE];

						// идем по всем тайлам
						int ox = (gridn % SUPERGRID_SIZE) * GRID_SIZE;
						int oy = (gridn / SUPERGRID_SIZE) * GRID_SIZE;
						_log.debug("process grid: " + gridn + " offset: " + ox + ", " + oy);
						for (int x = 0; x < GRID_SIZE; x++)
						{
							for (int y = 0; y < GRID_SIZE; y++)
							{
								int rgb = image.getRGB(ox + x, oy + y);
								int color = rgb & 0x00ffffff;
								byte tile = (byte) (getTile(color) & 0xff);
								if (tile == 0)
								{
									int red = (rgb & 0x00ff0000) >> 16;
									int green = (rgb & 0x0000ff00) >> 8;
									int blue = rgb & 0x000000ff;
									_log.error("unknown tile [" + red + ", " + green + ", " + blue + "]");
									System.exit(-1);
								}
								data[x + y * GRID_SIZE] = tile;
							}
						}

						setGrid(sg, gridn, data);
					}
					_log.debug("done");
					System.exit(0);
				}
			}
			catch (IOException e)
			{
				_log.warn("failed load image " + e.getMessage());
			}
			catch (SQLException e)
			{
				_log.error("sql error: " + e.getMessage());
			}
		}
	}

	public static void clearSupergrid(int sg) throws SQLException
	{
		String query = CLEAR_SUPERGRID;
		query = query.replaceFirst("sg_0", "sg_" + Integer.toString(sg));

		try (Connection con = Database.getInstance().getConnection();
			 PreparedStatement ps = con.prepareStatement(query))
		{
			ps.executeUpdate();
		}
	}

	public static void setGrid(int sg, int grid, byte[] data) throws SQLException
	{
		String query = SET_GRID_DATA;
		query = query.replaceFirst("sg_0", "sg_" + Integer.toString(sg));

		try (Connection con = Database.getInstance().getConnection();
			 PreparedStatement statement = con.prepareStatement(query))
		{
			Blob b = con.createBlob();
			b.setBytes(1, data);
			statement.setInt(1, grid);
			statement.setBlob(2, b);
			statement.setInt(3, 0);
			statement.executeUpdate();
			con.close();
		}
	}

	public static int getTile(int color)
	{
		Integer t = tiles.get(color);
		if (t != null)
		{
			return t;
		}

		return 0;
	}

	public static int makeColor(int red, int green, int blue)
	{
		return red << 16 | green << 8 | blue;
	}

	public static void fillConfig()
	{
		tiles.put(makeColor(0, 0, 100), TILE_WATER_DEEP.getCode());
		tiles.put(makeColor(100, 100, 255), TILE_WATER_LOW.getCode());
		tiles.put(makeColor(200, 200, 0), TILE_SAND.getCode());
		tiles.put(makeColor(255, 150, 0), TILE_DIRT.getCode());

		tiles.put(makeColor(0, 100, 0), TILE_FOREST_LEAF.getCode());
		tiles.put(makeColor(50, 50, 50), TILE_FOREST_FIR.getCode());
		tiles.put(makeColor(0, 255, 0), TILE_GRASS.getCode());
	}
}
