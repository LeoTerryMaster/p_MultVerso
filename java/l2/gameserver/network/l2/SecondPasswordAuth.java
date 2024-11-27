package l2.gameserver.network.l2;

import l2.commons.dbutils.DbUtils;
import l2.gameserver.Config;
import l2.gameserver.ThreadPoolManager;
import l2.gameserver.data.StringHolder;
import l2.gameserver.database.DatabaseFactory;
import l2.gameserver.network.l2.components.SystemMsg;
import l2.gameserver.network.l2.s2c.ConfirmDlg;
import l2.gameserver.network.l2.s2c.NpcHtmlMessage;
import l2.gameserver.utils.Language;
import l2.gameserver.utils.Util;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Random;
import java.util.Stack;

public class SecondPasswordAuth
{
	private static final Logger LOG = LoggerFactory.getLogger(SecondPasswordAuth.class);
	private final String _login;
	private String _secondPassword;
	private int _tryLine;
	private long _blockEndTime;
	private SecondPasswordAuthUI _ui;
	
	public SecondPasswordAuth(String login)
	{
		_login = login;
		_secondPassword = null;
	}
	
	private String getSecondPassword()
	{
		if(_secondPassword != null)
		{
			return _secondPassword;
		}
		Connection conn = null;
		PreparedStatement pstmt = null;
		ResultSet rset = null;
		try
		{
			conn = DatabaseFactory.getInstance().getConnection();
			pstmt = conn.prepareStatement("SELECT `password`, `tryLine`, `blockEndTime` FROM `second_auth` WHERE `login` = ?");
			pstmt.setString(1, _login);
			rset = pstmt.executeQuery();
			if(rset.next())
			{
				_secondPassword = rset.getString("password");
				_tryLine = Math.min(Config.SECOND_AUTH_MAX_TRYS, rset.getInt("tryLine"));
				_blockEndTime = rset.getLong("blockEndTime");
			}
		}
		catch(SQLException e)
		{
			LOG.warn("Database error on retreiving second password for login '" + _login + "' :", e);
		}
		finally
		{
			DbUtils.closeQuietly(conn, pstmt, rset);
		}
		return _secondPassword;
	}
	
	private void store()
	{
		if(_secondPassword == null)
		{
			return;
		}
		Connection conn = null;
		PreparedStatement pstmt = null;
		try
		{
			conn = DatabaseFactory.getInstance().getConnection();
			pstmt = conn.prepareStatement("REPLACE INTO `second_auth`(`login`, `password`, `tryLine`, `blockEndTime`) VALUES (?, ?, ?, ?)");
			pstmt.setString(1, _login);
			pstmt.setString(2, _secondPassword);
			pstmt.setInt(3, getTrysCount());
			pstmt.setLong(4, _blockEndTime);
			pstmt.executeUpdate();
		}
		catch(SQLException e)
		{
			LOG.warn("Database error on storing second password for login '" + _login + "' :", e);
		}
		finally
		{
			DbUtils.closeQuietly(conn, pstmt);
		}
	}
	
	public boolean isSecondPasswordSet()
	{
		return getSecondPassword() != null;
	}
	
	public boolean isBlocked()
	{
		if(_blockEndTime == 0)
		{
			return false;
		}
		if(_blockEndTime * 1000 < System.currentTimeMillis())
		{
			_blockEndTime = 0;
			_tryLine = 0;
			store();
			return false;
		}
		return true;
	}
	
	public int getBlockTimeLeft()
	{
		return (int) Math.max(0, _blockEndTime - System.currentTimeMillis() / 1000);
	}
	
	public int getTrysCount()
	{
		return Math.min(Config.SECOND_AUTH_MAX_TRYS, _tryLine);
	}
	
