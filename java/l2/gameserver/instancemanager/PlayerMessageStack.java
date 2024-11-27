package l2.gameserver.instancemanager;

import l2.gameserver.model.GameObjectsStorage;
import l2.gameserver.model.Player;
import l2.gameserver.network.l2.s2c.L2GameServerPacket;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PlayerMessageStack
{
	private static PlayerMessageStack _instance;
	private final Map<Integer, List<L2GameServerPacket>> _stack = new HashMap<>();
	
	public static PlayerMessageStack getInstance()
	{
		if(_instance == null)
		{
			_instance = new PlayerMessageStack();
		}
		return _instance;
	}
	
	public void mailto(int char_obj_id, L2GameServerPacket message)
	{
		Player cha = GameObjectsStorage.getPlayer(char_obj_id);
		if(cha != null)
		{
			cha.sendPacket(message);
			return;
		}
		Map<Integer, List<L2GameServerPacket>> map = _stack;
		synchronized(map)
		{
			List messages = _stack.containsKey(char_obj_id) ? _stack.remove(char_obj_id) : new ArrayList<L2GameServerPacket>();
			messages.add(message);
			_stack.put(char_obj_id, messages);
		}
	}
	
	public void CheckMessages(Player cha)
	{
		List<L2GameServerPacket> messages;
		Map<Integer, List<L2GameServerPacket>> map = _stack;
		synchronized(map)
		{
			if(!_stack.containsKey(cha.getObjectId()))
			{
				return;
			}
			messages = _stack.remove(cha.getObjectId());
		}
		if(messages == null || messages.size() == 0)
		{
			return;
		}
		for(L2GameServerPacket message : messages)
		{
			cha.sendPacket(message);
		}
	}
}