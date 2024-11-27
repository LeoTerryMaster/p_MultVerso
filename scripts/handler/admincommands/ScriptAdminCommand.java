package handler.admincommands;

import l2.gameserver.handler.admincommands.AdminCommandHandler;
import l2.gameserver.handler.admincommands.IAdminCommandHandler;
import l2.gameserver.scripts.ScriptFile;

public abstract class ScriptAdminCommand implements IAdminCommandHandler, ScriptFile
{
	@Override
	public void onLoad()
	{
		AdminCommandHandler.getInstance().registerAdminCommandHandler(this);
	}
	
	@Override
	public void onReload()
	{
	}
	
	@Override
	public void onShutdown()
	{
	}
}