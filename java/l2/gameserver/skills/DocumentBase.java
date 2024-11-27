package l2.gameserver.skills;

import l2.gameserver.model.Skill;
import l2.gameserver.skills.effects.EffectTemplate;
import l2.gameserver.stats.StatTemplate;
import l2.gameserver.stats.Stats;
import l2.gameserver.stats.conditions.*;
import l2.gameserver.stats.funcs.EFunction;
import l2.gameserver.stats.funcs.FuncTemplate;
import l2.gameserver.stats.triggers.TriggerInfo;
import l2.gameserver.stats.triggers.TriggerType;
import l2.gameserver.templates.StatsSet;
import l2.gameserver.templates.item.ArmorTemplate;
import l2.gameserver.templates.item.WeaponTemplate;
import l2.gameserver.utils.PositionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;
import java.util.StringTokenizer;

@Deprecated
abstract class DocumentBase
{
	private static final Logger _log = LoggerFactory.getLogger(DocumentBase.class);
	private final File file;
	
	DocumentBase(File file)
	{
		this.file = file;
	}
	
	Document parse()
	{
		Document doc;
		try
		{
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			factory.setValidating(false);
			factory.setIgnoringComments(true);
			doc = factory.newDocumentBuilder().parse(file);
		}
		catch(Exception e)
		{
			_log.error("Error loading file " + file, e);
			return null;
		}
		try
		{
			parseDocument(doc);
		}
		catch(Exception e)
		{
			_log.error("Error in file " + file, e);
			return null;
		}
		return doc;
	}
	
	protected abstract void parseDocument(Document doc);
	
	protected abstract Object getTableValue(String name);
	
	protected abstract Object getTableValue(String name, int level);
	
	protected void parseTemplate(Node n, StatTemplate template)
	{
		if((n = n.getFirstChild()) == null)
		{
			return;
		}
		while(n != null)
		{
			if(n.getNodeType() != 3)
			{
				String nodeName = n.getNodeName();
				if(EFunction.VALUES_BY_LOWER_NAME.containsKey(nodeName.toLowerCase()))
				{
					attachFunc(n, template, EFunction.VALUES_BY_LOWER_NAME.get(nodeName.toLowerCase()));
				}
				else if("effect".equalsIgnoreCase(nodeName))
				{
					if(template instanceof EffectTemplate)
					{
						throw new RuntimeException("Nested effects");
					}
					attachEffect(n, template);
				}
				else if(template instanceof EffectTemplate)
				{
					if("def".equalsIgnoreCase(nodeName))
					{
						EffectTemplate effectTemplate = (EffectTemplate) template;
						StatsSet effectTemplateParamsSet = effectTemplate.getParam();
						Skill skill = (Skill) effectTemplateParamsSet.getObject("object");
						parseBeanSet(n, effectTemplateParamsSet, skill.getLevel());
					}
					else
					{
						Condition cond = parseCondition(n);
						if(cond != null)
						{
							((EffectTemplate) template).attachCond(cond);
						}
					}
				}
				else
				{
					throw new RuntimeException("Unknown template " + nodeName);
				}
			}
			n = n.getNextSibling();
		}
	}
	
	protected void parseTrigger(Node n, StatTemplate template)
	{
		for(n = n.getFirstChild();n != null;n = n.getNextSibling())
		{
			if(!"trigger".equalsIgnoreCase(n.getNodeName()))
				continue;
			NamedNodeMap map = n.getAttributes();
			int id = parseNumber(map.getNamedItem("id").getNodeValue()).intValue();
			int level = parseNumber(map.getNamedItem("level").getNodeValue()).intValue();
			TriggerType t = TriggerType.valueOf(map.getNamedItem("type").getNodeValue());
			double chance = parseNumber(map.getNamedItem("chance").getNodeValue()).doubleValue();
			TriggerInfo trigger = new TriggerInfo(id, level, t, chance);
			template.addTrigger(trigger);
			for(Node n2 = n.getFirstChild();n2 != null;n2 = n2.getNextSibling())
			{
				Condition condition = parseCondition(n.getFirstChild());
				if(condition == null)
					continue;
				trigger.addCondition(condition);
			}
		}
	}
	
