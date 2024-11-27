package l2.gameserver.model.entity.oly;

import l2.commons.dbutils.DbUtils;
import l2.gameserver.Config;
import l2.gameserver.data.StringHolder;
import l2.gameserver.database.DatabaseFactory;
import l2.gameserver.database.mysql;
import l2.gameserver.model.GameObjectsStorage;
import l2.gameserver.model.Player;
import l2.gameserver.model.base.ClassId;
import l2.gameserver.model.entity.HeroDiary;
import l2.gameserver.model.items.ItemInstance;
import l2.gameserver.model.pledge.Alliance;
import l2.gameserver.model.pledge.Clan;
import l2.gameserver.network.l2.s2c.NpcHtmlMessage;
import l2.gameserver.network.l2.s2c.SocialAction;
import l2.gameserver.network.l2.s2c.SystemMessage;
import l2.gameserver.tables.ClanTable;
import l2.gameserver.tables.SkillTable;
import l2.gameserver.utils.Log;
import l2.gameserver.utils.Strings;
import org.apache.commons.lang3.mutable.MutableInt;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class HeroController
{
	public static final int[] HERO_WEAPONS = {6611, 6612, 6613, 6614, 6615, 6616, 6617, 6618, 6619, 6620, 6621, 9388, 9389, 9390};
	private static final Logger _log = LoggerFactory.getLogger(HeroController.class);
	private static final String SQL_GET_HEROES = "SELECT  `oly_heroes`.`char_id` AS `char_id`, `oly_nobles`.`char_name` AS `name`, `oly_nobles`.`class_id` AS `class_id`, `oly_heroes`.`count` AS `count`, `oly_heroes`.`played` AS `played`, `oly_heroes`.`active` AS `active`, `oly_heroes`.`message` AS `message` FROM    `oly_heroes`,`oly_nobles` WHERE   `oly_heroes`.`char_id` = `oly_nobles`.`char_id`";
	private static final String SQL_SET_HEROES = "REPLACE INTO `oly_heroes` (`char_id`, `count`, `played`, `active`, `message`) VALUES (?, ?, ?, ?, ?)";
	private static HeroController _instance;
	private static Map<Integer, List<HeroDiary>> _herodiary;
	private static Map<Integer, String> _heroMessage;
	private final ArrayList<HeroRecord> _currentHeroes = new ArrayList();
	private final ArrayList<HeroRecord> _allHeroes = new ArrayList();
	
	private HeroController()
	{
		_herodiary = new ConcurrentHashMap<>();
		_heroMessage = new ConcurrentHashMap<>();
		loadHeroes();
	}
	
	public static HeroController getInstance()
	{
		if(_instance == null)
		{
			_instance = new HeroController();
		}
		return _instance;
	}
	
	public static void addSkills(Player player)
	{
		player.addSkill(SkillTable.getInstance().getInfo(395, 1));
		player.addSkill(SkillTable.getInstance().getInfo(396, 1));
		player.addSkill(SkillTable.getInstance().getInfo(1374, 1));
		player.addSkill(SkillTable.getInstance().getInfo(1375, 1));
		player.addSkill(SkillTable.getInstance().getInfo(1376, 1));
	}
	
	public static void removeSkills(Player player)
	{
		player.removeSkillById(395);
		player.removeSkillById(396);
		player.removeSkillById(1374);
		player.removeSkillById(1375);
		player.removeSkillById(1376);
	}
	
	public static boolean isHaveHeroWeapon(Player player)
	{
		for(int iid : HERO_WEAPONS)
		{
			if(player.getInventory().getCountOf(iid) <= 0)
				continue;
			return true;
		}
		return false;
	}
	
	private static void removeAllHeroWeapons(Player player)
	{
		boolean removed = false;
		for(int itemId : HERO_WEAPONS)
		{
			while(player.getInventory().destroyItemByItemId(itemId, 1))
			{
				removed = true;
			}
			if(removed)
			{
				player.sendPacket(new SystemMessage(1726).addItemName(itemId));
			}
			removed = false;
		}
	}
	
	public static void checkHeroWeaponary(Player player)
	{
		if(!player.isHero())
		{
			removeAllHeroWeapons(player);
		}
	}
	
	private synchronized Collection<NoblesController.NobleRecord> CalcHeroesContenders()
	{
		_log.info("HeroController: Calculating heroes contenders.");
		HashMap<ClassId, NoblesController.NobleRecord> hero_contenders_map = new HashMap<>();
		for(NoblesController.NobleRecord nr : NoblesController.getInstance().getNoblesRecords())
		{
			try
			{
				if(nr.comp_done < Config.OLY_MIN_HERO_COMPS || nr.comp_win < Config.OLY_MIN_HERO_WIN)
					continue;
				ClassId cid = null;
				for(ClassId cid2 : ClassId.values())
				{
					if(cid2.getId() != nr.class_id || cid2.level() != 3)
						continue;
					cid = cid2;
				}
				if(cid == null)
				{
					_log.warn("HeroController: Not third or null ClassID for character '" + nr.char_name + "'");
					continue;
				}
				if(hero_contenders_map.containsKey(cid))
				{
					NoblesController.NobleRecord nr2 = hero_contenders_map.get(cid);
					if(nr.points_current <= nr2.points_current && (nr.points_current != nr2.points_current || nr.comp_win <= nr2.comp_win))
						continue;
					hero_contenders_map.put(cid, nr);
					continue;
				}
				hero_contenders_map.put(cid, nr);
			}
			catch(Exception e)
			{
				_log.warn("HeroController: Exception while claculating new heroes", e);
			}
		}
		for(NoblesController.NobleRecord nr : hero_contenders_map.values())
		{
			Log.add(String.format("HeroController: %s(%d) pretended to be a hero. points_current = %d", nr.char_name, nr.char_id, nr.points_current), "olympiad");
		}
		ArrayList<NoblesController.NobleRecord> result = new ArrayList<>();
		result.addAll(hero_contenders_map.values());
		return result;
	}
	
	private void loadHeroes()
	{
		Connection conn = null;
		Statement stmt = null;
		ResultSet rset = null;
		try
		{
			conn = DatabaseFactory.getInstance().getConnection();
			stmt = conn.createStatement();
			rset = stmt.executeQuery("SELECT  `oly_heroes`.`char_id` AS `char_id`, `oly_nobles`.`char_name` AS `name`, `oly_nobles`.`class_id` AS `class_id`, `oly_heroes`.`count` AS `count`, `oly_heroes`.`played` AS `played`, `oly_heroes`.`active` AS `active`, `oly_heroes`.`message` AS `message` FROM    `oly_heroes`,`oly_nobles` WHERE   `oly_heroes`.`char_id` = `oly_nobles`.`char_id`");
			while(rset.next())
			{
				int char_id = rset.getInt("char_id");
				String name = rset.getString("name");
				int class_id = rset.getInt("class_id");
				int count = rset.getInt("count");
				boolean played = rset.getInt("played") != 0;
				boolean active = rset.getInt("active") != 0;
				String message = rset.getString("message");
				HeroRecord nr = new HeroRecord(char_id, name, class_id, count, active, played, message);
				if(played)
				{
					_currentHeroes.add(nr);
				}
				_allHeroes.add(nr);
			}
			applyClanAndAlly();
		}
		catch(Exception e)
		{
			_log.warn("Exception while loading heroes", e);
		}
		finally
		{
			DbUtils.closeQuietly(conn, stmt, rset);
		}
	}
	
	public void saveHeroes()
	{
		Connection conn = null;
		PreparedStatement pstmt = null;
		try
		{
			conn = DatabaseFactory.getInstance().getConnection();
			pstmt = conn.prepareStatement("REPLACE INTO `oly_heroes` (`char_id`, `count`, `played`, `active`, `message`) VALUES (?, ?, ?, ?, ?)");
			for(HeroRecord nr : _allHeroes)
			{
				pstmt.setInt(1, nr.char_id);
				pstmt.setInt(2, nr.count);
				pstmt.setInt(3, nr.played ? 1 : 0);
				pstmt.setInt(4, nr.active ? 1 : 0);
				pstmt.setString(5, nr.message);
				pstmt.executeUpdate();
			}
		}
		catch(Exception e)
		{
			_log.warn("Exception while saving heroes", e);
		}
		finally
		{
			DbUtils.closeQuietly(conn, pstmt);
		}
	}
	
	private void clearHeroes()
	{
		_log.info("HeroController: Clearing previus season heroes.");
		mysql.set("UPDATE `oly_heroes` SET `played` = 0, `active` = 0");
		if(!_currentHeroes.isEmpty())
		{
			for(HeroRecord nr : _currentHeroes)
			{
				if(!nr.active)
					continue;
				Player player = GameObjectsStorage.getPlayer(nr.char_id);
				if(player != null)
				{
					player.getInventory().unEquipItemInBodySlot(256);
					player.getInventory().unEquipItemInBodySlot(128);
					player.getInventory().unEquipItemInBodySlot(16384);
					player.getInventory().unEquipItemInBodySlot(65536);
					player.getInventory().unEquipItemInBodySlot(524288);
					player.getInventory().unEquipItemInBodySlot(262144);
					for(ItemInstance item : player.getInventory().getItems())
					{
						if(item == null || !item.isHeroWeapon())
							continue;
						player.getInventory().destroyItem(item);
					}
					for(ItemInstance item : player.getWarehouse().getItems())
					{
						if(item == null || item.isEquipable() || !item.isHeroWeapon())
							continue;
						player.getWarehouse().destroyItem(item);
					}
					player.unsetVar("CustomHeroEndTime");
					player.setHero(false);
					player.updatePledgeClass();
					player.broadcastUserInfo(true);
					removeAllHeroWeapons(player);
				}
				nr.played = false;
				nr.active = false;
			}
		}
		saveHeroes();
		_currentHeroes.clear();
	}
	
	public synchronized void ComputeNewHeroNobleses()
	{
		_log.info("HeroController: Computing new heroes.");
		try
		{
			NoblesController.getInstance().SaveNobleses();
			saveHeroes();
			Collection<NoblesController.NobleRecord> hContenders = CalcHeroesContenders();
			clearHeroes();
			for(NoblesController.NobleRecord hnr : hContenders)
			{
				HeroRecord hr = null;
				for(HeroRecord hr2 : _allHeroes)
				{
					if(hnr.char_id != hr2.char_id)
						continue;
					hr = hr2;
				}
				if(hr == null)
				{
					hr = new HeroRecord(hnr.char_id, hnr.char_name, hnr.class_id, 0, false, true, "");
					_allHeroes.add(hr);
				}
				++hr.count;
				hr.played = true;
				_currentHeroes.add(hr);
			}
			saveHeroes();
			NoblesController.getInstance().TransactNewSeason();
			NoblesController.getInstance().ComputeRanks();
			NoblesController.getInstance().SaveNobleses();
			applyClanAndAlly();
		}
		catch(Exception e)
		{
			_log.warn("HeroController: Can't compute heroes.", e);
		}
	}
	
	private void applyClanAndAlly()
	{
		for(HeroRecord hr : _currentHeroes)
		{
			if(hr == null)
				continue;
			Map.Entry<Clan, Alliance> e = ClanTable.getInstance().getClanAndAllianceByCharId(hr.char_id);
			if(e.getKey() != null)
			{
				hr.clan_name = e.getKey().getName();
				hr.clan_crest = e.getKey().getCrestId();
			}
			else
			{
				hr.clan_name = "";
				hr.clan_crest = 0;
			}
			if(e.getValue() != null)
			{
				hr.ally_name = e.getValue().getAllyName();
				hr.ally_crest = e.getValue().getAllyCrestId();
				continue;
			}
			hr.ally_name = "";
			hr.ally_crest = 0;
		}
	}
	
	public Collection<HeroRecord> getCurrentHeroes()
	{
		return _currentHeroes;
	}
	
	public boolean isCurrentHero(Player player)
	{
		if(player == null)
		{
			return false;
		}
		if(_currentHeroes.isEmpty())
		{
			return false;
		}
		return isCurrentHero(player.getObjectId());
	}
	
	public boolean isInactiveHero(Player player)
	{
		if(player == null)
		{
			return false;
		}
		return isInactiveHero(player.getObjectId());
	}
	
	public void activateHero(Player player)
	{
		if(player == null)
		{
			return;
		}
		if(_currentHeroes.isEmpty())
		{
			return;
		}
		for(HeroRecord hr : _currentHeroes)
		{
			if(hr.char_id != player.getObjectId() || !hr.played)
				continue;
			hr.active = true;
			if(player.getBaseClassId() == player.getActiveClassId())
			{
				addSkills(player);
			}
			player.setHero(true);
			player.unsetVar("CustomHeroEndTime");
			player.broadcastPacket(new SocialAction(player.getObjectId(), 16));
			player.updatePledgeClass();
			player.getPlayer().sendUserInfo(true);
			if(player.getClan() != null && player.getClan().getLevel() >= 5)
			{
				player.getClan().incReputation(1000, true, "Hero:activateHero:" + player);
				player.getClan().broadcastToOtherOnlineMembers(new SystemMessage(1776).addString(player.getName()).addNumber(Math.round(1000.0 * Config.RATE_CLAN_REP_SCORE)), player);
			}
			player.broadcastUserInfo(true);
			saveHeroes();
		}
	}
	
	public boolean isCurrentHero(int obj_id)
	{
		if(_currentHeroes.isEmpty())
		{
			return false;
		}
		for(HeroRecord hr : _currentHeroes)
		{
			if(hr.char_id != obj_id)
				continue;
			return hr.active && hr.played;
		}
		return false;
	}
	
	public boolean isInactiveHero(int obj_id)
	{
		if(_currentHeroes.isEmpty())
		{
			return false;
		}
		for(HeroRecord hr : _currentHeroes)
		{
			if(hr.char_id != obj_id)
				continue;
			return hr.played && !hr.active;
		}
		return false;
	}
	
	public void showHistory(Player player, int targetClassId, int page)
	{
		int perpage = 15;
		HeroRecord hr = null;
		for(HeroRecord hr2 : getCurrentHeroes())
		{
			if(!hr2.active || !hr2.played || hr2.class_id != targetClassId)
				continue;
			hr = hr2;
		}
		if(hr == null)
		{
			return;
		}
		NpcHtmlMessage html = new NpcHtmlMessage(player, null);
		html.setFile("oly/monument_hero_info.htm");
		html.replace("%title%", StringHolder.getInstance().getNotNull(player, "hero.history"));
		Collection<CompetitionController.CompetitionResults> crs_list = hr.getCompetitions();
		CompetitionController.CompetitionResults[] crs = crs_list.toArray(new CompetitionController.CompetitionResults[crs_list.size()]);
		int allStatWinner = 0;
		int allStatLoss = 0;
		int allStatTie = 0;
		for(CompetitionController.CompetitionResults h : crs)
		{
			if(h.result > 0)
			{
				++allStatWinner;
				continue;
			}
			if(h.result < 0)
			{
				++allStatLoss;
				continue;
			}
			if(h.result != 0)
				continue;
			++allStatTie;
		}
		html.replace("%wins%", String.valueOf(allStatWinner));
		html.replace("%ties%", String.valueOf(allStatTie));
		html.replace("%losses%", String.valueOf(allStatLoss));
		int min = 15 * (page - 1);
		int max = 15 * page;
		MutableInt currentWinner = new MutableInt(0);
		MutableInt currentLoss = new MutableInt(0);
		MutableInt currentTie = new MutableInt(0);
		StringBuilder b = new StringBuilder(500);
		for(int i = 0;i < crs.length;++i)
		{
			CompetitionController.CompetitionResults h = crs[i];
			if(h.result > 0)
			{
				currentWinner.increment();
			}
			else if(h.result < 0)
			{
				currentLoss.increment();
			}
			else if(h.result == 0)
			{
				currentTie.increment();
			}
			if(i < min)
				continue;
			if(i >= max)
				break;
			b.append("<tr><td>");
			b.append(h.toString(player, currentWinner, currentLoss, currentTie));
			b.append("</td></tr");
		}
		if(min > 0)
		{
			html.replace("%buttprev%", "<button value=\"&$1037;\" action=\"bypass %prev_bypass%\" width=65 height=20 back=\"l2ui_ch3.smallbutton2_down\" fore=\"l2ui_ch3.smallbutton2\">");
			html.replace("%prev_bypass%", "_match?class=" + targetClassId + "&page=" + (page - 1));
		}
		else
		{
			html.replace("%buttprev%", "");
		}
		if(crs.length > max)
		{
			html.replace("%buttnext%", "<button value=\"&$1038;\" action=\"bypass %next_bypass%\" width=65 height=20 back=\"l2ui_ch3.smallbutton2_down\" fore=\"l2ui_ch3.smallbutton2\">");
			html.replace("%prev_bypass%", "_match?class=" + targetClassId + "&page=" + (page + 1));
		}
		else
		{
			html.replace("%buttnext%", "");
		}
		html.replace("%list%", b.toString());
		player.sendPacket(html);
	}
	
	public void loadDiary(int charId)
	{
		Connection con = null;
		PreparedStatement statement = null;
		ResultSet rset = null;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("SELECT * FROM  heroes_diary WHERE charId=? ORDER BY time ASC");
			statement.setInt(1, charId);
			rset = statement.executeQuery();
			ArrayList<HeroDiary> diary = new ArrayList<>();
			while(rset.next())
			{
				long time = rset.getLong("time");
				int action = rset.getInt("action");
				int param = rset.getInt("param");
				HeroDiary d = new HeroDiary(action, time, param);
				diary.add(d);
			}
			_herodiary.put(charId, diary);
			if(Config.DEBUG)
			{
				_log.info("Hero System: Loaded " + diary.size() + " diary entries for Hero(object id: #" + charId + ")");
			}
		}
		catch(SQLException e)
		{
			_log.warn("Hero System: Couldnt load Hero Diary for CharId: " + charId, e);
		}
		finally
		{
			DbUtils.closeQuietly(con, statement, rset);
		}
	}
	
	public void showHeroDiary(Player activeChar, int heroclass, int page)
	{
		int perpage = 10;
		HeroRecord hr = null;
		for(HeroRecord hr2 : getCurrentHeroes())
		{
			if(!hr2.active || !hr2.played || hr2.class_id != heroclass)
				continue;
			hr = hr2;
		}
		if(hr == null)
		{
			return;
		}
		List<HeroDiary> mainlist = _herodiary.get(hr.char_id);
		if(mainlist != null)
		{
			NpcHtmlMessage html = new NpcHtmlMessage(activeChar, null);
			html.setFile("oly/monument_hero_info.htm");
			html.replace("%title%", StringHolder.getInstance().getNotNull(activeChar, "hero.diary"));
			html.replace("%heroname%", hr.name);
			html.replace("%message%", hr.message);
			ArrayList<HeroDiary> list = new ArrayList<>(mainlist);
			Collections.reverse(list);
			boolean color = true;
			StringBuilder fList = new StringBuilder(500);
			int counter = 0;
			int breakat = 0;
			for(int i = (page - 1) * 10;i < list.size();++i)
			{
				breakat = i;
				HeroDiary diary = list.get(i);
				Map.Entry<String, String> entry = diary.toString(activeChar);
				fList.append("<tr><td>");
				if(color)
				{
					fList.append("<table width=270 bgcolor=\"131210\">");
				}
				else
				{
					fList.append("<table width=270>");
				}
				fList.append("<tr><td width=270><font color=\"LEVEL\">" + entry.getKey() + "</font></td></tr>");
				fList.append("<tr><td width=270>" + entry.getValue() + "</td></tr>");
				fList.append("<tr><td>&nbsp;</td></tr></table>");
				fList.append("</td></tr>");
				boolean bl = color = !color;
				if(++counter >= 10)
					break;
			}
			if(breakat < list.size() - 1)
			{
				html.replace("%buttprev%", "<button value=\"&$1037;\" action=\"bypass %prev_bypass%\" width=65 height=20 back=\"l2ui_ch3.smallbutton2_down\" fore=\"l2ui_ch3.smallbutton2\">");
				html.replace("%prev_bypass%", "_diary?class=" + heroclass + "&page=" + (page + 1));
			}
			else
			{
				html.replace("%buttprev%", "");
			}
			if(page > 1)
			{
				html.replace("%buttnext%", "<button value=\"&$1038;\" action=\"bypass %next_bypass%\" width=65 height=20 back=\"l2ui_ch3.smallbutton2_down\" fore=\"l2ui_ch3.smallbutton2\">");
				html.replace("%next_bypass%", "_diary?class=" + heroclass + "&page=" + (page - 1));
			}
			else
			{
				html.replace("%buttnext%", "");
			}
			html.replace("%list%", fList.toString());
			activeChar.sendPacket(html);
		}
	}
	
	public void addHeroDiary(int playerId, int id, int param)
	{
		insertHeroDiary(playerId, id, param);
		List<HeroDiary> list = _herodiary.get(playerId);
		if(list != null)
		{
			list.add(new HeroDiary(id, System.currentTimeMillis(), param));
		}
	}
	
	private void insertHeroDiary(int charId, int action, int param)
	{
		Connection con = null;
		PreparedStatement statement = null;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("INSERT INTO heroes_diary (charId, time, action, param) values(?,?,?,?)");
			statement.setInt(1, charId);
			statement.setLong(2, System.currentTimeMillis());
			statement.setInt(3, action);
			statement.setInt(4, param);
			statement.execute();
			statement.close();
		}
		catch(SQLException e)
		{
			_log.error("SQL exception while saving DiaryData.", e);
		}
		finally
		{
			DbUtils.closeQuietly(con, statement);
		}
	}
	
	public void setHeroMessage(int charId, String message)
	{
		HeroRecord hr = null;
		for(HeroRecord hr2 : getCurrentHeroes())
		{
			if(!hr2.active || !hr2.played || hr2.char_id != charId)
				continue;
			hr = hr2;
		}
		hr.message = Strings.stripSlashes(message);
	}
	
	public class HeroRecord
	{
		public int char_id;
		public int class_id;
		public int count;
		public boolean active;
		public boolean played;
		public String name;
		public String message;
		public String clan_name;
		public String ally_name;
		public int clan_crest;
		public int ally_crest;
		public Collection<CompetitionController.CompetitionResults> competitions;
		
		private HeroRecord(int _char_id, String _name, int _class_id, int _count, boolean _active, boolean _played, String _message)
		{
			char_id = _char_id;
			name = _name;
			count = _count;
			class_id = _class_id;
			active = _active;
			played = _played;
			message = _message;
		}
		
		public Collection<CompetitionController.CompetitionResults> getCompetitions()
		{
			if(competitions == null)
			{
				competitions = CompetitionController.getInstance().getCompetitionResults(char_id, OlyController.getInstance().getCurrentSeason() - 1);
			}
			return competitions;
		}
	}
}