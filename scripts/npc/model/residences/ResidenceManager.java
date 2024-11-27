package npc.model.residences;

import l2.gameserver.Config;
import l2.gameserver.model.Creature;
import l2.gameserver.model.Player;
import l2.gameserver.model.Skill;
import l2.gameserver.model.TeleportLocation;
import l2.gameserver.model.World;
import l2.gameserver.model.entity.residence.Residence;
import l2.gameserver.model.instances.MerchantInstance;
import l2.gameserver.model.pledge.Clan;
import l2.gameserver.network.l2.components.CustomMessage;
import l2.gameserver.network.l2.components.SystemMsg;
import l2.gameserver.network.l2.s2c.L2GameServerPacket;
import l2.gameserver.network.l2.s2c.NpcHtmlMessage;
import l2.gameserver.tables.SkillTable;
import l2.gameserver.templates.npc.NpcTemplate;
import l2.gameserver.utils.HtmlUtils;
import l2.gameserver.utils.ReflectionUtils;
import l2.gameserver.utils.TimeUtils;
import l2.gameserver.utils.WarehouseFunctions;
import org.apache.commons.lang3.ArrayUtils;

import java.util.Iterator;
import java.util.List;
import java.util.StringTokenizer;

public abstract class ResidenceManager extends MerchantInstance
{
	protected static final int COND_FAIL = 0;
	protected static final int COND_SIEGE = 1;
	protected static final int COND_OWNER = 2;
	protected String _siegeDialog;
	protected String _mainDialog;
	protected String _failDialog;
	protected int[] _doors;
	
	public ResidenceManager(int objectId, NpcTemplate template)
	{
		super(objectId, template);
		setDialogs();
		_doors = template.getAIParams().getIntegerArray("doors", ArrayUtils.EMPTY_INT_ARRAY);
	}
	
	protected void setDialogs()
	{
		_siegeDialog = getTemplate().getAIParams().getString("siege_dialog", "npcdefault.htm");
		_mainDialog = getTemplate().getAIParams().getString("main_dialog", "npcdefault.htm");
		_failDialog = getTemplate().getAIParams().getString("fail_dialog", "npcdefault.htm");
	}
	
	protected abstract Residence getResidence();
	
	protected abstract L2GameServerPacket decoPacket();
	
	protected abstract int getPrivUseFunctions();
	
	protected abstract int getPrivSetFunctions();
	
	protected abstract int getPrivDismiss();
	
	protected abstract int getPrivDoors();
	
	public void broadcastDecoInfo()
	{
		L2GameServerPacket decoPacket = decoPacket();
		if(decoPacket == null)
		{
			return;
		}
		for(Player player : World.getAroundPlayers(this))
		{
			player.sendPacket(decoPacket);
		}
	}
	
	protected int getCond(Player player)
	{
		Residence residence = getResidence();
		Clan residenceOwner = residence.getOwner();
		if(residenceOwner != null && player.getClan() == residenceOwner)
		{
			if(residence.getSiegeEvent().isInProgress())
			{
				return 1;
			}
			return 2;
		}
		return 0;
	}
	
	@Override
	public void showChatWindow(Player player, int val, Object... arg)
	{
		String filename = null;
		int cond = getCond(player);
		switch(cond)
		{
			case 2:
			{
				filename = _mainDialog;
				break;
			}
			case 1:
			{
				filename = _siegeDialog;
				break;
			}
			case 0:
			{
				filename = _failDialog;
			}
		}
		player.sendPacket(new NpcHtmlMessage(player, this, filename, val));
	}
	
