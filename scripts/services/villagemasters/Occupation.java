package services.villagemasters;

import l2.gameserver.model.Player;
import l2.gameserver.model.base.ClassId;
import l2.gameserver.model.base.Race;
import l2.gameserver.model.instances.NpcInstance;
import l2.gameserver.model.instances.VillageMasterInstance;
import l2.gameserver.scripts.Functions;

public class Occupation extends Functions
{
	public void onTalk30026()
	{
		Player pl = getSelf();
		NpcInstance npc = getNpc();
		if(pl == null || npc == null)
		{
			return;
		}
		if(!(npc instanceof VillageMasterInstance))
		{
			show("I have nothing to say you", pl, npc, (Object[]) new Object[0]);
			return;
		}
		ClassId classId = pl.getClassId();
		String htmltext = classId == ClassId.fighter ? "bitz003h.htm" : classId == ClassId.warrior || classId == ClassId.knight || classId == ClassId.rogue ? "bitz004.htm" : classId == ClassId.warlord || classId == ClassId.paladin || classId == ClassId.treasureHunter ? "bitz005.htm" : classId == ClassId.gladiator || classId == ClassId.darkAvenger || classId == ClassId.hawkeye ? "bitz005.htm" : "bitz002.htm";
		npc.showChatWindow(pl, "villagemaster/30026/" + htmltext);
	}
	
	public void onTalk30031()
	{
		Player pl = getSelf();
		NpcInstance npc = getNpc();
		if(pl == null || npc == null)
		{
			return;
		}
		if(!(npc instanceof VillageMasterInstance))
		{
			show("I have nothing to say you", pl, npc, (Object[]) new Object[0]);
			return;
		}
		ClassId classId = pl.getClassId();
		String htmltext = classId == ClassId.wizard || classId == ClassId.cleric ? "06.htm" : classId == ClassId.sorceror || classId == ClassId.necromancer || classId == ClassId.warlock || classId == ClassId.bishop || classId == ClassId.prophet ? "07.htm" : classId == ClassId.mage ? "01.htm" : "08.htm";
		npc.showChatWindow(pl, "villagemaster/30031/" + htmltext);
	}
	
	public void onTalk30037()
	{
		Player pl = getSelf();
		NpcInstance npc = getNpc();
		if(pl == null || npc == null)
		{
			return;
		}
		if(!(npc instanceof VillageMasterInstance))
		{
			show("I have nothing to say you", pl, npc, (Object[]) new Object[0]);
			return;
		}
		ClassId classId = pl.getClassId();
		String htmltext = classId == ClassId.elvenMage ? "01.htm" : classId == ClassId.mage ? "08.htm" : classId == ClassId.wizard || classId == ClassId.cleric || classId == ClassId.elvenWizard || classId == ClassId.oracle ? "31.htm" : classId == ClassId.sorceror || classId == ClassId.necromancer || classId == ClassId.bishop || classId == ClassId.warlock || classId == ClassId.prophet ? "32.htm" : classId == ClassId.spellsinger || classId == ClassId.elder || classId == ClassId.elementalSummoner ? "32.htm" : "33.htm";
		npc.showChatWindow(pl, "villagemaster/30037/" + htmltext);
	}
	
	public void onChange30037(String[] args)
	{
		Player pl = getSelf();
		NpcInstance npc = getNpc();
		if(pl == null || npc == null)
		{
			return;
		}
		if(!(npc instanceof VillageMasterInstance))
		{
			show("I have nothing to say you", pl, npc, (Object[]) new Object[0]);
			return;
		}
		int classid = Integer.parseInt(args[0]);
		int Level = pl.getLevel();
		String htmltext = "33.htm";
		if(classid == 26 && pl.getClassId() == ClassId.elvenMage)
		{
			int ETERNITY_DIAMOND_ID = 1230;
			if(Level <= 19 && pl.getInventory().getItemByItemId(ETERNITY_DIAMOND_ID) == null)
			{
				htmltext = "15.htm";
			}
			else if(Level <= 19 && pl.getInventory().getItemByItemId(ETERNITY_DIAMOND_ID) != null)
			{
				htmltext = "16.htm";
			}
			if(Level >= 20 && pl.getInventory().getItemByItemId(ETERNITY_DIAMOND_ID) == null)
			{
				htmltext = "17.htm";
			}
			if(Level >= 20 && pl.getInventory().getItemByItemId(ETERNITY_DIAMOND_ID) != null)
			{
				pl.getInventory().destroyItemByItemId(ETERNITY_DIAMOND_ID, 1);
				pl.setClassId(26, false, true);
				htmltext = "18.htm";
			}
		}
		else if(classid == 29 && pl.getClassId() == ClassId.elvenMage)
		{
			int LEAF_OF_ORACLE_ID = 1235;
			if(Level <= 19 && pl.getInventory().getItemByItemId(LEAF_OF_ORACLE_ID) == null)
			{
				htmltext = "19.htm";
			}
			if(Level <= 19 && pl.getInventory().getItemByItemId(LEAF_OF_ORACLE_ID) != null)
			{
				htmltext = "20.htm";
			}
			if(Level >= 20 && pl.getInventory().getItemByItemId(LEAF_OF_ORACLE_ID) == null)
			{
				htmltext = "21.htm";
			}
			if(Level >= 20 && pl.getInventory().getItemByItemId(LEAF_OF_ORACLE_ID) != null)
			{
				pl.getInventory().destroyItemByItemId(LEAF_OF_ORACLE_ID, 1);
				pl.setClassId(29, false, true);
				htmltext = "22.htm";
			}
		}
		else if(classid == 11 && pl.getClassId() == ClassId.mage)
		{
			int BEAD_OF_SEASON_ID = 1292;
			if(Level <= 19 && pl.getInventory().getItemByItemId(BEAD_OF_SEASON_ID) == null)
			{
				htmltext = "23.htm";
			}
			if(Level <= 19 && pl.getInventory().getItemByItemId(BEAD_OF_SEASON_ID) != null)
			{
				htmltext = "24.htm";
			}
			if(Level >= 20 && pl.getInventory().getItemByItemId(BEAD_OF_SEASON_ID) == null)
			{
				htmltext = "25.htm";
			}
			if(Level >= 20 && pl.getInventory().getItemByItemId(BEAD_OF_SEASON_ID) != null)
			{
				pl.getInventory().destroyItemByItemId(BEAD_OF_SEASON_ID, 1);
				pl.setClassId(11, false, true);
				htmltext = "26.htm";
			}
		}
		else if(classid == 15 && pl.getClassId() == ClassId.mage)
		{
			int MARK_OF_FAITH_ID = 1201;
			if(Level <= 19 && pl.getInventory().getItemByItemId(MARK_OF_FAITH_ID) == null)
			{
				htmltext = "27.htm";
			}
			if(Level <= 19 && pl.getInventory().getItemByItemId(MARK_OF_FAITH_ID) != null)
			{
				htmltext = "28.htm";
			}
			if(Level >= 20 && pl.getInventory().getItemByItemId(MARK_OF_FAITH_ID) == null)
			{
				htmltext = "29.htm";
			}
			if(Level >= 20 && pl.getInventory().getItemByItemId(MARK_OF_FAITH_ID) != null)
			{
				pl.getInventory().destroyItemByItemId(MARK_OF_FAITH_ID, 1);
				pl.setClassId(15, false, true);
				htmltext = "30.htm";
			}
		}
		npc.showChatWindow(pl, "villagemaster/30037/" + htmltext);
	}
	
	public void onTalk30066()
	{
		Player pl = getSelf();
		NpcInstance npc = getNpc();
		if(pl == null || npc == null)
		{
			return;
		}
		if(!(npc instanceof VillageMasterInstance))
		{
			show("I have nothing to say you", pl, npc, (Object[]) new Object[0]);
			return;
		}
		ClassId classId = pl.getClassId();
		String htmltext = classId == ClassId.elvenFighter ? "01.htm" : classId == ClassId.fighter ? "08.htm" : classId == ClassId.elvenKnight || classId == ClassId.elvenScout || classId == ClassId.warrior || classId == ClassId.knight || classId == ClassId.rogue ? "38.htm" : classId == ClassId.templeKnight || classId == ClassId.plainsWalker || classId == ClassId.swordSinger || classId == ClassId.silverRanger ? "39.htm" : classId == ClassId.warlord || classId == ClassId.paladin || classId == ClassId.treasureHunter ? "39.htm" : classId == ClassId.gladiator || classId == ClassId.darkAvenger || classId == ClassId.hawkeye ? "39.htm" : "40.htm";
		npc.showChatWindow(pl, "villagemaster/30066/" + htmltext);
	}
	
	public void onChange30066(String[] args)
	{
		Player pl = getSelf();
		NpcInstance npc = getNpc();
		if(pl == null || npc == null)
		{
			return;
		}
		if(!(npc instanceof VillageMasterInstance))
		{
			show("I have nothing to say you", pl, npc, (Object[]) new Object[0]);
			return;
		}
		int newclass = Integer.parseInt(args[0]);
		int Level = pl.getLevel();
		ClassId classId = pl.getClassId();
		String htmltext = "No Quest";
		if(newclass == 19 && classId == ClassId.elvenFighter)
		{
			int ELVEN_KNIGHT_BROOCH_ID = 1204;
			if(Level <= 19 && pl.getInventory().getItemByItemId(ELVEN_KNIGHT_BROOCH_ID) == null)
			{
				htmltext = "18.htm";
			}
			if(Level <= 19 && pl.getInventory().getItemByItemId(ELVEN_KNIGHT_BROOCH_ID) != null)
			{
				htmltext = "19.htm";
			}
			if(Level >= 20 && pl.getInventory().getItemByItemId(ELVEN_KNIGHT_BROOCH_ID) == null)
			{
				htmltext = "20.htm";
			}
			if(Level >= 20 && pl.getInventory().getItemByItemId(ELVEN_KNIGHT_BROOCH_ID) != null)
			{
				pl.getInventory().destroyItemByItemId(ELVEN_KNIGHT_BROOCH_ID, 1);
				pl.setClassId(19, false, true);
				htmltext = "21.htm";
			}
		}
		if(newclass == 22 && classId == ClassId.elvenFighter)
		{
			int REORIA_RECOMMENDATION_ID = 1217;
			if(Level <= 19 && pl.getInventory().getItemByItemId(REORIA_RECOMMENDATION_ID) == null)
			{
				htmltext = "22.htm";
			}
			if(Level <= 19 && pl.getInventory().getItemByItemId(REORIA_RECOMMENDATION_ID) != null)
			{
				htmltext = "23.htm";
			}
			if(Level >= 20 && pl.getInventory().getItemByItemId(REORIA_RECOMMENDATION_ID) == null)
			{
				htmltext = "24.htm";
			}
			if(Level >= 20 && pl.getInventory().getItemByItemId(REORIA_RECOMMENDATION_ID) != null)
			{
				pl.getInventory().destroyItemByItemId(REORIA_RECOMMENDATION_ID, 1);
				pl.setClassId(22, false, true);
				htmltext = "25.htm";
			}
		}
		if(newclass == 1 && classId == ClassId.fighter)
		{
			int MEDALLION_OF_WARRIOR_ID = 1145;
			if(Level <= 19 && pl.getInventory().getItemByItemId(MEDALLION_OF_WARRIOR_ID) == null)
			{
				htmltext = "26.htm";
			}
			if(Level <= 19 && pl.getInventory().getItemByItemId(MEDALLION_OF_WARRIOR_ID) != null)
			{
				htmltext = "27.htm";
			}
			if(Level >= 20 && pl.getInventory().getItemByItemId(MEDALLION_OF_WARRIOR_ID) == null)
			{
				htmltext = "28.htm";
			}
			if(Level >= 20 && pl.getInventory().getItemByItemId(MEDALLION_OF_WARRIOR_ID) != null)
			{
				pl.getInventory().destroyItemByItemId(MEDALLION_OF_WARRIOR_ID, 1);
				pl.setClassId(1, false, true);
				htmltext = "29.htm";
			}
		}
		if(newclass == 4 && classId == ClassId.fighter)
		{
			int SWORD_OF_RITUAL_ID = 1161;
			if(Level <= 19 && pl.getInventory().getItemByItemId(SWORD_OF_RITUAL_ID) == null)
			{
				htmltext = "30.htm";
			}
			if(Level <= 19 && pl.getInventory().getItemByItemId(SWORD_OF_RITUAL_ID) != null)
			{
				htmltext = "31.htm";
			}
			if(Level >= 20 && pl.getInventory().getItemByItemId(SWORD_OF_RITUAL_ID) == null)
			{
				htmltext = "32.htm";
			}
			if(Level >= 20 && pl.getInventory().getItemByItemId(SWORD_OF_RITUAL_ID) != null)
			{
				pl.getInventory().destroyItemByItemId(SWORD_OF_RITUAL_ID, 1);
				pl.setClassId(4, false, true);
				htmltext = "33.htm";
			}
		}
		if(newclass == 7 && classId == ClassId.fighter)
		{
			int BEZIQUES_RECOMMENDATION_ID = 1190;
			if(Level <= 19 && pl.getInventory().getItemByItemId(BEZIQUES_RECOMMENDATION_ID) == null)
			{
				htmltext = "34.htm";
			}
			if(Level <= 19 && pl.getInventory().getItemByItemId(BEZIQUES_RECOMMENDATION_ID) != null)
			{
				htmltext = "35.htm";
			}
			if(Level >= 20 && pl.getInventory().getItemByItemId(BEZIQUES_RECOMMENDATION_ID) == null)
			{
				htmltext = "36.htm";
			}
			if(Level >= 20 && pl.getInventory().getItemByItemId(BEZIQUES_RECOMMENDATION_ID) != null)
			{
				pl.getInventory().destroyItemByItemId(BEZIQUES_RECOMMENDATION_ID, 1);
				pl.setClassId(7, false, true);
				htmltext = "37.htm";
			}
		}
		npc.showChatWindow(pl, "villagemaster/30066/" + htmltext);
	}
	
