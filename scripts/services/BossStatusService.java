package services;

import bosses.EpicBossState;
import l2.gameserver.Config;
import l2.gameserver.data.StringHolder;
import l2.gameserver.data.xml.holder.NpcHolder;
import l2.gameserver.instancemanager.RaidBossSpawnManager;
import l2.gameserver.model.Player;
import l2.gameserver.model.Spawner;
import l2.gameserver.model.instances.NpcInstance;
import l2.gameserver.network.l2.s2c.NpcHtmlMessage;
import l2.gameserver.scripts.Functions;
import l2.gameserver.scripts.ScriptFile;
import l2.gameserver.templates.npc.NpcTemplate;
import l2.gameserver.utils.TimeUtils;

import java.text.SimpleDateFormat;
import java.util.ArrayList;

public class BossStatusService extends Functions implements ScriptFile
{
	private static BossStatusInfo[] _bossesInfo;
	
	private static BossStatusInfo[] initBossesInfo()
	{
		ArrayList<BossStatusInfo> result = new ArrayList<BossStatusInfo>() {};
		for(int raidBossNpcId : Config.SERVICES_BOSS_STATUS_ADDITIONAL_IDS)
		{
			result.add(new RaidBossStatusInfo(raidBossNpcId));
		}
		return result.toArray(new BossStatusInfo[result.size()]);
	}
	
	private static BossStatusInfo[] getBossesInfo()
	{
		if(_bossesInfo == null)
		{
			_bossesInfo = initBossesInfo();
			return _bossesInfo;
		}
		return _bossesInfo;
	}
	
	private static String formatBossHtml(Player player, BossStatusInfo bossStatusInfo, SimpleDateFormat dateFormat)
	{
		String bossHtml = StringHolder.getInstance().getNotNull(player, "scripts.services.BossStatusService." + bossStatusInfo.getStatus());
		long respawnDate = bossStatusInfo.getRespawnDate();
		bossHtml = bossHtml.replace("%name%", bossStatusInfo.getName());
		bossHtml = bossHtml.replace("%npc_id%", String.valueOf(bossStatusInfo.getNpcId()));
		bossHtml = bossHtml.replace("%respawn_date%", respawnDate > 0 ? dateFormat.format(respawnDate * 1000) : "");
		return bossHtml;
	}
	
	public void listBossStatuses()
	{
		Player player = getSelf();
		if(player == null)
		{
			return;
		}
		NpcInstance npc = getNpc();
		if(!Config.SERVICES_BOSS_STATUS_ENABLE)
		{
			player.sendPacket(new NpcHtmlMessage(5).setFile("scripts/services/service_disabled.htm"));
			return;
		}
		BossStatusInfo[] bossStatusInfos = getBossesInfo();
		StringBuilder bossStatusInfoHtml = new StringBuilder();
		SimpleDateFormat dateFormat = new SimpleDateFormat(Config.SERVICES_BOSS_STATUS_FORMAT);
		for(BossStatusInfo bossStatusInfo : bossStatusInfos)
		{
			bossStatusInfoHtml.append(formatBossHtml(player, bossStatusInfo, dateFormat));
		}
		NpcHtmlMessage html = new NpcHtmlMessage(player, npc);
		html.setFile("scripts/services/bossstatus.htm");
		html.replace("%list%", bossStatusInfoHtml.toString());
		html.replace("%current_date%", TimeUtils.toSimpleFormat(System.currentTimeMillis()));
		player.sendPacket(html);
	}
	
	@Override
	public void onLoad()
	{
	}
	
	@Override
	public void onReload()
	{
	}
	
	@Override
	public void onShutdown()
	{
	}
	
	public static final class RaidBossStatusInfo extends BossStatusInfo
	{
		public RaidBossStatusInfo(int bossNpcId)
		{
			super(bossNpcId);
		}
		
		@Override
		public BossStatusInfo.BossStatus getStatus()
		{
			if(RaidBossSpawnManager.getInstance().getRaidBossStatusId(getNpcId()) == RaidBossSpawnManager.Status.DEAD)
			{
				return BossStatusInfo.BossStatus.DEAD;
			}
			long respawnDate = getRespawnDate();
			if(respawnDate * 1000 > System.currentTimeMillis())
			{
				return BossStatusInfo.BossStatus.DEAD;
			}
			return BossStatusInfo.BossStatus.ALIVE;
		}
		
		@Override
		public long getRespawnDate()
		{
			Spawner spawner = RaidBossSpawnManager.getInstance().getSpawnTable().get(getNpcId());
			if(spawner == null)
			{
				return -1;
			}
			return spawner.getRespawnTime();
		}
	}
	
	public static final class EpicBossStatusInfo extends BossStatusInfo
	{
		private final EpicBossState _epicBossState;
		
		public EpicBossStatusInfo(int bossNpcId, EpicBossState epicBossState)
		{
			super(bossNpcId);
			_epicBossState = epicBossState;
		}
		
		@Override
		public BossStatusInfo.BossStatus getStatus()
		{
			switch(_epicBossState.getState())
			{
				case ALIVE:
				{
					return BossStatusInfo.BossStatus.ALIVE;
				}
				case NOTSPAWN:
				{
					return BossStatusInfo.BossStatus.READY;
				}
				case DEAD:
				{
					return BossStatusInfo.BossStatus.DEAD;
				}
			}
			return BossStatusInfo.BossStatus.RESPAWN;
		}
		
		@Override
		public long getRespawnDate()
		{
			return _epicBossState.getRespawnDate() / 1000;
		}
	}
	
	private abstract static class BossStatusInfo
	{
		private final int _bossNpcId;
		
		public BossStatusInfo(int bossNpcId)
		{
			_bossNpcId = bossNpcId;
		}
		
		public int getNpcId()
		{
			return _bossNpcId;
		}
		
		public String getName()
		{
			NpcTemplate bossNpcTemplate = NpcHolder.getInstance().getTemplate(getNpcId());
			if(bossNpcTemplate == null)
			{
				return "";
			}
			return bossNpcTemplate.getName();
		}
		
		public abstract BossStatus getStatus();
		
		public abstract long getRespawnDate();
		
		public enum BossStatus
		{
			ALIVE,
			DEAD,
			READY,
			RESPAWN;
			
			
		}
	}
}