	public boolean isValidSecondPassword(String checkSecondPassword)
	{
		if(checkSecondPassword == null && getSecondPassword() == null)
		{
			return true;
		}
		if(checkSecondPassword.equalsIgnoreCase(getSecondPassword()))
		{
			_blockEndTime = 0;
			_tryLine = 0;
			store();
			return true;
		}
		++_tryLine;
		if(_tryLine >= Config.SECOND_AUTH_MAX_TRYS)
		{
			_blockEndTime = System.currentTimeMillis() / 1000 + Config.SECOND_AUTH_BLOCK_TIME;
			_tryLine = Config.SECOND_AUTH_MAX_TRYS;
		}
		store();
		return false;
	}
	
	public boolean changePassword(String oldSecondPassword, String newSecondPassword)
	{
		if(!isValidSecondPassword(oldSecondPassword))
		{
			return false;
		}
		_secondPassword = newSecondPassword;
		store();
		return true;
	}
	
	public SecondPasswordAuthUI getUI()
	{
		return _ui;
	}
	
	public void setUI(SecondPasswordAuthUI ui)
	{
		_ui = ui;
	}
	
	public static class SecondPasswordAuthUI
	{
		private static final Language SPA_UI_LANG;
		private static final Random RND;
		
		static
		{
			RND = new Random();
			Language lang = Language.ENGLISH;
			for(Language lang2 : Language.VALUES)
			{
				if(!lang2.getShortName().equals(Config.DEFAULT_LANG))
					continue;
				lang = lang2;
			}
			SPA_UI_LANG = lang;
		}
		
		private final ArrayList<Integer> _numpad = new ArrayList<>(Arrays.asList(0, 1, 2, 3, 4, 5, 6, 7, 8, 9));
		private SecondPasswordAuthUIType _type;
		private SPAUIPINInputData[] _inputs;
		private SPAUIPINInputData _inputFocus;
		private SecondPasswordAuthUIResult _result;
		private Runnable _runOnVerify;
		
		public SecondPasswordAuthUI(SecondPasswordAuthUIType type)
		{
			Collections.shuffle(_numpad, RND);
			_type = type;
			switch(_type)
			{
				case CREATE:
				{
					_inputs = new SPAUIPINInputData[2];
					_inputs[0] = new SPAUIPINInputData(StringHolder.getInstance().getNotNull(SPA_UI_LANG, "l2.gameserver.network.l2.SecondPasswordAuth.CREATE.PIN"), 0);
					_inputs[1] = new SPAUIPINInputData(StringHolder.getInstance().getNotNull(SPA_UI_LANG, "l2.gameserver.network.l2.SecondPasswordAuth.CREATE.PINConfirm"), 1);
					_inputFocus = _inputs[0];
					break;
				}
				case VERIFY:
				{
					_inputs = new SPAUIPINInputData[1];
					_inputs[0] = new SPAUIPINInputData(StringHolder.getInstance().getNotNull(SPA_UI_LANG, "l2.gameserver.network.l2.SecondPasswordAuth.VERIFY.PIN"), 0);
					_inputFocus = _inputs[0];
					break;
				}
				case CHANGE:
				{
					_inputs = new SPAUIPINInputData[3];
					_inputs[0] = new SPAUIPINInputData(StringHolder.getInstance().getNotNull(SPA_UI_LANG, "l2.gameserver.network.l2.SecondPasswordAuth.CHANGE.PINOld"), 0);
					_inputs[1] = new SPAUIPINInputData(StringHolder.getInstance().getNotNull(SPA_UI_LANG, "l2.gameserver.network.l2.SecondPasswordAuth.CHANGE.PINNew"), 1);
					_inputs[2] = new SPAUIPINInputData(StringHolder.getInstance().getNotNull(SPA_UI_LANG, "l2.gameserver.network.l2.SecondPasswordAuth.CHANGE.PINNewConfirm"), 2);
					_inputFocus = _inputs[0];
				}
			}
			_result = null;
		}
		
		public void setRunOnVerify(Runnable runOnVerify)
		{
			_runOnVerify = runOnVerify;
		}
		
