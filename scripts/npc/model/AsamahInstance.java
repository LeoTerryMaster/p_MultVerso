package npc.model;

import l2.gameserver.model.Player;
import l2.gameserver.model.instances.NpcInstance;
import l2.gameserver.model.quest.QuestState;
import l2.gameserver.scripts.Functions;
import l2.gameserver.templates.npc.NpcTemplate;
import quests._111_ElrokianHuntersProof;

public class AsamahInstance extends NpcInstance
{
	private static final int ElrokianTrap = 8763;
	private static final int TrapStone = 8764;
	
	public AsamahInstance(int objectId, NpcTemplate template)
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
		if(command.equals("buyTrap"))
		{
			String htmltext;
			QuestState ElrokianHuntersProof = player.getQuestState(_111_ElrokianHuntersProof.class);
			if(player.getLevel() >= 75 && ElrokianHuntersProof != null && ElrokianHuntersProof.isCompleted() && Functions.getItemCount(player, 57) > 1000000)
			{
				if(Functions.getItemCount(player, 8763) > 0)
				{
					htmltext = "" + getNpcId() + "-alreadyhave.htm";
				}
				else
				{
					Functions.removeItem(player, 57, (long) 1000000);
					Functions.addItem(player, 8763, (long) 1);
					htmltext = "" + getNpcId() + "-given.htm";
				}
			}
			else
			{
				htmltext = "" + getNpcId() + "-cant.htm";
			}
			showChatWindow(player, "default/" + htmltext);
		}
		else if(command.equals("buyStones"))
		{
			String htmltext;
			QuestState ElrokianHuntersProof = player.getQuestState(_111_ElrokianHuntersProof.class);
			if(player.getLevel() >= 75 && ElrokianHuntersProof != null && ElrokianHuntersProof.isCompleted() && Functions.getItemCount(player, 57) > 1000000)
			{
				Functions.removeItem(player, 57, (long) 1000000);
				Functions.addItem(player, 8764, (long) 100);
				htmltext = "" + getNpcId() + "-given.htm";
			}
			else
			{
				htmltext = "" + getNpcId() + "-cant.htm";
			}
			showChatWindow(player, "default/" + htmltext);
		}
		else
		{
			super.onBypassFeedback(player, command);
		}
	}
}