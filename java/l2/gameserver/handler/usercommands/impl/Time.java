package l2.gameserver.handler.usercommands.impl;

import l2.gameserver.Config;
import l2.gameserver.GameTimeController;
import l2.gameserver.handler.usercommands.IUserCommandHandler;
import l2.gameserver.model.Player;
import l2.gameserver.network.l2.components.CustomMessage;
import l2.gameserver.network.l2.s2c.SystemMessage;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class Time implements IUserCommandHandler
{
	private static final int[] COMMAND_IDS = {77};
	private static final NumberFormat df = NumberFormat.getInstance(Locale.ENGLISH);
	private static final SimpleDateFormat sf = new SimpleDateFormat("H:mm");
	
	static
	{
		df.setMinimumIntegerDigits(2);
	}
	
	@Override
	public boolean useUserCommand(int id, Player activeChar)
	{
		if(COMMAND_IDS[0] != id)
		{
			return false;
		}
		int h = GameTimeController.getInstance().getGameHour();
		int m = GameTimeController.getInstance().getGameMin();
		SystemMessage sm = GameTimeController.getInstance().isNowNight() ? new SystemMessage(928) : new SystemMessage(927);
		sm.addString(df.format(h)).addString(df.format(m));
		activeChar.sendPacket(sm);
		if(Config.ALT_SHOW_SERVER_TIME)
		{
			activeChar.sendMessage(new CustomMessage("usercommandhandlers.Time.ServerTime", activeChar, sf.format(new Date(System.currentTimeMillis()))));
		}
		return true;
	}
	
	@Override
	public final int[] getUserCommandList()
	{
		return COMMAND_IDS;
	}
}