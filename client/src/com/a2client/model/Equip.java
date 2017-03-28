package com.a2client.model;

import com.a2client.ModelManager;
import com.a2client.g3d.Model;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by arksu on 28.03.17.
 */
public class Equip
{
	public enum Slot
	{
		LHand("EquipHand.L"),
		RHand("EquipHand.R"),
		Head("Head"),
		Body("Chest"),
		LeftFoot("Foot.L"),
		RightFoot("Foot.R"),
		Pants("Torso");

		private final String _boneName;

		Slot(String name)
		{
			_boneName = name;
		}

		public String getBoneName()
		{
			return _boneName;
		}
	}

	private static class Item
	{
		private Model _model;
		InventoryItem _item;

		public Item(Model model, InventoryItem item)
		{
			_model = model;
			_item = item;
		}
	}

	private final Character _character;

	private final Map<Slot, Item> _equipped = new HashMap<>();

	public Equip(Character character)
	{
		_character = character;
	}

	public Map<Slot, Item> getEquipped()
	{
		return _equipped;
	}

	public Slot bind(InventoryItem item)
	{
		Slot slot = Slot.values()[item.getY()];
		Item e = _equipped.get(slot);

		if (e == null || e._item.getTypeId() != item.getTypeId())
		{
			if (e != null)
			{
				e._model.unbind();
			}
			Model model = ModelManager.getInstance().getModelByType(item.getTypeId());
			if (model != null)
			{
				model.bindTo(_character.getModel(), slot.getBoneName());
			}
			_equipped.put(slot, new Item(model, item));
		}
		return slot;
	}

	public void unbind(Slot slot)
	{
		Item e = _equipped.remove(slot);
		if (e != null)
		{
			e._model.unbind();
		}
	}
}
