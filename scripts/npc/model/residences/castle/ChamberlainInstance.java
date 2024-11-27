package npc.model.residences.castle;

import l2.gameserver.dao.CastleDamageZoneDAO;
import l2.gameserver.dao.CastleDoorUpgradeDAO;
import l2.gameserver.data.xml.holder.ResidenceHolder;
import l2.gameserver.instancemanager.CastleManorManager;
import l2.gameserver.model.Player;
import l2.gameserver.model.entity.SevenSigns;
import l2.gameserver.model.entity.events.objects.CastleDamageZoneObject;
import l2.gameserver.model.entity.events.objects.DoorObject;
import l2.gameserver.model.entity.residence.Castle;
import l2.gameserver.model.entity.residence.Residence;
import l2.gameserver.model.instances.DoorInstance;
import l2.gameserver.model.pledge.Privilege;
import l2.gameserver.network.l2.components.CustomMessage;
import l2.gameserver.network.l2.components.SystemMsg;
import l2.gameserver.network.l2.s2c.CastleSiegeInfo;
import l2.gameserver.network.l2.s2c.ExShowCropInfo;
import l2.gameserver.network.l2.s2c.ExShowCropSetting;
import l2.gameserver.network.l2.s2c.ExShowManorDefaultInfo;
import l2.gameserver.network.l2.s2c.ExShowSeedInfo;
import l2.gameserver.network.l2.s2c.ExShowSeedSetting;
import l2.gameserver.network.l2.s2c.L2GameServerPacket;
import l2.gameserver.network.l2.s2c.NpcHtmlMessage;
import l2.gameserver.templates.npc.NpcTemplate;
import l2.gameserver.utils.ItemFunctions;
import l2.gameserver.utils.Log;
import l2.gameserver.utils.ReflectionUtils;
import npc.model.residences.ResidenceManager;

import java.util.Collections;
import java.util.List;
import java.util.StringTokenizer;

public class ChamberlainInstance extends ResidenceManager
{
	public ChamberlainInstance(int objectId, NpcTemplate template)
	{
		super(objectId, template);
	}
	
	private static long modifyPrice(long price)
	{
		switch(SevenSigns.getInstance().getSealOwner(3))
		{
			case 1:
			{
				int SSQ_DuskFactor_door = 300;
				price = price * (long) SSQ_DuskFactor_door / 100;
				break;
			}
			case 2:
			{
				int SSQ_DawnFactor_door = 80;
				price = price * (long) SSQ_DawnFactor_door / 100;
				break;
			}
			default:
			{
				int SSQ_DrawFactor_door = 100;
				price = price * (long) SSQ_DrawFactor_door / 100;
			}
		}
		return price;
	}
	
	@Override
	protected void setDialogs()
	{
		_mainDialog = "castle/chamberlain/chamberlain.htm";
		_failDialog = "castle/chamberlain/chamberlain-notlord.htm";
		_siegeDialog = _mainDialog;
	}
	
