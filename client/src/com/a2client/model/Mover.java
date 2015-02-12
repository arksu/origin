package com.a2client.model;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * класс который сглаживает передвижения объекта путем линейной интерполяции
 * Created by arksu on 12.02.15.
 */
public class Mover
{
    private static final Logger _log = LoggerFactory.getLogger(Mover.class.getName());

    private final GameObject _object;
    private double _currentX;
    private double _currentY;
    private int _cx;
    private int _cy;
    private int _vx;
    private int _vy;
    
    public Mover(GameObject object, int cx, int cy, int vx, int vy) {
        _object = object;
        _cx = cx;
        _cy = cy;
        _vx = vx;
        _vy = vy;
    }
    
    public void UpdateMove(int cx, int cy, int vx, int vy) {
        
        
    }
}
