package com.a4server.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by arksu on 07.01.2015.
 */
public class Rect
{
    private static final Logger _log = LoggerFactory.getLogger(Rect.class.getName());

    protected volatile int _left, _top, _right, _bottom;

    public Rect(int left, int top, int right, int bottom)
    {
        _left = left;
        _top = top;
        _right = right;
        _bottom = bottom;
        normalize();
    }

    public Rect(int size)
    {
        _left = -size;
        _top = -size;
        _right = size;
        _bottom = size;
    }

    public void normalize()
    {
        if (_right < _left)
        {
            int t = _left;
            _left = _right;
            _right = t;
        }
        if (_bottom < _top)
        {
            int t = _top;
            _top = _bottom;
            _bottom = t;
        }
    }

    public void add(Rect b)
    {
        _left += b._left;
        _right += b._right;
        _top += b._top;
        _bottom += b._bottom;
    }

    public void add(int dist)
    {
        _left -= dist;
        _top -= dist;
        _right += dist;
        _bottom += dist;
    }

    public int getLeft()
    {
        return _left;
    }

    public int getRight()
    {
        return _right;
    }

    public int getTop()
    {
        return _top;
    }

    public int getBottom()
    {
        return _bottom;
    }

    public boolean isPointInside(int x, int y)
    {
        return (x >= _left && x < _right && y >= _top && y < _bottom);
    }

    public Rect move(int x, int y)
    {
        _left += x;
        _right += x;
        _top += y;
        _bottom += y;
        return this;
    }

    public Rect clone()
    {
        return new Rect(_left, _top, _right, _bottom);
    }

    public boolean isIntersect(Rect r2)
    {
        return (((this._left > r2._left) && (this._left <= r2._right)) || ((this._right > r2._left) && (this._right <= r2._right)) ||
                ((r2._left > this._left) && (r2._left <= this._right)) || ((r2._right > this._left) && (r2._right <= this._right))) && (((this._top > r2._top) && (this._top <= r2._bottom)) || ((this._bottom > r2._top) && (this._bottom <= r2._bottom)) ||
                ((r2._top > this._top) && (r2._top <= this._bottom)) || ((r2._bottom > this._top) && (r2._bottom <= this._bottom)));
    }
}