	protected void attachFunc(Node n, StatTemplate template, String name)
	{
		Stats stat = Stats.valueOfXml(n.getAttributes().getNamedItem("stat").getNodeValue());
		String order = n.getAttributes().getNamedItem("order").getNodeValue();
		int ord = parseNumber(order).intValue();
		Condition applyCond = parseCondition(n.getFirstChild());
		double val = 0.0;
		if(n.getAttributes().getNamedItem("val") != null)
		{
			val = parseNumber(n.getAttributes().getNamedItem("val").getNodeValue()).doubleValue();
		}
		template.attachFunc(new FuncTemplate(applyCond, name, stat, ord, val));
	}
	
	protected void attachFunc(Node n, StatTemplate template, EFunction func)
	{
		Stats stat = Stats.valueOfXml(n.getAttributes().getNamedItem("stat").getNodeValue());
		String order = n.getAttributes().getNamedItem("order").getNodeValue();
		int ord = parseNumber(order).intValue();
		Condition applyCond = parseCondition(n.getFirstChild());
		double val = 0.0;
		if(n.getAttributes().getNamedItem("val") != null)
		{
			val = parseNumber(n.getAttributes().getNamedItem("val").getNodeValue()).doubleValue();
		}
		template.attachFunc(new FuncTemplate(applyCond, func, stat, ord, val));
	}
	
	protected void attachEffect(Node n, Object template)
	{
		NamedNodeMap attrs = n.getAttributes();
		StatsSet set = new StatsSet();
		set.set("name", attrs.getNamedItem("name").getNodeValue());
		set.set("object", template);
		if(attrs.getNamedItem("count") != null)
		{
			set.set("count", parseNumber(attrs.getNamedItem("count").getNodeValue()).intValue());
		}
		if(attrs.getNamedItem("time") != null)
		{
			set.set("time", parseNumber(attrs.getNamedItem("time").getNodeValue()).intValue());
		}
		set.set("value", attrs.getNamedItem("val") != null ? parseNumber(attrs.getNamedItem("val").getNodeValue()).doubleValue() : 0.0);
		set.set("abnormal", AbnormalEffect.NULL);
		set.set("abnormal2", AbnormalEffect.NULL);
		set.set("abnormal3", AbnormalEffect.NULL);
		if(attrs.getNamedItem("abnormal") != null)
		{
			AbnormalEffect ae = AbnormalEffect.getByName(attrs.getNamedItem("abnormal").getNodeValue());
			if(ae.isSpecial())
			{
				set.set("abnormal2", ae);
			}
			if(ae.isEvent())
			{
				set.set("abnormal3", ae);
			}
			else
			{
				set.set("abnormal", ae);
			}
		}
		if(attrs.getNamedItem("stackType") != null)
		{
			set.set("stackType", attrs.getNamedItem("stackType").getNodeValue());
		}
		if(attrs.getNamedItem("stackType2") != null)
		{
			set.set("stackType2", attrs.getNamedItem("stackType2").getNodeValue());
		}
		if(attrs.getNamedItem("stackOrder") != null)
		{
			set.set("stackOrder", parseNumber(attrs.getNamedItem("stackOrder").getNodeValue()).intValue());
		}
		if(attrs.getNamedItem("applyOnCaster") != null)
		{
			set.set("applyOnCaster", Boolean.valueOf(attrs.getNamedItem("applyOnCaster").getNodeValue()));
		}
		if(attrs.getNamedItem("applyOnSummon") != null)
		{
			set.set("applyOnSummon", Boolean.valueOf(attrs.getNamedItem("applyOnSummon").getNodeValue()));
		}
		if(attrs.getNamedItem("displayId") != null)
		{
			set.set("displayId", parseNumber(attrs.getNamedItem("displayId").getNodeValue()).intValue());
		}
		if(attrs.getNamedItem("displayLevel") != null)
		{
			set.set("displayLevel", parseNumber(attrs.getNamedItem("displayLevel").getNodeValue()).intValue());
		}
		if(attrs.getNamedItem("chance") != null)
		{
			set.set("chance", parseNumber(attrs.getNamedItem("chance").getNodeValue()).intValue());
		}
		if(attrs.getNamedItem("cancelOnAction") != null)
		{
			set.set("cancelOnAction", Boolean.valueOf(attrs.getNamedItem("cancelOnAction").getNodeValue()));
		}
		if(attrs.getNamedItem("isOffensive") != null)
		{
			set.set("isOffensive", Boolean.valueOf(attrs.getNamedItem("isOffensive").getNodeValue()));
		}
		if(attrs.getNamedItem("isReflectable") != null)
		{
			set.set("isReflectable", Boolean.valueOf(attrs.getNamedItem("isReflectable").getNodeValue()));
		}
		EffectTemplate lt = new EffectTemplate(set);
		parseTemplate(n, lt);
		for(Node n1 = n.getFirstChild();n1 != null;n1 = n1.getNextSibling())
		{
			if(!"triggers".equalsIgnoreCase(n1.getNodeName()))
				continue;
			parseTrigger(n1, lt);
		}
		if(template instanceof Skill)
		{
			((Skill) template).attach(lt);
		}
	}
	