	public void onTalk30511()
	{
		Player pl = getSelf();
		NpcInstance npc = getNpc();
		if(pl == null || npc == null)
		{
			return;
		}
		if(!(npc instanceof VillageMasterInstance))
		{
			show("I have nothing to say you", pl, npc, (Object[]) new Object[0]);
			return;
		}
		ClassId classId = pl.getClassId();
		String htmltext = classId == ClassId.scavenger ? "01.htm" : classId == ClassId.dwarvenFighter ? "09.htm" : classId == ClassId.bountyHunter || classId == ClassId.warsmith ? "10.htm" : "11.htm";
		npc.showChatWindow(pl, "villagemaster/30511/" + htmltext);
	}
	
	public void onChange30511(String[] args)
	{
		Player pl = getSelf();
		NpcInstance npc = getNpc();
		if(pl == null || npc == null)
		{
			return;
		}
		if(!(npc instanceof VillageMasterInstance))
		{
			show("I have nothing to say you", pl, npc, (Object[]) new Object[0]);
			return;
		}
		int newclass = Integer.parseInt(args[0]);
		int Level = pl.getLevel();
		ClassId classId = pl.getClassId();
		String htmltext = "No Quest";
		if(newclass == 55 && classId == ClassId.scavenger)
		{
			int MARK_OF_PROSPERITY_ID = 3238;
			int MARK_OF_GUILDSMAN_ID = 3119;
			int MARK_OF_SEARCHER_ID = 2809;
			if(Level <= 39)
			{
				htmltext = pl.getInventory().getItemByItemId(MARK_OF_SEARCHER_ID) == null || pl.getInventory().getItemByItemId(MARK_OF_GUILDSMAN_ID) == null || pl.getInventory().getItemByItemId(MARK_OF_PROSPERITY_ID) == null ? "05.htm" : "06.htm";
			}
			else if(pl.getInventory().getItemByItemId(MARK_OF_SEARCHER_ID) == null || pl.getInventory().getItemByItemId(MARK_OF_GUILDSMAN_ID) == null || pl.getInventory().getItemByItemId(MARK_OF_PROSPERITY_ID) == null)
			{
				htmltext = "07.htm";
			}
			else
			{
				pl.getInventory().destroyItemByItemId(MARK_OF_SEARCHER_ID, 1);
				pl.getInventory().destroyItemByItemId(MARK_OF_GUILDSMAN_ID, 1);
				pl.getInventory().destroyItemByItemId(MARK_OF_PROSPERITY_ID, 1);
				pl.setClassId(55, false, true);
				htmltext = "08.htm";
			}
		}
		npc.showChatWindow(pl, "villagemaster/30511/" + htmltext);
	}
	
	public void onTalk30070()
	{
		Player pl = getSelf();
		NpcInstance npc = getNpc();
		if(pl == null || npc == null)
		{
			return;
		}
		if(!(npc instanceof VillageMasterInstance))
		{
			show("I have nothing to say you", pl, npc, (Object[]) new Object[0]);
			return;
		}
		ClassId classId = pl.getClassId();
		String htmltext = classId == ClassId.elvenMage ? "01.htm" : classId == ClassId.wizard || classId == ClassId.cleric || classId == ClassId.elvenWizard || classId == ClassId.oracle ? "31.htm" : classId == ClassId.sorceror || classId == ClassId.necromancer || classId == ClassId.bishop || classId == ClassId.warlock || classId == ClassId.prophet || classId == ClassId.spellsinger || classId == ClassId.elder || classId == ClassId.elementalSummoner ? "32.htm" : classId == ClassId.mage ? "08.htm" : "33.htm";
		npc.showChatWindow(pl, "villagemaster/30070/" + htmltext);
	}
	
	public void onChange30070(String[] args)
	{
		Player pl = getSelf();
		NpcInstance npc = getNpc();
		if(pl == null || npc == null)
		{
			return;
		}
		if(!(npc instanceof VillageMasterInstance))
		{
			show("I have nothing to say you", pl, npc, (Object[]) new Object[0]);
			return;
		}
		int event = Integer.parseInt(args[0]);
		int Level = pl.getLevel();
		ClassId classId = pl.getClassId();
		String htmltext = "No Quest";
		if(event == 26 && classId == ClassId.elvenMage)
		{
			int ETERNITY_DIAMOND_ID = 1230;
			if(Level <= 19 && pl.getInventory().getItemByItemId(ETERNITY_DIAMOND_ID) == null)
			{
				htmltext = "15.htm";
			}
			if(Level <= 19 && pl.getInventory().getItemByItemId(ETERNITY_DIAMOND_ID) != null)
			{
				htmltext = "16.htm";
			}
			if(Level >= 20 && pl.getInventory().getItemByItemId(ETERNITY_DIAMOND_ID) == null)
			{
				htmltext = "17.htm";
			}
			if(Level >= 20 && pl.getInventory().getItemByItemId(ETERNITY_DIAMOND_ID) != null)
			{
				pl.getInventory().destroyItemByItemId(ETERNITY_DIAMOND_ID, 1);
				pl.setClassId(26, false, true);
				htmltext = "18.htm";
			}
		}
		else if(event == 29 && classId == ClassId.elvenMage)
		{
			int LEAF_OF_ORACLE_ID = 1235;
			if(Level <= 19 && pl.getInventory().getItemByItemId(LEAF_OF_ORACLE_ID) == null)
			{
				htmltext = "19.htm";
			}
			if(Level <= 19 && pl.getInventory().getItemByItemId(LEAF_OF_ORACLE_ID) != null)
			{
				htmltext = "20.htm";
			}
			if(Level >= 20 && pl.getInventory().getItemByItemId(LEAF_OF_ORACLE_ID) == null)
			{
				htmltext = "21.htm";
			}
			if(Level >= 20 && pl.getInventory().getItemByItemId(LEAF_OF_ORACLE_ID) != null)
			{
				pl.getInventory().destroyItemByItemId(LEAF_OF_ORACLE_ID, 1);
				pl.setClassId(29, false, true);
				htmltext = "22.htm";
			}
		}
		else if(event == 11 && classId == ClassId.mage)
		{
			int BEAD_OF_SEASON_ID = 1292;
			if(Level <= 19 && pl.getInventory().getItemByItemId(BEAD_OF_SEASON_ID) == null)
			{
				htmltext = "23.htm";
			}
			if(Level <= 19 && pl.getInventory().getItemByItemId(BEAD_OF_SEASON_ID) != null)
			{
				htmltext = "24.htm";
			}
			if(Level >= 20 && pl.getInventory().getItemByItemId(BEAD_OF_SEASON_ID) == null)
			{
				htmltext = "25.htm";
			}
			if(Level >= 20 && pl.getInventory().getItemByItemId(BEAD_OF_SEASON_ID) != null)
			{
				pl.getInventory().destroyItemByItemId(BEAD_OF_SEASON_ID, 1);
				pl.setClassId(11, false, true);
				htmltext = "26.htm";
			}
		}
		else if(event == 15 && classId == ClassId.mage)
		{
			int MARK_OF_FAITH_ID = 1201;
			if(Level <= 19 && pl.getInventory().getItemByItemId(MARK_OF_FAITH_ID) == null)
			{
				htmltext = "27.htm";
			}
			if(Level <= 19 && pl.getInventory().getItemByItemId(MARK_OF_FAITH_ID) != null)
			{
				htmltext = "28.htm";
			}
			if(Level >= 20 && pl.getInventory().getItemByItemId(MARK_OF_FAITH_ID) == null)
			{
				htmltext = "29.htm";
			}
			if(Level >= 20 && pl.getInventory().getItemByItemId(MARK_OF_FAITH_ID) != null)
			{
				pl.getInventory().destroyItemByItemId(MARK_OF_FAITH_ID, 1);
				pl.setClassId(15, false, true);
				htmltext = "30.htm";
			}
		}
		npc.showChatWindow(pl, "villagemaster/30070/" + htmltext);
	}
	
	public void onTalk30154()
	{
		Player pl = getSelf();
		NpcInstance npc = getNpc();
		if(pl == null || npc == null)
		{
			return;
		}
		if(!(npc instanceof VillageMasterInstance))
		{
			show("I have nothing to say you", pl, npc, (Object[]) new Object[0]);
			return;
		}
		ClassId classId = pl.getClassId();
		String htmltext = classId == ClassId.elvenFighter ? "01.htm" : classId == ClassId.elvenMage ? "02.htm" : classId == ClassId.elvenWizard || classId == ClassId.oracle || classId == ClassId.elvenKnight || classId == ClassId.elvenScout ? "12.htm" : pl.getRace() == Race.elf ? "13.htm" : "11.htm";
		npc.showChatWindow(pl, "villagemaster/30154/" + htmltext);
	}
	
	public void onTalk30358()
	{
		Player pl = getSelf();
		NpcInstance npc = getNpc();
		if(pl == null || npc == null)
		{
			return;
		}
		if(!(npc instanceof VillageMasterInstance))
		{
			show("I have nothing to say you", pl, npc, (Object[]) new Object[0]);
			return;
		}
		ClassId classId = pl.getClassId();
		String htmltext = classId == ClassId.darkFighter ? "01.htm" : classId == ClassId.darkMage ? "02.htm" : classId == ClassId.darkWizard || classId == ClassId.shillienOracle || classId == ClassId.palusKnight || classId == ClassId.assassin ? "12.htm" : pl.getRace() == Race.darkelf ? "13.htm" : "11.htm";
		npc.showChatWindow(pl, "villagemaster/30358/" + htmltext);
	}
	
