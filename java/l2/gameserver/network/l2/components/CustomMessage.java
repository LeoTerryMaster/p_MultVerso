package l2.gameserver.network.l2.components;

import l2.gameserver.data.StringHolder;
import l2.gameserver.data.xml.holder.ItemHolder;
import l2.gameserver.model.Creature;
import l2.gameserver.model.Player;
import l2.gameserver.model.Skill;
import l2.gameserver.model.items.ItemInstance;
import l2.gameserver.tables.SkillTable;
import l2.gameserver.templates.item.ItemTemplate;

public class CustomMessage
{
	private String _text;
	private int mark;
	
	public CustomMessage(String address, Player player, Object... args)
	{
		_text = StringHolder.getInstance().getNotNull(player, address);
		add(args);
	}
	
	public CustomMessage addNumber(long number)
	{
		_text = _text.replace("{" + mark + "}", String.valueOf(number));
		++mark;
		return this;
	}
	
	public CustomMessage add(Object... args)
	{
		for(Object arg : args)
		{
			if(arg instanceof String)
			{
				addString((String) arg);
				continue;
			}
			if(arg instanceof Integer)
			{
				addNumber(((Integer) arg).intValue());
				continue;
			}
			if(arg instanceof Long)
			{
				addNumber((Long) arg);
				continue;
			}
			if(arg instanceof ItemTemplate)
			{
				addItemName((ItemTemplate) arg);
				continue;
			}
			if(arg instanceof ItemInstance)
			{
				addItemName((ItemInstance) arg);
				continue;
			}
			if(arg instanceof Creature)
			{
				addCharName((Creature) arg);
				continue;
			}
			if(arg instanceof Skill)
			{
				addSkillName((Skill) arg);
				continue;
			}
			System.out.println("unknown CustomMessage arg type: " + arg);
			Thread.dumpStack();
		}
		return this;
	}
	
	public CustomMessage addString(String str)
	{
		_text = _text.replace("{" + mark + "}", str);
		++mark;
		return this;
	}
	
	public CustomMessage addSkillName(Skill skill)
	{
		_text = _text.replace("{" + mark + "}", skill.getName());
		++mark;
		return this;
	}
	
	public CustomMessage addSkillName(int skillId, int skillLevel)
	{
		return addSkillName(SkillTable.getInstance().getInfo(skillId, skillLevel));
	}
	
	public CustomMessage addItemName(ItemTemplate item)
	{
		_text = _text.replace("{" + mark + "}", item.getName());
		++mark;
		return this;
	}
	
	public CustomMessage addItemName(int itemId)
	{
		return addItemName(ItemHolder.getInstance().getTemplate(itemId));
	}
	
	public CustomMessage addItemName(ItemInstance item)
	{
		return addItemName(item.getTemplate());
	}
	
	public CustomMessage addCharName(Creature cha)
	{
		_text = _text.replace("{" + mark + "}", cha.getName());
		++mark;
		return this;
	}
	
	@Override
	public String toString()
	{
		return _text;
	}
}