package l2.gameserver.handler.admincommands.impl;

import l2.gameserver.handler.admincommands.IAdminCommandHandler;
import l2.gameserver.model.GameObjectsStorage;
import l2.gameserver.model.Player;
import l2.gameserver.model.instances.NpcInstance;
import l2.gameserver.network.l2.s2c.SystemMessage;

import java.util.ArrayList;
import java.util.List;

public class AdminMammon implements IAdminCommandHandler
{
	List<Integer> npcIds = new ArrayList<>();
	
	@Override
	public boolean useAdminCommand(Enum comm, String[] wordList, String fullString, Player activeChar)
	{
		Commands command = (Commands) comm;
		npcIds.clear();
		if(!activeChar.getPlayerAccess().Menu)
		{
			return false;
		}
		if(fullString.startsWith("admin_find_mammon"))
		{
			npcIds.add(31113);
			npcIds.add(31126);
			npcIds.add(31092);
			int teleportIndex = -1;
			try
			{
				if(fullString.length() > 16)
				{
					teleportIndex = Integer.parseInt(fullString.substring(18));
				}
			}
			catch(Exception e)
			{
				
			}
			findAdminNPCs(activeChar, npcIds, teleportIndex, -1);
		}
		else if(fullString.equals("admin_show_mammon"))
		{
			npcIds.add(31113);
			npcIds.add(31126);
			findAdminNPCs(activeChar, npcIds, -1, 1);
		}
		else if(fullString.equals("admin_hide_mammon"))
		{
			npcIds.add(31113);
			npcIds.add(31126);
			findAdminNPCs(activeChar, npcIds, -1, 0);
		}
		else if(fullString.startsWith("admin_list_spawns"))
		{
			int npcId = 0;
			try
			{
				npcId = Integer.parseInt(fullString.substring(18).trim());
			}
			catch(Exception e)
			{
				activeChar.sendMessage("Command format is //list_spawns <NPC_ID>");
			}
			npcIds.add(npcId);
			findAdminNPCs(activeChar, npcIds, -1, -1);
		}
		else if(fullString.startsWith("admin_msg"))
		{
			activeChar.sendPacket(new SystemMessage(Integer.parseInt(fullString.substring(10).trim())));
		}
		return true;
	}
	
	@Override
	public Enum[] getAdminCommandEnum()
	{
		return Commands.values();
	}
	
	public void findAdminNPCs(Player activeChar, List<Integer> npcIdList, int teleportIndex, int makeVisible)
	{
		int index = 0;
		for(NpcInstance npcInst : GameObjectsStorage.getAllNpcsForIterate())
		{
			int npcId = npcInst.getNpcId();
			if(!npcIdList.contains(npcId))
				continue;
			if(makeVisible == 1)
			{
				npcInst.spawnMe();
			}
			else if(makeVisible == 0)
			{
				npcInst.decayMe();
			}
			if(!npcInst.isVisible())
				continue;
			++index;
			if(teleportIndex > -1)
			{
				if(teleportIndex != index)
					continue;
				activeChar.teleToLocation(npcInst.getLoc());
				continue;
			}
			activeChar.sendMessage("" + index + " - " + npcInst.getName() + " (" + npcInst.getObjectId() + "): " + npcInst.getX() + " " + npcInst.getY() + " " + npcInst.getZ());
		}
	}
	
	private enum Commands
	{
		admin_find_mammon,
		admin_show_mammon,
		admin_hide_mammon,
		admin_list_spawns;
	}
}