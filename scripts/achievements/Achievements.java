package achievements;

import l2.commons.time.cron.SchedulingPattern;
import l2.gameserver.Config;
import l2.gameserver.handler.voicecommands.IVoicedCommandHandler;
import l2.gameserver.handler.voicecommands.VoicedCommandHandler;
import l2.gameserver.model.Player;
import l2.gameserver.model.reward.RewardData;
import l2.gameserver.scripts.ScriptFile;
import l2.gameserver.scripts.Scripts;
import org.apache.commons.lang3.ArrayUtils;
import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class Achievements implements ScriptFile
{
	private static final Achievements INSTANCE = new Achievements();
	private static final Logger LOG = LoggerFactory.getLogger(Achievements.class);
	private static final SAXReader READER = new SAXReader(true);
	private static final AchVoicedCommandHandler VOICED_COMMAND_HANDLER = new AchVoicedCommandHandler();
	private static final File ACHIEVEMENTS_FILE = new File(Config.DATAPACK_ROOT, "data/achievements.xml");
	private boolean _isEnabled;
	private String[] _voiceCommands = ArrayUtils.EMPTY_STRING_ARRAY;
	private Map<AchievementMetricType, List<AchievementInfo>> _achievementByMetric = Collections.emptyMap();
	private Map<AchievementInfo.AchievementInfoCategory, List<AchievementInfo>> _achievementByCategory = Collections.emptyMap();
	private List<AchievementInfo.AchievementInfoCategory> _categories = Collections.emptyList();
	private List<AchievementInfo> _achievementInfos = Collections.emptyList();
	
	public static Achievements getInstance()
	{
		return INSTANCE;
	}
	
	public boolean isEnabled()
	{
		return _isEnabled;
	}
	
	private String[] getVoiceCommands()
	{
		return _voiceCommands;
	}
	
	public List<AchievementInfo> getAchievementInfosByMetric(AchievementMetricType metricType)
	{
		return _achievementByMetric.get(metricType);
	}
	
	public List<AchievementInfo> getAchievementInfosByCategory(AchievementInfo.AchievementInfoCategory category)
	{
		return _achievementByCategory.get(category);
	}
	
	public AchievementInfo getAchievementInfoById(int achievementId)
	{
		for(AchievementInfo achievementInfo : _achievementInfos)
		{
			if(achievementInfo.getId() != achievementId)
				continue;
			return achievementInfo;
		}
		return null;
	}
	
	public List<AchievementInfo.AchievementInfoCategory> getCategories()
	{
		return _categories;
	}
	
	public void parse()
	{
		try
		{
			Document document = READER.read(ACHIEVEMENTS_FILE);
			Element rootElement = document.getRootElement();
			_isEnabled = Boolean.parseBoolean(rootElement.attributeValue("enabled", "false"));
			String voiceCommandsText = rootElement.attributeValue("voice_commands");
			String[] arrstring = _voiceCommands = voiceCommandsText != null && !voiceCommandsText.trim().isEmpty() ? voiceCommandsText.split("[^\\w\\d_]+") : ArrayUtils.EMPTY_STRING_ARRAY;
			HashMap<AchievementMetricType, List<AchievementInfo>> achievementsByMetric = new HashMap<>();
			HashMap<AchievementInfo.AchievementInfoCategory, List<AchievementInfo>> achievementsByCategory = new HashMap<>();
			LinkedList<AchievementInfo.AchievementInfoCategory> categoryList = new LinkedList<>();
			ArrayList<AchievementInfo> achievementList = new ArrayList<>();
			if(_isEnabled)
			{
				Iterator achievementsElementIt = document.getRootElement().elementIterator();
				TreeMap<String, Object> categories = new TreeMap<>();
				while(achievementsElementIt.hasNext())
				{
					Object category;
					String name;
					Element achievementsElement = (Element) achievementsElementIt.next();
					if("category".equalsIgnoreCase(achievementsElement.getName()))
					{
						name = achievementsElement.attributeValue("name");
						category = new AchievementInfo.AchievementInfoCategory(name, achievementsElement.attributeValue("title_address"));
						categories.put(name, category);
						categoryList.add((AchievementInfo.AchievementInfoCategory) category);
						continue;
					}
					if(!"achievement".equalsIgnoreCase(achievementsElement.getName()))
						continue;
					name = achievementsElement.attributeValue("name_address");
					category = achievementsElement.attributeValue("category");
					AchievementMetricType metricType = AchievementMetricType.valueOf(achievementsElement.attributeValue("type"));
					String expireCronPattern = achievementsElement.attributeValue("expire_cron");
					long metricNotifyDelay = Long.parseLong(achievementsElement.attributeValue("metric_stage_notify_delay", String.valueOf(0)));
					AchievementInfo achInfo = new AchievementInfo(Integer.parseInt(achievementsElement.attributeValue("id")), metricType, metricNotifyDelay, name, expireCronPattern != null ? new SchedulingPattern(expireCronPattern) : null);
					achInfo.setCategory((AchievementInfo.AchievementInfoCategory) categories.get(category));
					String icon = achievementsElement.attributeValue("icon", "Icon.NOIMAGE");
					achInfo.setIcon(icon);
					int lastStageLvl = 0;
					Iterator achievementElementIt = achievementsElement.elementIterator();
					while(achievementElementIt.hasNext())
					{
						Element achievementElement = (Element) achievementElementIt.next();
						if("conds".equalsIgnoreCase(achievementElement.getName()))
						{
							Iterator condElementIt = achievementElement.elementIterator();
							while(condElementIt.hasNext())
							{
								Element condElement = (Element) condElementIt.next();
								if(!"cond".equalsIgnoreCase(condElement.getName()))
									continue;
								AchievementCondition cond = AchievementCondition.makeCond(condElement.attributeValue("name"), condElement.attributeValue("value"));
								if(cond == null)
								{
									throw new RuntimeException("Unknown condition \"" + condElement.getName() + " of achievement " + name + "(" + achInfo.getId() + ")");
								}
								achInfo.addCond(cond);
							}
							continue;
						}
						if(!"stage".equalsIgnoreCase(achievementElement.getName()))
							continue;
						int level = lastStageLvl = Integer.parseInt(achievementElement.attributeValue("level", String.valueOf(lastStageLvl + 1)));
						String stageDescAddr = achievementElement.attributeValue("desc_address");
						int stageVal = Integer.parseInt(achievementElement.attributeValue("value"));
						boolean resetMetric = Boolean.parseBoolean(achievementElement.attributeValue("reset_metric", String.valueOf(Boolean.TRUE)));
						if(level - achInfo.getMaxLevel() > 1)
						{
							LOG.warn("Inconsistent level \"" + level + "\" of achievement \"" + name + "\"(" + achInfo.getId() + ")");
						}
						AchievementInfo.AchievementInfoLevel achInfoLevel = achInfo.addLevel(level, stageVal, stageDescAddr, resetMetric);
						Iterator stageElementIt = achievementElement.elementIterator();
						while(stageElementIt.hasNext())
						{
							Element stageElement = (Element) stageElementIt.next();
							if("rewards".equals(stageElement.getName()))
							{
								Iterator rewardElementIt = stageElement.elementIterator();
								while(rewardElementIt.hasNext())
								{
									Element rewardElement = (Element) rewardElementIt.next();
									if(!"reward".equalsIgnoreCase(rewardElement.getName()))
										continue;
									int itemId = Integer.parseInt(rewardElement.attributeValue("item_id"));
									long minAmmount = Long.parseLong(rewardElement.attributeValue("min"));
									long maxAmmount = Long.parseLong(rewardElement.attributeValue("max"));
									int chance = (int) (Double.parseDouble(rewardElement.attributeValue("chance")) * 10000.0);
									RewardData data = new RewardData(itemId);
									data.setChance((double) chance);
									data.setMinDrop(minAmmount);
									data.setMaxDrop(maxAmmount);
									achInfoLevel.addRewardData(data);
								}
								continue;
							}
							if(!"conds".equalsIgnoreCase(stageElement.getName()))
								continue;
							Iterator condElementIt = stageElement.elementIterator();
							while(condElementIt.hasNext())
							{
								Element condElement = (Element) condElementIt.next();
								if(!"cond".equalsIgnoreCase(condElement.getName()))
									continue;
								AchievementCondition cond = AchievementCondition.makeCond(condElement.attributeValue("name"), condElement.attributeValue("value"));
								if(cond == null)
								{
									throw new RuntimeException("Unknown condition \"" + condElement.getName() + " of achievement " + name + "(" + achInfo.getId() + ")");
								}
								achInfoLevel.addCond(cond);
							}
						}
					}
					List<AchievementInfo> byMetric = achievementsByMetric.get(achInfo.getMetricType());
					if(byMetric == null)
					{
						byMetric = new ArrayList<>();
						achievementsByMetric.put(achInfo.getMetricType(), byMetric);
					}
					byMetric.add(achInfo);
					List<AchievementInfo> byCategory = achievementsByCategory.get(achInfo.getCategory());
					if(byCategory == null)
					{
						byCategory = new ArrayList<>();
						achievementsByCategory.put(achInfo.getCategory(), byCategory);
					}
					byCategory.add(achInfo);
					achievementList.add(achInfo);
				}
			}
			_categories = categoryList;
			_achievementByMetric = achievementsByMetric;
			_achievementByCategory = achievementsByCategory;
			_achievementInfos = achievementList;
			LOG.info("Achievements: Loaded " + _achievementInfos.size() + " achievement(s) for " + _categories.size() + " category(ies).");
		}
		catch(Exception e)
		{
			LOG.warn("Can't parse achievements", e);
		}
	}
	
	@Override
	public void onLoad()
	{
		getInstance().parse();
		if(getInstance().isEnabled())
		{
			AchievementMetricListeners.getInstance().init();
			if(VOICED_COMMAND_HANDLER.getVoicedCommandList().length > 0)
			{
				VoicedCommandHandler.getInstance().registerVoicedCommandHandler(VOICED_COMMAND_HANDLER);
			}
		}
	}
	
	@Override
	public void onReload()
	{
		onShutdown();
		onLoad();
	}
	
	@Override
	public void onShutdown()
	{
		if(getInstance().isEnabled())
		{
			AchievementMetricListeners.getInstance().done();
		}
	}
	
	private static class AchVoicedCommandHandler implements IVoicedCommandHandler
	{
		@Override
		public boolean useVoicedCommand(String command, Player activeChar, String target)
		{
			if(!getInstance().isEnabled())
			{
				return false;
			}
			for(String achVCmd : getInstance().getVoiceCommands())
			{
				if(achVCmd.isEmpty() || !achVCmd.equalsIgnoreCase(command))
					continue;
				Scripts.getInstance().callScripts(activeChar, AchievementUI.class.getName(), "achievements");
				return true;
			}
			return false;
		}
		
		@Override
		public String[] getVoicedCommandList()
		{
			if(getInstance().isEnabled())
			{
				return getInstance().getVoiceCommands();
			}
			return ArrayUtils.EMPTY_STRING_ARRAY;
		}
	}
}