package l2.gameserver.scripts;

public interface ScriptFile
{
	void onLoad();
	
	void onReload();
	
	void onShutdown();
}