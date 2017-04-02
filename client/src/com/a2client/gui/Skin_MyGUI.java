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

package com.a2client.gui;

import com.a2client.util.Rect;

public class Skin_MyGUI extends Skin
{
	public void Init()
	{
		SkinElement el;
		int x, y, x1, y1, x2, y2, x3, y3, w1, w2, w3, h1, h2, h3;
		int wi1, wi2, hi1, hi2;
		int x4, x5, x6, x7, y4, y5, y6, y7;

		// WINDOW
		el = new SkinElement(this, "window_caption", "core_skin", 66, 28, new Rect(0, 0, 0, 0));
		w1 = 12;
		w2 = 42;
		w3 = 14;
		h1 = 28;
		h2 = 0;
		h3 = 0;
		x = 356;
		y = 36;
		x1 = x;
		y1 = 70;
		x2 = -1;
		y2 = -1;
		x3 = -1;
		y3 = -1;
		addStates(el, w1, w2, w3, h1, h2, h3, x, x1, x2, x3, y, y1, y2, y3);
		el = new SkinElement(this, "window_caption_close", "core_skin", 66, 28, new Rect(0, 0, 0, 0));
		w1 = 12;
		w2 = 19;
		w3 = 37;
		h1 = 28;
		h2 = 0;
		h3 = 0;
		x = 356;
		y = 104;
		x1 = x;
		y1 = 138;
		x2 = -1;
		y2 = -1;
		x3 = -1;
		y3 = -1;
		addStates(el, w1, w2, w3, h1, h2, h3, x, x1, x2, x3, y, y1, y2, y3);
		el = new SkinElement(this, "window", "core_skin", 21, 19, new Rect(0, 0, 0, 0));
		w1 = 3;
		w2 = 15;
		w3 = 5;
		h1 = 2;
		h2 = 14;
		h3 = 5;
		x = 384;
		y = 173;
		x1 = -1;
		y1 = -1;
		x2 = -1;
		y2 = -1;
		x3 = -1;
		y3 = -1;
		addStates(el, w1, w2, w3, h1, h2, h3, x, x1, x2, x3, y, y1, y2, y3);
		el = new SkinElement(this, "window_resize_right", "core_skin", 9, 9, new Rect(0, 0, 0, 0));
		w1 = 9;
		h1 = 9;
		x = 369;
		y = 185;
		x1 = x;
		y1 = 172;
		x2 = x;
		y2 = -1;
		x3 = x;
		y3 = -1;
		addStatesCenter(el, h1, w1, x, x1, x2, x3, y, y1, y2, y3);
		el = new SkinElement(this, "window_resize_left", "core_skin", 9, 9, new Rect(0, 0, 0, 0));
		w1 = 9;
		h1 = 9;
		x = 356;
		y = 185;
		x1 = x;
		y1 = 172;
		x2 = x;
		y2 = -1;
		x3 = x;
		y3 = -1;
		addStatesCenter(el, h1, w1, x, x1, x2, x3, y, y1, y2, y3);
		// BUTTON =========================================
		el = new SkinElement(this, "button", "core_skin", 27, 19, new Rect(0, 0, 0, 0));
		w1 = 6;
		w2 = 15;
		w3 = 8;
		h1 = 9;
		h2 = 2;
		h3 = 10;
		x = 169;
		y = 24;
		x1 = x;
		y1 = 68;
		x2 = x;
		y2 = 46;
		x3 = x;
		y3 = 2;
		addStates(el, w1, w2, w3, h1, h2, h3, x, x1, x2, x3, y, y1, y2, y3);
		// BUTTON DOWN =========================================
		el = new SkinElement(this, "button_down", "core_skin", 15, 13, new Rect(0, -1, 0, 0));
		w1 = 15;
		h1 = 12;
		x = 94;
		y = 14;
		x1 = x;
		y1 = 40;
		x2 = x;
		y2 = 27;
		x3 = x;
		y3 = 1;
		addStatesCenter(el, h1, w1, x, x1, x2, x3, y, y1, y2, y3);
		// BUTTON UP =========================================
		el = new SkinElement(this, "button_up", "core_skin", 15, 11, new Rect(0, 0, 0, 0));
		w1 = 15;
		h1 = 11;
		x = 94;
		y = 82;
		x1 = x;
		y1 = 110;
		x2 = x;
		y2 = 96;
		x3 = x;
		y3 = 68;
		addStatesCenter(el, h1, w1, x, x1, x2, x3, y, y1, y2, y3);
		// BUTTON LEFT =========================================
		el = new SkinElement(this, "button_left", "core_skin", 12, 14, new Rect(0, 0, 2, 2));
		w1 = 12;
		h1 = 14;
		x = 112;
		y = 18;
		x1 = x;
		y1 = 50;
		x2 = x;
		y2 = 34;
		x3 = x;
		y3 = 2;
		addStatesCenter(el, h1, w1, x, x1, x2, x3, y, y1, y2, y3);
		// BUTTON RIGHT =========================================
		el = new SkinElement(this, "button_right", "core_skin", 13, 15, new Rect(-1, -1, 0, 0));
		w1 = 12;
		h1 = 14;
		x = 112;
		y = 100;
		x1 = x;
		y1 = 132;
		x2 = x;
		y2 = 116;
		x3 = x;
		y3 = 84;
		addStatesCenter(el, h1, w1, x, x1, x2, x3, y, y1, y2, y3);
		// CLOSE BUTTON =========================================================
		el = new SkinElement(this, "button_close", "core_skin", 16, 15, new Rect(0, 0, 2, 2));
		w1 = 18;
		h1 = 17;
		x = 72;
		y = 21;
		x1 = x;
		y1 = 59;
		x2 = x;
		y2 = 40;
		x3 = x;
		y3 = 2;
		addStatesCenter(el, h1, w1, x, x1, x2, x3, y, y1, y2, y3);

		// CHECKBOX ============================================================
		el = new SkinElement(this, "checkbox", "core_skin", 21, 21, new Rect(0, 0, 0, 0));
		w1 = 0;
		w2 = 21;
		w3 = 0;
		h1 = 0;
		h2 = 21;
		h3 = 0;
		x = 2;
		y = 24;
		x1 = x;
		y1 = 68;
		x2 = x;
		y2 = 46;
		x3 = x;
		y3 = 2;
		x4 = x;
		y4 = 112;
		x5 = x;
		y5 = 156;
		x6 = x;
		y6 = 134;
		x7 = x;
		y7 = 90;
		addStates(el, w1, w2, w3, h1, h2, h3, 0, 0, 0, 0, x, y, x1, y1, x2, y2, x3, y3, x4, y4, x5, y5, x6, y6, x7, y7);
		// EDIT =========================================
		el = new SkinElement(this, "edit", "core_skin", 27, 24, new Rect(0, 0, 2, 2));
		w1 = 6;
		w2 = 15;
		w3 = 8;
		h1 = 11;
		h2 = 1;
		h3 = 14;
		x = 235;
		y = 29;
		x1 = x;
		y1 = 83;
		x2 = x;
		y2 = 56;
		x3 = x;
		y3 = 2;
		x4 = x;
		y4 = 110; // focus
		addStates(el, w1, w2, w3, h1, h2, h3, 0, 0, 0, 0, x, y, x1, y1, x2, y2, x3, y3, x4, y4, -1, -1, -1, -1, -1, -1);
		// LISTBOX =========================================
		el = new SkinElement(this, "listbox", "core_skin", 23, 22, new Rect(0, 0, 2, 2));
		w1 = 3;
		w2 = 15;
		w3 = 5;
		h1 = 3;
		h2 = 14;
		h3 = 5;
		x = 429;
		y = 70;
		x1 = -1;
		y1 = -1;
		x2 = -1;
		y2 = -1;
		x3 = -1;
		y3 = -1;
		addStates(el, w1, w2, w3, h1, h2, h3, x, x1, x2, x3, y, y1, y2, y3);
		el = new SkinElement(this, "listbox_item", "core_skin", 3, 3, new Rect(0, 0, 0, 0));
		w1 = 0;
		w2 = 3;
		w3 = 0;
		h1 = 0;
		h2 = 3;
		h3 = 0;
		x = -1;
		y = -1; // normal
		x1 = 326;
		y1 = 124; // hl
		x2 = -1;
		y2 = -1; // pressed
		x3 = -1;
		y3 = -1; // disable
		x4 = 314;
		y4 = 112; // check_normal
		x5 = 326;
		y5 = 113; // check_hl
		addStates(el, w1, w2, w3, h1, h2, h3, 0, 0, 0, 0, x, y, x1, y1, x2, y2, x3, y3, x4, y4, x5, y5, -1, -1, -1, -1);
		// SCROLLBAR ============================================================
		el = new SkinElement(this, "vscroll", "core_skin", 15, 9, new Rect(0, 0, 0, 0));
		w1 = 0;
		w2 = 15;
		w3 = 0;
		h1 = 3;
		h2 = 3;
		h3 = 3;
		wi1 = 0;
		wi2 = 0;
		hi1 = 0;
		hi2 = 40;
		x = 301;
		y = 55;
		x1 = -1;
		y1 = 68;
		x2 = -1;
		y2 = 46;
		x3 = -1;
		y3 = 2;
		addStates(el, w1, w2, w3, h1, h2, h3, wi1, wi2, hi1, hi2, x, x1, x2, x3, y, y1, y2, y3);

		el = new SkinElement(this, "vscroll_track", "core_skin", 15, 5, new Rect(0, 0, 0, 0));
		w1 = 0;
		w2 = 14;
		w3 = 0;
		h1 = 2;
		h2 = 1;
		h3 = 4;
		x = 127;
		y = 10;
		x1 = x;
		y1 = 26;
		x2 = x;
		y2 = 18;
		x3 = x;
		y3 = 2;
		addStates(el, w1, w2, w3, h1, h2, h3, x, x1, x2, x3, y, y1, y2, y3);

		el = new SkinElement(this, "hscroll", "core_skin", 9, 15, new Rect(0, 0, 0, 0));
		w1 = 3;
		w2 = 3;
		w3 = 3;
		h1 = 0;
		h2 = 15;
		h3 = 0;
		wi1 = 0;
		wi2 = 40;
		hi1 = 0;
		hi2 = 0;
		x = 301;
		y = 36;
		x1 = -1;
		y1 = 68;
		x2 = -1;
		y2 = 46;
		x3 = -1;
		y3 = 2;
		addStates(el, w1, w2, w3, h1, h2, h3, wi1, wi2, hi1, hi2, x, x1, x2, x3, y, y1, y2, y3);

		el = new SkinElement(this, "hscroll_track", "core_skin", 5, 15, new Rect(0, 0, 0, 0));
		w1 = 2;
		w2 = 1;
		w3 = 4;
		h1 = 0;
		h2 = 15;
		h3 = 0;
		x = 146;
		y = 16;
		x1 = x;
		y1 = 46;
		x2 = x;
		y2 = 31;
		x3 = x;
		y3 = 1;
		addStates(el, w1, w2, w3, h1, h2, h3, x, x1, x2, x3, y, y1, y2, y3);

		// MEMO ==========================
		el = new SkinElement(this, "memo", "core_skin", 21, 20, new Rect(0, 0, 0, 0));
		w1 = 3;
		w2 = 15;
		w3 = 5;
		h1 = 3;
		h2 = 14;
		h3 = 5;
		x = 429;
		y = 70;
		x1 = -1;
		y1 = -1;
		x2 = -1;
		y2 = -1;
		x3 = -1;
		y3 = -1;
		addStates(el, w1, w2, w3, h1, h2, h3, x, x1, x2, x3, y, y1, y2, y3);

		// HINT ==========================
		el = new SkinElement(this, "hint", "core_skin", 9, 9, new Rect(0, 0, 0, 0));
		w1 = 3;
		w2 = 3;
		w3 = 3;
		h1 = 3;
		h2 = 3;
		h3 = 3;
		x = 205;
		y = 223;
		x1 = -1;
		y1 = -1;
		x2 = -1;
		y2 = -1;
		x3 = -1;
		y3 = -1;
		addStates(el, w1, w2, w3, h1, h2, h3, x, x1, x2, x3, y, y1, y2, y3);
		// BALOON
		el = new SkinElement(this, "baloon_left", "core_skin", 12, 34, new Rect(0, 0, 0, 0));
		w1 = 11;
		w2 = 1;
		w3 = 0;
		h1 = 34;
		h2 = 0;
		h3 = 0;
		x = 430;
		y = 122;
		x1 = -1;
		y1 = -1;
		x2 = -1;
		y2 = -1;
		x3 = -1;
		y3 = -1;
		addStates(el, w1, w2, w3, h1, h2, h3, x, x1, x2, x3, y, y1, y2, y3);
		el = new SkinElement(this, "baloon_center", "core_skin", 13, 34, new Rect(0, 0, 0, 0));
		w1 = 13;
		h1 = 34;
		x = 442;
		y = 122;
		addStatesCenter(el, h1, w1, x, y);
		el = new SkinElement(this, "baloon_right", "core_skin", 13, 34, new Rect(0, 0, 0, 0));
		w1 = 0;
		w2 = 1;
		w3 = 13;
		h1 = 34;
		h2 = 0;
		h3 = 0;
		x = 455;
		y = 122;
		x1 = -1;
		y1 = -1;
		x2 = -1;
		y2 = -1;
		x3 = -1;
		y3 = -1;
		addStates(el, w1, w2, w3, h1, h2, h3, x, x1, x2, x3, y, y1, y2, y3);

		// Rotate
		el = new SkinElement(this, "btn_rotate", "core_skin", 32, 32, new Rect(0, 0, 0, 0));
		w1 = 32;
		h1 = 32;
		x = 252;
		y = 146;
		addStatesStretch(el, h1, w1, x, y);

		// ICONS =========================================
		el = new SkinElement(this, "icon_bg", "core_skin", 34, 34, new Rect(0, 0, 0, 0));
		w1 = 34;
		h1 = 34;
		x = 695;
		y = 2;
		addStatesCenter(el, h1, w1, x, y);

		el = new SkinElement(this, "icon_unknown", "core_skin", 34, 34, new Rect(0, 0, 0, 0));
		w1 = 34;
		h1 = 34;
		x = 695;
		y = 2;
		addStatesCenter(el, h1, w1, x, y);

		el = new SkinElement(this, "icon_run_1", "core_skin", 5, 5, new Rect(0, 0, 0, 0));
		w1 = 5;
		h1 = 5;
		x = 509;
		y = 108;
		addStatesCenter(el, h1, w1, x, y);
		el = new SkinElement(this, "icon_run_2", "core_skin", 5, 5, new Rect(0, 0, 0, 0));
		w1 = 5;
		h1 = 5;
		x = 515;
		y = 108;
		addStatesCenter(el, h1, w1, x, y);
		el = new SkinElement(this, "icon_run_3", "core_skin", 5, 5, new Rect(0, 0, 0, 0));
		w1 = 5;
		h1 = 5;
		x = 520;
		y = 108;
		addStatesCenter(el, h1, w1, x, y);
		el = new SkinElement(this, "icon_run_4", "core_skin", 5, 5, new Rect(0, 0, 0, 0));
		w1 = 5;
		h1 = 5;
		x = 525;
		y = 108;
		addStatesCenter(el, h1, w1, x, y);

		el = new SkinElement(this, "icon_gui_rotate", "icons", 32, 32, new Rect(0, 0, 0, 0));
		w1 = 32;
		h1 = 32;
		x = 160;
		y = 0;
		addStatesCenter(el, h1, w1, x, y);
		el = new SkinElement(this, "icon_gui_close", "icons", 32, 32, new Rect(0, 0, 0, 0));
		w1 = 32;
		h1 = 32;
		x = 128;
		y = 0;
		addStatesCenter(el, h1, w1, x, y);
		el = new SkinElement(this, "icon_gui_move", "icons", 32, 32, new Rect(0, 0, 0, 0));
		w1 = 32;
		h1 = 32;
		x = 96;
		y = 0;
		addStatesCenter(el, h1, w1, x, y);
		el = new SkinElement(this, "icon_abar_nav_left", "icons", 32, 32, new Rect(0, 0, 0, 0));
		w1 = 32;
		h1 = 32;
		x = 32;
		y = 0;
		addStatesCenter(el, h1, w1, x, y);
		el = new SkinElement(this, "icon_abar_nav_right", "icons", 32, 32, new Rect(0, 0, 0, 0));
		w1 = 32;
		h1 = 32;
		x = 64;
		y = 0;
		addStatesCenter(el, h1, w1, x, y);
		el = new SkinElement(this, "icon_abar_nav_top", "icons", 32, 32, new Rect(0, 0, 0, 0));
		w1 = 32;
		h1 = 32;
		x = 192;
		y = 0;
		addStatesCenter(el, h1, w1, x, y);
		// ActionBar Elements ===========================================
		el = new SkinElement(this, "element_button_left", "core_skin", 12, 32, new Rect(0, 0, 0, 0));
		w1 = 12;
		h1 = 32;
		x = 184;
		y = 180;
		addStatesCenter(el, h1, w1, x, y);
		el = new SkinElement(this, "element_button_right", "core_skin", 12, 32, new Rect(0, 0, 0, 0));
		w1 = 12;
		h1 = 32;
		x = 198;
		y = 180;
		addStatesCenter(el, h1, w1, x, y);

		// HOURGLASS ===========================================
		w1 = 51;
		h1 = 112;
		el = new SkinElement(this, "hourglass_1", "sand_clock", 51, 112, new Rect(0, 0, 0, 0));
		x = 0;
		y = 0;
		addStatesCenter(el, h1, w1, x, y);
		el = new SkinElement(this, "hourglass_2", "sand_clock", 51, 112, new Rect(0, 0, 0, 0));
		x = 51;
		y = 0;
		addStatesCenter(el, h1, w1, x, y);
		el = new SkinElement(this, "hourglass_3", "sand_clock", 51, 112, new Rect(0, 0, 0, 0));
		x = 102;
		y = 0;
		addStatesCenter(el, h1, w1, x, y);
		el = new SkinElement(this, "hourglass_4", "sand_clock", 51, 112, new Rect(0, 0, 0, 0));
		x = 153;
		y = 0;
		addStatesCenter(el, h1, w1, x, y);
		el = new SkinElement(this, "hourglass_5", "sand_clock", 51, 112, new Rect(0, 0, 0, 0));
		x = 204;
		y = 0;
		addStatesCenter(el, h1, w1, x, y);
		el = new SkinElement(this, "hourglass_6", "sand_clock", 51, 112, new Rect(0, 0, 0, 0));
		x = 0;
		y = 112;
		addStatesCenter(el, h1, w1, x, y);
		el = new SkinElement(this, "hourglass_7", "sand_clock", 51, 112, new Rect(0, 0, 0, 0));
		x = 51;
		y = 112;
		addStatesCenter(el, h1, w1, x, y);
		el = new SkinElement(this, "hourglass_8", "sand_clock", 51, 112, new Rect(0, 0, 0, 0));
		x = 102;
		y = 112;
		addStatesCenter(el, h1, w1, x, y);
		el = new SkinElement(this, "hourglass_9", "sand_clock", 51, 112, new Rect(0, 0, 0, 0));
		x = 153;
		y = 112;
		addStatesCenter(el, h1, w1, x, y);
		el = new SkinElement(this, "hourglass_10", "sand_clock", 51, 112, new Rect(0, 0, 0, 0));
		x = 204;
		y = 112;
		addStatesCenter(el, h1, w1, x, y);
		el = new SkinElement(this, "hourglass_11", "sand_clock", 51, 112, new Rect(0, 0, 0, 0));
		x = 0;
		y = 224;
		addStatesCenter(el, h1, w1, x, y);
		el = new SkinElement(this, "hourglass_12", "sand_clock", 51, 112, new Rect(0, 0, 0, 0));
		x = 51;
		y = 224;
		addStatesCenter(el, h1, w1, x, y);
		el = new SkinElement(this, "hourglass_13", "sand_clock", 51, 112, new Rect(0, 0, 0, 0));
		x = 102;
		y = 224;
		addStatesCenter(el, h1, w1, x, y);
		el = new SkinElement(this, "hourglass_14", "sand_clock", 51, 112, new Rect(0, 0, 0, 0));
		x = 153;
		y = 224;
		addStatesCenter(el, h1, w1, x, y);
		el = new SkinElement(this, "hourglass_15", "sand_clock", 51, 112, new Rect(0, 0, 0, 0));
		x = 204;
		y = 224;
		addStatesCenter(el, h1, w1, x, y);

		el = new SkinElement(this, "hotbar_slot_anim_1", "core_skin", 64, 64, new Rect(0, 0, 0, 0));
		w1 = 64;
		h1 = 64;
		x = 400;
		y = 200;
		addStatesCenter(el, h1, w1, x, y);

		el = new SkinElement(this, "hotbar_slot_anim_2", "core_skin", 64, 64, new Rect(0, 0, 0, 0));
		w1 = 64;
		h1 = 64;
		x = 464;
		y = 200;
		addStatesCenter(el, h1, w1, x, y);

		el = new SkinElement(this, "hotbar_slot_anim_3", "core_skin", 64, 64, new Rect(0, 0, 0, 0));
		w1 = 64;
		h1 = 64;
		x = 528;
		y = 200;
		addStatesCenter(el, h1, w1, x, y);

		el = new SkinElement(this, "hotbar_slot_anim_4", "core_skin", 64, 64, new Rect(0, 0, 0, 0));
		w1 = 64;
		h1 = 64;
		x = 592;
		y = 200;
		addStatesCenter(el, h1, w1, x, y);

		el = new SkinElement(this, "hotbar_slot_anim_5", "core_skin", 64, 64, new Rect(0, 0, 0, 0));
		w1 = 64;
		h1 = 64;
		x = 656;
		y = 200;
		addStatesCenter(el, h1, w1, x, y);

		el = new SkinElement(this, "hotbar_slot", "core_skin", 34, 34, new Rect(0, 0, 0, 0));
		w1 = 34;
		h1 = 34;
		x = 466;
		y = 166;
		addStatesCenter(el, h1, w1, x, y);

		el = new SkinElement(this, "hotbar_bg", "core_skin", 35, 35, new Rect(0, 0, 3, 3));
		w1 = 15;
		w2 = 1;
		w3 = 19;
		h1 = 15;
		h2 = 1;
		h3 = 19;
		x = 504;
		y = 158;
		x1 = -1;
		y1 = -1;
		x2 = -1;
		y2 = -1;
		x3 = -1;
		y3 = -1;
		addStates(el, w1, w2, w3, h1, h2, h3, x, x1, x2, x3, y, y1, y2, y3);

		el = new SkinElement(this, "equip_man", "core_skin", 151, 284, new Rect(0, 0, 0, 0));
		w1 = 151;
		h1 = 284;
		x = 747;
		y = 3;
		addStatesCenter(el, h1, w1, x, y);

	}

}