		private void handleArg(GameClient client, String args)
		{
			if(args.equals("cnl"))
			{
				return;
			}
			if(args.startsWith("af"))
			{
				int inputFieldidx = args.charAt(2) - 48;
				_inputFocus = _inputs[inputFieldidx];
			}
			else if(args.startsWith("np"))
			{
				if(_inputFocus != null)
				{
					if(args.equals("npc"))
					{
						_inputFocus.clear();
					}
					else if(args.equals("npb"))
					{
						_inputFocus.back();
					}
					else
					{
						int digit = args.charAt(2) - 48;
						if(digit >= 0 && digit <= 9 && _inputFocus.getLen() < 8)
						{
							_inputFocus.add(digit);
						}
					}
				}
			}
			else
			{
				if(args.equals("hlp"))
				{
					NpcHtmlMessage html = new NpcHtmlMessage(5);
					html.setFile("spahelp.htm");
					client.sendPacket(html);
					return;
				}
				if(args.equals("hlb"))
				{
					client.getSecondPasswordAuth().getUI().handle(client, "");
					return;
				}
				if(args.equals("cgh"))
				{
					SecondPasswordAuthUI changeUI = new SecondPasswordAuthUI(SecondPasswordAuthUIType.CHANGE);
					changeUI.setRunOnVerify(_runOnVerify);
					client.getSecondPasswordAuth().setUI(changeUI);
					changeUI.handle(client, "");
					return;
				}
				if(args.equals("okk"))
				{
					SecondPasswordAuth spa = client.getSecondPasswordAuth();
					if(spa == null)
					{
						return;
					}
					switch(_type)
					{
						case CREATE:
						{
							if(_inputs[0].getLen() < 6 || _inputs[1].getLen() < 6)
							{
								_result = SecondPasswordAuthUIResult.TOO_SHORT;
								break;
							}
							if(!_inputs[0].toString().equals(_inputs[1].toString()))
							{
								_result = SecondPasswordAuthUIResult.NOT_MATCH;
								break;
							}
							if(!_inputs[0].isStrongPin())
							{
								_result = SecondPasswordAuthUIResult.TOO_SIMPLE;
								break;
							}
							if(spa.isSecondPasswordSet())
							{
								_result = SecondPasswordAuthUIResult.ERROR;
								break;
							}
							String pin = _inputs[0].toString();
							spa.changePassword(null, pin);
							SecondPasswordAuthUI verifyUI = new SecondPasswordAuthUI(SecondPasswordAuthUIType.VERIFY);
							verifyUI.setRunOnVerify(_runOnVerify);
							client.getSecondPasswordAuth().setUI(verifyUI);
							verifyUI.handle(client, "");
							return;
						}
						case CHANGE:
						{
							if(!spa.isSecondPasswordSet())
							{
								_result = SecondPasswordAuthUIResult.ERROR;
								break;
							}
							if(_inputs[0].getLen() < 6 || _inputs[1].getLen() < 6 || _inputs[2].getLen() < 6)
							{
								_result = SecondPasswordAuthUIResult.TOO_SHORT;
								break;
							}
							String oldPin = _inputs[0].toString();
							if(!_inputs[1].toString().equals(_inputs[2].toString()))
							{
								_result = SecondPasswordAuthUIResult.NOT_MATCH;
								break;
							}
							if(!_inputs[1].isStrongPin())
							{
								_result = SecondPasswordAuthUIResult.TOO_SIMPLE;
								break;
							}
							String newPin = _inputs[1].toString();
							if(!spa.isBlocked() && spa.changePassword(oldPin, newPin))
							{
								SecondPasswordAuthUI verifyUI = new SecondPasswordAuthUI(SecondPasswordAuthUIType.VERIFY);
								verifyUI.setRunOnVerify(_runOnVerify);
								client.getSecondPasswordAuth().setUI(verifyUI);
								verifyUI.handle(client, "");
								return;
							}
							if(spa.isBlocked())
							{
								_result = SecondPasswordAuthUIResult.BLOCK_HOMEPAGE;
								String blockMsg = StringHolder.getInstance().getNotNull(SPA_UI_LANG, "l2.gameserver.network.l2.SecondPasswordAuth.Result.BLOCK_HOMEPAGE");
								blockMsg = blockMsg.replace("%tryCnt%", Integer.toString(spa.getTrysCount()));
								blockMsg = blockMsg.replace("%time%", Util.formatTime(spa.getBlockTimeLeft()));
								client.close(new ConfirmDlg(SystemMsg.S1, -1).addString(blockMsg));
							}
							else
							{
								_result = SecondPasswordAuthUIResult.FAIL_VERIFY;
								verifyFail(client);
							}
							return;
						}
						case VERIFY:
						{
							if(!spa.isSecondPasswordSet())
							{
								_result = SecondPasswordAuthUIResult.ERROR;
								break;
							}
							String pin = _inputs[0].toString();
							if(!spa.isBlocked() && spa.isValidSecondPassword(pin))
							{
								if(_runOnVerify != null)
								{
									client.setSecondPasswordAuthed(true);
									ThreadPoolManager.getInstance().execute(_runOnVerify);
								}
								return;
							}
							if(spa.isBlocked())
							{
								_result = SecondPasswordAuthUIResult.BLOCK_HOMEPAGE;
								String blockMsg = StringHolder.getInstance().getNotNull(SPA_UI_LANG, "l2.gameserver.network.l2.SecondPasswordAuth.Result.BLOCK_HOMEPAGE");
								blockMsg = blockMsg.replace("%tryCnt%", Integer.toString(spa.getTrysCount()));
								blockMsg = blockMsg.replace("%time%", Util.formatTime(spa.getBlockTimeLeft()));
								client.close(new ConfirmDlg(SystemMsg.S1, -1).addString(blockMsg));
							}
							else
							{
								_result = SecondPasswordAuthUIResult.FAIL_VERIFY;
								verifyFail(client);
							}
							return;
						}
					}
				}
			}
			NpcHtmlMessage html = new NpcHtmlMessage(5);
			html.setHtml(format());
			client.sendPacket(html);
		}
		
