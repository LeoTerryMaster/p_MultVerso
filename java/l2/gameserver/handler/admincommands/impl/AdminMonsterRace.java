package l2.gameserver.handler.admincommands.impl;

import l2.commons.threading.RunnableImpl;
import l2.gameserver.ThreadPoolManager;
import l2.gameserver.cache.Msg;
import l2.gameserver.handler.admincommands.IAdminCommandHandler;
import l2.gameserver.model.Player;
import l2.gameserver.model.entity.MonsterRace;
import l2.gameserver.model.instances.NpcInstance;
import l2.gameserver.network.l2.s2c.DeleteObject;
import l2.gameserver.network.l2.s2c.MonRaceInfo;
import l2.gameserver.network.l2.s2c.PlaySound;
import l2.gameserver.utils.Location;

public class AdminMonsterRace implements IAdminCommandHandler
{
	protected static int state = -1;
	
	@Override
	public boolean useAdminCommand(Enum comm, String[] wordList, String fullString, Player activeChar)
	{
		Commands command = (Commands) comm;
		if(fullString.equalsIgnoreCase("admin_mons"))
		{
			if(!activeChar.getPlayerAccess().MonsterRace)
			{
				return false;
			}
			handleSendPacket(activeChar);
		}
		return true;
	}
	
	@Override
	public Enum[] getAdminCommandEnum()
	{
		return Commands.values();
	}
	
	private void handleSendPacket(Player activeChar)
	{
		int[][] codes = {{-1, 0}, {0, 15322}, {13765, -1}, {-1, 0}};
		MonsterRace race = MonsterRace.getInstance();
		if(state == -1)
		{
			race.newRace();
			race.newSpeeds();
			activeChar.broadcastPacket(new MonRaceInfo(codes[state][0], codes[++state][1], race.getMonsters(), race.getSpeeds()));
		}
		else if(state == 0)
		{
			activeChar.sendPacket(Msg.THEYRE_OFF);
			activeChar.broadcastPacket(new PlaySound("S_Race"));
			activeChar.broadcastPacket(new PlaySound(PlaySound.Type.SOUND, "ItemSound2.race_start", 1, 121209259, new Location(12125, 182487, -3559)));
			activeChar.broadcastPacket(new MonRaceInfo(codes[state][0], codes[++state][1], race.getMonsters(), race.getSpeeds()));
			ThreadPoolManager.getInstance().schedule(new RunRace(codes, activeChar), 5000);
		}
	}
	
	private enum Commands
	{
		admin_mons;
	}
	
	class RunEnd extends RunnableImpl
	{
		private final Player activeChar;
		
		public RunEnd(Player activeChar)
		{
			this.activeChar = activeChar;
		}
		
		@Override
		public void runImpl() throws Exception
		{
			for(int i = 0;i < 8;++i)
			{
				NpcInstance obj = MonsterRace.getInstance().getMonsters()[i];
				activeChar.broadcastPacket(new DeleteObject(obj));
			}
			state = -1;
		}
	}
	
	class RunRace extends RunnableImpl
	{
		private final int[][] codes;
		private final Player activeChar;
		
		public RunRace(int[][] codes, Player activeChar)
		{
			this.codes = codes;
			this.activeChar = activeChar;
		}
		
		@Override
		public void runImpl() throws Exception
		{
			activeChar.broadcastPacket(new MonRaceInfo(codes[2][0], codes[2][1], MonsterRace.getInstance().getMonsters(), MonsterRace.getInstance().getSpeeds()));
			ThreadPoolManager.getInstance().schedule(new RunEnd(activeChar), 30000);
		}
	}
}