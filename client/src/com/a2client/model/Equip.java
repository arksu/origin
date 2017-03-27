package com.a2client.model;

/**
 * Created by arksu on 28.03.17.
 */
public class Equip
{
	public enum Slot
	{
		LHand("EquipHand.L"),
		RHand("EquipHand.R");

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
}
