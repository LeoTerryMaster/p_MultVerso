package l2.gameserver.network.l2.c2s;

import l2.commons.lang.ArrayUtils;
import l2.gameserver.instancemanager.ReflectionManager;
import l2.gameserver.listener.actor.player.OnAnswerListener;
import l2.gameserver.listener.actor.player.impl.ReviveAnswerListener;
import l2.gameserver.model.Player;
import l2.gameserver.model.base.RestartType;
import l2.gameserver.model.entity.Reflection;
import l2.gameserver.model.entity.events.GlobalEvent;
import l2.gameserver.model.entity.residence.Castle;
import l2.gameserver.model.entity.residence.ClanHall;
import l2.gameserver.model.pledge.Clan;
import l2.gameserver.network.l2.components.CustomMessage;
import l2.gameserver.network.l2.s2c.ActionFail;
import l2.gameserver.network.l2.s2c.Die;
import l2.gameserver.utils.ItemFunctions;
import l2.gameserver.utils.Location;
import l2.gameserver.utils.TeleportUtils;
import org.apache.commons.lang3.tuple.Pair;

public class RequestRestartPoint extends L2GameClientPacket
{
	private RestartType _restartType;
	
	public static Location defaultLoc(RestartType restartType, Player activeChar)
	{
		Location loc = null;
		Clan clan = activeChar.getClan();
		switch(restartType)
		{
			case TO_CLANHALL:
			{
				if(clan == null || clan.getHasHideout() == 0)
					break;
				ClanHall clanHall = activeChar.getClanHall();
				loc = TeleportUtils.getRestartLocation(activeChar, RestartType.TO_CLANHALL);
				if(clanHall.getFunction(5) == null)
					break;
				activeChar.restoreExp(clanHall.getFunction(5).getLevel());
				break;
			}
			case TO_CASTLE:
			{
				if(clan == null || clan.getCastle() == 0)
					break;
				Castle castle = activeChar.getCastle();
				loc = TeleportUtils.getRestartLocation(activeChar, RestartType.TO_CASTLE);
				if(castle.getFunction(5) == null)
					break;
				activeChar.restoreExp(castle.getFunction(5).getLevel());
				break;
			}
			default:
			{
				loc = TeleportUtils.getRestartLocation(activeChar, RestartType.TO_VILLAGE);
			}
		}
		return loc;
	}
	
	@Override
	protected void readImpl()
	{
		_restartType = ArrayUtils.valid(RestartType.VALUES, readD());
	}
	
	@Override
	protected void runImpl()
	{
		Player activeChar = getClient().getActiveChar();
		if(_restartType == null || activeChar == null)
		{
			return;
		}
		if(activeChar.isFakeDeath())
		{
			activeChar.breakFakeDeath();
			return;
		}
		if(!activeChar.isDead() && !activeChar.isGM())
		{
			activeChar.sendActionFailed();
			return;
		}
		if(activeChar.isFestivalParticipant())
		{
			activeChar.doRevive();
			return;
		}
		if(activeChar.isResurectProhibited())
		{
			activeChar.sendActionFailed();
			return;
		}
		switch(_restartType)
		{
			case FIXED:
			{
				if(activeChar.getPlayerAccess().ResurectFixed)
				{
					activeChar.doRevive(100.0);
					break;
				}
				if(ItemFunctions.removeItem(activeChar, 9218, 1, true) == 1)
				{
					activeChar.sendMessage(new CustomMessage("YOU_HAVE_USED_THE_FEATHER_OF_BLESSING_TO_RESURRECT", activeChar));
					activeChar.doRevive(100.0);
					break;
				}
				activeChar.sendPacket(ActionFail.STATIC, new Die(activeChar));
				break;
			}
			default:
			{
				Location loc = null;
				Reflection ref = activeChar.getReflection();
				if(ref == ReflectionManager.DEFAULT)
				{
					for(GlobalEvent e : activeChar.getEvents())
					{
						loc = e.getRestartLoc(activeChar, _restartType);
					}
				}
				if(loc == null)
				{
					loc = defaultLoc(_restartType, activeChar);
				}
				if(loc != null)
				{
					Pair<Integer, OnAnswerListener> ask = activeChar.getAskListener(false);
					if(ask != null && ask.getValue() instanceof ReviveAnswerListener && !((ReviveAnswerListener) ask.getValue()).isForPet())
					{
						activeChar.getAskListener(true);
					}
					activeChar.setPendingRevive(true);
					activeChar.teleToLocation(loc, ReflectionManager.DEFAULT);
					break;
				}
				activeChar.sendPacket(ActionFail.STATIC, new Die(activeChar));
			}
		}
	}
}