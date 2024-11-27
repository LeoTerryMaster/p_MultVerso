package l2.gameserver.data.xml.parser;

import l2.commons.data.xml.AbstractDirParser;
import l2.gameserver.Config;
import l2.gameserver.data.xml.holder.ItemHolder;
import l2.gameserver.data.xml.holder.NpcHolder;
import l2.gameserver.model.Skill;
import l2.gameserver.model.TeleportLocation;
import l2.gameserver.model.base.ClassId;
import l2.gameserver.model.reward.RewardData;
import l2.gameserver.model.reward.RewardGroup;
import l2.gameserver.model.reward.RewardList;
import l2.gameserver.model.reward.RewardType;
import l2.gameserver.tables.SkillTable;
import l2.gameserver.templates.StatsSet;
import l2.gameserver.templates.npc.AbsorbInfo;
import l2.gameserver.templates.npc.Faction;
import l2.gameserver.templates.npc.MinionData;
import l2.gameserver.templates.npc.NpcTemplate;
import l2.gameserver.utils.Location;
import org.apache.commons.lang3.ArrayUtils;
import org.dom4j.Element;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;

public final class NpcParser extends AbstractDirParser<NpcHolder>
{
	private static final NpcParser _instance = new NpcParser();
	
	private NpcParser()
	{
		super(NpcHolder.getInstance());
	}
	
	public static NpcParser getInstance()
	{
		return _instance;
	}
	
	@Override
	public File getXMLDir()
	{
		return new File(Config.DATAPACK_ROOT, "data/npc/");
	}
	
	@Override
	public boolean isIgnored(File f)
	{
		return false;
	}
	
	@Override
	public String getDTDFileName()
	{
		return "npc.dtd";
	}
	