	public void onTalk30498()
	{
		Player pl = getSelf();
		NpcInstance npc = getNpc();
		if(pl == null || npc == null)
		{
			return;
		}
		if(!(npc instanceof VillageMasterInstance))
		{
			show("I have nothing to say you", pl, npc, (Object[]) new Object[0]);
			return;
		}
		ClassId classId = pl.getClassId();
		String htmltext = classId == ClassId.dwarvenFighter ? "01.htm" : classId == ClassId.scavenger || classId == ClassId.artisan ? "09.htm" : pl.getRace() == Race.dwarf ? "10.htm" : "11.htm";
		npc.showChatWindow(pl, "villagemaster/30498/" + htmltext);
	}
	
	public void onChange30498(String[] args)
	{
		Player pl = getSelf();
		NpcInstance npc = getNpc();
		if(pl == null || npc == null)
		{
			return;
		}
		if(!(npc instanceof VillageMasterInstance))
		{
			show("I have nothing to say you", pl, npc, (Object[]) new Object[0]);
			return;
		}
		int event = Integer.parseInt(args[0]);
		int Level = pl.getLevel();
		ClassId classId = pl.getClassId();
		String htmltext = "No Quest";
		if(event == 54 && classId == ClassId.dwarvenFighter)
		{
			int RING_OF_RAVEN_ID = 1642;
			if(Level <= 19 && pl.getInventory().getItemByItemId(RING_OF_RAVEN_ID) == null)
			{
				htmltext = "05.htm";
			}
			if(Level <= 19 && pl.getInventory().getItemByItemId(RING_OF_RAVEN_ID) != null)
			{
				htmltext = "06.htm";
			}
			if(Level >= 20 && pl.getInventory().getItemByItemId(RING_OF_RAVEN_ID) == null)
			{
				htmltext = "07.htm";
			}
			if(Level >= 20 && pl.getInventory().getItemByItemId(RING_OF_RAVEN_ID) != null)
			{
				pl.getInventory().destroyItemByItemId(RING_OF_RAVEN_ID, 1);
				pl.setClassId(54, false, true);
				htmltext = "08.htm";
			}
		}
		npc.showChatWindow(pl, "villagemaster/30498/" + htmltext);
	}
	
	public void onTalk30499()
	{
		Player pl = getSelf();
		NpcInstance npc = getNpc();
		if(pl == null || npc == null)
		{
			return;
		}
		if(!(npc instanceof VillageMasterInstance))
		{
			show("I have nothing to say you", pl, npc, (Object[]) new Object[0]);
			return;
		}
		ClassId classId = pl.getClassId();
		String htmltext = classId == ClassId.dwarvenFighter ? "01.htm" : classId == ClassId.scavenger || classId == ClassId.artisan ? "09.htm" : pl.getRace() == Race.dwarf ? "10.htm" : "11.htm";
		npc.showChatWindow(pl, "villagemaster/30499/" + htmltext);
	}
	
	public void onChange30499(String[] args)
	{
		Player pl = getSelf();
		NpcInstance npc = getNpc();
		if(pl == null || npc == null)
		{
			return;
		}
		if(!(npc instanceof VillageMasterInstance))
		{
			show("I have nothing to say you", pl, npc, (Object[]) new Object[0]);
			return;
		}
		int event = Integer.parseInt(args[0]);
		int Level = pl.getLevel();
		ClassId classId = pl.getClassId();
		String htmltext = "No Quest";
		if(event == 56 && classId == ClassId.dwarvenFighter)
		{
			int PASS_FINAL_ID = 1635;
			if(Level <= 19 && pl.getInventory().getItemByItemId(PASS_FINAL_ID) == null)
			{
				htmltext = "05.htm";
			}
			if(Level <= 19 && pl.getInventory().getItemByItemId(PASS_FINAL_ID) != null)
			{
				htmltext = "06.htm";
			}
			if(Level >= 20 && pl.getInventory().getItemByItemId(PASS_FINAL_ID) == null)
			{
				htmltext = "07.htm";
			}
			if(Level >= 20 && pl.getInventory().getItemByItemId(PASS_FINAL_ID) != null)
			{
				pl.getInventory().destroyItemByItemId(PASS_FINAL_ID, 1);
				pl.setClassId(56, false, true);
				htmltext = "08.htm";
			}
		}
		npc.showChatWindow(pl, "villagemaster/30499/" + htmltext);
	}
	
	public void onTalk30525()
	{
		Player pl = getSelf();
		NpcInstance npc = getNpc();
		if(pl == null || npc == null)
		{
			return;
		}
		if(!(npc instanceof VillageMasterInstance))
		{
			show("I have nothing to say you", pl, npc, (Object[]) new Object[0]);
			return;
		}
		ClassId classId = pl.getClassId();
		String htmltext = classId == ClassId.dwarvenFighter ? "01.htm" : classId == ClassId.artisan ? "05.htm" : classId == ClassId.warsmith ? "06.htm" : "07.htm";
		npc.showChatWindow(pl, "villagemaster/30525/" + htmltext);
	}
	
	public void onTalk30520()
	{
		Player pl = getSelf();
		NpcInstance npc = getNpc();
		if(pl == null || npc == null)
		{
			return;
		}
		if(!(npc instanceof VillageMasterInstance))
		{
			show("I have nothing to say you", pl, npc, (Object[]) new Object[0]);
			return;
		}
		ClassId classId = pl.getClassId();
		String htmltext = classId == ClassId.dwarvenFighter ? "01.htm" : classId == ClassId.artisan || classId == ClassId.scavenger ? "05.htm" : classId == ClassId.warsmith || classId == ClassId.bountyHunter ? "06.htm" : "07.htm";
		npc.showChatWindow(pl, "villagemaster/30520/" + htmltext);
	}
	
	public void onTalk30512()
	{
		Player pl = getSelf();
		NpcInstance npc = getNpc();
		if(pl == null || npc == null)
		{
			return;
		}
		if(!(npc instanceof VillageMasterInstance))
		{
			show("I have nothing to say you", pl, npc, (Object[]) new Object[0]);
			return;
		}
		ClassId classId = pl.getClassId();
		String htmltext = classId == ClassId.artisan ? "01.htm" : classId == ClassId.dwarvenFighter ? "09.htm" : classId == ClassId.warsmith || classId == ClassId.bountyHunter ? "10.htm" : "11.htm";
		npc.showChatWindow(pl, "villagemaster/30512/" + htmltext);
	}
	
	public void onChange30512(String[] args)
	{
		Player pl = getSelf();
		NpcInstance npc = getNpc();
		if(pl == null || npc == null)
		{
			return;
		}
		if(!(npc instanceof VillageMasterInstance))
		{
			show("I have nothing to say you", pl, npc, (Object[]) new Object[0]);
			return;
		}
		int event = Integer.parseInt(args[0]);
		int Level = pl.getLevel();
		ClassId classId = pl.getClassId();
		String htmltext = "No Quest";
		if(event == 57 && classId == ClassId.artisan)
		{
			int MARK_OF_PROSPERITY_ID = 3238;
			int MARK_OF_GUILDSMAN_ID = 3119;
			int MARK_OF_MAESTRO_ID = 2867;
			if(Level <= 39)
			{
				htmltext = pl.getInventory().getItemByItemId(MARK_OF_MAESTRO_ID) == null || pl.getInventory().getItemByItemId(MARK_OF_GUILDSMAN_ID) == null || pl.getInventory().getItemByItemId(MARK_OF_PROSPERITY_ID) == null ? "05.htm" : "06.htm";
			}
			else if(pl.getInventory().getItemByItemId(MARK_OF_MAESTRO_ID) == null || pl.getInventory().getItemByItemId(MARK_OF_GUILDSMAN_ID) == null || pl.getInventory().getItemByItemId(MARK_OF_PROSPERITY_ID) == null)
			{
				htmltext = "07.htm";
			}
			else
			{
				pl.getInventory().destroyItemByItemId(MARK_OF_GUILDSMAN_ID, 1);
				pl.getInventory().destroyItemByItemId(MARK_OF_MAESTRO_ID, 1);
				pl.getInventory().destroyItemByItemId(MARK_OF_PROSPERITY_ID, 1);
				pl.setClassId(57, false, true);
				htmltext = "08.htm";
			}
		}
		npc.showChatWindow(pl, "villagemaster/30512/" + htmltext);
	}
	
	public void onTalk30565()
	{
		Player pl = getSelf();
		NpcInstance npc = getNpc();
		if(pl == null || npc == null)
		{
			return;
		}
		if(!(npc instanceof VillageMasterInstance))
		{
			show("I have nothing to say you", pl, npc, (Object[]) new Object[0]);
			return;
		}
		ClassId classId = pl.getClassId();
		String htmltext = classId == ClassId.orcFighter ? "01.htm" : classId == ClassId.orcRaider || classId == ClassId.orcMonk || classId == ClassId.orcShaman ? "09.htm" : classId == ClassId.orcMage ? "16.htm" : pl.getRace() == Race.orc ? "10.htm" : "11.htm";
		npc.showChatWindow(pl, "villagemaster/30565/" + htmltext);
	}
	
	public void onTalk30109()
	{
		Player pl = getSelf();
		NpcInstance npc = getNpc();
		if(pl == null || npc == null)
		{
			return;
		}
		if(!(npc instanceof VillageMasterInstance))
		{
			show("I have nothing to say you", pl, npc, (Object[]) new Object[0]);
			return;
		}
		ClassId classId = pl.getClassId();
		String htmltext = classId == ClassId.elvenKnight ? "01.htm" : classId == ClassId.knight ? "08.htm" : classId == ClassId.rogue ? "15.htm" : classId == ClassId.elvenScout ? "22.htm" : classId == ClassId.warrior ? "29.htm" : classId == ClassId.elvenFighter || classId == ClassId.fighter ? "76.htm" : classId == ClassId.templeKnight || classId == ClassId.plainsWalker || classId == ClassId.swordSinger || classId == ClassId.silverRanger ? "77.htm" : classId == ClassId.warlord || classId == ClassId.paladin || classId == ClassId.treasureHunter ? "77.htm" : classId == ClassId.gladiator || classId == ClassId.darkAvenger || classId == ClassId.hawkeye ? "77.htm" : "78.htm";
		npc.showChatWindow(pl, "villagemaster/30109/" + htmltext);
	}
	
