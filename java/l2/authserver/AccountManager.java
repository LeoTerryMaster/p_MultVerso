package l2.authserver;

import l2.authserver.accounts.Account;
import l2.authserver.database.L2DatabaseFactory;

public class AccountManager
{
	private static final void printUsage()
	{
		System.out.println("Usage: ");
		System.out.println(" -c <name> <password>\t Creates account <name> with password <password>");
		System.out.println(" -p <name> <password>\t Update <name>'s password to <password>");
	}
	
	public static void main(String... args) throws Throwable
	{
		Config.load();
		L2DatabaseFactory.getInstance().getConnection().close();
		Config.initCrypt();
		if(args.length > 2 && "-c".equalsIgnoreCase(args[0]))
		{
			String login = args[1].trim();
			String password = args[2].trim();
			System.out.println("Creating account \"" + login + "\" with password \"" + password + "\"");
			Account account = new Account(login);
			account.restore();
			if(account.getPasswordHash() != null)
			{
				System.err.println("Account \"" + login + "\" already exists");
				System.exit(-1);
				return;
			}
			account.setPasswordHash(Config.DEFAULT_CRYPT.encrypt(password));
			account.setAccessLevel(0);
			account.save();
			System.out.println("Account \"" + account.getLogin() + "\" created.");
			System.exit(0);
		}
		else if(args.length > 2 && "-p".equalsIgnoreCase(args[0]))
		{
			String login = args[1].trim();
			String password = args[2].trim();
			System.out.println("Set account \"" + login + "\" password to \"" + password + "\"");
			Account account = new Account(login);
			account.restore();
			if(account.getPasswordHash() == null)
			{
				System.err.println("Account \"" + login + "\" dose not exists");
				System.exit(-1);
				return;
			}
			account.setPasswordHash(Config.DEFAULT_CRYPT.encrypt(password));
			account.setAccessLevel(0);
			account.update();
			System.out.println("Account \"" + account.getLogin() + "\" password set to \"" + password + "\".");
			System.exit(0);
		}
		else
		{
			printUsage();
			System.exit(0);
		}
	}
}