package l2.gameserver.network.l2.c2s;

import l2.gameserver.Config;
import l2.gameserver.cache.Msg;
import l2.gameserver.dao.CharacterDAO;
import l2.gameserver.database.mysql;
import l2.gameserver.model.GameObjectsStorage;
import l2.gameserver.model.Player;
import l2.gameserver.model.World;
import l2.gameserver.model.items.ItemInstance;
import l2.gameserver.model.mail.Mail;
import l2.gameserver.network.l2.components.CustomMessage;
import l2.gameserver.network.l2.components.SystemMsg;
import l2.gameserver.network.l2.s2c.ExNoticePostArrived;
import l2.gameserver.network.l2.s2c.ExReplyWritePost;
import l2.gameserver.network.l2.s2c.SystemMessage;
import l2.gameserver.scripts.Functions;
import l2.gameserver.utils.Log;
import l2.gameserver.utils.Util;
import org.apache.commons.lang3.ArrayUtils;

import java.util.ArrayList;
import java.util.HashMap;

public class RequestExSendPost extends L2GameClientPacket
{
	private int _messageType;
	private String _recieverName;
	private String _topic;
	private String _body;
	private int _count;
	private int[] _items;
	private long[] _itemQ;
	private long _price;
	
	@Override
	protected void readImpl()
	{
		_recieverName = readS(35);
		_messageType = readD();
		_topic = readS(127);
		_body = readS(32767);
		_count = readD();
		if(_count * 8 + 4 > _buf.remaining() || _count > 32767 || _count < 1)
		{
			_count = 0;
			return;
		}
		_items = new int[_count];
		_itemQ = new long[_count];
		for(int i = 0;i < _count;++i)
		{
			_items[i] = readD();
			_itemQ[i] = readD();
			if(_itemQ[i] >= 1 && ArrayUtils.indexOf(_items, _items[i]) >= i)
				continue;
			_count = 0;
			return;
		}
		_price = readQ();
		if(_price < 0)
		{
			_count = 0;
			_price = 0;
		}
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
		if(activeChar.isGM() && _recieverName.equalsIgnoreCase("ONLINE_ALL"))
		{
			HashMap<Integer, Long> map = new HashMap<>();
			if(_items != null && _items.length > 0)
			{
				for(int i = 0;i < _items.length;++i)
				{
					ItemInstance item = activeChar.getInventory().getItemByObjectId(_items[i]);
					map.put(item.getItemId(), _itemQ[i]);
				}
			}
			for(Player p : GameObjectsStorage.getAllPlayersForIterate())
			{
				if(p == null || !p.isOnline())
					continue;
				Functions.sendSystemMail(p, _topic, _body, map);
			}
			activeChar.sendPacket(ExReplyWritePost.STATIC_TRUE);
			activeChar.sendPacket(Msg.MAIL_SUCCESSFULLY_SENT);
			return;
		}
		if(!Config.ALLOW_MAIL)
		{
			activeChar.sendMessage(new CustomMessage("mail.Disabled", activeChar));
			activeChar.sendActionFailed();
			return;
		}
		if(activeChar.isInStoreMode())
		{
			activeChar.sendPacket(Msg.YOU_CANNOT_FORWARD_BECAUSE_THE_PRIVATE_SHOP_OR_WORKSHOP_IS_IN_PROGRESS);
			return;
		}
		if(activeChar.isInTrade())
		{
			activeChar.sendPacket(Msg.YOU_CANNOT_FORWARD_DURING_AN_EXCHANGE);
			return;
		}
		if(activeChar.getEnchantScroll() != null)
		{
			activeChar.sendPacket(Msg.YOU_CANNOT_FORWARD_DURING_AN_ITEM_ENHANCEMENT_OR_ATTRIBUTE_ENHANCEMENT);
			return;
		}
		if(activeChar.getName().equalsIgnoreCase(_recieverName))
		{
			activeChar.sendPacket(Msg.YOU_CANNOT_SEND_A_MAIL_TO_YOURSELF);
			return;
		}
		if(_count > 0 && !activeChar.isInPeaceZone())
		{
			activeChar.sendPacket(Msg.YOU_CANNOT_FORWARD_IN_A_NON_PEACE_ZONE_LOCATION);
			return;
		}
		if(activeChar.isFishing())
		{
			activeChar.sendPacket(Msg.YOU_CANNOT_DO_THAT_WHILE_FISHING);
			return;
		}
		if(!activeChar.antiFlood.canMail())
		{
			activeChar.sendPacket(Msg.THE_PREVIOUS_MAIL_WAS_FORWARDED_LESS_THAN_1_MINUTE_AGO_AND_THIS_CANNOT_BE_FORWARDED);
			return;
		}
		if(_price > 0)
		{
			if(!activeChar.getPlayerAccess().UseTrade)
			{
				activeChar.sendPacket(Msg.THIS_ACCOUNT_CANOT_TRADE_ITEMS);
				activeChar.sendActionFailed();
				return;
			}
			String tradeBan = activeChar.getVar("tradeBan");
			if(tradeBan != null && (tradeBan.equals("-1") || Long.parseLong(tradeBan) >= System.currentTimeMillis()))
			{
				if(tradeBan.equals("-1"))
				{
					activeChar.sendMessage(new CustomMessage("common.TradeBannedPermanently", activeChar));
				}
				else
				{
					activeChar.sendMessage(new CustomMessage("common.TradeBanned", activeChar).addString(Util.formatTime((int) (Long.parseLong(tradeBan) / 1000 - System.currentTimeMillis() / 1000))));
				}
				return;
			}
		}
		if(activeChar.isInBlockList(_recieverName))
		{
			activeChar.sendPacket(new SystemMessage(2057).addString(_recieverName));
			return;
		}
		Player target = World.getPlayer(_recieverName);
		int recieverId;
		if(target != null)
		{
			recieverId = target.getObjectId();
			_recieverName = target.getName();
			if(target.isInBlockList(activeChar))
			{
				activeChar.sendPacket(new SystemMessage(1228).addString(_recieverName));
				return;
			}
		}
		else
		{
			recieverId = CharacterDAO.getInstance().getObjectIdByName(_recieverName);
			if(recieverId > 0 && mysql.simple_get_int("target_Id", "character_blocklist", "obj_Id=" + recieverId + " AND target_Id=" + activeChar.getObjectId()) > 0)
			{
				activeChar.sendPacket(new SystemMessage(1228).addString(_recieverName));
				return;
			}
		}
		if(recieverId == 0)
		{
			activeChar.sendPacket(Msg.WHEN_THE_RECIPIENT_DOESN_T_EXIST_OR_THE_CHARACTER_HAS_BEEN_DELETED_SENDING_MAIL_IS_NOT_POSSIBLE);
			return;
		}
		int expireTime = (_messageType == 1 ? 12 : 360) * 3600 + (int) (System.currentTimeMillis() / 1000);
		if(_count > 8)
		{
			activeChar.sendPacket(SystemMsg.INCORRECT_ITEM_COUNT);
			return;
		}
		long serviceCost = 100 + _count * 1000;
		ArrayList<ItemInstance> attachments = new ArrayList<>();
		activeChar.getInventory().writeLock();
		try
		{
			if(activeChar.getAdena() < serviceCost)
			{
				activeChar.sendPacket(Msg.YOU_CANNOT_FORWARD_BECAUSE_YOU_DON_T_HAVE_ENOUGH_ADENA);
				return;
			}
			int i;
			ItemInstance item;
			if(_count > 0)
			{
				for(i = 0;i < _count;++i)
				{
					item = activeChar.getInventory().getItemByObjectId(_items[i]);
					if(item != null && item.getCount() >= _itemQ[i] && (item.getItemId() != 57 || item.getCount() >= _itemQ[i] + serviceCost) && item.canBeTraded(activeChar))
						continue;
					activeChar.sendPacket(Msg.THE_ITEM_THAT_YOU_RE_TRYING_TO_SEND_CANNOT_BE_FORWARDED_BECAUSE_IT_ISN_T_PROPER);
					return;
				}
			}
			if(!activeChar.reduceAdena(serviceCost, true))
			{
				activeChar.sendPacket(Msg.YOU_CANNOT_FORWARD_BECAUSE_YOU_DON_T_HAVE_ENOUGH_ADENA);
				return;
			}
			if(_count > 0)
			{
				for(i = 0;i < _count;++i)
				{
					item = activeChar.getInventory().removeItemByObjectId(_items[i], _itemQ[i]);
					Log.LogItem(activeChar, Log.ItemLog.PostSend, item);
					item.setOwnerId(activeChar.getObjectId());
					item.setLocation(ItemInstance.ItemLocation.MAIL);
					item.save();
					attachments.add(item);
				}
			}
		}
		finally
		{
			activeChar.getInventory().writeUnlock();
		}
		Mail mail = new Mail();
		mail.setSenderId(activeChar.getObjectId());
		mail.setSenderName(activeChar.getName());
		mail.setReceiverId(recieverId);
		mail.setReceiverName(_recieverName);
		mail.setTopic(_topic);
		mail.setBody(_body);
		mail.setPrice(_messageType > 0 ? _price : 0);
		mail.setUnread(true);
		mail.setType(Mail.SenderType.NORMAL);
		mail.setExpireTime(expireTime);
		for(ItemInstance item : attachments)
		{
			mail.addAttachment(item);
		}
		mail.save();
		activeChar.sendPacket(ExReplyWritePost.STATIC_TRUE);
		activeChar.sendPacket(Msg.MAIL_SUCCESSFULLY_SENT);
		if(target != null)
		{
			target.sendPacket(ExNoticePostArrived.STATIC_TRUE);
			target.sendPacket(Msg.THE_MAIL_HAS_ARRIVED);
		}
	}
}