	public void onChange30109(String[] args)
	{
		Player pl = getSelf();
		NpcInstance npc = getNpc();
		if(pl == null || npc == null)
		{
			return;
		}
		if(!(npc instanceof VillageMasterInstance))
		{
			show("I have nothing to say you", pl, npc, (Object[]) new Object[0]);
			return;
		}
		int MARK_OF_CHALLENGER_ID = 2627;
		int MARK_OF_DUTY_ID = 2633;
		int MARK_OF_SEEKER_ID = 2673;
		int MARK_OF_TRUST_ID = 2734;
		int MARK_OF_DUELIST_ID = 2762;
		int MARK_OF_SEARCHER_ID = 2809;
		int MARK_OF_HEALER_ID = 2820;
		int MARK_OF_LIFE_ID = 3140;
		int MARK_OF_SAGITTARIUS_ID = 3293;
		int event = Integer.parseInt(args[0]);
		int Level = pl.getLevel();
		ClassId classId = pl.getClassId();
		String htmltext = "No Quest";
		if(event == 20 && classId == ClassId.elvenKnight)
		{
			if(Level <= 39)
			{
				htmltext = pl.getInventory().getItemByItemId(MARK_OF_DUTY_ID) == null || pl.getInventory().getItemByItemId(MARK_OF_LIFE_ID) == null || pl.getInventory().getItemByItemId(MARK_OF_HEALER_ID) == null ? "36.htm" : "37.htm";
			}
			else if(pl.getInventory().getItemByItemId(MARK_OF_DUTY_ID) == null || pl.getInventory().getItemByItemId(MARK_OF_LIFE_ID) == null || pl.getInventory().getItemByItemId(MARK_OF_HEALER_ID) == null)
			{
				htmltext = "38.htm";
			}
			else
			{
				pl.getInventory().destroyItemByItemId(MARK_OF_DUTY_ID, 1);
				pl.getInventory().destroyItemByItemId(MARK_OF_LIFE_ID, 1);
				pl.getInventory().destroyItemByItemId(MARK_OF_HEALER_ID, 1);
				pl.setClassId(20, false, true);
				htmltext = "39.htm";
			}
		}
		else if(event == 21 && classId == ClassId.elvenKnight)
		{
			if(Level <= 39)
			{
				htmltext = pl.getInventory().getItemByItemId(MARK_OF_CHALLENGER_ID) == null || pl.getInventory().getItemByItemId(MARK_OF_LIFE_ID) == null || pl.getInventory().getItemByItemId(MARK_OF_DUELIST_ID) == null ? "40.htm" : "41.htm";
			}
			else if(pl.getInventory().getItemByItemId(MARK_OF_CHALLENGER_ID) == null || pl.getInventory().getItemByItemId(MARK_OF_LIFE_ID) == null || pl.getInventory().getItemByItemId(MARK_OF_DUELIST_ID) == null)
			{
				htmltext = "42.htm";
			}
			else
			{
				pl.getInventory().destroyItemByItemId(MARK_OF_CHALLENGER_ID, 1);
				pl.getInventory().destroyItemByItemId(MARK_OF_LIFE_ID, 1);
				pl.getInventory().destroyItemByItemId(MARK_OF_DUELIST_ID, 1);
				pl.setClassId(21, false, true);
				htmltext = "43.htm";
			}
		}
		else if(event == 5 && classId == ClassId.knight)
		{
			if(Level <= 39)
			{
				htmltext = pl.getInventory().getItemByItemId(MARK_OF_DUTY_ID) == null || pl.getInventory().getItemByItemId(MARK_OF_TRUST_ID) == null || pl.getInventory().getItemByItemId(MARK_OF_HEALER_ID) == null ? "44.htm" : "45.htm";
			}
			else if(pl.getInventory().getItemByItemId(MARK_OF_DUTY_ID) == null || pl.getInventory().getItemByItemId(MARK_OF_TRUST_ID) == null || pl.getInventory().getItemByItemId(MARK_OF_HEALER_ID) == null)
			{
				htmltext = "46.htm";
			}
			else
			{
				pl.getInventory().destroyItemByItemId(MARK_OF_DUTY_ID, 1);
				pl.getInventory().destroyItemByItemId(MARK_OF_TRUST_ID, 1);
				pl.getInventory().destroyItemByItemId(MARK_OF_HEALER_ID, 1);
				pl.setClassId(5, false, true);
				htmltext = "47.htm";
			}
		}
		else if(event == 6 && classId == ClassId.knight)
		{
			int MARK_OF_WITCHCRAFT_ID = 3307;
			if(Level <= 39)
			{
				htmltext = pl.getInventory().getItemByItemId(MARK_OF_DUTY_ID) == null || pl.getInventory().getItemByItemId(MARK_OF_TRUST_ID) == null || pl.getInventory().getItemByItemId(MARK_OF_WITCHCRAFT_ID) == null ? "48.htm" : "49.htm";
			}
			else if(pl.getInventory().getItemByItemId(MARK_OF_DUTY_ID) == null || pl.getInventory().getItemByItemId(MARK_OF_TRUST_ID) == null || pl.getInventory().getItemByItemId(MARK_OF_WITCHCRAFT_ID) == null)
			{
				htmltext = "50.htm";
			}
			else
			{
				pl.getInventory().destroyItemByItemId(MARK_OF_DUTY_ID, 1);
				pl.getInventory().destroyItemByItemId(MARK_OF_TRUST_ID, 1);
				pl.getInventory().destroyItemByItemId(MARK_OF_WITCHCRAFT_ID, 1);
				pl.setClassId(6, false, true);
				htmltext = "51.htm";
			}
		}
		else if(event == 8 && classId == ClassId.rogue)
		{
			if(Level <= 39)
			{
				htmltext = pl.getInventory().getItemByItemId(MARK_OF_SEEKER_ID) == null || pl.getInventory().getItemByItemId(MARK_OF_TRUST_ID) == null || pl.getInventory().getItemByItemId(MARK_OF_SEARCHER_ID) == null ? "52.htm" : "53.htm";
			}
			else if(pl.getInventory().getItemByItemId(MARK_OF_SEEKER_ID) == null || pl.getInventory().getItemByItemId(MARK_OF_TRUST_ID) == null || pl.getInventory().getItemByItemId(MARK_OF_SEARCHER_ID) == null)
			{
				htmltext = "54.htm";
			}
			else
			{
				pl.getInventory().destroyItemByItemId(MARK_OF_SEEKER_ID, 1);
				pl.getInventory().destroyItemByItemId(MARK_OF_TRUST_ID, 1);
				pl.getInventory().destroyItemByItemId(MARK_OF_SEARCHER_ID, 1);
				pl.setClassId(8, false, true);
				htmltext = "55.htm";
			}
		}
		else if(event == 9 && classId == ClassId.rogue)
		{
			if(Level <= 39)
			{
				htmltext = pl.getInventory().getItemByItemId(MARK_OF_SEEKER_ID) == null || pl.getInventory().getItemByItemId(MARK_OF_TRUST_ID) == null || pl.getInventory().getItemByItemId(MARK_OF_SAGITTARIUS_ID) == null ? "56.htm" : "57.htm";
			}
			else if(pl.getInventory().getItemByItemId(MARK_OF_SEEKER_ID) == null || pl.getInventory().getItemByItemId(MARK_OF_TRUST_ID) == null || pl.getInventory().getItemByItemId(MARK_OF_SAGITTARIUS_ID) == null)
			{
				htmltext = "58.htm";
			}
			else
			{
				pl.getInventory().destroyItemByItemId(MARK_OF_SEEKER_ID, 1);
				pl.getInventory().destroyItemByItemId(MARK_OF_TRUST_ID, 1);
				pl.getInventory().destroyItemByItemId(MARK_OF_SAGITTARIUS_ID, 1);
				pl.setClassId(9, false, true);
				htmltext = "59.htm";
			}
		}
		else if(event == 23 && classId == ClassId.elvenScout)
		{
			if(Level <= 39)
			{
				htmltext = pl.getInventory().getItemByItemId(MARK_OF_SEEKER_ID) == null || pl.getInventory().getItemByItemId(MARK_OF_LIFE_ID) == null || pl.getInventory().getItemByItemId(MARK_OF_SEARCHER_ID) == null ? "60.htm" : "61.htm";
			}
			else if(pl.getInventory().getItemByItemId(MARK_OF_SEEKER_ID) == null || pl.getInventory().getItemByItemId(MARK_OF_LIFE_ID) == null || pl.getInventory().getItemByItemId(MARK_OF_SEARCHER_ID) == null)
			{
				htmltext = "62.htm";
			}
			else
			{
				pl.getInventory().destroyItemByItemId(MARK_OF_SEEKER_ID, 1);
				pl.getInventory().destroyItemByItemId(MARK_OF_LIFE_ID, 1);
				pl.getInventory().destroyItemByItemId(MARK_OF_SEARCHER_ID, 1);
				pl.setClassId(23, false, true);
				htmltext = "63.htm";
			}
		}
		else if(event == 24 && classId == ClassId.elvenScout)
		{
			if(Level <= 39)
			{
				htmltext = pl.getInventory().getItemByItemId(MARK_OF_SEEKER_ID) == null || pl.getInventory().getItemByItemId(MARK_OF_LIFE_ID) == null || pl.getInventory().getItemByItemId(MARK_OF_SAGITTARIUS_ID) == null ? "64.htm" : "65.htm";
			}
			else if(pl.getInventory().getItemByItemId(MARK_OF_SEEKER_ID) == null || pl.getInventory().getItemByItemId(MARK_OF_LIFE_ID) == null || pl.getInventory().getItemByItemId(MARK_OF_SAGITTARIUS_ID) == null)
			{
				htmltext = "66.htm";
			}
			else
			{
				pl.getInventory().destroyItemByItemId(MARK_OF_SEEKER_ID, 1);
				pl.getInventory().destroyItemByItemId(MARK_OF_LIFE_ID, 1);
				pl.getInventory().destroyItemByItemId(MARK_OF_SAGITTARIUS_ID, 1);
				pl.setClassId(24, false, true);
				htmltext = "67.htm";
			}
		}
		else if(event == 2 && classId == ClassId.warrior)
		{
			if(Level <= 39)
			{
				htmltext = pl.getInventory().getItemByItemId(MARK_OF_CHALLENGER_ID) == null || pl.getInventory().getItemByItemId(MARK_OF_TRUST_ID) == null || pl.getInventory().getItemByItemId(MARK_OF_DUELIST_ID) == null ? "68.htm" : "69.htm";
			}
			else if(pl.getInventory().getItemByItemId(MARK_OF_CHALLENGER_ID) == null || pl.getInventory().getItemByItemId(MARK_OF_TRUST_ID) == null || pl.getInventory().getItemByItemId(MARK_OF_DUELIST_ID) == null)
			{
				htmltext = "70.htm";
			}
			else
			{
				pl.getInventory().destroyItemByItemId(MARK_OF_CHALLENGER_ID, 1);
				pl.getInventory().destroyItemByItemId(MARK_OF_TRUST_ID, 1);
				pl.getInventory().destroyItemByItemId(MARK_OF_DUELIST_ID, 1);
				pl.setClassId(2, false, true);
				htmltext = "71.htm";
			}
		}
		else if(event == 3 && classId == ClassId.warrior)
		{
			int MARK_OF_CHAMPION_ID = 3276;
			if(Level <= 39)
			{
				htmltext = pl.getInventory().getItemByItemId(MARK_OF_CHALLENGER_ID) == null || pl.getInventory().getItemByItemId(MARK_OF_TRUST_ID) == null || pl.getInventory().getItemByItemId(MARK_OF_CHAMPION_ID) == null ? "72.htm" : "73.htm";
			}
			else if(pl.getInventory().getItemByItemId(MARK_OF_CHALLENGER_ID) == null || pl.getInventory().getItemByItemId(MARK_OF_TRUST_ID) == null || pl.getInventory().getItemByItemId(MARK_OF_CHAMPION_ID) == null)
			{
				htmltext = "74.htm";
			}
			else
			{
				pl.getInventory().destroyItemByItemId(MARK_OF_CHALLENGER_ID, 1);
				pl.getInventory().destroyItemByItemId(MARK_OF_TRUST_ID, 1);
				pl.getInventory().destroyItemByItemId(MARK_OF_CHAMPION_ID, 1);
				pl.setClassId(3, false, true);
				htmltext = "75.htm";
			}
		}
		npc.showChatWindow(pl, "villagemaster/30109/" + htmltext);
	}
	
