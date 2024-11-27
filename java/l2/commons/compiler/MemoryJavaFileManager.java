package l2.commons.compiler;

import javax.tools.FileObject;
import javax.tools.ForwardingJavaFileManager;
import javax.tools.JavaFileManager;
import javax.tools.JavaFileObject;
import javax.tools.StandardJavaFileManager;
import java.io.IOException;
import java.net.URI;

public class MemoryJavaFileManager extends ForwardingJavaFileManager<StandardJavaFileManager>
{
	private final MemoryClassLoader cl;
	
	public MemoryJavaFileManager(StandardJavaFileManager sjfm, MemoryClassLoader xcl)
	{
		super(sjfm);
		cl = xcl;
	}
	
	@Override
	public JavaFileObject getJavaFileForOutput(JavaFileManager.Location location, String className, JavaFileObject.Kind kind, FileObject sibling) throws IOException
	{
		MemoryByteCode mbc = new MemoryByteCode(className.replace('/', '.').replace('\\', '.'), URI.create("file:///" + className.replace('.', '/').replace('\\', '/') + kind.extension));
		cl.addClass(mbc);
		return mbc;
	}
	
	@Override
	public ClassLoader getClassLoader(JavaFileManager.Location location)
	{
		return cl;
	}
}