	@Override
	public void onBypassFeedback(Player player, String command)
	{
		if(!canBypassCheck(player, this))
		{
			return;
		}
		int condition = getCond(player);
		if(condition != 2)
		{
			return;
		}
		StringTokenizer st = new StringTokenizer(command, " ");
		String actualCommand = st.nextToken();
		String val = "";
		if(st.countTokens() >= 1)
		{
			val = st.nextToken();
		}
		Castle castle = getCastle();
		if(actualCommand.equalsIgnoreCase("viewSiegeInfo"))
		{
			if(!isHaveRigths(player, 131072))
			{
				player.sendPacket(SystemMsg.YOU_ARE_NOT_AUTHORIZED_TO_DO_THAT);
				return;
			}
			player.sendPacket(new CastleSiegeInfo(castle, player));
		}
		else if(actualCommand.equalsIgnoreCase("ManageTreasure"))
		{
			if(!player.isClanLeader())
			{
				player.sendPacket(SystemMsg.YOU_ARE_NOT_AUTHORIZED_TO_DO_THAT);
				return;
			}
			NpcHtmlMessage html = new NpcHtmlMessage(player, this);
			html.setFile("castle/chamberlain/chamberlain-castlevault.htm");
			html.replace("%Treasure%", String.valueOf(castle.getTreasury()));
			html.replace("%CollectedShops%", String.valueOf(castle.getCollectedShops()));
			html.replace("%CollectedSeed%", String.valueOf(castle.getCollectedSeed()));
			player.sendPacket(html);
		}
		else if(actualCommand.equalsIgnoreCase("TakeTreasure"))
		{
			if(!player.isClanLeader())
			{
				player.sendPacket(SystemMsg.YOU_ARE_NOT_AUTHORIZED_TO_DO_THAT);
				return;
			}
			if(!val.equals(""))
			{
				long treasure = Long.parseLong(val);
				if(castle.getTreasury() < treasure)
				{
					NpcHtmlMessage html = new NpcHtmlMessage(player, this);
					html.setFile("castle/chamberlain/chamberlain-havenottreasure.htm");
					html.replace("%Treasure%", String.valueOf(castle.getTreasury()));
					html.replace("%Requested%", String.valueOf(treasure));
					player.sendPacket(html);
					return;
				}
				if(treasure > 0)
				{
					castle.addToTreasuryNoTax(-treasure, false, false);
					Log.add(castle.getName() + "|" + -treasure + "|CastleChamberlain", "treasury");
					player.addAdena(treasure);
				}
			}
			NpcHtmlMessage html = new NpcHtmlMessage(player, this);
			html.setFile("castle/chamberlain/chamberlain-castlevault.htm");
			html.replace("%Treasure%", String.valueOf(castle.getTreasury()));
			html.replace("%CollectedShops%", String.valueOf(castle.getCollectedShops()));
			html.replace("%CollectedSeed%", String.valueOf(castle.getCollectedSeed()));
			player.sendPacket(html);
		}
		else if(actualCommand.equalsIgnoreCase("PutTreasure"))
		{
			if(!val.equals(""))
			{
				long treasure = Long.parseLong(val);
				if(treasure > player.getAdena())
				{
					player.sendPacket(SystemMsg.YOU_DO_NOT_HAVE_ENOUGH_ADENA);
					return;
				}
				if(treasure > 0)
				{
					castle.addToTreasuryNoTax(treasure, false, false);
					Log.add(castle.getName() + "|" + treasure + "|CastleChamberlain", "treasury");
					player.reduceAdena(treasure, true);
				}
			}
			NpcHtmlMessage html = new NpcHtmlMessage(player, this);
			html.setFile("castle/chamberlain/chamberlain-castlevault.htm");
			html.replace("%Treasure%", String.valueOf(castle.getTreasury()));
			html.replace("%CollectedShops%", String.valueOf(castle.getCollectedShops()));
			html.replace("%CollectedSeed%", String.valueOf(castle.getCollectedSeed()));
			player.sendPacket(html);
		}
		else if(actualCommand.equalsIgnoreCase("manor"))
		{
			if(!isHaveRigths(player, 65536))
			{
				player.sendPacket(SystemMsg.YOU_ARE_NOT_AUTHORIZED_TO_DO_THAT);
				return;
			}
			String filename;
			if(CastleManorManager.getInstance().isDisabled())
			{
				filename = "npcdefault.htm";
			}
			else
			{
				int cmd = Integer.parseInt(val);
				switch(cmd)
				{
					case 0:
					{
						filename = "castle/chamberlain/manor/manor.htm";
						break;
					}
					case 4:
					{
						filename = "castle/chamberlain/manor/manor_help00" + st.nextToken() + ".htm";
						break;
					}
					default:
					{
						filename = "castle/chamberlain/chamberlain-no.htm";
					}
				}
			}
			if(filename.length() > 0)
			{
				NpcHtmlMessage html = new NpcHtmlMessage(player, this);
				html.setFile(filename);
				player.sendPacket(html);
			}
		}
		else if(actualCommand.startsWith("manor_menu_select"))
		{
			if(!isHaveRigths(player, 65536))
			{
				player.sendPacket(SystemMsg.YOU_ARE_NOT_AUTHORIZED_TO_DO_THAT);
				return;
			}
			if(CastleManorManager.getInstance().isUnderMaintenance())
			{
				player.sendPacket(SystemMsg.THE_MANOR_SYSTEM_IS_CURRENTLY_UNDER_MAINTENANCE);
				player.sendActionFailed();
				return;
			}
			String params = actualCommand.substring(actualCommand.indexOf("?") + 1);
			StringTokenizer str = new StringTokenizer(params, "&");
			int ask = Integer.parseInt(str.nextToken().split("=")[1]);
			int state = Integer.parseInt(str.nextToken().split("=")[1]);
			int time = Integer.parseInt(str.nextToken().split("=")[1]);
			int castleId = state == -1 ? castle.getId() : state;
			switch(ask)
			{
				case 3:
				{
					if(time == 1 && !ResidenceHolder.getInstance().getResidence(Castle.class, castleId).isNextPeriodApproved())
					{
						player.sendPacket(new ExShowSeedInfo(castleId, Collections.emptyList()));
						break;
					}
					player.sendPacket(new ExShowSeedInfo(castleId, ResidenceHolder.getInstance().getResidence(Castle.class, castleId).getSeedProduction(time)));
					break;
				}
				case 4:
				{
					if(time == 1 && !ResidenceHolder.getInstance().getResidence(Castle.class, castleId).isNextPeriodApproved())
					{
						player.sendPacket(new ExShowCropInfo(castleId, Collections.emptyList()));
						break;
					}
					player.sendPacket(new ExShowCropInfo(castleId, ResidenceHolder.getInstance().getResidence(Castle.class, castleId).getCropProcure(time)));
					break;
				}
				case 5:
				{
					player.sendPacket(new ExShowManorDefaultInfo());
					break;
				}
				case 7:
				{
					if(castle.isNextPeriodApproved())
					{
						player.sendPacket(SystemMsg.A_MANOR_CANNOT_BE_SET_UP_BETWEEN_430_AM_AND_8_PM);
						break;
					}
					player.sendPacket(new ExShowSeedSetting(castle.getId()));
					break;
				}
				case 8:
				{
					if(castle.isNextPeriodApproved())
					{
						player.sendPacket(SystemMsg.A_MANOR_CANNOT_BE_SET_UP_BETWEEN_430_AM_AND_8_PM);
						break;
					}
					player.sendPacket(new ExShowCropSetting(castle.getId()));
				}
			}
		}
		else if(actualCommand.equalsIgnoreCase("operate_door"))
		{
			if(!isHaveRigths(player, 32768))
			{
				player.sendPacket(SystemMsg.YOU_ARE_NOT_AUTHORIZED_TO_DO_THAT);
				return;
			}
			if(castle.getSiegeEvent().isInProgress())
			{
				showChatWindow(player, "residence2/castle/chamberlain_saius021.htm");
				return;
			}
			if(!val.equals(""))
			{
				boolean open;
				boolean bl = open = Integer.parseInt(val) == 1;
				while(st.hasMoreTokens())
				{
					DoorInstance door = ReflectionUtils.getDoor(Integer.parseInt(st.nextToken()));
					if(open)
					{
						door.openMe(player, true);
						continue;
					}
					door.closeMe(player, true);
				}
			}
			NpcHtmlMessage html = new NpcHtmlMessage(player, this);
			html.setFile("castle/chamberlain/" + getTemplate().npcId + "-d.htm");
			player.sendPacket(html);
		}
		else if(actualCommand.equalsIgnoreCase("tax_set"))
		{
			if(!isHaveRigths(player, 1048576))
			{
				player.sendPacket(SystemMsg.YOU_ARE_NOT_AUTHORIZED_TO_DO_THAT);
				return;
			}
			if(!val.equals(""))
			{
				int maxTax = 15;
				if(SevenSigns.getInstance().getSealOwner(3) == 1)
				{
					maxTax = 5;
				}
				else if(SevenSigns.getInstance().getSealOwner(3) == 2)
				{
					maxTax = 25;
				}
				int tax = Integer.parseInt(val);
				if(tax < 0 || tax > maxTax)
				{
					NpcHtmlMessage html = new NpcHtmlMessage(player, this);
					html.setFile("castle/chamberlain/chamberlain-hightax.htm");
					html.replace("%CurrentTax%", String.valueOf(castle.getTaxPercent()));
					player.sendPacket(html);
					return;
				}
				castle.setTaxPercent(player, tax);
			}
			NpcHtmlMessage html = new NpcHtmlMessage(player, this);
			html.setFile("castle/chamberlain/chamberlain-settax.htm");
			html.replace("%CurrentTax%", String.valueOf(castle.getTaxPercent()));
			player.sendPacket(html);
		}
		else if(actualCommand.equalsIgnoreCase("upgrade_castle"))
		{
			if(!checkSiegeFunctions(player))
			{
				return;
			}
			showChatWindow(player, "castle/chamberlain/chamberlain-upgrades.htm");
		}
		else if(actualCommand.equalsIgnoreCase("reinforce"))
		{
			if(!checkSiegeFunctions(player))
			{
				return;
			}
			NpcHtmlMessage html = new NpcHtmlMessage(player, this);
			html.setFile("castle/chamberlain/doorStrengthen-" + castle.getName() + ".htm");
			player.sendPacket(html);
		}
		else if(actualCommand.equalsIgnoreCase("trap_select"))
		{
			if(!checkSiegeFunctions(player))
			{
				return;
			}
			NpcHtmlMessage html = new NpcHtmlMessage(player, this);
			html.setFile("castle/chamberlain/trap_select-" + castle.getName() + ".htm");
			player.sendPacket(html);
		}
		else if(actualCommand.equalsIgnoreCase("buy_trap"))
		{
			if(!checkSiegeFunctions(player))
			{
				return;
			}
			if(castle.getSiegeEvent().getObjects("bought_zones").contains(val))
			{
				NpcHtmlMessage html = new NpcHtmlMessage(player, this);
				html.setFile("castle/chamberlain/trapAlready.htm");
				player.sendPacket(html);
				return;
			}
			List<CastleDamageZoneObject> objects = castle.getSiegeEvent().getObjects(val);
			long price = 0;
			for(CastleDamageZoneObject o : objects)
			{
				price += o.getPrice();
			}
			price = modifyPrice(price);
			if(player.getClan().getAdenaCount() < price)
			{
				player.sendPacket(SystemMsg.YOU_DO_NOT_HAVE_ENOUGH_ADENA);
				return;
			}
			player.getClan().getWarehouse().destroyItemByItemId(57, price);
			castle.getSiegeEvent().addObject("bought_zones", val);
			CastleDamageZoneDAO.getInstance().insert(castle, val);
			NpcHtmlMessage html = new NpcHtmlMessage(player, this);
			html.setFile("castle/chamberlain/trapSuccess.htm");
			player.sendPacket(html);
		}
		else if(actualCommand.equalsIgnoreCase("door_manage"))
		{
			if(!isHaveRigths(player, 32768))
			{
				player.sendPacket(SystemMsg.YOU_ARE_NOT_AUTHORIZED_TO_DO_THAT);
				return;
			}
			if(castle.getSiegeEvent().isInProgress())
			{
				showChatWindow(player, "residence2/castle/chamberlain_saius021.htm");
				return;
			}
			NpcHtmlMessage html = new NpcHtmlMessage(player, this);
			html.setFile("castle/chamberlain/doorManage.htm");
			html.replace("%id%", val);
			html.replace("%type%", st.nextToken());
			player.sendPacket(html);
		}
		else if(actualCommand.equalsIgnoreCase("upgrade_door_confirm"))
		{
			if(!isHaveRigths(player, 131072))
			{
				player.sendPacket(SystemMsg.YOU_ARE_NOT_AUTHORIZED_TO_DO_THAT);
				return;
			}
			int id = Integer.parseInt(val);
			int type = Integer.parseInt(st.nextToken());
			int level = Integer.parseInt(st.nextToken());
			long price = getDoorCost(type, level);
			NpcHtmlMessage html = new NpcHtmlMessage(player, this);
			html.setFile("castle/chamberlain/doorConfirm.htm");
			html.replace("%id%", String.valueOf(id));
			html.replace("%level%", String.valueOf(level));
			html.replace("%type%", String.valueOf(type));
			html.replace("%price%", String.valueOf(price));
			player.sendPacket(html);
		}
		else if(actualCommand.equalsIgnoreCase("upgrade_door"))
		{
			if(!checkSiegeFunctions(player))
			{
				return;
			}
			int id = Integer.parseInt(val);
			int type = Integer.parseInt(st.nextToken());
			int level = Integer.parseInt(st.nextToken());
			long price = getDoorCost(type, level);
			List<DoorObject> doorObjects = castle.getSiegeEvent().getObjects("doors");
			DoorObject targetDoorObject = null;
			for(DoorObject o : doorObjects)
			{
				if(o.getUId() != id)
					continue;
				targetDoorObject = o;
				break;
			}
			DoorInstance door = targetDoorObject.getDoor();
			int upgradeHp = (door.getMaxHp() - door.getUpgradeHp()) * level - door.getMaxHp();
			if(price == 0 || upgradeHp < 0)
			{
				player.sendMessage(new CustomMessage("common.Error", player));
				return;
			}
			if(door.getUpgradeHp() >= upgradeHp)
			{
				int oldLevel = door.getUpgradeHp() / (door.getMaxHp() - door.getUpgradeHp()) + 1;
				NpcHtmlMessage html = new NpcHtmlMessage(player, this);
				html.setFile("castle/chamberlain/doorAlready.htm");
				html.replace("%level%", String.valueOf(oldLevel));
				player.sendPacket(html);
				return;
			}
			if(player.getClan().getAdenaCount() < price)
			{
				player.sendPacket(SystemMsg.YOU_DO_NOT_HAVE_ENOUGH_ADENA);
				return;
			}
			player.getClan().getWarehouse().destroyItemByItemId(57, price);
			player.sendMessage("Build reinforced");
			targetDoorObject.setUpgradeValue(castle.getSiegeEvent(), upgradeHp);
			CastleDoorUpgradeDAO.getInstance().insert(door.getDoorId(), upgradeHp);
		}
		else if(actualCommand.equalsIgnoreCase("report"))
		{
			if(!isHaveRigths(player, 262144))
			{
				player.sendPacket(SystemMsg.YOU_ARE_NOT_AUTHORIZED_TO_DO_THAT);
				return;
			}
			NpcHtmlMessage html = new NpcHtmlMessage(player, this);
			html.setFile("castle/chamberlain/chamberlain-report.htm");
			html.replace("%FeudName%", castle.getName());
			html.replace("%CharClan%", player.getClan().getName());
			html.replace("%CharName%", player.getName());
			if(SevenSigns.getInstance().getCurrentPeriod() == 1)
			{
				html.replace("%SSPeriod%", new CustomMessage("ChamberlainInstance.NpcString.COMPETITION", player, new Object[0]).toString());
			}
			else if(SevenSigns.getInstance().getCurrentPeriod() == 3)
			{
				html.replace("%SSPeriod%", new CustomMessage("ChamberlainInstance.NpcString.SEAL_VALIDATION", player, new Object[0]).toString());
			}
			else
			{
				html.replace("%SSPeriod%", new CustomMessage("ChamberlainInstance.NpcString.PREPARATION", player, new Object[0]).toString());
			}
			switch(SevenSigns.getInstance().getSealOwner(1))
			{
				case 1:
				{
					html.replace("%Avarice%", new CustomMessage("SevenSigns.NpcString.DUSK", player, new Object[0]).toString());
					break;
				}
				case 2:
				{
					html.replace("%Avarice%", new CustomMessage("SevenSigns.NpcString.DAWN", player, new Object[0]).toString());
					break;
				}
				case 0:
				{
					html.replace("%Avarice%", new CustomMessage("SevenSigns.NpcString.NO_OWNER", player, new Object[0]).toString());
				}
			}
			switch(SevenSigns.getInstance().getSealOwner(2))
			{
				case 1:
				{
					html.replace("%Revelation%", new CustomMessage("SevenSigns.NpcString.DUSK", player, new Object[0]).toString());
					break;
				}
				case 2:
				{
					html.replace("%Revelation%", new CustomMessage("SevenSigns.NpcString.DAWN", player, new Object[0]).toString());
					break;
				}
				case 0:
				{
					html.replace("%Revelation%", new CustomMessage("SevenSigns.NpcString.NO_OWNER", player, new Object[0]).toString());
				}
			}
			switch(SevenSigns.getInstance().getSealOwner(3))
			{
				case 1:
				{
					html.replace("%Strife%", new CustomMessage("SevenSigns.NpcString.DUSK", player, new Object[0]).toString());
					break;
				}
				case 2:
				{
					html.replace("%Strife%", new CustomMessage("SevenSigns.NpcString.DAWN", player, new Object[0]).toString());
					break;
				}
				case 0:
				{
					html.replace("%Strife%", new CustomMessage("SevenSigns.NpcString.NO_OWNER", player, new Object[0]).toString());
				}
			}
			player.sendPacket(html);
		}
		else if(actualCommand.equalsIgnoreCase("Crown"))
		{
			if(!player.isClanLeader())
			{
				player.sendPacket(SystemMsg.YOU_ARE_NOT_AUTHORIZED_TO_DO_THAT);
				return;
			}
			if(player.getInventory().getItemByItemId(6841) == null)
			{
				player.getInventory().addItem(ItemFunctions.createItem(6841));
				NpcHtmlMessage html = new NpcHtmlMessage(player, this);
				html.setFile("castle/chamberlain/chamberlain-givecrown.htm");
				html.replace("%CharName%", player.getName());
				html.replace("%FeudName%", castle.getName());
				player.sendPacket(html);
			}
			else
			{
				NpcHtmlMessage html = new NpcHtmlMessage(player, this);
				html.setFile("castle/chamberlain/alreadyhavecrown.htm");
				player.sendPacket(html);
			}
		}
		else if(actualCommand.equalsIgnoreCase("manageFunctions"))
		{
			if(!player.hasPrivilege(Privilege.CS_FS_SET_FUNCTIONS))
			{
				showChatWindow(player, "residence2/castle/chamberlain_saius063.htm");
			}
			else
			{
				showChatWindow(player, "residence2/castle/chamberlain_saius065.htm");
			}
		}
		else if(actualCommand.equalsIgnoreCase("manageSiegeFunctions"))
		{
			if(!player.hasPrivilege(Privilege.CS_FS_SET_FUNCTIONS))
			{
				showChatWindow(player, "residence2/castle/chamberlain_saius063.htm");
			}
			else if(SevenSigns.getInstance().getCurrentPeriod() != 3)
			{
				showChatWindow(player, "residence2/castle/chamberlain_saius068.htm");
			}
			else
			{
				showChatWindow(player, "residence2/castle/chamberlain_saius052.htm");
			}
		}
		else if(actualCommand.equalsIgnoreCase("items"))
		{
			NpcHtmlMessage html = new NpcHtmlMessage(player, this);
			html.setFile("residence2/castle/chamberlain_saius064.htm");
			html.replace("%npcId%", String.valueOf(getNpcId()));
			player.sendPacket(html);
		}
		else if(actualCommand.equalsIgnoreCase("default"))
		{
			NpcHtmlMessage html = new NpcHtmlMessage(player, this);
			html.setFile("castle/chamberlain/chamberlain.htm");
			player.sendPacket(html);
		}
		else
		{
			super.onBypassFeedback(player, command);
		}
	}
	