		public void handle(GameClient client, String args)
		{
			if(args != null)
			{
				try
				{
					handleArg(client, args);
				}
				catch(Exception e)
				{
					e.printStackTrace();
				}
			}
		}
		
		public boolean verify(GameClient client, Runnable runOnSuccess)
		{
			SecondPasswordAuth spa = client.getSecondPasswordAuth();
			if(spa == null)
			{
				return false;
			}
			setRunOnVerify(runOnSuccess);
			if(spa.isBlocked())
			{
				_result = SecondPasswordAuthUIResult.BLOCK_HOMEPAGE;
				String blockMsg = StringHolder.getInstance().getNotNull(SPA_UI_LANG, "l2.gameserver.network.l2.SecondPasswordAuth.Result.BLOCK_HOMEPAGE");
				blockMsg = blockMsg.replace("%tryCnt%", Integer.toString(spa.getTrysCount()));
				blockMsg = blockMsg.replace("%time%", Util.formatTime(spa.getBlockTimeLeft()));
				client.close(new ConfirmDlg(SystemMsg.S1, -1).addString(blockMsg));
				return false;
			}
			handle(client, "");
			return true;
		}
		
		private void verifyFail(GameClient client)
		{
			SecondPasswordAuth spa = client.getSecondPasswordAuth();
			if(spa == null)
			{
				return;
			}
			_result = SecondPasswordAuthUIResult.FAIL_VERIFY;
			String blockMsg = StringHolder.getInstance().getNotNull(SPA_UI_LANG, "l2.gameserver.network.l2.SecondPasswordAuth.Result.FAIL_VERIFY");
			blockMsg = blockMsg.replace("%tryCnt%", Integer.toString(Config.SECOND_AUTH_MAX_TRYS - spa.getTrysCount()));
			client.close(new ConfirmDlg(SystemMsg.S1, -1).addString(blockMsg));
		}
		
		private String getTitle()
		{
			return StringHolder.getInstance().getNotNull(SPA_UI_LANG, "l2.gameserver.network.l2.SecondPasswordAuth." + _type.name() + ".Title");
		}
		
