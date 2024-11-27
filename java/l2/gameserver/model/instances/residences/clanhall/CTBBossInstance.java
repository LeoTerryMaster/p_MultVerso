package l2.gameserver.model.instances.residences.clanhall;

import l2.gameserver.model.Creature;
import l2.gameserver.model.Skill;
import l2.gameserver.model.entity.events.impl.ClanHallTeamBattleEvent;
import l2.gameserver.model.entity.events.objects.CTBSiegeClanObject;
import l2.gameserver.model.entity.events.objects.CTBTeamObject;
import l2.gameserver.model.instances.MonsterInstance;
import l2.gameserver.tables.SkillTable;
import l2.gameserver.templates.npc.NpcTemplate;

public abstract class CTBBossInstance extends MonsterInstance
{
	public static final Skill SKILL = SkillTable.getInstance().getInfo(5456, 1);
	private CTBTeamObject _matchTeamObject;
	
	public CTBBossInstance(int objectId, NpcTemplate template)
	{
		super(objectId, template);
		setHasChatWindow(false);
	}
	
	@Override
	public void reduceCurrentHp(double damage, Creature attacker, Skill skill, boolean awake, boolean standUp, boolean directHp, boolean canReflect, boolean transferDamage, boolean isDot, boolean sendMessage)
	{
		if(attacker.getLevel() > getLevel() + 8 && attacker.getEffectList().getEffectsCountForSkill(SKILL.getId()) == 0)
		{
			doCast(SKILL, attacker, false);
			return;
		}
		super.reduceCurrentHp(damage, attacker, skill, awake, standUp, directHp, canReflect, transferDamage, isDot, sendMessage);
	}
	
	@Override
	public boolean isAttackable(Creature attacker)
	{
		CTBSiegeClanObject clan = _matchTeamObject.getSiegeClan();
		return clan == null || !attacker.isPlayable() || attacker.getPlayer().getClan() != clan.getClan();
	}
	
	@Override
	public boolean isAutoAttackable(Creature attacker)
	{
		return isAttackable(attacker);
	}
	
	@Override
	public void onDeath(Creature killer)
	{
		ClanHallTeamBattleEvent event = getEvent(ClanHallTeamBattleEvent.class);
		event.processStep(_matchTeamObject);
		super.onDeath(killer);
	}
	
	@Override
	public String getTitle()
	{
		CTBSiegeClanObject clan = _matchTeamObject.getSiegeClan();
		return clan == null ? "" : clan.getClan().getName();
	}
	
	public void setMatchTeamObject(CTBTeamObject matchTeamObject)
	{
		_matchTeamObject = matchTeamObject;
	}
}