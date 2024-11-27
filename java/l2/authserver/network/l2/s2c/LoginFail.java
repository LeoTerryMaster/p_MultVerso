package l2.authserver.network.l2.s2c;

public final class LoginFail extends L2LoginServerPacket
{
	private final int reason_code;
	
	public LoginFail(LoginFailReason reason)
	{
		reason_code = reason.getCode();
	}
	
	@Override
	protected void writeImpl()
	{
		writeC(1);
		writeD(reason_code);
	}
	
	public enum LoginFailReason
	{
		REASON_NO_MESSAGE(0),
		REASON_SYSTEM_ERROR(1),
		REASON_PASS_WRONG(2),
		REASON_USER_OR_PASS_WRONG(3),
		REASON_ACCESS_FAILED_TRYA1(4),
		REASON_ACCOUNT_INFO_INCORR(5),
		REASON_ACCESS_FAILED_TRYA2(6),
		REASON_ACCOUNT_IN_USE(7),
		REASON_MIN_AGE(12),
		REASON_SERVER_MAINTENANCE(16),
		REASON_CHANGE_TEMP_PASS(17),
		REASON_USAGE_TEMP_EXPIRED(18),
		REASON_TIME_LEFT_EXPIRED(19),
		REASON_SYS_ERR(20),
		REASON_ACCESS_FAILED(21),
		REASON_ATTEMPTED_RESTRICTED_IP(22),
		REASON_WEEK_USAGE_TIME_END(30),
		REASON_SECURITY_CARD_NUMB_I(31),
		REASON_VERIFY_AGE(32),
		REASON_CANNOT_ACC_COUPON(33),
		REASON_DUAL_BOX(35),
		REASON_ACCOUNT_INACTIVE(36),
		REASON_USER_AGREEMENT_DIS(37),
		REASON_GUARDIAN_CONSENT_REQ(38),
		REASON_USER_AGREEMENT_DEC(39),
		REASON_ACCOUNT_SUSPENDED(40),
		REASON_CHANGE_PASS_AND_QUIZ(41),
		REASON_LOGGED_INTO_10_ACCS(42);
		
		private final int _code;
		
		LoginFailReason(int code)
		{
			_code = code;
		}
		
		public final int getCode()
		{
			return _code;
		}
	}
}