		private String getFormDescription()
		{
			return StringHolder.getInstance().getNotNull(SPA_UI_LANG, "l2.gameserver.network.l2.SecondPasswordAuth." + _type.name() + ".Description");
		}
		
		private String getNote()
		{
			if(_result == null)
			{
				return StringHolder.getInstance().getNotNull(SPA_UI_LANG, "l2.gameserver.network.l2.SecondPasswordAuth.Note");
			}
			return StringHolder.getInstance().getNotNull(SPA_UI_LANG, "l2.gameserver.network.l2.SecondPasswordAuth.Result." + _result.name());
		}
		
		private String getInputDescription()
		{
			return "<font color=\"a2a0a2\">(Enter PIN - Mouse Click 6 to 8 digits)</font>";
		}
		
		private String format()
		{
			StringBuilder sb = new StringBuilder(8192);
			sb.append("<html>");
			sb.append("<head><title>").append(getTitle()).append("</title></head>");
			sb.append("<body><center>");
			sb.append("<table width=270 border=0 cellspacing=0 cellpadding=0>");
			sb.append("<br>");
			sb.append("<tr><td fixwidth=270 align=center>");
			formatFormContent(sb);
			sb.append("</td></tr>");
			sb.append("<tr><td fixwidth=270 align=center>");
			sb.append("<img src=\"L2UI.SquareBlank\" width=270 height=10>");
			formatNote(sb);
			sb.append("</td></tr>");
			sb.append("<tr><td align=center>");
			sb.append("<img src=\"L2UI.SquareBlank\" width=270 height=10>");
			formatButtons(sb);
			sb.append("</td></tr>");
			sb.append("</table>");
			sb.append("</center></body>");
			sb.append("</html> ");
			return sb.toString();
		}
		
		private void formatFormContent(StringBuilder sb)
		{
			sb.append("<table width=260 height=250 border=0 cellspacing=5 cellpadding=0 bgcolor=000000>");
			sb.append("<tr><td valign=TOP height=80>").append(getFormDescription()).append("</td></tr>");
			sb.append("<tr><td align=CENTER>").append(getInputDescription()).append("</td></tr>");
			sb.append("<tr><td valign=TOP>");
			formatInputs(sb);
			sb.append("<br>");
			sb.append("</td></tr>");
			sb.append("<tr><td valign=TOP align=center height=100>");
			sb.append("<img src=\"L2UI.SquareGray\" width=250 height=1><br>");
			formatNumPad(sb);
			sb.append("<br>");
			sb.append("</td></tr>");
			sb.append("</table>");
		}
		
		private void formatInputs(StringBuilder sb)
		{
			sb.append("<table width=250 height=60 border=0 cellspacing=5 cellpadding=0>");
			for(SPAUIPINInputData suid : _inputs)
			{
				suid.formatPINInput(sb, _inputFocus == suid);
			}
			sb.append("</table>");
		}
		
		private void formatNote(StringBuilder sb)
		{
			sb.append("<table height=20 border=0 cellspacing=0 cellpadding=0 bgcolor=\"000000\">").append("<tr><td align=center valign=center fixwidth=264>").append(getNote()).append("</td></tr></table>");
		}
		
