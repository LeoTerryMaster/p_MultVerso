package l2.gameserver.network.l2.c2s;

import l2.gameserver.Config;
import l2.gameserver.data.xml.holder.MultiSellHolder;
import l2.gameserver.handler.admincommands.AdminCommandHandler;
import l2.gameserver.handler.bbs.CommunityBoardManager;
import l2.gameserver.handler.bbs.ICommunityBoardHandler;
import l2.gameserver.handler.voicecommands.IVoicedCommandHandler;
import l2.gameserver.handler.voicecommands.VoicedCommandHandler;
import l2.gameserver.instancemanager.BypassManager;
import l2.gameserver.model.GameObject;
import l2.gameserver.model.Player;
import l2.gameserver.model.entity.oly.CompetitionController;
import l2.gameserver.model.entity.oly.HeroController;
import l2.gameserver.model.instances.NpcInstance;
import l2.gameserver.network.l2.GameClient;
import l2.gameserver.network.l2.s2c.NpcHtmlMessage;
import l2.gameserver.network.l2.s2c.SystemMessage;
import l2.gameserver.scripts.Scripts;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.StringTokenizer;

public class RequestBypassToServer extends L2GameClientPacket
{
	private static final Logger _log = LoggerFactory.getLogger(RequestBypassToServer.class);
	private BypassManager.DecodedBypass bp;
	private String _bypass;
	
	private static void comeHere(GameClient client)
	{
		GameObject obj = client.getActiveChar().getTarget();
		if(obj != null && obj.isNpc())
		{
			NpcInstance temp = (NpcInstance) obj;
			Player activeChar = client.getActiveChar();
			temp.setTarget(activeChar);
			temp.moveToLocation(activeChar.getLoc(), 0, true);
		}
	}
	
	private static void playerHelp(Player activeChar, String path)
	{
		NpcHtmlMessage html = new NpcHtmlMessage(5);
		html.setFile(path);
		activeChar.sendPacket(html);
	}
	
	@Override
	protected void readImpl()
	{
		_bypass = readS();
	}
	
