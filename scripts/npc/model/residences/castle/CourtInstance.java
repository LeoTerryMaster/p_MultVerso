package npc.model.residences.castle;

import l2.gameserver.model.Player;
import l2.gameserver.model.instances.NpcInstance;
import l2.gameserver.network.l2.components.SystemMsg;
import l2.gameserver.network.l2.s2c.NpcHtmlMessage;
import l2.gameserver.skills.skillclasses.Call;
import l2.gameserver.templates.npc.NpcTemplate;
import l2.gameserver.utils.Location;

public class CourtInstance extends NpcInstance
{
	protected static final int COND_ALL_FALSE = 0;
	protected static final int COND_BUSY_BECAUSE_OF_SIEGE = 1;
	protected static final int COND_OWNER = 2;
	
	public CourtInstance(int objectId, NpcTemplate template)
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
		int condition = validateCondition(player);
		if(condition <= 0)
		{
			return;
		}
		if(condition == 1)
		{
			return;
		}
		if((player.getClanPrivileges() & 262144) != 262144)
		{
			player.sendPacket(SystemMsg.YOU_ARE_NOT_AUTHORIZED_TO_DO_THAT);
			return;
		}
		if(condition == 2)
		{
			if(command.startsWith("Chat"))
			{
				int val = 0;
				try
				{
					val = Integer.parseInt(command.substring(5));
				}
				catch(IndexOutOfBoundsException e)
				{
				}
				catch(NumberFormatException e)
				{
					
				}
				showChatWindow(player, val);
				return;
			}
			if(command.startsWith("gotoleader"))
			{
				if(player.getClan() != null)
				{
					Player clanLeader = player.getClan().getLeader().getPlayer();
					if(clanLeader == null)
					{
						return;
					}
					if(clanLeader.getEffectList().getEffectsBySkillId(3632) != null)
					{
						if(Call.canSummonHere(clanLeader) != null)
						{
							return;
						}
						if(Call.canBeSummoned(player) == null)
						{
							player.teleToLocation(Location.findAroundPosition(clanLeader, 100));
						}
						return;
					}
					showChatWindow(player, "castle/CourtMagician/CourtMagician-nogate.htm");
				}
			}
			else
			{
				super.onBypassFeedback(player, command);
			}
		}
	}
	
	@Override
	public void showChatWindow(Player player, int val, Object... arg)
	{
		player.sendActionFailed();
		String filename = "castle/CourtMagician/CourtMagician-no.htm";
		int condition = validateCondition(player);
		if(condition > 0)
		{
			if(condition == 1)
			{
				filename = "castle/CourtMagician/CourtMagician-busy.htm";
			}
			else if(condition == 2)
			{
				filename = val == 0 ? "castle/CourtMagician/CourtMagician.htm" : "castle/CourtMagician/CourtMagician-" + val + ".htm";
			}
		}
		NpcHtmlMessage html = new NpcHtmlMessage(player, this);
		html.setFile(filename);
		html.replace("%objectId%", String.valueOf(getObjectId()));
		html.replace("%npcname%", getName());
		player.sendPacket(html);
	}
	
	protected int validateCondition(Player player)
	{
		if(player.isGM())
		{
			return 2;
		}
		if(getCastle() != null && getCastle().getId() > 0 && player.getClan() != null)
		{
			if(getCastle().getSiegeEvent().isInProgress())
			{
				return 1;
			}
			if(getCastle().getOwnerId() == player.getClanId())
			{
				return 2;
			}
		}
		return 0;
	}
}