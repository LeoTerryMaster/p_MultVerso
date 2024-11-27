package l2.gameserver.network.l2.s2c;

import l2.gameserver.model.items.ItemInstance;
import l2.gameserver.model.mail.Mail;

public class ExReplySentPost extends L2GameServerPacket
{
	private final Mail mail;
	
	public ExReplySentPost(Mail mail)
	{
		this.mail = mail;
	}
	
	@Override
	protected void writeImpl()
	{
		writeEx(173);
		writeD(mail.getMessageId());
		writeD(mail.isPayOnDelivery() ? 1 : 0);
		writeS(mail.getReceiverName());
		writeS(mail.getTopic());
		writeS(mail.getBody());
		writeD(mail.getAttachments().size());
		for(ItemInstance item : mail.getAttachments())
		{
			writeItemInfo(item);
			writeD(item.getObjectId());
		}
		writeQ(mail.getPrice());
		writeD(0);
	}
}