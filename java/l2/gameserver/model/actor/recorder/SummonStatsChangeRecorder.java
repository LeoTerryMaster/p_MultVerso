package l2.gameserver.model.actor.recorder;

import l2.gameserver.model.Summon;

public class SummonStatsChangeRecorder extends CharStatsChangeRecorder<Summon>
{
	public SummonStatsChangeRecorder(Summon actor)
	{
		super(actor);
	}
	
	@Override
	protected void onSendChanges()
	{
		super.onSendChanges();
		if((_changes & 2) == 2)
		{
			_activeChar.sendPetInfo();
		}
		else if((_changes & 1) == 1)
		{
			_activeChar.broadcastCharInfo();
		}
	}
}