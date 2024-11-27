package ai.door;

import l2.gameserver.ai.DoorAI;
import l2.gameserver.data.xml.holder.ResidenceHolder;
import l2.gameserver.listener.actor.player.OnAnswerListener;
import l2.gameserver.model.Player;
import l2.gameserver.model.entity.residence.Residence;
import l2.gameserver.model.instances.DoorInstance;
import l2.gameserver.network.l2.components.SystemMsg;
import l2.gameserver.network.l2.s2c.ConfirmDlg;

public class ResidenceDoor extends DoorAI
{
	public ResidenceDoor(DoorInstance actor)
	{
		super(actor);
	}
	
	@Override
	public void onEvtTwiceClick(Player player)
	{
		DoorInstance door = getActor();
		Residence residence = ResidenceHolder.getInstance().getResidence(door.getTemplate().getAIParams().getInteger("residence_id"));
		if(residence.getOwner() != null && player.getClan() != null && player.getClan() == residence.getOwner() && (player.getClanPrivileges() & 32768) == 32768)
		{
			SystemMsg msg = door.isOpen() ? SystemMsg.WOULD_YOU_LIKE_TO_CLOSE_THE_GATE : SystemMsg.WOULD_YOU_LIKE_TO_OPEN_THE_GATE;
			player.ask(new ConfirmDlg(msg, 0), new OnAnswerListener()
			{
				
				@Override
				public void sayYes()
				{
					if(door.isOpen())
					{
						door.closeMe(player, true);
					}
					else
					{
						door.openMe(player, true);
					}
				}
				
				@Override
				public void sayNo()
				{
				}
			});
		}
	}
}