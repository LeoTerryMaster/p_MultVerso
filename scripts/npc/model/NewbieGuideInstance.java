package npc.model;

import l2.gameserver.model.GameObjectsStorage;
import l2.gameserver.model.Player;
import l2.gameserver.model.base.Race;
import l2.gameserver.model.instances.NpcInstance;
import l2.gameserver.model.quest.QuestState;
import l2.gameserver.network.l2.s2c.NpcHtmlMessage;
import l2.gameserver.network.l2.s2c.PlaySound;
import l2.gameserver.network.l2.s2c.RadarControl;
import l2.gameserver.scripts.Functions;
import l2.gameserver.templates.npc.NpcTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.List;

public class NewbieGuideInstance extends NpcInstance
{
	private static final Logger _log = LoggerFactory.getLogger(NewbieGuideInstance.class);
	private static final List<?> mainHelpers = Arrays.asList(30598, 30599, 30600, 30601, 30602, 32135);
	
	public NewbieGuideInstance(int objectId, NpcTemplate template)
	{
		super(objectId, template);
	}
	
	@Override
	public void showChatWindow(Player player, int val, Object... arg)
	{
		if(val == 0 && mainHelpers.contains(getNpcId()))
		{
			if(player.getClassId().getLevel() == 1)
			{
				if(player.getVar("NewGuidReward") == null)
				{
					QuestState qs = player.getQuestState("_999_T1Tutorial");
					if(qs != null)
					{
						qs.unset("step");
					}
					player.setVar("NewGuidReward", "1", -1);
					boolean isMage;
					boolean bl = isMage = player.getClassId().getRace() != Race.orc && player.getClassId().isMage();
					if(isMage)
					{
						player.sendPacket(new PlaySound("tutorial_voice_027"));
						Functions.addItem(player, 5790, (long) 100);
					}
					else
					{
						player.sendPacket(new PlaySound("tutorial_voice_026"));
						Functions.addItem(player, 5789, (long) 200);
					}
					Functions.addItem(player, 8594, (long) 2);
				}
				String oldVar;
				if(player.getLevel() < 6)
				{
					if(player.isQuestCompleted("_001_LettersOfLove") || player.isQuestCompleted("_002_WhatWomenWant") || player.isQuestCompleted("_004_LongLivethePaagrioLord") || player.isQuestCompleted("_005_MinersFavor") || player.isQuestCompleted("_166_DarkMass"))
					{
						if(!player.getVarB("ng1"))
						{
							oldVar = player.getVar("ng1");
							player.setVar("ng1", oldVar == null ? "1" : String.valueOf(Integer.parseInt(oldVar) + 1), -1);
							player.addAdena(11567);
						}
						player.sendPacket(new NpcHtmlMessage(player, this, "newbiehelper/q1-2.htm", 0));
						return;
					}
					player.sendPacket(new NpcHtmlMessage(player, this, "newbiehelper/q1-1.htm", 0).replace("%tonpc%", getQuestNpc(1, player)));
					return;
				}
				if(player.getLevel() < 10)
				{
					if(player.getVarB("p1q2"))
					{
						if(!player.getVarB("ng2"))
						{
							oldVar = player.getVar("ng2");
							player.setVar("ng2", oldVar == null ? "1" : String.valueOf(Integer.parseInt(oldVar) + 1), -1);
						}
						player.sendPacket(new NpcHtmlMessage(player, this, "newbiehelper/q3-1.htm", 0).replace("%tonpc%", getQuestNpc(3, player)));
						return;
					}
					player.sendPacket(new NpcHtmlMessage(player, this, "newbiehelper/q2-1.htm", val).replace("%tonpc%", getQuestNpc(2, player)));
					return;
				}
				if(player.getLevel() < 15)
				{
					if(player.getVarB("p1q3"))
					{
						if(!player.getVarB("ng3"))
						{
							oldVar = player.getVar("ng3");
							player.setVar("ng3", oldVar == null ? "1" : String.valueOf(Integer.parseInt(oldVar) + 1), -1);
							player.addAdena(38180);
						}
						player.sendPacket(new NpcHtmlMessage(player, this, "newbiehelper/q4-1.htm", val).replace("%tonpc%", getQuestNpc(4, player)));
						return;
					}
					player.sendPacket(new NpcHtmlMessage(player, this, "newbiehelper/q3-1.htm", val).replace("%tonpc%", getQuestNpc(3, player)));
					return;
				}
				if(player.getLevel() < 18)
				{
					if(player.getVarB("p1q4"))
					{
						if(!player.getVarB("ng4"))
						{
							oldVar = player.getVar("ng4");
							player.setVar("ng4", oldVar == null ? "1" : String.valueOf(Integer.parseInt(oldVar) + 1), -1);
							player.addAdena(10018);
						}
						player.sendPacket(new NpcHtmlMessage(player, this, "newbiehelper/q4-2.htm", val));
						return;
					}
					player.sendPacket(new NpcHtmlMessage(player, this, "newbiehelper/q4-1.htm", val).replace("%tonpc%", getQuestNpc(4, player)));
					return;
				}
				player.sendPacket(new NpcHtmlMessage(player, this, "newbiehelper/q-no.htm", val));
				return;
			}
			player.sendPacket(new NpcHtmlMessage(player, this, "newbiehelper/q-no.htm", val));
			return;
		}
		super.showChatWindow(player, val);
	}
	
