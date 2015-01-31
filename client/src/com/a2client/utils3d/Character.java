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
import com.a2client.corex.*;
import com.a2client.xml.XML;
import com.a2client.xml.XMLIterator;

import java.util.*;

/**
 * основной класс реализации для визуализации персонажа
 */
public class Character extends CustomNode
{
    // перс имеет анимации. в данный момент времени проигрывается 1 анимация
    // можно одевать эквип. знать что одеть и в какой слот (кость)
    // можем сменить модель тела
    // должен уметь передвигаться с заданным режимом
    // координаты в 2д

    // должен уметь показывай свой скелет (для отладки)
    // учитывать зум

    /**
     * родитель. тот к чем забиндены
     */
    private Character parent = null;

    /**
     * забинденые объекты ко мне
     */
    private List<Character> binded = new LinkedList<Character>();

    /**
     * скелет
     */
    private Skeleton skeleton;

    /**
     * загруженные и установленные меши
     */
    private List<Mesh> meshes = new LinkedList<Mesh>();


    /**
     * список эквипа который висит на чаре
     */
    private List<Equip> equip = new LinkedList<Equip>();

    /**
     * анимации
     */
    private CharacterActions actions;

    /**
     * кэш загруженных анимаций
     */
    protected static Map<String, CharacterActions> actionsMap = new HashMap<String, CharacterActions>();

    /**
     * список отображений персонажей (конфиг)
     */
    public static Map<String, String> characters_config = new HashMap<String, String>();

    static
    {
        XML xml = XML.load("characters");

        xml.ProcessNodes("character", new XMLIterator()
        {
            public void ProcessNode(XML x)
            {
                characters_config.put(x.params.get("obj_type"), x.params.get("file"));
            }
        });
    }

    public enum STATE
    {
        csNone,
        csIdle,
        csDead,
        csWalk,
        csRun,
        csAction
    }

    protected STATE state = STATE.csNone;

    public enum MERGE_STATE
    {
        msNone,
        msHandUp
    }

    protected static Map<MERGE_STATE, String> merge_names = new HashMap<MERGE_STATE, String>();

    static
    {
        merge_names.put(MERGE_STATE.msHandUp, "handup");
    }

    protected MERGE_STATE merge_state = MERGE_STATE.msNone;


    /**
     * создать персонажа
     *
     * @param class_type тип
     */
    public Character(String class_type)
    {
        super(class_type);

        // skeleton
        skeleton = new Skeleton(name + Const.PATH_DELIM + xml.getNode("skeleton").params.get("file"));

        // берем действия из кэша
        actions = actionsMap.get(class_type);
        // если в кэше пусто - грузим
        if (actions == null)
        {
            actions = new CharacterActions(xml, class_type, this);
            // добавляем в кэш
            actionsMap.put(class_type, actions);
        }

        // установим главное тело
        setBody("main");

        setState(STATE.csIdle);

        // загрузим эквип
        for (Iterator<XML> i = xml.getIterator(); i.hasNext(); )
        {
            XML bx = i.next();

            // equip
        }

        setMatrix(new Mat4f().identity());
    }

    /**
     * забиндить к другому объекту
     *
     * @param parent    объект
     * @param bone_name имя кости к которой линкуемся у него
     */
    public void Bind(Character parent, String bone_name)
    {
        if (parent == null)
            UnBind();

        if (this.parent != parent && !parent.binded.contains(this))
        {
            this.parent = parent;

            skeleton.anims.clear();
            skeleton.setParent(parent.getSkeleton(), parent.getSkeleton().data.getIdx(bone_name));
            skeleton.ResetState();
            parent.binded.add(this);
        }
    }

    /**
     * отлинковать от другого объекта
     */
    public void UnBind()
    {
        if (parent == null)
            return;

        parent.binded.remove(this);
        skeleton.anims.clear();
        skeleton.setParent(null, 0);
        skeleton.ResetState();
        this.parent = null;
    }

    /**
     * установить основное тело
     *
     * @param name имя
     */
    public void setBody(String name)
    {
        for (Iterator<XML> i = xml.getIterator(); i.hasNext(); )
        {
            XML bx = i.next();
            // main body
            if (bx.getTag().equals("body") && bx.params.get("name").equals(name))
            {

                for (Iterator<XML> im = bx.getIterator(); im.hasNext(); )
                {
                    XML x = im.next();

                    if (x.getTag().equals("mesh"))
                    {
                        Mesh mesh = new Mesh(this.name + Const.PATH_DELIM + x.params.get("file"));
                        mesh.setMaterial(Material.load(this.name + Const.PATH_DELIM + x.params.get("material")));
                        mesh.setSkeleton(skeleton);
                        meshes.add(mesh);
                    }
                }
            }
        }
    }

