package ai;

import l2.commons.util.Rnd;
import l2.gameserver.ai.DefaultAI;
import l2.gameserver.model.instances.NpcInstance;
import l2.gameserver.scripts.Functions;

public class BlacksmithMammon extends DefaultAI
{
	private static final long chatDelay = 1800000;
	private static final String[] mamonText = {"Rulers of the seal! I bring you wondrous gifts!", "Rulers of the seal! I have some excellent weapons to show you!", "I've been so busy lately, in addition to planning my trip!"};
	private long _chatVar;
	
	public BlacksmithMammon(NpcInstance actor)
	{
		super(actor);
	}
	
	@Override
	protected boolean thinkActive()
	{
		NpcInstance actor = getActor();
		if(actor.isDead())
		{
			return true;
		}
		if(_chatVar + 1800000 < System.currentTimeMillis())
		{
			_chatVar = System.currentTimeMillis();
			Functions.npcShout(actor, mamonText[Rnd.get(mamonText.length)]);
		}
		return false;
	}
	
	@Override
	public boolean isGlobalAI()
	{
		return true;
	}
}