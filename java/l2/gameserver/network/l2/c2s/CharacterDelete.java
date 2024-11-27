package l2.gameserver.network.l2.c2s;

import l2.commons.threading.RunnableImpl;
import l2.gameserver.Config;
import l2.gameserver.ThreadPoolManager;
import l2.gameserver.database.mysql;
import l2.gameserver.network.l2.GameClient;
import l2.gameserver.network.l2.SecondPasswordAuth;
import l2.gameserver.network.l2.s2c.CharacterDeleteFail;
import l2.gameserver.network.l2.s2c.CharacterDeleteSuccess;
import l2.gameserver.network.l2.s2c.CharacterSelectionInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CharacterDelete extends L2GameClientPacket
{
	private static final Logger _log = LoggerFactory.getLogger(CharacterDelete.class);
	private int _charSlot;
	
	@Override
	protected void readImpl()
	{
		_charSlot = readD();
	}
	
	@Override
	protected void runImpl()
	{
		int clan = clanStatus();
		int online = onlineStatus();
		if(clan > 0 || online > 0)
		{
			if(clan == 2)
			{
				sendPacket(new CharacterDeleteFail(CharacterDeleteFail.REASON_CLAN_LEADERS_MAY_NOT_BE_DELETED));
			}
			else if(clan == 1)
			{
				sendPacket(new CharacterDeleteFail(CharacterDeleteFail.REASON_YOU_MAY_NOT_DELETE_CLAN_MEMBER));
			}
			else if(online > 0)
			{
				sendPacket(new CharacterDeleteFail(CharacterDeleteFail.REASON_DELETION_FAILED));
			}
			return;
		}
		GameClient client = getClient();
		RunnableImpl doDelete = new RunnableImpl()
		{
			
			@Override
			public void runImpl() throws Exception
			{
				try
				{
					if(Config.DELETE_DAYS == 0)
					{
						client.deleteCharacterInSlot(_charSlot);
					}
					else
					{
						client.markToDeleteChar(_charSlot);
					}
					client.sendPacket(new CharacterDeleteSuccess());
				}
				finally
				{
					CharacterSelectionInfo cl = new CharacterSelectionInfo(client.getLogin(), client.getSessionKey().playOkID1);
					client.sendPacket(cl);
					client.setCharSelection(cl.getCharInfo());
				}
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
			client.getSecondPasswordAuth().getUI().verify(client, doDelete);
		}
		else
		{
			ThreadPoolManager.getInstance().execute(doDelete);
		}
	}
	
	private int clanStatus()
	{
		int obj = getClient().getObjectIdForSlot(_charSlot);
		if(obj == -1)
		{
			return 0;
		}
		if(mysql.simple_get_int("clanid", "characters", "obj_Id=" + obj) > 0)
		{
			if(mysql.simple_get_int("leader_id", "clan_subpledges", "leader_id=" + obj + " AND type = " + 0) > 0)
			{
				return 2;
			}
			return 1;
		}
		return 0;
	}
	
	private int onlineStatus()
	{
		int obj = getClient().getObjectIdForSlot(_charSlot);
		if(obj == -1)
		{
			return 0;
		}
		if(mysql.simple_get_int("online", "characters", "obj_Id=" + obj) > 0)
		{
			return 1;
		}
		return 0;
	}
}