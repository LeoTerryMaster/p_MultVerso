package l2.gameserver.scripts;

import l2.commons.compiler.Compiler;
import l2.commons.compiler.MemoryClassLoader;
import l2.gameserver.Config;
import l2.gameserver.model.Player;
import l2.gameserver.model.quest.Quest;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.filefilter.FileFilterUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.ClassUtils;
import org.apache.commons.lang3.reflect.FieldUtils;
import org.apache.commons.lang3.reflect.MethodUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;

public class Scripts
{
	public static final Map<Integer, List<ScriptClassAndMethod>> dialogAppends = new HashMap<>();
	public static final Map<String, ScriptClassAndMethod> onAction = new HashMap<>();
	public static final Map<String, ScriptClassAndMethod> onActionShift = new HashMap<>();
	private static final Logger _log = LoggerFactory.getLogger(Scripts.class);
	private static final Scripts _instance = new Scripts();
	private final Compiler compiler = new Compiler();
	private final Map<String, Class<?>> _classes = new TreeMap();
	
	private Scripts()
	{
		load();
		loadExt();
	}
	
	public static final Scripts getInstance()
	{
		return _instance;
	}
	
	private void load()
	{
		_log.info("Scripts: Loading...");
		ArrayList classes = new ArrayList();
		boolean result = false;
		File f = new File(".././libs/scripts.jar");
		if(f.exists())
		{
			JarInputStream stream = null;
			MemoryClassLoader classLoader = new MemoryClassLoader();
			try
			{
				stream = new JarInputStream(new FileInputStream(f));
				JarEntry entry = null;
				while((entry = stream.getNextJarEntry()) != null)
				{
					if(!entry.getName().contains(ClassUtils.INNER_CLASS_SEPARATOR) && entry.getName().endsWith(".class"))
					{
						String name = entry.getName().replace(".class", "").replace("/", ".");
						Class<?> clazz = classLoader.loadClass(name);
						if(!Modifier.isAbstract(clazz.getModifiers()))
						{
							classes.add(clazz);
						}
					}
				}
				result = true;
			}
			catch(Exception e)
			{
				_log.error("Fail to load scripts.jar!", e);
				classes.clear();
			}
			finally
			{
				IOUtils.closeQuietly(stream);
			}
		}
		if(!result)
		{
			result = load(classes, "");
		}
		if(!result)
		{
			_log.error("Scripts: Failed loading scripts!");
			Runtime.getRuntime().exit(0);
			return;
		}
		_log.info("Scripts: Loaded " + classes.size() + " classes.");
		for(int i = 0;i < classes.size();++i)
		{
			Class clazz = (Class) classes.get(i);
			_classes.put(clazz.getName(), clazz);
		}
	}
	
	private void loadExt()
	{
		_log.info("Extensions: Loading...");
		ArrayList classes = new ArrayList();
		boolean result = false;
		File[] extFiles = new File(".").listFiles(pathname -> pathname.getName().endsWith(".ext.jar"));
		for(File extFile : extFiles)
		{
			if(!extFile.exists())
				continue;
			JarInputStream stream = null;
			MemoryClassLoader classLoader = new MemoryClassLoader();
			try
			{
				stream = new JarInputStream(new FileInputStream(extFile));
				JarEntry entry = null;
				while((entry = stream.getNextJarEntry()) != null)
				{
					if(!entry.getName().startsWith("java/") && !entry.getName().startsWith("l2/authserver") && !entry.getName().startsWith("l2/commons") && !entry.getName().startsWith("l2/gameserver") && !entry.getName().contains(ClassUtils.INNER_CLASS_SEPARATOR) && entry.getName().endsWith(".class"))
					{
						String name = entry.getName().replace(".class", "").replace("/", ".");
						Class<?> clazz = classLoader.loadClass(name);
						if(!Modifier.isAbstract(clazz.getModifiers()))
						{
							classes.add(clazz);
						}
					}
				}
				result = true;
			}
			catch(Exception e)
			{
				_log.error("Failed to load \"" + extFile + "\"!", e);
				classes.clear();
			}
			finally
			{
				IOUtils.closeQuietly(stream);
			}
		}
		if(!result)
		{
			load(classes, "");
		}
		_log.info("Extensions: Loaded " + classes.size() + " extension classes.");
		for(int i = 0;i < classes.size();++i)
		{
			Class clazz = (Class) classes.get(i);
			_classes.put(clazz.getName(), clazz);
		}
	}
	
