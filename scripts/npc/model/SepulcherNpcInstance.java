package npc.model;

import bosses.FourSepulchersManager;
import bosses.FourSepulchersSpawn;
import l2.commons.threading.RunnableImpl;
import l2.gameserver.ThreadPoolManager;
import l2.gameserver.model.GameObjectsStorage;
import l2.gameserver.model.Player;
import l2.gameserver.model.instances.NpcInstance;
import l2.gameserver.model.items.ItemInstance;
import l2.gameserver.network.l2.components.ChatType;
import l2.gameserver.network.l2.s2c.NpcHtmlMessage;
import l2.gameserver.network.l2.s2c.Say2;
import l2.gameserver.scripts.Functions;
import l2.gameserver.templates.npc.NpcTemplate;
import l2.gameserver.utils.ItemFunctions;
import l2.gameserver.utils.PositionUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Future;

public class SepulcherNpcInstance extends NpcInstance
{
	private static final String HTML_FILE_PATH = "SepulcherNpc/";
	private static final int HALLS_KEY = 7260;
	protected static Map<Integer, Integer> _hallGateKeepers = new HashMap<>();
	protected Future<?> _closeTask;
	protected Future<?> _spawnMonsterTask;
	
	public SepulcherNpcInstance(int objectId, NpcTemplate template)
	{
		super(objectId, template);
	}
	
	@Override
	protected void onDelete()
	{
		if(_closeTask != null)
		{
			_closeTask.cancel(false);
			_closeTask = null;
		}
		if(_spawnMonsterTask != null)
		{
			_spawnMonsterTask.cancel(false);
			_spawnMonsterTask = null;
		}
		super.onDelete();
	}
	
	@Override
	public void showChatWindow(Player player, int val, Object... arg)
	{
		if(isDead())
		{
			player.sendActionFailed();
			return;
		}
		switch(getNpcId())
		{
			case 31468:
			case 31469:
			case 31470:
			case 31471:
			case 31472:
			case 31473:
			case 31474:
			case 31475:
			case 31476:
			case 31477:
			case 31478:
			case 31479:
			case 31480:
			case 31481:
			case 31482:
			case 31483:
			case 31484:
			case 31485:
			case 31486:
			case 31487:
			{
				doDie(player);
				if(_spawnMonsterTask != null)
				{
					_spawnMonsterTask.cancel(false);
				}
				_spawnMonsterTask = ThreadPoolManager.getInstance().schedule(new SpawnMonster(getNpcId()), 3500);
				return;
			}
			case 31455:
			case 31456:
			case 31457:
			case 31458:
			case 31459:
			case 31460:
			case 31461:
			case 31462:
			case 31463:
			case 31464:
			case 31465:
			case 31466:
			case 31467:
			{
				if(player.isInParty() && !hasPartyAKey(player) && player.getParty().getPartyLeader() == player)
				{
					Functions.addItem(player.getParty().getPartyLeader(), 7260, (long) 1);
					doDie(player);
				}
				return;
			}
		}
		super.showChatWindow(player, val);
	}
	
	@Override
	public String getHtmlPath(int npcId, int val, Player player)
	{
		String pom = val == 0 ? String.valueOf(npcId) : "" + npcId + "-" + val;
		return "SepulcherNpc/" + pom + ".htm";
	}
	
	@Override
	public void onBypassFeedback(Player player, String command)
	{
		if(command.startsWith("open_gate"))
		{
			ItemInstance hallsKey = player.getInventory().getItemByItemId(7260);
			if(hallsKey == null)
			{
				showHtmlFile(player, "Gatekeeper-no.htm");
			}
			else if(FourSepulchersManager.isAttackTime())
			{
				switch(getNpcId())
				{
					case 31929:
					case 31934:
					case 31939:
					case 31944:
					{
						if(FourSepulchersSpawn.isShadowAlive(getNpcId()))
							break;
						FourSepulchersSpawn.spawnShadow(getNpcId());
					}
				}
				openNextDoor(getNpcId());
				if(player.getParty() != null)
				{
					for(Player mem : player.getParty().getPartyMembers())
					{
						hallsKey = mem.getInventory().getItemByItemId(7260);
						if(hallsKey == null)
							continue;
						Functions.removeItem(mem, 7260, hallsKey.getCount());
					}
				}
				else
				{
					Functions.removeItem(player, 7260, hallsKey.getCount());
				}
			}
		}
		else
		{
			super.onBypassFeedback(player, command);
		}
	}
	
	public void openNextDoor(int npcId)
	{
		FourSepulchersSpawn.GateKeeper gk = FourSepulchersManager.getHallGateKeeper(npcId);
		gk.door.openMe();
		if(_closeTask != null)
		{
			_closeTask.cancel(false);
		}
		_closeTask = ThreadPoolManager.getInstance().schedule(new CloseNextDoor(gk), 10000);
	}
	
	public void sayInShout(String msg)
	{
		if(msg == null || msg.isEmpty())
		{
			return;
		}
		List<Player> knownPlayers = GameObjectsStorage.getAllPlayers();
		if(knownPlayers == null || knownPlayers.isEmpty())
		{
			return;
		}
		Say2 sm = new Say2(0, ChatType.SHOUT, getName(), msg);
		for(Player player : knownPlayers)
		{
			if(player == null || !PositionUtils.checkIfInRange(15000, player, this, true))
				continue;
			player.sendPacket(sm);
		}
	}
	
	public void showHtmlFile(Player player, String file)
	{
		NpcHtmlMessage html = new NpcHtmlMessage(player, this);
		html.setFile("SepulcherNpc/" + file);
		html.replace("%npcname%", getName());
		player.sendPacket(html);
	}
	
	private boolean hasPartyAKey(Player player)
	{
		for(Player m : player.getParty().getPartyMembers())
		{
			if(ItemFunctions.getItemCount(m, 7260) <= 0)
				continue;
			return true;
		}
		return false;
	}
	
	private class SpawnMonster extends RunnableImpl
	{
		private final int _NpcId;
		
		public SpawnMonster(int npcId)
		{
			_NpcId = npcId;
		}
		
		@Override
		public void runImpl() throws Exception
		{
			FourSepulchersSpawn.spawnMonster(_NpcId);
		}
	}
	
	private class CloseNextDoor extends RunnableImpl
	{
		private final FourSepulchersSpawn.GateKeeper _gk;
		private int state;
		
		public CloseNextDoor(FourSepulchersSpawn.GateKeeper gk)
		{
			state = 0;
			_gk = gk;
		}
		
		@Override
		public void runImpl() throws Exception
		{
			if(state == 0)
			{
				try
				{
					_gk.door.closeMe();
				}
				catch(Exception e)
				{
					e.printStackTrace();
				}
				++state;
				_closeTask = ThreadPoolManager.getInstance().schedule(this, 10000);
			}
			else if(state == 1)
			{
				FourSepulchersSpawn.spawnMysteriousBox(_gk.template.npcId);
				_closeTask = null;
			}
		}
	}
}