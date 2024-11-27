package ai.custom;

import l2.commons.util.Rnd;
import l2.gameserver.ai.Mystic;
import l2.gameserver.model.Creature;
import l2.gameserver.model.Player;
import l2.gameserver.model.entity.Reflection;
import l2.gameserver.model.instances.NpcInstance;
import l2.gameserver.network.l2.s2c.MagicSkillUse;
import l2.gameserver.scripts.Functions;

import java.util.List;

public class SSQAnakim extends Mystic
{
	private static final String PLAYER_NAME = "%playerName%";
	private static final String[] chat = {"For the eternity of Einhasad!!!", "Dear Shillien's offspring! You are not capable of confronting us!", "I'll show you the real power of Einhasad!", "Dear Military Force of Light! Go destroy the offspring of Shillien!!!"};
	private static final String[] pms = {"My power's weakening.. Hurry and turn on the sealing device!!!", "All 4 sealing devices must be turned on!!!", "Lilith's attack is getting stronger! Go ahead and turn it on!", "%playerName%, hold on. We're almost done!"};
	private long _lastChatTime;
	private long _lastPMTime;
	private long _lastSkillTime;
	
	public SSQAnakim(NpcInstance actor)
	{
		super(actor);
		actor.setHasChatWindow(false);
	}
	
	@Override
	protected void onEvtSpawn()
	{
		super.onEvtSpawn();
	}
	
	@Override
	protected boolean thinkActive()
	{
		if(_lastChatTime < System.currentTimeMillis())
		{
			Functions.npcSay(getActor(), chat[Rnd.get(chat.length)]);
			_lastChatTime = System.currentTimeMillis() + 12000;
		}
		if(_lastPMTime < System.currentTimeMillis())
		{
			Player player = getPlayer();
			if(player != null)
			{
				String text = pms[Rnd.get(pms.length)];
				if(text.contains("%playerName%"))
				{
					text = text.replace("%playerName%", player.getName());
				}
				Functions.npcSayToPlayer(getActor(), player, text);
			}
			_lastPMTime = System.currentTimeMillis() + 20000;
		}
		if(_lastSkillTime < System.currentTimeMillis())
		{
			if(getLilith() != null)
			{
				getActor().broadcastPacket(new MagicSkillUse(getActor(), getLilith(), 6191, 1, 5000, 10));
			}
			_lastSkillTime = System.currentTimeMillis() + 6500;
		}
		return true;
	}
	
	private NpcInstance getLilith()
	{
		List<NpcInstance> around = getActor().getAroundNpc(1000, 300);
		if(around != null && !around.isEmpty())
		{
			for(NpcInstance npc : around)
			{
				if(npc.getNpcId() != 32715)
					continue;
				return npc;
			}
		}
		return null;
	}
	
	private Player getPlayer()
	{
		Reflection reflection = getActor().getReflection();
		if(reflection == null)
		{
			return null;
		}
		List pl = reflection.getPlayers();
		if(pl.isEmpty())
		{
			return null;
		}
		return (Player) pl.get(0);
	}
	
	@Override
	protected boolean randomWalk()
	{
		return false;
	}
	
	@Override
	protected void onEvtAttacked(Creature attacker, int damage)
	{
	}
	
	@Override
	protected void onEvtAggression(Creature attacker, int aggro)
	{
	}
}