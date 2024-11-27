package l2.gameserver.listener.reflection;

import l2.commons.listener.Listener;
import l2.gameserver.model.entity.Reflection;

public interface OnReflectionCollapseListener extends Listener<Reflection>
{
	void onReflectionCollapse(Reflection reflection);
}