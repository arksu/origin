/*
 * This file is part of the Origin-World game client.
 * Copyright (C) 2013 Arkadiy Fattakhov <ark@ark.su>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, version 3 of the License.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.a2client.utils3d;


import com.a2client.Log;
import com.a2client.corex.Anim;
import com.a2client.corex.Const;
import com.a2client.corex.Skeleton;
import com.a2client.util.WeightList;
import com.a2client.xml.XML;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Random;

public class CharacterActions
{
    public Map<String, Action> actions = new HashMap<String, Action>();
    private Random rnd;
    private String animations_prefix;

    public enum LOOP_MODE
    {
        lmOnce,
        lmRepeat
    }

    public CharacterActions(XML xml, String animations_prefix, final Character character)
    {
        // загружаем все действия
        for (Iterator<XML> i = xml.getIterator(); i.hasNext(); )
        {
            XML x = i.next();
            if (x.getTag().equals("action"))
            {
                Action a = new Action(x)
                {
                    protected void onEndAction()
                    {
                        Log.debug("onEndAction: " + name);
                        character.onActionEnd(this);
                    }
                };
                actions.put(a.name, a);
            }
        }

        rnd = new Random();
        rnd.setSeed(rnd.nextInt());
        this.animations_prefix = animations_prefix;
    }

    public boolean play(Skeleton skeleton, String name, Anim.LOOP_MODE loop_mode)
    {
        Action a = actions.get(name);
        if (a != null)
        {
            a.Play(skeleton, name, loop_mode);
            return true;
        }
        return false;
    }

    public boolean play(Skeleton skeleton, String name)
    {
        return play(skeleton, name, Anim.LOOP_MODE.lmRepeat);
    }

    public class Action
    {
        public String name;
        public WeightList<AnimAction> anims = new WeightList<AnimAction>();
        LOOP_MODE loop_mode = LOOP_MODE.lmRepeat;

        public Action(XML xml)
        {
            name = xml.params.get("name");

            if (xml.params.get("loop").equals("repeat"))
                loop_mode = LOOP_MODE.lmRepeat;

            if (xml.params.get("loop").equals("once"))
                loop_mode = LOOP_MODE.lmOnce;

            // загружаем все анимации для действия
            for (Iterator<XML> i = xml.getIterator(); i.hasNext(); )
            {
                XML x = i.next();
                if (x.getTag().equals("anim"))
                {
                    AnimAction a = new AnimAction(x);
                    anims.add(a, a.Weight);
                }
            }
        }

        public void Play(final Skeleton skeleton, final String a_name, final Anim.LOOP_MODE loop_mode_play)
        {
            // выбираем случайную анимацию из списка на основе весов
            AnimAction animAction = anims.pick(rnd);

            Anim anim = new Anim(animations_prefix + Const.PATH_DELIM + animAction.name, skeleton, animAction)
            {
                protected void onEnd()
                {
                    // реагируем только если это последняя анимация (активная)
                    if (skeleton.anims.indexOf(this) != skeleton.anims.size() - 1)
                        return;

                    Log.debug("on anim end: " + a_name + " " + name);
                    if (loop_mode == CharacterActions.LOOP_MODE.lmRepeat)
                        Play(skeleton, a_name, loop_mode_play);
                    else
                        onEndAction();
                }
            };

            anim.ActionName = name;
            skeleton.addAnim(anim);
            anim.play(animAction.BlendWeight, animAction.BlendTime / 1000f, loop_mode_play);
        }


        protected void onEndAction() { } // abstract
    }

    public class AnimAction
    {
        public int BlendTime;
        public float BlendWeight;
        public int Weight;
        public String name;
        public boolean is_merge;

        public AnimAction(XML xml)
        {
            BlendTime = Integer.parseInt(xml.params.get("blendtime"));
            BlendWeight = Float.parseFloat(xml.params.get("blendweight"));
            Weight = Integer.parseInt(xml.params.get("weight"));
            name = xml.params.get("file");
            is_merge = xml.params.get("merge").equals("1");

            if (is_merge)
                Log.debug("111");
        }
    }

}
