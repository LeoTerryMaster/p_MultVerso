package l2.gameserver.network.l2.c2s;

import l2.commons.threading.RunnableImpl;
import l2.gameserver.Config;
import l2.gameserver.ThreadPoolManager;
import l2.gameserver.dao.CharacterVariablesDAO;
import l2.gameserver.model.Player;
import l2.gameserver.network.l2.GameClient;
import l2.gameserver.network.l2.SecondPasswordAuth;
import l2.gameserver.network.l2.s2c.ActionFail;
import l2.gameserver.network.l2.s2c.CharSelected;
import l2.gameserver.network.l2.s2c.ExShowScreenMessage;
import l2.gameserver.utils.AutoBan;

public class CharacterSelected extends L2GameClientPacket
{
	private int _charSlot;
	
	@Override
	protected void readImpl()
	{
		_charSlot = readD();
	}
	
	@Override
	protected void runImpl()
	{
		GameClient client = getClient();
		if(client.getActiveChar() != null)
		{
			return;
		}
		int objId = client.getObjectIdForSlot(_charSlot);
		if(AutoBan.isBanned(objId))
		{
			sendPacket(ActionFail.STATIC);
			return;
		}
		String hwidLock = CharacterVariablesDAO.getInstance().getVar(objId, "hwidlock@");
		if(!(hwidLock == null || hwidLock.isEmpty() || client.getHwid() == null || client.getHwid().isEmpty() || hwidLock.equalsIgnoreCase(client.getHwid())))
		{
			sendPacket(new ExShowScreenMessage("HWID is locked.", 10000, ExShowScreenMessage.ScreenMessageAlign.TOP_CENTER, true));
			sendPacket(ActionFail.STATIC);
			return;
		}
		String ipLock = CharacterVariablesDAO.getInstance().getVar(objId, "iplock@");
		if(!(ipLock == null || ipLock.isEmpty() || client.getIpAddr() == null || client.getIpAddr().isEmpty() || ipLock.equalsIgnoreCase(client.getIpAddr())))
		{
			sendPacket(new ExShowScreenMessage("IP address is locked.", 10000, ExShowScreenMessage.ScreenMessageAlign.TOP_CENTER, true));
			sendPacket(ActionFail.STATIC);
			return;
		}
		RunnableImpl doSelect = new RunnableImpl()
		{
			
			@Override
			public void runImpl() throws Exception
			{
				Player activeChar = client.loadCharFromDisk(_charSlot);
				if(activeChar == null)
				{
					sendPacket(ActionFail.STATIC);
					return;
				}
				if(activeChar.getAccessLevel() < 0)
				{
					activeChar.setAccessLevel(0);
				}
				client.setState(GameClient.GameClientState.IN_GAME);
				client.sendPacket(new CharSelected(activeChar, client.getSessionKey().playOkID1));
			}
		};
		if(Config.USE_SECOND_PASSWORD_AUTH && !client.isSecondPasswordAuthed())
		{
			if(client.getSecondPasswordAuth().isSecondPasswordSet())
			{
				if(client.getSecondPasswordAuth().getUI() == null)
				{
					client.getSecondPasswordAuth().setUI(new SecondPasswordAuth.SecondPasswordAuthUI(SecondPasswordAuth.SecondPasswordAuthUI.SecondPasswordAuthUIType.VERIFY));
				}
			}
			else if(client.getSecondPasswordAuth().getUI() == null)
			{
				client.getSecondPasswordAuth().setUI(new SecondPasswordAuth.SecondPasswordAuthUI(SecondPasswordAuth.SecondPasswordAuthUI.SecondPasswordAuthUIType.CREATE));
			}
			client.getSecondPasswordAuth().getUI().verify(client, doSelect);
		}
		else
		{
			ThreadPoolManager.getInstance().execute(doSelect);
		}
	}
}