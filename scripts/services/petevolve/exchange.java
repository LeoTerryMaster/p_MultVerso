package services.petevolve;

import l2.gameserver.Config;
import l2.gameserver.cache.Msg;
import l2.gameserver.data.xml.holder.ItemHolder;
import l2.gameserver.model.Player;
import l2.gameserver.model.Summon;
import l2.gameserver.model.instances.PetInstance;
import l2.gameserver.model.items.ItemInstance;
import l2.gameserver.network.l2.components.SystemMsg;
import l2.gameserver.network.l2.s2c.InventoryUpdate;
import l2.gameserver.scripts.Functions;
import l2.gameserver.templates.item.ItemTemplate;
import l2.gameserver.utils.Util;

public class exchange extends Functions
{
	private static final int PEticketB = 7583;
	private static final int PEticketC = 7584;
	private static final int PEticketK = 7585;
	private static final int BbuffaloP = 6648;
	private static final int BcougarC = 6649;
	private static final int BkookaburraO = 6650;
	
	public void exch_1()
	{
		Player player = getSelf();
		if(player == null)
		{
			return;
		}
		if(getItemCount(player, 7583) >= 1)
		{
			removeItem(player, 7583, (long) 1);
			addItem(player, 6648, (long) 1);
			return;
		}
		show("scripts/services/petevolve/exchange_no.htm", player);
	}
	
	public void exch_2()
	{
		Player player = getSelf();
		if(player == null)
		{
			return;
		}
		if(getItemCount(player, 7584) >= 1)
		{
			removeItem(player, 7584, (long) 1);
			addItem(player, 6649, (long) 1);
			return;
		}
		show("scripts/services/petevolve/exchange_no.htm", player);
	}
	
	public void exch_3()
	{
		Player player = getSelf();
		if(player == null)
		{
			return;
		}
		if(getItemCount(player, 7585) >= 1)
		{
			removeItem(player, 7585, (long) 1);
			addItem(player, 6650, (long) 1);
			return;
		}
		show("scripts/services/petevolve/exchange_no.htm", player);
	}
	
	public void showErasePetName()
	{
		Player player = getSelf();
		if(player == null)
		{
			return;
		}
		if(!Config.SERVICES_CHANGE_PET_NAME_ENABLED)
		{
			show("Сервис отключен.", player);
			return;
		}
		ItemTemplate item = ItemHolder.getInstance().getTemplate(Config.SERVICES_CHANGE_PET_NAME_ITEM);
		String out = "";
		out = out + "<html><body>Вы можете обнулить имя у пета, для того чтобы назначить новое. Пет при этом должен быть вызван.";
		out = out + "<br>Стоимость обнуления: " + Util.formatAdena((long) Config.SERVICES_CHANGE_PET_NAME_PRICE) + " " + item.getName();
		out = out + "<br><button width=100 height=15 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_CT1.Button_DF\" action=\"bypass -h scripts_services.petevolve.exchange:erasePetName\" value=\"Обнулить имя\">";
		out = out + "</body></html>";
		show(out, player);
	}
	
	public void erasePetName()
	{
		Player player = getSelf();
		if(player == null)
		{
			return;
		}
		if(!Config.SERVICES_CHANGE_PET_NAME_ENABLED)
		{
			show("Сервис отключен.", player);
			return;
		}
		Summon pl_pet = player.getPet();
		if(pl_pet == null || !pl_pet.isPet())
		{
			show("Питомец должен быть вызван.", player);
			return;
		}
		if(player.getInventory().destroyItemByItemId(Config.SERVICES_CHANGE_PET_NAME_ITEM, (long) Config.SERVICES_CHANGE_PET_NAME_PRICE))
		{
			pl_pet.setName(pl_pet.getTemplate().name);
			pl_pet.broadcastCharInfo();
			PetInstance _pet = (PetInstance) pl_pet;
			ItemInstance control = _pet.getControlItem();
			if(control != null)
			{
				control.setDamaged(1);
				player.sendPacket(new InventoryUpdate().addModifiedItem(control));
			}
			show("Имя стерто.", player);
		}
		else if(Config.SERVICES_CHANGE_PET_NAME_ITEM == 57)
		{
			player.sendPacket(Msg.YOU_DO_NOT_HAVE_ENOUGH_ADENA);
		}
		else
		{
			player.sendPacket(SystemMsg.INCORRECT_ITEM_COUNT);
		}
	}
	
	public String DialogAppend_30731(Integer val)
	{
		return getHtmlAppends(val);
	}
	
	public String DialogAppend_30827(Integer val)
	{
		return getHtmlAppends(val);
	}
	
	public String DialogAppend_30828(Integer val)
	{
		return getHtmlAppends(val);
	}
	
	public String DialogAppend_30829(Integer val)
	{
		return getHtmlAppends(val);
	}
	
	public String DialogAppend_30830(Integer val)
	{
		return getHtmlAppends(val);
	}
	
	public String DialogAppend_30831(Integer val)
	{
		return getHtmlAppends(val);
	}
	
	public String DialogAppend_30869(Integer val)
	{
		return getHtmlAppends(val);
	}
	
	public String DialogAppend_31067(Integer val)
	{
		return getHtmlAppends(val);
	}
	
	public String DialogAppend_31265(Integer val)
	{
		return getHtmlAppends(val);
	}
	
	public String DialogAppend_31309(Integer val)
	{
		return getHtmlAppends(val);
	}
	
	public String DialogAppend_31954(Integer val)
	{
		return getHtmlAppends(val);
	}
	
	public String getHtmlAppends(Integer val)
	{
		Player player = getSelf();
		if(Config.SERVICES_CHANGE_PET_NAME_ENABLED)
		{
			return player.isLangRus() ? "<br>[scripts_services.petevolve.exchange:showErasePetName|Обнулить имя у пета]" : "<br>[scripts_services.petevolve.exchange:showErasePetName|Erase Pet Name]";
		}
		return "";
	}
}