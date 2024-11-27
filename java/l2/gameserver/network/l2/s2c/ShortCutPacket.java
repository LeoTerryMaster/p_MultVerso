package l2.gameserver.network.l2.s2c;

import l2.gameserver.model.Player;
import l2.gameserver.model.actor.instances.player.ShortCut;
import l2.gameserver.model.items.ItemInstance;
import l2.gameserver.skills.TimeStamp;

public abstract class ShortCutPacket extends L2GameServerPacket
{
	public static ShortcutInfo convert(Player player, ShortCut shortCut)
	{
		ShortcutInfo shortcutInfo;
		int page = shortCut.getSlot() + shortCut.getPage() * 12;
		switch(shortCut.getType())
		{
			case 1:
			{
				int reuseGroup = -1;
				int currentReuse = 0;
				int reuse = 0;
				int variation1 = 0;
				int variation2 = 0;
				ItemInstance item = player.getInventory().getItemByObjectId(shortCut.getId());
				if(item != null)
				{
					variation1 = item.getVariationStat1();
					variation2 = item.getVariationStat2();
					reuseGroup = item.getTemplate().getDisplayReuseGroup();
					TimeStamp timeStamp;
					if(item.getTemplate().getReuseDelay() > 0 && (timeStamp = player.getSharedGroupReuse(item.getTemplate().getReuseGroup())) != null)
					{
						currentReuse = (int) (timeStamp.getReuseCurrent() / 1000);
						reuse = (int) (timeStamp.getReuseBasic() / 1000);
					}
				}
				shortcutInfo = new ItemShortcutInfo(shortCut.getType(), page, shortCut.getId(), reuseGroup, currentReuse, reuse, variation1, variation2, shortCut.getCharacterType());
				break;
			}
			case 2:
			{
				shortcutInfo = new SkillShortcutInfo(shortCut.getType(), page, shortCut.getId(), shortCut.getLevel(), shortCut.getCharacterType());
				break;
			}
			default:
			{
				shortcutInfo = new ShortcutInfo(shortCut.getType(), page, shortCut.getId(), shortCut.getCharacterType());
			}
		}
		return shortcutInfo;
	}
	
	protected static class ShortcutInfo
	{
		protected final int _type;
		protected final int _page;
		protected final int _id;
		protected final int _characterType;
		
		public ShortcutInfo(int type, int page, int id, int characterType)
		{
			_type = type;
			_page = page;
			_id = id;
			_characterType = characterType;
		}
		
		protected void write(ShortCutPacket p)
		{
			p.writeD(_type);
			p.writeD(_page);
			write0(p);
		}
		
		protected void write0(ShortCutPacket p)
		{
			p.writeD(_id);
			p.writeD(_characterType);
		}
	}
	
	protected static class SkillShortcutInfo extends ShortcutInfo
	{
		private final int _level;
		
		public SkillShortcutInfo(int type, int page, int id, int level, int characterType)
		{
			super(type, page, id, characterType);
			_level = level;
		}
		
		public int getLevel()
		{
			return _level;
		}
		
		@Override
		protected void write0(ShortCutPacket p)
		{
			p.writeD(_id);
			p.writeD(_level);
			p.writeC(0);
			p.writeD(_characterType);
		}
	}
	
	protected static class ItemShortcutInfo extends ShortcutInfo
	{
		private final int _reuseGroup;
		private final int _currentReuse;
		private final int _basicReuse;
		private final int _varia1;
		private final int _varia2;
		
		public ItemShortcutInfo(int type, int page, int id, int reuseGroup, int currentReuse, int basicReuse, int variation1, int variation2, int characterType)
		{
			super(type, page, id, characterType);
			_reuseGroup = reuseGroup;
			_currentReuse = currentReuse;
			_basicReuse = basicReuse;
			_varia1 = variation1;
			_varia2 = variation2;
		}
		
		@Override
		protected void write0(ShortCutPacket p)
		{
			p.writeD(_id);
			p.writeD(_characterType);
			p.writeD(_reuseGroup);
			p.writeD(_currentReuse);
			p.writeD(_basicReuse);
			p.writeH(_varia1);
			p.writeH(_varia2);
		}
	}
}