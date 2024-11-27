package npc.model.residences.castle;

import l2.gameserver.ai.CtrlIntention;
import l2.gameserver.instancemanager.CastleManorManager;
import l2.gameserver.model.Player;
import l2.gameserver.model.instances.NpcInstance;
import l2.gameserver.network.l2.s2c.MyTargetSelected;
import l2.gameserver.network.l2.s2c.NpcHtmlMessage;
import l2.gameserver.network.l2.s2c.ValidateLocation;
import l2.gameserver.templates.npc.NpcTemplate;

public class BlacksmithInstance extends NpcInstance
{
	protected static final int COND_ALL_FALSE = 0;
	protected static final int COND_BUSY_BECAUSE_OF_SIEGE = 1;
	protected static final int COND_OWNER = 2;
	
	public BlacksmithInstance(int objectId, NpcTemplate template)
	{
		super(objectId, template);
	}
	
	@Override
	public void onAction(Player player, boolean shift)
	{
		if(this != player.getTarget())
		{
			player.setTarget(this);
			player.sendPacket(new MyTargetSelected(getObjectId(), player.getLevel() - getLevel()));
			player.sendPacket(new ValidateLocation(this));
		}
		else
		{
			player.sendPacket(new MyTargetSelected(getObjectId(), player.getLevel() - getLevel()));
			if(!isInActingRange(player))
			{
				player.getAI().setIntention(CtrlIntention.AI_INTENTION_INTERACT, this);
				player.sendActionFailed();
			}
			else
			{
				if(CastleManorManager.getInstance().isDisabled())
				{
					NpcHtmlMessage html = new NpcHtmlMessage(player, this);
					html.setFile("npcdefault.htm");
					player.sendPacket(html);
				}
				else
				{
					showMessageWindow(player, 0);
				}
				player.sendActionFailed();
			}
		}
	}
	
	@Override
	public void onBypassFeedback(Player player, String command)
	{
		if(!canBypassCheck(player, this))
		{
			return;
		}
		if(CastleManorManager.getInstance().isDisabled())
		{
			NpcHtmlMessage html = new NpcHtmlMessage(player, this);
			html.setFile("npcdefault.htm");
			player.sendPacket(html);
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
				showMessageWindow(player, val);
			}
			else
			{
				super.onBypassFeedback(player, command);
			}
		}
	}
	
	private void showMessageWindow(Player player, int val)
	{
		player.sendActionFailed();
		String filename = "castle/blacksmith/castleblacksmith-no.htm";
		int condition = validateCondition(player);
		if(condition > 0)
		{
			if(condition == 1)
			{
				filename = "castle/blacksmith/castleblacksmith-busy.htm";
			}
			else if(condition == 2)
			{
				filename = val == 0 ? "castle/blacksmith/castleblacksmith.htm" : "castle/blacksmith/castleblacksmith-" + val + ".htm";
			}
		}
		NpcHtmlMessage html = new NpcHtmlMessage(player, this);
		html.setFile(filename);
		html.replace("%castleid%", Integer.toString(getCastle().getId()));
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
			if(getCastle().getOwnerId() == player.getClanId() && (player.getClanPrivileges() & 65536) == 65536)
			{
				return 2;
			}
		}
		return 0;
	}
}