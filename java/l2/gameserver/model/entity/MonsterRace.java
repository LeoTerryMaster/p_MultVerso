package l2.gameserver.model.entity;

import l2.commons.util.Rnd;
import l2.gameserver.data.xml.holder.NpcHolder;
import l2.gameserver.idfactory.IdFactory;
import l2.gameserver.model.instances.NpcInstance;
import l2.gameserver.templates.npc.NpcTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Constructor;

public class MonsterRace
{
	private static final Logger _log = LoggerFactory.getLogger(MonsterRace.class);
	private static MonsterRace _instance;
	private final NpcInstance[] monsters = new NpcInstance[8];
	private final int[] first = new int[2];
	private final int[] second = new int[2];
	private Constructor<?> _constructor;
	private int[][] speeds = new int[8][20];
	
	private MonsterRace()
	{
	}
	
	public static MonsterRace getInstance()
	{
		if(_instance == null)
		{
			_instance = new MonsterRace();
		}
		return _instance;
	}
	
	public void newRace()
	{
		for(int i = 0;i < 8;++i)
		{
			int random = Rnd.get(24);
			int id = 31003;
			for(int j = i - 1;j >= 0;--j)
			{
				if(monsters[j].getTemplate().npcId != id + random)
					continue;
				random = Rnd.get(24);
			}
			try
			{
				NpcTemplate template = NpcHolder.getInstance().getTemplate(id + random);
				_constructor = template.getInstanceConstructor();
				int objectId = IdFactory.getInstance().getNextId();
				monsters[i] = (NpcInstance) _constructor.newInstance(objectId, template);
				continue;
			}
			catch(Exception e)
			{
				_log.error("", e);
			}
		}
		newSpeeds();
	}
	
	public void newSpeeds()
	{
		speeds = new int[8][20];
		first[1] = 0;
		second[1] = 0;
		for(int i = 0;i < 8;++i)
		{
			int total = 0;
			for(int j = 0;j < 20;++j)
			{
				speeds[i][j] = j == 19 ? 100 : Rnd.get(65, 124);
				total += speeds[i][j];
			}
			if(total >= first[1])
			{
				second[0] = first[0];
				second[1] = first[1];
				first[0] = 8 - i;
				first[1] = total;
				continue;
			}
			if(total < second[1])
				continue;
			second[0] = 8 - i;
			second[1] = total;
		}
	}
	
	public NpcInstance[] getMonsters()
	{
		return monsters;
	}
	
	public int[][] getSpeeds()
	{
		return speeds;
	}
	
	public int getFirstPlace()
	{
		return 8 - first[0];
	}
	
	public int getSecondPlace()
	{
		return 8 - second[0];
	}
}