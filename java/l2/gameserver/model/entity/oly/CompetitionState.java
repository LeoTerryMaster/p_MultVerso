package l2.gameserver.model.entity.oly;

public enum CompetitionState
{
	INIT(0),
	STAND_BY(1),
	PLAYING(2),
	FINISH(0);
	
	private final int _state_id;
	
	CompetitionState(int state_id)
	{
		_state_id = state_id;
	}
	
	public int getStateId()
	{
		return _state_id;
	}
}