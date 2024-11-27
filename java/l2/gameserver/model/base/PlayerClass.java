package l2.gameserver.model.base;

import l2.gameserver.Config;

import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Set;

public enum PlayerClass
{
	HumanFighter(Race.human, ClassType.Fighter, ClassLevel.First),
	Warrior(Race.human, ClassType.Fighter, ClassLevel.Second),
	Gladiator(Race.human, ClassType.Fighter, ClassLevel.Third),
	Warlord(Race.human, ClassType.Fighter, ClassLevel.Third),
	HumanKnight(Race.human, ClassType.Fighter, ClassLevel.Second),
	Paladin(Race.human, ClassType.Fighter, ClassLevel.Third),
	DarkAvenger(Race.human, ClassType.Fighter, ClassLevel.Third),
	Rogue(Race.human, ClassType.Fighter, ClassLevel.Second),
	TreasureHunter(Race.human, ClassType.Fighter, ClassLevel.Third),
	Hawkeye(Race.human, ClassType.Fighter, ClassLevel.Third),
	HumanMystic(Race.human, ClassType.Mystic, ClassLevel.First),
	HumanWizard(Race.human, ClassType.Mystic, ClassLevel.Second),
	Sorceror(Race.human, ClassType.Mystic, ClassLevel.Third),
	Necromancer(Race.human, ClassType.Mystic, ClassLevel.Third),
	Warlock(Race.human, ClassType.Mystic, ClassLevel.Third),
	Cleric(Race.human, ClassType.Priest, ClassLevel.Second),
	Bishop(Race.human, ClassType.Priest, ClassLevel.Third),
	Prophet(Race.human, ClassType.Priest, ClassLevel.Third),
	ElvenFighter(Race.elf, ClassType.Fighter, ClassLevel.First),
	ElvenKnight(Race.elf, ClassType.Fighter, ClassLevel.Second),
	TempleKnight(Race.elf, ClassType.Fighter, ClassLevel.Third),
	Swordsinger(Race.elf, ClassType.Fighter, ClassLevel.Third),
	ElvenScout(Race.elf, ClassType.Fighter, ClassLevel.Second),
	Plainswalker(Race.elf, ClassType.Fighter, ClassLevel.Third),
	SilverRanger(Race.elf, ClassType.Fighter, ClassLevel.Third),
	ElvenMystic(Race.elf, ClassType.Mystic, ClassLevel.First),
	ElvenWizard(Race.elf, ClassType.Mystic, ClassLevel.Second),
	Spellsinger(Race.elf, ClassType.Mystic, ClassLevel.Third),
	ElementalSummoner(Race.elf, ClassType.Mystic, ClassLevel.Third),
	ElvenOracle(Race.elf, ClassType.Priest, ClassLevel.Second),
	ElvenElder(Race.elf, ClassType.Priest, ClassLevel.Third),
	DarkElvenFighter(Race.darkelf, ClassType.Fighter, ClassLevel.First),
	PalusKnight(Race.darkelf, ClassType.Fighter, ClassLevel.Second),
	ShillienKnight(Race.darkelf, ClassType.Fighter, ClassLevel.Third),
	Bladedancer(Race.darkelf, ClassType.Fighter, ClassLevel.Third),
	Assassin(Race.darkelf, ClassType.Fighter, ClassLevel.Second),
	AbyssWalker(Race.darkelf, ClassType.Fighter, ClassLevel.Third),
	PhantomRanger(Race.darkelf, ClassType.Fighter, ClassLevel.Third),
	DarkElvenMystic(Race.darkelf, ClassType.Mystic, ClassLevel.First),
	DarkElvenWizard(Race.darkelf, ClassType.Mystic, ClassLevel.Second),
	Spellhowler(Race.darkelf, ClassType.Mystic, ClassLevel.Third),
	PhantomSummoner(Race.darkelf, ClassType.Mystic, ClassLevel.Third),
	ShillienOracle(Race.darkelf, ClassType.Priest, ClassLevel.Second),
	ShillienElder(Race.darkelf, ClassType.Priest, ClassLevel.Third),
	OrcFighter(Race.orc, ClassType.Fighter, ClassLevel.First),
	orcRaider(Race.orc, ClassType.Fighter, ClassLevel.Second),
	Destroyer(Race.orc, ClassType.Fighter, ClassLevel.Third),
	orcMonk(Race.orc, ClassType.Fighter, ClassLevel.Second),
	Tyrant(Race.orc, ClassType.Fighter, ClassLevel.Third),
	orcMystic(Race.orc, ClassType.Mystic, ClassLevel.First),
	orcShaman(Race.orc, ClassType.Mystic, ClassLevel.Second),
	Overlord(Race.orc, ClassType.Mystic, ClassLevel.Third),
	Warcryer(Race.orc, ClassType.Mystic, ClassLevel.Third),
	DwarvenFighter(Race.dwarf, ClassType.Fighter, ClassLevel.First),
	DwarvenScavenger(Race.dwarf, ClassType.Fighter, ClassLevel.Second),
	BountyHunter(Race.dwarf, ClassType.Fighter, ClassLevel.Third),
	DwarvenArtisan(Race.dwarf, ClassType.Fighter, ClassLevel.Second),
	Warsmith(Race.dwarf, ClassType.Fighter, ClassLevel.Third),
	DummyEntry1(null, null, null),
	DummyEntry2(null, null, null),
	DummyEntry3(null, null, null),
	DummyEntry4(null, null, null),
	DummyEntry5(null, null, null),
	DummyEntry6(null, null, null),
	DummyEntry7(null, null, null),
	DummyEntry8(null, null, null),
	DummyEntry9(null, null, null),
	DummyEntry10(null, null, null),
	DummyEntry11(null, null, null),
	DummyEntry12(null, null, null),
	DummyEntry13(null, null, null),
	DummyEntry14(null, null, null),
	DummyEntry15(null, null, null),
	DummyEntry16(null, null, null),
	DummyEntry17(null, null, null),
	DummyEntry18(null, null, null),
	DummyEntry19(null, null, null),
	DummyEntry20(null, null, null),
	DummyEntry21(null, null, null),
	DummyEntry22(null, null, null),
	DummyEntry23(null, null, null),
	DummyEntry24(null, null, null),
	DummyEntry25(null, null, null),
	DummyEntry26(null, null, null),
	DummyEntry27(null, null, null),
	DummyEntry28(null, null, null),
	DummyEntry29(null, null, null),
	DummyEntry30(null, null, null),
	Duelist(Race.human, ClassType.Fighter, ClassLevel.Fourth),
	Dreadnought(Race.human, ClassType.Fighter, ClassLevel.Fourth),
	PhoenixKnight(Race.human, ClassType.Fighter, ClassLevel.Fourth),
	HellKnight(Race.human, ClassType.Fighter, ClassLevel.Fourth),
	Sagittarius(Race.human, ClassType.Fighter, ClassLevel.Fourth),
	Adventurer(Race.human, ClassType.Fighter, ClassLevel.Fourth),
	Archmage(Race.human, ClassType.Mystic, ClassLevel.Fourth),
	Soultaker(Race.human, ClassType.Mystic, ClassLevel.Fourth),
	ArcanaLord(Race.human, ClassType.Mystic, ClassLevel.Fourth),
	Cardinal(Race.human, ClassType.Priest, ClassLevel.Fourth),
	Hierophant(Race.human, ClassType.Priest, ClassLevel.Fourth),
	EvaTemplar(Race.elf, ClassType.Fighter, ClassLevel.Fourth),
	SwordMuse(Race.elf, ClassType.Fighter, ClassLevel.Fourth),
	WindRider(Race.elf, ClassType.Fighter, ClassLevel.Fourth),
	MoonlightSentinel(Race.elf, ClassType.Fighter, ClassLevel.Fourth),
	MysticMuse(Race.elf, ClassType.Mystic, ClassLevel.Fourth),
	ElementalMaster(Race.elf, ClassType.Mystic, ClassLevel.Fourth),
	EvaSaint(Race.elf, ClassType.Priest, ClassLevel.Fourth),
	ShillienTemplar(Race.darkelf, ClassType.Fighter, ClassLevel.Fourth),
	SpectralDancer(Race.darkelf, ClassType.Fighter, ClassLevel.Fourth),
	GhostHunter(Race.darkelf, ClassType.Fighter, ClassLevel.Fourth),
	GhostSentinel(Race.darkelf, ClassType.Fighter, ClassLevel.Fourth),
	StormScreamer(Race.darkelf, ClassType.Mystic, ClassLevel.Fourth),
	SpectralMaster(Race.darkelf, ClassType.Mystic, ClassLevel.Fourth),
	ShillienSaint(Race.darkelf, ClassType.Priest, ClassLevel.Fourth),
	Titan(Race.orc, ClassType.Fighter, ClassLevel.Fourth),
	GrandKhauatari(Race.orc, ClassType.Fighter, ClassLevel.Fourth),
	Dominator(Race.orc, ClassType.Mystic, ClassLevel.Fourth),
	Doomcryer(Race.orc, ClassType.Mystic, ClassLevel.Fourth),
	FortuneSeeker(Race.dwarf, ClassType.Fighter, ClassLevel.Fourth),
	Maestro(Race.dwarf, ClassType.Fighter, ClassLevel.Fourth),
	DummyEntry31(null, null, null),
	DummyEntry32(null, null, null),
	DummyEntry33(null, null, null),
	DummyEntry34(null, null, null);
	
