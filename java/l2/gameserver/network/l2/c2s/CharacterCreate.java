package l2.gameserver.network.l2.c2s;

import l2.gameserver.Config;
import l2.gameserver.dao.CharacterDAO;
import l2.gameserver.data.xml.holder.SkillAcquireHolder;
import l2.gameserver.instancemanager.QuestManager;
import l2.gameserver.model.Player;
import l2.gameserver.model.SkillLearn;
import l2.gameserver.model.actor.instances.player.ShortCut;
import l2.gameserver.model.base.AcquireType;
import l2.gameserver.model.base.ClassId;
import l2.gameserver.model.base.Experience;
import l2.gameserver.model.items.ItemInstance;
import l2.gameserver.model.quest.Quest;
import l2.gameserver.network.l2.GameClient;
import l2.gameserver.network.l2.s2c.CharacterCreateFail;
import l2.gameserver.network.l2.s2c.CharacterCreateSuccess;
import l2.gameserver.network.l2.s2c.CharacterSelectionInfo;
import l2.gameserver.tables.SkillTable;
import l2.gameserver.templates.PlayerTemplate;
import l2.gameserver.templates.item.ItemTemplate;
import l2.gameserver.utils.ItemFunctions;
import l2.gameserver.utils.Util;

public class CharacterCreate extends L2GameClientPacket
{
	private String _name;
	private int _sex;
	private int _classId;
	private int _hairStyle;
	private int _hairColor;
	private int _face;
	
	public static void startInitialQuests(Player player)
	{
		for(int startQuestIdx = 0;startQuestIdx < Config.ALT_INITIAL_QUESTS.length;++startQuestIdx)
		{
			int questId = Config.ALT_INITIAL_QUESTS[startQuestIdx];
			Quest q = QuestManager.getQuest(questId);
			if(q == null)
				continue;
			q.newQuestState(player, 1);
		}
	}
	
	@Override
	protected void readImpl()
	{
		_name = readS();
		readD();
		_sex = readD();
		_classId = readD();
		readD();
		readD();
		readD();
		readD();
		readD();
		readD();
		_hairStyle = readD();
		_hairColor = readD();
		_face = readD();
	}
	
	@Override
	protected void runImpl()
	{
		for(ClassId cid : ClassId.VALUES)
		{
			if(cid.getId() != _classId || cid.getLevel() == 1)
				continue;
			return;
		}
		GameClient client = getClient();
		if(client == null)
		{
			return;
		}
		if(CharacterDAO.getInstance().accountCharNumber(getClient().getLogin()) >= 8)
		{
			sendPacket(CharacterCreateFail.REASON_TOO_MANY_CHARACTERS);
			return;
		}
		if(!Util.isMatchingRegexp(_name, Config.CNAME_TEMPLATE))
		{
			sendPacket(CharacterCreateFail.REASON_16_ENG_CHARS);
			return;
		}
		if(Util.isMatchingRegexp(_name, Config.CNAME_FORBIDDEN_PATTERN) || CharacterDAO.getInstance().getObjectIdByName(_name) > 0)
		{
			sendPacket(CharacterCreateFail.REASON_NAME_ALREADY_EXISTS);
			return;
		}
		for(String forbiddenName : Config.CNAME_FORBIDDEN_NAMES)
		{
			if(!forbiddenName.equalsIgnoreCase(_name))
				continue;
			sendPacket(CharacterCreateFail.REASON_NAME_ALREADY_EXISTS);
			return;
		}
		if(_hairStyle < 0 || _sex == 0 && _hairStyle > 4 || _sex != 0 && _hairStyle > 6)
		{
			sendPacket(CharacterCreateFail.REASON_CREATION_FAILED);
			return;
		}
		if(_face > 2 || _face < 0)
		{
			sendPacket(CharacterCreateFail.REASON_CREATION_FAILED);
			return;
		}
		if(_hairColor > 3 || _hairColor < 0)
		{
			sendPacket(CharacterCreateFail.REASON_CREATION_FAILED);
			return;
		}
		Player newChar = Player.create(_classId, _sex, getClient().getLogin(), _name, _hairStyle, _hairColor, _face);
		if(newChar == null)
		{
			return;
		}
		CharacterSelectionInfo csi = new CharacterSelectionInfo(client.getLogin(), client.getSessionKey().playOkID1);
		sendPacket(CharacterCreateSuccess.STATIC, csi);
		initNewChar(getClient(), newChar);
	}
	
	private void initNewChar(GameClient client, Player newChar)
	{
		PlayerTemplate template = newChar.getTemplate();
		Player.restoreCharSubClasses(newChar);
		if(Config.STARTING_ADENA > 0)
		{
			newChar.addAdena(Config.STARTING_ADENA);
		}
		newChar.setLoc(template.spawnLoc);
		if(Config.ALT_NEW_CHARACTER_LEVEL > 0)
		{
			newChar.getActiveClass().setExp(Experience.getExpForLevel(Config.ALT_NEW_CHARACTER_LEVEL));
		}
		if(Config.CHAR_TITLE)
		{
			newChar.setTitle(Config.ADD_CHAR_TITLE);
		}
		else
		{
			newChar.setTitle("");
		}
		for(ItemTemplate i : template.getItems())
		{
			ItemInstance item = ItemFunctions.createItem(i.getItemId());
			newChar.getInventory().addItem(item);
			if(item.getItemId() == 5588)
			{
				newChar.registerShortCut(new ShortCut(11, 0, 1, item.getObjectId(), -1, 1));
			}
			if(!item.isEquipable() || newChar.getActiveWeaponItem() != null && item.getTemplate().getType2() == 0)
				continue;
			newChar.getInventory().equipItem(item);
		}
		for(int i = 0;i < Config.STARTING_ITEMS.length;i += 2)
		{
			int itemId = Config.STARTING_ITEMS[i];
			long count = Config.STARTING_ITEMS[i + 1];
			ItemFunctions.addItem(newChar, itemId, count, false);
		}
		for(SkillLearn skill : SkillAcquireHolder.getInstance().getAvailableSkills(newChar, AcquireType.NORMAL))
		{
			newChar.addSkill(SkillTable.getInstance().getInfo(skill.getId(), skill.getLevel()), true);
		}
		if(newChar.getSkillLevel(1001) > 0)
		{
			newChar.registerShortCut(new ShortCut(1, 0, 2, 1001, 1, 1));
		}
		if(newChar.getSkillLevel(1177) > 0)
		{
			newChar.registerShortCut(new ShortCut(1, 0, 2, 1177, 1, 1));
		}
		if(newChar.getSkillLevel(1216) > 0)
		{
			newChar.registerShortCut(new ShortCut(2, 0, 2, 1216, 1, 1));
		}
		newChar.registerShortCut(new ShortCut(0, 0, 3, 2, -1, 1));
		newChar.registerShortCut(new ShortCut(3, 0, 3, 5, -1, 1));
		newChar.registerShortCut(new ShortCut(10, 0, 3, 0, -1, 1));
		startInitialQuests(newChar);
		newChar.setCurrentHpMp(newChar.getMaxHp(), newChar.getMaxMp());
		newChar.setCurrentCp(0.0);
		newChar.setOnlineStatus(false);
		newChar.store(false);
		newChar.getInventory().store();
		newChar.deleteMe();
		client.setCharSelection(CharacterSelectionInfo.loadCharacterSelectInfo(client.getLogin()));
	}
}