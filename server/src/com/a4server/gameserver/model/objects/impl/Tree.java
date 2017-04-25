package com.a4server.gameserver.model.objects.impl;

import com.a4server.gameserver.model.GameObject;
import com.a4server.gameserver.model.Grid;
import com.a4server.gameserver.model.Player;
import com.a4server.gameserver.model.objects.ObjectTemplate;
import com.a4server.gameserver.model.objects.ObjectsFactory;
import com.a4server.gameserver.model.position.ObjectPosition;
import com.a4server.util.Rnd;
import com.a4server.util.Vec2i;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * деревья
 * Created by arksu on 22.08.16.
 */
public class Tree extends GameObject
{
	private static final Logger _log = LoggerFactory.getLogger(Tree.class.getName());

	public Tree(Grid grid, ResultSet rset) throws SQLException
	{
		super(grid, rset);
	}

	public Tree(int objectId, ObjectTemplate template)
	{
		super(objectId, template);
	}

	@Override
	public List<String> getContextMenu(Player player)
	{
		List<String> list = new ArrayList<>();
		list.add("take_branch");
		list.add("chop");
		return list;
	}

	@Override
	protected void contextRun(Player player, String contextItem)
	{
		_log.debug("context: " + contextItem);

		if ("take_branch".equals(contextItem))
		{
			player.generateItem(ObjectsFactory.getInstance().getTemplate("branch").getTypeId(), getQuality(), true);
		}
		else if ("chop".equals(contextItem))
		{
			player.doAction(5, this, () ->
			{
				_log.debug("run chop!");
				spawnLog(40, player);
				spawnLog(90, player);
				delete();
				spawnStump();
			});
		}
	}

	private void spawnLog(final int offset, final Player player)
	{
		GameObject log = ObjectsFactory.getInstance().createObject("tree_log");

		log.setQuality(getQuality());

		Vec2i d = this.getPos().sub(player.getPos());

		double l = d.len();
		double dx = (d.x / l) * offset;
		double dy = (d.y / l) * offset;

		ObjectPosition pos = log.setPos(new ObjectPosition(
				this.getPos().getX() + (int) Math.round(dx),
				this.getPos().getY() + (int) Math.round(dy),
				0, this.getPos().getLevel(),
				log));
		pos.setHeading(Vec2i.z.direction(d));

		if (!log.getPos().trySpawn() || !log.store())
		{
			_log.error("failed spawn log tree");
		}
	}

	private void spawnStump()
	{
		GameObject stump = ObjectsFactory.getInstance().createObject("pine_stump");

		stump.setQuality(getQuality());

		ObjectPosition pos = stump.setPos(new ObjectPosition(this.getPos(), stump));
		pos.setHeading(Rnd.get(360));

		if (!stump.getPos().trySpawn() || !stump.store())
		{
			_log.error("failed spawn stump");
		}
	}
}
