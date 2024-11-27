package l2.gameserver.data.xml.parser;

import l2.commons.data.xml.AbstractDirParser;
import l2.gameserver.Config;
import l2.gameserver.data.xml.holder.ResidenceHolder;
import l2.gameserver.model.Skill;
import l2.gameserver.model.TeleportLocation;
import l2.gameserver.model.entity.residence.Castle;
import l2.gameserver.model.entity.residence.Residence;
import l2.gameserver.model.entity.residence.ResidenceFunction;
import l2.gameserver.tables.SkillTable;
import l2.gameserver.templates.StatsSet;
import l2.gameserver.templates.item.support.MerchantGuard;
import l2.gameserver.utils.Location;
import org.dom4j.Attribute;
import org.dom4j.Element;
import org.napile.primitive.sets.impl.HashIntSet;

import java.io.File;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Iterator;

public final class ResidenceParser extends AbstractDirParser<ResidenceHolder>
{
	private static final ResidenceParser _instance = new ResidenceParser();
	
	private ResidenceParser()
	{
		super(ResidenceHolder.getInstance());
	}
	
	public static ResidenceParser getInstance()
	{
		return _instance;
	}
	
	@Override
	public File getXMLDir()
	{
		return new File(Config.DATAPACK_ROOT, "data/residences/");
	}
	
	@Override
	public boolean isIgnored(File f)
	{
		return false;
	}
	
	@Override
	public String getDTDFileName()
	{
		return "residence.dtd";
	}
	