	@Override
	public void onBypassFeedback(Player player, String command)
	{
		if(!canBypassCheck(player, this))
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
		int cond = getCond(player);
		switch(cond)
		{
			case 1:
			{
				showChatWindow(player, _siegeDialog);
				return;
			}
			case 0:
			{
				showChatWindow(player, _failDialog);
				return;
			}
		}
		if(actualCommand.equalsIgnoreCase("banish"))
		{
			NpcHtmlMessage html = new NpcHtmlMessage(player, this);
			html.setFile("residence/Banish.htm");
			sendHtmlMessage(player, html);
		}
		else
		{
			if(actualCommand.equalsIgnoreCase("banish_foreigner"))
			{
				if(!isHaveRigths(player, getPrivDismiss()))
				{
					player.sendPacket(SystemMsg.YOU_ARE_NOT_AUTHORIZED_TO_DO_THAT);
					return;
				}
				getResidence().banishForeigner();
				return;
			}
			if(actualCommand.equalsIgnoreCase("Buy"))
			{
				if(val.equals(""))
				{
					return;
				}
				showShopWindow(player, Integer.valueOf(val).intValue(), true);
			}
			else
			{
				if(actualCommand.equalsIgnoreCase("manage_vault"))
				{
					if(val.equalsIgnoreCase("deposit"))
					{
						WarehouseFunctions.showDepositWindowClan(player);
					}
					else if(val.equalsIgnoreCase("withdraw"))
					{
						int value = Integer.valueOf(st.nextToken());
						if(value == 99)
						{
							NpcHtmlMessage html = new NpcHtmlMessage(player, this);
							html.setFile("residence/clan.htm");
							html.replace("%npcname%", getName());
							player.sendPacket(html);
						}
						else
						{
							WarehouseFunctions.showWithdrawWindowClan(player, value);
						}
					}
					else
					{
						NpcHtmlMessage html = new NpcHtmlMessage(player, this);
						html.setFile("residence/vault.htm");
						sendHtmlMessage(player, html);
					}
					return;
				}
				if(actualCommand.equalsIgnoreCase("door"))
				{
					showChatWindow(player, "residence/door.htm");
				}
				else if(actualCommand.equalsIgnoreCase("openDoors"))
				{
					if(isHaveRigths(player, getPrivDoors()))
					{
						for(int i : _doors)
						{
							ReflectionUtils.getDoor(i).openMe();
						}
						showChatWindow(player, "residence/door.htm");
					}
					else
					{
						player.sendPacket(SystemMsg.YOU_ARE_NOT_AUTHORIZED_TO_DO_THAT);
					}
				}
				else if(actualCommand.equalsIgnoreCase("closeDoors"))
				{
					if(isHaveRigths(player, getPrivDoors()))
					{
						for(int i : _doors)
						{
							ReflectionUtils.getDoor(i).closeMe();
						}
						showChatWindow(player, "residence/door.htm");
					}
					else
					{
						player.sendPacket(SystemMsg.YOU_ARE_NOT_AUTHORIZED_TO_DO_THAT);
					}
				}
				else if(actualCommand.equalsIgnoreCase("functions"))
				{
					if(!isHaveRigths(player, getPrivUseFunctions()))
					{
						player.sendPacket(SystemMsg.YOU_ARE_NOT_AUTHORIZED_TO_DO_THAT);
						return;
					}
					if(val.equalsIgnoreCase("tele"))
					{
						if(!getResidence().isFunctionActive(1))
						{
							NpcHtmlMessage html = new NpcHtmlMessage(player, this);
							html.setFile("residence/teleportNotActive.htm");
							sendHtmlMessage(player, html);
							return;
						}
						NpcHtmlMessage html = new NpcHtmlMessage(player, this);
						html.setFile("residence/teleport.htm");
						TeleportLocation[] locs = getResidence().getFunction(1).getTeleports();
						StringBuilder teleport_list = new StringBuilder(100 * locs.length);
						for(TeleportLocation loc : locs)
						{
							String price = String.valueOf(loc.getPrice());
							String nameAddrVal = new CustomMessage(loc.getName(), player, new Object[0]).toString();
							teleport_list.append("<a action=\"bypass -h scripts_Util:Gatekeeper ");
							teleport_list.append(loc.getX());
							teleport_list.append(" ");
							teleport_list.append(loc.getY());
							teleport_list.append(" ");
							teleport_list.append(loc.getZ());
							teleport_list.append(" ");
							teleport_list.append(price);
							teleport_list.append("\" msg=\"811;F;");
							teleport_list.append(nameAddrVal);
							teleport_list.append("\">");
							teleport_list.append(nameAddrVal);
							teleport_list.append(" - ");
							teleport_list.append(price);
							teleport_list.append(" ");
							teleport_list.append("Adena");
							teleport_list.append("</a><br1>");
						}
						html.replace("%teleList%", teleport_list.toString());
						sendHtmlMessage(player, html);
					}
					else if(val.equalsIgnoreCase("item_creation"))
					{
						if(!getResidence().isFunctionActive(2))
						{
							NpcHtmlMessage html = new NpcHtmlMessage(player, this);
							html.setFile("residence/itemNotActive.htm");
							sendHtmlMessage(player, html);
							return;
						}
						NpcHtmlMessage html = new NpcHtmlMessage(player, this);
						html.setFile("residence/item.htm");
						String template = "<button value=\"Buy Item\" action=\"bypass -h npc_%objectId%_Buy %id%\" width=67 height=19 back=\"L2UI.DefaultButton_click\" fore=\"L2UI.DefaultButton\">";
						template = template.replaceAll("%id%", String.valueOf(getResidence().getFunction(2).getBuylist()[1])).replace("%objectId%", String.valueOf(getObjectId()));
						html.replace("%itemList%", template);
						sendHtmlMessage(player, html);
					}
					else if(val.equalsIgnoreCase("support"))
					{
						if(!getResidence().isFunctionActive(6))
						{
							NpcHtmlMessage html = new NpcHtmlMessage(player, this);
							html.setFile("residence/supportNotActive.htm");
							sendHtmlMessage(player, html);
							return;
						}
						NpcHtmlMessage html = new NpcHtmlMessage(player, this);
						html.setFile("residence/support.htm");
						Object[][] allBuffs = getResidence().getFunction(6).getBuffs();
						StringBuilder support_list = new StringBuilder(allBuffs.length * 50);
						int i = 0;
						for(Object[] buff : allBuffs)
						{
							Skill s = (Skill) buff[0];
							support_list.append("<a action=\"bypass -h npc_%objectId%_support ");
							support_list.append(String.valueOf(s.getId()));
							support_list.append(" ");
							support_list.append(String.valueOf(s.getLevel()));
							support_list.append("\">");
							support_list.append(s.getName());
							support_list.append(" Lv.");
							support_list.append(String.valueOf(s.getDisplayLevel()));
							support_list.append("</a><br1>");
							if(++i % 5 != 0)
								continue;
							support_list.append("<br>");
						}
						html.replace("%magicList%", support_list.toString());
						html.replace("%mp%", String.valueOf(Math.round(getCurrentMp())));
						html.replace("%all%", Config.ALT_CH_ALL_BUFFS ? "<a action=\"bypass -h npc_%objectId%_support all\">Give all</a><br1><a action=\"bypass -h npc_%objectId%_support allW\">Give warrior</a><br1><a action=\"bypass -h npc_%objectId%_support allM\">Give mystic</a><br>" : "");
						sendHtmlMessage(player, html);
					}
					else if(val.equalsIgnoreCase("back"))
					{
						showChatWindow(player, 0);
					}
					else
					{
						NpcHtmlMessage html = new NpcHtmlMessage(player, this);
						html.setFile("residence/functions.htm");
						if(getResidence().isFunctionActive(5))
						{
							html.replace("%xp_regen%", String.valueOf(getResidence().getFunction(5).getLevel()) + "%");
						}
						else
						{
							html.replace("%xp_regen%", "0%");
						}
						if(getResidence().isFunctionActive(3))
						{
							html.replace("%hp_regen%", String.valueOf(getResidence().getFunction(3).getLevel()) + "%");
						}
						else
						{
							html.replace("%hp_regen%", "0%");
						}
						if(getResidence().isFunctionActive(4))
						{
							html.replace("%mp_regen%", String.valueOf(getResidence().getFunction(4).getLevel()) + "%");
						}
						else
						{
							html.replace("%mp_regen%", "0%");
						}
						sendHtmlMessage(player, html);
					}
				}
				else
				{
					if(actualCommand.equalsIgnoreCase("manage"))
					{
						if(!isHaveRigths(player, getPrivSetFunctions()))
						{
							player.sendPacket(SystemMsg.YOU_ARE_NOT_AUTHORIZED_TO_DO_THAT);
							return;
						}
						if(val.equalsIgnoreCase("recovery"))
						{
							if(st.countTokens() >= 1)
							{
								val = st.nextToken();
								boolean success = true;
								if(val.equalsIgnoreCase("hp"))
								{
									success = getResidence().updateFunctions(3, Integer.valueOf(st.nextToken()).intValue());
								}
								else if(val.equalsIgnoreCase("mp"))
								{
									success = getResidence().updateFunctions(4, Integer.valueOf(st.nextToken()).intValue());
								}
								else if(val.equalsIgnoreCase("exp"))
								{
									success = getResidence().updateFunctions(5, Integer.valueOf(st.nextToken()).intValue());
								}
								if(!success)
								{
									player.sendPacket(SystemMsg.THERE_IS_NOT_ENOUGH_ADENA_IN_THE_CLAN_HALL_WAREHOUSE);
								}
								else
								{
									broadcastDecoInfo();
								}
							}
							showManageRecovery(player);
						}
						else if(val.equalsIgnoreCase("other"))
						{
							if(st.countTokens() >= 1)
							{
								val = st.nextToken();
								boolean success = true;
								if(val.equalsIgnoreCase("item"))
								{
									success = getResidence().updateFunctions(2, Integer.valueOf(st.nextToken()).intValue());
								}
								else if(val.equalsIgnoreCase("tele"))
								{
									success = getResidence().updateFunctions(1, Integer.valueOf(st.nextToken()).intValue());
								}
								else if(val.equalsIgnoreCase("support"))
								{
									success = getResidence().updateFunctions(6, Integer.valueOf(st.nextToken()).intValue());
								}
								if(!success)
								{
									player.sendPacket(SystemMsg.THERE_IS_NOT_ENOUGH_ADENA_IN_THE_CLAN_HALL_WAREHOUSE);
								}
								else
								{
									broadcastDecoInfo();
								}
							}
							showManageOther(player);
						}
						else if(val.equalsIgnoreCase("deco"))
						{
							if(st.countTokens() >= 1)
							{
								val = st.nextToken();
								boolean success = true;
								if(val.equalsIgnoreCase("platform"))
								{
									success = getResidence().updateFunctions(8, Integer.valueOf(st.nextToken()).intValue());
								}
								else if(val.equalsIgnoreCase("curtain"))
								{
									success = getResidence().updateFunctions(7, Integer.valueOf(st.nextToken()).intValue());
								}
								if(!success)
								{
									player.sendPacket(SystemMsg.THERE_IS_NOT_ENOUGH_ADENA_IN_THE_CLAN_HALL_WAREHOUSE);
								}
								else
								{
									broadcastDecoInfo();
								}
							}
							showManageDeco(player);
						}
						else if(val.equalsIgnoreCase("back"))
						{
							showChatWindow(player, 0);
						}
						else
						{
							NpcHtmlMessage html = new NpcHtmlMessage(player, this);
							html.setFile("residence/manage.htm");
							sendHtmlMessage(player, html);
						}
						return;
					}
					if(actualCommand.equalsIgnoreCase("support"))
					{
						if(!isHaveRigths(player, getPrivUseFunctions()))
						{
							player.sendPacket(SystemMsg.YOU_ARE_NOT_AUTHORIZED_TO_DO_THAT);
							return;
						}
						setTarget(player);
						if(val.equals(""))
						{
							return;
						}
						if(!getResidence().isFunctionActive(6))
						{
							return;
						}
						if(val.startsWith("all"))
						{
							for(Object[] buff : getResidence().getFunction(6).getBuffs())
							{
								Skill s;
								if(val.equals("allM") && buff[1] == "W" || val.equals("allW") && buff[1] == "M" || useSkill((s = (Skill) buff[0]).getId(), s.getLevel(), player))
								{
									continue;
								}
								break;
							}
						}
						else
						{
							int skill_id = Integer.parseInt(val);
							int skill_lvl = 0;
							if(st.countTokens() >= 1)
							{
								skill_lvl = Integer.parseInt(st.nextToken());
							}
							useSkill(skill_id, skill_lvl, player);
						}
						onBypassFeedback(player, "functions support");
						return;
					}
				}
			}
		}
		super.onBypassFeedback(player, command);
	}
	
