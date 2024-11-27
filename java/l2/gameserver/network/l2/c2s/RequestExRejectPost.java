package l2.gameserver.network.l2.c2s;

import l2.gameserver.cache.Msg;
import l2.gameserver.dao.MailDAO;
import l2.gameserver.model.Player;
import l2.gameserver.model.World;
import l2.gameserver.model.mail.Mail;
import l2.gameserver.network.l2.s2c.ExNoticePostArrived;
import l2.gameserver.network.l2.s2c.ExShowReceivedPostList;

public class RequestExRejectPost extends L2GameClientPacket
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
		if(activeChar.isActionsDisabled())
		{
			activeChar.sendActionFailed();
			return;
		}
		if(activeChar.isInStoreMode())
		{
			activeChar.sendPacket(Msg.YOU_CANNOT_CANCEL_BECAUSE_THE_PRIVATE_SHOP_OR_WORKSHOP_IS_IN_PROGRESS);
			return;
		}
		if(activeChar.isInTrade())
		{
			activeChar.sendPacket(Msg.YOU_CANNOT_CANCEL_DURING_AN_EXCHANGE);
			return;
		}
		if(activeChar.getEnchantScroll() != null)
		{
			activeChar.sendPacket(Msg.YOU_CANNOT_CANCEL_DURING_AN_ITEM_ENHANCEMENT_OR_ATTRIBUTE_ENHANCEMENT);
			return;
		}
		if(!activeChar.isInPeaceZone())
		{
			activeChar.sendPacket(Msg.YOU_CANNOT_CANCEL_IN_A_NON_PEACE_ZONE_LOCATION);
			return;
		}
		if(activeChar.isFishing())
		{
			activeChar.sendPacket(Msg.YOU_CANNOT_DO_THAT_WHILE_FISHING);
			return;
		}
		Mail mail = MailDAO.getInstance().getReceivedMailByMailId(activeChar.getObjectId(), postId);
		if(mail != null)
		{
			if(mail.getType() != Mail.SenderType.NORMAL || mail.getAttachments().isEmpty())
			{
				activeChar.sendActionFailed();
				return;
			}
			int expireTime = 1296000 + (int) (System.currentTimeMillis() / 1000);
			Mail reject = mail.reject();
			MailDAO.getInstance().deleteReceivedMailByMailId(mail.getReceiverId(), mail.getMessageId());
			MailDAO.getInstance().deleteSentMailByMailId(mail.getReceiverId(), mail.getMessageId());
			reject.setExpireTime(expireTime);
			reject.save();
			Player sender = World.getPlayer(reject.getReceiverId());
			if(sender != null)
			{
				sender.sendPacket(ExNoticePostArrived.STATIC_TRUE);
			}
		}
		activeChar.sendPacket(new ExShowReceivedPostList(activeChar));
	}
}