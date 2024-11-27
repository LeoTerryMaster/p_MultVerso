package l2.gameserver.model.actor.recorder;

import l2.gameserver.model.instances.NpcInstance;

public class NpcStatsChangeRecorder extends CharStatsChangeRecorder<NpcInstance>
{
	public NpcStatsChangeRecorder(NpcInstance actor)
	{
		super(actor);
	}
	
	@Override
	protected void onSendChanges()
	{
		super.onSendChanges();
		if((_changes & 1) == 1)
		{
			_activeChar.broadcastCharInfo();
		}
	}
}