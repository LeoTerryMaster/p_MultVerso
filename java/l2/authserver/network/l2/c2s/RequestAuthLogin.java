package l2.authserver.network.l2.c2s;

import l2.authserver.Config;
import l2.authserver.IpBanManager;
import l2.authserver.accounts.Account;
import l2.authserver.accounts.SessionManager;
import l2.authserver.crypt.PasswordHash;
import l2.authserver.network.l2.L2LoginClient;
import l2.authserver.network.l2.s2c.LoginFail;
import l2.authserver.network.l2.s2c.LoginOk;
import l2.authserver.utils.Log;

import javax.crypto.Cipher;

public class RequestAuthLogin extends L2LoginClientPacket
{
	private final byte[] _raw = new byte[128];
	
	@Override
	protected void readImpl()
	{
		readB(_raw);
		readD();
		readD();
		readD();
		readD();
		readD();
		readD();
		readH();
		readC();
	}
	
	@Override
	protected void runImpl() throws Exception
	{
		byte[] decrypted;
		L2LoginClient client = getClient();
		try
		{
			Cipher rsaCipher = Cipher.getInstance("RSA/ECB/nopadding");
			rsaCipher.init(2, client.getRSAPrivateKey());
			decrypted = rsaCipher.doFinal(_raw, 0, 128);
		}
		catch(Exception e)
		{
			client.closeNow(true);
			return;
		}
		String user = new String(decrypted, 94, 14).trim();
		user = user.toLowerCase();
		String password = new String(decrypted, 108, 16).trim();
		int ncotp = decrypted[124] & 255;
		ncotp |= (decrypted[125] & 255) << 8;
		ncotp |= (decrypted[126] & 255) << 16;
		ncotp |= (decrypted[127] & 255) << 24;
		int currentTime = (int) (System.currentTimeMillis() / 1000);
		Account account = new Account(user);
		account.restore();
		String passwordHash = Config.DEFAULT_CRYPT.encrypt(password);
		if(account.getPasswordHash() == null)
		{
			if(Config.AUTO_CREATE_ACCOUNTS && user.matches(Config.ANAME_TEMPLATE) && password.matches(Config.APASSWD_TEMPLATE))
			{
				account.setPasswordHash(passwordHash);
				account.save();
			}
			else
			{
				client.close(LoginFail.LoginFailReason.REASON_USER_OR_PASS_WRONG);
				return;
			}
		}
		boolean passwordCorrect;
		if(!(passwordCorrect = account.getPasswordHash().equalsIgnoreCase(passwordHash)))
		{
			for(PasswordHash c : Config.LEGACY_CRYPT)
			{
				if(!c.compare(password, account.getPasswordHash()))
					continue;
				passwordCorrect = true;
				account.setPasswordHash(passwordHash);
				break;
			}
		}
		if(!IpBanManager.getInstance().tryLogin(client.getIpAddress(), passwordCorrect))
		{
			client.closeNow(false);
			return;
		}
		if(!passwordCorrect)
		{
			client.close(LoginFail.LoginFailReason.REASON_USER_OR_PASS_WRONG);
			return;
		}
		if(account.getAccessLevel() < 0)
		{
			client.close(LoginFail.LoginFailReason.REASON_ACCESS_FAILED);
			return;
		}
		if(account.getBanExpire() > currentTime)
		{
			client.close(LoginFail.LoginFailReason.REASON_ACCESS_FAILED);
			return;
		}
		if(!account.isAllowedIP(client.getIpAddress()))
		{
			client.close(LoginFail.LoginFailReason.REASON_ATTEMPTED_RESTRICTED_IP);
			return;
		}
		account.setLastAccess(currentTime);
		account.setLastIP(client.getIpAddress());
		Log.LogAccount(account);
		SessionManager.Session session = SessionManager.getInstance().openSession(account);
		client.setAuthed(true);
		client.setLogin(user);
		client.setAccount(account);
		client.setSessionKey(session.getSessionKey());
		client.setState(L2LoginClient.LoginClientState.AUTHED);
		client.sendPacket(new LoginOk(client.getSessionKey()));
	}
}