package l2.gameserver.stats.conditions;

import l2.gameserver.model.Player;
import l2.gameserver.model.entity.residence.ResidenceType;
import l2.gameserver.model.pledge.Clan;
import l2.gameserver.stats.Env;

public class ConditionPlayerResidence extends Condition
{
	private final int _id;
	private final ResidenceType _type;
	
	public ConditionPlayerResidence(int id, ResidenceType type)
	{
		_id = id;
		_type = type;
	}
	
	@Override
	protected boolean testImpl(Env env)
	{
		if(!env.character.isPlayer())
		{
			return false;
		}
		Player player = (Player) env.character;
		Clan clan = player.getClan();
		if(clan == null)
		{
			return false;
		}
		int residenceId = clan.getResidenceId(_type);
		return _id > 0 ? residenceId == _id : residenceId > 0;
	}
}