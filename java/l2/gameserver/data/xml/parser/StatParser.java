package l2.gameserver.data.xml.parser;

import l2.commons.data.xml.AbstractDirParser;
import l2.commons.data.xml.AbstractHolder;
import l2.gameserver.model.entity.residence.ResidenceType;
import l2.gameserver.stats.StatTemplate;
import l2.gameserver.stats.Stats;
import l2.gameserver.stats.conditions.*;
import l2.gameserver.stats.funcs.EFunction;
import l2.gameserver.stats.funcs.FuncTemplate;
import l2.gameserver.stats.triggers.TriggerInfo;
import l2.gameserver.stats.triggers.TriggerType;
import l2.gameserver.templates.item.ArmorTemplate;
import l2.gameserver.templates.item.WeaponTemplate;
import org.dom4j.Attribute;
import org.dom4j.Element;

import java.util.Iterator;
import java.util.List;
import java.util.StringTokenizer;

public abstract class StatParser<H extends AbstractHolder> extends AbstractDirParser<H>
{
	protected StatParser(H holder)
	{
		super(holder);
	}
	
	protected Condition parseFirstCond(Element sub)
	{
		List e = sub.elements();
		if(e.isEmpty())
		{
			return null;
		}
		Element element = (Element) e.get(0);
		return parseCond(element);
	}
	
	protected Condition parseCond(Element element)
	{
		String name = element.getName();
		if(name.equalsIgnoreCase("and"))
		{
			return parseLogicAnd(element);
		}
		if(name.equalsIgnoreCase("or"))
		{
			return parseLogicOr(element);
		}
		if(name.equalsIgnoreCase("not"))
		{
			return parseLogicNot(element);
		}
		if(name.equalsIgnoreCase("target"))
		{
			return parseTargetCondition(element);
		}
		if(name.equalsIgnoreCase("player"))
		{
			return parsePlayerCondition(element);
		}
		if(name.equalsIgnoreCase("using"))
		{
			return parseUsingCondition(element);
		}
		if(name.equalsIgnoreCase("zone"))
		{
			return parseZoneCondition(element);
		}
		return null;
	}
	
	protected Condition parseLogicAnd(Element n)
	{
		ConditionLogicAnd cond = new ConditionLogicAnd();
		Iterator iterator = n.elementIterator();
		while(iterator.hasNext())
		{
			Element condElement = (Element) iterator.next();
			cond.add(parseCond(condElement));
		}
		if(cond._conditions == null || cond._conditions.length == 0)
		{
			error("Empty <and> condition in " + getCurrentFileName());
		}
		return cond;
	}
	
	protected Condition parseLogicOr(Element n)
	{
		ConditionLogicOr cond = new ConditionLogicOr();
		Iterator iterator = n.elementIterator();
		while(iterator.hasNext())
		{
			Element condElement = (Element) iterator.next();
			cond.add(parseCond(condElement));
		}
		if(cond._conditions == null || cond._conditions.length == 0)
		{
			error("Empty <or> condition in " + getCurrentFileName());
		}
		return cond;
	}
	
	protected Condition parseLogicNot(Element n)
	{
		Iterator iterator = n.elements().iterator();
		if(iterator.hasNext())
		{
			Object element = iterator.next();
			return new ConditionLogicNot(parseCond((Element) element));
		}
		error("Empty <not> condition in " + getCurrentFileName());
		return null;
	}
	
	protected Condition parseTargetCondition(Element element)
	{
		Condition cond = null;
		Iterator iterator = element.attributeIterator();
		while(iterator.hasNext())
		{
			Attribute attribute = (Attribute) iterator.next();
			String name = attribute.getName();
			String value = attribute.getValue();
			if(!name.equalsIgnoreCase("pvp"))
				continue;
			cond = joinAnd(cond, new ConditionTargetPlayable(Boolean.valueOf(value)));
		}
		return cond;
	}
	
	protected Condition parseZoneCondition(Element element)
	{
		Condition cond = null;
		Iterator iterator = element.attributeIterator();
		while(iterator.hasNext())
		{
			Attribute attribute = (Attribute) iterator.next();
			String name = attribute.getName();
			String value = attribute.getValue();
			if(!name.equalsIgnoreCase("type"))
				continue;
			cond = joinAnd(cond, new ConditionZoneType(value));
		}
		return cond;
	}
	
