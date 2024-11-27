package l2.commons.compiler;

import javax.tools.JavaFileObject;
import javax.tools.SimpleJavaFileObject;
import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.net.URI;

public class MemoryByteCode extends SimpleJavaFileObject
{
	private final String className;
	private ByteArrayOutputStream oStream;
	
	public MemoryByteCode(String className, URI uri)
	{
		super(uri, JavaFileObject.Kind.CLASS);
		this.className = className;
	}
	
	@Override
	public OutputStream openOutputStream()
	{
		oStream = new ByteArrayOutputStream();
		return oStream;
	}
	
	public byte[] getBytes()
	{
		return oStream.toByteArray();
	}
	
	@Override
	public String getName()
	{
		return className;
	}
}