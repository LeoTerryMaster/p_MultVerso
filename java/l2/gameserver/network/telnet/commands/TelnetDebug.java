package l2.gameserver.network.telnet.commands;

import gnu.trove.TIntObjectHashMap;
import gnu.trove.TIntObjectIterator;
import l2.gameserver.model.GameObject;
import l2.gameserver.model.GameObjectsStorage;
import l2.gameserver.model.instances.NpcInstance;
import l2.gameserver.network.authcomm.AuthServerCommunication;
import l2.gameserver.network.telnet.TelnetCommand;
import l2.gameserver.network.telnet.TelnetCommandHolder;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public class TelnetDebug implements TelnetCommandHolder
{
	private final Set<TelnetCommand> _commands = new LinkedHashSet<>();
	
	public TelnetDebug()
	{
		_commands.add(new TelnetCommand("dumpnpc", "dnpc")
		{
			
			@Override
			public String getUsage()
			{
				return "dumpnpc";
			}
			
			@Override
			public String handle(String[] args)
			{
				List<NpcInstance> list;
				StringBuilder sb = new StringBuilder();
				int total = 0;
				int maxId = 0;
				int maxCount = 0;
				TIntObjectHashMap npcStats = new TIntObjectHashMap();
				for(GameObject obj : GameObjectsStorage.getAllObjects())
				{
					if(!obj.isCreature() || !obj.isNpc())
						continue;
					NpcInstance npc = (NpcInstance) obj;
					int id = npc.getNpcId();
					list = (ArrayList<NpcInstance>) npcStats.get(id);
					if(list == null)
					{
						list = new ArrayList<>();
						npcStats.put(id, list);
					}
					list.add(npc);
					if(list.size() > maxCount)
					{
						maxId = id;
						maxCount = list.size();
					}
					++total;
				}
				sb.append("Total NPCs: ").append(total).append("\n");
				sb.append("Maximum NPC ID: ").append(maxId).append(" count : ").append(maxCount).append("\n");
				TIntObjectIterator itr = npcStats.iterator();
				while(itr.hasNext())
				{
					itr.advance();
					int id = itr.key();
					list = (List) itr.value();
					sb.append("=== ID: ").append(id).append(" ").append(" Count: ").append(list.size()).append(" ===").append("\n");
					for(NpcInstance npc : list)
					{
						try
						{
							sb.append("AI: ");
							if(npc.hasAI())
							{
								sb.append(npc.getAI().getClass().getName());
							}
							else
							{
								sb.append("none");
							}
							sb.append(", ");
							if(npc.getReflectionId() > 0)
							{
								sb.append("ref: ").append(npc.getReflectionId());
								sb.append(" - ").append(npc.getReflection().getName());
							}
							sb.append("loc: ").append(npc.getLoc());
							sb.append(", ");
							sb.append("spawned: ");
							sb.append(npc.isVisible());
							sb.append("\n");
						}
						catch(Exception e)
						{
							e.printStackTrace();
						}
					}
				}
				try
				{
					new File("stats").mkdir();
					FileUtils.writeStringToFile(new File("stats/NpcStats-" + new SimpleDateFormat("MMddHHmmss").format(System.currentTimeMillis()) + ".txt"), sb.toString());
				}
				catch(IOException e)
				{
					e.printStackTrace();
				}
				return "NPC stats saved.\n";
			}
		});
		_commands.add(new TelnetCommand("asrestart")
		{
			
			@Override
			public String getUsage()
			{
				return "asrestart";
			}
			
			@Override
			public String handle(String[] args)
			{
				AuthServerCommunication.getInstance().restart();
				return "Restarted.\n";
			}
		});
	}
	
	@Override
	public Set<TelnetCommand> getCommands()
	{
		return _commands;
	}
}