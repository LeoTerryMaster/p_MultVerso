package l2.gameserver.data.xml.parser;

import l2.gameserver.Config;
import l2.gameserver.data.xml.holder.ItemHolder;
import l2.gameserver.data.xml.holder.OptionDataHolder;
import l2.gameserver.model.Skill;
import l2.gameserver.stats.conditions.Condition;
import l2.gameserver.tables.SkillTable;
import l2.gameserver.templates.OptionDataTemplate;
import l2.gameserver.templates.StatsSet;
import l2.gameserver.templates.item.ArmorTemplate;
import l2.gameserver.templates.item.Bodypart;
import l2.gameserver.templates.item.EtcItemTemplate;
import l2.gameserver.templates.item.ItemTemplate;
import l2.gameserver.templates.item.WeaponTemplate;
import org.dom4j.Element;

import java.io.File;
import java.util.Iterator;

public final class ItemParser extends StatParser<ItemHolder>
{
	private static final ItemParser _instance = new ItemParser();
	
	protected ItemParser()
	{
		super(ItemHolder.getInstance());
	}
	
	public static ItemParser getInstance()
	{
		return _instance;
	}
	
	@Override
	public File getXMLDir()
	{
		return new File(Config.DATAPACK_ROOT, "data/items/");
	}
	
	@Override
	public boolean isIgnored(File f)
	{
		return false;
	}
	
	@Override
	public String getDTDFileName()
	{
		return "item.dtd";
	}
	
	@Override
	protected void readData(Element rootElement) throws Exception
	{
		Iterator itemIterator = rootElement.elementIterator();
		while(itemIterator.hasNext())
		{
			Element itemElement = (Element) itemIterator.next();
			StatsSet set = new StatsSet();
			set.set("item_id", itemElement.attributeValue("id"));
			set.set("name", itemElement.attributeValue("name"));
			set.set("add_name", itemElement.attributeValue("add_name", ""));
			int slot = 0;
			Iterator subIterator = itemElement.elementIterator();
			while(subIterator.hasNext())
			{
				Element subElement = (Element) subIterator.next();
				String subName = subElement.getName();
				if(subName.equalsIgnoreCase("set"))
				{
					set.set(subElement.attributeValue("name"), subElement.attributeValue("value"));
					continue;
				}
				if(!subName.equalsIgnoreCase("equip"))
					continue;
				Iterator slotIterator = subElement.elementIterator();
				while(slotIterator.hasNext())
				{
					Element slotElement = (Element) slotIterator.next();
					Bodypart bodypart = Bodypart.valueOf(slotElement.attributeValue("id"));
					if(bodypart.getReal() != null)
					{
						slot = bodypart.mask();
						continue;
					}
					slot |= bodypart.mask();
				}
			}
			set.set("bodypart", slot);
			ItemTemplate template;
			try
			{
				if(itemElement.getName().equalsIgnoreCase("weapon"))
				{
					if(!set.containsKey("class"))
					{
						if((slot & 256) > 0)
						{
							set.set("class", ItemTemplate.ItemClass.ARMOR);
						}
						else
						{
							set.set("class", ItemTemplate.ItemClass.WEAPON);
						}
					}
					template = new WeaponTemplate(set);
				}
				else if(itemElement.getName().equalsIgnoreCase("armor"))
				{
					if(!set.containsKey("class"))
					{
						if((slot & 180032) > 0)
						{
							set.set("class", ItemTemplate.ItemClass.ARMOR);
						}
						else if((slot & 62) > 0)
						{
							set.set("class", ItemTemplate.ItemClass.JEWELRY);
						}
						else
						{
							set.set("class", ItemTemplate.ItemClass.ACCESSORY);
						}
					}
					template = new ArmorTemplate(set);
				}
				else
				{
					template = new EtcItemTemplate(set);
				}
			}
			catch(Exception e)
			{
				warn("Fail create item: " + set.get("item_id"), e);
				continue;
			}
			Iterator subIterator2 = itemElement.elementIterator();
			while(subIterator2.hasNext())
			{
				Element subElement = (Element) subIterator2.next();
				String subName = subElement.getName();
				if(subName.equalsIgnoreCase("for"))
				{
					parseFor(subElement, template);
					continue;
				}
				if(subName.equalsIgnoreCase("triggers"))
				{
					parseTriggers(subElement, template);
					continue;
				}
				Iterator nextIterator;
				if(subName.equalsIgnoreCase("skills"))
				{
					nextIterator = subElement.elementIterator();
					while(nextIterator.hasNext())
					{
						Element nextElement = (Element) nextIterator.next();
						int id = Integer.parseInt(nextElement.attributeValue("id"));
						int level = Integer.parseInt(nextElement.attributeValue("level"));
						Skill skill = SkillTable.getInstance().getInfo(id, level);
						if(skill != null)
						{
							template.attachSkill(skill);
							continue;
						}
						info("Skill not found(" + id + "," + level + ") for item:" + set.getObject("item_id") + "; file:" + getCurrentFileName());
					}
					continue;
				}
				if(subName.equalsIgnoreCase("enchant4_skill"))
				{
					int id = Integer.parseInt(subElement.attributeValue("id"));
					int level = Integer.parseInt(subElement.attributeValue("level"));
					Skill skill = SkillTable.getInstance().getInfo(id, level);
					if(skill == null)
						continue;
					template.setEnchant4Skill(skill);
					continue;
				}
				if(subName.equalsIgnoreCase("cond"))
				{
					Condition condition = parseFirstCond(subElement);
					if(condition == null)
						continue;
					int msgId = parseNumber(subElement.attributeValue("msgId")).intValue();
					condition.setSystemMsg(msgId);
					template.addCondition(condition);
					continue;
				}
				if(subName.equalsIgnoreCase("attributes"))
				{
					int[] attributes = new int[6];
					Iterator nextIterator2 = subElement.elementIterator();
					while(nextIterator2.hasNext())
					{
						Element nextElement = (Element) nextIterator2.next();
						if(!nextElement.getName().equalsIgnoreCase("attribute"))
							continue;
						l2.gameserver.model.base.Element element = l2.gameserver.model.base.Element.getElementByName(nextElement.attributeValue("element"));
						attributes[element.getId()] = Integer.parseInt(nextElement.attributeValue("value"));
					}
					template.setBaseAtributeElements(attributes);
					continue;
				}
				if(!subName.equalsIgnoreCase("enchant_options"))
					continue;
				nextIterator = subElement.elementIterator();
				while(nextIterator.hasNext())
				{
					Element nextElement = (Element) nextIterator.next();
					if(!nextElement.getName().equalsIgnoreCase("level"))
						continue;
					int val = Integer.parseInt(nextElement.attributeValue("val"));
					int i = 0;
					int[] options = new int[3];
					for(Element optionElement : nextElement.elements())
					{
						OptionDataTemplate optionData = OptionDataHolder.getInstance().getTemplate(Integer.parseInt(optionElement.attributeValue("id")));
						if(optionData == null)
						{
							error("Not found option_data for id: " + optionElement.attributeValue("id") + "; item_id: " + set.get("item_id"));
							continue;
						}
						options[i++] = optionData.getId();
					}
					template.addEnchantOptions(val, options);
				}
			}
			getHolder().addItem(template);
		}
	}
	
	@Override
	protected Object getTableValue(String name)
	{
		return null;
	}
}