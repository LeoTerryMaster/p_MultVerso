package l2.gameserver.network.l2.c2s;

import l2.commons.lang.ArrayUtils;
import l2.gameserver.Config;
import l2.gameserver.cache.ItemInfoCache;
import l2.gameserver.cache.Msg;
import l2.gameserver.handler.voicecommands.IVoicedCommandHandler;
import l2.gameserver.handler.voicecommands.VoicedCommandHandler;
import l2.gameserver.instancemanager.PetitionManager;
import l2.gameserver.model.GameObjectsStorage;
import l2.gameserver.model.Player;
import l2.gameserver.model.World;
import l2.gameserver.model.chat.ChatFilters;
import l2.gameserver.model.chat.chatfilter.ChatFilter;
import l2.gameserver.model.chat.chatfilter.ChatMsg;
import l2.gameserver.model.items.ItemInstance;
import l2.gameserver.model.matching.MatchingRoom;
import l2.gameserver.network.l2.components.ChatType;
import l2.gameserver.network.l2.components.CustomMessage;
import l2.gameserver.network.l2.s2c.ActionFail;
import l2.gameserver.network.l2.s2c.Say2;
import l2.gameserver.network.l2.s2c.SystemMessage;
import l2.gameserver.utils.Log;
import l2.gameserver.utils.MapUtils;
import l2.gameserver.utils.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Say2C extends L2GameClientPacket
{
	private static final Logger _log = LoggerFactory.getLogger(Say2C.class);
	private static final Pattern EX_ITEM_LINK_PATTERN = Pattern.compile("[\b]\tType=[0-9]+[\\s]+\tID=([0-9]+)[\\s]+\tColor=[0-9]+[\\s]+\tUnderline=[0-9]+[\\s]+\tTitle=\u001b(.[^\u001b]*)[^\b]");
	private static final Pattern SKIP_ITEM_LINK_PATTERN = Pattern.compile("[\b]\tType=[0-9]+(.[^\b]*)[\b]");
	private String _text;
	private ChatType _type;
	private String _target;
	
	private static void shout(Player activeChar, Say2 cs)
	{
		int rx = MapUtils.regionX(activeChar);
		int ry = MapUtils.regionY(activeChar);
		int offset = Config.SHOUT_OFFSET;
		for(Player player : GameObjectsStorage.getAllPlayersForIterate())
		{
			if(player == activeChar || activeChar.getReflection() != player.getReflection() || player.isBlockAll() || player.isInBlockList(activeChar))
				continue;
			int tx = MapUtils.regionX(player);
			int ty = MapUtils.regionY(player);
			if((tx < rx - offset || tx > rx + offset || ty < ry - offset || ty > ry + offset) && !activeChar.isInRangeZ(player, (long) Config.CHAT_RANGE))
				continue;
			player.sendPacket(cs);
		}
	}
	
	private static void announce(Player activeChar, Say2 cs)
	{
		for(Player player : GameObjectsStorage.getAllPlayersForIterate())
		{
			if(player == activeChar || activeChar.getReflection() != player.getReflection() || player.isBlockAll() || player.isInBlockList(activeChar))
				continue;
			player.sendPacket(cs);
		}
	}
	
	@Override
	protected void readImpl()
	{
		_text = readS(Config.CHAT_MESSAGE_MAX_LEN);
		_type = ArrayUtils.valid(ChatType.VALUES, readD());
		_target = _type == ChatType.TELL ? readS(Config.CNAME_MAXLEN) : null;
	}
	
	@Override
	protected void runImpl()
	{
		Player activeChar = getClient().getActiveChar();
		if(activeChar == null)
		{
			return;
		}
		if(_type == null || _text == null || _text.length() == 0)
		{
			activeChar.sendActionFailed();
			return;
		}
		_text = _text.replaceAll("\\\\n", "\n");
		if(_text.contains("\n"))
		{
			String[] lines = _text.split("\n");
			_text = "";
			for(int i = 0;i < lines.length;++i)
			{
				lines[i] = lines[i].trim();
				if(lines[i].length() == 0)
					continue;
				if(_text.length() > 0)
				{
					_text = _text + "\n  >";
				}
				_text = _text + lines[i];
			}
		}
		if(_text.length() == 0)
		{
			activeChar.sendActionFailed();
			return;
		}
		if(_text.startsWith("."))
		{
			IVoicedCommandHandler vch;
			String fullcmd = _text.substring(1).trim();
			String command = fullcmd.split("\\s+")[0];
			if(command.length() > 0 && (vch = VoicedCommandHandler.getInstance().getVoicedCommandHandler(command)) != null)
			{
				String args = fullcmd.substring(command.length()).trim();
				vch.useVoicedCommand(command, activeChar, args);
				return;
			}
			activeChar.sendMessage(new CustomMessage("common.command404", activeChar));
			return;
		}
		Player receiver = _target == null ? null : World.getPlayer(_target);
		long currentTimeMillis = System.currentTimeMillis();
		if(!activeChar.getPlayerAccess().CanAnnounce)
		{
			block23:
			for(ChatFilter f : ChatFilters.getinstance().getFilters())
			{
				if(!f.isMatch(activeChar, _type, _text, receiver))
					continue;
				switch(f.getAction())
				{
					case 1:
					{
						activeChar.updateNoChannel((long) Integer.parseInt(f.getValue()) * 1000);
						break block23;
					}
					case 2:
					{
						activeChar.sendMessage(new CustomMessage(f.getValue(), activeChar));
						return;
					}
					case 3:
					{
						_text = f.getValue();
						break block23;
					}
					case 4:
					{
						_type = ChatType.valueOf(f.getValue());
					}
					default:
					{
						continue block23;
					}
				}
			}
		}
		if(activeChar.getNoChannel() > 0 && org.apache.commons.lang3.ArrayUtils.contains(Config.BAN_CHANNEL_LIST, _type))
		{
			if(activeChar.getNoChannelRemained() > 0)
			{
				long timeRemained = activeChar.getNoChannelRemained() / 60000;
				activeChar.sendMessage(new CustomMessage("common.ChatBanned", activeChar).addNumber(timeRemained));
				return;
			}
			activeChar.updateNoChannel(0);
		}
		if(_text.isEmpty())
		{
			return;
		}
		Matcher m = EX_ITEM_LINK_PATTERN.matcher(_text);
		while(m.find())
		{
			int objectId = Integer.parseInt(m.group(1));
			ItemInstance item = activeChar.getInventory().getItemByObjectId(objectId);
			if(item == null)
			{
				activeChar.sendActionFailed();
				break;
			}
			ItemInfoCache.getInstance().put(item);
		}
		String translit;
		if((translit = activeChar.getVar("translit")) != null)
		{
			m = SKIP_ITEM_LINK_PATTERN.matcher(_text);
			StringBuilder sb = new StringBuilder();
			int end = 0;
			while(m.find())
			{
				int n = end;
				end = m.start();
				sb.append(Strings.fromTranslit(_text.substring(n, end), translit.equals("tl") ? 1 : 2));
				int n2 = end;
				end = m.end();
				sb.append(_text.substring(n2, end));
			}
			_text = sb.append(Strings.fromTranslit(_text.substring(end, _text.length()), translit.equals("tl") ? 1 : 2)).toString();
		}
		Say2 cs = new Say2(activeChar.getObjectId(), _type, activeChar.getName(), _text);
		switch(_type)
		{
			case TELL:
			{
				if(receiver != null && receiver.isInOfflineMode())
				{
					activeChar.sendMessage("The person is in offline trade mode.");
					activeChar.sendActionFailed();
					break;
				}
				if(receiver != null && !receiver.isInBlockList(activeChar) && !receiver.isBlockAll())
				{
					if(!receiver.getMessageRefusal())
					{
						if(activeChar.antiFlood.canTell(receiver.getObjectId(), _text))
						{
							receiver.sendPacket(cs);
						}
						cs = new Say2(activeChar.getObjectId(), _type, "->" + receiver.getName(), _text);
						activeChar.sendPacket(cs);
						break;
					}
					activeChar.sendPacket(Msg.THE_PERSON_IS_IN_A_MESSAGE_REFUSAL_MODE);
					break;
				}
				if(receiver == null)
				{
					activeChar.sendPacket(new SystemMessage(3).addString(_target), ActionFail.STATIC);
					break;
				}
				activeChar.sendPacket(Msg.YOU_HAVE_BEEN_BLOCKED_FROM_THE_CONTACT_YOU_SELECTED, ActionFail.STATIC);
				break;
			}
			case SHOUT:
			{
				if(activeChar.isCursedWeaponEquipped())
				{
					activeChar.sendMessage(new CustomMessage("SHOUT_AND_TRADE_CHATING_CANNOT_BE_USED_SHILE_POSSESSING_A_CURSED_WEAPON", activeChar));
					return;
				}
				if(activeChar.isInObserverMode())
				{
					activeChar.sendPacket(Msg.YOU_CANNOT_CHAT_LOCALLY_WHILE_OBSERVING);
					return;
				}
				if(!activeChar.isGM() && !activeChar.antiFlood.canShout(_text))
				{
					activeChar.sendMessage("Shout chat is allowed once per 5 seconds.");
					return;
				}
				if(Config.GLOBAL_SHOUT && activeChar.getLevel() > Config.GLOBAL_SHOUT_MIN_LEVEL && activeChar.getPvpKills() >= Config.GLOBAL_SHOUT_MIN_PVP_COUNT)
				{
					announce(activeChar, cs);
				}
				else
				{
					shout(activeChar, cs);
				}
				activeChar.sendPacket(cs);
				break;
			}
			case TRADE:
			{
				if(activeChar.isCursedWeaponEquipped())
				{
					activeChar.sendMessage(new CustomMessage("SHOUT_AND_TRADE_CHATING_CANNOT_BE_USED_SHILE_POSSESSING_A_CURSED_WEAPON", activeChar));
					return;
				}
				if(activeChar.isInObserverMode())
				{
					activeChar.sendPacket(Msg.YOU_CANNOT_CHAT_LOCALLY_WHILE_OBSERVING);
					return;
				}
				if(!activeChar.isGM() && !activeChar.antiFlood.canTrade(_text))
				{
					activeChar.sendMessage("Trade chat is allowed once per 5 seconds.");
					return;
				}
				if(Config.GLOBAL_TRADE_CHAT && activeChar.getLevel() > Config.GLOBAL_SHOUT_MIN_LEVEL && activeChar.getPvpKills() >= Config.GLOBAL_SHOUT_MIN_PVP_COUNT)
				{
					announce(activeChar, cs);
				}
				else
				{
					shout(activeChar, cs);
				}
				activeChar.sendPacket(cs);
				break;
			}
			case ALL:
			{
				if(activeChar.isCursedWeaponEquipped())
				{
					cs = new Say2(activeChar.getObjectId(), _type, activeChar.getTransformationName(), _text);
				}
				List<Player> list = World.getAroundPlayers(activeChar);
				if(list != null)
				{
					for(Player player : list)
					{
						if(player == activeChar || player.getReflection() != activeChar.getReflection() || player.isBlockAll() || player.isInBlockList(activeChar))
							continue;
						player.sendPacket(cs);
					}
				}
				activeChar.sendPacket(cs);
				break;
			}
			case CLAN:
			{
				if(activeChar.getClan() == null)
					break;
				activeChar.getClan().broadcastToOnlineMembers(cs);
				break;
			}
			case ALLIANCE:
			{
				if(activeChar.getClan() == null || activeChar.getClan().getAlliance() == null)
					break;
				activeChar.getClan().getAlliance().broadcastToOnlineMembers(cs);
				break;
			}
			case PARTY:
			{
				if(!activeChar.isInParty())
					break;
				activeChar.getParty().broadCast(cs);
				break;
			}
			case PARTY_ROOM:
			{
				MatchingRoom r = activeChar.getMatchingRoom();
				if(r == null || r.getType() != MatchingRoom.PARTY_MATCHING)
					break;
				r.broadCast(cs);
				break;
			}
			case COMMANDCHANNEL_ALL:
			{
				if(!activeChar.isInParty() || !activeChar.getParty().isInCommandChannel())
				{
					activeChar.sendPacket(Msg.YOU_DO_NOT_HAVE_AUTHORITY_TO_USE_THE_COMMAND_CHANNEL);
					return;
				}
				if(activeChar.getParty().getCommandChannel().getChannelLeader() == activeChar)
				{
					activeChar.getParty().getCommandChannel().broadCast(cs);
					break;
				}
				activeChar.sendPacket(Msg.ONLY_CHANNEL_OPENER_CAN_GIVE_ALL_COMMAND);
				break;
			}
			case COMMANDCHANNEL_COMMANDER:
			{
				if(!activeChar.isInParty() || !activeChar.getParty().isInCommandChannel())
				{
					activeChar.sendPacket(Msg.YOU_DO_NOT_HAVE_AUTHORITY_TO_USE_THE_COMMAND_CHANNEL);
					return;
				}
				if(activeChar.getParty().isLeader(activeChar))
				{
					activeChar.getParty().getCommandChannel().broadcastToChannelPartyLeaders(cs);
					break;
				}
				activeChar.sendPacket(Msg.ONLY_A_PARTY_LEADER_CAN_ACCESS_THE_COMMAND_CHANNEL);
				break;
			}
			case HERO_VOICE:
			{
				if(!activeChar.isHero() && !activeChar.getPlayerAccess().CanAnnounce)
					break;
				if(!activeChar.getPlayerAccess().CanAnnounce && !activeChar.antiFlood.canHero(_text))
				{
					activeChar.sendMessage("Hero chat is allowed once per 10 seconds.");
					return;
				}
				for(Player player : GameObjectsStorage.getAllPlayersForIterate())
				{
					if(player.isInBlockList(activeChar) || player.isBlockAll())
						continue;
					player.sendPacket(cs);
				}
				break;
			}
			case PETITION_PLAYER:
			case PETITION_GM:
			{
				if(!PetitionManager.getInstance().isPlayerInConsultation(activeChar))
				{
					activeChar.sendPacket(new SystemMessage(745));
					return;
				}
				PetitionManager.getInstance().sendActivePetitionMessage(activeChar, _text);
				break;
			}
			case BATTLEFIELD:
			{
				if(activeChar.getBattlefieldChatId() == 0)
				{
					return;
				}
				for(Player player : GameObjectsStorage.getAllPlayersForIterate())
				{
					if(player.isInBlockList(activeChar) || player.isBlockAll() || player.getBattlefieldChatId() != activeChar.getBattlefieldChatId())
						continue;
					player.sendPacket(cs);
				}
				break;
			}
			case MPCC_ROOM:
			{
				MatchingRoom r2 = activeChar.getMatchingRoom();
				if(r2 == null || r2.getType() != MatchingRoom.CC_MATCHING)
					break;
				r2.broadCast(cs);
				break;
			}
			default:
			{
				_log.warn("Character " + activeChar.getName() + " used unknown chat type: " + _type.ordinal() + ".");
			}
		}
		Log.LogChat(_type.name(), activeChar.getName(), _target, _text, 0);
		activeChar.getMessageBucket().addLast(new ChatMsg(_type, receiver == null ? 0 : receiver.getObjectId(), _text.hashCode(), (int) (currentTimeMillis / 1000)));
	}
}