package l2.gameserver.data.xml.parser;

import l2.commons.data.xml.AbstractFileParser;
import l2.gameserver.Config;
import l2.gameserver.data.xml.holder.SoulCrystalHolder;
import l2.gameserver.templates.SoulCrystal;
import org.dom4j.Element;

import java.io.File;
import java.util.Iterator;

public final class SoulCrystalParser extends AbstractFileParser<SoulCrystalHolder>
{
	private static final SoulCrystalParser _instance = new SoulCrystalParser();
	
	private SoulCrystalParser()
	{
		super(SoulCrystalHolder.getInstance());
	}
	
	public static SoulCrystalParser getInstance()
	{
		return _instance;
	}
	
	@Override
	public File getXMLFile()
	{
		return new File(Config.DATAPACK_ROOT, "data/soul_crystals.xml");
	}
	
	@Override
	public String getDTDFileName()
	{
		return "soul_crystals.dtd";
	}
	
	@Override
	protected void readData(Element rootElement) throws Exception
	{
		Iterator iterator = rootElement.elementIterator("crystal");
		while(iterator.hasNext())
		{
			Element element = (Element) iterator.next();
			int itemId = Integer.parseInt(element.attributeValue("item_id"));
			int level = Integer.parseInt(element.attributeValue("level"));
			int nextItemId = Integer.parseInt(element.attributeValue("next_item_id"));
			int cursedNextItemId = element.attributeValue("cursed_next_item_id") == null ? 0 : Integer.parseInt(element.attributeValue("cursed_next_item_id"));
			getHolder().addCrystal(new SoulCrystal(itemId, level, nextItemId, cursedNextItemId));
		}
	}
}