	public void onTalk30115()
	{
		Player pl = getSelf();
		NpcInstance npc = getNpc();
		if(pl == null || npc == null)
		{
			return;
		}
		if(!(npc instanceof VillageMasterInstance))
		{
			show("I have nothing to say you", pl, npc, (Object[]) new Object[0]);
			return;
		}
		ClassId classId = pl.getClassId();
		String htmltext = classId == ClassId.elvenWizard ? "01.htm" : classId == ClassId.wizard ? "08.htm" : classId == ClassId.sorceror || classId == ClassId.necromancer || classId == ClassId.warlock ? "39.htm" : classId == ClassId.spellsinger || classId == ClassId.elementalSummoner ? "39.htm" : (pl.getRace() == Race.elf || pl.getRace() == Race.human) && classId.isMage() ? "38.htm" : "40.htm";
		npc.showChatWindow(pl, "villagemaster/30115/" + htmltext);
	}
	
	public void onChange30115(String[] args)
	{
		Player pl = getSelf();
		NpcInstance npc = getNpc();
		if(pl == null || npc == null)
		{
			return;
		}
		if(!(npc instanceof VillageMasterInstance))
		{
			show("I have nothing to say you", pl, npc, (Object[]) new Object[0]);
			return;
		}
		int MARK_OF_SCHOLAR_ID = 2674;
		int MARK_OF_TRUST_ID = 2734;
		int MARK_OF_MAGUS_ID = 2840;
		int MARK_OF_LIFE_ID = 3140;
		int MARK_OF_SUMMONER_ID = 3336;
		int event = Integer.parseInt(args[0]);
		int Level = pl.getLevel();
		ClassId classId = pl.getClassId();
		String htmltext = "No Quest";
		if(event == 27 && classId == ClassId.elvenWizard)
		{
			if(Level <= 39)
			{
				htmltext = pl.getInventory().getItemByItemId(MARK_OF_SCHOLAR_ID) == null || pl.getInventory().getItemByItemId(MARK_OF_LIFE_ID) == null || pl.getInventory().getItemByItemId(MARK_OF_MAGUS_ID) == null ? "18.htm" : "19.htm";
			}
			else if(pl.getInventory().getItemByItemId(MARK_OF_SCHOLAR_ID) == null || pl.getInventory().getItemByItemId(MARK_OF_LIFE_ID) == null || pl.getInventory().getItemByItemId(MARK_OF_MAGUS_ID) == null)
			{
				htmltext = "20.htm";
			}
			else
			{
				pl.getInventory().destroyItemByItemId(MARK_OF_SCHOLAR_ID, 1);
				pl.getInventory().destroyItemByItemId(MARK_OF_LIFE_ID, 1);
				pl.getInventory().destroyItemByItemId(MARK_OF_MAGUS_ID, 1);
				pl.setClassId(27, false, true);
				htmltext = "21.htm";
			}
		}
		else if(event == 28 && classId == ClassId.elvenWizard)
		{
			if(Level <= 39)
			{
				htmltext = pl.getInventory().getItemByItemId(MARK_OF_SCHOLAR_ID) == null || pl.getInventory().getItemByItemId(MARK_OF_LIFE_ID) == null || pl.getInventory().getItemByItemId(MARK_OF_SUMMONER_ID) == null ? "22.htm" : "23.htm";
			}
			else if(pl.getInventory().getItemByItemId(MARK_OF_SCHOLAR_ID) == null || pl.getInventory().getItemByItemId(MARK_OF_LIFE_ID) == null || pl.getInventory().getItemByItemId(MARK_OF_SUMMONER_ID) == null)
			{
				htmltext = "24.htm";
			}
			else
			{
				pl.getInventory().destroyItemByItemId(MARK_OF_SCHOLAR_ID, 1);
				pl.getInventory().destroyItemByItemId(MARK_OF_LIFE_ID, 1);
				pl.getInventory().destroyItemByItemId(MARK_OF_SUMMONER_ID, 1);
				pl.setClassId(28, false, true);
				htmltext = "25.htm";
			}
		}
		else if(event == 12 && classId == ClassId.wizard)
		{
			if(Level <= 39)
			{
				htmltext = pl.getInventory().getItemByItemId(MARK_OF_SCHOLAR_ID) == null || pl.getInventory().getItemByItemId(MARK_OF_TRUST_ID) == null || pl.getInventory().getItemByItemId(MARK_OF_MAGUS_ID) == null ? "26.htm" : "27.htm";
			}
			else if(pl.getInventory().getItemByItemId(MARK_OF_SCHOLAR_ID) == null || pl.getInventory().getItemByItemId(MARK_OF_TRUST_ID) == null || pl.getInventory().getItemByItemId(MARK_OF_MAGUS_ID) == null)
			{
				htmltext = "28.htm";
			}
			else
			{
				pl.getInventory().destroyItemByItemId(MARK_OF_SCHOLAR_ID, 1);
				pl.getInventory().destroyItemByItemId(MARK_OF_TRUST_ID, 1);
				pl.getInventory().destroyItemByItemId(MARK_OF_MAGUS_ID, 1);
				pl.setClassId(12, false, true);
				htmltext = "29.htm";
			}
		}
		else if(event == 13 && classId == ClassId.wizard)
		{
			int MARK_OF_WITCHCRFAT_ID = 3307;
			if(Level <= 39)
			{
				htmltext = pl.getInventory().getItemByItemId(MARK_OF_SCHOLAR_ID) == null || pl.getInventory().getItemByItemId(MARK_OF_TRUST_ID) == null || pl.getInventory().getItemByItemId(MARK_OF_WITCHCRFAT_ID) == null ? "30.htm" : "31.htm";
			}
			else if(pl.getInventory().getItemByItemId(MARK_OF_SCHOLAR_ID) == null || pl.getInventory().getItemByItemId(MARK_OF_TRUST_ID) == null || pl.getInventory().getItemByItemId(MARK_OF_WITCHCRFAT_ID) == null)
			{
				htmltext = "32.htm";
			}
			else
			{
				pl.getInventory().destroyItemByItemId(MARK_OF_SCHOLAR_ID, 1);
				pl.getInventory().destroyItemByItemId(MARK_OF_TRUST_ID, 1);
				pl.getInventory().destroyItemByItemId(MARK_OF_WITCHCRFAT_ID, 1);
				pl.setClassId(13, false, true);
				htmltext = "33.htm";
			}
		}
		else if(event == 14 && classId == ClassId.wizard)
		{
			if(Level <= 39)
			{
				htmltext = pl.getInventory().getItemByItemId(MARK_OF_SCHOLAR_ID) == null || pl.getInventory().getItemByItemId(MARK_OF_TRUST_ID) == null || pl.getInventory().getItemByItemId(MARK_OF_SUMMONER_ID) == null ? "34.htm" : "35.htm";
			}
			else if(pl.getInventory().getItemByItemId(MARK_OF_SCHOLAR_ID) == null || pl.getInventory().getItemByItemId(MARK_OF_TRUST_ID) == null || pl.getInventory().getItemByItemId(MARK_OF_SUMMONER_ID) == null)
			{
				htmltext = "36.htm";
			}
			else
			{
				pl.getInventory().destroyItemByItemId(MARK_OF_SCHOLAR_ID, 1);
				pl.getInventory().destroyItemByItemId(MARK_OF_TRUST_ID, 1);
				pl.getInventory().destroyItemByItemId(MARK_OF_SUMMONER_ID, 1);
				pl.setClassId(14, false, true);
				htmltext = "37.htm";
			}
		}
		npc.showChatWindow(pl, "villagemaster/30115/" + htmltext);
	}
	
	public void onTalk30120()
	{
		Player pl = getSelf();
		NpcInstance npc = getNpc();
		if(pl == null || npc == null)
		{
			return;
		}
		if(!(npc instanceof VillageMasterInstance))
		{
			show("I have nothing to say you", pl, npc, (Object[]) new Object[0]);
			return;
		}
		ClassId classId = pl.getClassId();
		String htmltext = classId == ClassId.oracle ? "01.htm" : classId == ClassId.cleric ? "05.htm" : classId == ClassId.elder || classId == ClassId.bishop || classId == ClassId.prophet ? "25.htm" : (pl.getRace() == Race.human || pl.getRace() == Race.elf) && classId.isMage() ? "24.htm" : "26.htm";
		npc.showChatWindow(pl, "villagemaster/30120/" + htmltext);
	}
	
	public void onChange30120(String[] args)
	{
		Player pl = getSelf();
		NpcInstance npc = getNpc();
		if(pl == null || npc == null)
		{
			return;
		}
		if(!(npc instanceof VillageMasterInstance))
		{
			show("I have nothing to say you", pl, npc, (Object[]) new Object[0]);
			return;
		}
		int MARK_OF_PILGRIM_ID = 2721;
		int MARK_OF_TRUST_ID = 2734;
		int MARK_OF_HEALER_ID = 2820;
		int event = Integer.parseInt(args[0]);
		int Level = pl.getLevel();
		ClassId classId = pl.getClassId();
		String htmltext = "No Quest";
		if(event == 30 || classId == ClassId.oracle)
		{
			int MARK_OF_LIFE_ID = 3140;
			if(Level <= 39)
			{
				htmltext = pl.getInventory().getItemByItemId(MARK_OF_PILGRIM_ID) == null || pl.getInventory().getItemByItemId(MARK_OF_LIFE_ID) == null || pl.getInventory().getItemByItemId(MARK_OF_HEALER_ID) == null ? "12.htm" : "13.htm";
			}
			else if(pl.getInventory().getItemByItemId(MARK_OF_PILGRIM_ID) == null || pl.getInventory().getItemByItemId(MARK_OF_LIFE_ID) == null || pl.getInventory().getItemByItemId(MARK_OF_HEALER_ID) == null)
			{
				htmltext = "14.htm";
			}
			else
			{
				pl.getInventory().destroyItemByItemId(MARK_OF_PILGRIM_ID, 1);
				pl.getInventory().destroyItemByItemId(MARK_OF_LIFE_ID, 1);
				pl.getInventory().destroyItemByItemId(MARK_OF_HEALER_ID, 1);
				pl.setClassId(event, false, true);
				htmltext = "15.htm";
			}
		}
		else if(event == 16 && classId == ClassId.cleric)
		{
			if(Level <= 39)
			{
				htmltext = pl.getInventory().getItemByItemId(MARK_OF_PILGRIM_ID) == null || pl.getInventory().getItemByItemId(MARK_OF_TRUST_ID) == null || pl.getInventory().getItemByItemId(MARK_OF_HEALER_ID) == null ? "16.htm" : "17.htm";
			}
			else if(pl.getInventory().getItemByItemId(MARK_OF_PILGRIM_ID) == null || pl.getInventory().getItemByItemId(MARK_OF_TRUST_ID) == null || pl.getInventory().getItemByItemId(MARK_OF_HEALER_ID) == null)
			{
				htmltext = "18.htm";
			}
			else
			{
				pl.getInventory().destroyItemByItemId(MARK_OF_PILGRIM_ID, 1);
				pl.getInventory().destroyItemByItemId(MARK_OF_TRUST_ID, 1);
				pl.getInventory().destroyItemByItemId(MARK_OF_HEALER_ID, 1);
				pl.setClassId(16, false, true);
				htmltext = "19.htm";
			}
		}
		else if(event == 17 && classId == ClassId.cleric)
		{
			int MARK_OF_REFORMER_ID = 2821;
			if(Level <= 39)
			{
				htmltext = pl.getInventory().getItemByItemId(MARK_OF_PILGRIM_ID) == null || pl.getInventory().getItemByItemId(MARK_OF_TRUST_ID) == null || pl.getInventory().getItemByItemId(MARK_OF_REFORMER_ID) == null ? "20.htm" : "21.htm";
			}
			else if(pl.getInventory().getItemByItemId(MARK_OF_PILGRIM_ID) == null || pl.getInventory().getItemByItemId(MARK_OF_TRUST_ID) == null || pl.getInventory().getItemByItemId(MARK_OF_REFORMER_ID) == null)
			{
				htmltext = "22.htm";
			}
			else
			{
				pl.getInventory().destroyItemByItemId(MARK_OF_PILGRIM_ID, 1);
				pl.getInventory().destroyItemByItemId(MARK_OF_TRUST_ID, 1);
				pl.getInventory().destroyItemByItemId(MARK_OF_REFORMER_ID, 1);
				pl.setClassId(17, false, true);
				htmltext = "23.htm";
			}
		}
		npc.showChatWindow(pl, "villagemaster/30120/" + htmltext);
	}
	
