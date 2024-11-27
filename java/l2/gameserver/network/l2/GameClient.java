package l2.gameserver.network.l2;

import l2.commons.collections.LazyArrayList;
import l2.commons.dbutils.DbUtils;
import l2.commons.net.nio.impl.MMOClient;
import l2.commons.net.nio.impl.MMOConnection;
import l2.commons.threading.RunnableImpl;
import l2.gameserver.Config;
import l2.gameserver.GameServer;
import l2.gameserver.ThreadPoolManager;
import l2.gameserver.cache.Msg;
import l2.gameserver.dao.CharacterDAO;
import l2.gameserver.database.DatabaseFactory;
import l2.gameserver.instancemanager.BypassManager;
import l2.gameserver.model.CharSelectInfoPackage;
import l2.gameserver.model.GameObjectsStorage;
import l2.gameserver.model.Player;
import l2.gameserver.network.authcomm.AuthServerCommunication;
import l2.gameserver.network.authcomm.SessionKey;
import l2.gameserver.network.authcomm.gs2as.PlayerLogout;
import l2.gameserver.network.l2.s2c.L2GameServerPacket;
import l2.gameserver.network.l2.s2c.NpcHtmlMessage;
import l2.gameserver.network.l2.s2c.RequestNetPing;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.ByteBuffer;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ScheduledFuture;

public final class GameClient extends MMOClient<MMOConnection<GameClient>>
{
	public static final String NO_IP = "?.?.?.?";
	private static final Logger _log = LoggerFactory.getLogger(GameClient.class);
	public static int DEFAULT_PAWN_CLIPPING_RANGE = 2048;
	private final List<Integer> _charSlotMapping = new ArrayList<>();
	public GameCrypt _crypt = CGMHelper.isActive() ? CGMHelper.getInstance().createCrypt() : new GameCrypt();
	public GameClientState _state = GameClientState.CONNECTED;
	private String _login;
	private Player _activeChar;
	private SessionKey _sessionKey;
	private String _ip = "?.?.?.?";
	private int revision;
	private String _hwid;
	private List<String> _bypasses;
	private List<String> _bypasses_bbs;
	private long _lastEnchantPacket;
	private SecondPasswordAuth _secondPasswordAuth;
	private boolean _isSecondPasswordAuthed;
	private int _failedPackets;
	private int _unknownPackets;
	private int _pingTimestamp;
	private int _ping;
	private int _fps;
	private int _pawnClippingRange;
	private ScheduledFuture<?> _pingTaskFuture;
	
	public GameClient(MMOConnection<GameClient> con)
	{
		super(con);
		_ip = con.getSocket().getInetAddress().getHostAddress();
	}
	
	@Override
	protected void onDisconnection()
	{
		if(_pingTaskFuture != null)
		{
			_pingTaskFuture.cancel(true);
			_pingTaskFuture = null;
		}
		setState(GameClientState.DISCONNECTED);
		Player player = getActiveChar();
		setActiveChar(null);
		if(player != null)
		{
			player.setNetConnection(null);
			player.scheduleDelete();
		}
		if(getSessionKey() != null)
		{
			if(isAuthed())
			{
				AuthServerCommunication.getInstance().removeAuthedClient(getLogin());
				AuthServerCommunication.getInstance().sendPacket(new PlayerLogout(getLogin()));
			}
			else
			{
				AuthServerCommunication.getInstance().removeWaitingClient(getLogin());
			}
		}
	}
	
	@Override
	protected void onForcedDisconnection()
	{
	}
	
