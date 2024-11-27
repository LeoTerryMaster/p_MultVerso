package quests;

import l2.gameserver.model.Player;
import l2.gameserver.model.instances.NpcInstance;
import l2.gameserver.model.quest.Quest;
import l2.gameserver.model.quest.QuestState;
import l2.gameserver.scripts.ScriptFile;

public class _1103_OracleTeleport extends Quest implements ScriptFile
{
	private static final int GLUDIN_DAWN = 31078;
	private static final int GLUDIN_DUSK = 31085;
	private static final int GLUDIO_DAWN = 31079;
	private static final int GLUDIO_DUSK = 31086;
	private static final int DION_DAWN = 31080;
	private static final int DION_DUSK = 31087;
	private static final int GIRAN_DAWN = 31081;
	private static final int GIRAN_DUSK = 31088;
	private static final int OREN_DAWN = 31083;
	private static final int OREN_DUSK = 31090;
	private static final int ADEN_DAWN = 31084;
	private static final int ADEN_DUSK = 31091;
	private static final int HEINE_DAWN = 31082;
	private static final int HEINE_DUSK = 31089;
	private static final int GODDARD_DAWN = 31692;
	private static final int GODDARD_DUSK = 31693;
	private static final int RUNE_DAWN = 31694;
	private static final int RUNE_DUSK = 31695;
	private static final int SCHUTTGART_DAWN = 31997;
	private static final int SCHUTTGART_DUSK = 31998;
	private static final int HV_DAWN = 31168;
	private static final int HV_DUSK = 31169;
	
	public _1103_OracleTeleport()
	{
		super(false);
		int i;
		for(i = 31078;i <= 31091;++i)
		{
			addStartNpc(i);
		}
		for(i = 31168;i <= 31170;++i)
		{
			addStartNpc(i);
		}
		for(i = 31692;i <= 31696;++i)
		{
			addStartNpc(i);
		}
		for(i = 31997;i <= 31999;++i)
		{
			addStartNpc(i);
		}
		for(int j = 31127;j <= 31142;++j)
		{
			addStartNpc(j);
		}
	}
	
	@Override
	public void onLoad()
	{
	}
	
	@Override
	public void onReload()
	{
	}
	
	@Override
	public void onShutdown()
	{
	}
	
	@Override
	public String onTalk(NpcInstance npc, QuestState st)
	{
		int npcId = npc.getNpcId();
		Player player = st.getPlayer();
		String back = player.getVar("FestivalBackCoords");
		if(back == null)
		{
			back = "1";
		}
		String htmltext = "Started.htm";
		if(npcId == 31078)
		{
			player.teleToLocation(-80157, 111344, -4901);
			player.setVar("id", "1", -1);
			return htmltext;
		}
		if(npcId == 31079)
		{
			player.teleToLocation(-80157, 111344, -4901);
			player.setVar("id", "2", -1);
			return htmltext;
		}
		if(npcId == 31080)
		{
			player.teleToLocation(-80157, 111344, -4901);
			player.setVar("id", "3", -1);
			return htmltext;
		}
		if(npcId == 31081)
		{
			player.teleToLocation(-80157, 111344, -4901);
			player.setVar("id", "4", -1);
			return htmltext;
		}
		if(npcId == 31083)
		{
			player.teleToLocation(-80157, 111344, -4901);
			player.setVar("id", "5", -1);
			return htmltext;
		}
		if(npcId == 31084)
		{
			player.teleToLocation(-80157, 111344, -4901);
			player.setVar("id", "6", -1);
			return htmltext;
		}
		if(npcId == 31082)
		{
			player.teleToLocation(-80157, 111344, -4901);
			player.setVar("id", "7", -1);
			return htmltext;
		}
		if(npcId == 31692)
		{
			player.teleToLocation(-80157, 111344, -4901);
			player.setVar("id", "8", -1);
			return htmltext;
		}
		if(npcId == 31694)
		{
			player.teleToLocation(-80157, 111344, -4901);
			player.setVar("id", "9", -1);
			return htmltext;
		}
		if(npcId == 31997)
		{
			player.teleToLocation(-80157, 111344, -4901);
			player.setVar("id", "10", -1);
			return htmltext;
		}
		if(npcId == 31168)
		{
			player.teleToLocation(-80157, 111344, -4901);
			player.setVar("id", "11", -1);
			return htmltext;
		}
		if(npcId == 31085)
		{
			player.teleToLocation(-81261, 86531, -5157);
			player.setVar("id", "1", -1);
			return htmltext;
		}
		if(npcId == 31086)
		{
			player.teleToLocation(-81261, 86531, -5157);
			player.setVar("id", "2", -1);
			return htmltext;
		}
		if(npcId == 31087)
		{
			player.teleToLocation(-81261, 86531, -5157);
			player.setVar("id", "3", -1);
			return htmltext;
		}
		if(npcId == 31088)
		{
			player.teleToLocation(-81261, 86531, -5157);
			player.setVar("id", "4", -1);
			return htmltext;
		}
		if(npcId == 31090)
		{
			player.teleToLocation(-81261, 86531, -5157);
			player.setVar("id", "5", -1);
			return htmltext;
		}
		if(npcId == 31091)
		{
			player.teleToLocation(-81261, 86531, -5157);
			player.setVar("id", "6", -1);
			return htmltext;
		}
		if(npcId == 31089)
		{
			player.teleToLocation(-81261, 86531, -5157);
			player.setVar("id", "7", -1);
			return htmltext;
		}
		if(npcId == 31693)
		{
			player.teleToLocation(-81261, 86531, -5157);
			player.setVar("id", "8", -1);
			return htmltext;
		}
		if(npcId == 31695)
		{
			player.teleToLocation(-81261, 86531, -5157);
			player.setVar("id", "9", -1);
			return htmltext;
		}
		if(npcId == 31998)
		{
			player.teleToLocation(-81261, 86531, -5157);
			player.setVar("id", "10", -1);
			return htmltext;
		}
		if(npcId == 31169)
		{
			player.teleToLocation(-81261, 86531, -5157);
			player.setVar("id", "11", -1);
			return htmltext;
		}
		htmltext = "Completed.htm";
		if(back.equals("1"))
		{
			player.teleToLocation(-80826, 149775, -3043);
			return htmltext;
		}
		if(back.equals("2"))
		{
			player.teleToLocation(-12672, 122776, -3116);
			return htmltext;
		}
		if(back.equals("3"))
		{
			player.teleToLocation(15670, 142983, -2705);
			return htmltext;
		}
		if(back.equals("4"))
		{
			player.teleToLocation(83400, 147943, -3404);
			return htmltext;
		}
		if(back.equals("5"))
		{
			player.teleToLocation(82956, 53162, -1495);
			return htmltext;
		}
		if(back.equals("6"))
		{
			player.teleToLocation(146331, 25762, -2018);
			return htmltext;
		}
		if(back.equals("7"))
		{
			player.teleToLocation(111409, 219364, -3545);
			return htmltext;
		}
		if(back.equals("8"))
		{
			player.teleToLocation(147928, -55273, -2734);
			return htmltext;
		}
		if(back.equals("9"))
		{
			player.teleToLocation(43799, -47727, -798);
			return htmltext;
		}
		if(back.equals("10"))
		{
			player.teleToLocation(87386, -143246, -1293);
			return htmltext;
		}
		if(back.equals("11"))
		{
			player.teleToLocation(116819, 76994, -2714);
			return htmltext;
		}
		return htmltext;
	}
}