	public void init()
	{
		for(Class clazz : _classes.values())
		{
			addHandlers(clazz);
			if(Config.DONTLOADQUEST && ClassUtils.isAssignable(clazz, Quest.class) || !ClassUtils.isAssignable(clazz, ScriptFile.class))
				continue;
			try
			{
				((ScriptFile) clazz.newInstance()).onLoad();
			}
			catch(Exception e)
			{
				_log.error("Scripts: Failed running " + clazz.getName() + ".onLoad()", e);
			}
		}
	}
	
	public boolean reload()
	{
		_log.info("Scripts: Reloading...");
		return reload("");
	}
	
	public boolean reload(String target)
	{
		ArrayList classes = new ArrayList();
		if(!load(classes, target))
		{
			_log.error("Scripts: Failed reloading script(s): " + target + "!");
			return false;
		}
		_log.info("Scripts: Reloaded " + classes.size() + " classes.");
		for(int i = 0;i < classes.size();++i)
		{
			Class clazz = (Class) classes.get(i);
			Class prevClazz = _classes.put(clazz.getName(), clazz);
			if(prevClazz != null)
			{
				if(ClassUtils.isAssignable(prevClazz, ScriptFile.class))
				{
					try
					{
						((ScriptFile) prevClazz.newInstance()).onReload();
					}
					catch(Exception e)
					{
						_log.error("Scripts: Failed running " + prevClazz.getName() + ".onReload()", e);
					}
				}
				removeHandlers(prevClazz);
			}
			if(Config.DONTLOADQUEST && ClassUtils.isAssignable(clazz, Quest.class))
				continue;
			if(ClassUtils.isAssignable(clazz, ScriptFile.class))
			{
				try
				{
					((ScriptFile) clazz.newInstance()).onLoad();
				}
				catch(Exception e)
				{
					_log.error("Scripts: Failed running " + clazz.getName() + ".onLoad()", e);
				}
			}
			addHandlers(clazz);
		}
		return true;
	}
	
	public void shutdown()
	{
		for(Class clazz : _classes.values())
		{
			if(ClassUtils.isAssignable(clazz, Quest.class) || !ClassUtils.isAssignable(clazz, ScriptFile.class))
				continue;
			try
			{
				((ScriptFile) clazz.newInstance()).onShutdown();
			}
			catch(Exception e)
			{
				_log.error("Scripts: Failed running " + clazz.getName() + ".onShutdown()", e);
			}
		}
	}
	
	private boolean load(List<Class<?>> classes, String target)
	{
		Collection scriptFiles = Collections.emptyList();
		File file = new File(Config.DATAPACK_ROOT, "data/scripts/" + target.replace(".", "/") + ".java");
		if(file.isFile())
		{
			scriptFiles = new ArrayList(1);
			scriptFiles.add(file);
		}
		else
		{
			file = new File(Config.DATAPACK_ROOT, "data/scripts/" + target);
			if(file.isDirectory())
			{
				scriptFiles = FileUtils.listFiles(file, FileFilterUtils.suffixFileFilter(".java"), FileFilterUtils.directoryFileFilter());
			}
		}
		if(scriptFiles.isEmpty())
		{
			return false;
		}
		boolean success = compiler.compile(scriptFiles);
		if(success)
		{
			MemoryClassLoader classLoader = compiler.getClassLoader();
			for(String name : classLoader.getLoadedClasses())
			{
				if(name.contains(ClassUtils.INNER_CLASS_SEPARATOR))
					continue;
				try
				{
					Class clazz = classLoader.loadClass(name);
					if(Modifier.isAbstract(clazz.getModifiers()))
						continue;
					classes.add(clazz);
				}
				catch(ClassNotFoundException e)
				{
					success = false;
					_log.error("Scripts: Can't load script class: " + name, e);
				}
			}
			classLoader.clear();
		}
		return success;
	}
	
	private void addHandlers(Class<?> clazz)
	{
		try
		{
			for(Method method : clazz.getMethods())
			{
				if(method.getName().contains("DialogAppend_"))
				{
					Integer id = Integer.parseInt(method.getName().substring(13));
					List<ScriptClassAndMethod> handlers = dialogAppends.get(id);
					if(handlers == null)
					{
						handlers = new ArrayList<>();
						dialogAppends.put(id, handlers);
					}
					handlers.add(new ScriptClassAndMethod(clazz.getName(), method.getName()));
					continue;
				}
				String name;
				if(method.getName().contains("OnAction_"))
				{
					name = method.getName().substring(9);
					onAction.put(name, new ScriptClassAndMethod(clazz.getName(), method.getName()));
					continue;
				}
				if(!method.getName().contains("OnActionShift_"))
					continue;
				name = method.getName().substring(14);
				onActionShift.put(name, new ScriptClassAndMethod(clazz.getName(), method.getName()));
			}
		}
		catch(Exception e)
		{
			_log.error("", e);
		}
	}
	
