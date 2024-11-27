package zones;

import l2.commons.threading.RunnableImpl;
import l2.gameserver.ThreadPoolManager;
import l2.gameserver.instancemanager.ReflectionManager;
import l2.gameserver.listener.zone.OnZoneEnterLeaveListener;
import l2.gameserver.model.Creature;
import l2.gameserver.model.Player;
import l2.gameserver.model.Skill;
import l2.gameserver.model.Zone;
import l2.gameserver.model.entity.events.impl.SiegeEvent;
import l2.gameserver.scripts.ScriptFile;
import l2.gameserver.tables.SkillTable;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.StringTokenizer;

public class AutoBuffZone implements ScriptFile
{
	private static final Logger LOG = LoggerFactory.getLogger(AutoBuffZone.class);
	
	private static final boolean checkPlayerForZoneBuff(Player player)
	{
		if(player == null || player.isDead())
		{
			return false;
		}
		if(player.isCursedWeaponEquipped() || player.isFakeDeath() || player.isFlying())
		{
			return false;
		}
		if(player.isInDuel() || player.isOlyParticipant() || player.isInStoreMode() || player.isSitting())
		{
			return false;
		}
		return player.getEvent(SiegeEvent.class) == null;
	}
	
	private static List<Pair<Skill, Integer>> parseZoneBuffs(String zoneBuffsText)
	{
		ArrayList<Pair<Skill, Integer>> result = new ArrayList<>();
		StringTokenizer zoneBuffTok = new StringTokenizer(zoneBuffsText, ",;");
		while(zoneBuffTok.hasMoreTokens())
		{
			String zoneBuffText = zoneBuffTok.nextToken().trim();
			if(zoneBuffText.isEmpty())
				continue;
			int durationModDelimIdx = zoneBuffText.indexOf(47);
			String skillIdLvlText = zoneBuffText;
			Integer durationMod = null;
			if(durationModDelimIdx > 0)
			{
				durationMod = Integer.parseInt(zoneBuffText.substring(durationModDelimIdx + 1).trim());
				skillIdLvlText = zoneBuffText.substring(0, durationModDelimIdx).trim();
			}
			int idLvlDelimIdx;
			if((idLvlDelimIdx = skillIdLvlText.indexOf(58)) < 0)
			{
				throw new IllegalArgumentException("Can't parse \"" + zoneBuffText + "\"");
			}
			int skillId = Integer.parseInt(skillIdLvlText.substring(0, idLvlDelimIdx).trim());
			int skillLevel = Integer.parseInt(skillIdLvlText.substring(idLvlDelimIdx + 1).trim());
			Skill skill = SkillTable.getInstance().getInfo(skillId, skillLevel);
			if(skill == null)
			{
				throw new IllegalArgumentException("Unknown skill \"" + zoneBuffText + "\"");
			}
			result.add(Pair.of(skill, durationMod));
		}
		result.trimToSize();
		return Collections.unmodifiableList(result);
	}
	
	private static void init()
	{
		int count = 0;
		Collection<Zone> zones = ReflectionManager.DEFAULT.getZones();
		for(Zone zone : zones)
		{
			List<Pair<Skill, Integer>> zoneBuffs;
			String zoneBuffsText = zone.getParams().getString("zoneBuffs", null);
			if(zoneBuffsText == null || zoneBuffsText.isEmpty() || (zoneBuffs = parseZoneBuffs(zoneBuffsText)).isEmpty())
				continue;
			zone.addListener(new AutoBuffZoneListener(zoneBuffs));
			++count;
		}
		LOG.info("AutoBuffZone: Loaded " + count + " auto buff zone(s).");
	}
	
	@Override
	public void onLoad()
	{
		init();
	}
	
	@Override
	public void onReload()
	{
	}
	
	@Override
	public void onShutdown()
	{
	}
	
	private static class AutoBuffZoneListener implements OnZoneEnterLeaveListener
	{
		private final List<Pair<Skill, Integer>> _zoneBuffs;
		
		private AutoBuffZoneListener(List<Pair<Skill, Integer>> zoneBuffs)
		{
			_zoneBuffs = zoneBuffs;
		}
		
		@Override
		public void onZoneEnter(Zone zone, Creature actor)
		{
			if(actor == null || !actor.isPlayer())
			{
				return;
			}
			Player target = actor.getPlayer();
			if(!checkPlayerForZoneBuff(target))
			{
				return;
			}
			ThreadPoolManager.getInstance().execute(new RunnableImpl()
			{
				
				@Override
				public void runImpl() throws Exception
				{
					for(Pair zoneBuff : _zoneBuffs)
					{
						Skill skill = (Skill) zoneBuff.getLeft();
						Integer durationMod = (Integer) zoneBuff.getRight();
						if(target.getEffectList().containEffectFromSkills(skill.getId()))
							continue;
						if(durationMod != null)
						{
							skill.getEffects(target, target, false, false, (long) durationMod.intValue() * 1000, 1.0, false);
							continue;
						}
						skill.getEffects(target, target, false, false);
					}
				}
			});
		}
		
		@Override
		public void onZoneLeave(Zone zone, Creature actor)
		{
		}
	}
}