	protected Condition parseCondition(Node n)
	{
		while(n != null && n.getNodeType() != 1)
		{
			n = n.getNextSibling();
		}
		if(n == null)
		{
			return null;
		}
		if("and".equalsIgnoreCase(n.getNodeName()))
		{
			return parseLogicAnd(n);
		}
		if("or".equalsIgnoreCase(n.getNodeName()))
		{
			return parseLogicOr(n);
		}
		if("not".equalsIgnoreCase(n.getNodeName()))
		{
			return parseLogicNot(n);
		}
		if("player".equalsIgnoreCase(n.getNodeName()))
		{
			return parsePlayerCondition(n);
		}
		if("target".equalsIgnoreCase(n.getNodeName()))
		{
			return parseTargetCondition(n);
		}
		if("has".equalsIgnoreCase(n.getNodeName()))
		{
			return parseHasCondition(n);
		}
		if("using".equalsIgnoreCase(n.getNodeName()))
		{
			return parseUsingCondition(n);
		}
		if("game".equalsIgnoreCase(n.getNodeName()))
		{
			return parseGameCondition(n);
		}
		if("zone".equalsIgnoreCase(n.getNodeName()))
		{
			return parseZoneCondition(n);
		}
		return null;
	}
	
	protected Condition parseLogicAnd(Node n)
	{
		ConditionLogicAnd cond = new ConditionLogicAnd();
		for(n = n.getFirstChild();n != null;n = n.getNextSibling())
		{
			if(n.getNodeType() != 1)
				continue;
			cond.add(parseCondition(n));
		}
		if(cond._conditions == null || cond._conditions.length == 0)
		{
			_log.error("Empty <and> condition in " + file);
		}
		return cond;
	}
	
	protected Condition parseLogicOr(Node n)
	{
		ConditionLogicOr cond = new ConditionLogicOr();
		for(n = n.getFirstChild();n != null;n = n.getNextSibling())
		{
			if(n.getNodeType() != 1)
				continue;
			cond.add(parseCondition(n));
		}
		if(cond._conditions == null || cond._conditions.length == 0)
		{
			_log.error("Empty <or> condition in " + file);
		}
		return cond;
	}
	
	protected Condition parseLogicNot(Node n)
	{
		for(n = n.getFirstChild();n != null;n = n.getNextSibling())
		{
			if(n.getNodeType() != 1)
				continue;
			return new ConditionLogicNot(parseCondition(n));
		}
		_log.error("Empty <not> condition in " + file);
		return null;
	}
	