	private void removeHandlers(Class<?> script)
	{
		try
		{
			for(List<ScriptClassAndMethod> entry : dialogAppends.values())
			{
				ArrayList<ScriptClassAndMethod> toRemove = new ArrayList<>();
				for(ScriptClassAndMethod sc : entry)
				{
					if(!sc.className.equals(script.getName()))
						continue;
					toRemove.add(sc);
				}
				for(ScriptClassAndMethod sc : toRemove)
				{
					entry.remove(sc);
				}
			}
			ArrayList<String> toRemove = new ArrayList<>();
			for(Map.Entry<String, ScriptClassAndMethod> entry : onAction.entrySet())
			{
				if(!entry.getValue().className.equals(script.getName()))
					continue;
				toRemove.add(entry.getKey());
			}
			for(String key : toRemove)
			{
				onAction.remove(key);
			}
			toRemove = new ArrayList();
			for(Map.Entry<String, ScriptClassAndMethod> entry : onActionShift.entrySet())
			{
				if(!entry.getValue().className.equals(script.getName()))
					continue;
				toRemove.add(entry.getKey());
			}
			for(String key : toRemove)
			{
				onActionShift.remove(key);
			}
		}
		catch(Exception e)
		{
			_log.error("", e);
		}
	}
	
	public Object callScripts(String className, String methodName)
	{
		return callScripts(null, className, methodName, null, null);
	}
	
	public Object callScripts(String className, String methodName, Object[] args)
	{
		return callScripts(null, className, methodName, args, null);
	}
	
	public Object callScripts(String className, String methodName, Map<String, Object> variables)
	{
		return callScripts(null, className, methodName, ArrayUtils.EMPTY_OBJECT_ARRAY, variables);
	}
	
	public Object callScripts(String className, String methodName, Object[] args, Map<String, Object> variables)
	{
		return callScripts(null, className, methodName, args, variables);
	}
	
	public Object callScripts(Player caller, String className, String methodName)
	{
		return callScripts(caller, className, methodName, ArrayUtils.EMPTY_OBJECT_ARRAY, null);
	}
	
	public Object callScripts(Player caller, String className, String methodName, Object[] args)
	{
		return callScripts(caller, className, methodName, args, null);
	}
	
	public Object callScripts(Player caller, String className, String methodName, Map<String, Object> variables)
	{
		return callScripts(caller, className, methodName, ArrayUtils.EMPTY_OBJECT_ARRAY, variables);
	}
	
	public Object callScripts(Player caller, String className, String methodName, Object[] args, Map<String, Object> variables)
	{
		Class clazz = _classes.get(className);
		if(clazz == null)
		{
			_log.error("Script class " + className + " not found!");
			return null;
		}
		Object o;
		try
		{
			o = clazz.newInstance();
		}
		catch(Exception e)
		{
			_log.error("Scripts: Failed creating instance of " + clazz.getName(), e);
			return null;
		}
		if(variables != null && !variables.isEmpty())
		{
			for(Map.Entry<String, Object> param : variables.entrySet())
			{
				try
				{
					FieldUtils.writeField(o, param.getKey(), param.getValue());
				}
				catch(Exception e)
				{
					_log.error("Scripts: Failed setting fields for " + clazz.getName(), e);
				}
			}
		}
		if(caller != null)
		{
			try
			{
				Field field = FieldUtils.getField(clazz, "self");
				if(field != null)
				{
					FieldUtils.writeField(field, o, caller.getRef());
				}
			}
			catch(Exception e)
			{
				_log.error("Scripts: Failed setting field for " + clazz.getName(), e);
			}
		}
		Object ret = null;
		try
		{
			Class[] parameterTypes = new Class[args.length];
			for(int i = 0;i < args.length;++i)
			{
				parameterTypes[i] = args[i] != null ? args[i].getClass() : null;
			}
			ret = MethodUtils.invokeMethod(o, methodName, args, parameterTypes);
		}
		catch(NoSuchMethodException e)
		{
			_log.error("Scripts: No such method " + clazz.getName() + "." + methodName + "()!");
		}
		catch(InvocationTargetException e)
		{
			_log.error("Scripts: Error while calling " + clazz.getName() + "." + methodName + "()", e.getTargetException());
		}
		catch(Exception e)
		{
			_log.error("Scripts: Failed calling " + clazz.getName() + "." + methodName + "()", e);
		}
		return ret;
	}
	
	public Map<String, Class<?>> getClasses()
	{
		return _classes;
	}
	
	public static class ScriptClassAndMethod
	{
		public final String className;
		public final String methodName;
		
		public ScriptClassAndMethod(String className, String methodName)
		{
			this.className = className;
			this.methodName = methodName;
		}
	}
}