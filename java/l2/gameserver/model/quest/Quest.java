package l2.gameserver.model.quest;

import gnu.trove.TIntHashSet;
import gnu.trove.TIntObjectHashMap;
import l2.commons.dbutils.DbUtils;
import l2.commons.logging.LogUtils;
import l2.commons.threading.RunnableImpl;
import l2.commons.util.TroveUtils;
import l2.gameserver.Config;
import l2.gameserver.ThreadPoolManager;
import l2.gameserver.data.xml.holder.ItemHolder;
import l2.gameserver.data.xml.holder.NpcHolder;
import l2.gameserver.database.DatabaseFactory;
import l2.gameserver.instancemanager.QuestManager;
import l2.gameserver.instancemanager.ReflectionManager;
import l2.gameserver.model.Creature;
import l2.gameserver.model.GameObject;
import l2.gameserver.model.Player;
import l2.gameserver.model.Skill;
import l2.gameserver.model.entity.oly.CompetitionType;
import l2.gameserver.model.instances.NpcInstance;
import l2.gameserver.network.l2.components.CustomMessage;
import l2.gameserver.network.l2.s2c.ExQuestNpcLogList;
import l2.gameserver.network.l2.s2c.NpcHtmlMessage;
import l2.gameserver.scripts.Functions;
import l2.gameserver.templates.item.ItemTemplate;
import l2.gameserver.templates.npc.NpcTemplate;
import l2.gameserver.utils.Location;
import org.apache.commons.lang3.ArrayUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class Quest
{
	public static final String SOUND_ITEMGET = "ItemSound.quest_itemget";
	public static final String SOUND_ACCEPT = "ItemSound.quest_accept";
	public static final String SOUND_MIDDLE = "ItemSound.quest_middle";
	public static final String SOUND_FINISH = "ItemSound.quest_finish";
	public static final String SOUND_GIVEUP = "ItemSound.quest_giveup";
	public static final String SOUND_TUTORIAL = "ItemSound.quest_tutorial";
	public static final String SOUND_JACKPOT = "ItemSound.quest_jackpot";
	public static final String SOUND_LIQUID_MIX_01 = "SkillSound5.liquid_mix_01";
	public static final String SOUND_LIQUID_SUCCESS_01 = "SkillSound5.liquid_success_01";
	public static final String SOUND_LIQUID_FAIL_01 = "SkillSound5.liquid_fail_01";
	public static final String SOUND_BEFORE_BATTLE = "Itemsound.quest_before_battle";
	public static final String SOUND_FANFARE_MIDDLE = "ItemSound.quest_fanfare_middle";
	public static final String SOUND_FANFARE2 = "ItemSound.quest_fanfare_2";
	public static final String SOUND_BROKEN_KEY = "ItemSound2.broken_key";
	public static final String SOUND_ENCHANT_SUCESS = "ItemSound3.sys_enchant_sucess";
	public static final String SOUND_ENCHANT_FAILED = "ItemSound3.sys_enchant_failed";
	public static final String SOUND_ED_CHIMES05 = "AmdSound.ed_chimes_05";
	public static final String SOUND_ED_DRONE_02 = "AmbSound.ed_drone_02";
	public static final String SOUND_CD_CRYSTAL_LOOP = "AmbSound.cd_crystal_loop";
	public static final String SOUND_DT_PERCUSSION_01 = "AmbSound.dt_percussion_01";
	public static final String SOUND_AC_PERCUSSION_02 = "AmbSound.ac_percussion_02";
	public static final String SOUND_ARMOR_WOOD_3 = "ItemSound.armor_wood_3";
	public static final String SOUND_ITEM_DROP_EQUIP_ARMOR_CLOTH = "ItemSound.item_drop_equip_armor_cloth";
	public static final String SOUND_MT_CREAK01 = "AmbSound.mt_creak01";
	public static final String SOUND_D_WIND_LOOT_02 = "AmdSound.d_wind_loot_02";
	public static final String SOUND_CHARSTAT_OPEN_01 = "InterfaceSound.charstat_open_01";
	public static final String SOUND_DD_HORROR_01 = "AmbSound.dd_horror_01";
	public static final String SOUND_HORROR1 = "SkillSound5.horror_01";
	public static final String SOUND_HORROR2 = "SkillSound5.horror_02";
	public static final String SOUND_ELCROKI_SONG_FULL = "EtcSound.elcroki_song_full";
	public static final String SOUND_ELCROKI_SONG_1ST = "EtcSound.elcroki_song_1st";
	public static final String SOUND_ELCROKI_SONG_2ND = "EtcSound.elcroki_song_2nd";
	public static final String SOUND_ELCROKI_SONG_3RD = "EtcSound.elcroki_song_3rd";
	public static final String SOUND_ITEMDROP_ARMOR_LEATHER = "ItemSound.itemdrop_armor_leather";
	public static final String SOUND_EG_DRON_02 = "AmbSound.eg_dron_02";
	public static final String SOUND_MHFIGHTER_CRY = "ChrSound.MHFighter_cry";
	public static final String SOUND_ITEMDROP_WEAPON_SPEAR = "ItemSound.itemdrop_weapon_spear";
	public static final String SOUND_FDELF_CRY = "ChrSound.FDElf_Cry";
	public static final String SOUND_DD_HORROR_02 = "AmdSound.dd_horror_02";
	public static final String SOUND_D_HORROR_03 = "AmbSound.d_horror_03";
	public static final String SOUND_D_HORROR_15 = "AmbSound.d_horror_15";
	public static final String SOUND_ANTARAS_FEAR = "SkillSound3.antaras_fear";
	public static final String NO_QUEST_DIALOG = "no-quest";
	public static final int ADENA_ID = 57;
	public static final int PARTY_NONE = 0;
	public static final int PARTY_ONE = 1;
	public static final int PARTY_ALL = 2;
	public static final int CREATED = 1;
	public static final int STARTED = 2;
	public static final int COMPLETED = 3;
	public static final int DELAYED = 4;
	private static final Logger _log = LoggerFactory.getLogger(Quest.class);
	protected final String _name;
	protected final int _party;
	protected final int _questId;
	private final Map<Integer, Map<String, QuestTimer>> _pausedQuestTimers = new ConcurrentHashMap<>();
	private final TIntHashSet _questItems = new TIntHashSet();
	protected String _descr;
	private TIntObjectHashMap<List<QuestNpcLogInfo>> _npcLogList = TroveUtils.emptyIntObjectMap();
	
	public Quest(boolean party)
	{
		this(party ? 1 : 0);
	}
	
	public Quest(int party)
	{
		_name = getClass().getSimpleName();
		_questId = Integer.parseInt(_name.split("_")[1]);
		_party = party;
		QuestManager.addQuest(this);
	}
	
	public static void updateQuestInDb(QuestState qs)
	{
		updateQuestVarInDb(qs, "<state>", qs.getStateName());
	}
	
	public static void updateQuestVarInDb(QuestState qs, String var, String value)
	{
		Player player = qs.getPlayer();
		if(player == null)
		{
			return;
		}
		Connection con = null;
		PreparedStatement statement = null;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("REPLACE INTO character_quests (char_id,name,var,value) VALUES (?,?,?,?)");
			statement.setInt(1, qs.getPlayer().getObjectId());
			statement.setString(2, qs.getQuest().getName());
			statement.setString(3, var);
			statement.setString(4, value);
			statement.executeUpdate();
		}
		catch(Exception e)
		{
			_log.error("could not insert char quest:", e);
		}
		finally
		{
			DbUtils.closeQuietly(con, statement);
		}
	}
	
	public static void deleteQuestInDb(QuestState qs)
	{
		Connection con = null;
		PreparedStatement statement = null;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("DELETE FROM character_quests WHERE char_id=? AND name=?");
			statement.setInt(1, qs.getPlayer().getObjectId());
			statement.setString(2, qs.getQuest().getName());
			statement.executeUpdate();
		}
		catch(Exception e)
		{
			_log.error("could not delete char quest:", e);
		}
		finally
		{
			DbUtils.closeQuietly(con, statement);
		}
	}
	
	public static void deleteQuestVarInDb(QuestState qs, String var)
	{
		Connection con = null;
		PreparedStatement statement = null;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("DELETE FROM character_quests WHERE char_id=? AND name=? AND var=?");
			statement.setInt(1, qs.getPlayer().getObjectId());
			statement.setString(2, qs.getQuest().getName());
			statement.setString(3, var);
			statement.executeUpdate();
		}
		catch(Exception e)
		{
			_log.error("could not delete char quest:", e);
		}
		finally
		{
			DbUtils.closeQuietly(con, statement);
		}
	}
	
	public static void restoreQuestStates(Player player)
	{
		Connection con = null;
		PreparedStatement statement = null;
		PreparedStatement invalidQuestData = null;
		ResultSet rset = null;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			invalidQuestData = con.prepareStatement("DELETE FROM character_quests WHERE char_id=? and name=?");
			statement = con.prepareStatement("SELECT name,value FROM character_quests WHERE char_id=? AND var=?");
			statement.setInt(1, player.getObjectId());
			statement.setString(2, "<state>");
			rset = statement.executeQuery();
			String questId;
			while(rset.next())
			{
				questId = rset.getString("name");
				String state = rset.getString("value");
				if(state.equalsIgnoreCase("Start"))
				{
					invalidQuestData.setInt(1, player.getObjectId());
					invalidQuestData.setString(2, questId);
					invalidQuestData.executeUpdate();
					continue;
				}
				Quest q = QuestManager.getQuest(questId);
				if(q == null)
				{
					if(Config.DONTLOADQUEST)
						continue;
					_log.warn("Unknown quest " + questId + " for player " + player.getName());
					continue;
				}
				new QuestState(q, player, getStateId(state));
			}
			DbUtils.close(statement, rset);
			statement = con.prepareStatement("SELECT name,var,value FROM character_quests WHERE char_id=?");
			statement.setInt(1, player.getObjectId());
			rset = statement.executeQuery();
			while(rset.next())
			{
				questId = rset.getString("name");
				String var = rset.getString("var");
				String value = rset.getString("value");
				QuestState qs = player.getQuestState(questId);
				if(qs == null)
					continue;
				if(var.equals("cond") && Integer.parseInt(value) < 0)
				{
					value = String.valueOf(Integer.parseInt(value) | 1);
				}
				qs.set(var, value, false);
			}
		}
		catch(Exception e)
		{
			_log.error("could not insert char quest:", e);
		}
		finally
		{
			DbUtils.closeQuietly(invalidQuestData);
			DbUtils.closeQuietly(con, statement, rset);
		}
	}
	
	public static String getStateName(int state)
	{
		switch(state)
		{
			case 1:
			{
				return "Start";
			}
			case 2:
			{
				return "Started";
			}
			case 3:
			{
				return "Completed";
			}
			case 4:
			{
				return "Delayed";
			}
		}
		return "Start";
	}
	
	public static int getStateId(String state)
	{
		if(state.equalsIgnoreCase("Start"))
		{
			return 1;
		}
		if(state.equalsIgnoreCase("Started"))
		{
			return 2;
		}
		if(state.equalsIgnoreCase("Completed"))
		{
			return 3;
		}
		if(state.equalsIgnoreCase("Delayed"))
		{
			return 4;
		}
		return 1;
	}
	
	public static NpcInstance addSpawnToInstance(int npcId, int x, int y, int z, int heading, int randomOffset, int refId)
	{
		return addSpawnToInstance(npcId, new Location(x, y, z, heading), randomOffset, refId);
	}
	
	public static NpcInstance addSpawnToInstance(int npcId, Location loc, int randomOffset, int refId)
	{
		try
		{
			NpcTemplate template = NpcHolder.getInstance().getTemplate(npcId);
			if(template != null)
			{
				NpcInstance npc = NpcHolder.getInstance().getTemplate(npcId).getNewInstance();
				npc.setReflection(refId);
				npc.setSpawnedLoc(randomOffset > 50 ? Location.findPointToStay(loc, 50, randomOffset, npc.getGeoIndex()) : loc);
				npc.spawnMe(npc.getSpawnedLoc());
				return npc;
			}
		}
		catch(Exception e)
		{
			_log.warn("Could not spawn Npc " + npcId);
		}
		return null;
	}
	
	public void addQuestItem(int... ids)
	{
		for(int id : ids)
		{
			if(id == 0)
				continue;
			ItemTemplate i = ItemHolder.getInstance().getTemplate(id);
			if(_questItems.contains(id))
			{
				_log.warn("Item " + i + " multiple times in quest drop in " + getName());
			}
			_questItems.add(id);
		}
	}
	
	public int[] getItems()
	{
		return _questItems.toArray();
	}
	
	public boolean isQuestItem(int id)
	{
		return _questItems.contains(id);
	}
	
	public List<QuestNpcLogInfo> getNpcLogList(int cond)
	{
		return _npcLogList.get(cond);
	}
	
	public void addAttackId(int... attackIds)
	{
		for(int attackId : attackIds)
		{
			addEventId(attackId, QuestEventType.ATTACKED_WITH_QUEST);
		}
	}
	
	public NpcTemplate addEventId(int npcId, QuestEventType eventType)
	{
		try
		{
			NpcTemplate t = NpcHolder.getInstance().getTemplate(npcId);
			if(t != null)
			{
				t.addQuestEvent(eventType, this);
			}
			return t;
		}
		catch(Exception e)
		{
			_log.error("", e);
			return null;
		}
	}
	
	public void addKillId(int... killIds)
	{
		for(int killid : killIds)
		{
			addEventId(killid, QuestEventType.MOB_KILLED_WITH_QUEST);
		}
	}
	
	public void addKillNpcWithLog(int cond, String varName, int max, int... killIds)
	{
		if(killIds.length == 0)
		{
			throw new IllegalArgumentException("Npc list cant be empty!");
		}
		addKillId(killIds);
		if(_npcLogList.isEmpty())
		{
			_npcLogList = new TIntObjectHashMap(5);
		}
		ArrayList<QuestNpcLogInfo> vars;
		if((vars = (ArrayList<QuestNpcLogInfo>) _npcLogList.get(cond)) == null)
		{
			vars = new ArrayList<>(5);
			_npcLogList.put(cond, vars);
		}
		vars.add(new QuestNpcLogInfo(killIds, varName, max));
	}
	
	public boolean updateKill(NpcInstance npc, QuestState st)
	{
		Player player = st.getPlayer();
		if(player == null)
		{
			return false;
		}
		List<QuestNpcLogInfo> vars = getNpcLogList(st.getCond());
		if(vars == null)
		{
			return false;
		}
		boolean done = true;
		boolean find = false;
		for(QuestNpcLogInfo info : vars)
		{
			int count = st.getInt(info.getVarName());
			if(!find && ArrayUtils.contains(info.getNpcIds(), npc.getNpcId()))
			{
				find = true;
				if(count < info.getMaxCount())
				{
					st.set(info.getVarName(), ++count);
					player.sendPacket(new ExQuestNpcLogList(st));
				}
			}
			if(count == info.getMaxCount())
				continue;
			done = false;
		}
		return done;
	}
	
	public void addKillId(Collection<Integer> killIds)
	{
		Iterator<Integer> iterator = killIds.iterator();
		while(iterator.hasNext())
		{
			int killid = iterator.next();
			addKillId(killid);
		}
	}
	
	public NpcTemplate addSkillUseId(int npcId)
	{
		return addEventId(npcId, QuestEventType.MOB_TARGETED_BY_SKILL);
	}
	
	public void addStartNpc(int... npcIds)
	{
		for(int talkId : npcIds)
		{
			addStartNpc(talkId);
		}
	}
	
	public NpcTemplate addStartNpc(int npcId)
	{
		addTalkId(npcId);
		return addEventId(npcId, QuestEventType.QUEST_START);
	}
	
	public void addFirstTalkId(int... npcIds)
	{
		for(int npcId : npcIds)
		{
			addEventId(npcId, QuestEventType.NPC_FIRST_TALK);
		}
	}
	
	public void addTalkId(int... talkIds)
	{
		for(int talkId : talkIds)
		{
			addEventId(talkId, QuestEventType.QUEST_TALK);
		}
	}
	
	public void addTalkId(Collection<Integer> talkIds)
	{
		Iterator<Integer> iterator = talkIds.iterator();
		while(iterator.hasNext())
		{
			int talkId = iterator.next();
			addTalkId(talkId);
		}
	}
	
	public String getDescr(Player player)
	{
		String desc;
		QuestState qs = player.getQuestState(this);
		String string = desc = _descr != null ? _descr : new CustomMessage("q." + _questId, player, new Object[0]).toString();
		if(qs == null || qs.isCreated() && qs.isNowAvailable())
		{
			return desc;
		}
		if(qs.isCompleted() || !qs.isNowAvailable())
		{
			return new CustomMessage("quest.Done", player, desc).toString();
		}
		return new CustomMessage("quest.InProgress", player, desc).toString();
	}
	
	public String getName()
	{
		return _name;
	}
	
	public int getQuestIntId()
	{
		return _questId;
	}
	
	public int getParty()
	{
		return _party;
	}
	
	public QuestState newQuestState(Player player, int state)
	{
		QuestState qs = new QuestState(this, player, state);
		updateQuestInDb(qs);
		return qs;
	}
	
	public QuestState newQuestStateAndNotSave(Player player, int state)
	{
		return new QuestState(this, player, state);
	}
	
	public void notifyAttack(NpcInstance npc, QuestState qs)
	{
		String res;
		try
		{
			res = onAttack(npc, qs);
		}
		catch(Exception e)
		{
			showError(qs.getPlayer(), e);
			return;
		}
		showResult(npc, qs.getPlayer(), res);
	}
	
	public void notifyDeath(Creature killer, Creature victim, QuestState qs)
	{
		String res;
		try
		{
			res = onDeath(killer, victim, qs);
		}
		catch(Exception e)
		{
			showError(qs.getPlayer(), e);
			return;
		}
		showResult(null, qs.getPlayer(), res);
	}
	
	public void notifyEvent(String event, QuestState qs, NpcInstance npc)
	{
		String res;
		try
		{
			res = onEvent(event, qs, npc);
		}
		catch(Exception e)
		{
			showError(qs.getPlayer(), e);
			return;
		}
		showResult(npc, qs.getPlayer(), res);
	}
	
	public void notifyKill(NpcInstance npc, QuestState qs)
	{
		String res;
		try
		{
			res = onKill(npc, qs);
		}
		catch(Exception e)
		{
			showError(qs.getPlayer(), e);
			return;
		}
		showResult(npc, qs.getPlayer(), res);
	}
	
	public void notifyKill(Player target, QuestState qs)
	{
		String res;
		try
		{
			res = onKill(target, qs);
		}
		catch(Exception e)
		{
			showError(qs.getPlayer(), e);
			return;
		}
		showResult(null, qs.getPlayer(), res);
	}
	
	public final boolean notifyFirstTalk(NpcInstance npc, Player player)
	{
		String res;
		try
		{
			res = onFirstTalk(npc, player);
		}
		catch(Exception e)
		{
			showError(player, e);
			return true;
		}
		return showResult(npc, player, res, true);
	}
	
	public boolean notifyTalk(NpcInstance npc, QuestState qs)
	{
		String res;
		try
		{
			res = onTalk(npc, qs);
		}
		catch(Exception e)
		{
			showError(qs.getPlayer(), e);
			return true;
		}
		return showResult(npc, qs.getPlayer(), res);
	}
	
	public boolean notifySkillUse(NpcInstance npc, Skill skill, QuestState qs)
	{
		String res;
		try
		{
			res = onSkillUse(npc, skill, qs);
		}
		catch(Exception e)
		{
			showError(qs.getPlayer(), e);
			return true;
		}
		return showResult(npc, qs.getPlayer(), res);
	}
	
	public void notifyCreate(QuestState qs)
	{
		try
		{
			onCreate(qs);
		}
		catch(Exception e)
		{
			showError(qs.getPlayer(), e);
		}
	}
	
	public void notifyOlympiadResult(QuestState qs, CompetitionType type, boolean isWin)
	{
		try
		{
			onOlympiadResult(qs, type, isWin);
		}
		catch(Exception e)
		{
			showError(qs.getPlayer(), e);
		}
	}
	
	public void onOlympiadResult(QuestState qs, CompetitionType type, boolean isWin)
	{
	}
	
	public void onCreate(QuestState qs)
	{
	}
	
	public String onAttack(NpcInstance npc, QuestState qs)
	{
		return null;
	}
	
	public String onDeath(Creature killer, Creature victim, QuestState qs)
	{
		return null;
	}
	
	public String onEvent(String event, QuestState qs, NpcInstance npc)
	{
		return null;
	}
	
	public String onKill(NpcInstance npc, QuestState qs)
	{
		return null;
	}
	
	public String onKill(Player killed, QuestState st)
	{
		return null;
	}
	
	public String onFirstTalk(NpcInstance npc, Player player)
	{
		return null;
	}
	
	public String onTalk(NpcInstance npc, QuestState qs)
	{
		return null;
	}
	
	public String onSkillUse(NpcInstance npc, Skill skill, QuestState qs)
	{
		return null;
	}
	
	public void onAbort(QuestState qs)
	{
	}
	
	public boolean canAbortByPacket()
	{
		return true;
	}
	
	private void showError(Player player, Throwable t)
	{
		_log.error("", t);
		if(player != null && player.isGM())
		{
			String res = "<html><body><title>Script error</title>" + LogUtils.dumpStack(t).replace("\n", "<br>") + "</body></html>";
			showResult(null, player, res);
		}
	}
	
	protected void showHtmlFile(Player player, String fileName, boolean showQuestInfo)
	{
		showHtmlFile(player, fileName, showQuestInfo, ArrayUtils.EMPTY_OBJECT_ARRAY);
	}
	
	protected void showHtmlFile(Player player, String fileName, boolean showQuestInfo, Object... arg)
	{
		if(player == null)
		{
			return;
		}
		GameObject target = player.getTarget();
		NpcHtmlMessage npcReply = new NpcHtmlMessage(target == null ? 5 : target.getObjectId());
		npcReply.setFile("quests/" + getClass().getSimpleName() + "/" + fileName);
		if(arg.length % 2 == 0)
		{
			for(int i = 0;i < arg.length;i += 2)
			{
				npcReply.replace(String.valueOf(arg[i]), String.valueOf(arg[i + 1]));
			}
		}
		player.sendPacket(npcReply);
	}
	
	protected void showSimpleHtmFile(Player player, String fileName)
	{
		if(player == null)
		{
			return;
		}
		NpcHtmlMessage npcReply = new NpcHtmlMessage(5);
		npcReply.setFile(fileName);
		player.sendPacket(npcReply);
	}
	
	private boolean showResult(NpcInstance npc, Player player, String res)
	{
		return showResult(npc, player, res, false);
	}
	
	private boolean showResult(NpcInstance npc, Player player, String res, boolean isFirstTalk)
	{
		boolean showQuestInfo = showQuestInfo(player);
		if(isFirstTalk)
		{
			showQuestInfo = false;
		}
		if(res == null)
		{
			return true;
		}
		if(res.isEmpty())
		{
			return false;
		}
		if(res.startsWith("no_quest") || res.equalsIgnoreCase("noquest") || res.equalsIgnoreCase("no-quest"))
		{
			showSimpleHtmFile(player, "no-quest.htm");
		}
		else if(res.equalsIgnoreCase("completed"))
		{
			showSimpleHtmFile(player, "completed-quest.htm");
		}
		else if(res.endsWith(".htm"))
		{
			showHtmlFile(player, res, showQuestInfo);
		}
		else
		{
			NpcHtmlMessage npcReply = new NpcHtmlMessage(npc == null ? 5 : npc.getObjectId());
			npcReply.setHtml(res);
			player.sendPacket(npcReply);
		}
		return true;
	}
	
	private boolean showQuestInfo(Player player)
	{
		QuestState qs = player.getQuestState(getName());
		if(qs != null && qs.getState() != 1)
		{
			return false;
		}
		return isVisible();
	}
	
	void pauseQuestTimers(QuestState qs)
	{
		if(qs.getTimers().isEmpty())
		{
			return;
		}
		for(QuestTimer timer : qs.getTimers().values())
		{
			timer.setQuestState(null);
			timer.pause();
		}
		_pausedQuestTimers.put(qs.getPlayer().getObjectId(), qs.getTimers());
	}
	
	void resumeQuestTimers(QuestState qs)
	{
		Map<String, QuestTimer> timers = _pausedQuestTimers.remove(qs.getPlayer().getObjectId());
		if(timers == null)
		{
			return;
		}
		qs.getTimers().putAll(timers);
		for(QuestTimer timer : qs.getTimers().values())
		{
			timer.setQuestState(qs);
			timer.start();
		}
	}
	
	protected String str(long i)
	{
		return String.valueOf(i);
	}
	
	public NpcInstance addSpawn(int npcId, int x, int y, int z, int heading, int randomOffset, int despawnDelay)
	{
		return addSpawn(npcId, new Location(x, y, z, heading), randomOffset, despawnDelay);
	}
	
	public NpcInstance addSpawn(int npcId, Location loc, int randomOffset, int despawnDelay)
	{
		NpcInstance result = Functions.spawn(randomOffset > 50 ? Location.findPointToStay(loc, 0, randomOffset, ReflectionManager.DEFAULT.getGeoIndex()) : loc, npcId);
		if(despawnDelay > 0 && result != null)
		{
			ThreadPoolManager.getInstance().schedule(new DeSpawnScheduleTimerTask(result), despawnDelay);
		}
		return result;
	}
	
	public boolean isVisible()
	{
		return true;
	}
	
	public QuestRates getRates()
	{
		QuestRates questRates = Config.QUEST_RATES.get(getQuestIntId());
		if(questRates == null)
		{
			questRates = new QuestRates(getQuestIntId());
			Config.QUEST_RATES.put(getQuestIntId(), questRates);
		}
		return questRates;
	}
	
	public class DeSpawnScheduleTimerTask extends RunnableImpl
	{
		NpcInstance _npc;
		
		public DeSpawnScheduleTimerTask(NpcInstance npc)
		{
			_npc = null;
			_npc = npc;
		}
		
		@Override
		public void runImpl() throws Exception
		{
			if(_npc != null)
			{
				if(_npc.getSpawn() != null)
				{
					_npc.getSpawn().deleteAll();
				}
				else
				{
					_npc.deleteMe();
				}
			}
		}
	}
}