    /**
     * одеть вещь эквипа
     *
     * @param what  что одеть, название
     * @param where куда одеть, название кости - если пусто биндим в кость из хмл
     */
    public void BindEquip(String what, String where)
    {
        String bone;
        // если явно указано куда биндить туда и биндим
        if (where.isEmpty())
        {
            bone = xml.params.get("bone");
        }
        else
        {
            bone = where;
        }
        if (bone.isEmpty())
            return;

        // уже забиндено?
        for (Equip e : equip)
        {
            if (e.name.equals(what) && e.binded.equals(bone))
                return;
        }

        // мб есть свободный незабинденый эквип?
        for (Equip e : equip)
        {
            if (e.name.equals(what) && e.binded.isEmpty())
            {
                e.Bind(bone);
                return;
            }
        }

        XML xml = FindEquipXML(what);
        if (xml != null)
        {
            Log.debug("load equip: " + what);
            Equip e = new Equip(what, this, xml);
            e.Bind(bone);
            equip.add(e);
        }
    }

    /**
     * снять одетую вещь
     *
     * @param what имя эквипа или кости
     */
    public void UnbindEquip(String what)
    {
        for (Equip e : equip)
        {
            if (e.name.equals(what))
                e.Unbind();
            if (e.binded.equals(what))
                e.Unbind();
        }


    }

    public XML FindEquipXML(String name)
    {
        for (Iterator<XML> i = xml.getIterator(); i.hasNext(); )
        {
            XML x = i.next();
            // main body
            if (x.getTag().equals("equip") && x.params.get("name").equals(name))
            {
                return x;
            }
        }

        return null;
    }


    /**
     * установить основное состояние
     */
    public void setState(STATE state)
    {
        if (this.state != state)
        {
            Log.debug("Character.setState " + state.toString());
            this.state = state;
            switch (state)
            {
                case csIdle:
                    play_action("idle", false);
                    break;
                case csWalk:
                    play_action("walk", false);
                    break;
                case csRun:
                    play_action("run", false);
                    break;

                default:
            }
        }
    }

    /**
     * проиграть анимацию действия
     */
    public void PlayAction(String action_name)
    {
        PlayAction(action_name, false);
    }

    public void PlayAction(String action_name, boolean clear_anims)
    {
        if (play_action(action_name, clear_anims))
        {
            Log.debug("Character.PlayAction " + action_name);
            state = STATE.csAction;
        }
    }

    public void onActionEnd(CharacterActions.Action action)
    {
        if (!StandartActionName(action.name) && state == STATE.csAction)
        {
            setState(STATE.csIdle);
        }
    }

    protected void Render()
    {
        SetAngle();
        Render.ModelMatrix = matrix;
        render_mesh();
        render_equip();
        render_binded();
    }

    public void render_mesh()
    {
        for (Mesh m : meshes)
        {
            m.Render();
        }
    }

    public void render_equip()
    {
        for (Equip e : equip)
        {
            e.Render();
        }
    }

    public void render_binded()
    {
        for (Character c : binded)
        {
            c.Render();
        }
    }

    @Override
    protected void RenderSkeleton()
    {
        Render.ModelMatrix = matrix;
        skeleton.draw_bones();
    }

    protected void Update()
    {
        skeleton.update();
    }

    public Skeleton getSkeleton()
    {
        return skeleton;
    }


    protected boolean play_action(String name, boolean clear_anims)
    {
        //        for (Anim a : skeleton.anims) {
        //            if (a.ActionName.equals(name)) {
        //                a.play_repeat( a.animAction.BlendWeight, a.animAction.BlendTime / 1000f, Anim.LOOP_MODE.lmRepeat );
        //                Log.debug("play_action <"+name+"> play repeat");
        //                return;
        //            }
        //        }

        // среди мержинг анимаций ищем ту что надо проиграть
        for (Anim a : skeleton.merge_anims)
        {
            if (a.ActionName.equals(name))
            {
                // если нашли - повторяем ее. чтобы не создавать новую. ведь такая же уже играет
                a.play_repeat(a.animAction.BlendWeight, a.animAction.BlendTime / 1000f, Anim.LOOP_MODE.lmRepeat);
                Log.debug("play_action <" + name + "> play repeat");
                return true;
            }
        }
        if (clear_anims)
            skeleton.anims.clear();
        return actions.play(skeleton, name);
    }

    public void StopAction(String name)
    {
        for (Anim a : skeleton.merge_anims)
        {
            if (a.ActionName.equals(name))
            {
                a.stop(a.animAction.BlendTime / 1000f);
            }
        }
        for (Anim a : skeleton.anims)
        {
            if (a.ActionName.equals(name))
            {
                a.stop(a.animAction.BlendTime / 1000f);
            }
        }
    }

    /**
     * установить состояние мержинга анимаций (различные позиции рук и прочее)
     *
     * @param s состояние
     */
    public void SetMergeState(MERGE_STATE s)
    {
        if (merge_state != s)
        {
            switch (s)
            {
                // если выключаем все состояния
                case msNone:
                    // останавливаем текущее действие
                    StopAction(merge_names.get(merge_state));
                    break;

                // если запускаем какое-то действие
                default:
                    // если было запущено что-то
                    if (merge_state != MERGE_STATE.msNone)
                        // остановим его
                        StopAction(merge_names.get(merge_state));
                    // и запустим новое
                    PlayAction(merge_names.get(s));
            }
            merge_state = s;
        }
    }

    protected boolean StandartActionName(String str)
    {
        return (str.equals("walk") || str.equals("run") || str.equals("idle"));
    }

}
