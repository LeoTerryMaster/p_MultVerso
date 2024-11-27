package l2.gameserver.network.l2.s2c;

import l2.gameserver.model.Player;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.Charset;
import java.util.List;

public class ShowBoard extends L2GameServerPacket
{
	private static final Logger _log = LoggerFactory.getLogger(ShowBoard.class);
	private static final Charset BBS_CHARSET = Charset.forName("UTF-16LE");
	private final String _htmlCode;
	private String _id;
	private List<String> _arg;
	private String _addFav = "";
	
	private ShowBoard(String htmlCode, String id, Player player, boolean encodeBypasses)
	{
		if(htmlCode != null && htmlCode.length() > 8192)
		{
			_log.warn("Html '" + htmlCode + "' is too long! this will crash the client!");
			_htmlCode = "<html><body>Html was too long</body></html>";
			return;
		}
		_id = id;
		if(player.getSessionVar("add_fav") != null)
		{
			_addFav = "bypass _bbsaddfav_List";
		}
		_htmlCode = htmlCode != null ? encodeBypasses ? player.getNetConnection().encodeBypasses(htmlCode, true) : htmlCode : null;
	}
	
	public ShowBoard(String htmlCode, String id, Player player)
	{
		this(htmlCode, id, player, true);
	}
	
	public ShowBoard(List<String> arg)
	{
		_id = "1002";
		_htmlCode = null;
		_arg = arg;
	}
	
	public static void separateAndSend(String html, Player player)
	{
		html = player.getNetConnection().encodeBypasses(html, true);
		byte[] htmlBytes = html.getBytes(BBS_CHARSET);
		if(htmlBytes.length < 8180)
		{
			player.sendPacket(new ShowBoard(html, "101", player, false));
			player.sendPacket(new ShowBoard(null, "102", player, false));
			player.sendPacket(new ShowBoard(null, "103", player, false));
		}
		else if(htmlBytes.length < 16360)
		{
			player.sendPacket(new ShowBoard(new String(htmlBytes, 0, 8180, BBS_CHARSET), "101", player, false));
			player.sendPacket(new ShowBoard(new String(htmlBytes, 8180, htmlBytes.length - 8180, BBS_CHARSET), "102", player, false));
			player.sendPacket(new ShowBoard(null, "103", player, false));
		}
		else
		{
			player.sendPacket(new ShowBoard(new String(htmlBytes, 0, 8180, BBS_CHARSET), "101", player, false));
			player.sendPacket(new ShowBoard(new String(htmlBytes, 8180, htmlBytes.length - 8180, BBS_CHARSET), "102", player, false));
			player.sendPacket(new ShowBoard(new String(htmlBytes, 16360, htmlBytes.length - 16360, BBS_CHARSET), "103", player, false));
		}
	}
	
	@Override
	protected final void writeImpl()
	{
		writeC(110);
		writeC(1);
		writeS("bypass _bbshome");
		writeS("bypass _bbsgetfav");
		writeS("bypass _bbsloc");
		writeS("bypass _bbsclan");
		writeS("bypass _bbsmemo");
		writeS("bypass _maillist_0_1_0_");
		writeS("bypass _friendlist_0_");
		writeS(_addFav);
		String str = _id + "\b";
		if(!_id.equals("1002"))
		{
			if(_htmlCode != null)
			{
				str = str + _htmlCode;
			}
		}
		else
		{
			for(String arg : _arg)
			{
				str = str + arg + " \b";
			}
		}
		writeS(str);
	}
}