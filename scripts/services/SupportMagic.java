package services;

import l2.gameserver.Config;
import l2.gameserver.model.Creature;
import l2.gameserver.model.Player;
import l2.gameserver.model.Skill;
import l2.gameserver.model.base.Race;
import l2.gameserver.model.instances.NpcInstance;
import l2.gameserver.network.l2.s2c.MagicSkillUse;
import l2.gameserver.scripts.Functions;
import l2.gameserver.scripts.ScriptFile;
import l2.gameserver.tables.SkillTable;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class SupportMagic extends Functions implements ScriptFile
{
	private static final Logger LOG = LoggerFactory.getLogger(SupportMagic.class);
	private static final File NEWBIE_BUFFS_XML_FILE = new File(Config.DATAPACK_ROOT, "data/newbie_buffs.xml");
	private static List<NewbieBuffsList> NEWBIE_BUFFS;
	
	public static void doSupportMagic(NpcInstance npc, Player player, boolean servitor)
	{
		if(player.isCursedWeaponEquipped())
		{
			return;
		}
		for(NewbieBuffsList newbieBuffsList : NEWBIE_BUFFS)
		{
			if(!newbieBuffsList.getType().canUse(player))
				continue;
			int lvl = player.getLevel();
			if(lvl < newbieBuffsList.getMinLevel())
			{
				show("default/newbie_nosupport_min.htm", player, npc, (Object[]) new Object[] {"%min_level%", newbieBuffsList.getMinLevel()});
				return;
			}
			if(lvl > newbieBuffsList.getMaxLevel())
			{
				show("default/newbie_nosupport_max.htm", player, npc, (Object[]) new Object[] {"%max_level%", newbieBuffsList.getMaxLevel()});
				return;
			}
			newbieBuffsList.apply(npc, player);
			return;
		}
	}
	
	private static NewbieBuffsList parseNewbieBuffsList(NewbieBuffsListType type, Node newbieBuffsListNode)
	{
		int listMaxLevel;
		int listMinLevel = listMaxLevel = Integer.parseInt(newbieBuffsListNode.getAttributes().getNamedItem("max_level").getNodeValue());
		ArrayList<Pair> buffsList = new ArrayList<>();
		for(Node newbieBuffsListEntryNode = newbieBuffsListNode.getFirstChild();newbieBuffsListEntryNode != null;newbieBuffsListEntryNode = newbieBuffsListEntryNode.getNextSibling())
		{
			if(!"buff".equalsIgnoreCase(newbieBuffsListEntryNode.getNodeName()))
				continue;
			NamedNodeMap buffAttrs = newbieBuffsListEntryNode.getAttributes();
			int skillId = Integer.parseInt(buffAttrs.getNamedItem("skill_id").getNodeValue());
			int skillLevel = Integer.parseInt(buffAttrs.getNamedItem("skill_level").getNodeValue());
			int minLevel = Integer.parseInt(buffAttrs.getNamedItem("min_level").getNodeValue());
			if(minLevel < listMinLevel)
			{
				listMinLevel = minLevel;
			}
			Pair newbieBuffPair = Pair.of((Object) minLevel, (Object) SkillTable.getInstance().getInfo(skillId, skillLevel));
			buffsList.add(newbieBuffPair);
		}
		return new NewbieBuffsList(type, listMinLevel, listMaxLevel, buffsList.toArray(new Pair[buffsList.size()]));
	}
	
	private static List<NewbieBuffsList> parseNewbieBuffsDocument(Document newbieBuffsDoc)
	{
		ArrayList<NewbieBuffsList> result = new ArrayList<>();
		for(Node newbieBuffsListRoot = newbieBuffsDoc.getFirstChild();newbieBuffsListRoot != null;newbieBuffsListRoot = newbieBuffsListRoot.getNextSibling())
		{
			if(!"list".equalsIgnoreCase(newbieBuffsListRoot.getNodeName()))
				continue;
			for(Node newbieBuffsListNode = newbieBuffsListRoot.getFirstChild();newbieBuffsListNode != null;newbieBuffsListNode = newbieBuffsListNode.getNextSibling())
			{
				if("warrior".equalsIgnoreCase(newbieBuffsListNode.getNodeName()))
				{
					NewbieBuffsList warriorBuffsList = parseNewbieBuffsList(NewbieBuffsListType.WARRIOR, newbieBuffsListNode);
					result.add(warriorBuffsList);
					continue;
				}
				if(!"mage".equalsIgnoreCase(newbieBuffsListNode.getNodeName()))
					continue;
				NewbieBuffsList mageBuffsList = parseNewbieBuffsList(NewbieBuffsListType.MAGE, newbieBuffsListNode);
				result.add(mageBuffsList);
			}
		}
		return Collections.unmodifiableList(result);
	}
	
	private static List<NewbieBuffsList> parseNewbieBuffs(File newbieBuffsXmlFile)
	{
		Document newbieBuffsDoc;
		try
		{
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			factory.setValidating(false);
			factory.setIgnoringComments(true);
			newbieBuffsDoc = factory.newDocumentBuilder().parse(newbieBuffsXmlFile);
		}
		catch(Exception e)
		{
			LOG.error("Error loading file " + newbieBuffsXmlFile, e);
			return Collections.emptyList();
		}
		try
		{
			List<NewbieBuffsList> result = parseNewbieBuffsDocument(newbieBuffsDoc);
			int loadBuffsCnt = 0;
			for(NewbieBuffsList nbl : result)
			{
				loadBuffsCnt += nbl.getBuffs().length;
			}
			LOG.info("SupportMagic: Loaded " + loadBuffsCnt + " newbie buff(s).");
			return result;
		}
		catch(Exception e)
		{
			LOG.error("Error in file " + newbieBuffsXmlFile, e);
			return Collections.emptyList();
		}
	}
	
	public void getSupportMagic()
	{
		Player player = getSelf();
		NpcInstance npc = getNpc();
		doSupportMagic(npc, player, false);
	}
	
	public void getSupportServitorMagic()
	{
		Player player = getSelf();
		NpcInstance npc = getNpc();
		doSupportMagic(npc, player, true);
	}
	
	public void getProtectionBlessing()
	{
		Player player = getSelf();
		NpcInstance npc = getNpc();
		if(player.getKarma() > 0)
		{
			return;
		}
		if(player.getLevel() > 39 || player.getClassId().getLevel() >= 3)
		{
			show("default/newbie_blessing_no.htm", player, npc, (Object[]) new Object[0]);
			return;
		}
		npc.doCast(SkillTable.getInstance().getInfo(5182, 1), player, true);
	}
	
	@Override
	public void onLoad()
	{
		NEWBIE_BUFFS = parseNewbieBuffs(NEWBIE_BUFFS_XML_FILE);
	}
	
	@Override
	public void onReload()
	{
		onLoad();
	}
	
	@Override
	public void onShutdown()
	{
	}
	
	private enum NewbieBuffsListType
	{
		WARRIOR
				{
					@Override
					public boolean canUse(Player player)
					{
						return !player.isMageClass() || player.getTemplate().race == Race.orc;
					}
				},
		MAGE
				{
					@Override
					public boolean canUse(Player player)
					{
						return player.isMageClass() && player.getTemplate().race != Race.orc;
					}
				};
		
		
		public abstract boolean canUse(Player player);
	}
	
	private static class NewbieBuffsList
	{
		private final NewbieBuffsListType _type;
		private final int _minLevel;
		private final int _maxLevel;
		private final Pair<Integer, Skill>[] _buffs;
		
		private NewbieBuffsList(NewbieBuffsListType type, int minLevel, int maxLevel, Pair<Integer, Skill>[] buffs)
		{
			_type = type;
			_minLevel = minLevel;
			_maxLevel = maxLevel;
			_buffs = buffs;
		}
		
		public NewbieBuffsListType getType()
		{
			return _type;
		}
		
		public int getMinLevel()
		{
			return _minLevel;
		}
		
		public int getMaxLevel()
		{
			return _maxLevel;
		}
		
		public Pair<Integer, Skill>[] getBuffs()
		{
			return _buffs;
		}
		
		public void apply(Creature buffer, Creature target)
		{
			int lvl = target.getLevel();
			for(Pair<Integer, Skill> newbieBuffPair : getBuffs())
			{
				if(lvl < newbieBuffPair.getKey())
					continue;
				Skill newbieBuffSkill = newbieBuffPair.getValue();
				buffer.broadcastPacket(new MagicSkillUse(buffer, target, newbieBuffSkill, 0, 0));
				buffer.callSkill(newbieBuffSkill, Collections.singletonList(target), true);
			}
		}
	}
}