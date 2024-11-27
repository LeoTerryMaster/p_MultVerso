package l2.gameserver.listener.actor.player.impl;

import l2.commons.lang.reference.HardReference;
import l2.gameserver.listener.actor.player.OnAnswerListener;
import l2.gameserver.model.Player;
import l2.gameserver.scripts.Scripts;

public class ScriptAnswerListener implements OnAnswerListener
{
	private final HardReference<Player> _playerRef;
	private final String _scriptName;
	private final Object[] _arg;
	private final long _endTime;
	
	public ScriptAnswerListener(Player player, String scriptName, Object[] arg, long time)
	{
		_scriptName = scriptName;
		_arg = arg;
		_playerRef = player.getRef();
		_endTime = System.currentTimeMillis() + time;
	}
	
	@Override
	public void sayYes()
	{
		Player player = _playerRef.get();
		if(player == null || System.currentTimeMillis() > _endTime)
		{
			return;
		}
		Scripts.getInstance().callScripts(player, _scriptName.split(":")[0], _scriptName.split(":")[1], _arg);
	}
	
	@Override
	public void sayNo()
	{
	}
}