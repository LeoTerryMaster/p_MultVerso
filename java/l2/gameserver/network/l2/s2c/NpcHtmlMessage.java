package l2.gameserver.network.l2.s2c;

import l2.gameserver.data.htm.HtmCache;
import l2.gameserver.model.Player;
import l2.gameserver.model.entity.SevenSignsFestival.SevenSignsFestival;
import l2.gameserver.model.instances.NpcInstance;
import l2.gameserver.network.l2.GameClient;
import l2.gameserver.network.l2.components.NpcString;
import l2.gameserver.scripts.Functions;
import l2.gameserver.scripts.Scripts;
import l2.gameserver.utils.HtmlUtils;
import l2.gameserver.utils.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class NpcHtmlMessage extends L2GameServerPacket
{
	protected static final Logger _log = LoggerFactory.getLogger(NpcHtmlMessage.class);
	protected static final Pattern objectId = Pattern.compile("%objectId%");
	protected static final Pattern playername = Pattern.compile("%playername%");
	protected int _npcObjId;
	protected String _html;
	protected String _file;
	protected List<String> _replaces = new ArrayList<>();
	protected boolean have_appends;
	
	public NpcHtmlMessage(Player player, int npcId, String filename, int val)
	{
		List<Scripts.ScriptClassAndMethod> appends = Scripts.dialogAppends.get(npcId);
		if(appends != null && appends.size() > 0)
		{
			have_appends = true;
			if("npcdefault.htm".equalsIgnoreCase(filename))
			{
				setHtml("");
			}
			else
			{
				setFile(filename);
			}
			String replaces = "";
			Object[] script_args = {new Integer(val)};
			for(Scripts.ScriptClassAndMethod append : appends)
			{
				Object obj = Scripts.getInstance().callScripts(player, append.className, append.methodName, script_args);
				if(obj == null)
					continue;
				replaces = replaces + obj;
			}
			if(!replaces.equals(""))
			{
				replace("</body>", "\n" + Strings.bbParse(replaces) + "</body>");
			}
		}
		else
		{
			setFile(filename);
		}
	}
	
	public NpcHtmlMessage(Player player, NpcInstance npc, String filename, int val)
	{
		this(player, npc.getNpcId(), filename, val);
		_npcObjId = npc.getObjectId();
		player.setLastNpc(npc);
		replace("%npcId%", String.valueOf(npc.getNpcId()));
		replace("%npcname%", npc.getName());
		replace("%festivalMins%", SevenSignsFestival.getInstance().getTimeToNextFestivalStr());
	}
	
	public NpcHtmlMessage(Player player, NpcInstance npc)
	{
		if(npc == null)
		{
			_npcObjId = 5;
			player.setLastNpc(null);
		}
		else
		{
			_npcObjId = npc.getObjectId();
			player.setLastNpc(npc);
		}
	}
	
	public NpcHtmlMessage(int npcObjId)
	{
		_npcObjId = npcObjId;
	}
	
	public final NpcHtmlMessage setHtml(String text)
	{
		if(!text.contains("<html>"))
		{
			text = "<html><body>" + text + "</body></html>";
		}
		_html = text;
		return this;
	}
	
	public final NpcHtmlMessage setFile(String file)
	{
		_file = file;
		if(_file.startsWith("data/html/"))
		{
			_log.info("NpcHtmlMessage: need fix : " + file, new Exception());
			_file = _file.replace("data/html/", "");
		}
		return this;
	}
	
	public NpcHtmlMessage replace(String pattern, String value)
	{
		if(pattern == null || value == null)
		{
			return this;
		}
		_replaces.add(pattern);
		_replaces.add(value);
		return this;
	}
	
	public NpcHtmlMessage replaceNpcString(String pattern, NpcString npcString, Object... arg)
	{
		if(pattern == null)
		{
			return this;
		}
		if(npcString.getSize() != arg.length)
		{
			throw new IllegalArgumentException("Not valid size of parameters: " + npcString);
		}
		_replaces.add(pattern);
		_replaces.add(HtmlUtils.htmlNpcString(npcString, arg));
		return this;
	}
	
	public void processHtml(GameClient client)
	{
		Player player = client.getActiveChar();
		if(_file != null)
		{
			if(player != null && player.isGM())
			{
				Functions.sendDebugMessage(player, "HTML: " + _file);
			}
			String content = HtmCache.getInstance().getNotNull(_file, player);
			String content2 = HtmCache.getInstance().getNullable(_file, player);
			if(content2 == null)
			{
				setHtml(have_appends && _file.endsWith(".htm") ? "" : content);
			}
			else
			{
				setHtml(content);
			}
		}
		for(int i = 0;i < _replaces.size();i += 2)
		{
			_html = _html.replace(_replaces.get(i), _replaces.get(i + 1));
		}
		if(_html == null)
		{
			return;
		}
		Matcher m = objectId.matcher(_html);
		if(m != null)
		{
			_html = m.replaceAll(String.valueOf(_npcObjId));
		}
		if(player != null)
		{
			_html = playername.matcher(_html).replaceAll(player.getName());
		}
		client.cleanBypasses(false);
		_html = client.encodeBypasses(_html, false);
	}
	
	@Override
	protected void writeImpl()
	{
		if(_html != null)
		{
			writeC(15);
			writeD(_npcObjId);
			writeS(_html);
			writeD(0);
		}
	}
}