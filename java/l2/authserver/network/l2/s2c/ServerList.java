package l2.authserver.network.l2.s2c;

import l2.authserver.GameServerManager;
import l2.authserver.accounts.Account;
import l2.authserver.network.gamecomm.GameServer;
import l2.authserver.network.gamecomm.ProxyServer;
import l2.commons.net.utils.NetUtils;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public final class ServerList extends L2LoginServerPacket
{
	private static final Comparator<ServerData> SERVER_DATA_COMPARATOR = new Comparator<ServerData>()
	{
		@Override
		public int compare(ServerData o1, ServerData o2)
		{
			return o1.serverId - o2.serverId;
		}
	};
	private final List<ServerData> _servers = new ArrayList<>();
	private final int _lastServer;
	
	public ServerList(Account account)
	{
		_lastServer = account.getLastServer();
		for(GameServer gs : GameServerManager.getInstance().getGameServers())
		{
			InetAddress ip;
			try
			{
				ip = NetUtils.isInternalIP(account.getLastIP()) ? gs.getInternalHost() : gs.getExternalHost();
			}
			catch(UnknownHostException e)
			{
				continue;
			}
			_servers.add(new ServerData(gs.getId(), ip, gs.getPort(), gs.isPvp(), gs.isShowingBrackets(), gs.getServerType(), gs.getOnline(), gs.getMaxPlayers(), gs.isOnline(), gs.getAgeLimit()));
			List<ProxyServer> proxyServers = GameServerManager.getInstance().getProxyServersList(gs.getId());
			for(ProxyServer ps : proxyServers)
			{
				_servers.add(new ServerData(ps.getProxyServerId(), ps.getProxyAddr(), ps.getProxyPort(), gs.isPvp(), gs.isShowingBrackets(), gs.getServerType(), gs.getOnline(), gs.getMaxPlayers(), gs.isOnline(), gs.getAgeLimit()));
			}
		}
		_servers.sort(SERVER_DATA_COMPARATOR);
	}
	
	@Override
	protected void writeImpl()
	{
		writeC(4);
		writeC(_servers.size());
		writeC(_lastServer);
		for(ServerData server : _servers)
		{
			writeC(server.serverId);
			InetAddress i4 = server.ip;
			byte[] raw = i4.getAddress();
			writeC(raw[0] & 255);
			writeC(raw[1] & 255);
			writeC(raw[2] & 255);
			writeC(raw[3] & 255);
			writeD(server.port);
			writeC(server.ageLimit);
			writeC(server.pvp ? 1 : 0);
			writeH(server.online);
			writeH(server.maxPlayers);
			writeC(server.status ? 1 : 0);
			writeD(server.type);
			writeC(server.brackets ? 1 : 0);
		}
	}
	
	private static class ServerData
	{
		int serverId;
		InetAddress ip;
		int port;
		int online;
		int maxPlayers;
		boolean status;
		boolean pvp;
		boolean brackets;
		int type;
		int ageLimit;
		
		ServerData(int serverId, InetAddress ip, int port, boolean pvp, boolean brackets, int type, int online, int maxPlayers, boolean status, int ageLimit)
		{
			this.serverId = serverId;
			this.ip = ip;
			this.port = port;
			this.pvp = pvp;
			this.brackets = brackets;
			this.type = type;
			this.online = online;
			this.maxPlayers = maxPlayers;
			this.status = status;
			this.ageLimit = ageLimit;
		}
	}
}