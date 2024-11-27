package l2.gameserver.model.instances;

import l2.gameserver.model.Player;
import l2.gameserver.templates.npc.NpcTemplate;

public final class TrainerInstance extends NpcInstance
{
	public TrainerInstance(int objectId, NpcTemplate template)
	{
		super(objectId, template);
	}
	
	@Override
	public String getHtmlPath(int npcId, int val, Player player)
	{
		String pom = val == 0 ? "" + npcId : "" + npcId + "-" + val;
		return "trainer/" + pom + ".htm";
	}
}