	protected Condition parsePlayerCondition(Element element)
	{
		Condition cond = null;
		Iterator iterator = element.attributeIterator();
		while(iterator.hasNext())
		{
			String[] st;
			Attribute attribute = (Attribute) iterator.next();
			String name = attribute.getName();
			String value = attribute.getValue();
			if(name.equalsIgnoreCase("residence"))
			{
				st = value.split(";");
				cond = joinAnd(cond, new ConditionPlayerResidence(Integer.parseInt(st[1]), ResidenceType.valueOf(st[0])));
				continue;
			}
			if(name.equalsIgnoreCase("classId"))
			{
				cond = joinAnd(cond, new ConditionPlayerClassId(value.split(",")));
				continue;
			}
			if(name.equalsIgnoreCase("olympiad"))
			{
				cond = joinAnd(cond, new ConditionPlayerOlympiad(Boolean.valueOf(value)));
				continue;
			}
			if(name.equalsIgnoreCase("min_pledge_rank"))
			{
				cond = joinAnd(cond, new ConditionClanPlayerMinPledgeRank(value));
				continue;
			}
			if(name.equalsIgnoreCase("is_hero"))
			{
				cond = joinAnd(cond, new ConditionPlayerIsHero(Boolean.parseBoolean(value)));
				continue;
			}
			if(name.equalsIgnoreCase("on_pvp_event"))
			{
				cond = joinAnd(cond, new ConditionPlayerInTeam(Boolean.parseBoolean(value)));
				continue;
			}
			if(name.equalsIgnoreCase("class_is_mage"))
			{
				cond = joinAnd(cond, new ConditionPlayerClassIsMage(Boolean.parseBoolean(value)));
				continue;
			}
			if(name.equalsIgnoreCase("instance_zone"))
			{
				cond = joinAnd(cond, new ConditionPlayerInstanceZone(Integer.parseInt(value)));
				continue;
			}
			if(name.equalsIgnoreCase("minLevel"))
			{
				cond = joinAnd(cond, new ConditionPlayerMinLevel(Integer.parseInt(value)));
				continue;
			}
			if(name.equalsIgnoreCase("maxLevel"))
			{
				cond = joinAnd(cond, new ConditionPlayerMaxLevel(Integer.parseInt(value)));
				continue;
			}
			if(name.equalsIgnoreCase("race"))
			{
				cond = joinAnd(cond, new ConditionPlayerRace(value));
				continue;
			}
			if(name.equalsIgnoreCase("gender"))
			{
				cond = joinAnd(cond, new ConditionPlayerGender(value));
				continue;
			}
			if(!name.equalsIgnoreCase("damage"))
				continue;
			st = value.split(";");
			cond = joinAnd(cond, new ConditionPlayerMinMaxDamage(Double.parseDouble(st[0]), Double.parseDouble(st[1])));
		}
		return cond;
	}
	
	protected Condition parseUsingCondition(Element element)
	{
		Condition cond = null;
		Iterator iterator = element.attributeIterator();
		while(iterator.hasNext())
		{
			Attribute attribute = (Attribute) iterator.next();
			String name = attribute.getName();
			String value = attribute.getValue();
			if(name.equalsIgnoreCase("slotitem"))
			{
				StringTokenizer st = new StringTokenizer(value, ";");
				int id = Integer.parseInt(st.nextToken().trim());
				int slot = Integer.parseInt(st.nextToken().trim());
				int enchant = 0;
				if(st.hasMoreTokens())
				{
					enchant = Integer.parseInt(st.nextToken().trim());
				}
				cond = joinAnd(cond, new ConditionSlotItemId(slot, id, enchant));
				continue;
			}
			if(name.equalsIgnoreCase("kind") || name.equalsIgnoreCase("weapon"))
			{
				long mask = 0;
				StringTokenizer st = new StringTokenizer(value, ",");
				block1:
				while(st.hasMoreTokens())
				{
					String item = st.nextToken().trim();
					for(WeaponTemplate.WeaponType wt : WeaponTemplate.WeaponType.VALUES)
					{
						if(!wt.toString().equalsIgnoreCase(item))
							continue;
						mask |= wt.mask();
						continue block1;
					}
					for(ArmorTemplate.ArmorType at : ArmorTemplate.ArmorType.VALUES)
					{
						if(!at.toString().equalsIgnoreCase(item))
							continue;
						mask |= at.mask();
						continue block1;
					}
					error("Invalid item kind: \"" + item + "\" in " + getCurrentFileName());
				}
				if(mask == 0)
					continue;
				cond = joinAnd(cond, new ConditionUsingItemType(mask));
				continue;
			}
			if(!name.equalsIgnoreCase("skill"))
				continue;
			cond = joinAnd(cond, new ConditionUsingSkill(Integer.parseInt(value)));
		}
		return cond;
	}
	
