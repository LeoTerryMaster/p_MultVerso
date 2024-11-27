package l2.gameserver.model.instances;

import l2.gameserver.instancemanager.ReflectionManager;
import l2.gameserver.model.Player;
import l2.gameserver.model.World;
import l2.gameserver.model.Zone;
import l2.gameserver.network.l2.components.SystemMsg;
import l2.gameserver.templates.npc.NpcTemplate;
import l2.gameserver.utils.Location;

import java.util.ArrayList;
import java.util.StringTokenizer;

public final class ObservationInstance extends NpcInstance
{
	public ObservationInstance(int objectId, NpcTemplate template)
	{
		super(objectId, template);
	}
	
	@Override
	public void onBypassFeedback(Player player, String command)
	{
		if(!canBypassCheck(player, this))
		{
			return;
		}
		if(player.isOlyParticipant())
		{
			return;
		}
		if(command.startsWith("observeSiege"))
		{
			String val = command.substring(13);
			StringTokenizer st = new StringTokenizer(val);
			st.nextToken();
			ArrayList<Zone> zones = new ArrayList<>();
			World.getZones(zones, new Location(Integer.parseInt(st.nextToken()), Integer.parseInt(st.nextToken()), Integer.parseInt(st.nextToken())), ReflectionManager.DEFAULT);
			for(Zone z : zones)
			{
				if(z.getType() != Zone.ZoneType.SIEGE || !z.isActive())
					continue;
				doObserve(player, val);
				return;
			}
			player.sendPacket(SystemMsg.OBSERVATION_IS_ONLY_POSSIBLE_DURING_A_SIEGE);
		}
		else if(command.startsWith("observe"))
		{
			doObserve(player, command.substring(8));
		}
		else
		{
			super.onBypassFeedback(player, command);
		}
	}
	
	@Override
	public String getHtmlPath(int npcId, int val, Player player)
	{
		String pom = val == 0 ? "" + npcId : "" + npcId + "-" + val;
		return "observation/" + pom + ".htm";
	}
	
	private void doObserve(Player player, String val)
	{
		StringTokenizer st = new StringTokenizer(val);
		int cost = Integer.parseInt(st.nextToken());
		int x = Integer.parseInt(st.nextToken());
		int y = Integer.parseInt(st.nextToken());
		int z = Integer.parseInt(st.nextToken());
		if(!player.reduceAdena(cost, true))
		{
			player.sendPacket(SystemMsg.YOU_DO_NOT_HAVE_ENOUGH_ADENA);
			player.sendActionFailed();
			return;
		}
		player.enterObserverMode(new Location(x, y, z));
	}
}