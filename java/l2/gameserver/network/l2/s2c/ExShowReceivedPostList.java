package l2.gameserver.network.l2.s2c;

import l2.commons.collections.CollectionUtils;
import l2.gameserver.dao.MailDAO;
import l2.gameserver.model.Player;
import l2.gameserver.model.mail.Mail;

import java.util.List;

public class ExShowReceivedPostList extends L2GameServerPacket
{
	private final List<Mail> mails;
	
	public ExShowReceivedPostList(Player cha)
	{
		mails = MailDAO.getInstance().getReceivedMailByOwnerId(cha.getObjectId());
		CollectionUtils.eqSort(mails);
	}
	
	@Override
	protected void writeImpl()
	{
		writeEx(170);
		writeD((int) (System.currentTimeMillis() / 1000));
		writeD(mails.size());
		for(Mail mail : mails)
		{
			writeD(mail.getMessageId());
			writeS(mail.getTopic());
			writeS(mail.getSenderName());
			writeD(mail.isPayOnDelivery() ? 1 : 0);
			writeD(mail.getExpireTime());
			writeD(mail.isUnread() ? 1 : 0);
			writeD(mail.getType() == Mail.SenderType.NORMAL ? 0 : 1);
			writeD(mail.getAttachments().isEmpty() ? 0 : 1);
			writeD(0);
			writeD(mail.getType().ordinal());
			writeD(0);
		}
	}
}