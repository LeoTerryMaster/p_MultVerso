package l2.gameserver.handler.voicecommands.impl;

import l2.gameserver.Config;
import l2.gameserver.handler.voicecommands.IVoicedCommandHandler;
import l2.gameserver.model.Player;
import l2.gameserver.model.Zone;
import l2.gameserver.model.entity.oly.ParticipantPool;
import l2.gameserver.network.l2.components.CustomMessage;
import l2.gameserver.scripts.Functions;

public class Offline extends Functions implements IVoicedCommandHandler
{
	private final String[] _commandList = {"offline"};
	
	@Override
	public boolean useVoicedCommand(String command, Player activeChar, String args)
	{
		if(!Config.SERVICES_OFFLINE_TRADE_ALLOW)
		{
			return false;
		}
		if(activeChar.isOlyParticipant() || ParticipantPool.getInstance().isRegistred(activeChar) || activeChar.getKarma() > 0)
		{
			activeChar.sendActionFailed();
			return false;
		}
		if(activeChar.getLevel() < Config.SERVICES_OFFLINE_TRADE_MIN_LEVEL)
		{
			show(new CustomMessage("voicedcommandhandlers.Offline.LowLevel", activeChar).addNumber(Config.SERVICES_OFFLINE_TRADE_MIN_LEVEL), activeChar);
			return false;
		}
		if(!activeChar.isInZone(Zone.ZoneType.offshore) && Config.SERVICES_OFFLINE_TRADE_ALLOW_OFFSHORE)
		{
			show(new CustomMessage("trade.OfflineNoTradeZoneOnlyOffshore", activeChar), activeChar);
			return false;
		}
		if(!activeChar.isInStoreMode())
		{
			show(new CustomMessage("voicedcommandhandlers.Offline.IncorrectUse", activeChar), activeChar);
			return false;
		}
		if(activeChar.getNoChannelRemained() > 0)
		{
			show(new CustomMessage("voicedcommandhandlers.Offline.BanChat", activeChar), activeChar);
			return false;
		}
		if(activeChar.isActionBlocked("open_private_store"))
		{
			show(new CustomMessage("trade.OfflineNoTradeZone", activeChar), activeChar);
			return false;
		}
		if(Config.SERVICES_OFFLINE_TRADE_PRICE > 0 && Config.SERVICES_OFFLINE_TRADE_PRICE_ITEM > 0)
		{
			if(getItemCount(activeChar, Config.SERVICES_OFFLINE_TRADE_PRICE_ITEM) < (long) Config.SERVICES_OFFLINE_TRADE_PRICE)
			{
				show(new CustomMessage("voicedcommandhandlers.Offline.NotEnough", activeChar).addItemName(Config.SERVICES_OFFLINE_TRADE_PRICE_ITEM).addNumber(Config.SERVICES_OFFLINE_TRADE_PRICE), activeChar);
				return false;
			}
			removeItem(activeChar, Config.SERVICES_OFFLINE_TRADE_PRICE_ITEM, Config.SERVICES_OFFLINE_TRADE_PRICE);
		}
		activeChar.offline();
		return true;
	}
	
	@Override
	public String[] getVoicedCommandList()
	{
		return _commandList;
	}
}