	@Override
	protected void readData(Element rootElement) throws Exception
	{
		String impl = rootElement.attributeValue("impl");
		StatsSet set = new StatsSet();
		Iterator iterator = rootElement.attributeIterator();
		while(iterator.hasNext())
		{
			Attribute element = (Attribute) iterator.next();
			set.set(element.getName(), element.getValue());
		}
		Residence residence;
		try
		{
			Class clazz = Class.forName("l2.gameserver.model.entity.residence." + impl);
			Constructor constructor = clazz.getConstructor(StatsSet.class);
			residence = (Residence) constructor.newInstance(set);
			getHolder().addResidence(residence);
		}
		catch(Exception e)
		{
			error("fail to init: " + getCurrentFileName(), e);
			return;
		}
		Iterator iterator2 = rootElement.elementIterator();
		while(iterator2.hasNext())
		{
			Location loc;
			Element element = (Element) iterator2.next();
			String nodeName = element.getName();
			int level = element.attributeValue("level") == null ? 0 : Integer.valueOf(element.attributeValue("level"));
			int lease = (int) ((double) (element.attributeValue("lease") == null ? 0 : Integer.valueOf(element.attributeValue("lease"))) * Config.RESIDENCE_LEASE_FUNC_MULTIPLIER);
			int npcId = element.attributeValue("npcId") == null ? 0 : Integer.valueOf(element.attributeValue("npcId"));
			int listId = element.attributeValue("listId") == null ? 0 : Integer.valueOf(element.attributeValue("listId"));
			ResidenceFunction function = null;
			if(nodeName.equalsIgnoreCase("teleport"))
			{
				function = checkAndGetFunction(residence, 1);
				ArrayList<TeleportLocation> targets = new ArrayList<>();
				Iterator it2 = element.elementIterator();
				while(it2.hasNext())
				{
					Element teleportElement = (Element) it2.next();
					if(!"target".equalsIgnoreCase(teleportElement.getName()))
						continue;
					String name = teleportElement.attributeValue("name");
					long price = Long.parseLong(teleportElement.attributeValue("price"));
					int itemId = teleportElement.attributeValue("item") == null ? 57 : Integer.parseInt(teleportElement.attributeValue("item"));
					TeleportLocation loc2 = new TeleportLocation(itemId, price, name, 0);
					loc2.set(Location.parseLoc(teleportElement.attributeValue("loc")));
					targets.add(loc2);
				}
				function.addTeleports(level, targets.toArray(new TeleportLocation[targets.size()]));
			}
			else if(nodeName.equalsIgnoreCase("support"))
			{
				if(level > 9 && !Config.ALT_CH_ALLOW_1H_BUFFS)
					continue;
				function = checkAndGetFunction(residence, 6);
				function.addBuffs(level);
			}
			else if(nodeName.equalsIgnoreCase("item_create"))
			{
				function = checkAndGetFunction(residence, 2);
				function.addBuylist(level, new int[] {npcId, listId});
			}
			else if(nodeName.equalsIgnoreCase("curtain"))
			{
				function = checkAndGetFunction(residence, 7);
			}
			else if(nodeName.equalsIgnoreCase("platform"))
			{
				function = checkAndGetFunction(residence, 8);
			}
			else if(nodeName.equalsIgnoreCase("restore_exp"))
			{
				function = checkAndGetFunction(residence, 5);
			}
			else if(nodeName.equalsIgnoreCase("restore_hp"))
			{
				function = checkAndGetFunction(residence, 3);
			}
			else if(nodeName.equalsIgnoreCase("restore_mp"))
			{
				function = checkAndGetFunction(residence, 4);
			}
			else if(nodeName.equalsIgnoreCase("skills"))
			{
				Iterator nextIterator = element.elementIterator();
				while(nextIterator.hasNext())
				{
					Element nextElement = (Element) nextIterator.next();
					int id2 = Integer.parseInt(nextElement.attributeValue("id"));
					int level2 = Integer.parseInt(nextElement.attributeValue("level"));
					Skill skill = SkillTable.getInstance().getInfo(id2, level2);
					if(skill == null)
						continue;
					residence.addSkill(skill);
				}
			}
			else if(nodeName.equalsIgnoreCase("banish_points"))
			{
				Iterator banishPointsIterator = element.elementIterator();
				while(banishPointsIterator.hasNext())
				{
					loc = Location.parse((Element) banishPointsIterator.next());
					residence.addBanishPoint(loc);
				}
			}
			else if(nodeName.equalsIgnoreCase("owner_restart_points"))
			{
				Iterator ownerRestartPointsIterator = element.elementIterator();
				while(ownerRestartPointsIterator.hasNext())
				{
					loc = Location.parse((Element) ownerRestartPointsIterator.next());
					residence.addOwnerRestartPoint(loc);
				}
			}
			else if(nodeName.equalsIgnoreCase("other_restart_points"))
			{
				Iterator otherRestartPointsIterator = element.elementIterator();
				while(otherRestartPointsIterator.hasNext())
				{
					loc = Location.parse((Element) otherRestartPointsIterator.next());
					residence.addOtherRestartPoint(loc);
				}
			}
			else if(nodeName.equalsIgnoreCase("chaos_restart_points"))
			{
				Iterator chaosRestartPointsIterator = element.elementIterator();
				while(chaosRestartPointsIterator.hasNext())
				{
					loc = Location.parse((Element) chaosRestartPointsIterator.next());
					residence.addChaosRestartPoint(loc);
				}
			}
			else if(nodeName.equalsIgnoreCase("merchant_guards"))
			{
				Iterator subElementIterator = element.elementIterator();
				while(subElementIterator.hasNext())
				{
					Element subElement = (Element) subElementIterator.next();
					int itemId = Integer.parseInt(subElement.attributeValue("item_id"));
					int npcId2 = Integer.parseInt(subElement.attributeValue("npc_id"));
					int maxGuard = Integer.parseInt(subElement.attributeValue("max"));
					String[] ssq = subElement.attributeValue("ssq").split(";");
					HashIntSet intSet = new HashIntSet(3);
					for(String q : ssq)
					{
						if(q.equalsIgnoreCase("cabal_null"))
						{
							intSet.add(0);
							continue;
						}
						if(q.equalsIgnoreCase("cabal_dusk"))
						{
							intSet.add(1);
							continue;
						}
						if(q.equalsIgnoreCase("cabal_dawn"))
						{
							intSet.add(2);
							continue;
						}
						error("Unknown ssq type: " + q + "; file: " + getCurrentFileName());
					}
					((Castle) residence).addMerchantGuard(new MerchantGuard(itemId, npcId2, maxGuard, intSet));
				}
			}
			if(function == null)
				continue;
			function.addLease(level, lease);
		}
	}
	
	private ResidenceFunction checkAndGetFunction(Residence residence, int type)
	{
		ResidenceFunction function = residence.getFunction(type);
		if(function == null)
		{
			function = new ResidenceFunction(residence.getId(), type);
			residence.addFunction(function);
		}
		return function;
	}
}