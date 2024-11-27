package l2.gameserver.model.instances;

import l2.gameserver.Config;
import l2.gameserver.model.Party;
import l2.gameserver.model.Player;
import l2.gameserver.model.entity.Reflection;
import l2.gameserver.model.entity.SevenSigns;
import l2.gameserver.model.entity.SevenSignsFestival.DarknessFestival;
import l2.gameserver.model.entity.SevenSignsFestival.SevenSignsFestival;
import l2.gameserver.network.l2.components.CustomMessage;
import l2.gameserver.network.l2.s2c.NpcHtmlMessage;
import l2.gameserver.network.l2.s2c.SystemMessage2;
import l2.gameserver.templates.StatsSet;
import l2.gameserver.templates.npc.NpcTemplate;

import java.util.Calendar;

public final class FestivalGuideInstance extends NpcInstance
{
	protected int _festivalType;
	protected int _festivalOracle;
	
	public FestivalGuideInstance(int objectId, NpcTemplate template)
	{
		super(objectId, template);
		switch(getNpcId())
		{
			case 31127:
			case 31132:
			{
				_festivalType = 0;
				_festivalOracle = 2;
				break;
			}
			case 31128:
			case 31133:
			{
				_festivalType = 1;
				_festivalOracle = 2;
				break;
			}
			case 31129:
			case 31134:
			{
				_festivalType = 2;
				_festivalOracle = 2;
				break;
			}
			case 31130:
			case 31135:
			{
				_festivalType = 3;
				_festivalOracle = 2;
				break;
			}
			case 31131:
			case 31136:
			{
				_festivalType = 4;
				_festivalOracle = 2;
				break;
			}
			case 31137:
			case 31142:
			{
				_festivalType = 0;
				_festivalOracle = 1;
				break;
			}
			case 31138:
			case 31143:
			{
				_festivalType = 1;
				_festivalOracle = 1;
				break;
			}
			case 31139:
			case 31144:
			{
				_festivalType = 2;
				_festivalOracle = 1;
				break;
			}
			case 31140:
			case 31145:
			{
				_festivalType = 3;
				_festivalOracle = 1;
				break;
			}
			case 31141:
			case 31146:
			{
				_festivalType = 4;
				_festivalOracle = 1;
			}
		}
	}
	