	@Override
	protected int getCond(Player player)
	{
		if(player.isGM())
		{
			return 2;
		}
		Castle castle = getCastle();
		if(castle != null && castle.getId() > 0 && player.getClan() != null)
		{
			if(castle.getSiegeEvent().isInProgress())
			{
				return 1;
			}
			if(castle.getOwnerId() == player.getClanId())
			{
				if(player.isClanLeader())
				{
					return 2;
				}
				if(isHaveRigths(player, 32768) || isHaveRigths(player, 65536) || isHaveRigths(player, 131072) || isHaveRigths(player, 262144) || isHaveRigths(player, 524288) || isHaveRigths(player, 1048576) || isHaveRigths(player, 2097152) || isHaveRigths(player, 4194304))
				{
					return 2;
				}
			}
		}
		return 0;
	}
	
	private long getDoorCost(int type, int level)
	{
		int price = 0;
		block:
		switch(type)
		{
			case 1:
			{
				switch(level)
				{
					case 2:
					{
						price = 3000000;
						break block;
					}
					case 3:
					{
						price = 4000000;
						break block;
					}
					case 5:
					{
						price = 5000000;
					}
				}
				break;
			}
			case 2:
			{
				switch(level)
				{
					case 2:
					{
						price = 750000;
						break block;
					}
					case 3:
					{
						price = 900000;
						break block;
					}
					case 5:
					{
						price = 1000000;
					}
				}
				break;
			}
			case 3:
			{
				switch(level)
				{
					case 2:
					{
						price = 1600000;
						break block;
					}
					case 3:
					{
						price = 1800000;
						break block;
					}
					case 5:
					{
						price = 2000000;
					}
				}
			}
		}
		return modifyPrice(price);
	}
	
	@Override
	protected Residence getResidence()
	{
		return getCastle();
	}
	
	@Override
	public L2GameServerPacket decoPacket()
	{
		return null;
	}
	
	@Override
	protected int getPrivUseFunctions()
	{
		return 262144;
	}
	
	@Override
	protected int getPrivSetFunctions()
	{
		return 4194304;
	}
	
	@Override
	protected int getPrivDismiss()
	{
		return 524288;
	}
	
	@Override
	protected int getPrivDoors()
	{
		return 32768;
	}
	
	private boolean checkSiegeFunctions(Player player)
	{
		Castle castle = getCastle();
		if(!player.hasPrivilege(Privilege.CS_FS_SIEGE_WAR))
		{
			player.sendPacket(SystemMsg.YOU_ARE_NOT_AUTHORIZED_TO_DO_THAT);
			return false;
		}
		if(castle.getSiegeEvent().isInProgress())
		{
			showChatWindow(player, "residence2/castle/chamberlain_saius021.htm");
			return false;
		}
		return true;
	}
}