package l2.commons.versioning;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.jar.Attributes;
import java.util.jar.JarFile;

public class Version
{
	private static final Logger _log = LoggerFactory.getLogger(Version.class);
	private String _revisionNumber = "exported";
	private String _versionNumber = "-1";
	private String _buildDate = "";
	private String _buildJdk = "";
	
	public Version(Class<?> c)
	{
		File jarName = null;
		try
		{
			jarName = Locator.getClassSource(c);
			JarFile jarFile = new JarFile(jarName);
			Attributes attrs = jarFile.getManifest().getMainAttributes();
			setBuildJdk(attrs);
			setBuildDate(attrs);
			setRevisionNumber(attrs);
			setVersionNumber(attrs);
		}
		catch(IOException e)
		{
			_log.error("Unable to get soft information\nFile name '" + (jarName == null ? "null" : jarName.getAbsolutePath()) + "' isn't a valid jar", e);
		}
	}
	
	public String getRevisionNumber()
	{
		return _revisionNumber;
	}
	
	private void setRevisionNumber(Attributes attrs)
	{
		String revisionNumber = attrs.getValue("Implementation-Build");
		_revisionNumber = revisionNumber != null ? revisionNumber : "-1";
	}
	
	public String getVersionNumber()
	{
		return _versionNumber;
	}
	
	private void setVersionNumber(Attributes attrs)
	{
		String versionNumber = attrs.getValue("Implementation-Version");
		_versionNumber = versionNumber != null ? versionNumber : "-1";
	}
	
	public String getBuildDate()
	{
		return _buildDate;
	}
	
	private void setBuildDate(Attributes attrs)
	{
		String buildDate = attrs.getValue("Build-Date");
		_buildDate = buildDate != null ? buildDate : "-1";
	}
	
	public String getBuildJdk()
	{
		return _buildJdk;
	}
	
	private void setBuildJdk(Attributes attrs)
	{
		String buildJdk = attrs.getValue("Build-Jdk");
		_buildJdk = buildJdk != null ? buildJdk : (buildJdk = attrs.getValue("Created-By")) != null ? buildJdk : "-1";
	}
}