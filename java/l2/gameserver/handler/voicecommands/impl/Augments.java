package l2.gameserver.handler.voicecommands.impl;

import l2.gameserver.data.xml.holder.OptionDataHolder;
import l2.gameserver.handler.voicecommands.IVoicedCommandHandler;
import l2.gameserver.model.Player;
import l2.gameserver.model.Skill;
import l2.gameserver.model.items.ItemInstance;
import l2.gameserver.stats.triggers.TriggerInfo;
import l2.gameserver.templates.OptionDataTemplate;

public class Augments implements IVoicedCommandHandler
{
	private final String[] _commandList = {"aug", "augments"};
	
	@Override
	public String[] getVoicedCommandList()
	{
		return _commandList;
	}
	
	@Override
	public boolean useVoicedCommand(String command, Player player, String args)
	{
		for(int slot = 0;slot < 17;++slot)
		{
			ItemInstance item = player.getInventory().getPaperdollItem(slot);
			if(item == null || !item.isAugmented())
				continue;
			StringBuilder info = new StringBuilder(30);
			info.append("<<Detail augments info>>");
			info.append("\n");
			info.append(item.getName() + " with enchant level " + item.getEnchantLevel() + " have augment:");
			info.append("\n");
			info.append("Option id 1 : " + item.getVariationStat1());
			info.append("\n");
			info.append("Option id 2 : " + item.getVariationStat2());
			getInfo(info, item.getVariationStat1());
			getInfo(info, item.getVariationStat2());
			player.sendMessage(info.toString());
		}
		return true;
	}
	
	private void getInfo(StringBuilder info, int id)
	{
		OptionDataTemplate template = OptionDataHolder.getInstance().getTemplate(id);
		if(template != null)
		{
			if(!template.getSkills().isEmpty())
			{
				for(Skill s : template.getSkills())
				{
					info.append(" ");
					info.append("\n");
					info.append("Skill name: " + s.getName() + " (id: " + s.getId() + ") - level " + s.getLevel());
					info.append("\n");
				}
			}
			if(!template.getTriggerList().isEmpty())
			{
				for(TriggerInfo t : template.getTriggerList())
				{
					info.append("\n");
					info.append("Chance skill id: " + t.id);
					info.append(" - level " + t.level);
					info.append("\n");
					info.append("Activation type " + t.getType());
					info.append("\n");
					info.append("Activation chance : " + t.getChance() + "%");
				}
			}
		}
	}
}