package com.a4server.gameserver.model.ai;

import com.a4server.gameserver.model.event.Event;

/**
 * мозг объекта который "думает"
 * должен реагировать на внутренние (передвижения, падение хп) 
 * и внешние раздражители (различные события от грида) 
 * Created by arksu on 27.02.15.
 */
public interface Mind
{
    void onArrived();

    void onTick();
    
    void handleEvent(Event event);
}