	public void markRestoredChar(int charslot) throws Exception
	{
		int objid = getObjectIdForSlot(charslot);
		if(objid < 0)
		{
			return;
		}
		if(_activeChar != null && _activeChar.getObjectId() == objid)
		{
			_activeChar.setDeleteTimer(0);
		}
		Connection con = null;
		PreparedStatement statement = null;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("UPDATE characters SET deletetime=0 WHERE obj_id=?");
			statement.setInt(1, objid);
			statement.execute();
		}
		catch(Exception e)
		{
			_log.error("", e);
		}
		finally
		{
			DbUtils.closeQuietly(con, statement);
		}
	}
	
	public void markToDeleteChar(int charslot) throws Exception
	{
		int objid = getObjectIdForSlot(charslot);
		if(objid < 0)
		{
			return;
		}
		if(_activeChar != null && _activeChar.getObjectId() == objid)
		{
			_activeChar.setDeleteTimer((int) (System.currentTimeMillis() / 1000));
		}
		Connection con = null;
		PreparedStatement statement = null;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("UPDATE characters SET deletetime=? WHERE obj_id=?");
			statement.setLong(1, (int) (System.currentTimeMillis() / 1000));
			statement.setInt(2, objid);
			statement.execute();
		}
		catch(Exception e)
		{
			_log.error("data error on update deletime char:", e);
		}
		finally
		{
			DbUtils.closeQuietly(con, statement);
		}
	}
	
	public void deleteCharacterInSlot(int charslot) throws Exception
	{
		if(_activeChar != null)
		{
			return;
		}
		int objid = getObjectIdForSlot(charslot);
		if(objid == -1)
		{
			return;
		}
		deleteCharacterByCharacterObjId(objid);
	}
	
	public void deleteCharacterByCharacterObjId(int charObjId)
	{
		CharacterDAO.getInstance().deleteCharacterDataByObjId(charObjId);
	}
	
	public Player loadCharFromDisk(int charslot)
	{
		int objectId = getObjectIdForSlot(charslot);
		if(objectId == -1)
		{
			return null;
		}
		Player character = null;
		Player oldPlayer = GameObjectsStorage.getPlayer(objectId);
		if(oldPlayer != null)
		{
			if(oldPlayer.isInOfflineMode() || oldPlayer.isLogoutStarted())
			{
				oldPlayer.kick();
				return null;
			}
			oldPlayer.sendPacket(Msg.ANOTHER_PERSON_HAS_LOGGED_IN_WITH_THE_SAME_ACCOUNT);
			GameClient oldClient = oldPlayer.getNetConnection();
			if(oldClient != null)
			{
				oldClient.setActiveChar(null);
				oldClient.closeNow(false);
			}
			oldPlayer.setNetConnection(this);
			character = oldPlayer;
		}
		if(character == null)
		{
			character = Player.restore(objectId);
		}
		if(character != null)
		{
			setActiveChar(character);
		}
		else
		{
			_log.warn("could not restore obj_id: " + objectId + " in slot:" + charslot);
		}
		return character;
	}
	
	public int getObjectIdForSlot(int charslot)
	{
		if(charslot < 0 || charslot >= _charSlotMapping.size())
		{
			_log.warn(getLogin() + " tried to modify Character in slot " + charslot + " but no characters exits at that slot.");
			return -1;
		}
		return _charSlotMapping.get(charslot);
	}
	
	public long getLastEnchantPacket()
	{
		return _lastEnchantPacket;
	}
	
	public void setLastEnchantPacket()
	{
		_lastEnchantPacket = System.currentTimeMillis();
	}
	
	public SecondPasswordAuth getSecondPasswordAuth()
	{
		if(getLogin() == null || !Config.USE_SECOND_PASSWORD_AUTH)
		{
			return null;
		}
		if(_secondPasswordAuth == null)
		{
			_secondPasswordAuth = new SecondPasswordAuth(getLogin());
		}
		return _secondPasswordAuth;
	}
	
	public boolean isSecondPasswordAuthed()
	{
		if(_secondPasswordAuth == null)
		{
			return false;
		}
		return _isSecondPasswordAuthed;
	}
	
	public void setSecondPasswordAuthed(boolean authed)
	{
		_isSecondPasswordAuthed = authed;
	}
	
	public Player getActiveChar()
	{
		return _activeChar;
	}
	
	public void setActiveChar(Player player)
	{
		_activeChar = player;
		if(player != null)
		{
			player.setNetConnection(this);
		}
	}
	
	public SessionKey getSessionKey()
	{
		return _sessionKey;
	}
	
	public String getLogin()
	{
		return _login;
	}
	
	public void setLoginName(String loginName)
	{
		_login = loginName;
	}
	
	public void setSessionId(SessionKey sessionKey)
	{
		_sessionKey = sessionKey;
	}
	
	public void setCharSelection(CharSelectInfoPackage[] chars)
	{
		_charSlotMapping.clear();
		for(CharSelectInfoPackage element : chars)
		{
			int objectId = element.getObjectId();
			_charSlotMapping.add(objectId);
		}
	}
	
	public void setCharSelection(int c)
	{
		_charSlotMapping.clear();
		_charSlotMapping.add(c);
	}
	
	public int getRevision()
	{
		return revision;
	}
	
	public void setRevision(int revision)
	{
		this.revision = revision;
	}
	
	public String getHwid()
	{
		return _hwid;
	}
	
	public void setHwid(String hwid)
	{
		_hwid = hwid;
	}
	
	private List<String> getStoredBypasses(boolean bbs)
	{
		if(bbs)
		{
			if(_bypasses_bbs == null)
			{
				_bypasses_bbs = new LazyArrayList<>();
			}
			return _bypasses_bbs;
		}
		if(_bypasses == null)
		{
			_bypasses = new LazyArrayList<>();
		}
		return _bypasses;
	}
	
	public void cleanBypasses(boolean bbs)
	{
		List<String> bypassStorage;
		List<String> list = bypassStorage = getStoredBypasses(bbs);
		synchronized(list)
		{
			bypassStorage.clear();
		}
	}
	
	public String encodeBypasses(String htmlCode, boolean bbs)
	{
		List<String> bypassStorage;
		List<String> list = bypassStorage = getStoredBypasses(bbs);
		synchronized(list)
		{
			return BypassManager.encode(htmlCode, bypassStorage, bbs);
		}
	}
	
	public BypassManager.DecodedBypass decodeBypass(String bypass)
	{
		BypassManager.BypassType bpType = BypassManager.getBypassType(bypass);
		boolean bbs = bpType == BypassManager.BypassType.ENCODED_BBS || bpType == BypassManager.BypassType.SIMPLE_BBS;
		List<String> bypassStorage = getStoredBypasses(bbs);
		if(bpType == BypassManager.BypassType.ENCODED || bpType == BypassManager.BypassType.ENCODED_BBS)
		{
			return BypassManager.decode(bypass, bypassStorage, bbs, this);
		}
		if(bpType == BypassManager.BypassType.SIMPLE || bpType == BypassManager.BypassType.SIMPLE_BBS)
		{
			return new BypassManager.DecodedBypass(bypass, bbs).trim();
		}
		_log.warn("Direct access to bypass: " + bypass + " / " + this);
		return null;
	}
	
	@Override
	public boolean encrypt(ByteBuffer buf, int size)
	{
		_crypt.encrypt(buf.array(), buf.position(), size);
		buf.position(buf.position() + size);
		return true;
	}
	
	@Override
	public boolean decrypt(ByteBuffer buf, int size)
	{
		boolean ret = _crypt.decrypt(buf.array(), buf.position(), size);
		return ret;
	}
	
	public void sendPacket(L2GameServerPacket gsp)
	{
		if(!isConnected())
		{
			return;
		}
		if(gsp instanceof NpcHtmlMessage)
		{
			NpcHtmlMessage npcHtmlMessage = (NpcHtmlMessage) gsp;
			npcHtmlMessage.processHtml(this);
		}
		getConnection().sendPacket(gsp);
	}
	
	public void sendPacket(L2GameServerPacket... gsps)
	{
		if(!isConnected())
		{
			return;
		}
		for(L2GameServerPacket gsp : gsps)
		{
			if(!(gsp instanceof NpcHtmlMessage))
				continue;
			NpcHtmlMessage npcHtmlMessage = (NpcHtmlMessage) gsp;
			npcHtmlMessage.processHtml(this);
		}
		getConnection().sendPacket(gsps);
	}
	
	public void sendPackets(List<L2GameServerPacket> gsps)
	{
		if(!isConnected())
		{
			return;
		}
		for(L2GameServerPacket gsp : gsps)
		{
			if(!(gsp instanceof NpcHtmlMessage))
				continue;
			NpcHtmlMessage npcHtmlMessage = (NpcHtmlMessage) gsp;
			npcHtmlMessage.processHtml(this);
		}
		getConnection().sendPackets(gsps);
	}
	
	public void close(L2GameServerPacket gsp)
	{
		if(!isConnected())
		{
			return;
		}
		if(gsp instanceof NpcHtmlMessage)
		{
			NpcHtmlMessage npcHtmlMessage = (NpcHtmlMessage) gsp;
			npcHtmlMessage.processHtml(this);
		}
		getConnection().close(gsp);
	}
	
	public String getIpAddr()
	{
		return _ip;
	}
	
	public byte[] enableCrypt()
	{
		byte[] key = CGMHelper.isActive() ? CGMHelper.getInstance().getRandomKey() : BlowFishKeygen.getRandomKey();
		_crypt.setKey(key);
		return key;
	}
	
	public GameClientState getState()
	{
		return _state;
	}
	
	public void setState(GameClientState state)
	{
		_state = state;
		switch(state)
		{
			case AUTHED:
			{
				onPing(0, 0, DEFAULT_PAWN_CLIPPING_RANGE);
			}
		}
	}
	
	public void onPacketReadFail()
	{
		if(_failedPackets++ >= 10)
		{
			_log.warn("Too many client packet fails, connection closed : " + this);
			closeNow(true);
		}
	}
	
	public void onUnknownPacket()
	{
		if(_unknownPackets++ >= 10)
		{
			_log.warn("Too many client unknown packets, connection closed : " + this);
			closeNow(true);
		}
	}
	
	@Override
	public String toString()
	{
		return _state + " IP: " + getIpAddr() + (_login == null ? "" : new StringBuilder().append(" Account: ").append(_login).toString()) + (_activeChar == null ? "" : new StringBuilder().append(" Player : ").append(_activeChar).toString());
	}
	
	public void onPing(int timestamp, int fps, int pawnClipRange)
	{
		if(_pingTimestamp == 0 || _pingTimestamp == timestamp)
		{
			long nowMs = System.currentTimeMillis();
			long serverStartTimeMs = GameServer.getInstance().getServerStartTime();
			_ping = _pingTimestamp > 0 ? (int) (nowMs - serverStartTimeMs - (long) timestamp) : 0;
			_fps = fps;
			_pawnClippingRange = pawnClipRange;
			_pingTaskFuture = ThreadPoolManager.getInstance().schedule(new PingTask(this), 30000);
		}
	}
	
	private final void doPing()
	{
		int timestamp;
		long nowMs = System.currentTimeMillis();
		long serverStartTimeMs = GameServer.getInstance().getServerStartTime();
		_pingTimestamp = timestamp = (int) (nowMs - serverStartTimeMs);
		sendPacket(new RequestNetPing(timestamp));
	}
	
	public int getPing()
	{
		return _ping;
	}
	
	public int getFps()
	{
		return _fps;
	}
	
	public int getPawnClippingRange()
	{
		return _pawnClippingRange;
	}
	
	public enum GameClientState
	{
		CONNECTED,
		AUTHED,
		IN_GAME,
		DISCONNECTED;
	}
	
	private static class PingTask extends RunnableImpl
	{
		private final GameClient _client;
		
		private PingTask(GameClient client)
		{
			_client = client;
		}
		
		@Override
		public void runImpl() throws Exception
		{
			if(_client == null || !_client.isConnected())
			{
				return;
			}
			_client.doPing();
		}
	}
}