	public static final PlayerClass[] VALUES;
	private static final Set<PlayerClass> mainSubclassSet;
	private static final Set<PlayerClass> neverSubclassed;
	private static final Set<PlayerClass> subclasseSet1;
	private static final Set<PlayerClass> subclasseSet2;
	private static final Set<PlayerClass> subclasseSet3;
	private static final Set<PlayerClass> subclasseSet4;
	private static final Set<PlayerClass> subclasseSet5;
	private static final EnumMap<PlayerClass, Set<PlayerClass>> subclassSetMap;
	
	static
	{
		VALUES = PlayerClass.values();
		neverSubclassed = EnumSet.of(Overlord, Warsmith);
		subclasseSet1 = EnumSet.of(DarkAvenger, Paladin, TempleKnight, ShillienKnight);
		subclasseSet2 = EnumSet.of(TreasureHunter, AbyssWalker, Plainswalker);
		subclasseSet3 = EnumSet.of(Hawkeye, SilverRanger, PhantomRanger);
		subclasseSet4 = EnumSet.of(Warlock, ElementalSummoner, PhantomSummoner);
		subclasseSet5 = EnumSet.of(Sorceror, Spellsinger, Spellhowler);
		subclassSetMap = new EnumMap(PlayerClass.class);
		EnumSet<PlayerClass> subclasses = getSet(null, ClassLevel.Third);
		if(!Config.ALTSUBCLASS_ALLOW_OVER_AND_WARSMITH_TO_ALL)
		{
			subclasses.removeAll(neverSubclassed);
		}
		mainSubclassSet = subclasses;
		subclassSetMap.put(DarkAvenger, subclasseSet1);
		subclassSetMap.put(HellKnight, subclasseSet1);
		subclassSetMap.put(Paladin, subclasseSet1);
		subclassSetMap.put(PhoenixKnight, subclasseSet1);
		subclassSetMap.put(TempleKnight, subclasseSet1);
		subclassSetMap.put(EvaTemplar, subclasseSet1);
		subclassSetMap.put(ShillienKnight, subclasseSet1);
		subclassSetMap.put(ShillienTemplar, subclasseSet1);
		subclassSetMap.put(TreasureHunter, subclasseSet2);
		subclassSetMap.put(Adventurer, subclasseSet2);
		subclassSetMap.put(AbyssWalker, subclasseSet2);
		subclassSetMap.put(GhostHunter, subclasseSet2);
		subclassSetMap.put(Plainswalker, subclasseSet2);
		subclassSetMap.put(WindRider, subclasseSet2);
		subclassSetMap.put(Hawkeye, subclasseSet3);
		subclassSetMap.put(Sagittarius, subclasseSet3);
		subclassSetMap.put(SilverRanger, subclasseSet3);
		subclassSetMap.put(MoonlightSentinel, subclasseSet3);
		subclassSetMap.put(PhantomRanger, subclasseSet3);
		subclassSetMap.put(GhostSentinel, subclasseSet3);
		subclassSetMap.put(Warlock, subclasseSet4);
		subclassSetMap.put(ArcanaLord, subclasseSet4);
		subclassSetMap.put(ElementalSummoner, subclasseSet4);
		subclassSetMap.put(ElementalMaster, subclasseSet4);
		subclassSetMap.put(PhantomSummoner, subclasseSet4);
		subclassSetMap.put(SpectralMaster, subclasseSet4);
		subclassSetMap.put(Sorceror, subclasseSet5);
		subclassSetMap.put(Archmage, subclasseSet5);
		subclassSetMap.put(Spellsinger, subclasseSet5);
		subclassSetMap.put(MysticMuse, subclasseSet5);
		subclassSetMap.put(Spellhowler, subclasseSet5);
		subclassSetMap.put(StormScreamer, subclasseSet5);
		subclassSetMap.put(Duelist, EnumSet.of(Gladiator));
		subclassSetMap.put(Dreadnought, EnumSet.of(Warlord));
		subclassSetMap.put(Soultaker, EnumSet.of(Necromancer));
		subclassSetMap.put(Cardinal, EnumSet.of(Bishop));
		subclassSetMap.put(Hierophant, EnumSet.of(Prophet));
		subclassSetMap.put(SwordMuse, EnumSet.of(Swordsinger));
		subclassSetMap.put(EvaSaint, EnumSet.of(ElvenElder));
		subclassSetMap.put(SpectralDancer, EnumSet.of(Bladedancer));
		subclassSetMap.put(Titan, EnumSet.of(Destroyer));
		subclassSetMap.put(GrandKhauatari, EnumSet.of(Tyrant));
		subclassSetMap.put(Dominator, EnumSet.of(Overlord));
		subclassSetMap.put(Doomcryer, EnumSet.of(Warcryer));
	}
	
