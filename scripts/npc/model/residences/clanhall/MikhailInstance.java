package npc.model.residences.clanhall;

import l2.gameserver.templates.npc.NpcTemplate;

public class MikhailInstance extends _34BossMinionInstance
{
	public MikhailInstance(int objectId, NpcTemplate template)
	{
		super(objectId, template);
	}
	
	@Override
	public String spawnChatSay()
	{
		return "Glory to Aden, the Kingdom of the Lion! Glory to Sir Gustav, our immortal lord!";
	}
	
	@Override
	public String teleChatSay()
	{
		return "Could it be that I have reached my end? I cannot die without honor, without the permission of Sir Gustav!";
	}
}