	public void onTalk30500()
	{
		Player pl = getSelf();
		NpcInstance npc = getNpc();
		if(pl == null || npc == null)
		{
			return;
		}
		if(!(npc instanceof VillageMasterInstance))
		{
			show("I have nothing to say you", pl, npc, (Object[]) new Object[0]);
			return;
		}
		ClassId classId = pl.getClassId();
		String htmltext = classId == ClassId.orcFighter ? "01.htm" : classId == ClassId.orcMage ? "06.htm" : classId == ClassId.orcRaider || classId == ClassId.orcMonk || classId == ClassId.orcShaman ? "21.htm" : classId == ClassId.destroyer || classId == ClassId.tyrant || classId == ClassId.overlord || classId == ClassId.warcryer ? "22.htm" : "23.htm";
		npc.showChatWindow(pl, "villagemaster/30500/" + htmltext);
	}
	
	public void onChange30500(String[] args)
	{
		Player pl = getSelf();
		NpcInstance npc = getNpc();
		if(pl == null || npc == null)
		{
			return;
		}
		if(!(npc instanceof VillageMasterInstance))
		{
			show("I have nothing to say you", pl, npc, (Object[]) new Object[0]);
			return;
		}
		int event = Integer.parseInt(args[0]);
		int Level = pl.getLevel();
		ClassId classId = pl.getClassId();
		String htmltext = "No Quest";
		if(event == 45 && classId == ClassId.orcFighter)
		{
			int MARK_OF_RAIDER_ID = 1592;
			if(Level <= 19 && pl.getInventory().getItemByItemId(MARK_OF_RAIDER_ID) == null)
			{
				htmltext = "09.htm";
			}
			if(Level <= 19 && pl.getInventory().getItemByItemId(MARK_OF_RAIDER_ID) != null)
			{
				htmltext = "10.htm";
			}
			if(Level >= 20 && pl.getInventory().getItemByItemId(MARK_OF_RAIDER_ID) == null)
			{
				htmltext = "11.htm";
			}
			if(Level >= 20 && pl.getInventory().getItemByItemId(MARK_OF_RAIDER_ID) != null)
			{
				pl.getInventory().destroyItemByItemId(MARK_OF_RAIDER_ID, 1);
				pl.setClassId(45, false, true);
				htmltext = "12.htm";
			}
		}
		else if(event == 47 && classId == ClassId.orcFighter)
		{
			int KHAVATARI_TOTEM_ID = 1615;
			if(Level <= 19 && pl.getInventory().getItemByItemId(KHAVATARI_TOTEM_ID) == null)
			{
				htmltext = "13.htm";
			}
			if(Level <= 19 && pl.getInventory().getItemByItemId(KHAVATARI_TOTEM_ID) != null)
			{
				htmltext = "14.htm";
			}
			if(Level >= 20 && pl.getInventory().getItemByItemId(KHAVATARI_TOTEM_ID) == null)
			{
				htmltext = "15.htm";
			}
			if(Level >= 20 && pl.getInventory().getItemByItemId(KHAVATARI_TOTEM_ID) != null)
			{
				pl.getInventory().destroyItemByItemId(KHAVATARI_TOTEM_ID, 1);
				pl.setClassId(47, false, true);
				htmltext = "16.htm";
			}
		}
		else if(event == 50 && classId == ClassId.orcMage)
		{
			int MASK_OF_MEDIUM_ID = 1631;
			if(Level <= 19 && pl.getInventory().getItemByItemId(MASK_OF_MEDIUM_ID) == null)
			{
				htmltext = "17.htm";
			}
			if(Level <= 19 && pl.getInventory().getItemByItemId(MASK_OF_MEDIUM_ID) != null)
			{
				htmltext = "18.htm";
			}
			if(Level >= 20 && pl.getInventory().getItemByItemId(MASK_OF_MEDIUM_ID) == null)
			{
				htmltext = "19.htm";
			}
			if(Level >= 20 && pl.getInventory().getItemByItemId(MASK_OF_MEDIUM_ID) != null)
			{
				pl.getInventory().destroyItemByItemId(MASK_OF_MEDIUM_ID, 1);
				pl.setClassId(50, false, true);
				htmltext = "20.htm";
			}
		}
		npc.showChatWindow(pl, "villagemaster/30500/" + htmltext);
	}
	
	public void onTalk30290()
	{
		Player pl = getSelf();
		NpcInstance npc = getNpc();
		if(pl == null || npc == null)
		{
			return;
		}
		if(!(npc instanceof VillageMasterInstance))
		{
			show("I have nothing to say you", pl, npc, (Object[]) new Object[0]);
			return;
		}
		ClassId classId = pl.getClassId();
		String htmltext = classId == ClassId.darkFighter ? "01.htm" : classId == ClassId.darkMage ? "08.htm" : classId == ClassId.palusKnight || classId == ClassId.assassin || classId == ClassId.darkWizard || classId == ClassId.shillienOracle ? "31.htm" : pl.getRace() == Race.darkelf ? "32.htm" : "33.htm";
		npc.showChatWindow(pl, "villagemaster/30290/" + htmltext);
	}
	
	public void onChange30290(String[] args)
	{
		Player pl = getSelf();
		NpcInstance npc = getNpc();
		if(pl == null || npc == null)
		{
			return;
		}
		if(!(npc instanceof VillageMasterInstance))
		{
			show("I have nothing to say you", pl, npc, (Object[]) new Object[0]);
			return;
		}
		int event = Integer.parseInt(args[0]);
		int Level = pl.getLevel();
		ClassId classId = pl.getClassId();
		String htmltext = "No Quest";
		if(event == 32 && classId == ClassId.darkFighter)
		{
			int GAZE_OF_ABYSS_ID = 1244;
			if(Level <= 19 && pl.getInventory().getItemByItemId(GAZE_OF_ABYSS_ID) == null)
			{
				htmltext = "15.htm";
			}
			if(Level <= 19 && pl.getInventory().getItemByItemId(GAZE_OF_ABYSS_ID) != null)
			{
				htmltext = "16.htm";
			}
			if(Level >= 20 && pl.getInventory().getItemByItemId(GAZE_OF_ABYSS_ID) == null)
			{
				htmltext = "17.htm";
			}
			if(Level >= 20 && pl.getInventory().getItemByItemId(GAZE_OF_ABYSS_ID) != null)
			{
				pl.getInventory().destroyItemByItemId(GAZE_OF_ABYSS_ID, 1);
				pl.setClassId(32, false, true);
				htmltext = "18.htm";
			}
		}
		else if(event == 35 && classId == ClassId.darkFighter)
		{
			int IRON_HEART_ID = 1252;
			if(Level <= 19 && pl.getInventory().getItemByItemId(IRON_HEART_ID) == null)
			{
				htmltext = "19.htm";
			}
			if(Level <= 19 && pl.getInventory().getItemByItemId(IRON_HEART_ID) != null)
			{
				htmltext = "20.htm";
			}
			if(Level >= 20 && pl.getInventory().getItemByItemId(IRON_HEART_ID) == null)
			{
				htmltext = "21.htm";
			}
			if(Level >= 20 && pl.getInventory().getItemByItemId(IRON_HEART_ID) != null)
			{
				pl.getInventory().destroyItemByItemId(IRON_HEART_ID, 1);
				pl.setClassId(35, false, true);
				htmltext = "22.htm";
			}
		}
		else if(event == 39 && classId == ClassId.darkMage)
		{
			int JEWEL_OF_DARKNESS_ID = 1261;
			if(Level <= 19 && pl.getInventory().getItemByItemId(JEWEL_OF_DARKNESS_ID) == null)
			{
				htmltext = "23.htm";
			}
			if(Level <= 19 && pl.getInventory().getItemByItemId(JEWEL_OF_DARKNESS_ID) != null)
			{
				htmltext = "24.htm";
			}
			if(Level >= 20 && pl.getInventory().getItemByItemId(JEWEL_OF_DARKNESS_ID) == null)
			{
				htmltext = "25.htm";
			}
			if(Level >= 20 && pl.getInventory().getItemByItemId(JEWEL_OF_DARKNESS_ID) != null)
			{
				pl.getInventory().destroyItemByItemId(JEWEL_OF_DARKNESS_ID, 1);
				pl.setClassId(39, false, true);
				htmltext = "26.htm";
			}
		}
		else if(event == 42 && classId == ClassId.darkMage)
		{
			int ORB_OF_ABYSS_ID = 1270;
			if(Level <= 19 && pl.getInventory().getItemByItemId(ORB_OF_ABYSS_ID) == null)
			{
				htmltext = "27.htm";
			}
			if(Level <= 19 && pl.getInventory().getItemByItemId(ORB_OF_ABYSS_ID) != null)
			{
				htmltext = "28.htm";
			}
			if(Level >= 20 && pl.getInventory().getItemByItemId(ORB_OF_ABYSS_ID) == null)
			{
				htmltext = "29.htm";
			}
			if(Level >= 20 && pl.getInventory().getItemByItemId(ORB_OF_ABYSS_ID) != null)
			{
				pl.getInventory().destroyItemByItemId(ORB_OF_ABYSS_ID, 1);
				pl.setClassId(42, false, true);
				htmltext = "30.htm";
			}
		}
		npc.showChatWindow(pl, "villagemaster/30290/" + htmltext);
	}
	
	public void onTalk30513()
	{
		Player pl = getSelf();
		NpcInstance npc = getNpc();
		if(pl == null || npc == null)
		{
			return;
		}
		if(!(npc instanceof VillageMasterInstance))
		{
			show("I have nothing to say you", pl, npc, (Object[]) new Object[0]);
			return;
		}
		ClassId classId = pl.getClassId();
		String htmltext = classId == ClassId.orcMonk ? "01.htm" : classId == ClassId.orcRaider ? "05.htm" : classId == ClassId.orcShaman ? "09.htm" : classId == ClassId.destroyer || classId == ClassId.tyrant || classId == ClassId.overlord || classId == ClassId.warcryer ? "32.htm" : classId == ClassId.orcFighter || classId == ClassId.orcMage ? "33.htm" : "34.htm";
		npc.showChatWindow(pl, "villagemaster/30513/" + htmltext);
	}
	
