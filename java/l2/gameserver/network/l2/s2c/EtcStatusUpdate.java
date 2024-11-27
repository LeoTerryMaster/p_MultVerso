package l2.gameserver.network.l2.s2c;

import l2.gameserver.model.Player;

public class EtcStatusUpdate extends L2GameServerPacket
{
	private final int IncreasedForce;
	private final int WeightPenalty;
	private final int MessageRefusal;
	private final int DangerArea;
	private final int GradeExpertisePenalty;
	private final int CharmOfCourage;
	private final int DeathPenaltyLevel;
	private final int ConsumedSouls;
	
	public EtcStatusUpdate(Player player)
	{
		IncreasedForce = player.getIncreasedForce();
		WeightPenalty = player.getWeightPenalty();
		MessageRefusal = player.getMessageRefusal() || player.getNoChannel() != 0 || player.isBlockAll() ? 1 : 0;
		DangerArea = player.isInDangerArea() ? 1 : 0;
		GradeExpertisePenalty = player.getGradePenalty();
		CharmOfCourage = player.isCharmOfCourage() ? 1 : 0;
		DeathPenaltyLevel = player.getDeathPenalty() == null ? 0 : player.getDeathPenalty().getLevel();
		ConsumedSouls = player.getConsumedSouls();
	}
	
	@Override
	protected final void writeImpl()
	{
		writeC(243);
		writeD(IncreasedForce);
		writeD(WeightPenalty);
		writeD(MessageRefusal);
		writeD(DangerArea);
		writeD(GradeExpertisePenalty);
		writeD(CharmOfCourage);
		writeD(DeathPenaltyLevel);
	}
}