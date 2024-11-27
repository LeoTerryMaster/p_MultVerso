package services;

import l2.commons.util.Rnd;
import l2.gameserver.model.Player;
import l2.gameserver.model.instances.NpcInstance;
import l2.gameserver.scripts.Functions;

public class LuxorAgathion extends Functions
{
	private static final int[][] INGRIDIENTS = {{6471, 25}, {5094, 50}, {9814, 4}, {9816, 5}, {9817, 5}, {9815, 3}, {57, 7500000}};
	private static final int OldAgathion = 10408;
	private static final int ShadowPurpleVikingCirclet = 10315;
	private static final int ShadowGoldenVikingCirclet = 10321;
	private static final int[] ANGEL_BRACELET_IDS = {10320, 10316, 10317, 10318, 10319};
	private static final int[] DEVIL_BRACELET_IDS = {10326, 10322, 10323, 10324, 10325};
	private static final int SUCCESS_RATE = 60;
	private static final int RARE_RATE = 5;
	
	public void angelAgathion()
	{
		agathion(ANGEL_BRACELET_IDS, 1);
	}
	
	public void devilAgathion()
	{
		agathion(DEVIL_BRACELET_IDS, 2);
	}
	
	private void agathion(int[] braceletes, int type)
	{
		Player player = getSelf();
		NpcInstance npc = getNpc();
		if(player == null || npc == null)
		{
			return;
		}
		for(int[] ingridient : INGRIDIENTS)
		{
			if(getItemCount(player, ingridient[0]) >= (long) ingridient[1])
				continue;
			show("merchant/30098-2.htm", player, npc, (Object[]) new Object[0]);
			return;
		}
		for(int[] ingridient : INGRIDIENTS)
		{
			removeItem(player, ingridient[0], (long) ingridient[1]);
		}
		if(!Rnd.chance(SUCCESS_RATE))
		{
			addItem(player, 10408, (long) 1);
			if(type == 1)
			{
				addItem(player, 10315, (long) 1);
			}
			else
			{
				addItem(player, 10321, (long) 1);
			}
			show("merchant/30098-3.htm", player, npc, (Object[]) new Object[0]);
			return;
		}
		addItem(player, braceletes[Rnd.chance(RARE_RATE) ? 0 : Rnd.get(1, braceletes.length - 1)], (long) 1);
		show("merchant/30098-4.htm", player, npc, (Object[]) new Object[0]);
	}
}