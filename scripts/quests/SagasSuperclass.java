package quests;

import gnu.trove.TIntIntHashMap;
import l2.commons.lang.reference.HardReference;
import l2.commons.util.Rnd;
import l2.gameserver.Config;
import l2.gameserver.ai.CtrlEvent;
import l2.gameserver.ai.CtrlIntention;
import l2.gameserver.model.Creature;
import l2.gameserver.model.GameObjectsStorage;
import l2.gameserver.model.Party;
import l2.gameserver.model.Player;
import l2.gameserver.model.base.ClassId;
import l2.gameserver.model.instances.NpcInstance;
import l2.gameserver.model.quest.Quest;
import l2.gameserver.model.quest.QuestState;
import l2.gameserver.network.l2.s2c.MagicSkillUse;
import l2.gameserver.scripts.Functions;
import l2.gameserver.utils.Location;
import l2.gameserver.utils.NpcUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.napile.primitive.maps.IntObjectMap;
import org.napile.primitive.maps.impl.CHashIntObjectMap;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public abstract class SagasSuperclass extends Quest
{
	protected static final Map<Integer, Pair<Class<?>, ClassId[]>> Quests;
	
	static
	{
		Map<Integer, Pair<Class<?>, ClassId[]>> theQuests = new HashMap();
		theQuests.put(70, Pair.of(_070_SagaOfThePhoenixKnight.class, new ClassId[] {ClassId.phoenixKnight}));
		theQuests.put(71, Pair.of(_071_SagaOfEvasTemplar.class, new ClassId[] {ClassId.evaTemplar}));
		theQuests.put(72, Pair.of(_072_SagaOfTheSwordMuse.class, new ClassId[] {ClassId.swordMuse}));
		theQuests.put(73, Pair.of(_073_SagaOfTheDuelist.class, new ClassId[] {ClassId.duelist}));
		theQuests.put(74, Pair.of(_074_SagaOfTheDreadnoughts.class, new ClassId[] {ClassId.dreadnought}));
		theQuests.put(75, Pair.of(_075_SagaOfTheTitan.class, new ClassId[] {ClassId.titan}));
		theQuests.put(76, Pair.of(_076_SagaOfTheGrandKhavatari.class, new ClassId[] {ClassId.grandKhauatari}));
		theQuests.put(77, Pair.of(_077_SagaOfTheDominator.class, new ClassId[] {ClassId.dominator}));
		theQuests.put(78, Pair.of(_078_SagaOfTheDoomcryer.class, new ClassId[] {ClassId.doomcryer}));
		theQuests.put(79, Pair.of(_079_SagaOfTheAdventurer.class, new ClassId[] {ClassId.adventurer}));
		theQuests.put(80, Pair.of(_080_SagaOfTheWindRider.class, new ClassId[] {ClassId.windRider}));
		theQuests.put(81, Pair.of(_081_SagaOfTheGhostHunter.class, new ClassId[] {ClassId.ghostHunter}));
		theQuests.put(82, Pair.of(_082_SagaOfTheSagittarius.class, new ClassId[] {ClassId.sagittarius}));
		theQuests.put(83, Pair.of(_083_SagaOfTheMoonlightSentinel.class, new ClassId[] {ClassId.moonlightSentinel}));
		theQuests.put(84, Pair.of(_084_SagaOfTheGhostSentinel.class, new ClassId[] {ClassId.ghostSentinel}));
		theQuests.put(85, Pair.of(_085_SagaOfTheCardinal.class, new ClassId[] {ClassId.cardinal}));
		theQuests.put(86, Pair.of(_086_SagaOfTheHierophant.class, new ClassId[] {ClassId.hierophant}));
		theQuests.put(87, Pair.of(_087_SagaOfEvasSaint.class, new ClassId[] {ClassId.evaSaint}));
		theQuests.put(88, Pair.of(_088_SagaOfTheArchmage.class, new ClassId[] {ClassId.archmage}));
		theQuests.put(89, Pair.of(_089_SagaOfTheMysticMuse.class, new ClassId[] {ClassId.mysticMuse}));
		theQuests.put(90, Pair.of(_090_SagaOfTheStormScreamer.class, new ClassId[] {ClassId.stormScreamer}));
		theQuests.put(91, Pair.of(_091_SagaOfTheArcanaLord.class, new ClassId[] {ClassId.arcanaLord}));
		theQuests.put(92, Pair.of(_092_SagaOfTheElementalMaster.class, new ClassId[] {ClassId.elementalMaster}));
		theQuests.put(93, Pair.of(_093_SagaOfTheSpectralMaster.class, new ClassId[] {ClassId.spectralMaster}));
		theQuests.put(94, Pair.of(_094_SagaOfTheSoultaker.class, new ClassId[] {ClassId.soultaker}));
		theQuests.put(95, Pair.of(_095_SagaOfTheHellKnight.class, new ClassId[] {ClassId.hellKnight}));
		theQuests.put(96, Pair.of(_096_SagaOfTheSpectralDancer.class, new ClassId[] {ClassId.spectralDancer}));
		theQuests.put(97, Pair.of(_097_SagaOfTheShillienTemplar.class, new ClassId[] {ClassId.shillienTemplar}));
		theQuests.put(98, Pair.of(_098_SagaOfTheShillienSaint.class, new ClassId[] {ClassId.shillienSaint}));
		theQuests.put(99, Pair.of(_099_SagaOfTheFortuneSeeker.class, new ClassId[] {ClassId.fortuneSeeker}));
		theQuests.put(100, Pair.of(_100_SagaOfTheMaestro.class, new ClassId[] {ClassId.maestro}));
		Quests = Collections.unmodifiableMap(theQuests);
	}
	
	public int[] Items = {0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10};
	public String[] Text = new String[18];
	protected int id;
	protected int classid;
	protected int prevclass;
	protected int[] NPC = {0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10};
	protected int[] Mob = {0, 1, 2};
	protected int[] X = {0, 1, 2};
	protected int[] Y = {0, 1, 2};
	protected int[] Z = {0, 1, 2};
	protected IntObjectMap<List<QuestSpawnInfo>> _spawnInfos = new CHashIntObjectMap();
	protected TIntIntHashMap _kills = new TIntIntHashMap();
	protected TIntIntHashMap _archons = new TIntIntHashMap();
	protected int[] Archon_Minions = {21646, 21647, 21648, 21649, 21650, 21651};
	protected int[] Guardian_Angels = {27214, 27215, 27216};
	protected int[] Archon_Hellisha_Norm = {18212, 18213, 18214, 18215, 18216, 18217, 18218, 18219};
	
	public SagasSuperclass(boolean party)
	{
		super(party);
	}
	
	public static QuestState findQuest(Player player)
	{
		for(Map.Entry<Integer, Pair<Class<?>, ClassId[]>> e : Quests.entrySet())
		{
			int questId = e.getKey();
			Class questClazz = e.getValue().getLeft();
			ClassId[] questClassIds = e.getValue().getRight();
			QuestState st = player.getQuestState(questClazz);
			if(st == null)
				continue;
			for(ClassId questCID : questClassIds)
			{
				if(questCID.getParent(player.getSex()) != player.getClassId())
					continue;
				return st;
			}
		}
		return null;
	}
	
	public static void process_step_15to16(QuestState st)
	{
		if(st == null || st.getCond() != 15)
		{
			return;
		}
		int Halishas_Mark = ((SagasSuperclass) st.getQuest()).Items[3];
		int Resonance_Amulet = ((SagasSuperclass) st.getQuest()).Items[8];
		st.takeItems(Halishas_Mark, -1);
		if(st.getQuestItemsCount(Resonance_Amulet) == 0)
		{
			st.giveItems(Resonance_Amulet, 1);
		}
		st.setCond(16);
		st.playSound("ItemSound.quest_middle");
	}
	
	private void FinishQuest(QuestState st, Player player)
	{
		_kills.remove(player.getObjectId());
		_archons.remove(player.getObjectId());
		_spawnInfos.remove(player.getObjectId());
		st.addExpAndSp(2586527, 0);
		st.giveItems(57, 5000000);
		st.giveItems(6622, 1, true);
		st.exitCurrentQuest(true);
		player.setClassId(getClassId(player), false, true);
		if(!player.isSubClassActive() && player.getBaseClassId() == getPrevClass(player))
		{
			player.setBaseClass(getClassId(player));
		}
		player.broadcastCharInfo();
		Cast(st.findTemplate(NPC[0]), player, 4339, 1);
	}
	
	protected void registerNPCs()
	{
		addStartNpc(NPC[0]);
		addAttackId(Mob[2]);
		addFirstTalkId(NPC[4]);
		for(int npc : NPC)
		{
			addTalkId(npc);
		}
		for(int mobid : Mob)
		{
			addKillId(mobid);
		}
		for(int mobid : Archon_Minions)
		{
			addKillId(mobid);
		}
		for(int mobid : Guardian_Angels)
		{
			addKillId(mobid);
		}
		for(int mobid : Archon_Hellisha_Norm)
		{
			addKillId(mobid);
		}
		for(int ItemId : Items)
		{
			if(ItemId == 0 || ItemId == 7080 || ItemId == 7081 || ItemId == 6480 || ItemId == 6482)
				continue;
			addQuestItem(ItemId);
		}
	}
	
	protected int getClassId(Player player)
	{
		return classid;
	}
	
	protected int getPrevClass(Player player)
	{
		return prevclass;
	}
	
	protected void Cast(NpcInstance npc, Creature target, int skillId, int level)
	{
		target.broadcastPacket(new MagicSkillUse(target, target, skillId, level, 6000, 1));
		target.broadcastPacket(new MagicSkillUse(npc, npc, skillId, level, 6000, 1));
	}
	
	protected void addSpawn(Player player, NpcInstance mob)
	{
		ArrayList<QuestSpawnInfo> list = (ArrayList<QuestSpawnInfo>) _spawnInfos.get(player.getObjectId());
		if(list == null)
		{
			list = new ArrayList<>(4);
			_spawnInfos.put(player.getObjectId(), list);
		}
		list.add(new QuestSpawnInfo(mob));
	}
	
	protected NpcInstance findMySpawn(Player player, int npcId)
	{
		if(npcId == 0 || player == null)
		{
			return null;
		}
		List<QuestSpawnInfo> list = _spawnInfos.get(player.getObjectId());
		if(list == null)
		{
			return null;
		}
		for(QuestSpawnInfo q : list)
		{
			if(q.npcId != npcId)
				continue;
			return q.getNpc();
		}
		return null;
	}
	
	protected void deleteMySpawn(Player player, int npcId)
	{
		List list = _spawnInfos.get(player.getObjectId());
		if(list == null)
		{
			return;
		}
		Iterator it = list.iterator();
		while(it.hasNext())
		{
			QuestSpawnInfo spawn = (QuestSpawnInfo) it.next();
			if(spawn.npcId != npcId)
				continue;
			NpcInstance npc = spawn.getNpc();
			if(npc != null)
			{
				npc.deleteMe();
			}
			it.remove();
		}
	}
	
	public void giveHallishaMark(QuestState st)
	{
		Player player = st.getPlayer();
		if(player == null)
		{
			return;
		}
		Integer val = _archons.get(player.getObjectId());
		if(val == null)
		{
			return;
		}
		if(GameObjectsStorage.getNpc(val) != null)
		{
			return;
		}
		st.cancelQuestTimer("Archon Hellisha has despawned");
		if(st.getQuestItemsCount(Items[3]) < 700)
		{
			st.giveItems(Items[3], (long) Rnd.get(1, 4));
		}
		else
		{
			st.takeItems(Items[3], 20);
			NpcInstance archon = NpcUtils.spawnSingle(Mob[1], st.getPlayer().getLoc(), (long) 600000);
			addSpawn(st.getPlayer(), archon);
			_archons.put(player.getObjectId(), archon.getObjectId());
			st.startQuestTimer("Archon Hellisha has despawned", 600000, archon);
			archon.setRunning();
			archon.getAI().notifyEvent(CtrlEvent.EVT_AGGRESSION, st.getPlayer(), 100000);
			AutoChat(archon, Text[13].replace("PLAYERNAME", st.getPlayer().getName()));
		}
	}
	
	protected QuestState findRightState(Player player, NpcInstance npc)
	{
		if(player == null || npc == null)
		{
			return null;
		}
		List<QuestSpawnInfo> list = _spawnInfos.get(player.getObjectId());
		if(list == null)
		{
			return null;
		}
		for(QuestSpawnInfo q : list)
		{
			NpcInstance npc2 = q.getNpc();
			if(npc2 != npc)
				continue;
			return player.getQuestState(this);
		}
		return null;
	}
	
	protected void AutoChat(NpcInstance npc, String text)
	{
		if(npc != null)
		{
			Functions.npcSay(npc, text);
		}
	}
	
	@Override
	public String onEvent(String event, QuestState st, NpcInstance npc)
	{
		String htmltext = "";
		Player player = st.getPlayer();
		if(event.equalsIgnoreCase("0-011.htm") || event.equalsIgnoreCase("0-012.htm") || event.equalsIgnoreCase("0-013.htm") || event.equalsIgnoreCase("0-014.htm") || event.equalsIgnoreCase("0-015.htm"))
		{
			htmltext = event;
		}
		else if(event.equalsIgnoreCase("accept"))
		{
			st.setCond(1);
			st.setState(2);
			st.playSound("ItemSound.quest_accept");
			st.giveItems(Items[10], 1);
			htmltext = "0-03.htm";
		}
		else if(event.equalsIgnoreCase("0-1"))
		{
			if(player.getLevel() < 76)
			{
				htmltext = "0-02.htm";
				st.exitCurrentQuest(true);
			}
			else
			{
				htmltext = "0-05.htm";
			}
		}
		else if(event.equalsIgnoreCase("0-2"))
		{
			if(player.getLevel() >= 76)
			{
				htmltext = "0-07.htm";
				st.takeItems(Items[10], -1);
				FinishQuest(st, player);
			}
			else
			{
				st.takeItems(Items[10], -1);
				st.playSound("ItemSound.quest_middle");
				st.setCond(20);
				htmltext = "0-08.htm";
			}
		}
		else if(event.equalsIgnoreCase("1-3"))
		{
			st.setCond(3);
			htmltext = "1-05.htm";
		}
		else if(event.equalsIgnoreCase("1-4"))
		{
			st.setCond(4);
			st.takeItems(Items[0], 1);
			if(Items[11] != 0)
			{
				st.takeItems(Items[11], 1);
			}
			st.giveItems(Items[1], 1);
			htmltext = "1-06.htm";
		}
		else if(event.equalsIgnoreCase("2-1"))
		{
			st.setCond(2);
			htmltext = "2-05.htm";
		}
		else if(event.equalsIgnoreCase("2-2"))
		{
			st.setCond(5);
			st.takeItems(Items[1], 1);
			st.giveItems(Items[4], 1);
			htmltext = "2-06.htm";
		}
		else if(event.equalsIgnoreCase("3-5"))
		{
			htmltext = "3-07.htm";
		}
		else if(event.equalsIgnoreCase("3-6"))
		{
			st.setCond(11);
			htmltext = "3-02.htm";
		}
		else if(event.equalsIgnoreCase("3-7"))
		{
			st.setCond(12);
			htmltext = "3-03.htm";
		}
		else if(event.equalsIgnoreCase("3-8"))
		{
			st.setCond(13);
			st.takeItems(Items[2], 1);
			st.giveItems(Items[7], 1);
			htmltext = "3-08.htm";
		}
		else if(event.equalsIgnoreCase("4-1"))
		{
			htmltext = "4-010.htm";
		}
		else if(event.equalsIgnoreCase("4-2"))
		{
			st.giveItems(Items[9], 1);
			st.setCond(18);
			st.playSound("ItemSound.quest_middle");
			htmltext = "4-011.htm";
		}
		else
		{
			if(event.equalsIgnoreCase("4-3"))
			{
				st.giveItems(Items[9], 1);
				st.setCond(18);
				st.set("Quest0", "0");
				st.playSound("ItemSound.quest_middle");
				NpcInstance Mob_2 = findMySpawn(player, NPC[4]);
				if(Mob_2 != null)
				{
					AutoChat(Mob_2, Text[13].replace("PLAYERNAME", player.getName()));
					deleteMySpawn(player, NPC[4]);
					st.cancelQuestTimer("Mob_2 has despawned");
					st.cancelQuestTimer("NPC_4 Timer");
				}
				return null;
			}
			if(event.equalsIgnoreCase("5-1"))
			{
				st.setCond(6);
				st.takeItems(Items[4], 1);
				Cast(st.findTemplate(NPC[5]), player, 4546, 1);
				st.playSound("ItemSound.quest_middle");
				htmltext = "5-02.htm";
			}
			else if(event.equalsIgnoreCase("6-1"))
			{
				st.setCond(8);
				st.takeItems(Items[5], 1);
				Cast(st.findTemplate(NPC[6]), player, 4546, 1);
				st.playSound("ItemSound.quest_middle");
				htmltext = "6-03.htm";
			}
			else if(event.equalsIgnoreCase("7-1"))
			{
				if(findMySpawn(player, Mob[0]) == null)
				{
					NpcInstance Mob_1 = NpcUtils.spawnSingle(Mob[0], new Location(X[0], Y[0], Z[0]), (long) 180000);
					addSpawn(player, Mob_1);
					st.startQuestTimer("Mob_0 Timer", 500, Mob_1);
					st.startQuestTimer("Mob_1 has despawned", 120000, Mob_1);
					htmltext = "7-02.htm";
				}
				else
				{
					htmltext = "7-03.htm";
				}
			}
			else if(event.equalsIgnoreCase("7-2"))
			{
				st.setCond(10);
				st.takeItems(Items[6], 1);
				Cast(st.findTemplate(NPC[7]), player, 4546, 1);
				st.playSound("ItemSound.quest_middle");
				htmltext = "7-06.htm";
			}
			else if(event.equalsIgnoreCase("8-1"))
			{
				st.setCond(14);
				st.takeItems(Items[7], 1);
				Cast(st.findTemplate(NPC[8]), player, 4546, 1);
				st.playSound("ItemSound.quest_middle");
				htmltext = "8-02.htm";
			}
			else if(event.equalsIgnoreCase("9-1"))
			{
				st.setCond(17);
				st.takeItems(Items[8], 1);
				Cast(st.findTemplate(NPC[9]), player, 4546, 1);
				st.playSound("ItemSound.quest_middle");
				htmltext = "9-03.htm";
			}
			else if(event.equalsIgnoreCase("10-1"))
			{
				if(st.getInt("Quest0") == 0 || findMySpawn(player, NPC[4]) == null)
				{
					deleteMySpawn(player, NPC[4]);
					deleteMySpawn(player, Mob[2]);
					st.set("Quest0", "1");
					st.set("Quest1", "45");
					NpcInstance NPC_4 = NpcUtils.spawnSingle(NPC[4], new Location(X[2], Y[2], Z[2]), (long) 300000);
					NpcInstance Mob_2 = NpcUtils.spawnSingle(Mob[2], new Location(X[1], Y[1], Z[1]), (long) 300000);
					addSpawn(player, Mob_2);
					addSpawn(player, NPC_4);
					st.startQuestTimer("Mob_2 Timer", 1000, Mob_2);
					st.startQuestTimer("Mob_2 despawn", 59000, Mob_2);
					st.startQuestTimer("NPC_4 Timer", 500, NPC_4);
					st.startQuestTimer("NPC_4 despawn", 60000, NPC_4);
					htmltext = "10-02.htm";
				}
				else if(st.getInt("Quest1") == 45)
				{
					htmltext = "10-03.htm";
				}
				else if(st.getInt("Tab") == 1)
				{
					NpcInstance Mob_2 = findMySpawn(player, NPC[4]);
					if(Mob_2 == null || !st.getPlayer().knowsObject(Mob_2))
					{
						deleteMySpawn(player, NPC[4]);
						Mob_2 = NpcUtils.spawnSingle(NPC[4], new Location(X[2], Y[2], Z[2]), (long) 300000);
						addSpawn(player, Mob_2);
						st.set("Quest0", "1");
						st.set("Quest1", "0");
						st.startQuestTimer("NPC_4 despawn", 180000, Mob_2);
					}
					htmltext = "10-04.htm";
				}
			}
			else if(event.equalsIgnoreCase("10-2"))
			{
				st.setCond(19);
				st.takeItems(Items[9], 1);
				Cast(st.findTemplate(NPC[10]), player, 4546, 1);
				st.playSound("ItemSound.quest_middle");
				htmltext = "10-06.htm";
			}
			else if(event.equalsIgnoreCase("11-9"))
			{
				st.setCond(15);
				htmltext = "11-03.htm";
			}
			else
			{
				if(event.equalsIgnoreCase("Mob_0 Timer"))
				{
					AutoChat(findMySpawn(player, Mob[0]), Text[0].replace("PLAYERNAME", player.getName()));
					return null;
				}
				if(event.equalsIgnoreCase("Mob_1 has despawned"))
				{
					AutoChat(findMySpawn(player, Mob[0]), Text[1].replace("PLAYERNAME", player.getName()));
					deleteMySpawn(player, Mob[0]);
					return null;
				}
				if(event.equalsIgnoreCase("Archon Hellisha has despawned"))
				{
					AutoChat(npc, Text[6].replace("PLAYERNAME", player.getName()));
					deleteMySpawn(player, Mob[1]);
					return null;
				}
				if(event.equalsIgnoreCase("Mob_2 Timer"))
				{
					NpcInstance Mob_2;
					NpcInstance NPC_4 = findMySpawn(player, NPC[4]);
					if(NPC_4.knowsObject(Mob_2 = findMySpawn(player, Mob[2])))
					{
						NPC_4.setRunning();
						NPC_4.getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, Mob_2, null);
						Mob_2.setRunning();
						Mob_2.getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, NPC_4, null);
						AutoChat(Mob_2, Text[14].replace("PLAYERNAME", player.getName()));
					}
					else
					{
						st.startQuestTimer("Mob_2 Timer", 1000, npc);
					}
					return null;
				}
				if(event.equalsIgnoreCase("Mob_2 despawn"))
				{
					NpcInstance Mob_2 = findMySpawn(player, Mob[2]);
					AutoChat(Mob_2, Text[15].replace("PLAYERNAME", player.getName()));
					st.set("Quest0", "2");
					if(Mob_2 != null)
					{
						Mob_2.reduceCurrentHp(9999999.0, Mob_2, null, true, true, false, false, false, false, false);
					}
					deleteMySpawn(player, Mob[2]);
					return null;
				}
				if(event.equalsIgnoreCase("NPC_4 Timer"))
				{
					AutoChat(findMySpawn(player, NPC[4]), Text[7].replace("PLAYERNAME", player.getName()));
					st.startQuestTimer("NPC_4 Timer 2", 1500, npc);
					if(st.getInt("Quest1") == 45)
					{
						st.set("Quest1", "0");
					}
					return null;
				}
				if(event.equalsIgnoreCase("NPC_4 Timer 2"))
				{
					AutoChat(findMySpawn(player, NPC[4]), Text[8].replace("PLAYERNAME", player.getName()));
					st.startQuestTimer("NPC_4 Timer 3", 10000, npc);
					return null;
				}
				if(event.equalsIgnoreCase("NPC_4 Timer 3"))
				{
					if(st.getInt("Quest0") == 0)
					{
						st.startQuestTimer("NPC_4 Timer 3", 13000, npc);
						AutoChat(findMySpawn(player, NPC[4]), Text[Rnd.get(9, 10)].replace("PLAYERNAME", player.getName()));
					}
					return null;
				}
				if(event.equalsIgnoreCase("NPC_4 despawn"))
				{
					st.set("Quest1", str((long) (st.getInt("Quest1") + 1)));
					NpcInstance NPC_4 = findMySpawn(player, NPC[4]);
					if(st.getInt("Quest0") == 1 || st.getInt("Quest0") == 2 || st.getInt("Quest1") > 3)
					{
						st.set("Quest0", "0");
						AutoChat(NPC_4, Text[Rnd.get(11, 12)].replace("PLAYERNAME", player.getName()));
						if(NPC_4 != null)
						{
							NPC_4.reduceCurrentHp(9999999.0, NPC_4, null, true, true, false, false, false, false, false);
						}
						deleteMySpawn(player, NPC[4]);
					}
					else
					{
						st.startQuestTimer("NPC_4 despawn", 1000, npc);
					}
					return null;
				}
			}
		}
		return htmltext;
	}
	
	@Override
	public String onTalk(NpcInstance npc, QuestState st)
	{
		String htmltext = "noquest";
		int npcId = npc.getNpcId();
		int cond = st.getCond();
		Player player = st.getPlayer();
		if(player.getClassId().getId() != getPrevClass(player))
		{
			st.exitCurrentQuest(true);
			return htmltext;
		}
		if(cond == 0)
		{
			if(npcId == NPC[0])
			{
				htmltext = "0-01.htm";
			}
		}
		else if(cond == 1)
		{
			if(npcId == NPC[0])
			{
				htmltext = "0-04.htm";
			}
			else if(npcId == NPC[2])
			{
				htmltext = "2-01.htm";
			}
		}
		else if(cond == 2)
		{
			if(npcId == NPC[2])
			{
				htmltext = "2-02.htm";
			}
			else if(npcId == NPC[1])
			{
				htmltext = "1-01.htm";
			}
		}
		else if(cond == 3)
		{
			if(npcId == NPC[1])
			{
				htmltext = st.getQuestItemsCount(Items[0]) > 0 ? Items[11] == 0 ? "1-03.htm" : st.getQuestItemsCount(Items[11]) > 0 ? "1-03.htm" : "1-02.htm" : "1-02.htm";
			}
			else if(npcId == 31537)
			{
				if(st.getQuestItemsCount(7546) == 0)
				{
					htmltext = "tunatun_q72_01.htm";
					st.giveItems(7546, 1);
				}
				else
				{
					htmltext = "tunatun_q72_02.htm";
				}
			}
		}
		else if(cond == 4)
		{
			if(npcId == NPC[1])
			{
				htmltext = "1-04.htm";
			}
			else if(npcId == NPC[2])
			{
				htmltext = "2-03.htm";
			}
		}
		else if(cond == 5)
		{
			if(npcId == NPC[2])
			{
				htmltext = "2-04.htm";
			}
			else if(npcId == NPC[5])
			{
				htmltext = "5-01.htm";
			}
		}
		else if(cond == 6)
		{
			if(npcId == NPC[5])
			{
				htmltext = "5-03.htm";
			}
			else if(npcId == NPC[6])
			{
				htmltext = "6-01.htm";
			}
		}
		else if(cond == 7)
		{
			if(npcId == NPC[6])
			{
				htmltext = "6-02.htm";
			}
		}
		else if(cond == 8)
		{
			if(npcId == NPC[6])
			{
				htmltext = "6-04.htm";
			}
			else if(npcId == NPC[7])
			{
				htmltext = "7-01.htm";
			}
		}
		else if(cond == 9)
		{
			if(npcId == NPC[7])
			{
				htmltext = "7-05.htm";
			}
		}
		else if(cond == 10)
		{
			if(npcId == NPC[7])
			{
				htmltext = "7-07.htm";
			}
			else if(npcId == NPC[3])
			{
				htmltext = "3-01.htm";
			}
		}
		else if(cond == 11 || cond == 12)
		{
			if(npcId == NPC[3])
			{
				htmltext = st.getQuestItemsCount(Items[2]) > 0 ? "3-05.htm" : "3-04.htm";
			}
		}
		else if(cond == 13)
		{
			if(npcId == NPC[3])
			{
				htmltext = "3-06.htm";
			}
			else if(npcId == NPC[8])
			{
				htmltext = "8-01.htm";
			}
		}
		else if(cond == 14)
		{
			if(npcId == NPC[8])
			{
				htmltext = "8-03.htm";
			}
			else if(npcId == NPC[11])
			{
				htmltext = "11-01.htm";
			}
		}
		else if(cond == 15)
		{
			if(npcId == NPC[11])
			{
				htmltext = "11-02.htm";
			}
			else if(npcId == NPC[9])
			{
				htmltext = "9-01.htm";
			}
		}
		else if(cond == 16)
		{
			if(npcId == NPC[9])
			{
				htmltext = "9-02.htm";
			}
		}
		else if(cond == 17)
		{
			if(npcId == NPC[9])
			{
				htmltext = "9-04.htm";
			}
			else if(npcId == NPC[10])
			{
				htmltext = "10-01.htm";
			}
		}
		else if(cond == 18)
		{
			if(npcId == NPC[10])
			{
				htmltext = "10-05.htm";
			}
		}
		else if(cond == 19)
		{
			if(npcId == NPC[10])
			{
				htmltext = "10-07.htm";
			}
			if(npcId == NPC[0])
			{
				htmltext = "0-06.htm";
			}
		}
		else if(cond == 20 && npcId == NPC[0])
		{
			if(player.getLevel() >= 76)
			{
				htmltext = "0-09.htm";
				if(getClassId(player) < 131 || getClassId(player) > 135)
				{
					FinishQuest(st, player);
				}
			}
			else
			{
				htmltext = "0-010.htm";
			}
		}
		return htmltext;
	}
	
	@Override
	public String onFirstTalk(NpcInstance npc, Player player)
	{
		String htmltext = "";
		QuestState st = player.getQuestState(this);
		if(st == null)
		{
			return htmltext;
		}
		int npcId = npc.getNpcId();
		int cond = st.getCond();
		if(npcId == NPC[4])
		{
			if(cond == 17)
			{
				QuestState st2 = findRightState(player, npc);
				if(st2 != null)
				{
					if(st == st2)
					{
						if(st.getInt("Tab") == 1)
						{
							if(st.getInt("Quest0") == 0)
							{
								htmltext = "4-04.htm";
							}
							else if(st.getInt("Quest0") == 1)
							{
								htmltext = "4-06.htm";
							}
						}
						else if(st.getInt("Quest0") == 0)
						{
							htmltext = "4-01.htm";
						}
						else if(st.getInt("Quest0") == 1)
						{
							htmltext = "4-03.htm";
						}
					}
					else if(st.getInt("Tab") == 1)
					{
						if(st.getInt("Quest0") == 0)
						{
							htmltext = "4-05.htm";
						}
						else if(st.getInt("Quest0") == 1)
						{
							htmltext = "4-07.htm";
						}
					}
					else if(st.getInt("Quest0") == 0)
					{
						htmltext = "4-02.htm";
					}
				}
			}
			else if(cond == 18)
			{
				htmltext = "4-08.htm";
			}
		}
		return htmltext;
	}
	
	@Override
	public String onAttack(NpcInstance npc, QuestState st)
	{
		Player player = st.getPlayer();
		if(st.getCond() == 17 && npc.getNpcId() == Mob[2] && st == findRightState(player, npc))
		{
			st.set("Quest0", str((long) (st.getInt("Quest0") + 1)));
			if(st.getInt("Quest0") == 1)
			{
				AutoChat(npc, Text[16].replace("PLAYERNAME", player.getName()));
			}
			if(st.getInt("Quest0") > 15)
			{
				st.set("Quest0", "1");
				AutoChat(npc, Text[17].replace("PLAYERNAME", player.getName()));
				npc.reduceCurrentHp(9999999.0, npc, null, true, true, false, false, false, false, false);
				deleteMySpawn(player, Mob[2]);
				st.cancelQuestTimer("Mob_2 despawn");
				st.set("Tab", "1");
			}
		}
		return null;
	}
	
	protected boolean isArchonMinions(int npcId)
	{
		for(int id : Archon_Minions)
		{
			if(id != npcId)
				continue;
			return true;
		}
		return false;
	}
	
	protected boolean isArchonHellishaNorm(int npcId)
	{
		for(int id : Archon_Hellisha_Norm)
		{
			if(id != npcId)
				continue;
			return true;
		}
		return false;
	}
	
	protected boolean isGuardianAngels(int npcId)
	{
		for(int id : Guardian_Angels)
		{
			if(id != npcId)
				continue;
			return true;
		}
		return false;
	}
	
	@Override
	public String onKill(NpcInstance npc, QuestState st)
	{
		int npcId = npc.getNpcId();
		Player player = st.getPlayer();
		if(player.getActiveClassId() != getPrevClass(player))
		{
			return null;
		}
		if(isArchonMinions(npcId))
		{
			Party party = player.getParty();
			if(party != null)
			{
				for(Player player1 : party.getPartyMembers())
				{
					QuestState st1;
					if(player1.getDistance(player) > (double) Config.ALT_PARTY_DISTRIBUTION_RANGE || (st1 = findQuest(player1)) == null || st1.getCond() != 15)
						continue;
					((SagasSuperclass) st1.getQuest()).giveHallishaMark(st1);
				}
			}
			else
			{
				QuestState st1 = findQuest(player);
				if(st1 != null && st1.getCond() == 15)
				{
					((SagasSuperclass) st1.getQuest()).giveHallishaMark(st1);
				}
			}
		}
		else if(isArchonHellishaNorm(npcId))
		{
			QuestState st1 = findQuest(player);
			if(st1 != null && st1.getCond() == 15)
			{
				AutoChat(npc, ((SagasSuperclass) st1.getQuest()).Text[4].replace("PLAYERNAME", st1.getPlayer().getName()));
				process_step_15to16(st1);
			}
		}
		else if(isGuardianAngels(npcId))
		{
			QuestState st1 = findQuest(player);
			if(st1 != null && st1.getCond() == 6)
			{
				Integer val0 = _kills.get(player.getObjectId());
				if(val0 == null)
				{
					val0 = 0;
				}
				if(val0 < 3)
				{
					_kills.put(player.getObjectId(), val0 + 1);
				}
				else
				{
					st1.playSound("ItemSound.quest_middle");
					st1.giveItems(((SagasSuperclass) st1.getQuest()).Items[5], 1);
					st1.setCond(7);
				}
			}
		}
		else
		{
			int cond = st.getCond();
			if(npcId == Mob[0] && cond == 8)
			{
				QuestState st22 = findRightState(player, npc);
				if(st22 != null)
				{
					if(!player.isInParty() && st == st22)
					{
						AutoChat(npc, Text[12].replace("PLAYERNAME", player.getName()));
						st.giveItems(Items[6], 1);
						st.setCond(9);
						st.playSound("ItemSound.quest_middle");
					}
					st.cancelQuestTimer("Mob_1 has despawned");
					deleteMySpawn(st22.getPlayer(), Mob[0]);
				}
			}
			else if(npcId == Mob[1] && cond == 15)
			{
				QuestState st23 = findRightState(player, npc);
				if(st23 != null)
				{
					if(!player.isInParty())
					{
						if(st == st23)
						{
							AutoChat(npc, Text[4].replace("PLAYERNAME", player.getName()));
							process_step_15to16(st);
						}
						else
						{
							AutoChat(npc, Text[5].replace("PLAYERNAME", player.getName()));
						}
					}
					st.cancelQuestTimer("Archon Hellisha has despawned");
					deleteMySpawn(st23.getPlayer(), Mob[1]);
				}
			}
			else if(npcId == Mob[2] && cond == 17 && st == findRightState(player, npc))
			{
				st.set("Quest0", "1");
				AutoChat(npc, Text[17].replace("PLAYERNAME", player.getName()));
				npc.reduceCurrentHp(9999999.0, npc, null, true, true, false, false, false, false, false);
				deleteMySpawn(player, Mob[2]);
				st.cancelQuestTimer("Mob_2 despawn");
				st.set("Tab", "1");
			}
		}
		return null;
	}
	
	private class QuestSpawnInfo
	{
		public final int npcId;
		private final HardReference<NpcInstance> _npcRef;
		
		public QuestSpawnInfo(NpcInstance npc)
		{
			npcId = npc.getNpcId();
			_npcRef = npc.getRef();
		}
		
		public NpcInstance getNpc()
		{
			return _npcRef.get();
		}
	}
}