	protected Condition parsePlayerCondition(Node n)
	{
		Condition cond = null;
		NamedNodeMap attrs = n.getAttributes();
		for(int i = 0;i < attrs.getLength();++i)
		{
			Node a = attrs.item(i);
			String nodeName = a.getNodeName();
			if("race".equalsIgnoreCase(nodeName))
			{
				cond = joinAnd(cond, new ConditionPlayerRace(a.getNodeValue()));
				continue;
			}
			int lvl;
			if("minLevel".equalsIgnoreCase(nodeName))
			{
				lvl = parseNumber(a.getNodeValue()).intValue();
				cond = joinAnd(cond, new ConditionPlayerMinLevel(lvl));
				continue;
			}
			if("summon_siege_golem".equalsIgnoreCase(nodeName))
			{
				cond = joinAnd(cond, new ConditionPlayerSummonSiegeGolem());
				continue;
			}
			if("maxLevel".equalsIgnoreCase(nodeName))
			{
				lvl = parseNumber(a.getNodeValue()).intValue();
				cond = joinAnd(cond, new ConditionPlayerMaxLevel(lvl));
				continue;
			}
			if("maxPK".equalsIgnoreCase(nodeName))
			{
				int pk = parseNumber(a.getNodeValue()).intValue();
				cond = joinAnd(cond, new ConditionPlayerMaxPK(pk));
				continue;
			}
			boolean val;
			if("resting".equalsIgnoreCase(nodeName))
			{
				val = Boolean.valueOf(a.getNodeValue()).booleanValue();
				cond = joinAnd(cond, new ConditionPlayerState(ConditionPlayerState.CheckPlayerState.RESTING, val));
				continue;
			}
			if("moving".equalsIgnoreCase(nodeName))
			{
				val = Boolean.valueOf(a.getNodeValue()).booleanValue();
				cond = joinAnd(cond, new ConditionPlayerState(ConditionPlayerState.CheckPlayerState.MOVING, val));
				continue;
			}
			if("running".equalsIgnoreCase(nodeName))
			{
				val = Boolean.valueOf(a.getNodeValue()).booleanValue();
				cond = joinAnd(cond, new ConditionPlayerState(ConditionPlayerState.CheckPlayerState.RUNNING, val));
				continue;
			}
			if("standing".equalsIgnoreCase(nodeName))
			{
				val = Boolean.valueOf(a.getNodeValue()).booleanValue();
				cond = joinAnd(cond, new ConditionPlayerState(ConditionPlayerState.CheckPlayerState.STANDING, val));
				continue;
			}
			if("flying".equalsIgnoreCase(a.getNodeName()))
			{
				val = Boolean.valueOf(a.getNodeValue()).booleanValue();
				cond = joinAnd(cond, new ConditionPlayerState(ConditionPlayerState.CheckPlayerState.FLYING, val));
				continue;
			}
			if("flyingTransform".equalsIgnoreCase(a.getNodeName()))
			{
				val = Boolean.valueOf(a.getNodeValue()).booleanValue();
				cond = joinAnd(cond, new ConditionPlayerState(ConditionPlayerState.CheckPlayerState.FLYING_TRANSFORM, val));
				continue;
			}
			if("olympiad".equalsIgnoreCase(a.getNodeName()))
			{
				val = Boolean.valueOf(a.getNodeValue()).booleanValue();
				cond = joinAnd(cond, new ConditionPlayerOlympiad(val));
				continue;
			}
			if("on_pvp_event".equalsIgnoreCase(a.getNodeName()))
			{
				val = Boolean.valueOf(a.getNodeValue()).booleanValue();
				cond = joinAnd(cond, new ConditionPlayerInTeam(val));
				continue;
			}
			if("is_hero".equalsIgnoreCase(nodeName))
			{
				val = Boolean.valueOf(a.getNodeValue()).booleanValue();
				cond = joinAnd(cond, new ConditionPlayerIsHero(val));
				continue;
			}
			if("on_pvp_event".equalsIgnoreCase(nodeName))
			{
				val = Boolean.valueOf(a.getNodeValue()).booleanValue();
				cond = joinAnd(cond, new ConditionPlayerInTeam(val));
				continue;
			}
			if("class_is_mage".equalsIgnoreCase(nodeName))
			{
				val = Boolean.valueOf(a.getNodeValue()).booleanValue();
				cond = joinAnd(cond, new ConditionPlayerClassIsMage(val));
				continue;
			}
			if("min_pledge_rank".equalsIgnoreCase(nodeName))
			{
				cond = joinAnd(cond, new ConditionClanPlayerMinPledgeRank(a.getNodeValue()));
				continue;
			}
			if("percentHP".equalsIgnoreCase(nodeName))
			{
				int percentHP = parseNumber(a.getNodeValue()).intValue();
				cond = joinAnd(cond, new ConditionPlayerPercentHp(percentHP));
				continue;
			}
			if("percentMP".equalsIgnoreCase(nodeName))
			{
				int percentMP = parseNumber(a.getNodeValue()).intValue();
				cond = joinAnd(cond, new ConditionPlayerPercentMp(percentMP));
				continue;
			}
			if("percentCP".equalsIgnoreCase(nodeName))
			{
				int percentCP = parseNumber(a.getNodeValue()).intValue();
				cond = joinAnd(cond, new ConditionPlayerPercentCp(percentCP));
				continue;
			}
			if("chargesMin".equalsIgnoreCase(nodeName))
			{
				int id = parseNumber(a.getNodeValue()).intValue();
				cond = joinAnd(cond, new ConditionPlayerChargesMin(id));
				continue;
			}
			if("chargesMax".equalsIgnoreCase(nodeName))
			{
				int id = parseNumber(a.getNodeValue()).intValue();
				cond = joinAnd(cond, new ConditionPlayerChargesMax(id));
				continue;
			}
			if("agathion".equalsIgnoreCase(nodeName))
			{
				int agathionId = parseNumber(a.getNodeValue()).intValue();
				cond = joinAnd(cond, new ConditionPlayerAgathion(agathionId));
				continue;
			}
			if("cubic".equalsIgnoreCase(nodeName))
			{
				int cubicId = parseNumber(a.getNodeValue()).intValue();
				cond = joinAnd(cond, new ConditionPlayerCubic(cubicId));
				continue;
			}
			if("instance_zone".equalsIgnoreCase(nodeName))
			{
				int id = parseNumber(a.getNodeValue()).intValue();
				cond = joinAnd(cond, new ConditionPlayerInstanceZone(id));
				continue;
			}
			if("riding".equalsIgnoreCase(nodeName))
			{
				String riding = a.getNodeValue();
				if("strider".equalsIgnoreCase(riding))
				{
					cond = joinAnd(cond, new ConditionPlayerRiding(ConditionPlayerRiding.CheckPlayerRiding.STRIDER));
					continue;
				}
				if("wyvern".equalsIgnoreCase(riding))
				{
					cond = joinAnd(cond, new ConditionPlayerRiding(ConditionPlayerRiding.CheckPlayerRiding.WYVERN));
					continue;
				}
				if(!"none".equalsIgnoreCase(riding))
					continue;
				cond = joinAnd(cond, new ConditionPlayerRiding(ConditionPlayerRiding.CheckPlayerRiding.NONE));
				continue;
			}
			if("classId".equalsIgnoreCase(nodeName))
			{
				cond = joinAnd(cond, new ConditionPlayerClassId(a.getNodeValue().split(",")));
				continue;
			}
			if("gender".equalsIgnoreCase(nodeName))
			{
				cond = joinAnd(cond, new ConditionPlayerGender(a.getNodeValue()));
				continue;
			}
			int level;
			if("hasBuffId".equalsIgnoreCase(nodeName))
			{
				StringTokenizer st = new StringTokenizer(a.getNodeValue(), ";");
				int id = Integer.parseInt(st.nextToken().trim());
				level = -1;
				if(st.hasMoreTokens())
				{
					level = Integer.parseInt(st.nextToken().trim());
				}
				cond = joinAnd(cond, new ConditionPlayerHasBuffId(id, level));
				continue;
			}
			if("hasBuff".equalsIgnoreCase(nodeName))
			{
				StringTokenizer st = new StringTokenizer(a.getNodeValue(), ";");
				EffectType et = Enum.valueOf(EffectType.class, st.nextToken().trim());
				level = -1;
				if(st.hasMoreTokens())
				{
					level = Integer.parseInt(st.nextToken().trim());
				}
				cond = joinAnd(cond, new ConditionPlayerHasBuff(et, level));
				continue;
			}
			if("damage".equalsIgnoreCase(nodeName))
			{
				String[] st = a.getNodeValue().split(";");
				cond = joinAnd(cond, new ConditionPlayerMinMaxDamage(Double.parseDouble(st[0]), Double.parseDouble(st[1])));
				continue;
			}
			if(!"skillMinSeed".equalsIgnoreCase(nodeName))
				continue;
			StringTokenizer st = new StringTokenizer(a.getNodeValue(), ";");
			int skillId = Integer.parseInt(st.nextToken().trim());
			int skillMinSeed = Integer.parseInt(st.nextToken().trim());
			cond = joinAnd(cond, new ConditionPlayerSkillMinSeed(skillId, skillMinSeed));
		}
		if(cond == null)
		{
			_log.error("Unrecognized <player> condition in " + file);
		}
		return cond;
	}
	
