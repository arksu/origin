package com.a2client.network.game.clientpackets;

public class CharacterCreate extends GameClientPacket
{
    private String _name;
    private int _sex;
    private int _hairColor;
    private int _hairStyle;
    private int _face;

    public CharacterCreate(String name, int sex, int hairColor, int hairStyle, int face)
    {
        _face = face;
        _name = name;
        _hairColor = hairColor;
        _hairStyle = hairStyle;
        _sex = sex;
    }

    @Override
    protected void write()
    {
        writeC(0x05);

        writeS(_name);
        writeC(_sex);
        writeC(_hairColor);
        writeC(_hairStyle);
        writeC(_face);
    }
}