		private void formatButtons(StringBuilder sb)
		{
			sb.append("<table width=260 border=0 cellspacing=0 cellpadding=0><tr>");
			sb.append("<td fixwidth=50>");
			if(_type.isCanChange())
			{
				sb.append("<button width=47 height=21 fore=\"L2UI_CH3.smallbutton1\" back=\"L2UI_CH3.smallbutton1_down\" value=\"").append(StringHolder.getInstance().getNotNull(SPA_UI_LANG, "l2.gameserver.network.l2.SecondPasswordAuth.Change")).append("\" action=\"bypass -h spa_cgh\">");
			}
			sb.append("</td>");
			sb.append("<td width=60>&nbsp;</td>");
			sb.append("<td>");
			sb.append("<button width=47 height=21 fore=\"L2UI_CH3.smallbutton1\" back=\"L2UI_CH3.smallbutton1_down\" value=\"").append(StringHolder.getInstance().getNotNull(SPA_UI_LANG, "l2.gameserver.network.l2.SecondPasswordAuth.Help")).append("\" action=\"bypass -h spa_hlp\">");
			sb.append("</td>");
			sb.append("<td>");
			sb.append("<button width=47 height=21 fore=\"L2UI_CH3.smallbutton1\" back=\"L2UI_CH3.smallbutton1_down\" value=\"").append(StringHolder.getInstance().getNotNull(SPA_UI_LANG, "l2.gameserver.network.l2.SecondPasswordAuth.OK")).append("\" action=\"bypass -h spa_okk\">");
			sb.append("</td>");
			sb.append("<td>");
			sb.append("<button width=47 height=21 fore=\"L2UI_CH3.smallbutton1\" back=\"L2UI_CH3.smallbutton1_down\" value=\"").append(StringHolder.getInstance().getNotNull(SPA_UI_LANG, "l2.gameserver.network.l2.SecondPasswordAuth.Cancel")).append("\" action=\"bypass -h spa_cnl\">");
			sb.append("</td>");
			sb.append("</tr></table>");
		}
		
		public void formatNumPad(StringBuilder sb)
		{
			sb.append("<table width=90 border=0 cellspacing=0 cellpadding=0>");
			int num;
			for(int i = 0;i < 3;++i)
			{
				sb.append("<tr>");
				for(int j = 0;j < 3;++j)
				{
					int idx = i * 3 + j;
					num = _numpad.get(idx);
					sb.append("<td>");
					sb.append("<button width=35 height=24 fore=\"L2UI_CH3.calculate2_").append(num).append("\" back=\"L2UI_CH3.calculate2_").append(num).append("_down\" value=\"\" action=\"bypass spa_np").append(num).append("\">");
					sb.append("</td>");
				}
				sb.append("</tr>");
			}
			sb.append("<tr><td>");
			num = _numpad.get(9);
			sb.append("<button width=35 height=24 fore=\"L2UI_CH3.calculate2_c\" back=\"L2UI_CH3.calculate2_c_down\" action=\"bypass spa_npc\">");
			sb.append("</td><td>");
			sb.append("<button width=35 height=24 fore=\"L2UI_CH3.calculate2_").append(num).append("\" back=\"L2UI_CH3.calculate2_").append(num).append("_down\" value=\"\" action=\"bypass spa_np").append(num).append("\">");
			sb.append("</td><td>");
			sb.append("<button width=35 height=24 fore=\"L2UI_CH3.calculate2_bs\" back=\"L2UI_CH3.calculate2_bs_down\" action=\"bypass spa_npb\">");
			sb.append("</td></tr>");
			sb.append("</table>");
		}
		
		public enum SecondPasswordAuthUIType
		{
			CREATE(false),
			VERIFY(true),
			CHANGE(false);
			
			private final boolean _canChange;
			
			SecondPasswordAuthUIType(boolean canChange)
			{
				_canChange = canChange;
			}
			
			public boolean isCanChange()
			{
				return _canChange;
			}
		}
		
		public enum SecondPasswordAuthUIResult
		{
			TOO_SHORT,
			NOT_MATCH,
			TOO_SIMPLE,
			FAIL_VERIFY,
			BLOCK_HOMEPAGE,
			ERROR;
			
			
		}
		
		private static class SPAUIPINInputData
		{
			private final Stack<Integer> _pin = new Stack();
			private final String _label;
			private final int _inputFieldIdx;
			
			public SPAUIPINInputData(String label, int inputFieldIdx)
			{
				_label = label;
				_inputFieldIdx = inputFieldIdx;
			}
			
			public String getLabel()
			{
				return _label;
			}
			
			public int getInputFieldIdx()
			{
				return _inputFieldIdx;
			}
			
			public void clear()
			{
				_pin.clear();
			}
			
			public void back()
			{
				if(!_pin.isEmpty())
				{
					_pin.pop();
				}
			}
			