	protected Condition parseTargetCondition(Node n)
	{
		Condition cond = null;
		NamedNodeMap attrs = n.getAttributes();
		for(int i = 0;i < attrs.getLength();++i)
		{
			Node a = attrs.item(i);
			String nodeName = a.getNodeName();
			String nodeValue = a.getNodeValue();
			if("aggro".equalsIgnoreCase(nodeName))
			{
				cond = joinAnd(cond, new ConditionTargetAggro(Boolean.valueOf(nodeValue)));
				continue;
			}
			if("pvp".equalsIgnoreCase(nodeName))
			{
				cond = joinAnd(cond, new ConditionTargetPlayable(Boolean.valueOf(nodeValue)));
				continue;
			}
			if("player".equalsIgnoreCase(nodeName))
			{
				cond = joinAnd(cond, new ConditionTargetPlayer(Boolean.valueOf(nodeValue)));
				continue;
			}
			if("exclude_caster".equalsIgnoreCase(nodeName))
			{
				cond = joinAnd(cond, new ConditionTargetPlayerNotMe(Boolean.valueOf(nodeValue)));
				continue;
			}
			if("summon".equalsIgnoreCase(nodeName))
			{
				cond = joinAnd(cond, new ConditionTargetSummon(Boolean.valueOf(nodeValue)));
				continue;
			}
			if("mob".equalsIgnoreCase(nodeName))
			{
				cond = joinAnd(cond, new ConditionTargetMob(Boolean.valueOf(nodeValue)));
				continue;
			}
			if("targetInTheSameParty".equalsIgnoreCase(nodeName))
			{
				cond = joinAnd(cond, new ConditionTargetInTheSameParty(Boolean.valueOf(nodeValue)));
				continue;
			}
			if("mobId".equalsIgnoreCase(nodeName))
			{
				cond = joinAnd(cond, new ConditionTargetMobId(Integer.parseInt(nodeValue)));
				continue;
			}
			if("race".equalsIgnoreCase(nodeName))
			{
				cond = joinAnd(cond, new ConditionTargetRace(nodeValue));
				continue;
			}
			if("npc_class".equalsIgnoreCase(nodeName))
			{
				cond = joinAnd(cond, new ConditionTargetNpcClass(nodeValue));
				continue;
			}
			if("playerRace".equalsIgnoreCase(nodeName))
			{
				cond = joinAnd(cond, new ConditionTargetPlayerRace(nodeValue));
				continue;
			}
			if("forbiddenClassIds".equalsIgnoreCase(nodeName))
			{
				cond = joinAnd(cond, new ConditionTargetForbiddenClassId(nodeValue.split(";")));
				continue;
			}
			if("playerSameClan".equalsIgnoreCase(nodeName))
			{
				cond = joinAnd(cond, new ConditionTargetClan(nodeValue));
				continue;
			}
			if("castledoor".equalsIgnoreCase(nodeName))
			{
				cond = joinAnd(cond, new ConditionTargetCastleDoor(Boolean.valueOf(nodeValue)));
				continue;
			}
			if("direction".equalsIgnoreCase(nodeName))
			{
				cond = joinAnd(cond, new ConditionTargetDirection(PositionUtils.TargetDirection.valueOf(nodeValue.toUpperCase())));
				continue;
			}
			if("percentHP".equalsIgnoreCase(nodeName))
			{
				cond = joinAnd(cond, new ConditionTargetPercentHp(parseNumber(a.getNodeValue()).intValue()));
				continue;
			}
			if("percentMP".equalsIgnoreCase(nodeName))
			{
				cond = joinAnd(cond, new ConditionTargetPercentMp(parseNumber(a.getNodeValue()).intValue()));
				continue;
			}
			if("percentCP".equalsIgnoreCase(nodeName))
			{
				cond = joinAnd(cond, new ConditionTargetPercentCp(parseNumber(a.getNodeValue()).intValue()));
				continue;
			}
			int level;
			StringTokenizer st;
			if("hasBuffId".equalsIgnoreCase(nodeName))
			{
				st = new StringTokenizer(nodeValue, ";");
				int id = Integer.parseInt(st.nextToken().trim());
				level = -1;
				if(st.hasMoreTokens())
				{
					level = Integer.parseInt(st.nextToken().trim());
				}
				cond = joinAnd(cond, new ConditionTargetHasBuffId(id, level));
				continue;
			}
			if("hasBuff".equalsIgnoreCase(nodeName))
			{
				st = new StringTokenizer(nodeValue, ";");
				EffectType et = Enum.valueOf(EffectType.class, st.nextToken().trim());
				level = -1;
				if(st.hasMoreTokens())
				{
					level = Integer.parseInt(st.nextToken().trim());
				}
				cond = joinAnd(cond, new ConditionTargetHasBuff(et, level));
				continue;
			}
			if(!"hasForbiddenSkill".equalsIgnoreCase(nodeName))
				continue;
			cond = joinAnd(cond, new ConditionTargetHasForbiddenSkill(parseNumber(a.getNodeValue()).intValue()));
		}
		if(cond == null)
		{
			_log.error("Unrecognized <target> condition in " + file);
		}
		return cond;
	}
	