	private final Race _race;
	private final ClassLevel _level;
	private final ClassType _type;
	
	PlayerClass(Race race, ClassType type, ClassLevel level)
	{
		_race = race;
		_level = level;
		_type = type;
	}
	
	public static EnumSet<PlayerClass> getSet(Race race, ClassLevel level)
	{
		EnumSet<PlayerClass> allOf = EnumSet.noneOf(PlayerClass.class);
		for(PlayerClass playerClass : EnumSet.allOf(PlayerClass.class))
		{
			if(race != null && !playerClass.isOfRace(race) || level != null && !playerClass.isOfLevel(level))
				continue;
			allOf.add(playerClass);
		}
		return allOf;
	}
	
	public static boolean areClassesComportable(PlayerClass c1, PlayerClass c2)
	{
		if(!Config.ALTSUBCLASS_LIST_ALL && (c1.isOfRace(Race.elf) && c2.isOfRace(Race.darkelf) || c1.isOfRace(Race.darkelf) && c2.isOfRace(Race.elf)))
		{
			return false;
		}
		if(!(Config.ALTSUBCLASS_ALLOW_OVER_AND_WARSMITH_TO_ALL || c1 != Overlord && c1 != Warsmith && c2 != Overlord && c2 != Warsmith))
		{
			return false;
		}
		return subclassSetMap.get(c1) != subclassSetMap.get(c2);
	}
	
