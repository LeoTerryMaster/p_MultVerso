package handler.usercommands;

import l2.gameserver.handler.usercommands.IUserCommandHandler;
import l2.gameserver.handler.usercommands.UserCommandHandler;
import l2.gameserver.scripts.ScriptFile;

public abstract class ScriptUserCommand implements IUserCommandHandler, ScriptFile
{
	@Override
	public void onLoad()
	{
		UserCommandHandler.getInstance().registerUserCommandHandler(this);
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