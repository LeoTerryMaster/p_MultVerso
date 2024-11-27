package l2.gameserver.taskmanager.actionrunner.tasks;

import l2.commons.dao.JdbcEntityState;
import l2.gameserver.cache.Msg;
import l2.gameserver.dao.MailDAO;
import l2.gameserver.model.Player;
import l2.gameserver.model.World;
import l2.gameserver.model.mail.Mail;
import l2.gameserver.network.l2.s2c.ExNoticePostArrived;

import java.util.List;

public class DeleteExpiredMailTask extends AutomaticTask
{
	@Override
	public void doTask() throws Exception
	{
		int expireTime = (int) (System.currentTimeMillis() / 1000);
		List<Mail> mails = MailDAO.getInstance().getExpiredMail(expireTime);
		for(Mail mail : mails)
		{
			if(!mail.getAttachments().isEmpty())
			{
				if(mail.getType() == Mail.SenderType.NORMAL)
				{
					Player player = World.getPlayer(mail.getSenderId());
					Mail reject = mail.reject();
					MailDAO.getInstance().deleteReceivedMailByMailId(mail.getReceiverId(), mail.getMessageId());
					MailDAO.getInstance().deleteSentMailByMailId(mail.getReceiverId(), mail.getMessageId());
					mail.delete();
					reject.setExpireTime(expireTime + 1296000);
					reject.save();
					if(player == null)
						continue;
					player.sendPacket(ExNoticePostArrived.STATIC_TRUE);
					player.sendPacket(Msg.THE_MAIL_HAS_ARRIVED);
					continue;
				}
				mail.setExpireTime(expireTime + 86400);
				mail.setJdbcState(JdbcEntityState.UPDATED);
				mail.update();
				continue;
			}
			MailDAO.getInstance().deleteReceivedMailByMailId(mail.getReceiverId(), mail.getMessageId());
			MailDAO.getInstance().deleteSentMailByMailId(mail.getReceiverId(), mail.getMessageId());
			mail.delete();
		}
	}
	
	@Override
	public long reCalcTime(boolean start)
	{
		return System.currentTimeMillis() + 600000;
	}
}