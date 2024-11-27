package l2.gameserver.network.l2.s2c;

import l2.gameserver.model.entity.oly.HeroController;

import java.util.ArrayList;
import java.util.Collection;

public class ExHeroList extends L2GameServerPacket
{
	private final Collection<HeroController.HeroRecord> heroes = new ArrayList<>();
	
	public ExHeroList()
	{
		for(HeroController.HeroRecord hr : HeroController.getInstance().getCurrentHeroes())
		{
			if(hr == null || !hr.active || !hr.played)
				continue;
			heroes.add(hr);
		}
	}
	
	@Override
	protected final void writeImpl()
	{
		writeEx(35);
		writeD(heroes.size());
		for(HeroController.HeroRecord hero : heroes)
		{
			writeS(hero.name);
			writeD(hero.class_id);
			writeS(hero.clan_name);
			writeD(hero.clan_crest);
			writeS(hero.ally_name);
			writeD(hero.ally_crest);
			writeD(hero.count);
		}
	}
}