	protected Condition parseUsingCondition(Node n)
	{
		Condition cond = null;
		NamedNodeMap attrs = n.getAttributes();
		for(int i = 0;i < attrs.getLength();++i)
		{
			Node a = attrs.item(i);
			String nodeName = a.getNodeName();
			String nodeValue = a.getNodeValue();
			if("kind".equalsIgnoreCase(nodeName) || "weapon".equalsIgnoreCase(nodeName))
			{
				long mask = 0;
				StringTokenizer st = new StringTokenizer(nodeValue, ",");
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
					_log.error("Invalid item kind: \"" + item + "\" in " + file);
				}
				if(mask == 0)
					continue;
				cond = joinAnd(cond, new ConditionUsingItemType(mask));
				continue;
			}
			if("armor".equalsIgnoreCase(nodeName))
			{
				ArmorTemplate.ArmorType armor = ArmorTemplate.ArmorType.valueOf(nodeValue.toUpperCase());
				cond = joinAnd(cond, new ConditionUsingArmor(armor));
				continue;
			}
			if("skill".equalsIgnoreCase(nodeName))
			{
				cond = joinAnd(cond, new ConditionUsingSkill(Integer.parseInt(nodeValue)));
				continue;
			}
			if("blowskill".equalsIgnoreCase(nodeName))
			{
				cond = joinAnd(cond, new ConditionUsingBlowSkill(Boolean.parseBoolean(nodeValue)));
				continue;
			}
			if(!"slotitem".equalsIgnoreCase(nodeName))
				continue;
			StringTokenizer st = new StringTokenizer(nodeValue, ";");
			int id = Integer.parseInt(st.nextToken().trim());
			int slot = Integer.parseInt(st.nextToken().trim());
			int enchant = 0;
			if(st.hasMoreTokens())
			{
				enchant = Integer.parseInt(st.nextToken().trim());
			}
			cond = joinAnd(cond, new ConditionSlotItemId(slot, id, enchant));
		}
		if(cond == null)
		{
			_log.error("Unrecognized <using> condition in " + file);
		}
		return cond;
	}
	
	protected Condition parseHasCondition(Node n)
	{
		Condition cond = null;
		NamedNodeMap attrs = n.getAttributes();
		for(int i = 0;i < attrs.getLength();++i)
		{
			Node a = attrs.item(i);
			String nodeName = a.getNodeName();
			String nodeValue = a.getNodeValue();
			if("skill".equalsIgnoreCase(nodeName))
			{
				StringTokenizer st = new StringTokenizer(nodeValue, ";");
				Integer id = parseNumber(st.nextToken().trim()).intValue();
				short level = parseNumber(st.nextToken().trim()).shortValue();
				cond = joinAnd(cond, new ConditionHasSkill(id, level));
				continue;
			}
			if(!"success".equalsIgnoreCase(nodeName))
				continue;
			cond = joinAnd(cond, new ConditionFirstEffectSuccess(Boolean.valueOf(nodeValue)));
		}
		if(cond == null)
		{
			_log.error("Unrecognized <has> condition in " + file);
		}
		return cond;
	}
	
	protected Condition parseGameCondition(Node n)
	{
		Condition cond = null;
		NamedNodeMap attrs = n.getAttributes();
		for(int i = 0;i < attrs.getLength();++i)
		{
			Node a = attrs.item(i);
			if(!"night".equalsIgnoreCase(a.getNodeName()))
				continue;
			boolean val = Boolean.valueOf(a.getNodeValue());
			cond = joinAnd(cond, new ConditionGameTime(ConditionGameTime.CheckGameTime.NIGHT, val));
		}
		if(cond == null)
		{
			_log.error("Unrecognized <game> condition in " + file);
		}
		return cond;
	}
	
	protected Condition parseZoneCondition(Node n)
	{
		Condition cond = null;
		NamedNodeMap attrs = n.getAttributes();
		for(int i = 0;i < attrs.getLength();++i)
		{
			Node a = attrs.item(i);
			if("type".equalsIgnoreCase(a.getNodeName()))
			{
				cond = joinAnd(cond, new ConditionZoneType(a.getNodeValue()));
				continue;
			}
			if(!"name".equalsIgnoreCase(a.getNodeName()))
				continue;
			cond = joinAnd(cond, new ConditionZoneName(a.getNodeValue()));
		}
		if(cond == null)
		{
			_log.error("Unrecognized <zone> condition in " + file);
		}
		return cond;
	}
	
	protected void parseBeanSet(Node n, StatsSet set, int level)
	{
		try
		{
			char ch;
			String name = n.getAttributes().getNamedItem("name").getNodeValue().trim();
			String value = n.getAttributes().getNamedItem("val").getNodeValue().trim();
			char c = ch = value.length() == 0 ? ' ' : value.charAt(0);
			if(value.contains("#") && ch != '#')
			{
				for(String str : value.split("[;: ]+"))
				{
					if(str.charAt(0) != '#')
						continue;
					value = value.replace(str, String.valueOf(getTableValue(str, level)));
				}
			}
			if(ch == '#')
			{
				Object tableVal = getTableValue(value, level);
				Number parsedVal = parseNumber(tableVal.toString());
				set.set(name, parsedVal == null ? tableVal : String.valueOf(parsedVal));
			}
			else if(!(!Character.isDigit(ch) && ch != '-' || value.contains(" ") || value.contains(";")))
			{
				set.set(name, String.valueOf(parseNumber(value)));
			}
			else
			{
				set.set(name, value);
			}
		}
		catch(Exception e)
		{
			System.out.println(n.getAttributes().getNamedItem("name") + " " + set.get("skill_id"));
			e.printStackTrace();
		}
	}
	
	protected Number parseNumber(String value)
	{
		if(value.charAt(0) == '#')
		{
			value = getTableValue(value).toString();
		}
		try
		{
			if(value.equalsIgnoreCase("max"))
			{
				return Double.POSITIVE_INFINITY;
			}
			if(value.equalsIgnoreCase("min"))
			{
				return Double.NEGATIVE_INFINITY;
			}
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
}