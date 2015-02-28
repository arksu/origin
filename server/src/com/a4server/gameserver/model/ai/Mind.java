package com.a4server.gameserver.model.ai;

import com.a4server.gameserver.model.collision.CollisionResult;
import com.a4server.gameserver.model.event.Event;

/**
 * мозг объекта который "думает"
 * должен реагировать на внутренние (передвижения, падение хп) 
 * и внешние раздражители (различные события от грида) 
 * Created by arksu on 27.02.15.
 */
public interface Mind
{
    /**
     * объект закончил движение
     */
    void onArrived(CollisionResult moveResult);

    /**
     * обработать игровой тик. периодически что-то делать
     */
    void onTick();

    /**
     * обработать событие, внешний раздражитель
     * @param event событие
     */
    void handleEvent(Event event);

    /**
     * отключить этот мозг, надо сделать все чтобы корректно перевести объект в нейтральное состояние 
     */
    void free();

    /**
     * мозг подключен к объекту. начало работы
     */
    void begin();
}