	private boolean useSkill(int id, int level, Player player)
	{
		Skill skill = SkillTable.getInstance().getInfo(id, level);
		if(skill == null)
		{
			player.sendMessage("Invalid skill " + id);
			return true;
		}
		if(skill.getMpConsume() > getCurrentMp())
		{
			NpcHtmlMessage html = new NpcHtmlMessage(player, this);
			html.setFile("residence/NeedCoolTime.htm");
			html.replace("%mp%", String.valueOf(Math.round(getCurrentMp())));
			sendHtmlMessage(player, html);
			return false;
		}
		altUseSkill(skill, player);
		return true;
	}
	
	private void sendHtmlMessage(Player player, NpcHtmlMessage html)
	{
		html.replace("%npcname%", HtmlUtils.htmlNpcName(getNpcId()));
		player.sendPacket(html);
	}
	
	private void replace(NpcHtmlMessage html, int type, String replace1, String replace2)
	{
		boolean proc;
		boolean bl = proc = type == 3 || type == 4 || type == 5;
		if(getResidence().isFunctionActive(type))
		{
			html.replace("%" + replace1 + "%", String.valueOf(getResidence().getFunction(type).getLevel()) + (proc ? "%" : ""));
			html.replace("%" + replace1 + "Price%", String.valueOf(getResidence().getFunction(type).getLease()));
			html.replace("%" + replace1 + "Date%", TimeUtils.toSimpleFormat(getResidence().getFunction(type).getEndTimeInMillis()));
		}
		else
		{
			html.replace("%" + replace1 + "%", "0");
			html.replace("%" + replace1 + "Price%", "0");
			html.replace("%" + replace1 + "Date%", "0");
		}
		if(getResidence().getFunction(type) != null && getResidence().getFunction(type).getLevels().size() > 0)
		{
			String out = "[<a action=\"bypass -h npc_%objectId%_manage " + replace2 + " " + replace1 + " 0\">Stop</a>]";
			Iterator<Integer> iterator = getResidence().getFunction(type).getLevels().iterator();
			while(iterator.hasNext())
			{
				int level = iterator.next();
				out = out + "[<a action=\"bypass -h npc_%objectId%_manage " + replace2 + " " + replace1 + " " + level + "\">" + level + (proc ? "%" : "") + "</a>]";
			}
			html.replace("%" + replace1 + "Manage%", out);
		}
		else
		{
			html.replace("%" + replace1 + "Manage%", "Not Available");
		}
	}
	
