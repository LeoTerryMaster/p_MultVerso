package l2.gameserver.network.l2.c2s;

import l2.gameserver.dao.MailDAO;
import l2.gameserver.model.Player;
import l2.gameserver.model.mail.Mail;
import l2.gameserver.network.l2.s2c.ExReplySentPost;
import l2.gameserver.network.l2.s2c.ExShowSentPostList;

public class RequestExRequestSentPost extends L2GameClientPacket
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
		Mail mail = MailDAO.getInstance().getSentMailByMailId(activeChar.getObjectId(), postId);
		if(mail != null)
		{
			activeChar.sendPacket(new ExReplySentPost(mail));
			return;
		}
		activeChar.sendPacket(new ExShowSentPostList(activeChar));
	}
}