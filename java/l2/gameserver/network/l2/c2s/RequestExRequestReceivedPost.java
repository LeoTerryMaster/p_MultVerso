package l2.gameserver.network.l2.c2s;

import l2.commons.dao.JdbcEntityState;
import l2.gameserver.dao.MailDAO;
import l2.gameserver.model.Player;
import l2.gameserver.model.mail.Mail;
import l2.gameserver.network.l2.s2c.ExChangePostState;
import l2.gameserver.network.l2.s2c.ExReplyReceivedPost;
import l2.gameserver.network.l2.s2c.ExShowReceivedPostList;

public class RequestExRequestReceivedPost extends L2GameClientPacket
{
	private int postId;
	
	@Override
	protected void readImpl()
	{
		postId = readD();
	}
	
	@Override
	protected void runImpl()
	{
		Player activeChar = getClient().getActiveChar();
		if(activeChar == null)
		{
			return;
		}
		Mail mail = MailDAO.getInstance().getReceivedMailByMailId(activeChar.getObjectId(), postId);
		if(mail != null)
		{
			if(mail.isUnread())
			{
				mail.setUnread(false);
				mail.setJdbcState(JdbcEntityState.UPDATED);
				mail.update();
				activeChar.sendPacket(new ExChangePostState(true, 1, mail));
			}
			activeChar.sendPacket(new ExReplyReceivedPost(mail));
			return;
		}
		activeChar.sendPacket(new ExShowReceivedPostList(activeChar));
	}
}