	private void showManageRecovery(Player player)
	{
		NpcHtmlMessage html = new NpcHtmlMessage(player, this);
		html.setFile("residence/edit_recovery.htm");
		replace(html, 5, "exp", "recovery");
		replace(html, 3, "hp", "recovery");
		replace(html, 4, "mp", "recovery");
		sendHtmlMessage(player, html);
	}
	
	private void showManageOther(Player player)
	{
		NpcHtmlMessage html = new NpcHtmlMessage(player, this);
		html.setFile("residence/edit_other.htm");
		replace(html, 1, "tele", "other");
		replace(html, 6, "support", "other");
		replace(html, 2, "item", "other");
		sendHtmlMessage(player, html);
	}
	
	private void showManageDeco(Player player)
	{
		NpcHtmlMessage html = new NpcHtmlMessage(player, this);
		html.setFile("residence/edit_deco.htm");
		replace(html, 7, "curtain", "deco");
		replace(html, 8, "platform", "deco");
		sendHtmlMessage(player, html);
	}
	
	protected boolean isHaveRigths(Player player, int rigthsToCheck)
	{
		return player.getClan() != null && (player.getClanPrivileges() & rigthsToCheck) == rigthsToCheck;
	}
	
	@Override
	public List<L2GameServerPacket> addPacketList(Player forPlayer, Creature dropper)
	{
		List list = super.addPacketList(forPlayer, dropper);
		L2GameServerPacket p = decoPacket();
		if(p != null)
		{
			list.add(p);
		}
		return list;
	}
}