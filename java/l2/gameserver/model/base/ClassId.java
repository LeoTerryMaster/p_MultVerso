package l2.gameserver.model.base;

public enum ClassId
{
	fighter(0, false, Race.human, null, null, 1, null),
	warrior(1, false, Race.human, fighter, null, 2, null),
	gladiator(2, false, Race.human, warrior, null, 3, ClassType2.Warrior),
	warlord(3, false, Race.human, warrior, null, 3, ClassType2.Warrior),
	knight(4, false, Race.human, fighter, null, 2, null),
	paladin(5, false, Race.human, knight, null, 3, ClassType2.Knight),
	darkAvenger(6, false, Race.human, knight, null, 3, ClassType2.Knight),
	rogue(7, false, Race.human, fighter, null, 2, null),
	treasureHunter(8, false, Race.human, rogue, null, 3, ClassType2.Rogue),
	hawkeye(9, false, Race.human, rogue, null, 3, ClassType2.Rogue),
	mage(10, true, Race.human, null, null, 1, null),
	wizard(11, true, Race.human, mage, null, 2, null),
	sorceror(12, true, Race.human, wizard, null, 3, ClassType2.Wizard),
	necromancer(13, true, Race.human, wizard, null, 3, ClassType2.Wizard),
	warlock(14, true, Race.human, wizard, null, 3, ClassType2.Summoner),
	cleric(15, true, Race.human, mage, null, 2, null),
	bishop(16, true, Race.human, cleric, null, 3, ClassType2.Healer),
	prophet(17, true, Race.human, cleric, null, 3, ClassType2.Enchanter),
	elvenFighter(18, false, Race.elf, null, null, 1, null),
	elvenKnight(19, false, Race.elf, elvenFighter, null, 2, null),
	templeKnight(20, false, Race.elf, elvenKnight, null, 3, ClassType2.Knight),
	swordSinger(21, false, Race.elf, elvenKnight, null, 3, ClassType2.Enchanter),
	elvenScout(22, false, Race.elf, elvenFighter, null, 2, null),
	plainsWalker(23, false, Race.elf, elvenScout, null, 3, ClassType2.Rogue),
	silverRanger(24, false, Race.elf, elvenScout, null, 3, ClassType2.Rogue),
	elvenMage(25, true, Race.elf, null, null, 1, null),
	elvenWizard(26, true, Race.elf, elvenMage, null, 2, null),
	spellsinger(27, true, Race.elf, elvenWizard, null, 3, ClassType2.Wizard),
	elementalSummoner(28, true, Race.elf, elvenWizard, null, 3, ClassType2.Summoner),
	oracle(29, true, Race.elf, elvenMage, null, 2, null),
	elder(30, true, Race.elf, oracle, null, 3, ClassType2.Healer),
	darkFighter(31, false, Race.darkelf, null, null, 1, null),
	palusKnight(32, false, Race.darkelf, darkFighter, null, 2, null),
	shillienKnight(33, false, Race.darkelf, palusKnight, null, 3, ClassType2.Knight),
	bladedancer(34, false, Race.darkelf, palusKnight, null, 3, ClassType2.Enchanter),
	assassin(35, false, Race.darkelf, darkFighter, null, 2, null),
	abyssWalker(36, false, Race.darkelf, assassin, null, 3, ClassType2.Rogue),
	phantomRanger(37, false, Race.darkelf, assassin, null, 3, ClassType2.Rogue),
	darkMage(38, true, Race.darkelf, null, null, 1, null),
	darkWizard(39, true, Race.darkelf, darkMage, null, 2, null),
	spellhowler(40, true, Race.darkelf, darkWizard, null, 3, ClassType2.Wizard),
	phantomSummoner(41, true, Race.darkelf, darkWizard, null, 3, ClassType2.Summoner),
	shillienOracle(42, true, Race.darkelf, darkMage, null, 2, null),
	shillienElder(43, true, Race.darkelf, shillienOracle, null, 3, ClassType2.Healer),
	orcFighter(44, false, Race.orc, null, null, 1, null),
	orcRaider(45, false, Race.orc, orcFighter, null, 2, null),
	destroyer(46, false, Race.orc, orcRaider, null, 3, ClassType2.Warrior),
	orcMonk(47, false, Race.orc, orcFighter, null, 2, null),
	tyrant(48, false, Race.orc, orcMonk, null, 3, ClassType2.Warrior),
	orcMage(49, true, Race.orc, null, null, 1, null),
	orcShaman(50, true, Race.orc, orcMage, null, 2, null),
	overlord(51, true, Race.orc, orcShaman, null, 3, ClassType2.Enchanter),
	warcryer(52, true, Race.orc, orcShaman, null, 3, ClassType2.Enchanter),
	dwarvenFighter(53, false, Race.dwarf, null, null, 1, null),
	scavenger(54, false, Race.dwarf, dwarvenFighter, null, 2, null),
	bountyHunter(55, false, Race.dwarf, scavenger, null, 3, ClassType2.Warrior),
	artisan(56, false, Race.dwarf, dwarvenFighter, null, 2, null),
	warsmith(57, false, Race.dwarf, artisan, null, 3, ClassType2.Warrior),
	dummyEntry1(58, false, null, null, null, 0, null),
	dummyEntry2(59, false, null, null, null, 0, null),
	dummyEntry3(60, false, null, null, null, 0, null),
	dummyEntry4(61, false, null, null, null, 0, null),
	dummyEntry5(62, false, null, null, null, 0, null),
	dummyEntry6(63, false, null, null, null, 0, null),
	dummyEntry7(64, false, null, null, null, 0, null),
	dummyEntry8(65, false, null, null, null, 0, null),
	dummyEntry9(66, false, null, null, null, 0, null),
	dummyEntry10(67, false, null, null, null, 0, null),
	dummyEntry11(68, false, null, null, null, 0, null),
	dummyEntry12(69, false, null, null, null, 0, null),
	dummyEntry13(70, false, null, null, null, 0, null),
	dummyEntry14(71, false, null, null, null, 0, null),
	dummyEntry15(72, false, null, null, null, 0, null),
	dummyEntry16(73, false, null, null, null, 0, null),
	dummyEntry17(74, false, null, null, null, 0, null),
	dummyEntry18(75, false, null, null, null, 0, null),
	dummyEntry19(76, false, null, null, null, 0, null),
	dummyEntry20(77, false, null, null, null, 0, null),
	dummyEntry21(78, false, null, null, null, 0, null),
	dummyEntry22(79, false, null, null, null, 0, null),
	dummyEntry23(80, false, null, null, null, 0, null),
	dummyEntry24(81, false, null, null, null, 0, null),
	dummyEntry25(82, false, null, null, null, 0, null),
	dummyEntry26(83, false, null, null, null, 0, null),
	dummyEntry27(84, false, null, null, null, 0, null),
	dummyEntry28(85, false, null, null, null, 0, null),
	dummyEntry29(86, false, null, null, null, 0, null),
	dummyEntry30(87, false, null, null, null, 0, null),
	duelist(88, false, Race.human, gladiator, null, 4, ClassType2.Warrior),
	dreadnought(89, false, Race.human, warlord, null, 4, ClassType2.Warrior),
	phoenixKnight(90, false, Race.human, paladin, null, 4, ClassType2.Knight),
	hellKnight(91, false, Race.human, darkAvenger, null, 4, ClassType2.Knight),
	sagittarius(92, false, Race.human, hawkeye, null, 4, ClassType2.Rogue),
	adventurer(93, false, Race.human, treasureHunter, null, 4, ClassType2.Rogue),
	archmage(94, true, Race.human, sorceror, null, 4, ClassType2.Wizard),
	soultaker(95, true, Race.human, necromancer, null, 4, ClassType2.Wizard),
	arcanaLord(96, true, Race.human, warlock, null, 4, ClassType2.Summoner),
	cardinal(97, true, Race.human, bishop, null, 4, ClassType2.Healer),
	hierophant(98, true, Race.human, prophet, null, 4, ClassType2.Enchanter),
	evaTemplar(99, false, Race.elf, templeKnight, null, 4, ClassType2.Knight),
	swordMuse(100, false, Race.elf, swordSinger, null, 4, ClassType2.Enchanter),
	windRider(101, false, Race.elf, plainsWalker, null, 4, ClassType2.Rogue),
	moonlightSentinel(102, false, Race.elf, silverRanger, null, 4, ClassType2.Rogue),
	mysticMuse(103, true, Race.elf, spellsinger, null, 4, ClassType2.Wizard),
	elementalMaster(104, true, Race.elf, elementalSummoner, null, 4, ClassType2.Summoner),
	evaSaint(105, true, Race.elf, elder, null, 4, ClassType2.Healer),
	shillienTemplar(106, false, Race.darkelf, shillienKnight, null, 4, ClassType2.Knight),
	spectralDancer(107, false, Race.darkelf, bladedancer, null, 4, ClassType2.Enchanter),
	ghostHunter(108, false, Race.darkelf, abyssWalker, null, 4, ClassType2.Rogue),
	ghostSentinel(109, false, Race.darkelf, phantomRanger, null, 4, ClassType2.Rogue),
	stormScreamer(110, true, Race.darkelf, spellhowler, null, 4, ClassType2.Wizard),
	spectralMaster(111, true, Race.darkelf, phantomSummoner, null, 4, ClassType2.Summoner),
	shillienSaint(112, true, Race.darkelf, shillienElder, null, 4, ClassType2.Healer),
	titan(113, false, Race.orc, destroyer, null, 4, ClassType2.Warrior),
	grandKhauatari(114, false, Race.orc, tyrant, null, 4, ClassType2.Warrior),
	dominator(115, true, Race.orc, overlord, null, 4, ClassType2.Enchanter),
	doomcryer(116, true, Race.orc, warcryer, null, 4, ClassType2.Enchanter),
	fortuneSeeker(117, false, Race.dwarf, bountyHunter, null, 4, ClassType2.Warrior),
	maestro(118, false, Race.dwarf, warsmith, null, 4, ClassType2.Warrior),
	dummyEntry31(119, false, null, null, null, 0, null),
	dummyEntry32(120, false, null, null, null, 0, null),
	dummyEntry33(121, false, null, null, null, 0, null),
	dummyEntry34(122, false, null, null, null, 0, null);
	
