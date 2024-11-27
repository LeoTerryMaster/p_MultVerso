package l2.authserver.accounts;

import l2.authserver.database.L2DatabaseFactory;
import l2.commons.dbutils.DbUtils;
import l2.commons.net.utils.NetList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;

public class Account
{
	private static final Logger _log = LoggerFactory.getLogger(Account.class);
	private static final String SQLP_ACCOUNT_LOAD = "{CALL `lip_AccountLoad`(?)}";
	private static final String SQLP_ACCOUNT_CREATE = "{CALL `lip_AccountCreate`(?, ?)}";
	private static final String SQLP_ACCOUNT_UPDATE = "{CALL `lip_AccountUpdate`(?, ?, ?, ?, ?, ?, ?)}";
	private final String login;
	private final NetList allowedIpList = new NetList();
	private String passwordHash;
	private int accessLevel;
	private int banExpire;
	private String lastIP;
	private int lastAccess;
	private int lastServer;
	private String email;
	
	public Account(String login)
	{
		this.login = login;
	}
	
	public String getLogin()
	{
		return login;
	}
	
	public String getEmail()
	{
		return email;
	}
	
	public void setEmail(String val)
	{
		email = val;
	}
	
	public String getPasswordHash()
	{
		return passwordHash;
	}
	
	public void setPasswordHash(String passwordHash)
	{
		this.passwordHash = passwordHash;
	}
	
	public boolean isAllowedIP(String ip)
	{
		return allowedIpList.isEmpty() || allowedIpList.isInRange(ip);
	}
	
	public int getAccessLevel()
	{
		return accessLevel;
	}
	
	public void setAccessLevel(int accessLevel)
	{
		this.accessLevel = accessLevel;
	}
	
	public int getBanExpire()
	{
		return banExpire;
	}
	
	public void setBanExpire(int banExpire)
	{
		this.banExpire = banExpire;
	}
	
	public String getLastIP()
	{
		return lastIP;
	}
	
	public void setLastIP(String lastIP)
	{
		this.lastIP = lastIP;
	}
	
	public int getLastAccess()
	{
		return lastAccess;
	}
	
	public void setLastAccess(int lastAccess)
	{
		this.lastAccess = lastAccess;
	}
	
	public int getLastServer()
	{
		return lastServer;
	}
	
	public void setLastServer(int lastServer)
	{
		this.lastServer = lastServer;
	}
	
	@Override
	public String toString()
	{
		return login;
	}
	
	public void restore()
	{
		Connection con = null;
		CallableStatement cstmt = null;
		ResultSet rset = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			cstmt = con.prepareCall("{CALL `lip_AccountLoad`(?)}");
			cstmt.setString(1, login);
			rset = cstmt.executeQuery();
			if(rset.next())
			{
				setPasswordHash(rset.getString("password").trim());
				setAccessLevel(rset.getInt("accessLevel"));
				setLastServer(rset.getInt("lastServerId"));
				setLastIP(rset.getString("lastIP"));
				setLastAccess(rset.getInt("lastactive"));
				setEmail(rset.getString("email"));
			}
		}
		catch(Exception e)
		{
			_log.error("", e);
		}
		finally
		{
			DbUtils.closeQuietly(con, cstmt, rset);
		}
	}
	
	public void save()
	{
		Connection con = null;
		CallableStatement cstmt = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			cstmt = con.prepareCall("{CALL `lip_AccountCreate`(?, ?)}");
			cstmt.setString(1, getLogin());
			cstmt.setString(2, getPasswordHash());
			cstmt.execute();
		}
		catch(Exception e)
		{
			_log.error("", e);
		}
		finally
		{
			DbUtils.closeQuietly(con, cstmt);
		}
	}
	
	public void update()
	{
		Connection con = null;
		CallableStatement cstmt = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			cstmt = con.prepareCall("{CALL `lip_AccountUpdate`(?, ?, ?, ?, ?, ?, ?)}");
			cstmt.setString(1, getLogin());
			cstmt.setString(2, getPasswordHash());
			cstmt.setInt(3, getAccessLevel());
			cstmt.setInt(4, getLastServer());
			cstmt.setString(5, getLastIP());
			cstmt.setInt(6, getLastAccess());
			cstmt.setString(7, getEmail());
			cstmt.execute();
		}
		catch(Exception e)
		{
			_log.error("", e);
		}
		finally
		{
			DbUtils.closeQuietly(con, cstmt);
		}
	}
}