	@Override
	public void onBypassFeedback(Player player, String command)
	{
		if(!canBypassCheck(player, this))
		{
			return;
		}
		if(SevenSigns.getInstance().getPlayerCabal(player) == 0)
		{
			player.sendMessage("You must be Seven Signs participant.");
			return;
		}
		if(command.startsWith("FestivalDesc"))
		{
			int val = Integer.parseInt(command.substring(13));
			showChatWindow(player, val, null, true);
			return;
		}
		else if(command.startsWith("Festival"))
		{
			Party playerParty = player.getParty();
			int val = Integer.parseInt(command.substring(9, 10));
			switch(val)
			{
				case 1:
				{
					showChatWindow(player, 1, null, false);
					return;
				}
				case 2:
				{
					if(SevenSigns.getInstance().getCurrentPeriod() != 1)
					{
						showChatWindow(player, 2, "a", false);
						return;
					}
					if(SevenSignsFestival.getInstance().isFestivalInitialized())
					{
						player.sendMessage(new CustomMessage("l2p.gameserver.model.instances.L2FestivalGuideInstance.InProgress", player));
						return;
					}
					if(playerParty == null || playerParty.getMemberCount() < Config.FESTIVAL_MIN_PARTY_SIZE)
					{
						showChatWindow(player, 2, "b", false);
						return;
					}
					if(!playerParty.isLeader(player))
					{
						showChatWindow(player, 2, "c", false);
						return;
					}
					int maxlevel = SevenSignsFestival.getMaxLevelForFestival(_festivalType);
					for(Player p : playerParty.getPartyMembers())
					{
						if(p.getLevel() > maxlevel)
						{
							showChatWindow(player, 2, "d", false);
							return;
						}
						if(SevenSigns.getInstance().getPlayerCabal(p) != 0)
							continue;
						showChatWindow(player, 2, "g", false);
						return;
					}
					if(player.isFestivalParticipant())
					{
						showChatWindow(player, 2, "f", false);
						return;
					}
					int stoneType = Integer.parseInt(command.substring(11));
					long stonesNeeded = (long) Math.floor((double) SevenSignsFestival.getStoneCount(_festivalType, stoneType) * Config.FESTIVAL_RATE_PRICE);
					if(!player.getInventory().destroyItemByItemId(stoneType, stonesNeeded))
					{
						player.sendMessage(new CustomMessage("l2p.gameserver.model.instances.L2FestivalGuideInstance.NotEnoughSSType", player));
						return;
					}
					player.sendPacket(SystemMessage2.removeItems(stoneType, stonesNeeded));
					SevenSignsFestival.getInstance().addAccumulatedBonus(_festivalType, stoneType, stonesNeeded);
					new DarknessFestival(player.getParty(), SevenSigns.getInstance().getPlayerCabal(player), _festivalType);
					showChatWindow(player, 2, "e", false);
					return;
				}
				case 4:
				{
					StringBuilder strBuffer = new StringBuilder("<html><body>Festival Guide:<br>These are the top scores of the week, for the ");
					StatsSet dawnData = SevenSignsFestival.getInstance().getHighestScoreData(2, _festivalType);
					StatsSet duskData = SevenSignsFestival.getInstance().getHighestScoreData(1, _festivalType);
					StatsSet overallData = SevenSignsFestival.getInstance().getOverallHighestScoreData(_festivalType);
					int dawnScore = dawnData.getInteger("score");
					int duskScore = duskData.getInteger("score");
					int overallScore = 0;
					if(overallData != null)
					{
						overallScore = overallData.getInteger("score");
					}
					strBuffer.append(SevenSignsFestival.getFestivalName(_festivalType) + " festival.<br>");
					if(dawnScore > 0)
					{
						strBuffer.append("Dawn: " + calculateDate(dawnData.getString("date")) + ". Score " + dawnScore + "<br>" + dawnData.getString("names").replaceAll(",", ", ") + "<br>");
					}
					else
					{
						strBuffer.append("Dawn: No record exists. Score 0<br>");
					}
					if(duskScore > 0)
					{
						strBuffer.append("Dusk: " + calculateDate(duskData.getString("date")) + ". Score " + duskScore + "<br>" + duskData.getString("names").replaceAll(",", ", ") + "<br>");
					}
					else
					{
						strBuffer.append("Dusk: No record exists. Score 0<br>");
					}
					if(overallScore > 0 && overallData != null)
					{
						String cabalStr = "Children of Dusk";
						if(overallData.getInteger("cabal") == 2)
						{
							cabalStr = "Children of Dawn";
						}
						strBuffer.append("Consecutive top scores: " + calculateDate(overallData.getString("date")) + ". Score " + overallScore + "<br>Affilated side: " + cabalStr + "<br>" + overallData.getString("names").replaceAll(",", ", ") + "<br>");
					}
					else
					{
						strBuffer.append("Consecutive top scores: No record exists. Score 0<br>");
					}
					strBuffer.append("<a action=\"bypass -h npc_" + getObjectId() + "_Chat 0\">Go back.</a></body></html>");
					NpcHtmlMessage html = new NpcHtmlMessage(player, this);
					html.setHtml(strBuffer.toString());
					player.sendPacket(html);
					return;
				}
				case 8:
				{
					if(playerParty == null)
					{
						return;
					}
					if(!playerParty.isLeader(player))
					{
						showChatWindow(player, 8, "a", false);
						return;
					}
					Reflection r = getReflection();
					if(!(r instanceof DarknessFestival))
						return;
					if(((DarknessFestival) r).increaseChallenge())
					{
						showChatWindow(player, 8, "b", false);
						return;
					}
					showChatWindow(player, 8, "c", false);
					return;
				}
				case 9:
				{
					if(playerParty == null)
					{
						return;
					}
					Reflection r = getReflection();
					if(!(r instanceof DarknessFestival))
					{
						return;
					}
					if(playerParty.isLeader(player))
					{
						r.collapse();
						return;
					}
					if(playerParty.getMemberCount() > Config.FESTIVAL_MIN_PARTY_SIZE)
					{
						player.leaveParty();
						return;
					}
					player.sendMessage("Only party leader can leave festival, if minmum party member is reached.");
					return;
				}
				default:
				{
					showChatWindow(player, val, null, false);
					return;
				}
			}
		}
		else
		{
			super.onBypassFeedback(player, command);
		}
	}
	
