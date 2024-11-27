package npc.model;

import l2.gameserver.model.Player;
import l2.gameserver.model.instances.NpcInstance;
import l2.gameserver.network.l2.components.SystemMsg;
import l2.gameserver.tables.SkillTable;
import l2.gameserver.templates.npc.NpcTemplate;

public class BlackJudeInstance extends NpcInstance
{
	public BlackJudeInstance(int objectId, NpcTemplate template)
	{
		super(objectId, template);
	}
	
	@Override
	public void onBypassFeedback(Player player, String command)
	{
		if(!canBypassCheck(player, this))
		{
			return;
		}
		if(command.equals("tryRemovePenalty"))
		{
			if(player.getDeathPenalty().getLevel() > 0)
			{
				showChatWindow(player, 2, "%price%", getPrice(player));
			}
			else
			{
				showChatWindow(player, 1);
			}
		}
		else if(command.equals("removePenalty"))
		{
			if(player.getDeathPenalty().getLevel() > 0)
			{
				if(player.getAdena() >= (long) getPrice(player))
				{
					player.reduceAdena((long) getPrice(player), true);
					doCast(SkillTable.getInstance().getInfo(5077, 1), player, false);
				}
				else
				{
					player.sendPacket(SystemMsg.YOU_DO_NOT_HAVE_ENOUGH_ADENA);
				}
			}
			else
			{
				showChatWindow(player, 1);
			}
		}
		else
		{
			super.onBypassFeedback(player, command);
		}
	}
	
	public int getPrice(Player player)
	{
		int playerLvl = player.getLevel();
		if(playerLvl <= 19)
		{
			return 3600;
		}
		if(playerLvl >= 20 && playerLvl <= 39)
		{
			return 16400;
		}
		if(playerLvl >= 40 && playerLvl <= 51)
		{
			return 36200;
		}
		if(playerLvl >= 52 && playerLvl <= 60)
		{
			return 50400;
		}
		if(playerLvl >= 61 && playerLvl <= 75)
		{
			return 78200;
		}
		return 102800;
	}
}