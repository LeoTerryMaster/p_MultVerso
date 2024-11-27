package npc.model;

import l2.commons.util.Rnd;
import l2.gameserver.data.xml.holder.NpcHolder;
import l2.gameserver.model.Creature;
import l2.gameserver.model.SimpleSpawner;
import l2.gameserver.model.instances.BossInstance;
import l2.gameserver.model.instances.MinionInstance;
import l2.gameserver.model.instances.NpcInstance;
import l2.gameserver.network.l2.s2c.PlaySound;
import l2.gameserver.scripts.Functions;
import l2.gameserver.templates.npc.NpcTemplate;
import l2.gameserver.utils.Location;

import java.util.ArrayList;
import java.util.List;

public class QueenAntInstance extends BossInstance
{
	private static final int Queen_Ant_Larva = 29002;
	private final List<SimpleSpawner> _spawns = new ArrayList<>();
	private NpcInstance Larva;
	
	public QueenAntInstance(int objectId, NpcTemplate template)
	{
		super(objectId, template);
	}
	
	public NpcInstance getLarva()
	{
		if(Larva == null)
		{
			Larva = SpawnNPC(29002, new Location(-21600, 179482, -5846, Rnd.get(0, 65535)));
		}
		return Larva;
	}
	
	@Override
	protected int getKilledInterval(MinionInstance minion)
	{
		return 130000;
	}
	
	@Override
	protected void onDeath(Creature killer)
	{
		broadcastPacketToOthers(new PlaySound(PlaySound.Type.MUSIC, "BS02_D", 1, 0, getLoc()));
		Functions.deSpawnNPCs(_spawns);
		Larva = null;
		super.onDeath(killer);
	}
	
	@Override
	protected void onSpawn()
	{
		super.onSpawn();
		getLarva();
		broadcastPacketToOthers(new PlaySound(PlaySound.Type.MUSIC, "BS01_A", 1, 0, getLoc()));
	}
	
	private NpcInstance SpawnNPC(int npcId, Location loc)
	{
		NpcTemplate template = NpcHolder.getInstance().getTemplate(npcId);
		if(template == null)
		{
			System.out.println("WARNING! template is null for npc: " + npcId);
			Thread.dumpStack();
			return null;
		}
		try
		{
			SimpleSpawner sp = new SimpleSpawner(template);
			sp.setLoc(loc);
			sp.setAmount(1);
			sp.setRespawnDelay(0);
			_spawns.add(sp);
			return sp.spawnOne();
		}
		catch(Exception e)
		{
			e.printStackTrace();
			return null;
		}
	}
}