	protected Condition joinAnd(Condition cond, Condition c)
	{
		if(cond == null)
		{
			return c;
		}
		if(cond instanceof ConditionLogicAnd)
		{
			((ConditionLogicAnd) cond).add(c);
			return cond;
		}
		ConditionLogicAnd and = new ConditionLogicAnd();
		and.add(cond);
		and.add(c);
		return and;
	}
	
	protected void parseFor(Element forElement, StatTemplate template)
	{
		Iterator iterator = forElement.elementIterator();
		while(iterator.hasNext())
		{
			Element element = (Element) iterator.next();
			String elementName = element.getName();
			EFunction func = EFunction.VALUES_BY_LOWER_NAME.get(elementName.toLowerCase());
			if(null == func)
			{
				throw new RuntimeException("Unknown function specified '" + elementName + "'");
			}
			attachFunc(element, template, func);
		}
	}
	
	protected void parseTriggers(Element f, StatTemplate triggerable)
	{
		Iterator iterator = f.elementIterator();
		while(iterator.hasNext())
		{
			Element element = (Element) iterator.next();
			int id = parseNumber(element.attributeValue("id")).intValue();
			int level = parseNumber(element.attributeValue("level")).intValue();
			TriggerType t = TriggerType.valueOf(element.attributeValue("type"));
			double chance = parseNumber(element.attributeValue("chance")).doubleValue();
			TriggerInfo trigger = new TriggerInfo(id, level, t, chance);
			triggerable.addTrigger(trigger);
			Iterator subIterator = element.elementIterator();
			while(subIterator.hasNext())
			{
				Element subElement = (Element) subIterator.next();
				Condition condition = parseFirstCond(subElement);
				if(condition == null)
					continue;
				trigger.addCondition(condition);
			}
		}
	}
	
	protected void attachFunc(Element n, StatTemplate template, String name)
	{
		Stats stat = Stats.valueOfXml(n.attributeValue("stat"));
		String order = n.attributeValue("order");
		int ord = parseNumber(order).intValue();
		Condition applyCond = parseFirstCond(n);
		double val = 0.0;
		if(n.attributeValue("value") != null)
		{
			val = parseNumber(n.attributeValue("value")).doubleValue();
		}
		template.attachFunc(new FuncTemplate(applyCond, name, stat, ord, val));
	}
	
	protected void attachFunc(Element n, StatTemplate template, EFunction func)
	{
		Stats stat = Stats.valueOfXml(n.attributeValue("stat"));
		String order = n.attributeValue("order");
		int ord = parseNumber(order).intValue();
		Condition applyCond = parseFirstCond(n);
		double val = 0.0;
		if(n.attributeValue("value") != null)
		{
			val = parseNumber(n.attributeValue("value")).doubleValue();
		}
		template.attachFunc(new FuncTemplate(applyCond, func, stat, ord, val));
	}
	
	protected Number parseNumber(String value)
	{
		if(value.charAt(0) == '#')
		{
			value = getTableValue(value).toString();
		}
		try
		{
			if(value.indexOf(46) == -1)
			{
				int radix = 10;
				if(value.length() > 2 && value.substring(0, 2).equalsIgnoreCase("0x"))
				{
					value = value.substring(2);
					radix = 16;
				}
				return Integer.valueOf(value, radix);
			}
			return Double.valueOf(value);
		}
		catch(NumberFormatException e)
		{
			return null;
		}
	}
	
	protected abstract Object getTableValue(String name);
}