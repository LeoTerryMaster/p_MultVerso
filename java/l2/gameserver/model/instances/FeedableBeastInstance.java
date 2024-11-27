package l2.gameserver.model.instances;

import gnu.trove.TIntArrayList;
import gnu.trove.TIntObjectHashMap;
import l2.commons.threading.RunnableImpl;
import l2.commons.util.Rnd;
import l2.gameserver.ThreadPoolManager;
import l2.gameserver.ai.CtrlEvent;
import l2.gameserver.data.xml.holder.NpcHolder;
import l2.gameserver.idfactory.IdFactory;
import l2.gameserver.model.Creature;
import l2.gameserver.model.Player;
import l2.gameserver.model.quest.QuestState;
import l2.gameserver.network.l2.s2c.SocialAction;
import l2.gameserver.templates.npc.NpcTemplate;
import l2.gameserver.utils.Location;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class FeedableBeastInstance extends MonsterInstance
{
	public static final TIntObjectHashMap<growthInfo> growthCapableMobs;
	public static final TIntArrayList tamedBeasts;
	public static final TIntArrayList feedableBeasts;
	private static final Logger _log;
	private static final int GOLDEN_SPICE;
	private static final int CRYSTAL_SPICE;
	private static final int SKILL_GOLDEN_SPICE;
	private static final int SKILL_CRYSTAL_SPICE;
	public static Map<Integer, Integer> feedInfo;
	
	static
	{
		Integer n;
		Integer n2;
		_log = LoggerFactory.getLogger(NpcInstance.class);
		GOLDEN_SPICE = 0;
		CRYSTAL_SPICE = 1;
		SKILL_GOLDEN_SPICE = 2188;
		SKILL_CRYSTAL_SPICE = 2189;
		growthCapableMobs = new TIntObjectHashMap();
		tamedBeasts = new TIntArrayList();
		feedableBeasts = new TIntArrayList();
		growthCapableMobs.put(21451, new growthInfo(0, new int[][] {{21452, 21453, 21454, 21455}, {21456, 21457, 21458, 21459}}, 100));
		growthCapableMobs.put(21452, new growthInfo(1, new int[][] {{21460, 21462}, new int[0]}, 40));
		growthCapableMobs.put(21453, new growthInfo(1, new int[][] {{21461, 21463}, new int[0]}, 40));
		growthCapableMobs.put(21454, new growthInfo(1, new int[][] {{21460, 21462}, new int[0]}, 40));
		growthCapableMobs.put(21455, new growthInfo(1, new int[][] {{21461, 21463}, new int[0]}, 40));
		growthCapableMobs.put(21456, new growthInfo(1, new int[][] {new int[0], {21464, 21466}}, 40));
		growthCapableMobs.put(21457, new growthInfo(1, new int[][] {new int[0], {21465, 21467}}, 40));
		growthCapableMobs.put(21458, new growthInfo(1, new int[][] {new int[0], {21464, 21466}}, 40));
		growthCapableMobs.put(21459, new growthInfo(1, new int[][] {new int[0], {21465, 21467}}, 40));
		growthCapableMobs.put(21460, new growthInfo(2, new int[][] {{21468, 16017}, new int[0]}, 25));
		growthCapableMobs.put(21461, new growthInfo(2, new int[][] {{21469, 16018}, new int[0]}, 25));
		growthCapableMobs.put(21462, new growthInfo(2, new int[][] {{21468, 16017}, new int[0]}, 25));
		growthCapableMobs.put(21463, new growthInfo(2, new int[][] {{21469, 16018}, new int[0]}, 25));
		growthCapableMobs.put(21464, new growthInfo(2, new int[][] {new int[0], {21468, 16017}}, 25));
		growthCapableMobs.put(21465, new growthInfo(2, new int[][] {new int[0], {21469, 16018}}, 25));
		growthCapableMobs.put(21466, new growthInfo(2, new int[][] {new int[0], {21468, 16017}}, 25));
		growthCapableMobs.put(21467, new growthInfo(2, new int[][] {new int[0], {21469, 16018}}, 25));
		growthCapableMobs.put(21470, new growthInfo(0, new int[][] {{21472, 21474, 21471, 21473}, {21475, 21476, 21477, 21478}}, 100));
		growthCapableMobs.put(21471, new growthInfo(1, new int[][] {{21479, 21481}, new int[0]}, 40));
		growthCapableMobs.put(21472, new growthInfo(1, new int[][] {{21480, 21482}, new int[0]}, 40));
		growthCapableMobs.put(21473, new growthInfo(1, new int[][] {{21479, 21481}, new int[0]}, 40));
		growthCapableMobs.put(21474, new growthInfo(1, new int[][] {{21480, 21482}, new int[0]}, 40));
		growthCapableMobs.put(21475, new growthInfo(1, new int[][] {new int[0], {21483, 21485}}, 40));
		growthCapableMobs.put(21476, new growthInfo(1, new int[][] {new int[0], {21484, 21486}}, 40));
		growthCapableMobs.put(21477, new growthInfo(1, new int[][] {new int[0], {21483, 21485}}, 40));
		growthCapableMobs.put(21478, new growthInfo(1, new int[][] {new int[0], {21484, 21486}}, 40));
		growthCapableMobs.put(21479, new growthInfo(2, new int[][] {{21487, 16014}, new int[0]}, 25));
		growthCapableMobs.put(21480, new growthInfo(2, new int[][] {{21488, 16013}, new int[0]}, 25));
		growthCapableMobs.put(21481, new growthInfo(2, new int[][] {{21487, 16014}, new int[0]}, 25));
		growthCapableMobs.put(21482, new growthInfo(2, new int[][] {{21488, 16013}, new int[0]}, 25));
		growthCapableMobs.put(21483, new growthInfo(2, new int[][] {new int[0], {21487, 16014}}, 25));
		growthCapableMobs.put(21484, new growthInfo(2, new int[][] {new int[0], {21488, 16013}}, 25));
		growthCapableMobs.put(21485, new growthInfo(2, new int[][] {new int[0], {21487, 16014}}, 25));
		growthCapableMobs.put(21486, new growthInfo(2, new int[][] {new int[0], {21488, 16013}}, 25));
		growthCapableMobs.put(21489, new growthInfo(0, new int[][] {{21491, 21493, 21490, 21492}, {21495, 21497, 21494, 21496}}, 100));
		growthCapableMobs.put(21490, new growthInfo(1, new int[][] {{21498, 21500}, new int[0]}, 40));
		growthCapableMobs.put(21491, new growthInfo(1, new int[][] {{21499, 21501}, new int[0]}, 40));
		growthCapableMobs.put(21492, new growthInfo(1, new int[][] {{21498, 21500}, new int[0]}, 40));
		growthCapableMobs.put(21493, new growthInfo(1, new int[][] {{21499, 21501}, new int[0]}, 40));
		growthCapableMobs.put(21494, new growthInfo(1, new int[][] {new int[0], {21502, 21504}}, 40));
		growthCapableMobs.put(21495, new growthInfo(1, new int[][] {new int[0], {21503, 21505}}, 40));
		growthCapableMobs.put(21496, new growthInfo(1, new int[][] {new int[0], {21502, 21504}}, 40));
		growthCapableMobs.put(21497, new growthInfo(1, new int[][] {new int[0], {21503, 21505}}, 40));
		growthCapableMobs.put(21498, new growthInfo(2, new int[][] {{21506, 16015}, new int[0]}, 25));
		growthCapableMobs.put(21499, new growthInfo(2, new int[][] {{21507, 16016}, new int[0]}, 25));
		growthCapableMobs.put(21500, new growthInfo(2, new int[][] {{21506, 16015}, new int[0]}, 25));
		growthCapableMobs.put(21501, new growthInfo(2, new int[][] {{21507, 16015}, new int[0]}, 25));
		growthCapableMobs.put(21502, new growthInfo(2, new int[][] {new int[0], {21506, 16015}}, 25));
		growthCapableMobs.put(21503, new growthInfo(2, new int[][] {new int[0], {21507, 16016}}, 25));
		growthCapableMobs.put(21504, new growthInfo(2, new int[][] {new int[0], {21506, 16015}}, 25));
		growthCapableMobs.put(21505, new growthInfo(2, new int[][] {new int[0], {21507, 16016}}, 25));
		
		for(int i = 16013;i <= 16018;i = i + 1)
		{
			tamedBeasts.add(i);
		}
		
		for(int i = 16013;i <= 16019;i = i + 1)
		{
			feedableBeasts.add(i);
		}
		
		for(int i = 21451;i <= 21507;i = i + 1)
		{
			feedableBeasts.add(i);
		}
		
		for(int i = 21824;i <= 21829;i = i + 1)
		{
			feedableBeasts.add(i);
		}
		
		feedInfo = new ConcurrentHashMap();
	}
	
	public FeedableBeastInstance(int objectId, NpcTemplate template)
	{
		super(objectId, template);
	}
	
	private boolean isGoldenSpice(int skillId)
	{
		return skillId == 2188;
	}
	
	private boolean isCrystalSpice(int skillId)
	{
		return skillId == 2189;
	}
	
	private int getFoodSpice(int skillId)
	{
		if(isGoldenSpice(skillId))
		{
			return 6643;
		}
		return 6644;
	}
	
	public int getItemIdBySkillId(int skillId)
	{
		int itemId;
		switch(skillId)
		{
			case 2188:
			{
				itemId = 6643;
				break;
			}
			case 2189:
			{
				itemId = 6644;
				break;
			}
			default:
			{
				itemId = 0;
			}
		}
		return itemId;
	}
	
	private void spawnNext(Player player, int growthLevel, int food)
	{
		int npcId = getNpcId();
		int nextNpcId = growthCapableMobs.get(npcId).spice[food][Rnd.get(growthCapableMobs.get(npcId).spice[food].length)];
		feedInfo.remove(getObjectId());
		if(growthCapableMobs.get(npcId).growth_level == 0)
		{
			onDecay();
		}
		else
		{
			deleteMe();
		}
		if(tamedBeasts.contains(nextNpcId))
		{
			if(player.getTrainedBeast() != null)
			{
				player.getTrainedBeast().doDespawn();
			}
			NpcTemplate template = NpcHolder.getInstance().getTemplate(nextNpcId);
			TamedBeastInstance nextNpc = new TamedBeastInstance(IdFactory.getInstance().getNextId(), template);
			Location loc = player.getLoc();
			loc.x += Rnd.get(-50, 50);
			loc.y += Rnd.get(-50, 50);
			nextNpc.spawnMe(loc);
			nextNpc.setTameType(player);
			nextNpc.setFoodType(getFoodSpice(food == GOLDEN_SPICE ? SKILL_GOLDEN_SPICE : SKILL_CRYSTAL_SPICE));
			nextNpc.setRunning();
			nextNpc.setOwner(player);
			QuestState st = player.getQuestState("_020_BringUpWithLove");
			if(st != null && !st.isCompleted() && Rnd.chance(5) && st.getQuestItemsCount(7185) == 0)
			{
				st.giveItems(7185, 1);
				st.setCond(2);
			}
			if((st = player.getQuestState("_655_AGrandPlanForTamingWildBeasts")) != null && !st.isCompleted() && st.getCond() == 1 && st.getQuestItemsCount(8084) < 10)
			{
				st.giveItems(8084, 1);
			}
		}
		else
		{
			MonsterInstance nextNpc = spawn(nextNpcId, getX(), getY(), getZ());
			feedInfo.put(nextNpc.getObjectId(), player.getObjectId());
			player.setObjectTarget(nextNpc);
			ThreadPoolManager.getInstance().schedule(new AggrPlayer(nextNpc, player), 3000);
		}
	}
	
	@Override
	protected void onDeath(Creature killer)
	{
		feedInfo.remove(getObjectId());
		super.onDeath(killer);
	}
	
	public MonsterInstance spawn(int npcId, int x, int y, int z)
	{
		try
		{
			MonsterInstance monster = (MonsterInstance) NpcHolder.getInstance().getTemplate(npcId).getInstanceConstructor().newInstance(IdFactory.getInstance().getNextId(), NpcHolder.getInstance().getTemplate(npcId));
			monster.setSpawnedLoc(new Location(x, y, z));
			monster.spawnMe(monster.getSpawnedLoc());
			return monster;
		}
		catch(Exception e)
		{
			_log.error("Could not spawn Npc " + npcId, e);
			return null;
		}
	}
	
	public void onSkillUse(Player player, int skillId)
	{
		int npcId = getNpcId();
		if(!feedableBeasts.contains(npcId))
		{
			return;
		}
		if(isGoldenSpice(skillId) && isCrystalSpice(skillId))
		{
			return;
		}
		int food = isGoldenSpice(skillId) ? 0 : 1;
		int objectId = getObjectId();
		broadcastPacket(new SocialAction(objectId, 2));
		if(growthCapableMobs.containsKey(npcId))
		{
			if(growthCapableMobs.get(npcId).spice[food].length == 0)
			{
				return;
			}
			int growthLevel = growthCapableMobs.get(npcId).growth_level;
			if(growthLevel > 0 && feedInfo.get(objectId) != null && feedInfo.get(objectId).intValue() != player.getObjectId())
			{
				return;
			}
			if(Rnd.chance(growthCapableMobs.get(npcId).growth_chance))
			{
				spawnNext(player, growthLevel, food);
			}
		}
		else if(Rnd.chance(60))
		{
			dropItem(player, getItemIdBySkillId(skillId), 1);
		}
	}
	
	public static class AggrPlayer extends RunnableImpl
	{
		private final NpcInstance _actor;
		private final Player _killer;
		
		public AggrPlayer(NpcInstance actor, Player killer)
		{
			_actor = actor;
			_killer = killer;
		}
		
		@Override
		public void runImpl() throws Exception
		{
			_actor.getAI().notifyEvent(CtrlEvent.EVT_AGGRESSION, _killer, 1000);
		}
	}
	
	private static class growthInfo
	{
		public int growth_level;
		public int growth_chance;
		public int[][] spice;
		
		public growthInfo(int level, int[][] sp, int chance)
		{
			growth_level = level;
			spice = sp;
			growth_chance = chance;
		}
	}
}