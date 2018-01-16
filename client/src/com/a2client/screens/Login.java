package com.a2client.screens;

import com.a2client.Config;
import com.a2client.Input;
import com.a2client.Lang;
import com.a2client.Main;
import com.a2client.gui.*;
import com.a2client.network.Net;
import com.a2client.network.netty.NettyConnection;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import com.badlogic.gdx.graphics.Texture;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Login extends BaseScreen
{
	private static final Logger _log = LoggerFactory.getLogger(Login.class.getName());

	static public String _account;
	static public String _password;
	static private String _status = "";
	/**
	 * текст ошибки в процессе логина. если установлено - выполнится onError()
	 */
	static public String _login_error = null;

	static public String _gameserver_host;
	static public int _gameserver_port;
	static public int _gameserver_key1;
	static public int _gameserver_key2;

	/**
	 * gui контролы экрана логина
	 */
	private GUI_Button btn_login, btn_exit, btn_options;
	GUI_Label lbl_login, lbl_password, lbl_status;
	GUI_Edit edit_login, edit_password;
	GUI_Texture logo;

	public Login()
	{
		GUI.reCreate();

		logo = new GUI_Texture(GUI.rootNormal());
		logo.setTexture(Main.getAssetManager().get(Config.RESOURCE_DIR + "origin_logo.png", Texture.class));
		logo.setPos(0, 10);
		logo.centerX();

		int ypos = logo.pos.y + logo.getHeight() + 20;
		ypos = Math.max(ypos, (Gdx.graphics.getHeight() / 2) - 90);

		lbl_login = new GUI_Label(GUI.rootNormal());
		lbl_login.caption = Lang.getTranslate("LoginScreen.account");
		lbl_login.setPos(10, ypos);
		lbl_login.UpdateSize();
		lbl_login.centerX();

		edit_login = new GUI_Edit(GUI.rootNormal());
		edit_login.setSize(150, 25);
		edit_login.setPos(lbl_login.pos.add(0, 15));
		edit_login.centerX();
		edit_login.text = Config.getInstance()._account;

		lbl_password = new GUI_Label(GUI.rootNormal());
		lbl_password.caption = Lang.getTranslate("LoginScreen.password");
		lbl_password.setPos(edit_login.pos.add(0, 40));
		lbl_password.UpdateSize();
		lbl_password.centerX();

		edit_password = new GUI_Edit(GUI.rootNormal());
		edit_password.setSize(150, 25);
		edit_password.setPos(lbl_password.pos.add(0, 15));
		edit_password.centerX();
		edit_password.secret_symbol = "*";
		edit_password.allow_copy = false;
		edit_password.text = Config.getInstance()._password;

		btn_login = new GUI_Button(GUI.rootNormal())
		{
			@Override
			public void doClick()
			{
				doLogin();
			}
		};
		btn_login.caption = Lang.getTranslate("LoginScreen.login");
		btn_login.setPos(edit_password.pos.add(0, 40));
		btn_login.setSize(100, 25);
		btn_login.centerX();

		lbl_status = new GUI_Label(GUI.rootNormal());
		lbl_status.caption = "";
		lbl_status.setPos(btn_login.pos.add(0, 35));

		btn_exit = new GUI_Button(GUI.rootNormal())
		{
			@Override
			public void doClick()
			{
				System.exit(0);
			}
		};
		btn_exit.caption = Lang.getTranslate("LoginScreen.quit");
		btn_exit.setSize(100, 25);
		btn_exit.setPos(Gdx.graphics.getWidth() - 110, Gdx.graphics.getHeight() - 35);

		btn_options = new GUI_Button(GUI.rootNormal());
		btn_options.caption = Lang.getTranslate("LoginScreen.options");
		btn_options.setSize(100, 25);
		btn_options.setPos(btn_exit.pos.add(0, -35));
		btn_options.enabled = false;

		if (!edit_login.text.isEmpty())
		{
			GUI.getInstance().setFocus(edit_password);
			edit_password.SelectAll();
		}

		if (Config.getInstance()._quickLoginMode)
		{
			doLogin();
		}

		// тут запустим музыку в фоне
		if (!LwjglApplicationConfiguration.disableAudio)
		{

		}
	}

	protected void doLogin()
	{
		if (edit_login.text.isEmpty() || edit_password.text.isEmpty())
		{
			return;
		}

		_account = edit_login.text;
		_password = edit_password.text;
		Config.getInstance()._account = _account;
		Config.getInstance()._password = _password;
		_status = "login";

		btn_login.enabled = false;
		edit_login.enabled = false;
		edit_password.enabled = false;

		Config.getInstance()._quickLoginMode = false;

		if (Net.getConnection() != null)
		{
			Net.getConnection().Close();
		}
		Net.newConnection(Config.getInstance()._loginServer, Config.getInstance()._loginServerPort, NettyConnection.ConnectionType.LOGIN_SERVER);

	}

	static public void setStatus(String s)
	{
		_status = s;
	}

	static public void Error(String s)
	{
		_login_error = s;
	}

	static public void onError()
	{
		_status = _login_error;
		_login_error = null;
		Config.getInstance()._quickLoginMode = false;
		Main.ReleaseAll();
	}

	public void onUpdate()
	{
		// upd block
		if (Input.KeyHit(com.badlogic.gdx.Input.Keys.TAB))
		{
			if (!edit_login.isFocused())
			{
				GUI.getInstance().setFocus(edit_login);
			}
			else if (!edit_password.isFocused())
			{
				GUI.getInstance().setFocus(edit_password);
			}
		}

		if (Input.KeyHit(com.badlogic.gdx.Input.Keys.ENTER) && !Input.isAltPressed() && !Input.isCtrlPressed() && !Input
				.isShiftPressed())
		{
			doLogin();
		}

		if (!_status.isEmpty())
		{
			lbl_status.caption = Lang.getTranslate("LoginScreen.status." + _status);
			UpdateStatusLbl();
		}
		else if (Net.getConnection() != null)
		{
			lbl_status.caption = Lang.getTranslate("LoginScreen.status.connect_error");
			UpdateStatusLbl();
		}
		else
		{
			lbl_status.caption = Lang.getTranslate("LoginScreen.status.disconnected");
			UpdateStatusLbl();
		}
	}

	private void UpdateStatusLbl()
	{
		lbl_status.UpdateSize();
		lbl_status.centerX();
	}

	public static void Show()
	{
		Main.freeScreen();
		Main.getInstance().setScreen(new Login());
	}
}
