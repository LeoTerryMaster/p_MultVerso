package l2.gameserver.data.xml;

import l2.gameserver.data.StringHolder;
import l2.gameserver.data.htm.HtmCache;
import l2.gameserver.data.xml.holder.BuyListHolder;
import l2.gameserver.data.xml.holder.MultiSellHolder;
import l2.gameserver.data.xml.parser.*;
import l2.gameserver.instancemanager.ReflectionManager;
import l2.gameserver.tables.SkillTable;

public abstract class Parsers
{
	public static void parseAll()
	{
		HtmCache.getInstance().reload();
		StringHolder.getInstance().load();
		EnchantSkillParser.getInstance().load();
		SkillTable.getInstance().load();
		OptionDataParser.getInstance().load();
		ItemParser.getInstance().load();
		VariationGroupParser.getInstance().load();
		VariationChanceParser.getInstance().load();
		NpcParser.getInstance().load();
		DomainParser.getInstance().load();
		RestartPointParser.getInstance().load();
		StaticObjectParser.getInstance().load();
		DoorParser.getInstance().load();
		ZoneParser.getInstance().load();
		SpawnParser.getInstance().load();
		InstantZoneParser.getInstance().load();
		ReflectionManager.getInstance();
		SkillAcquireParser.getInstance().load();
		ResidenceParser.getInstance().load();
		EventParser.getInstance().load();
		CubicParser.getInstance().load();
		RecipeParser.getInstance().load();
		BuyListHolder.getInstance();
		MultiSellHolder.getInstance();
		HennaParser.getInstance().load();
		EnchantItemParser.getInstance().load();
		SoulCrystalParser.getInstance().load();
		ArmorSetsParser.getInstance().load();
		FishDataParser.getInstance().load();
		CapsuleItemParser.getInstance().load();
	}
}