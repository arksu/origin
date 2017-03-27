package com.a2client.model;

import com.a2client.ModelManager;
import com.a2client.g3d.Model;
import com.a2client.network.game.serverpackets.ObjectAdd;

/**
 * особый вид игрового объекта - персонаж
 * Created by arksu on 24.03.17.
 */
public class Character extends GameObject
{
	public Character(ObjectAdd pkt)
	{
		super(pkt);
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

	public void equip(int typeId, Equip.Slot slot)
	{
		Model model = ModelManager.getInstance().getModelByType(typeId);
		model.bindTo(this._model, slot.getBoneName());
	}
}
