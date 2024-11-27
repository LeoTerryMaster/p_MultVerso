package l2.gameserver.handler.admincommands.impl;

import l2.gameserver.cache.Msg;
import l2.gameserver.data.xml.holder.SkillAcquireHolder;
import l2.gameserver.handler.admincommands.IAdminCommandHandler;
import l2.gameserver.model.Creature;
import l2.gameserver.model.Effect;
import l2.gameserver.model.GameObject;
import l2.gameserver.model.GameObjectsStorage;
import l2.gameserver.model.Playable;
import l2.gameserver.model.Player;
import l2.gameserver.model.Skill;
import l2.gameserver.model.SkillLearn;
import l2.gameserver.model.base.AcquireType;
import l2.gameserver.network.l2.s2c.ExEnchantSkillList;
import l2.gameserver.network.l2.s2c.NpcHtmlMessage;
import l2.gameserver.network.l2.s2c.SkillCoolTime;
import l2.gameserver.network.l2.s2c.SkillList;
import l2.gameserver.stats.Calculator;
import l2.gameserver.stats.Env;
import l2.gameserver.stats.funcs.Func;
import l2.gameserver.tables.SkillTable;
import l2.gameserver.utils.Log;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class AdminSkill implements IAdminCommandHandler
{
	private static Skill[] adminSkills;
	
	@Override
	public boolean useAdminCommand(Enum comm, String[] wordList, String fullString, Player activeChar)
	{
		Commands command = (Commands) comm;
		if(!activeChar.getPlayerAccess().CanEditChar)
		{
			return false;
		}
		switch(command)
		{
			case admin_show_skills:
			{
				showSkillsPage(activeChar);
				break;
			}
			case admin_show_effects:
			{
				showEffects(activeChar);
				break;
			}
			case admin_stop_effect:
			{
				stopEffect(activeChar, wordList);
				break;
			}
			case admin_remove_skills:
			{
				removeSkillsPage(activeChar);
				break;
			}
			case admin_skill_list:
			{
				activeChar.sendPacket(new NpcHtmlMessage(5).setFile("admin/skills.htm"));
				break;
			}
			case admin_skill_index:
			{
				if(wordList.length <= 1)
					break;
				activeChar.sendPacket(new NpcHtmlMessage(5).setFile("admin/skills/" + wordList[1] + ".htm"));
				break;
			}
			case admin_add_skill:
			{
				adminAddSkill(activeChar, wordList);
				break;
			}
			case admin_remove_skill:
			{
				adminRemoveSkill(activeChar, wordList);
				break;
			}
			case admin_get_skills:
			{
				adminGetSkills(activeChar);
				break;
			}
			case admin_reset_skills:
			{
				adminResetSkills(activeChar);
				break;
			}
			case admin_give_all_skills:
			{
				adminGiveAllSkills(activeChar);
				break;
			}
			case admin_debug_stats:
			{
				debug_stats(activeChar);
				break;
			}
			case admin_remove_cooldown:
			{
				Player target = wordList.length > 1 ? GameObjectsStorage.getPlayer(wordList[1]) : null;
				Player player = activeChar.getTarget() != null ? activeChar.getTarget().getPlayer() : target;
				if(player != null)
				{
					player.resetReuse();
					player.sendPacket(new SkillCoolTime(player));
					activeChar.sendMessage("Skills reuse delay reseted.");
					break;
				}
				activeChar.sendMessage("Usage: //remove_cooldown [<target>|player_name]");
				break;
			}
			case admin_buff:
			{
				for(int i = 7041;i <= 7064;++i)
				{
					activeChar.addSkill(SkillTable.getInstance().getInfo(i, 1));
				}
				activeChar.sendPacket(new SkillList(activeChar));
				break;
			}
			case admin_skill_ench:
			case admin_skill_enchant:
			{
				activeChar.sendPacket(ExEnchantSkillList.packetFor(activeChar));
			}
		}
		return true;
	}
	
	private void debug_stats(Player activeChar)
	{
		GameObject target_obj = activeChar.getTarget();
		if(!target_obj.isCreature())
		{
			activeChar.sendPacket(Msg.INVALID_TARGET);
			return;
		}
		Creature target = (Creature) target_obj;
		Calculator[] calculators = target.getCalculators();
		String log_str = "--- Debug for " + target.getName() + " ---\r\n";
		for(Calculator calculator : calculators)
		{
			if(calculator == null)
				continue;
			Env env = new Env(target, activeChar, null);
			env.value = calculator.getBase();
			log_str = log_str + "Stat: " + calculator._stat.getValue() + ", prevValue: " + calculator.getLast() + "\r\n";
			Func[] funcs = calculator.getFunctions();
			for(int i = 0;i < funcs.length;++i)
			{
				String order = Integer.toHexString(funcs[i].order).toUpperCase();
				if(order.length() == 1)
				{
					order = "0" + order;
				}
				log_str = log_str + "\tFunc #" + i + "@ [0x" + order + "]" + funcs[i].getClass().getSimpleName() + "\t" + env.value;
				if(funcs[i].getCondition() == null || funcs[i].getCondition().test(env))
				{
					funcs[i].calc(env);
				}
				log_str = log_str + " -> " + env.value + (funcs[i].owner != null ? new StringBuilder().append("; owner: ").append(funcs[i].owner).toString() : "; no owner") + "\r\n";
			}
		}
		Log.add(log_str, "debug_stats");
	}
	
	private void adminGiveAllSkills(Player activeChar)
	{
		GameObject target = activeChar.getTarget();
		if(target == null || !target.isPlayer() || activeChar != target && !activeChar.getPlayerAccess().CanEditCharAll)
		{
			activeChar.sendPacket(Msg.INVALID_TARGET);
			return;
		}
		Player player = (Player) target;
		int unLearnable = 0;
		int skillCounter = 0;
		Collection<SkillLearn> skills = SkillAcquireHolder.getInstance().getAvailableSkills(player, AcquireType.NORMAL);
		while(skills.size() > unLearnable)
		{
			unLearnable = 0;
			for(SkillLearn s : skills)
			{
				Skill sk = SkillTable.getInstance().getInfo(s.getId(), s.getLevel());
				if(sk == null || !sk.getCanLearn(player.getClassId()))
				{
					++unLearnable;
					continue;
				}
				if(player.getSkillLevel(sk.getId()) == -1)
				{
					++skillCounter;
				}
				player.addSkill(sk, true);
			}
			skills = SkillAcquireHolder.getInstance().getAvailableSkills(player, AcquireType.NORMAL);
		}
		player.sendMessage("Admin gave you " + skillCounter + " skills.");
		player.sendPacket(new SkillList(player));
		activeChar.sendMessage("You gave " + skillCounter + " skills to " + player.getName());
	}
	
	@Override
	public Enum[] getAdminCommandEnum()
	{
		return Commands.values();
	}
	
	private void removeSkillsPage(Player activeChar)
	{
		GameObject target = activeChar.getTarget();
		if(!target.isPlayer() || activeChar != target && !activeChar.getPlayerAccess().CanEditCharAll)
		{
			activeChar.sendPacket(Msg.INVALID_TARGET);
			return;
		}
		Player player = (Player) target;
		List<Skill> skills = new ArrayList<>();
		skills.addAll(player.getAllSkills());
		skills = skills.subList(0, Math.min(skills.size(), 50));
		NpcHtmlMessage adminReply = new NpcHtmlMessage(5);
		StringBuilder replyMSG = new StringBuilder("<html><body>");
		replyMSG.append("<table width=260><tr>");
		replyMSG.append("<td width=40><button value=\"Main\" action=\"bypass -h admin_admin\" width=40 height=15 back=\"sek.cbui94\" fore=\"sek.cbui94\"></td>");
		replyMSG.append("<td width=180><center>Character Selection Menu</center></td>");
		replyMSG.append("<td width=40><button value=\"Back\" action=\"bypass -h admin_show_skills\" width=40 height=15 back=\"sek.cbui94\" fore=\"sek.cbui94\"></td>");
		replyMSG.append("</tr></table>");
		replyMSG.append("<br><br>");
		replyMSG.append("<center>Editing character: " + player.getName() + "</center>");
		replyMSG.append("<br><table width=270><tr><td>Lv: " + player.getLevel() + " " + player.getTemplate().className + "</td></tr></table>");
		replyMSG.append("<br><center>Click on the skill you wish to remove:</center>");
		replyMSG.append("<br><table width=270>");
		replyMSG.append("<tr><td width=80>Name:</td><td width=60>Level:</td><td width=40>Id:</td></tr>");
		for(Skill element : skills)
		{
			replyMSG.append("<tr><td width=80><a action=\"bypass -h admin_remove_skill " + element.getId() + "\">" + element.getName() + "</a></td><td width=60>" + element.getLevel() + "</td><td width=40>" + element.getId() + "</td></tr>");
		}
		replyMSG.append("</table>");
		replyMSG.append("<br><center><table>");
		replyMSG.append("Remove custom skill:");
		replyMSG.append("<tr><td>Id: </td>");
		replyMSG.append("<td><edit var=\"id_to_remove\" width=110></td></tr>");
		replyMSG.append("</table></center>");
		replyMSG.append("<center><button value=\"Remove skill\" action=\"bypass -h admin_remove_skill $id_to_remove\" width=110 height=15 back=\"sek.cbui94\" fore=\"sek.cbui94\"></center>");
		replyMSG.append("<br><center><button value=\"Back\" action=\"bypass -h admin_current_player\" width=40 height=15></center>");
		replyMSG.append("</body></html>");
		adminReply.setHtml(replyMSG.toString());
		activeChar.sendPacket(adminReply);
	}
	
	private void showSkillsPage(Player activeChar)
	{
		GameObject target = activeChar.getTarget();
		if(target == null || !target.isPlayer() || activeChar != target && !activeChar.getPlayerAccess().CanEditCharAll)
		{
			activeChar.sendPacket(Msg.INVALID_TARGET);
			return;
		}
		Player player = (Player) target;
		NpcHtmlMessage adminReply = new NpcHtmlMessage(5);
		StringBuilder replyMSG = new StringBuilder("<html><body>");
		replyMSG.append("<table width=260><tr>");
		replyMSG.append("<td width=40><button value=\"Main\" action=\"bypass -h admin_admin\" width=40 height=15 back=\"sek.cbui94\" fore=\"sek.cbui94\"></td>");
		replyMSG.append("<td width=180><center>Character Selection Menu</center></td>");
		replyMSG.append("<td width=40><button value=\"Back\" action=\"bypass -h admin_current_player\" width=40 height=15 back=\"sek.cbui94\" fore=\"sek.cbui94\"></td>");
		replyMSG.append("</tr></table>");
		replyMSG.append("<br><br>");
		replyMSG.append("<center>Editing character: " + player.getName() + "</center>");
		replyMSG.append("<br><table width=270><tr><td>Lv: " + player.getLevel() + " " + player.getTemplate().className + "</td></tr></table>");
		replyMSG.append("<br><center><table>");
		replyMSG.append("<tr><td><button value=\"Add skills\" action=\"bypass -h admin_skill_list\" width=70 height=15 back=\"sek.cbui94\" fore=\"sek.cbui94\"></td>");
		replyMSG.append("<td><button value=\"Get skills\" action=\"bypass -h admin_get_skills\" width=70 height=15 back=\"sek.cbui94\" fore=\"sek.cbui94\"></td></tr>");
		replyMSG.append("<tr><td><button value=\"Delete skills\" action=\"bypass -h admin_remove_skills\" width=70 height=15 back=\"sek.cbui94\" fore=\"sek.cbui94\"></td>");
		replyMSG.append("<td><button value=\"Reset skills\" action=\"bypass -h admin_reset_skills\" width=70 height=15 back=\"sek.cbui94\" fore=\"sek.cbui94\"></td></tr>");
		replyMSG.append("<tr><td><button value=\"Give All Skills\" action=\"bypass -h admin_give_all_skills\" width=70 height=15 back=\"sek.cbui94\" fore=\"sek.cbui94\"></td></tr>");
		replyMSG.append("</table></center>");
		replyMSG.append("</body></html>");
		adminReply.setHtml(replyMSG.toString());
		activeChar.sendPacket(adminReply);
	}
	
	private void showEffects(Player activeChar)
	{
		GameObject target = activeChar.getTarget();
		if(target == null || !target.isPlayable() || activeChar != target && !activeChar.getPlayerAccess().CanEditCharAll)
		{
			activeChar.sendPacket(Msg.INVALID_TARGET);
			return;
		}
		Playable playable = (Playable) target;
		NpcHtmlMessage adminReply = new NpcHtmlMessage(5);
		StringBuilder replyMSG = new StringBuilder("<html><body>");
		replyMSG.append("<table width=260><tr>");
		replyMSG.append("<td width=40><button value=\"Main\" action=\"bypass -h admin_admin\" width=40 height=15 back=\"sek.cbui94\" fore=\"sek.cbui94\"></td>");
		replyMSG.append("<td width=180><center>Character Selection Menu</center></td>");
		replyMSG.append("<td width=40><button value=\"Back\" action=\"bypass -h admin_current_player\" width=40 height=15 back=\"sek.cbui94\" fore=\"sek.cbui94\"></td>");
		replyMSG.append("</tr></table>");
		replyMSG.append("<br><br>");
		replyMSG.append("<center>Editing character: " + playable.getName() + "</center>");
		replyMSG.append("<br><center><button value=\"Refresh\" action=\"bypass -h admin_show_effects\" width=100 height=15 back=\"sek.cbui94\" fore=\"sek.cbui94\" /></center>");
		replyMSG.append("<br>");
		List<Effect> list = playable.getEffectList().getAllEffects();
		if(list != null && !list.isEmpty())
		{
			for(Effect e : list)
			{
				replyMSG.append("&nbsp;<a action=\"bypass -h admin_stop_effect ").append(e.getSkill().getId()).append("\">");
				replyMSG.append(e.getSkill().getName()).append(" ").append(e.getSkill().getLevel());
				replyMSG.append("</a> - ").append(e.getSkill().isToggle() ? "Infinity" : "" + e.getTimeLeft() + " seconds").append("<br1>");
			}
		}
		replyMSG.append("<br></body></html>");
		adminReply.setHtml(replyMSG.toString());
		activeChar.sendPacket(adminReply);
	}
	
	private void stopEffect(Player activeChar, String[] wordList)
	{
		GameObject target = activeChar.getTarget();
		if(target == null || !target.isPlayable() || activeChar != target && !activeChar.getPlayerAccess().CanEditCharAll)
		{
			activeChar.sendPacket(Msg.INVALID_TARGET);
			return;
		}
		Playable playable = (Playable) target;
		if(wordList.length == 2)
		{
			int id = Integer.parseInt(wordList[1]);
			List<Effect> effects = playable.getEffectList().getEffectsBySkillId(id);
			if(effects != null && !effects.isEmpty())
			{
				for(Effect eff : effects)
				{
					eff.exit();
					playable.getPlayer().sendMessage("Admin removed effect of " + eff.getSkill().getName() + ".");
					playable.sendChanges();
					playable.updateStats();
					playable.updateEffectIcons();
					playable.broadcastStatusUpdate();
					activeChar.sendMessage("You removed effect of " + eff.getSkill().getName() + " from " + playable.getName() + ".");
				}
			}
			else
			{
				activeChar.sendMessage("Error: there is no such skill.");
			}
		}
		showEffects(activeChar);
	}
	
	private void adminGetSkills(Player activeChar)
	{
		GameObject target = activeChar.getTarget();
		if(!target.isPlayer() || activeChar != target && !activeChar.getPlayerAccess().CanEditCharAll)
		{
			activeChar.sendPacket(Msg.INVALID_TARGET);
			return;
		}
		Player player = (Player) target;
		if(player.getName().equals(activeChar.getName()))
		{
			player.sendMessage("There is no point in doing it on your character.");
		}
		else
		{
			Collection<Skill> skills = player.getAllSkills();
			adminSkills = activeChar.getAllSkillsArray();
			for(Skill element : adminSkills)
			{
				activeChar.removeSkill(element, true);
			}
			for(Skill element : skills)
			{
				activeChar.addSkill(element, true);
			}
			activeChar.sendMessage("You now have all the skills of  " + player.getName() + ".");
		}
		showSkillsPage(activeChar);
	}
	
	private void adminResetSkills(Player activeChar)
	{
		GameObject target = activeChar.getTarget();
		if(!target.isPlayer() || activeChar != target && !activeChar.getPlayerAccess().CanEditCharAll)
		{
			activeChar.sendPacket(Msg.INVALID_TARGET);
			return;
		}
		Player player = (Player) target;
		Skill[] skills = player.getAllSkillsArray();
		int counter = 0;
		for(Skill skill : skills)
		{
			if(skill.isClanSkill() || skill.isCommon() || SkillAcquireHolder.getInstance().isSkillPossible(player, skill))
				continue;
			player.removeSkill(skill, true);
			player.removeSkillFromShortCut(skill.getId());
			++counter;
		}
		player.checkSkills();
		player.sendPacket(new SkillList(player));
		player.sendMessage("[GM]" + activeChar.getName() + " has updated your skills.");
		activeChar.sendMessage("" + counter + " skills removed.");
		showSkillsPage(activeChar);
	}
	
	private void adminAddSkill(Player activeChar, String[] wordList)
	{
		GameObject target = activeChar.getTarget();
		if(target == null || !target.isPlayer() || activeChar != target && !activeChar.getPlayerAccess().CanEditCharAll)
		{
			activeChar.sendPacket(Msg.INVALID_TARGET);
			return;
		}
		Player player = (Player) target;
		if(wordList.length == 3)
		{
			int id = Integer.parseInt(wordList[1]);
			int level = Integer.parseInt(wordList[2]);
			Skill skill = SkillTable.getInstance().getInfo(id, level);
			if(skill != null)
			{
				player.sendMessage("Admin gave you the skill " + skill.getName() + ".");
				player.addSkill(skill, true);
				player.sendPacket(new SkillList(player));
				activeChar.sendMessage("You gave the skill " + skill.getName() + " to " + player.getName() + ".");
			}
			else
			{
				activeChar.sendMessage("Error: there is no such skill.");
			}
		}
		showSkillsPage(activeChar);
	}
	
	private void adminRemoveSkill(Player activeChar, String[] wordList)
	{
		GameObject target = activeChar.getTarget();
		if(!target.isPlayer() || activeChar != target && !activeChar.getPlayerAccess().CanEditCharAll)
		{
			activeChar.sendPacket(Msg.INVALID_TARGET);
			return;
		}
		Player player = (Player) target;
		if(wordList.length == 2)
		{
			int id = Integer.parseInt(wordList[1]);
			int level = player.getSkillLevel(id);
			Skill skill = SkillTable.getInstance().getInfo(id, level);
			if(skill != null)
			{
				player.sendMessage("Admin removed the skill " + skill.getName() + ".");
				player.removeSkill(skill, true);
				player.sendPacket(new SkillList(player));
				activeChar.sendMessage("You removed the skill " + skill.getName() + " from " + player.getName() + ".");
			}
			else
			{
				activeChar.sendMessage("Error: there is no such skill.");
			}
		}
		removeSkillsPage(activeChar);
	}
	
	private enum Commands
	{
		admin_show_skills,
		admin_remove_skills,
		admin_skill_list,
		admin_skill_index,
		admin_add_skill,
		admin_remove_skill,
		admin_get_skills,
		admin_reset_skills,
		admin_give_all_skills,
		admin_show_effects,
		admin_stop_effect,
		admin_debug_stats,
		admin_remove_cooldown,
		admin_buff,
		admin_skill_ench,
		admin_skill_enchant;
	}
}