			public void add(int digit)
			{
				_pin.add(digit);
			}
			
			public boolean isEmpty()
			{
				return _pin.isEmpty();
			}
			
			public boolean isStrongPin()
			{
				return !isEmpty();
			}
			
			public int getLen()
			{
				return _pin.size();
			}
			
			private void formatPINInputBox(StringBuilder sb, boolean isActive, int len, String link)
			{
				int dWidth = 8 * Math.min(8, len);
				int cWidth = isActive ? 1 : 0;
				String hTexture = isActive ? "L2UI_CH3.inputbox02_over" : "L2UI_CH3.M_inputbox02";
				String vTexture = isActive ? "L2UI_CH3.inputbox04_over" : "L2UI_CH3.M_inputbox04";
				String dTexture = isActive ? "L2UI_CH3.radar_tutorial1" : "L2UI_CH3.radar_tutorial2";
				sb.append("<table width=67 height=12 border=0 cellspacing=0 cellpadding=0>");
				sb.append("<tr><td>");
				sb.append("<img src=\"").append(hTexture).append("\" width=67 height=1>");
				sb.append("</td></tr>");
				sb.append("<tr><td>");
				sb.append("<table width=67 height=12 border=0 cellspacing=0 cellpadding=0><tr>");
				sb.append("<td><img src=\"").append(vTexture).append("\" width=1 height=12></td>");
				sb.append("<td>");
				sb.append("<img src=\"L2UI.SquareBlank\" width=65 height=4>");
				sb.append("<table border=0 cellspacing=0 cellpadding=0><tr>");
				if(dWidth > 0)
				{
					sb.append("<td fixwidth=").append(dWidth).append(">");
					sb.append("<button width=").append(dWidth).append(" height=8 ").append("fore=\"").append(dTexture).append("\" back=\"").append(dTexture).append("\" value=\" \"");
					if(link != null)
					{
						sb.append(" action=\"bypass spa_").append(link).append("\"");
					}
					sb.append(">");
					sb.append("</td>");
				}
				if(cWidth > 0)
				{
					sb.append("<td valign=TOP><img src=\"L2UI.SquareWhite\" width=1 height=8></td>");
				}
				int eWidth = 65 - (dWidth + cWidth);
				if(eWidth > 0)
				{
					sb.append("<td FIXWIDTH=").append(eWidth).append(">");
					sb.append("<button width=").append(eWidth).append(" height=8 ").append("fore=\"L2UI.SquareBlank\" back=\"L2UI.SquareBlank\" value=\" \"");
					if(link != null)
					{
						sb.append(" action=\"bypass spa_").append(link).append("\"");
					}
					sb.append(">");
					sb.append("</td>");
				}
				sb.append("</tr></table>");
				sb.append("</td>");
				sb.append("<td><img src=\"").append(vTexture).append("\" width=1 height=12></td>");
				sb.append("</tr></table>");
				sb.append("</td></tr>");
				sb.append("<tr><td>");
				sb.append("<img src=\"").append(hTexture).append("\" width=67 height=1>");
				sb.append("</td></tr>");
				sb.append("</table>");
			}
			
			public void formatPINInput(StringBuilder sb, boolean isActive)
			{
				sb.append("<tr>");
				sb.append("<td align=right valign=TOP fixwidth=100>");
				if(!isActive)
				{
					sb.append("<font color=\"a2a0a2\">").append(getLabel()).append("</font>");
				}
				else
				{
					sb.append(getLabel());
				}
				sb.append("</td>");
				sb.append("<td align=left>");
				formatPINInputBox(sb, isActive, getLen(), String.format("af%d", getInputFieldIdx()));
				sb.append("</td>");
				sb.append("</tr>");
			}
			
			@Override
			public String toString()
			{
				StringBuilder pinText = new StringBuilder(8);
				for(Integer digit : _pin)
				{
					pinText.append((char) (48 + Math.min(9, Math.max(digit, 0))));
				}
				return pinText.toString();
			}
		}
	}
}