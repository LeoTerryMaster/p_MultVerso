package l2.gameserver.stats.conditions;

import l2.gameserver.model.Player;
import l2.gameserver.stats.Env;

public class ConditionPlayerGender extends Condition
{
	private final Gender _gender;
	
	public ConditionPlayerGender(Gender gender)
	{
		_gender = gender;
	}
	
	public ConditionPlayerGender(String gender)
	{
		this(Gender.valueOf(gender.toUpperCase()));
	}
	
	@Override
	protected boolean testImpl(Env env)
	{
		if(!env.character.isPlayer())
		{
			return false;
		}
		return ((Player) env.character).getSex() == _gender.getPlayerGenderId();
	}
	
	public enum Gender
	{
		MALE(0),
		FEMALE(1);
		
		private final int _playerGenderId;
		
		Gender(int playerGenderId)
		{
			_playerGenderId = playerGenderId;
		}
		
		public int getPlayerGenderId()
		{
			return _playerGenderId;
		}
	}
}