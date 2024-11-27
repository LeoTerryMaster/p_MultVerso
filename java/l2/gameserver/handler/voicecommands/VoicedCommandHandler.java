package l2.gameserver.handler.voicecommands;

import l2.commons.data.xml.AbstractHolder;
import l2.gameserver.Config;
import l2.gameserver.handler.voicecommands.impl.*;

import java.util.HashMap;
import java.util.Map;

public class VoicedCommandHandler extends AbstractHolder
{
	private static final VoicedCommandHandler _instance = new VoicedCommandHandler();
	private final Map<String, IVoicedCommandHandler> _datatable = new HashMap<>();
	
	private VoicedCommandHandler()
	{
		registerVoicedCommandHandler(new Offline());
		registerVoicedCommandHandler(new Online());
		registerVoicedCommandHandler(new ServerInfo());
		registerVoicedCommandHandler(new Wedding());
		registerVoicedCommandHandler(new Services());
		registerVoicedCommandHandler(new WhoAmI());
		registerVoicedCommandHandler(new Help());
		if(Config.ALT_ALLOW_MENU_COMMAND)
		{
			registerVoicedCommandHandler(new Cfg());
		}
		registerVoicedCommandHandler(new CWHPrivileges());
		registerVoicedCommandHandler(new Augments());
		registerVoicedCommandHandler(new Relocate());
		registerVoicedCommandHandler(new Banking());
	}
	
	public static VoicedCommandHandler getInstance()
	{
		return _instance;
	}
	
	public void registerVoicedCommandHandler(IVoicedCommandHandler handler)
	{
		String[] ids = handler.getVoicedCommandList();
		for(String element : ids)
		{
			_datatable.put(element, handler);
		}
	}
	
	public IVoicedCommandHandler getVoicedCommandHandler(String voicedCommand)
	{
		String command = voicedCommand;
		if(voicedCommand.indexOf(" ") != -1)
		{
			command = voicedCommand.substring(0, voicedCommand.indexOf(" "));
		}
		return _datatable.get(command);
	}
	
	@Override
	public int size()
	{
		return _datatable.size();
	}
	
	@Override
	public void clear()
	{
		_datatable.clear();
	}
}