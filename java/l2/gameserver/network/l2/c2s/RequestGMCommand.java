package l2.gameserver.network.l2.c2s;

import l2.gameserver.model.Player;
import l2.gameserver.model.World;
import l2.gameserver.model.items.ItemInstance;
import l2.gameserver.network.l2.s2c.GMHennaInfo;
import l2.gameserver.network.l2.s2c.GMViewCharacterInfo;
import l2.gameserver.network.l2.s2c.GMViewItemList;
import l2.gameserver.network.l2.s2c.GMViewPledgeInfo;
import l2.gameserver.network.l2.s2c.GMViewQuestInfo;
import l2.gameserver.network.l2.s2c.GMViewSkillInfo;
import l2.gameserver.network.l2.s2c.GMViewWarehouseWithdrawList;

public class RequestGMCommand extends L2GameClientPacket
{
	private String _targetName;
	private int _command;
	
	@Override
	protected void readImpl()
	{
		_targetName = readS();
		_command = readD();
	}
	
	@Override
	protected void runImpl()
	{
		Player player = getClient().getActiveChar();
		Player target = World.getPlayer(_targetName);
		if(player == null || target == null)
		{
			return;
		}
		if(!player.getPlayerAccess().CanViewChar)
		{
			return;
		}
		switch(_command)
		{
			case 1:
			{
				player.sendPacket(new GMViewCharacterInfo(target));
				player.sendPacket(new GMHennaInfo(target));
				break;
			}
			case 2:
			{
				if(target.getClan() == null)
					break;
				player.sendPacket(new GMViewPledgeInfo(target));
				break;
			}
			case 3:
			{
				player.sendPacket(new GMViewSkillInfo(target));
				break;
			}
			case 4:
			{
				player.sendPacket(new GMViewQuestInfo(target));
				break;
			}
			case 5:
			{
				ItemInstance[] items = target.getInventory().getItems();
				int questSize = 0;
				player.sendPacket(new GMViewItemList(target, items, items.length - questSize));
				break;
			}
			case 6:
			{
				player.sendPacket(new GMViewWarehouseWithdrawList(target));
			}
		}
	}
}