package l2.gameserver.model.actor.recorder;

import l2.gameserver.model.Creature;
import l2.gameserver.model.base.TeamType;

public class CharStatsChangeRecorder<T extends Creature>
{
	public static final int BROADCAST_CHAR_INFO = 1;
	public static final int SEND_CHAR_INFO = 2;
	public static final int SEND_STATUS_INFO = 4;
	protected final T _activeChar;
	protected int _level;
	protected int _accuracy;
	protected int _attackSpeed;
	protected int _castSpeed;
	protected int _criticalHit;
	protected int _evasion;
	protected int _magicAttack;
	protected int _magicDefence;
	protected int _maxHp;
	protected int _maxMp;
	protected int _physicAttack;
	protected int _physicDefence;
	protected int _runSpeed;
	protected int _abnormalEffects;
	protected int _abnormalEffects2;
	protected int _abnormalEffects3;
	protected TeamType _team;
	protected int _changes;
	
	public CharStatsChangeRecorder(T actor)
	{
		_activeChar = actor;
	}
	
	protected int set(int flag, int oldValue, int newValue)
	{
		if(oldValue != newValue)
		{
			_changes |= flag;
		}
		return newValue;
	}
	
	protected long set(int flag, long oldValue, long newValue)
	{
		if(oldValue != newValue)
		{
			_changes |= flag;
		}
		return newValue;
	}
	
	protected String set(int flag, String oldValue, String newValue)
	{
		if(!oldValue.equals(newValue))
		{
			_changes |= flag;
		}
		return newValue;
	}
	
	protected <E extends Enum<E>> E set(int flag, E oldValue, E newValue)
	{
		if(oldValue != newValue)
		{
			_changes |= flag;
		}
		return newValue;
	}
	
	protected void refreshStats()
	{
		_accuracy = set(2, _accuracy, _activeChar.getAccuracy());
		_attackSpeed = set(1, _attackSpeed, _activeChar.getPAtkSpd());
		_castSpeed = set(1, _castSpeed, _activeChar.getMAtkSpd());
		_criticalHit = set(2, _criticalHit, _activeChar.getCriticalHit(null, null));
		_evasion = set(2, _evasion, _activeChar.getEvasionRate(null));
		_runSpeed = set(1, _runSpeed, _activeChar.getRunSpeed());
		_physicAttack = set(2, _physicAttack, _activeChar.getPAtk(null));
		_physicDefence = set(2, _physicDefence, _activeChar.getPDef(null));
		_magicAttack = set(2, _magicAttack, _activeChar.getMAtk(null, null));
		_magicDefence = set(2, _magicDefence, _activeChar.getMDef(null, null));
		_maxHp = set(4, _maxHp, _activeChar.getMaxHp());
		_maxMp = set(4, _maxMp, _activeChar.getMaxMp());
		_level = set(2, _level, _activeChar.getLevel());
		_abnormalEffects = set(1, _abnormalEffects, _activeChar.getAbnormalEffect());
		_abnormalEffects2 = set(1, _abnormalEffects2, _activeChar.getAbnormalEffect2());
		_abnormalEffects3 = set(1, _abnormalEffects3, _activeChar.getAbnormalEffect3());
		_team = set(1, _team, _activeChar.getTeam());
	}
	
	public final void sendChanges()
	{
		refreshStats();
		onSendChanges();
		_changes = 0;
	}
	
	protected void onSendChanges()
	{
		if((_changes & 4) == 4)
		{
			_activeChar.broadcastStatusUpdate();
		}
	}
}