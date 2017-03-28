package com.a2client.model;

import com.a2client.network.game.serverpackets.ObjectAdd;

/**
 * особый вид игрового объекта - персонаж
 * Created by arksu on 24.03.17.
 */
public class Character extends GameObject
{
	private final Equip _equip;

	public Character(ObjectAdd pkt)
	{
		super(pkt);
		_equip = new Equip(this);
	}

	public Equip getEquip()
	{
		return _equip;
	}

	@Override
	public void setMoving(boolean moving)
	{
		if (moving == _isMoving) return;
		_isMoving = moving;
		if (_isMoving)
		{
			_model.playAnimation("walk");
		}
		else
		{
			_model.playAnimation("idle");
		}
	}
}