	private void showChatWindow(Player player, int val, String suffix, boolean isDescription)
	{
		String filename = "seven_signs/festival/";
		filename = filename + (isDescription ? "desc_" : "festival_");
		filename = filename + (suffix != null ? new StringBuilder().append(val).append(suffix).append(".htm").toString() : new StringBuilder().append(val).append(".htm").toString());
		NpcHtmlMessage html = new NpcHtmlMessage(player, this);
		html.setFile(filename);
		html.replace("%festivalType%", SevenSignsFestival.getFestivalName(_festivalType));
		html.replace("%min%", String.valueOf(Config.FESTIVAL_MIN_PARTY_SIZE));
		if(val == 1)
		{
			html.replace("%price1%", String.valueOf((long) Math.floor((double) SevenSignsFestival.getStoneCount(_festivalType, 6362) * Config.FESTIVAL_RATE_PRICE)));
			html.replace("%price2%", String.valueOf((long) Math.floor((double) SevenSignsFestival.getStoneCount(_festivalType, 6361) * Config.FESTIVAL_RATE_PRICE)));
			html.replace("%price3%", String.valueOf((long) Math.floor((double) SevenSignsFestival.getStoneCount(_festivalType, 6360) * Config.FESTIVAL_RATE_PRICE)));
		}
		if(val == 5)
		{
			html.replace("%statsTable%", getStatsTable());
		}
		if(val == 6)
		{
			html.replace("%bonusTable%", getBonusTable());
		}
		player.sendPacket(html);
		player.sendActionFailed();
	}
	
	@Override
	public void showChatWindow(Player player, int val, Object... arg)
	{
		String filename = "seven_signs/";
		switch(getNpcId())
		{
			case 31127:
			case 31128:
			case 31129:
			case 31130:
			case 31131:
			{
				filename = filename + "festival/dawn_guide.htm";
				break;
			}
			case 31137:
			case 31138:
			case 31139:
			case 31140:
			case 31141:
			{
				filename = filename + "festival/dusk_guide.htm";
				break;
			}
			case 31132:
			case 31133:
			case 31134:
			case 31135:
			case 31136:
			case 31142:
			case 31143:
			case 31144:
			case 31145:
			case 31146:
			{
				filename = filename + "festival/festival_witch.htm";
				break;
			}
			default:
			{
				filename = getHtmlPath(getNpcId(), val, player);
			}
		}
		player.sendPacket(new NpcHtmlMessage(player, this, filename, val));
	}
	
	private String getStatsTable()
	{
		StringBuilder tableHtml = new StringBuilder();
		for(int i = 0;i < 5;++i)
		{
			long dawnScore = SevenSignsFestival.getInstance().getHighestScore(2, i);
			long duskScore = SevenSignsFestival.getInstance().getHighestScore(1, i);
			String festivalName = SevenSignsFestival.getFestivalName(i);
			String winningCabal = "Children of Dusk";
			if(dawnScore > duskScore)
			{
				winningCabal = "Children of Dawn";
			}
			else if(dawnScore == duskScore)
			{
				winningCabal = "None";
			}
			tableHtml.append("<tr><td width=\"100\" align=\"center\">" + festivalName + "</td><td align=\"center\" width=\"35\">" + duskScore + "</td><td align=\"center\" width=\"35\">" + dawnScore + "</td><td align=\"center\" width=\"130\">" + winningCabal + "</td></tr>");
		}
		return tableHtml.toString();
	}
	
	private String getBonusTable()
	{
		StringBuilder tableHtml = new StringBuilder();
		for(int i = 0;i < 5;++i)
		{
			long accumScore = SevenSignsFestival.getInstance().getAccumulatedBonus(i);
			String festivalName = SevenSignsFestival.getFestivalName(i);
			tableHtml.append("<tr><td align=\"center\" width=\"150\">" + festivalName + "</td><td align=\"center\" width=\"150\">" + accumScore + "</td></tr>");
		}
		return tableHtml.toString();
	}
	
	private String calculateDate(String milliFromEpoch)
	{
		long numMillis = Long.valueOf(milliFromEpoch);
		Calendar calCalc = Calendar.getInstance();
		calCalc.setTimeInMillis(numMillis);
		return "" + calCalc.get(1) + "/" + calCalc.get(2) + "/" + calCalc.get(5);
	}
}