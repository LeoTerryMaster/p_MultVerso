package l2.authserver.network.l2.s2c;

public final class AccountKicked extends L2LoginServerPacket
{
	private final int reason;
	
	public AccountKicked(AccountKickedReason reason)
	{
		this.reason = reason.getCode();
	}
	
	@Override
	protected void writeImpl()
	{
		writeC(2);
		writeD(reason);
	}
	
	public enum AccountKickedReason
	{
		REASON_FALSE_DATA_STEALER_REPORT(0),
		REASON_DATA_STEALER(1),
		REASON_SOUSPICION_DATA_STEALER(3),
		REASON_NON_PAYEMENT_CELL_PHONE(4),
		REASON_30_DAYS_SUSPENDED_CASH(8),
		REASON_PERMANENTLY_SUSPENDED_CASH(16),
		REASON_PERMANENTLY_BANNED(32),
		REASON_ACCOUNT_MUST_BE_VERIFIED(64);
		
		private final int _code;
		
		AccountKickedReason(int code)
		{
			_code = code;
		}
		
		public final int getCode()
		{
			return _code;
		}
	}
}