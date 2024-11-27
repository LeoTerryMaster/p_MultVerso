package l2.gameserver.model.reward;

public class RewardItem
{
	public final int itemId;
	public long count;
	public boolean isAdena;
	public boolean isSealStone;
	
	public RewardItem(int itemId)
	{
		this.itemId = itemId;
		count = 1;
	}
}