package l2.gameserver.network.l2.c2s;

import l2.commons.lang.ArrayUtils;
import l2.gameserver.Config;
import l2.gameserver.data.xml.holder.SkillAcquireHolder;
import l2.gameserver.model.Player;
import l2.gameserver.model.SkillLearn;
import l2.gameserver.model.base.AcquireType;
import l2.gameserver.model.base.ClassId;
import l2.gameserver.model.instances.NpcInstance;
import l2.gameserver.network.l2.s2c.AcquireSkillInfo;
import l2.gameserver.tables.SkillTable;

public class RequestAquireSkillInfo extends L2GameClientPacket
{
	private int _id;
	private int _level;
	private AcquireType _type;
	
	@Override
	protected void readImpl()
	{
		_id = readD();
		_level = readD();
		_type = ArrayUtils.valid(AcquireType.VALUES, readD());
	}
	
	@Override
	protected void runImpl()
	{
		Player player = getClient().getActiveChar();
		if(player == null || player.getTransformation() != 0 || SkillTable.getInstance().getInfo(_id, _level) == null || _type == null)
		{
			return;
		}
		NpcInstance trainer = player.getLastNpc();
		if(trainer == null || !trainer.isInActingRange(player))
		{
			return;
		}
		int clsId = player.getVarInt("AcquireSkillClassId", player.getClassId().getId());
		ClassId classId = clsId >= 0 && clsId < ClassId.VALUES.length ? ClassId.VALUES[clsId] : null;
		SkillLearn skillLearn = SkillAcquireHolder.getInstance().getSkillLearn(player, classId, _id, _level, _type);
		if(skillLearn == null)
		{
			return;
		}
		if(Config.ALT_DISABLE_SPELLBOOKS && _type == AcquireType.NORMAL)
		{
			sendPacket(new AcquireSkillInfo(_type, skillLearn, 0, 0));
		}
		else
		{
			sendPacket(new AcquireSkillInfo(_type, skillLearn, skillLearn.getItemId(), (int) skillLearn.getItemCount()));
		}
	}
}