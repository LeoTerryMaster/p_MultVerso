package l2.gameserver.data.xml.parser;

import l2.commons.data.xml.AbstractFileParser;
import l2.gameserver.Config;
import l2.gameserver.data.xml.holder.EnchantSkillHolder;
import org.dom4j.Element;

import java.io.File;
import java.util.Iterator;

public class EnchantSkillParser extends AbstractFileParser<EnchantSkillHolder>
{
	private static final EnchantSkillParser INSTANCE = new EnchantSkillParser();
	
	private EnchantSkillParser()
	{
		super(EnchantSkillHolder.getInstance());
	}
	
	public static EnchantSkillParser getInstance()
	{
		return INSTANCE;
	}
	
	@Override
	public File getXMLFile()
	{
		return new File(Config.DATAPACK_ROOT, "data/skill_tree/skill_enchant_data.xml");
	}
	
	@Override
	public String getDTDFileName()
	{
		return "skill_enchant_data.dtd";
	}
	
	@Override
	protected void readData(Element rootElement) throws Exception
	{
		Iterator skillElementIt = rootElement.elementIterator("skill");
		while(skillElementIt.hasNext())
		{
			Element skillElement = (Element) skillElementIt.next();
			Iterator skillRouteElementIt = skillElement.elementIterator("route");
			int skillId = Integer.parseInt(skillElement.attributeValue("id"));
			while(skillRouteElementIt.hasNext())
			{
				Element skillRouteElement = (Element) skillRouteElementIt.next();
				Iterator skillEnchantElementIt = skillRouteElement.elementIterator("enchant");
				int routeId = Integer.parseInt(skillRouteElement.attributeValue("id"));
				while(skillEnchantElementIt.hasNext())
				{
					Element skillEnchantElement = (Element) skillEnchantElementIt.next();
					int enchantLevel = Integer.parseInt(skillEnchantElement.attributeValue("level"));
					int skillLevel = Integer.parseInt(skillEnchantElement.attributeValue("skillLvl"));
					long exp = Long.parseLong(skillEnchantElement.attributeValue("exp"));
					int sp = Integer.parseInt(skillEnchantElement.attributeValue("sp"));
					String chancesVal = skillEnchantElement.attributeValue("chances");
					String[] chancesValArr = chancesVal.split("\\s+");
					int[] chances = new int[chancesValArr.length];
					for(int i = 0;i < chancesValArr.length;++i)
					{
						chances[i] = Integer.parseInt(chancesValArr[i]);
					}
					String neededItemIdVal = skillEnchantElement.attributeValue("neededItemId");
					int neededItemId = neededItemIdVal != null ? Integer.parseInt(neededItemIdVal) : 0;
					String neededItemCntVal = skillEnchantElement.attributeValue("neededItemCount");
					int neededItemCnt = neededItemCntVal != null ? Integer.parseInt(neededItemCntVal) : 0;
					getHolder().addEnchantSkill(skillId, skillLevel, enchantLevel, routeId, exp, sp, chances, neededItemId, neededItemCnt);
				}
			}
		}
	}
}