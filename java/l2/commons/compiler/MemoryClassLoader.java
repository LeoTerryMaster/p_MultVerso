package l2.commons.compiler;

import java.util.HashMap;
import java.util.Map;

public class MemoryClassLoader extends ClassLoader
{
	private final Map<String, MemoryByteCode> classes = new HashMap<>();
	private final Map<String, MemoryByteCode> loaded = new HashMap<>();
	
	@Override
	protected Class<?> findClass(String name) throws ClassNotFoundException
	{
		MemoryByteCode mbc = classes.get(name);
		if(mbc == null && (mbc = classes.get(name)) == null)
		{
			return super.findClass(name);
		}
		return defineClass(name, mbc.getBytes(), 0, mbc.getBytes().length);
	}
	
	public void addClass(MemoryByteCode mbc)
	{
		classes.put(mbc.getName(), mbc);
		loaded.put(mbc.getName(), mbc);
	}
	
	public MemoryByteCode getClass(String name)
	{
		return classes.get(name);
	}
	
	public String[] getLoadedClasses()
	{
		return loaded.keySet().toArray(new String[loaded.size()]);
	}
	
	public void clear()
	{
		loaded.clear();
	}
}