	@Override
	protected void runImpl()
	{
		GameClient client = getClient();
		Player activeChar = client.getActiveChar();
		try
		{
			if(!_bypass.isEmpty())
			{
				bp = client.decodeBypass(_bypass);
			}
			if(bp == null)
			{
				return;
			}
			if(activeChar == null)
			{
				if(Config.USE_SECOND_PASSWORD_AUTH && bp.bypass.startsWith("spa_"))
				{
					client.getSecondPasswordAuth().getUI().handle(client, bp.bypass.substring(4));
				}
				return;
			}
			NpcInstance npc = activeChar.getLastNpc();
			GameObject target = activeChar.getTarget();
			if(npc == null && target != null && target.isNpc())
			{
				npc = (NpcInstance) target;
			}
			if(bp.bypass.startsWith("admin_"))
			{
				AdminCommandHandler.getInstance().useAdminCommandHandler(activeChar, bp.bypass);
			}
			else if(bp.bypass.equals("come_here") && activeChar.isGM())
			{
				comeHere(getClient());
			}
			else if(bp.bypass.startsWith("player_help "))
			{
				playerHelp(activeChar, bp.bypass.substring(12));
			}
			else if(bp.bypass.startsWith("scripts_"))
			{
				String command = bp.bypass.substring(8).trim();
				String[] word = command.split("\\s+");
				String[] args = command.substring(word[0].length()).trim().split("\\s+");
				String[] path = word[0].split(":");
				if(path.length != 2)
				{
					_log.warn("Bad Script bypass!");
					return;
				}
				HashMap<String, Object> variables = null;
				if(npc != null)
				{
					variables = new HashMap<>(1);
					variables.put("npc", npc.getRef());
				}
				if(word.length == 1)
				{
					Scripts.getInstance().callScripts(activeChar, path[0], path[1], variables);
				}
				else
				{
					Scripts.getInstance().callScripts(activeChar, path[0], path[1], new Object[] {args}, variables);
				}
			}
			else if(bp.bypass.startsWith("user_"))
			{
				String command = bp.bypass.substring(5).trim();
				String word = command.split("\\s+")[0];
				IVoicedCommandHandler vch = VoicedCommandHandler.getInstance().getVoicedCommandHandler(word);
				if(vch != null)
				{
					String args = command.substring(word.length()).trim();
					vch.useVoicedCommand(word, activeChar, args);
				}
				else
				{
					_log.warn("Unknown voiced command '" + word + "'");
				}
			}
			else if(bp.bypass.startsWith("npc_"))
			{
				int endOfId = bp.bypass.indexOf(95, 5);
				String id = endOfId > 0 ? bp.bypass.substring(4, endOfId) : bp.bypass.substring(4);
				GameObject object = activeChar.getVisibleObject(Integer.parseInt(id));
				if(object != null && object.isNpc() && endOfId > 0 && object.isInActingRange(activeChar))
				{
					activeChar.setLastNpc((NpcInstance) object);
					((NpcInstance) object).onBypassFeedback(activeChar, bp.bypass.substring(endOfId + 1));
				}
			}
			else if(bp.bypass.startsWith("_olympiad?command=move_op_field&field="))
			{
				if(!Config.OLY_SPECTATION_ALLOWED)
				{
					return;
				}
				try
				{
					int stadium_id = Integer.parseInt(bp.bypass.substring(38));
					CompetitionController.getInstance().watchCompetition(activeChar, stadium_id);
				}
				catch(Exception e)
				{
					_log.warn("OlyObserver", e);
					e.printStackTrace();
				}
			}
			else if(bp.bypass.startsWith("_diary"))
			{
				String params = bp.bypass.substring(bp.bypass.indexOf("?") + 1);
				StringTokenizer st = new StringTokenizer(params, "&");
				int heroclass = Integer.parseInt(st.nextToken().split("=")[1]);
				int heropage = Integer.parseInt(st.nextToken().split("=")[1]);
				HeroController.getInstance().showHeroDiary(activeChar, heroclass, heropage);
			}
			else if(bp.bypass.startsWith("_match"))
			{
				String params = bp.bypass.substring(bp.bypass.indexOf("?") + 1);
				StringTokenizer st = new StringTokenizer(params, "&");
				int heroclass = Integer.parseInt(st.nextToken().split("=")[1]);
				int heropage = Integer.parseInt(st.nextToken().split("=")[1]);
				HeroController.getInstance().showHistory(activeChar, heroclass, heropage);
			}
			else if(bp.bypass.startsWith("manor_menu_select?"))
			{
				GameObject object = activeChar.getTarget();
				if(object != null && object.isNpc())
				{
					((NpcInstance) object).onBypassFeedback(activeChar, bp.bypass);
				}
			}
			else if(bp.bypass.startsWith("multisell "))
			{
				MultiSellHolder.getInstance().SeparateAndSend(Integer.parseInt(bp.bypass.substring(10)), activeChar, 0.0);
			}
			else if(bp.bypass.startsWith("Quest "))
			{
				String p = bp.bypass.substring(6).trim();
				int idx = p.indexOf(32);
				if(idx < 0)
				{
					activeChar.processQuestEvent(p, "", npc);
				}
				else
				{
					activeChar.processQuestEvent(p.substring(0, idx), p.substring(idx).trim(), npc);
				}
			}
			else if(bp.bbs)
			{
				if(!Config.COMMUNITYBOARD_ENABLED)
				{
					activeChar.sendPacket(new SystemMessage(938));
				}
				else
				{
					ICommunityBoardHandler communityBoardHandler = CommunityBoardManager.getInstance().getCommunityHandler(bp.bypass);
					communityBoardHandler.onBypassCommand(activeChar, bp.bypass);
				}
			}
		}
		catch(Exception e)
		{
			GameObject target;
			String st = "Bad RequestBypassToServer: " + bp.bypass;
			GameObject gameObject = target = activeChar != null ? activeChar.getTarget() : null;
			if(target != null && target.isNpc())
			{
				st = st + " via NPC #" + ((NpcInstance) target).getNpcId();
			}
			_log.error(st, e);
		}
	}
}