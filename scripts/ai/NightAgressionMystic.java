package ai;

import l2.gameserver.GameTimeController;
import l2.gameserver.ai.Mystic;
import l2.gameserver.listener.game.OnDayNightChangeListener;
import l2.gameserver.model.instances.NpcInstance;

public class NightAgressionMystic extends Mystic
{
	public NightAgressionMystic(NpcInstance actor)
	{
		super(actor);
		GameTimeController.getInstance().addListener(new NightAgressionDayNightListener());
	}
	
	private class NightAgressionDayNightListener implements OnDayNightChangeListener
	{
		private NightAgressionDayNightListener()
		{
			if(GameTimeController.getInstance().isNowNight())
			{
				onNight();
			}
			else
			{
				onDay();
			}
		}
		
		@Override
		public void onDay()
		{
			getActor().setAggroRange(0);
		}
		
		@Override
		public void onNight()
		{
			getActor().setAggroRange(-1);
		}
	}
}