	public void onChange30513(String[] args)
	{
		Player pl = getSelf();
		NpcInstance npc = getNpc();
		if(pl == null || npc == null)
		{
			return;
		}
		if(!(npc instanceof VillageMasterInstance))
		{
			show("I have nothing to say you", pl, npc, (Object[]) new Object[0]);
			return;
		}
		int MARK_OF_CHALLENGER_ID = 2627;
		int MARK_OF_PILGRIM_ID = 2721;
		int MARK_OF_GLORY_ID = 3203;
		int event = Integer.parseInt(args[0]);
		int Level = pl.getLevel();
		ClassId classId = pl.getClassId();
		String htmltext = "No Quest";
		if(event == 48 && classId == ClassId.orcMonk)
		{
			int MARK_OF_DUELIST_ID = 2762;
			if(Level <= 39)
			{
				htmltext = pl.getInventory().getItemByItemId(MARK_OF_CHALLENGER_ID) == null || pl.getInventory().getItemByItemId(MARK_OF_GLORY_ID) == null || pl.getInventory().getItemByItemId(MARK_OF_DUELIST_ID) == null ? "16.htm" : "17.htm";
			}
			else if(pl.getInventory().getItemByItemId(MARK_OF_CHALLENGER_ID) == null || pl.getInventory().getItemByItemId(MARK_OF_GLORY_ID) == null || pl.getInventory().getItemByItemId(MARK_OF_DUELIST_ID) == null)
			{
				htmltext = "18.htm";
			}
			else
			{
				pl.getInventory().destroyItemByItemId(MARK_OF_CHALLENGER_ID, 1);
				pl.getInventory().destroyItemByItemId(MARK_OF_GLORY_ID, 1);
				pl.getInventory().destroyItemByItemId(MARK_OF_DUELIST_ID, 1);
				pl.setClassId(48, false, true);
				htmltext = "19.htm";
			}
		}
		else if(event == 46 && classId == ClassId.orcRaider)
		{
			int MARK_OF_CHAMPION_ID = 3276;
			if(Level <= 39)
			{
				htmltext = pl.getInventory().getItemByItemId(MARK_OF_CHALLENGER_ID) == null || pl.getInventory().getItemByItemId(MARK_OF_GLORY_ID) == null || pl.getInventory().getItemByItemId(MARK_OF_CHAMPION_ID) == null ? "20.htm" : "21.htm";
			}
			else if(pl.getInventory().getItemByItemId(MARK_OF_CHALLENGER_ID) == null || pl.getInventory().getItemByItemId(MARK_OF_GLORY_ID) == null || pl.getInventory().getItemByItemId(MARK_OF_CHAMPION_ID) == null)
			{
				htmltext = "22.htm";
			}
			else
			{
				pl.getInventory().destroyItemByItemId(MARK_OF_CHALLENGER_ID, 1);
				pl.getInventory().destroyItemByItemId(MARK_OF_GLORY_ID, 1);
				pl.getInventory().destroyItemByItemId(MARK_OF_CHAMPION_ID, 1);
				pl.setClassId(46, false, true);
				htmltext = "23.htm";
			}
		}
		else if(event == 51 && classId == ClassId.orcShaman)
		{
			int MARK_OF_LORD_ID = 3390;
			if(Level <= 39)
			{
				htmltext = pl.getInventory().getItemByItemId(MARK_OF_PILGRIM_ID) == null || pl.getInventory().getItemByItemId(MARK_OF_GLORY_ID) == null || pl.getInventory().getItemByItemId(MARK_OF_LORD_ID) == null ? "24.htm" : "25.htm";
			}
			else if(pl.getInventory().getItemByItemId(MARK_OF_PILGRIM_ID) == null || pl.getInventory().getItemByItemId(MARK_OF_GLORY_ID) == null || pl.getInventory().getItemByItemId(MARK_OF_LORD_ID) == null)
			{
				htmltext = "26.htm";
			}
			else
			{
				pl.getInventory().destroyItemByItemId(MARK_OF_PILGRIM_ID, 1);
				pl.getInventory().destroyItemByItemId(MARK_OF_GLORY_ID, 1);
				pl.getInventory().destroyItemByItemId(MARK_OF_LORD_ID, 1);
				pl.setClassId(51, false, true);
				htmltext = "27.htm";
			}
		}
		else if(event == 52 && classId == ClassId.orcShaman)
		{
			int MARK_OF_WARSPIRIT_ID = 2879;
			if(Level <= 39)
			{
				htmltext = pl.getInventory().getItemByItemId(MARK_OF_PILGRIM_ID) == null || pl.getInventory().getItemByItemId(MARK_OF_GLORY_ID) == null || pl.getInventory().getItemByItemId(MARK_OF_WARSPIRIT_ID) == null ? "28.htm" : "29.htm";
			}
			else if(pl.getInventory().getItemByItemId(MARK_OF_PILGRIM_ID) == null || pl.getInventory().getItemByItemId(MARK_OF_GLORY_ID) == null || pl.getInventory().getItemByItemId(MARK_OF_WARSPIRIT_ID) == null)
			{
				htmltext = "30.htm";
			}
			else
			{
				pl.getInventory().destroyItemByItemId(MARK_OF_PILGRIM_ID, 1);
				pl.getInventory().destroyItemByItemId(MARK_OF_GLORY_ID, 1);
				pl.getInventory().destroyItemByItemId(MARK_OF_WARSPIRIT_ID, 1);
				pl.setClassId(52, false, true);
				htmltext = "31.htm";
			}
		}
		npc.showChatWindow(pl, "villagemaster/30513/" + htmltext);
	}
	
	public void onTalk30474()
	{
		Player pl = getSelf();
		NpcInstance npc = getNpc();
		if(pl == null || npc == null)
		{
			return;
		}
		if(!(npc instanceof VillageMasterInstance))
		{
			show("I have nothing to say you", pl, npc, (Object[]) new Object[0]);
			return;
		}
		ClassId classId = pl.getClassId();
		String htmltext = npc.getNpcId() == 30175 ? classId == ClassId.shillienOracle ? "08.htm" : classId == ClassId.darkWizard ? "19.htm" : classId == ClassId.spellhowler || classId == ClassId.shillienElder || classId == ClassId.phantomSummoner ? "54.htm" : classId == ClassId.darkMage ? "55.htm" : "56.htm" : classId == ClassId.palusKnight ? "01.htm" : classId == ClassId.shillienOracle ? "08.htm" : classId == ClassId.assassin ? "12.htm" : classId == ClassId.darkWizard ? "19.htm" : classId == ClassId.shillienKnight || classId == ClassId.abyssWalker || classId == ClassId.bladedancer || classId == ClassId.phantomRanger ? "54.htm" : classId == ClassId.spellhowler || classId == ClassId.shillienElder || classId == ClassId.phantomSummoner ? "54.htm" : classId == ClassId.darkFighter || classId == ClassId.darkMage ? "55.htm" : "56.htm";
		npc.showChatWindow(pl, "villagemaster/30474/" + htmltext);
	}
	
	public void onChange30474(String[] args)
	{
		Player pl = getSelf();
		NpcInstance npc = getNpc();
		if(pl == null || npc == null)
		{
			return;
		}
		if(!(npc instanceof VillageMasterInstance))
		{
			show("I have nothing to say you", pl, npc, (Object[]) new Object[0]);
			return;
		}
		int MARK_OF_SEEKER_ID = 2673;
		int MARK_OF_SCHOLAR_ID = 2674;
		int MARK_OF_FATE_ID = 3172;
		int event = Integer.parseInt(args[0]);
		int Level = pl.getLevel();
		ClassId classId = pl.getClassId();
		String htmltext = "No Quest";
		if(event == 33 && classId == ClassId.palusKnight)
		{
			int MARK_OF_WITCHCRAFT_ID = 3307;
			int MARK_OF_DUTY_ID = 2633;
			if(Level <= 39)
			{
				htmltext = pl.getInventory().getItemByItemId(MARK_OF_DUTY_ID) == null || pl.getInventory().getItemByItemId(MARK_OF_FATE_ID) == null || pl.getInventory().getItemByItemId(MARK_OF_WITCHCRAFT_ID) == null ? "26.htm" : "27.htm";
			}
			else if(pl.getInventory().getItemByItemId(MARK_OF_DUTY_ID) == null || pl.getInventory().getItemByItemId(MARK_OF_FATE_ID) == null || pl.getInventory().getItemByItemId(MARK_OF_WITCHCRAFT_ID) == null)
			{
				htmltext = "28.htm";
			}
			else
			{
				pl.getInventory().destroyItemByItemId(MARK_OF_DUTY_ID, 1);
				pl.getInventory().destroyItemByItemId(MARK_OF_FATE_ID, 1);
				pl.getInventory().destroyItemByItemId(MARK_OF_WITCHCRAFT_ID, 1);
				pl.setClassId(33, false, true);
				htmltext = "29.htm";
			}
		}
		else if(event == 34 && classId == ClassId.palusKnight)
		{
			int MARK_OF_DUELIST_ID = 2762;
			int MARK_OF_CHALLENGER_ID = 2627;
			if(Level <= 39)
			{
				htmltext = pl.getInventory().getItemByItemId(MARK_OF_CHALLENGER_ID) == null || pl.getInventory().getItemByItemId(MARK_OF_FATE_ID) == null || pl.getInventory().getItemByItemId(MARK_OF_DUELIST_ID) == null ? "30.htm" : "31.htm";
			}
			else if(pl.getInventory().getItemByItemId(MARK_OF_CHALLENGER_ID) == null || pl.getInventory().getItemByItemId(MARK_OF_FATE_ID) == null || pl.getInventory().getItemByItemId(MARK_OF_DUELIST_ID) == null)
			{
				htmltext = "32.htm";
			}
			else
			{
				pl.getInventory().destroyItemByItemId(MARK_OF_CHALLENGER_ID, 1);
				pl.getInventory().destroyItemByItemId(MARK_OF_FATE_ID, 1);
				pl.getInventory().destroyItemByItemId(MARK_OF_DUELIST_ID, 1);
				pl.setClassId(34, false, true);
				htmltext = "33.htm";
			}
		}
		else if(event == 43 && classId == ClassId.shillienOracle)
		{
			int MARK_OF_REFORMER_ID = 2821;
			int MARK_OF_PILGRIM_ID = 2721;
			if(Level <= 39)
			{
				htmltext = pl.getInventory().getItemByItemId(MARK_OF_PILGRIM_ID) == null || pl.getInventory().getItemByItemId(MARK_OF_FATE_ID) == null || pl.getInventory().getItemByItemId(MARK_OF_REFORMER_ID) == null ? "34.htm" : "35.htm";
			}
			else if(pl.getInventory().getItemByItemId(MARK_OF_PILGRIM_ID) == null || pl.getInventory().getItemByItemId(MARK_OF_FATE_ID) == null || pl.getInventory().getItemByItemId(MARK_OF_REFORMER_ID) == null)
			{
				htmltext = "36.htm";
			}
			else
			{
				pl.getInventory().destroyItemByItemId(MARK_OF_PILGRIM_ID, 1);
				pl.getInventory().destroyItemByItemId(MARK_OF_FATE_ID, 1);
				pl.getInventory().destroyItemByItemId(MARK_OF_REFORMER_ID, 1);
				pl.setClassId(43, false, true);
				htmltext = "37.htm";
			}
		}
		else if(event == 36 && classId == ClassId.assassin)
		{
			int MARK_OF_SEARCHER_ID = 2809;
			if(Level <= 39)
			{
				htmltext = pl.getInventory().getItemByItemId(MARK_OF_SEEKER_ID) == null || pl.getInventory().getItemByItemId(MARK_OF_FATE_ID) == null || pl.getInventory().getItemByItemId(MARK_OF_SEARCHER_ID) == null ? "38.htm" : "39.htm";
			}
			else if(pl.getInventory().getItemByItemId(MARK_OF_SEEKER_ID) == null || pl.getInventory().getItemByItemId(MARK_OF_FATE_ID) == null || pl.getInventory().getItemByItemId(MARK_OF_SEARCHER_ID) == null)
			{
				htmltext = "40.htm";
			}
			else
			{
				pl.getInventory().destroyItemByItemId(MARK_OF_SEEKER_ID, 1);
				pl.getInventory().destroyItemByItemId(MARK_OF_FATE_ID, 1);
				pl.getInventory().destroyItemByItemId(MARK_OF_SEARCHER_ID, 1);
				pl.setClassId(36, false, true);
				htmltext = "41.htm";
			}
		}
		else if(event == 37 && classId == ClassId.assassin)
		{
			int MARK_OF_SAGITTARIUS_ID = 3293;
			if(Level <= 39)
			{
				htmltext = pl.getInventory().getItemByItemId(MARK_OF_SEEKER_ID) == null || pl.getInventory().getItemByItemId(MARK_OF_FATE_ID) == null || pl.getInventory().getItemByItemId(MARK_OF_SAGITTARIUS_ID) == null ? "42.htm" : "43.htm";
			}
			else if(pl.getInventory().getItemByItemId(MARK_OF_SEEKER_ID) == null || pl.getInventory().getItemByItemId(MARK_OF_FATE_ID) == null || pl.getInventory().getItemByItemId(MARK_OF_SAGITTARIUS_ID) == null)
			{
				htmltext = "44.htm";
			}
			else
			{
				pl.getInventory().destroyItemByItemId(MARK_OF_SEEKER_ID, 1);
				pl.getInventory().destroyItemByItemId(MARK_OF_FATE_ID, 1);
				pl.getInventory().destroyItemByItemId(MARK_OF_SAGITTARIUS_ID, 1);
				pl.setClassId(37, false, true);
				htmltext = "45.htm";
			}
		}
		else if(event == 40 && classId == ClassId.darkWizard)
		{
			int MARK_OF_MAGUS_ID = 2840;
			if(Level <= 39)
			{
				htmltext = pl.getInventory().getItemByItemId(MARK_OF_SCHOLAR_ID) == null || pl.getInventory().getItemByItemId(MARK_OF_FATE_ID) == null || pl.getInventory().getItemByItemId(MARK_OF_MAGUS_ID) == null ? "46.htm" : "47.htm";
			}
			else if(pl.getInventory().getItemByItemId(MARK_OF_SCHOLAR_ID) == null || pl.getInventory().getItemByItemId(MARK_OF_FATE_ID) == null || pl.getInventory().getItemByItemId(MARK_OF_MAGUS_ID) == null)
			{
				htmltext = "48.htm";
			}
			else
			{
				pl.getInventory().destroyItemByItemId(MARK_OF_SCHOLAR_ID, 1);
				pl.getInventory().destroyItemByItemId(MARK_OF_FATE_ID, 1);
				pl.getInventory().destroyItemByItemId(MARK_OF_MAGUS_ID, 1);
				pl.setClassId(40, false, true);
				htmltext = "49.htm";
			}
		}
		else if(event == 41 && classId == ClassId.darkWizard)
		{
			int MARK_OF_SUMMONER_ID = 3336;
			if(Level <= 39)
			{
				htmltext = pl.getInventory().getItemByItemId(MARK_OF_SCHOLAR_ID) == null || pl.getInventory().getItemByItemId(MARK_OF_FATE_ID) == null || pl.getInventory().getItemByItemId(MARK_OF_SUMMONER_ID) == null ? "50.htm" : "51.htm";
			}
			else if(pl.getInventory().getItemByItemId(MARK_OF_SCHOLAR_ID) == null || pl.getInventory().getItemByItemId(MARK_OF_FATE_ID) == null || pl.getInventory().getItemByItemId(MARK_OF_SUMMONER_ID) == null)
			{
				htmltext = "52.htm";
			}
			else
			{
				pl.getInventory().destroyItemByItemId(MARK_OF_SCHOLAR_ID, 1);
				pl.getInventory().destroyItemByItemId(MARK_OF_FATE_ID, 1);
				pl.getInventory().destroyItemByItemId(MARK_OF_SUMMONER_ID, 1);
				pl.setClassId(41, false, true);
				htmltext = "53.htm";
			}
		}
		npc.showChatWindow(pl, "villagemaster/30474/" + htmltext);
	}
	
