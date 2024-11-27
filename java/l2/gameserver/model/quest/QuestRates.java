package l2.gameserver.model.quest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class QuestRates
{
	private static final Logger LOG = LoggerFactory.getLogger(QuestRates.class);
	private final int _questId;
	private double _dropRate;
	private double _rewardRate;
	private double _expRate;
	private double _spRate;
	
	public QuestRates(int questId)
	{
		_questId = questId;
		_dropRate = 1.0;
		_rewardRate = 1.0;
		_expRate = 1.0;
		_spRate = 1.0;
	}
	
	public void updateParam(String paramName, String paramValue)
	{
		if(paramName.equalsIgnoreCase("Drop") || paramName.equalsIgnoreCase("DropRate"))
		{
			setDropRate(Double.parseDouble(paramValue));
		}
		else if(paramName.equalsIgnoreCase("Reward") || paramName.equalsIgnoreCase("RewardRate"))
		{
			setRewardRate(Double.parseDouble(paramValue));
		}
		else if(paramName.equalsIgnoreCase("Exp") || paramName.equalsIgnoreCase("ExpRate"))
		{
			setExpRate(Double.parseDouble(paramValue));
		}
		else if(paramName.equalsIgnoreCase("Sp") || paramName.equalsIgnoreCase("SpRate"))
		{
			setExpRate(Double.parseDouble(paramValue));
		}
		else
		{
			throw new IllegalArgumentException("Unknown param \"" + paramName + "\"");
		}
	}
	
	public double getDropRate()
	{
		return _dropRate;
	}
	
	public void setDropRate(double dropRate)
	{
		_dropRate = dropRate;
	}
	
	public double getRewardRate()
	{
		return _rewardRate;
	}
	
	public void setRewardRate(double rewardRate)
	{
		_rewardRate = rewardRate;
	}
	
	public double getExpRate()
	{
		return _expRate;
	}
	
	public void setExpRate(double expRate)
	{
		_expRate = expRate;
	}
	
	public double getSpRate()
	{
		return _spRate;
	}
	
	public void setSpRate(double spRate)
	{
		_spRate = spRate;
	}
}