	public final Set<PlayerClass> getAvailableSubclasses()
	{
		EnumSet<PlayerClass> subclasses = null;
		if(_level == ClassLevel.Third || _level == ClassLevel.Fourth)
		{
			subclasses = EnumSet.copyOf(mainSubclassSet);
			if(!Config.ALTSUBCLASS_ALLOW_OVER_AND_WARSMITH_TO_ALL)
			{
				subclasses.removeAll(neverSubclassed);
			}
			subclasses.remove(this);
			if(!Config.ALTSUBCLASS_LIST_ALL)
			{
				switch(_race)
				{
					case elf:
					{
						subclasses.removeAll(getSet(Race.darkelf, ClassLevel.Third));
						break;
					}
					case darkelf:
					{
						subclasses.removeAll(getSet(Race.elf, ClassLevel.Third));
					}
				}
			}
			Set<PlayerClass> unavailableClasses;
			if((unavailableClasses = subclassSetMap.get(this)) != null)
			{
				subclasses.removeAll(unavailableClasses);
			}
		}
		return subclasses;
	}
	
	public final boolean isOfRace(Race race)
	{
		return _race == race;
	}
	
	public final boolean isOfType(ClassType type)
	{
		return _type == type;
	}
	
	public final boolean isOfLevel(ClassLevel level)
	{
		return _level == level;
	}
}