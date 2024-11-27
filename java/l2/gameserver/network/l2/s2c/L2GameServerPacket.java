package l2.gameserver.network.l2.s2c;

import l2.commons.net.nio.impl.SendablePacket;
import l2.gameserver.GameServer;
import l2.gameserver.data.xml.holder.ItemHolder;
import l2.gameserver.model.Player;
import l2.gameserver.model.base.Element;
import l2.gameserver.model.base.MultiSellIngredient;
import l2.gameserver.model.items.ItemInfo;
import l2.gameserver.model.items.ItemInstance;
import l2.gameserver.network.l2.GameClient;
import l2.gameserver.network.l2.components.IStaticPacket;
import l2.gameserver.templates.item.ItemTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class L2GameServerPacket extends SendablePacket<GameClient> implements IStaticPacket
{
	private static final Logger _log = LoggerFactory.getLogger(L2GameServerPacket.class);
	
	@Override
	public final boolean write()
	{
		try
		{
			writeImpl();
			boolean bl = true;
			return bl;
		}
		catch(Exception e)
		{
			_log.error("Client: " + getClient() + " - Failed writing: " + getType() + " - Server Version: " + GameServer.getInstance().getVersion().getRevisionNumber(), e);
		}
		return false;
	}
	
	protected abstract void writeImpl();
	
	protected void writeEx(int value)
	{
		writeC(254);
		writeH(value);
	}
	
	protected void writeD(boolean b)
	{
		writeD(b ? 1 : 0);
	}
	
	protected void writeDD(int[] values, boolean sendCount)
	{
		if(sendCount)
		{
			getByteBuffer().putInt(values.length);
		}
		for(int value : values)
		{
			getByteBuffer().putInt(value);
		}
	}
	
	protected void writeDD(int[] values)
	{
		writeDD(values, false);
	}
	
	protected void writeItemInfo(ItemInstance item)
	{
		writeItemInfo(item, item.getCount());
	}
	
	protected void writeItemInfo(ItemInstance item, long count)
	{
		writeH(item.getTemplate().getType1());
		writeD(item.getObjectId());
		writeD(item.getItemId());
		writeD((int) count);
		writeH(item.getTemplate().getType2ForPackets());
		writeH(item.getBlessed());
		writeH(item.isEquipped() ? 1 : 0);
		writeD(item.getTemplate().getBodyPart());
		writeH(item.getEnchantLevel());
		writeH(item.getDamaged());
		writeH(item.getVariationStat1());
		writeH(item.getVariationStat2());
		writeD(item.getDuration());
	}
	
	protected void writeItemInfo(ItemInfo item)
	{
		writeItemInfo(item, item.getCount());
	}
	
	protected void writeItemInfo(ItemInfo item, long count)
	{
		writeH(item.getItem().getType1());
		writeD(item.getObjectId());
		writeD(item.getItemId());
		writeD((int) count);
		writeH(item.getItem().getType2ForPackets());
		writeH(item.getCustomType1());
		writeH(item.isEquipped() ? 1 : 0);
		writeD(item.getItem().getBodyPart());
		writeH(item.getEnchantLevel());
		writeH(item.getCustomType2());
		writeH(item.getVariationStat1());
		writeH(item.getVariationStat2());
		writeD(item.getShadowLifeTime());
	}
	
	protected void writeItemElements(MultiSellIngredient item)
	{
		if(item.getItemId() <= 0)
		{
			writeItemElements();
			return;
		}
		ItemTemplate i = ItemHolder.getInstance().getTemplate(item.getItemId());
		if(item.getItemAttributes().getValue() > 0)
		{
			if(i.isWeapon())
			{
				Element e = item.getItemAttributes().getElement();
				writeH(e.getId());
				writeH(item.getItemAttributes().getValue(e) + i.getBaseAttributeValue(e));
				writeH(0);
				writeH(0);
				writeH(0);
				writeH(0);
				writeH(0);
				writeH(0);
			}
			else if(i.isArmor())
			{
				writeH(-1);
				writeH(0);
				for(Element e : Element.VALUES)
				{
					writeH(item.getItemAttributes().getValue(e) + i.getBaseAttributeValue(e));
				}
			}
			else
			{
				writeItemElements();
			}
		}
		else
		{
			writeItemElements();
		}
	}
	
	protected void writeItemElements()
	{
		writeH(-1);
		writeH(0);
		writeH(0);
		writeH(0);
		writeH(0);
		writeH(0);
		writeH(0);
		writeH(0);
	}
	
	public String getType()
	{
		return "[S] " + getClass().getSimpleName();
	}
	
	@Override
	public L2GameServerPacket packet(Player player)
	{
		return this;
	}
}