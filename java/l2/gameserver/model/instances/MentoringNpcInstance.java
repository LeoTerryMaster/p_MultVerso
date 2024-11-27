package l2.gameserver.model.instances;

import l2.gameserver.Config;
import l2.gameserver.dao.CharacterDAO;
import l2.gameserver.model.Player;
import l2.gameserver.model.World;
import l2.gameserver.model.entity.MentorController;
import l2.gameserver.model.entity.Mentoring;
import l2.gameserver.templates.npc.NpcTemplate;

import java.util.StringTokenizer;

public class MentoringNpcInstance extends NpcInstance
{
	public MentoringNpcInstance(int objectId, NpcTemplate template)
	{
		super(objectId, template);
	}
	
	@Override
	public void onBypassFeedback(Player player, String command)
	{
		if(!canBypassCheck(player, this))
		{
			return;
		}
		if(Config.MENTORING_ENABLE)
		{
			if(command.startsWith("mngslot"))
			{
				if(MentorController.isCanBeMentor(player))
				{
					Mentoring m = player.getMentoring();
					StringBuilder sb = new StringBuilder();
					sb.append("<table width=200>");
					int cnt = 0;
					if(m != null)
					{
						for(Integer eid : m.getMenteesIds())
						{
							Player mentee = World.getPlayer(eid);
							String name = mentee != null ? mentee.getName() : CharacterDAO.getInstance().getNameByObjectId(eid);
							sb.append("<tr><td width=20><button action=\"");
							sb.append("bypass -h npc_%objectId%_menterrem ");
							sb.append(eid);
							sb.append("\" width=16 height=16 back=\"L2UI_CH3.questwndminusbtn_over\" fore=\"L2UI_CH3.QuestWndMinusBtn\"></td>");
							sb.append("<td width=180 align=left>");
							sb.append(name);
							sb.append("</td></tr>");
							++cnt;
						}
					}
					if(cnt < 3)
					{
						sb.append("<tr><td width=20><button action=\"bypass -h npc_%objectId%_menteradd $cname\" width=16 height=16 back=\"L2UI_CH3.questwndplusbtn_over\" ");
						sb.append("fore=\"L2UI_CH3.QuestWndPlusBtn\"></td><td width=180 align=left><edit var=\"cname\" width=140 height=14 length=14></td></tr>");
					}
					sb.append("</table>");
					showChatWindow(player, 14, "%list%", sb.toString());
				}
				else
				{
					showChatWindow(player, 10);
				}
				return;
			}
			if(command.startsWith("menterrem"))
			{
				Mentoring m;
				if(MentorController.isCanBeMentor(player) && (m = player.getMentoring()) != null)
				{
					StringTokenizer st = new StringTokenizer(command, " ");
					st.nextToken();
					MentorController.getInstance().removeMentoring(m, Integer.parseInt(st.nextToken()));
				}
				showChatWindow(player, 0);
				return;
			}
			if(command.startsWith("menteradd"))
			{
				StringTokenizer st;
				if(MentorController.isCanBeMentor(player) && (st = new StringTokenizer(command, " ")).hasMoreTokens())
				{
					st.nextToken();
					if(st.hasMoreTokens())
					{
						MentorController.getInstance().askForMentoring(player, st.nextToken());
					}
				}
				showChatWindow(player, 0);
				return;
			}
			if(command.startsWith("mngcontract"))
			{
				if(MentorController.isCanBeMentee(player))
				{
					Mentoring m = player.getMentoring();
					if(m != null)
					{
						Player mentor = m.getMentor();
						if(mentor != null)
						{
							showChatWindow(player, 13, "%name%", mentor.getName());
						}
						else
						{
							showChatWindow(player, 13, "%name%", CharacterDAO.getInstance().getNameByObjectId(m.getMentorObjId()));
						}
					}
					else
					{
						showChatWindow(player, 12);
					}
				}
				else
				{
					showChatWindow(player, 11);
				}
				return;
			}
			if(command.startsWith("menteeexit"))
			{
				Mentoring m;
				if(MentorController.isCanBeMentee(player) && (m = player.getMentoring()) != null)
				{
					MentorController.getInstance().removeMentoring(m, player.getObjectId());
				}
				showChatWindow(player, 0);
				return;
			}
		}
		else
		{
			showChatWindow(player, 16);
			return;
		}
		super.onBypassFeedback(player, command);
	}
}