	public static final ClassId[] VALUES;
	
	static
	{
		VALUES = ClassId.values();
	}
	
	private final int _id;
	private final boolean _isMage;
	private final Race _race;
	private final ClassId _parent;
	private final ClassId _parent2;
	private final ClassType2 _type2;
	private final int _level;
	
	ClassId(int id, boolean isMage, Race race, ClassId parent, ClassId parent2, int level, ClassType2 classType2)
	{
		_id = id;
		_isMage = isMage;
		_race = race;
		_parent = parent;
		_parent2 = parent2;
		_level = level;
		_type2 = classType2;
	}
	
	public final int getId()
	{
		return _id;
	}
	
	public final boolean isMage()
	{
		return _isMage;
	}
	
	public final Race getRace()
	{
		return _race;
	}
	
	public final boolean childOf(ClassId cid)
	{
		if(_parent == null)
		{
			return false;
		}
		if(_parent == cid || _parent2 == cid)
		{
			return true;
		}
		return _parent.childOf(cid);
	}
	
	public final boolean equalsOrChildOf(ClassId cid)
	{
		return this == cid || childOf(cid);
	}
	
	public final int level()
	{
		if(_parent == null)
		{
			return 0;
		}
		return 1 + _parent.level();
	}
	
	public final ClassId getParent(int sex)
	{
		return sex == 0 || _parent2 == null ? _parent : _parent2;
	}
	
	public final int getLevel()
	{
		return _level;
	}
	
	public ClassType2 getType2()
	{
		return _type2;
	}
}