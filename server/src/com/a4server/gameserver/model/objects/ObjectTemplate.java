package com.a4server.gameserver.model.objects;

/**
 * шаблон объекта по которому создаются конечные экземпляры
 * Created by arksu on 15.02.15.
 */
public interface ObjectTemplate
{
    /**
     * уникальный идентификатор типа объекта
     */
    public int getTypeId();

    /**
     * ширина объекта
     */
    public int getWidth();

    /**
     * высота объекта
     */
    public int getHeight();

    /**
     * имя шаблона
     */
    public String getName();

    public CollisionTemplate getCollision();
}