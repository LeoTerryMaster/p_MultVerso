package l2.gameserver.model.base;

import l2.gameserver.Config;
import l2.gameserver.model.Creature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;
import java.util.NoSuchElementException;

public enum BaseStats
{
	STR
			{
				@Override
				public final int getStat(Creature actor)
				{
					return actor == null ? 1 : actor.getSTR();
				}
				
				@Override
				public final double calcBonus(Creature actor)
				{
					return actor == null ? 1.0 : STRbonus[actor.getSTR()];
				}
				
				@Override
				public final double calcChanceMod(Creature actor)
				{
					return Math.min(2.0 - Math.sqrt(calcBonus(actor)), 1.0);
				}
			},
	INT
			{
				@Override
				public final int getStat(Creature actor)
				{
					return actor == null ? 1 : actor.getINT();
				}
				
				@Override
				public final double calcBonus(Creature actor)
				{
					return actor == null ? 1.0 : INTbonus[actor.getINT()];
				}
			},
	DEX
			{
				@Override
				public final int getStat(Creature actor)
				{
					return actor == null ? 1 : actor.getDEX();
				}
				
				@Override
				public final double calcBonus(Creature actor)
				{
					return actor == null ? 1.0 : DEXbonus[actor.getDEX()];
				}
			},
	WIT
			{
				@Override
				public final int getStat(Creature actor)
				{
					return actor == null ? 1 : actor.getWIT();
				}
				
				@Override
				public final double calcBonus(Creature actor)
				{
					return actor == null ? 1.0 : WITbonus[actor.getWIT()];
				}
			},
	CON
			{
				@Override
				public final int getStat(Creature actor)
				{
					return actor == null ? 1 : actor.getCON();
				}
				
				@Override
				public final double calcBonus(Creature actor)
				{
					return actor == null ? 1.0 : CONbonus[actor.getCON()];
				}
			},
	MEN
			{
				@Override
				public final int getStat(Creature actor)
				{
					return actor == null ? 1 : actor.getMEN();
				}
				
				@Override
				public final double calcBonus(Creature actor)
				{
					return actor == null ? 1.0 : MENbonus[actor.getMEN()];
				}
			},
	NONE;
	
	public static final BaseStats[] VALUES;
	protected static final Logger _log;
	private static final int MAX_STAT_VALUE = 100;
	private static final double[] STRbonus;
	private static final double[] INTbonus;
	private static final double[] DEXbonus;
	private static final double[] WITbonus;
	private static final double[] CONbonus;
	private static final double[] MENbonus;
	
	static
	{
		VALUES = BaseStats.values();
		_log = LoggerFactory.getLogger(BaseStats.class);
		STRbonus = new double[100];
		INTbonus = new double[100];
		DEXbonus = new double[100];
		WITbonus = new double[100];
		CONbonus = new double[100];
		MENbonus = new double[100];
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		factory.setValidating(false);
		factory.setIgnoringComments(true);
		File file = new File(Config.DATAPACK_ROOT, "data/attribute_bonus.xml");
		Document doc = null;
		try
		{
			doc = factory.newDocumentBuilder().parse(file);
		}
		catch(SAXException e)
		{
			_log.error("", e);
		}
		catch(IOException e)
		{
			_log.error("", e);
		}
		catch(ParserConfigurationException e)
		{
			_log.error("", e);
		}
		if(doc != null)
		{
			for(Node z = doc.getFirstChild();z != null;z = z.getNextSibling())
			{
				for(Node n = z.getFirstChild();n != null;n = n.getNextSibling())
				{
					String node;
					Node d;
					double val;
					if(n.getNodeName().equalsIgnoreCase("str_bonus"))
					{
						for(d = n.getFirstChild();d != null;d = d.getNextSibling())
						{
							node = d.getNodeName();
							if(!node.equalsIgnoreCase("set"))
								continue;
							int i = Integer.valueOf(d.getAttributes().getNamedItem("attribute").getNodeValue());
							val = Integer.valueOf(d.getAttributes().getNamedItem("val").getNodeValue()).intValue();
							STRbonus[i] = (100.0 + val) / 100.0;
						}
					}
					if(n.getNodeName().equalsIgnoreCase("int_bonus"))
					{
						for(d = n.getFirstChild();d != null;d = d.getNextSibling())
						{
							node = d.getNodeName();
							if(!node.equalsIgnoreCase("set"))
								continue;
							int i = Integer.valueOf(d.getAttributes().getNamedItem("attribute").getNodeValue());
							val = Integer.valueOf(d.getAttributes().getNamedItem("val").getNodeValue()).intValue();
							INTbonus[i] = (100.0 + val) / 100.0;
						}
					}
					if(n.getNodeName().equalsIgnoreCase("con_bonus"))
					{
						for(d = n.getFirstChild();d != null;d = d.getNextSibling())
						{
							node = d.getNodeName();
							if(!node.equalsIgnoreCase("set"))
								continue;
							int i = Integer.valueOf(d.getAttributes().getNamedItem("attribute").getNodeValue());
							val = Integer.valueOf(d.getAttributes().getNamedItem("val").getNodeValue()).intValue();
							CONbonus[i] = (100.0 + val) / 100.0;
						}
					}
					if(n.getNodeName().equalsIgnoreCase("men_bonus"))
					{
						for(d = n.getFirstChild();d != null;d = d.getNextSibling())
						{
							node = d.getNodeName();
							if(!node.equalsIgnoreCase("set"))
								continue;
							int i = Integer.valueOf(d.getAttributes().getNamedItem("attribute").getNodeValue());
							val = Integer.valueOf(d.getAttributes().getNamedItem("val").getNodeValue()).intValue();
							MENbonus[i] = (100.0 + val) / 100.0;
						}
					}
					if(n.getNodeName().equalsIgnoreCase("dex_bonus"))
					{
						for(d = n.getFirstChild();d != null;d = d.getNextSibling())
						{
							node = d.getNodeName();
							if(!node.equalsIgnoreCase("set"))
								continue;
							int i = Integer.valueOf(d.getAttributes().getNamedItem("attribute").getNodeValue());
							val = Integer.valueOf(d.getAttributes().getNamedItem("val").getNodeValue()).intValue();
							DEXbonus[i] = (100.0 + val) / 100.0;
						}
					}
					if(!n.getNodeName().equalsIgnoreCase("wit_bonus"))
						continue;
					for(d = n.getFirstChild();d != null;d = d.getNextSibling())
					{
						node = d.getNodeName();
						if(!node.equalsIgnoreCase("set"))
							continue;
						int i = Integer.valueOf(d.getAttributes().getNamedItem("attribute").getNodeValue());
						val = Integer.valueOf(d.getAttributes().getNamedItem("val").getNodeValue()).intValue();
						WITbonus[i] = (100.0 + val) / 100.0;
					}
				}
			}
		}
	}
	
	public static final BaseStats valueOfXml(String name)
	{
		name = name.intern();
		for(BaseStats s : VALUES)
		{
			if(!s.toString().equalsIgnoreCase(name))
				continue;
			if(s == NONE)
			{
				return null;
			}
			return s;
		}
		throw new NoSuchElementException("Unknown name '" + name + "' for enum BaseStats");
	}
	
	public int getStat(Creature actor)
	{
		return 1;
	}
	
	public double calcBonus(Creature actor)
	{
		return 1.0;
	}
	
	public double calcChanceMod(Creature actor)
	{
		return 2.0 - Math.sqrt(calcBonus(actor));
	}
}