	public String getQuestNpc(int quest, Player player)
	{
		int val = 0;
		block:
		switch(quest)
		{
			case 1:
			{
				switch(getNpcId())
				{
					case 30598:
					{
						val = 30048;
						break block;
					}
					case 30599:
					{
						val = 30223;
						break block;
					}
					case 30600:
					{
						val = 30130;
						break block;
					}
					case 30601:
					{
						val = 30554;
						break block;
					}
					case 30602:
					{
						val = 30578;
					}
				}
				break;
			}
			case 2:
			{
				switch(getNpcId())
				{
					case 30598:
					{
						val = 30039;
						break block;
					}
					case 30599:
					{
						val = 30221;
						break block;
					}
					case 30600:
					{
						val = 30357;
						break block;
					}
					case 30601:
					{
						val = 30535;
						break block;
					}
					case 30602:
					{
						val = 30566;
					}
				}
				break;
			}
			case 3:
			{
				switch(player.getClassId())
				{
					case fighter:
					{
						val = 30008;
						break block;
					}
					case mage:
					{
						val = 30017;
						break block;
					}
					case elvenFighter:
					case elvenMage:
					{
						val = 30218;
						break block;
					}
					case darkFighter:
					case darkMage:
					{
						val = 30358;
						break block;
					}
					case orcFighter:
					case orcMage:
					{
						val = 30568;
						break block;
					}
					case dwarvenFighter:
					{
						val = 30523;
					}
				}
				break;
			}
			case 4:
			{
				switch(getNpcId())
				{
					case 30598:
					{
						val = 30050;
						break block;
					}
					case 30599:
					{
						val = 30222;
						break block;
					}
					case 30600:
					{
						val = 30145;
						break block;
					}
					case 30601:
					{
						val = 30519;
						break block;
					}
					case 30602:
					{
						val = 30571;
					}
				}
			}
		}
		if(val == 0)
		{
			_log.warn("WTF? L2NewbieGuideInstance " + getNpcId() + " not found next step " + quest + " for " + player.getClassId());
			return null;
		}
		NpcInstance npc = GameObjectsStorage.getByNpcId(val);
		if(npc == null)
		{
			return "";
		}
		player.sendPacket(new RadarControl(2, 1, npc.getLoc()));
		player.sendPacket(new RadarControl(0, 2, npc.getLoc()));
		return npc.getName();
	}
	
	@Override
	public String getHtmlPath(int npcId, int val, Player player)
	{
		String pom = val == 0 ? "" + npcId : "" + npcId + "-" + val;
		return "newbiehelper/" + pom + ".htm";
	}
}