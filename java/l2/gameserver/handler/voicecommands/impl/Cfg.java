package l2.gameserver.handler.voicecommands.impl;

import l2.commons.dbutils.DbUtils;
import l2.gameserver.Config;
import l2.gameserver.database.DatabaseFactory;
import l2.gameserver.handler.voicecommands.IVoicedCommandHandler;
import l2.gameserver.model.Player;
import l2.gameserver.model.World;
import l2.gameserver.network.authcomm.AuthServerCommunication;
import l2.gameserver.network.authcomm.gs2as.IGPwdCng;
import l2.gameserver.network.l2.s2c.NpcHtmlMessage;
import org.apache.commons.lang3.math.NumberUtils;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Cfg implements IVoicedCommandHandler
{
	private static final Pattern PASSWORD_BYPASS_PATTERN = Pattern.compile("^([\\w\\d_-]{4,18})\\s+([\\w\\d_-]{4,16})$");
	private static final long PASSWORD_CHANGE_INTERVAL = 3600000L;
	private final String[] _commandList = {"cfg", "menu", "password", "repair"};
	
	@Override
	public boolean useVoicedCommand(String command, Player activeChar, String args)
	{
		NpcHtmlMessage dialog;
		if(!command.equalsIgnoreCase(_commandList[0]) && !command.equalsIgnoreCase(_commandList[1]))
		{
			if(command.equalsIgnoreCase(_commandList[2]))
			{
				String lastChanged = activeChar.getVar("LastPwdChng");
				boolean canChange = true;
				if(lastChanged != null && !lastChanged.isEmpty())
				{
					long lastChange = Long.parseLong(lastChanged) * 1000L;
					if(lastChange + 3600000L > System.currentTimeMillis())
					{
						activeChar.sendMessage("Password can't be change so frequently.");
						canChange = false;
					}
				}
				
				if(canChange && !args.isEmpty())
				{
					Matcher m = PASSWORD_BYPASS_PATTERN.matcher(args);
					if(m.find() && m.groupCount() == 2)
					{
						String oldpassword = m.group(1);
						String newpassword = m.group(2);
						AuthServerCommunication.getInstance().sendPacket(new IGPwdCng(activeChar, oldpassword, newpassword));
						return true;
					}
					
					activeChar.sendMessage("Password requirement's is not met!");
				}
				
				dialog = new NpcHtmlMessage(5);
				dialog.setFile("command/passchg.htm");
				activeChar.sendPacket(dialog);
			}
			else if(command.equalsIgnoreCase(_commandList[3]))
			{
				dialog = new NpcHtmlMessage(5);
				dialog.setFile("command/repair.htm");
				if(args.isEmpty())
				{
					StringBuilder cl = new StringBuilder();
					Connection con = null;
					PreparedStatement fpstmt = null;
					ResultSet rset = null;
					
					try
					{
						con = DatabaseFactory.getInstance().getConnection();
						fpstmt = con.prepareStatement("SELECT `obj_Id`, `char_name` FROM `characters` WHERE `account_name` = ? AND `online` = 0");
						fpstmt.setString(1, activeChar.getAccountName());
						rset = fpstmt.executeQuery();
						
						while(rset.next())
						{
							String charName = rset.getString("char_name");
							int charId = rset.getInt("obj_Id");
							cl.append("<a action=\"bypass -h user_repair " + charId + "\">" + charName + "</a><br1>");
						}
					}
					catch(Exception e)
					{
					}
					finally
					{
						DbUtils.closeQuietly(con, fpstmt, rset);
					}
					
					dialog.replace("%repair%", cl.toString());
				}
				else
				{
					Connection con = null;
					PreparedStatement fpstmt = null;
					ResultSet rset = null;
					
					try
					{
						con = DatabaseFactory.getInstance().getConnection();
						fpstmt = con.prepareStatement("SELECT * FROM `characters` WHERE `account_name` = ? AND `obj_Id` = ? AND `online` = 0");
						fpstmt.setString(1, activeChar.getAccountName());
						int charId = Integer.parseInt(args);
						fpstmt.setInt(2, charId);
						rset = fpstmt.executeQuery();
						if(!rset.next())
						{
							activeChar.sendMessage("Character not found.");
							return true;
						}
						
						if(World.getPlayer(charId) != null)
						{
							activeChar.sendMessage("Character online.");
							return true;
						}
						
						DbUtils.close(fpstmt, rset);
						fpstmt = con.prepareStatement("UPDATE `characters` SET `x` = 17867, `y` = 170259, `z` = -3503 WHERE `obj_Id` = ?");
						fpstmt.setInt(1, charId);
						fpstmt.executeUpdate();
						DbUtils.close(fpstmt);
						fpstmt = con.prepareStatement("UPDATE `items` SET `location` = \"INVENTORY\" WHERE `location` = \"PAPERDOLL\" AND `owner_id` = ?");
						fpstmt.setInt(1, charId);
						fpstmt.executeUpdate();
						DbUtils.close(fpstmt);
						dialog.replace("%repair%", "Character successfully repaired.");
						activeChar.sendMessage("Character successfully repaired.");
					}
					catch(Exception e)
					{
						dialog.replace("%repair%", "Character reparation failed.");
						e.printStackTrace();
					}
					finally
					{
						DbUtils.closeQuietly(con, fpstmt, rset);
					}
				}
				
				activeChar.sendPacket(dialog);
			}
		}
		else
		{
			if(args != null)
			{
				String[] param = args.split(" ");
				if(param.length == 2)
				{
					if(param[0].equalsIgnoreCase("dli"))
					{
						if(param[1].equalsIgnoreCase("on"))
						{
							activeChar.setVar("DroplistIcons", "1", -1L);
						}
						else if(param[1].equalsIgnoreCase("of"))
						{
							activeChar.unsetVar("DroplistIcons");
						}
					}
					
					if(param[0].equalsIgnoreCase("noe"))
					{
						if(param[1].equalsIgnoreCase("on"))
						{
							activeChar.setVar("NoExp", "1", -1L);
						}
						else if(param[1].equalsIgnoreCase("of"))
						{
							activeChar.unsetVar("NoExp");
						}
					}
					
					if(param[0].equalsIgnoreCase("notraders"))
					{
						if(param[1].equalsIgnoreCase("on"))
						{
							activeChar.setNotShowTraders(true);
							activeChar.setVar("notraders", "true", -1L);
						}
						else if(param[1].equalsIgnoreCase("of"))
						{
							activeChar.setNotShowTraders(false);
							activeChar.unsetVar("notraders");
						}
					}
					
					int time;
					if(param[0].equalsIgnoreCase("buffAnimRange"))
					{
						time = 15 * NumberUtils.toInt(param[1], 0);
						if(time < 0)
						{
							time = -1;
						}
						else if(time > 1500)
						{
							time = 1500;
						}
						
						activeChar.setBuffAnimRange(time);
						activeChar.setVar("buffAnimRange", String.valueOf(time), -1L);
					}
					
					if(param[0].equalsIgnoreCase("noShift"))
					{
						if(param[1].equalsIgnoreCase("on"))
						{
							activeChar.setVar("noShift", "1", -1L);
						}
						else if(param[1].startsWith("of"))
						{
							activeChar.unsetVar("noShift");
						}
					}
					
					if(param[0].equalsIgnoreCase("hwidlock") && activeChar.getNetConnection() != null && activeChar.getNetConnection().getHwid() != null && !activeChar.getNetConnection().getHwid().isEmpty())
					{
						if(param[1].equalsIgnoreCase("on"))
						{
							activeChar.setHWIDLock(activeChar.getNetConnection().getHwid());
						}
						else if(param[1].startsWith("of"))
						{
							activeChar.setHWIDLock(null);
						}
					}
					
					if(param[0].equalsIgnoreCase("iplock") && activeChar.getNetConnection() != null && activeChar.getNetConnection().getIpAddr() != null && !activeChar.getNetConnection().getIpAddr().isEmpty())
					{
						if(param[1].equalsIgnoreCase("on"))
						{
							activeChar.setIPLock(activeChar.getNetConnection().getIpAddr());
						}
						else if(param[1].startsWith("of"))
						{
							activeChar.setIPLock(null);
						}
					}
					
					if(param[0].equalsIgnoreCase("lang"))
					{
						if(param[1].equalsIgnoreCase("en"))
						{
							activeChar.setVar("lang@", "en", -1L);
						}
						else if(param[1].equalsIgnoreCase("ru"))
						{
							activeChar.setVar("lang@", "ru", -1L);
						}
					}
					
					if(Config.SERVICES_ENABLE_NO_CARRIER && param[0].equalsIgnoreCase("noCarrier"))
					{
						time = NumberUtils.toInt(param[1], 0);
						if(time <= 0)
						{
							time = 0;
						}
						else if(time > Config.SERVICES_NO_CARRIER_MAX_TIME)
						{
							time = Config.SERVICES_NO_CARRIER_MAX_TIME;
						}
						else if(time < Config.SERVICES_NO_CARRIER_MIN_TIME)
						{
							time = Config.SERVICES_NO_CARRIER_MIN_TIME;
						}
						
						activeChar.setVar("noCarrier", String.valueOf(time), -1L);
					}
					
					if(param[0].equalsIgnoreCase("translit"))
					{
						if(param[1].equalsIgnoreCase("on"))
						{
							activeChar.setVar("translit", "tl", -1L);
						}
						else if(param[1].equalsIgnoreCase("la"))
						{
							activeChar.setVar("translit", "tc", -1L);
						}
						else if(param[1].equalsIgnoreCase("of"))
						{
							activeChar.unsetVar("translit");
						}
					}
					
					if(Config.AUTO_LOOT_INDIVIDUAL && param[0].equalsIgnoreCase("autoloot"))
					{
						if(param[1].equalsIgnoreCase("on"))
						{
							activeChar.setAutoLoot(true);
							if(Config.AUTO_LOOT_HERBS)
							{
								activeChar.setAutoLootHerbs(true);
							}
							
							activeChar.setAutoLootAdena(true);
							activeChar.sendMessage("Autolooting all.");
						}
						else if(param[1].equalsIgnoreCase("ad"))
						{
							activeChar.setAutoLoot(false);
							activeChar.setAutoLootHerbs(false);
							activeChar.setAutoLootAdena(true);
							activeChar.sendMessage("Autolooting adena only.");
						}
						else if(param[1].equalsIgnoreCase("of"))
						{
							activeChar.setAutoLoot(false);
							activeChar.setAutoLootHerbs(false);
							activeChar.setAutoLootAdena(false);
							activeChar.sendMessage("Autolooting off.");
						}
					}
				}
			}
			
			dialog = new NpcHtmlMessage(5);
			dialog.setFile("command/cfg.htm");
			dialog.replace("%dli%", activeChar.getVarB("DroplistIcons") ? "On" : "Off");
			dialog.replace("%noe%", activeChar.getVarB("NoExp") ? "On" : "Off");
			dialog.replace("%notraders%", activeChar.getVarB("notraders") ? "On" : "Off");
			dialog.replace("%noShift%", activeChar.getVarB("noShift") ? "On" : "Off");
			dialog.replace("%noCarrier%", Config.SERVICES_ENABLE_NO_CARRIER ? activeChar.getVarB("noCarrier") ? activeChar.getVar("noCarrier") : "0" : "N/A");
			if(activeChar.isAutoLootEnabled())
			{
				dialog.replace("%autoloot%", "All");
			}
			else if(activeChar.isAutoLootAdenaEnabled())
			{
				dialog.replace("%autoloot%", "Adena");
			}
			else
			{
				dialog.replace("%autoloot%", "Off");
			}
			
			if(activeChar.getLangId() == 0)
			{
				dialog.replace("%lang%", "En");
			}
			else if(activeChar.getLangId() == 1)
			{
				dialog.replace("%lang%", "Ru");
			}
			else
			{
				dialog.replace("%lang%", "Unk");
			}
			
			if(activeChar.getHWIDLock() != null && activeChar.getNetConnection() != null && activeChar.getNetConnection().getHwid() != null && !activeChar.getNetConnection().getHwid().isEmpty())
			{
				dialog.replace("%hwidlock%", "On");
			}
			else
			{
				dialog.replace("%hwidlock%", "Off");
			}
			
			if(activeChar.getIPLock() != null && activeChar.getNetConnection() != null && activeChar.getNetConnection().getIpAddr() != null && !activeChar.getNetConnection().getIpAddr().isEmpty())
			{
				dialog.replace("%iplock%", "On");
			}
			else
			{
				dialog.replace("%iplock%", "Off");
			}
			
			if(activeChar.buffAnimRange() < 0)
			{
				dialog.replace("%buffAnimRange%", "Off");
			}
			else if(activeChar.buffAnimRange() == 0)
			{
				if(activeChar.isLangRus())
				{
					dialog.replace("%buffAnimRange%", "Свои");
				}
				else
				{
					dialog.replace("%buffAnimRange%", "Self");
				}
			}
			else
			{
				dialog.replace("%buffAnimRange%", String.valueOf(activeChar.buffAnimRange() / 15));
			}
			
			String tl = activeChar.getVar("translit");
			if(tl == null)
			{
				dialog.replace("%translit%", "Off");
			}
			else if(tl.equals("tl"))
			{
				dialog.replace("%translit%", "On");
			}
			else
			{
				dialog.replace("%translit%", "Lt");
			}
			
			activeChar.sendPacket(dialog);
		}
		
		return true;
	}
	
	@Override
	public String[] getVoicedCommandList()
	{
		return _commandList;
	}
}