package handler.admincommands;

import l2.gameserver.data.xml.holder.ItemHolder;
import l2.gameserver.data.xml.holder.NpcHolder;
import l2.gameserver.model.GameObject;
import l2.gameserver.model.Player;
import l2.gameserver.model.Skill;
import l2.gameserver.model.instances.NpcInstance;
import l2.gameserver.model.items.ItemInstance;
import l2.gameserver.network.l2.components.SystemMsg;
import l2.gameserver.network.l2.s2c.SystemMessage2;
import l2.gameserver.tables.SkillTable;
import l2.gameserver.templates.item.ItemTemplate;
import l2.gameserver.templates.npc.NpcTemplate;
import l2.gameserver.utils.ItemFunctions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AdminClientSupport extends ScriptAdminCommand
{
	private static final Logger _log = LoggerFactory.getLogger(AdminClientSupport.class);
	
	@Override
	public boolean useAdminCommand(Enum comm, String[] wordList, String fullString, Player player)
	{
		Commands c = (Commands) comm;
		GameObject target = player.getTarget();
		switch(c)
		{
			case admin_setskill:
			{
				if(wordList.length != 3)
				{
					return false;
				}
				if(!player.getPlayerAccess().CanEditChar)
				{
					return false;
				}
				if(target == null || !target.isPlayer())
				{
					return false;
				}
				try
				{
					Skill skill = SkillTable.getInstance().getInfo(Integer.parseInt(wordList[1]), Integer.parseInt(wordList[2]));
					target.getPlayer().addSkill(skill, true);
					target.getPlayer().sendPacket(new SystemMessage2(SystemMsg.YOU_HAVE_EARNED_S1_SKILL).addSkillName(skill.getId(), skill.getLevel()));
					break;
				}
				catch(NumberFormatException e)
				{
					_log.info("AdminClientSupport:useAdminCommand(Enum,String[],String,L2Player): " + e, e);
					return false;
				}
			}
			case admin_summon:
			{
				if(wordList.length != 3)
				{
					return false;
				}
				if(!player.getPlayerAccess().CanEditChar)
				{
					return false;
				}
				try
				{
					int id = Integer.parseInt(wordList[1]);
					long count = Long.parseLong(wordList[2]);
					if(id >= 1000000)
					{
						if(target == null)
						{
							target = player;
						}
						NpcTemplate template = NpcHolder.getInstance().getTemplate(id - 1000000);
						int i = 0;
						while((long) i < count)
						{
							NpcInstance npc = template.getNewInstance();
							npc.setSpawnedLoc(target.getLoc());
							npc.setCurrentHpMp((double) npc.getMaxHp(), (double) npc.getMaxMp(), true);
							npc.spawnMe(npc.getSpawnedLoc());
							++i;
						}
					}
					else
					{
						if(target == null)
						{
							target = player;
						}
						if(!target.isPlayer())
						{
							return false;
						}
						ItemTemplate template = ItemHolder.getInstance().getTemplate(id);
						if(template == null)
						{
							return false;
						}
						if(template.isStackable())
						{
							ItemInstance item = ItemFunctions.createItem(id);
							item.setCount(count);
							target.getPlayer().getInventory().addItem(item);
							target.getPlayer().sendPacket(SystemMessage2.obtainItems(item));
							break;
						}
						int i = 0;
						while((long) i < count)
						{
							ItemInstance item = ItemFunctions.createItem(id);
							target.getPlayer().getInventory().addItem(item);
							target.getPlayer().sendPacket(SystemMessage2.obtainItems(item));
							++i;
						}
					}
					break;
				}
				catch(NumberFormatException e)
				{
					_log.info("AdminClientSupport:useAdminCommand(Enum,String[],String,L2Player): " + e, e);
					return false;
				}
			}
		}
		return true;
	}
	
	@Override
	public Enum[] getAdminCommandEnum()
	{
		return Commands.values();
	}
	
	public enum Commands
	{
		admin_setskill,
		admin_summon;
	}
}