	public void onChange32145(String[] args)
	{
		Player pl = getSelf();
		NpcInstance npc = getNpc();
		if(pl == null || npc == null)
		{
			return;
		}
		if(!(npc instanceof VillageMasterInstance))
		{
			show("I have nothing to say you", pl, npc, (Object[]) new Object[0]);
			return;
		}
		int SteelrazorEvaluation = 9772;
		int event = Integer.parseInt(args[0]);
		int Level = pl.getLevel();
		ClassId classId = pl.getClassId();
		String htmltext = "04.htm";
		npc.showChatWindow(pl, "villagemaster/32145/" + htmltext);
	}
	
	public void onTalk32145()
	{
		Player pl = getSelf();
		NpcInstance npc = getNpc();
		if(pl == null || npc == null)
		{
			return;
		}
		if(!(npc instanceof VillageMasterInstance))
		{
			show("I have nothing to say you", pl, npc, (Object[]) new Object[0]);
			return;
		}
		npc.showChatWindow(pl, "villagemaster/32145/02.htm");
	}
	
	public void onChange32146(String[] args)
	{
		Player pl = getSelf();
		NpcInstance npc = getNpc();
		if(pl == null || npc == null)
		{
			return;
		}
		if(!(npc instanceof VillageMasterInstance))
		{
			show("I have nothing to say you", pl, npc, (Object[]) new Object[0]);
			return;
		}
		int GwainsRecommendation = 9753;
		int event = Integer.parseInt(args[0]);
		int Level = pl.getLevel();
		ClassId classId = pl.getClassId();
		String htmltext = "04.htm";
		npc.showChatWindow(pl, "villagemaster/32146/" + htmltext);
	}
	
	public void onTalk32146()
	{
		Player pl = getSelf();
		NpcInstance npc = getNpc();
		if(pl == null || npc == null)
		{
			return;
		}
		if(!(npc instanceof VillageMasterInstance))
		{
			show("I have nothing to say you", pl, npc, (Object[]) new Object[0]);
			return;
		}
		ClassId classId = pl.getClassId();
		npc.showChatWindow(pl, "villagemaster/32146/02.htm");
	}
	
	public void onTalk32199()
	{
		Player pl = getSelf();
		NpcInstance npc = getNpc();
		if(pl == null || npc == null)
		{
			return;
		}
		if(!(npc instanceof VillageMasterInstance))
		{
			show("I have nothing to say you", pl, npc, (Object[]) new Object[0]);
			return;
		}
		ClassId classId = pl.getClassId();
		npc.showChatWindow(pl, "villagemaster/32199/02.htm");
	}
	
	public void onTalk32157()
	{
		Player pl = getSelf();
		NpcInstance npc = getNpc();
		if(pl == null || npc == null)
		{
			return;
		}
		if(!(npc instanceof VillageMasterInstance))
		{
			show("I have nothing to say you", pl, npc, (Object[]) new Object[0]);
			return;
		}
		ClassId classId = pl.getClassId();
		Race race = pl.getRace();
		String htmltext = race != Race.dwarf ? "002.htm" : classId == ClassId.dwarvenFighter ? "003f.htm" : classId.getLevel() == 3 ? "004.htm" : "005.htm";
		String prefix = "head_blacksmith_mokabred";
		npc.showChatWindow(pl, "villagemaster/32157/" + prefix + htmltext);
	}
	
	public void onTalk32160()
	{
		Player pl = getSelf();
		NpcInstance npc = getNpc();
		if(pl == null || npc == null)
		{
			return;
		}
		if(!(npc instanceof VillageMasterInstance))
		{
			show("I have nothing to say you", pl, npc, (Object[]) new Object[0]);
			return;
		}
		ClassId classId = pl.getClassId();
		Race race = pl.getRace();
		String htmltext = race != Race.darkelf ? "002.htm" : classId == ClassId.darkFighter ? "003f.htm" : classId == ClassId.darkMage ? "003m.htm" : classId.getLevel() == 3 ? "004.htm" : "005.htm";
		String prefix = "grandmagister_devon";
		npc.showChatWindow(pl, "villagemaster/32160/" + prefix + htmltext);
	}
	
	public void onChange32199(String[] args)
	{
		Player pl = getSelf();
		NpcInstance npc = getNpc();
		if(pl == null || npc == null)
		{
			return;
		}
		if(!(npc instanceof VillageMasterInstance))
		{
			show("I have nothing to say you", pl, npc, (Object[]) new Object[0]);
			return;
		}
		int KamaelInquisitorMark = 9782;
		int SB_Certificate = 9806;
		int OrkurusRecommendation = 9760;
		int classid = Integer.parseInt(args[0]);
		int Level = pl.getLevel();
		npc.showChatWindow(pl, "villagemaster/32199/02.htm");
	}
	
	public void onTalk32158()
	{
		Player pl = getSelf();
		NpcInstance npc = getNpc();
		if(pl == null || npc == null)
		{
			return;
		}
		if(!(npc instanceof VillageMasterInstance))
		{
			show("I have nothing to say you", pl, npc, (Object[]) new Object[0]);
			return;
		}
		ClassId classId = pl.getClassId();
		Race race = pl.getRace();
		String htmltext = race != Race.dwarf ? "002.htm" : classId == ClassId.dwarvenFighter ? "003f.htm" : classId.getLevel() == 3 ? "004.htm" : "005.htm";
		String prefix = "warehouse_chief_fisser";
		npc.showChatWindow(pl, "villagemaster/32158/" + prefix + htmltext);
	}
	
	public void onTalk32171()
	{
		Player pl = getSelf();
		NpcInstance npc = getNpc();
		if(pl == null || npc == null)
		{
			return;
		}
		if(!(npc instanceof VillageMasterInstance))
		{
			show("I have nothing to say you", pl, npc, (Object[]) new Object[0]);
			return;
		}
		ClassId classId = pl.getClassId();
		Race race = pl.getRace();
		String htmltext = race != Race.dwarf ? "002.htm" : classId == ClassId.dwarvenFighter ? "003f.htm" : classId.getLevel() == 3 ? "004.htm" : "005.htm";
		String prefix = "warehouse_chief_hufran";
		npc.showChatWindow(pl, "villagemaster/32171/" + prefix + htmltext);
	}
	
	public void onTalk32213()
	{
		onTalk32199();
	}
	
	public void onChange32213(String[] args)
	{
		onChange32199(args);
	}
	
	public void onTalk32214()
	{
		onTalk32199();
	}
	
	public void onChange32214(String[] args)
	{
		onChange32199(args);
	}
	
	public void onTalk32217()
	{
		onTalk32199();
	}
	
	public void onChange32217(String[] args)
	{
		onChange32199(args);
	}
	
	public void onTalk32218()
	{
		onTalk32199();
	}
	
	public void onChange32218(String[] args)
	{
		onChange32199(args);
	}
	
	public void onTalk32221()
	{
		onTalk32199();
	}
	
	public void onChange32221(String[] args)
	{
		onChange32199(args);
	}
	
	public void onTalk32222()
	{
		onTalk32199();
	}
	
	public void onChange32222(String[] args)
	{
		onChange32199(args);
	}
	
	public void onTalk32205()
	{
		onTalk32199();
	}
	
	public void onChange32205(String[] args)
	{
		onChange32199(args);
	}
	
	public void onTalk32206()
	{
		onTalk32199();
	}
	
	public void onChange32206(String[] args)
	{
		onChange32199(args);
	}
	
	public void onTalk32147()
	{
		onTalk30037();
	}
	
	public void onTalk32150()
	{
		onTalk30565();
	}
	
	public void onTalk32153()
	{
		onTalk30037();
	}
	
	public void onTalk32154()
	{
		onTalk30066();
	}
	
	public void onTalk32226()
	{
		onTalk32199();
	}
	
	public void onTalk32225()
	{
		onTalk32199();
	}
	
	public void onTalk32230()
	{
		onTalk32199();
	}
	
	public void onTalk32229()
	{
		onTalk32199();
	}
	
	public void onTalk32233()
	{
		onTalk32199();
	}
	
	public void onTalk32234()
	{
		onTalk32199();
	}
	
	public void onTalk32202()
	{
		onTalk32199();
	}
	
	public void onTalk32210()
	{
		onTalk32199();
	}
	
	public void onTalk32209()
	{
		onTalk32199();
	}
}