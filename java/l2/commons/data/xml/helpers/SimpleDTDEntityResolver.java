package l2.commons.data.xml.helpers;

import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import java.io.File;
import java.io.IOException;

public class SimpleDTDEntityResolver implements EntityResolver
{
	private final String _fileName;
	
	public SimpleDTDEntityResolver(File f)
	{
		_fileName = f.getAbsolutePath();
	}
	
	@Override
	public InputSource resolveEntity(String publicId, String systemId) throws SAXException, IOException
	{
		return new InputSource(_fileName);
	}
}