	@Override
	protected void readData(Element rootElement) throws Exception
	{
		Iterator npcIterator = rootElement.elementIterator();
		while(npcIterator.hasNext())
		{
			Element npcElement = (Element) npcIterator.next();
			int npcId = Integer.parseInt(npcElement.attributeValue("id"));
			int templateId = npcElement.attributeValue("template_id") == null ? 0 : Integer.parseInt(npcElement.attributeValue("id"));
			String name = npcElement.attributeValue("name");
			String title = npcElement.attributeValue("title");
			StatsSet set = new StatsSet();
			set.set("npcId", npcId);
			set.set("displayId", templateId);
			set.set("name", name);
			set.set("title", title);
			set.set("baseCpReg", 0);
			set.set("baseCpMax", 0);
			Iterator firstIterator = npcElement.elementIterator();
			while(firstIterator.hasNext())
			{
				Element firstElement = (Element) firstIterator.next();
				if(firstElement.getName().equalsIgnoreCase("set"))
				{
					set.set(firstElement.attributeValue("name"), firstElement.attributeValue("value"));
					continue;
				}
				if(firstElement.getName().equalsIgnoreCase("equip"))
				{
					Iterator eIterator = firstElement.elementIterator();
					while(eIterator.hasNext())
					{
						Element eElement = (Element) eIterator.next();
						int itemId = Integer.parseInt(eElement.attributeValue("item_id"));
						if(ItemHolder.getInstance().getTemplate(itemId) == null)
						{
							_log.error("Undefined item " + itemId + " used in slot " + eElement.getName() + " of npc " + npcId);
						}
						set.set(eElement.getName(), String.valueOf(itemId));
					}
					continue;
				}
				if(firstElement.getName().equalsIgnoreCase("ai_params"))
				{
					StatsSet ai = new StatsSet();
					Iterator eIterator = firstElement.elementIterator();
					while(eIterator.hasNext())
					{
						Element eElement = (Element) eIterator.next();
						ai.set(eElement.attributeValue("name"), eElement.attributeValue("value"));
					}
					set.set("aiParams", ai);
					continue;
				}
				if(!firstElement.getName().equalsIgnoreCase("attributes"))
					continue;
				int[] attributeAttack = new int[6];
				int[] attributeDefence = new int[6];
				Iterator eIterator = firstElement.elementIterator();
				while(eIterator.hasNext())
				{
					l2.gameserver.model.base.Element element;
					Element eElement = (Element) eIterator.next();
					if(eElement.getName().equalsIgnoreCase("defence"))
					{
						element = l2.gameserver.model.base.Element.getElementByName(eElement.attributeValue("attribute"));
						attributeDefence[element.getId()] = Integer.parseInt(eElement.attributeValue("value"));
						continue;
					}
					if(!eElement.getName().equalsIgnoreCase("attack"))
						continue;
					element = l2.gameserver.model.base.Element.getElementByName(eElement.attributeValue("attribute"));
					attributeAttack[element.getId()] = Integer.parseInt(eElement.attributeValue("value"));
				}
				set.set("baseAttributeAttack", attributeAttack);
				set.set("baseAttributeDefence", attributeDefence);
			}
			NpcTemplate template = new NpcTemplate(set);
			LinkedList<TeleportLocation> teleportLocations = new LinkedList<>();
			Iterator secondIterator = npcElement.elementIterator();
			while(secondIterator.hasNext())
			{
				Element nextElement;
				Element secondElement = (Element) secondIterator.next();
				String nodeName = secondElement.getName();
				if(nodeName.equalsIgnoreCase("faction"))
				{
					String factionId = secondElement.attributeValue("name");
					Faction faction = new Faction(factionId);
					int factionRange = Integer.parseInt(secondElement.attributeValue("range"));
					faction.setRange(factionRange);
					Iterator nextIterator2 = secondElement.elementIterator();
					while(nextIterator2.hasNext())
					{
						nextElement = (Element) nextIterator2.next();
						int ignoreId = Integer.parseInt(nextElement.attributeValue("npc_id"));
						faction.addIgnoreNpcId(ignoreId);
					}
					template.setFaction(faction);
					continue;
				}
				if(nodeName.equalsIgnoreCase("rewardlist"))
				{
					RewardType type = RewardType.valueOf(secondElement.attributeValue("type"));
					boolean autoLoot = secondElement.attributeValue("auto_loot") != null && Boolean.parseBoolean(secondElement.attributeValue("auto_loot"));
					RewardList list = new RewardList(type, autoLoot);
					Iterator nextIterator2 = secondElement.elementIterator();
					while(nextIterator2.hasNext())
					{
						nextElement = (Element) nextIterator2.next();
						String nextName = nextElement.getName();
						if(nextName.equalsIgnoreCase("group"))
						{
							double enterChance = nextElement.attributeValue("chance") == null ? 1000000.0 : Double.parseDouble(nextElement.attributeValue("chance")) * 10000.0;
							RewardGroup group = type == RewardType.SWEEP || type == RewardType.NOT_RATED_NOT_GROUPED ? null : new RewardGroup(enterChance);
							Iterator rewardIterator = nextElement.elementIterator();
							while(rewardIterator.hasNext())
							{
								Element rewardElement = (Element) rewardIterator.next();
								RewardData data = parseReward(rewardElement, type);
								if(data == null)
									continue;
								if(type == RewardType.SWEEP || type == RewardType.NOT_RATED_NOT_GROUPED)
								{
									warn("Can't load rewardlist from group: " + npcId + "; type: " + type);
									continue;
								}
								group.addData(data);
							}
							if(group == null)
								continue;
							list.add(group);
							continue;
						}
						if(!nextName.equalsIgnoreCase("reward"))
							continue;
						if(type != RewardType.SWEEP && type != RewardType.NOT_RATED_NOT_GROUPED)
						{
							warn("Reward can't be without group(and not grouped): " + npcId + "; type: " + type);
							continue;
						}
						RewardData data = parseReward(nextElement, type);
						if(data == null)
							continue;
						RewardGroup g = new RewardGroup(1000000.0);
						g.addData(data);
						list.add(g);
					}
					if(!(type != RewardType.RATED_GROUPED && type != RewardType.NOT_RATED_GROUPED || list.validate()))
					{
						warn("Problems with rewardlist for npc: " + npcId + "; type: " + type);
					}
					template.putRewardList(type, list);
					continue;
				}
				int id;
				Iterator nextIterator;
				if(nodeName.equalsIgnoreCase("skills"))
				{
					nextIterator = secondElement.elementIterator();
					while(nextIterator.hasNext())
					{
						Element nextElement22 = (Element) nextIterator.next();
						id = Integer.parseInt(nextElement22.attributeValue("id"));
						int level = Integer.parseInt(nextElement22.attributeValue("level"));
						if(SkillTable.getInstance().getInfo(id, level) == null)
						{
							_log.error("Undefined id " + id + " and level " + level + " of npc " + npcId);
						}
						if(id == 4416)
						{
							template.setRace(level);
						}
						Skill skill;
						if((skill = SkillTable.getInstance().getInfo(id, level)) == null)
							continue;
						template.addSkill(skill);
					}
					continue;
				}
				Element nextElement2;
				if(nodeName.equalsIgnoreCase("minions"))
				{
					nextIterator = secondElement.elementIterator();
					while(nextIterator.hasNext())
					{
						nextElement2 = (Element) nextIterator.next();
						id = Integer.parseInt(nextElement2.attributeValue("npc_id"));
						int count = Integer.parseInt(nextElement2.attributeValue("count"));
						template.addMinion(new MinionData(id, count));
					}
					continue;
				}
				if(nodeName.equalsIgnoreCase("teach_classes"))
				{
					nextIterator = secondElement.elementIterator();
					while(nextIterator.hasNext())
					{
						nextElement2 = (Element) nextIterator.next();
						id = Integer.parseInt(nextElement2.attributeValue("id"));
						template.addTeachInfo(ClassId.VALUES[id]);
					}
					continue;
				}
				if(nodeName.equalsIgnoreCase("absorblist"))
				{
					nextIterator = secondElement.elementIterator();
					while(nextIterator.hasNext())
					{
						nextElement2 = (Element) nextIterator.next();
						int chance = Integer.parseInt(nextElement2.attributeValue("chance"));
						int cursedChance = nextElement2.attributeValue("cursed_chance") == null ? 0 : Integer.parseInt(nextElement2.attributeValue("cursed_chance"));
						int minLevel = Integer.parseInt(nextElement2.attributeValue("min_level"));
						int maxLevel = Integer.parseInt(nextElement2.attributeValue("max_level"));
						boolean skill = nextElement2.attributeValue("skill") != null && Boolean.parseBoolean(nextElement2.attributeValue("skill"));
						AbsorbInfo.AbsorbType absorbType = AbsorbInfo.AbsorbType.valueOf(nextElement2.attributeValue("type"));
						template.addAbsorbInfo(new AbsorbInfo(skill, absorbType, chance, cursedChance, minLevel, maxLevel));
					}
					continue;
				}
				if(!nodeName.equalsIgnoreCase("teleportlist"))
					continue;
				Iterator sublistIterator = secondElement.elementIterator();
				while(sublistIterator.hasNext())
				{
					Element subListElement = (Element) sublistIterator.next();
					id = Integer.parseInt(subListElement.attributeValue("id"));
					ArrayList<TeleportLocation> list = new ArrayList<>();
					Iterator targetIterator = subListElement.elementIterator();
					while(targetIterator.hasNext())
					{
						Element targetElement = (Element) targetIterator.next();
						int itemId = Integer.parseInt(targetElement.attributeValue("item_id", "57"));
						long price = Integer.parseInt(targetElement.attributeValue("price"));
						int minLevel = Integer.parseInt(targetElement.attributeValue("min_level", "0"));
						int maxLevel = Integer.parseInt(targetElement.attributeValue("max_level", "0"));
						String nameCustomStringAddr = targetElement.attributeValue("name").trim();
						int castleId = Integer.parseInt(targetElement.attributeValue("castle_id", "0"));
						TeleportLocation loc = new TeleportLocation(itemId, price, minLevel, maxLevel, nameCustomStringAddr, castleId);
						loc.set(Location.parseLoc(targetElement.attributeValue("loc")));
						if(minLevel > 0 || maxLevel > 0)
						{
							for(Location minMaxCheckLoc : teleportLocations)
							{
								if(minMaxCheckLoc.x != loc.x || minMaxCheckLoc.y != loc.y || minMaxCheckLoc.z != loc.z)
									continue;
								_log.warn("Teleport location may intersect for " + targetElement.asXML());
							}
						}
						teleportLocations.add(loc);
						list.add(loc);
					}
					template.addTeleportList(id, list.toArray(new TeleportLocation[list.size()]));
				}
			}
			getHolder().addTemplate(template);
		}
	}
	
	private RewardData parseReward(Element rewardElement, RewardType rewardType)
	{
		int itemId = Integer.parseInt(rewardElement.attributeValue("item_id"));
		if(rewardType == RewardType.SWEEP ? ArrayUtils.contains(Config.NO_DROP_ITEMS_FOR_SWEEP, itemId) : ArrayUtils.contains(Config.NO_DROP_ITEMS, itemId))
		{
			return null;
		}
		int min = Integer.parseInt(rewardElement.attributeValue("min"));
		int max = Integer.parseInt(rewardElement.attributeValue("max"));
		int chance = (int) (Double.parseDouble(rewardElement.attributeValue("chance")) * 10000.0);
		RewardData data = new RewardData(itemId);
		if(data.getItem().isHerb())
		{
			data.setChance((double) chance * Config.RATE_DROP_HERBS);
		}
		else
		{
			data.setChance(chance);
		}
		data.setMinDrop(min);
		data.setMaxDrop(max);
		return data;
	}
}