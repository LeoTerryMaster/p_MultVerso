package npc.model;

import bosses.FrintezzaManager;
import l2.gameserver.cache.Msg;
import l2.gameserver.model.CommandChannel;
import l2.gameserver.model.Party;
import l2.gameserver.model.Player;
import l2.gameserver.model.instances.NpcInstance;
import l2.gameserver.templates.npc.NpcTemplate;
import l2.gameserver.utils.ItemFunctions;

import java.util.List;

public final class FrintezzaGatekeeperInstance extends NpcInstance
{
	public FrintezzaGatekeeperInstance(int objectId, NpcTemplate template)
	{
		super(objectId, template);
	}
	
	@Override
	public void onBypassFeedback(Player player, String command)
	{
		block:
		{
			if(!canBypassCheck(player, this))
			{
				return;
			}
			if(command.equalsIgnoreCase("request_frintezza"))
			{
				try
				{
					if(!FrintezzaManager.getInstance().isInUse())
					{
						if(FrintezzaManager.getInstance().canEnter())
						{
							if(player.getParty() == null || player.getParty().getCommandChannel() == null)
							{
								player.sendMessage("Only a party room leader can request to enter.");
								return;
							}
							CommandChannel cc = player.getParty().getCommandChannel();
							List partyList = cc.getParties();
							if(partyList.size() < 4 || partyList.size() > 5)
							{
								player.sendMessage("Party room is too small or too big to enter.");
								return;
							}
							if(cc.getChannelLeader() != player)
							{
								player.sendMessage("Only a party room leader can request to enter.");
								return;
							}
							for(Party party : cc.getParties())
							{
								for(Player member : party.getPartyMembers())
								{
									if(member.getLevel() < 74)
									{
										player.sendMessage("Level of " + member.getName() + " to low to enter.");
										return;
									}
									if(getDistance(member) <= 300.0)
										continue;
									player.sendMessage(member.getName() + " to far.");
									return;
								}
							}
							if(ItemFunctions.removeItem(player, 8073, (long) 1, true) < 1)
							{
								player.sendPacket(Msg.YOU_DO_NOT_HAVE_ENOUGH_REQUIRED_ITEMS);
								return;
							}
							FrintezzaManager.getInstance().tryEnter(partyList);
						}
						else
						{
							player.sendMessage("Frintezza is still reborning. You cannot enter now.");
						}
						break block;
					}
					player.sendMessage("Frintezza is under attack. You cannot enter now.");
				}
				catch(Exception e)
				{
					e.printStackTrace();
				}
			}
			else
			{
				super.onBypassFeedback(player, command);
			}
		}
	}
}