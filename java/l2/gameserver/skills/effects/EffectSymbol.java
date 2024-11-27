package l2.gameserver.skills.effects;

import l2.gameserver.cache.Msg;
import l2.gameserver.data.xml.holder.NpcHolder;
import l2.gameserver.geodata.GeoEngine;
import l2.gameserver.idfactory.IdFactory;
import l2.gameserver.model.Creature;
import l2.gameserver.model.Effect;
import l2.gameserver.model.Player;
import l2.gameserver.model.Skill;
import l2.gameserver.model.World;
import l2.gameserver.model.instances.NpcInstance;
import l2.gameserver.model.instances.SymbolInstance;
import l2.gameserver.network.l2.s2c.MagicSkillLaunched;
import l2.gameserver.stats.Env;
import l2.gameserver.templates.npc.NpcTemplate;
import l2.gameserver.utils.Location;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;

public final class EffectSymbol extends Effect
{
	private static final Logger _log = LoggerFactory.getLogger(EffectSymbol.class);
	private NpcInstance _symbol;
	
	public EffectSymbol(Env env, EffectTemplate template)
	{
		super(env, template);
	}
	
	@Override
	public boolean checkCondition()
	{
		if(getSkill().getTargetType() != Skill.SkillTargetType.TARGET_SELF)
		{
			_log.error("Symbol skill with target != self, id = " + getSkill().getId());
			return false;
		}
		Skill skill = getSkill().getFirstAddedSkill();
		if(skill == null)
		{
			_log.error("Not implemented symbol skill, id = " + getSkill().getId());
			return false;
		}
		return super.checkCondition();
	}
	
	@Override
	public void onStart()
	{
		super.onStart();
		Skill skill = getSkill().getFirstAddedSkill();
		skill.setMagicType(getSkill().getMagicType());
		Location loc = _effected.getLoc();
		if(_effected.isPlayer() && ((Player) _effected).getGroundSkillLoc() != null)
		{
			loc = ((Player) _effected).getGroundSkillLoc();
			((Player) _effected).setGroundSkillLoc(null);
		}
		NpcTemplate template = NpcHolder.getInstance().getTemplate(_skill.getSymbolId());
		_symbol = getTemplate()._count <= 1 ? new SymbolInstance(IdFactory.getInstance().getNextId(), template, _effected, skill) : new NpcInstance(IdFactory.getInstance().getNextId(), template);
		_symbol.setLevel(_effected.getLevel());
		_symbol.setReflection(_effected.getReflection());
		_symbol.spawnMe(loc);
	}
	
	@Override
	public void onExit()
	{
		super.onExit();
		if(_symbol != null && _symbol.isVisible())
		{
			_symbol.deleteMe();
		}
		_symbol = null;
	}
	
	@Override
	public boolean onActionTime()
	{
		if(getTemplate()._count <= 1)
		{
			return false;
		}
		Creature effector = getEffector();
		Skill skill = getSkill().getFirstAddedSkill();
		NpcInstance symbol = _symbol;
		double mpConsume = getSkill().getMpConsume();
		if(effector == null || skill == null || symbol == null)
		{
			return false;
		}
		if(mpConsume > effector.getCurrentMp())
		{
			effector.sendPacket(Msg.NOT_ENOUGH_MP);
			return false;
		}
		effector.reduceCurrentMp(mpConsume, effector);
		for(Creature cha : World.getAroundCharacters(symbol, getSkill().getSkillRadius(), 200))
		{
			if(cha.isDoor() || cha.getEffectList().getEffectsBySkill(skill) != null || skill.checkTarget(effector, cha, cha, false, false) != null || skill.isOffensive() && !GeoEngine.canSeeTarget(symbol, cha, false))
				continue;
			ArrayList<Creature> targets = new ArrayList<>(1);
			targets.add(cha);
			effector.callSkill(skill, targets, true);
			effector.broadcastPacket(new MagicSkillLaunched(symbol, getSkill().getDisplayId(), getSkill().getDisplayLevel(), cha));
		}
		return true;
	}
}