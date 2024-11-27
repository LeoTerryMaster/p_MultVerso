package l2.gameserver.model.instances;

import gnu.trove.TIntHashSet;
import l2.gameserver.ai.CtrlIntention;
import l2.gameserver.model.Creature;
import l2.gameserver.model.Player;
import l2.gameserver.model.Skill;
import l2.gameserver.network.l2.s2c.MagicSkillUse;
import l2.gameserver.network.l2.s2c.MyTargetSelected;
import l2.gameserver.network.l2.s2c.ValidateLocation;
import l2.gameserver.scripts.Events;
import l2.gameserver.tables.SkillTable;
import l2.gameserver.templates.npc.NpcTemplate;

import java.util.ArrayList;
import java.util.StringTokenizer;

public class OlympiadBufferInstance extends NpcInstance
{
	private final TIntHashSet buffs = new TIntHashSet();
	
	public OlympiadBufferInstance(int objectId, NpcTemplate template)
	{
		super(objectId, template);
	}
	
	@Override
	public void onAction(Player player, boolean shift)
	{
		if(Events.onAction(player, this, shift))
		{
			player.sendActionFailed();
			return;
		}
		if(this != player.getTarget())
		{
			player.setTarget(this);
			MyTargetSelected my = new MyTargetSelected(getObjectId(), player.getLevel() - getLevel());
			player.sendPacket(my);
			player.sendPacket(new ValidateLocation(this));
		}
		else
		{
			MyTargetSelected my = new MyTargetSelected(getObjectId(), player.getLevel() - getLevel());
			player.sendPacket(my);
			if(!isInActingRange(player))
			{
				if(!player.getAI().isIntendingInteract(this))
				{
					player.getAI().setIntention(CtrlIntention.AI_INTENTION_INTERACT, this);
				}
			}
			else if(buffs.size() > 4)
			{
				showChatWindow(player, 1);
			}
			else
			{
				showChatWindow(player, 0);
			}
			player.sendActionFailed();
		}
	}
	
	@Override
	public void onBypassFeedback(Player player, String command)
	{
		if(!canBypassCheck(player, this))
		{
			return;
		}
		if(buffs.size() > 4)
		{
			showChatWindow(player, 1);
		}
		if(command.startsWith("Buff"))
		{
			StringTokenizer st = new StringTokenizer(command, " ");
			st.nextToken();
			int id = Integer.parseInt(st.nextToken());
			int lvl = Integer.parseInt(st.nextToken());
			Skill skill = SkillTable.getInstance().getInfo(id, lvl);
			ArrayList<Creature> target = new ArrayList<>();
			target.add(player);
			broadcastPacket(new MagicSkillUse(this, player, id, lvl, 0, 0));
			callSkill(skill, target, true);
			buffs.add(id);
			if(buffs.size() > 4)
			{
				showChatWindow(player, 1);
			}
			else
			{
				showChatWindow(player, 0);
			}
		}
		else
		{
			showChatWindow(player, 0);
		}
	}
	
	@Override
	public String getHtmlPath(int npcId, int val, Player player)
	{
		String pom = val == 0 ? "buffer" : "buffer-" + val;
		return "oly/" + pom + ".htm";
	}
}