package l2.gameserver.network.l2.components;

import java.util.NoSuchElementException;

public enum NpcString
{
	NONE(-1),
	THE_RADIO_SIGNAL_DETECTOR_IS_RESPONDING_A_SUSPICIOUS_PILE_OF_STONES_CATCHES_YOUR_EYE(11453),
	ATT__ATTACK__S1__RO__ROGUE__S2(46350),
	LISTEN_YOU_VILLAGERS_OUR_LIEGE_WHO_WILL_SOON_BECAME_A_LORD_HAS_DEFEATED_THE_HEADLESS_KNIGHT(70854),
	S1_HAS_BECOME_THE_LORD_OF_THE_TOWN_OF_GLUDIO(70859),
	S1_HAS_BECOME_THE_LORD_OF_THE_TOWN_OF_DION(70959),
	S1_HAS_BECOME_THE_LORD_OF_THE_TOWN_OF_GIRAN(71059),
	S1_HAS_BECOME_THE_LORD_OF_THE_TOWN_OF_OREN(71259),
	S1_HAS_BECOME_THE_LORD_OF_THE_TOWN_OF_ADEN(71351),
	S1_HAS_BECOME_THE_LORD_OF_THE_TOWN_OF_SCHUTTGART(71459),
	S1_HAS_BECOME_THE_LORD_OF_THE_TOWN_OF_INNADRIL(71159),
	S1_HAS_BECOME_THE_LORD_OF_THE_TOWN_OF_RUNE(71659),
	PROTECT_THE_CATAPULT_OF_GLUDIO(72951),
	PROTECT_THE_CATAPULT_OF_DION(72952),
	PROTECT_THE_CATAPULT_OF_GIRAN(72953),
	PROTECT_THE_CATAPULT_OF_OREN(72954),
	PROTECT_THE_CATAPULT_OF_ADEN(72955),
	PROTECT_THE_CATAPULT_OF_INNADRIL(72956),
	PROTECT_THE_CATAPULT_OF_GODDARD(72957),
	PROTECT_THE_CATAPULT_OF_RUNE(72958),
	PROTECT_THE_CATAPULT_OF_SCHUTTGART(72959),
	THE_CATAPULT_OF_GLUDIO_HAS_BEEN_DESTROYED(72961),
	THE_CATAPULT_OF_DION_HAS_BEEN_DESTROYED(72962),
	THE_CATAPULT_OF_GIRAN_HAS_BEEN_DESTROYED(72963),
	THE_CATAPULT_OF_OREN_HAS_BEEN_DESTROYED(72964),
	THE_CATAPULT_OF_ADEN_HAS_BEEN_DESTROYED(72965),
	THE_CATAPULT_OF_INNADRIL_HAS_BEEN_DESTROYED(72966),
	THE_CATAPULT_OF_GODDARD_HAS_BEEN_DESTROYED(72967),
	THE_CATAPULT_OF_RUNE_HAS_BEEN_DESTROYED(72968),
	THE_CATAPULT_OF_SCHUTTGART_HAS_BEEN_DESTROYED(72969),
	PROTECT_THE_SUPPLIES_SAFE_OF_GLUDIO(73051),
	PROTECT_THE_SUPPLIES_SAFE_OF_DION(73052),
	PROTECT_THE_SUPPLIES_SAFE_OF_GIRAN(73053),
	PROTECT_THE_SUPPLIES_SAFE_OF_OREN(73054),
	PROTECT_THE_SUPPLIES_SAFE_OF_ADEN(73055),
	PROTECT_THE_SUPPLIES_SAFE_OF_INNADRIL(73056),
	PROTECT_THE_SUPPLIES_SAFE_OF_GODDARD(73057),
	PROTECT_THE_SUPPLIES_SAFE_OF_RUNE(73058),
	PROTECT_THE_SUPPLIES_SAFE_OF_SCHUTTGART(73059),
	THE_SUPPLIES_SAFE_OF_GLUDIO_HAS_BEEN_DESTROYED(73061),
	THE_SUPPLIES_SAFE_OF_DION_HAS_BEEN_DESTROYED(73062),
	THE_SUPPLIES_SAFE_OF_GIRAN_HAS_BEEN_DESTROYED(73063),
	THE_SUPPLIES_SAFE_OF_OREN_HAS_BEEN_DESTROYED(73064),
	THE_SUPPLIES_SAFE_OF_ADEN_HAS_BEEN_DESTROYED(73065),
	THE_SUPPLIES_SAFE_OF_INNADRIL_HAS_BEEN_DESTROYED(73066),
	THE_SUPPLIES_SAFE_OF_GODDARD_HAS_BEEN_DESTROYED(73067),
	THE_SUPPLIES_SAFE_OF_RUNE_HAS_BEEN_DESTROYED(73068),
	THE_SUPPLIES_SAFE_OF_SCHUTTGART_HAS_BEEN_DESTROYED(73069),
	PROTECT_THE_MILITARY_ASSOCIATION_LEADER_OF_GLUDIO(73151),
	PROTECT_THE_MILITARY_ASSOCIATION_LEADER_OF_DION(73152),
	PROTECT_THE_MILITARY_ASSOCIATION_LEADER_OF_GIRAN(73153),
	PROTECT_THE_MILITARY_ASSOCIATION_LEADER_OF_OREN(73154),
	PROTECT_THE_MILITARY_ASSOCIATION_LEADER_OF_ADEN(73155),
	PROTECT_THE_MILITARY_ASSOCIATION_LEADER_OF_INNADRIL(73156),
	PROTECT_THE_MILITARY_ASSOCIATION_LEADER_OF_GODDARD(73157),
	PROTECT_THE_MILITARY_ASSOCIATION_LEADER_OF_RUNE(73158),
	PROTECT_THE_MILITARY_ASSOCIATION_LEADER_OF_SCHUTTGART(73159),
	THE_MILITARY_ASSOCIATION_LEADER_OF_GLUDIO_IS_DEAD(73161),
	THE_MILITARY_ASSOCIATION_LEADER_OF_DION_IS_DEAD(73162),
	THE_MILITARY_ASSOCIATION_LEADER_OF_GIRAN_IS_DEAD(73163),
	THE_MILITARY_ASSOCIATION_LEADER_OF_OREN_IS_DEAD(73164),
	THE_MILITARY_ASSOCIATION_LEADER_OF_ADEN_IS_DEAD(73165),
	THE_MILITARY_ASSOCIATION_LEADER_OF_INNADRIL_IS_DEAD(73166),
	THE_MILITARY_ASSOCIATION_LEADER_OF_GODDARD_IS_DEAD(73167),
	THE_MILITARY_ASSOCIATION_LEADER_OF_RUNE_IS_DEAD(73168),
	THE_MILITARY_ASSOCIATION_LEADER_OF_SCHUTTGART_IS_DEAD(73169),
	PROTECT_THE_RELIGIOUS_ASSOCIATION_LEADER_OF_GLUDIO(73251),
	PROTECT_THE_RELIGIOUS_ASSOCIATION_LEADER_OF_DION(73252),
	PROTECT_THE_RELIGIOUS_ASSOCIATION_LEADER_OF_GIRAN(73253),
	PROTECT_THE_RELIGIOUS_ASSOCIATION_LEADER_OF_OREN(73254),
	PROTECT_THE_RELIGIOUS_ASSOCIATION_LEADER_OF_ADEN(73255),
	PROTECT_THE_RELIGIOUS_ASSOCIATION_LEADER_OF_INNADRIL(73256),
	PROTECT_THE_RELIGIOUS_ASSOCIATION_LEADER_OF_GODDARD(73257),
	PROTECT_THE_RELIGIOUS_ASSOCIATION_LEADER_OF_RUNE(73258),
	PROTECT_THE_RELIGIOUS_ASSOCIATION_LEADER_OF_SCHUTTGART(73259),
	THE_RELIGIOUS_ASSOCIATION_LEADER_OF_GLUDIO_IS_DEAD(73261),
	THE_RELIGIOUS_ASSOCIATION_LEADER_OF_DION_IS_DEAD(73262),
	THE_RELIGIOUS_ASSOCIATION_LEADER_OF_GIRAN_IS_DEAD(73263),
	THE_RELIGIOUS_ASSOCIATION_LEADER_OF_OREN_IS_DEAD(73264),
	THE_RELIGIOUS_ASSOCIATION_LEADER_OF_ADEN_IS_DEAD(73265),
	THE_RELIGIOUS_ASSOCIATION_LEADER_OF_INNADRIL_IS_DEAD(73266),
	THE_RELIGIOUS_ASSOCIATION_LEADER_OF_GODDARD_IS_DEAD(73267),
	THE_RELIGIOUS_ASSOCIATION_LEADER_OF_RUNE_IS_DEAD(73268),
	THE_RELIGIOUS_ASSOCIATION_LEADER_OF_SCHUTTGART_IS_DEAD(73269),
	PROTECT_THE_ECONOMIC_ASSOCIATION_LEADER_OF_GLUDIO(73351),
	PROTECT_THE_ECONOMIC_ASSOCIATION_LEADER_OF_DION(73352),
	PROTECT_THE_ECONOMIC_ASSOCIATION_LEADER_OF_GIRAN(73353),
	PROTECT_THE_ECONOMIC_ASSOCIATION_LEADER_OF_OREN(73354),
	PROTECT_THE_ECONOMIC_ASSOCIATION_LEADER_OF_ADEN(73355),
	PROTECT_THE_ECONOMIC_ASSOCIATION_LEADER_OF_INNADRIL(73356),
	PROTECT_THE_ECONOMIC_ASSOCIATION_LEADER_OF_GODDARD(73357),
	PROTECT_THE_ECONOMIC_ASSOCIATION_LEADER_OF_RUNE(73358),
	PROTECT_THE_ECONOMIC_ASSOCIATION_LEADER_OF_SCHUTTGART(73359),
	THE_ECONOMIC_ASSOCIATION_LEADER_OF_GLUDIO_IS_DEAD(73361),
	THE_ECONOMIC_ASSOCIATION_LEADER_OF_DION_IS_DEAD(73362),
	THE_ECONOMIC_ASSOCIATION_LEADER_OF_GIRAN_IS_DEAD(73363),
	THE_ECONOMIC_ASSOCIATION_LEADER_OF_OREN_IS_DEAD(73364),
	THE_ECONOMIC_ASSOCIATION_LEADER_OF_ADEN_IS_DEAD(73365),
	THE_ECONOMIC_ASSOCIATION_LEADER_OF_INNADRIL_IS_DEAD(73366),
	THE_ECONOMIC_ASSOCIATION_LEADER_OF_GODDARD_IS_DEAD(73367),
	THE_ECONOMIC_ASSOCIATION_LEADER_OF_RUNE_IS_DEAD(73368),
	THE_ECONOMIC_ASSOCIATION_LEADER_OF_SCHUTTGART_IS_DEAD(73369),
	DEFEAT_S1_ENEMY_KNIGHTS(73451),
	YOU_HAVE_DEFEATED_S2_OF_S1_KNIGHTS(73461),
	YOU_WEAKENED_THE_ENEMYS_DEFENSE(73462),
	DEFEAT_S1_WARRIORS_AND_ROGUES(73551),
	YOU_HAVE_DEFEATED_S2_OF_S1_WARRIORS_AND_ROGUES(73561),
	YOU_WEAKENED_THE_ENEMYS_ATTACK(73562),
	DEFEAT_S1_WIZARDS_AND_SUMMONERS(73651),
	YOU_HAVE_DEFEATED_S2_OF_S1_ENEMIES(73661),
	YOU_WEAKENED_THE_ENEMYS_MAGIC(73662),
	DEFEAT_S1_HEALERS_AND_BUFFERS(73751),
	YOU_HAVE_DEFEATED_S2_OF_S1_HEALERS_AND_BUFFERS(73761),
	YOU_HAVE_WEAKENED_THE_ENEMYS_SUPPORT(73762),
	DEFEAT_S1_WARSMITHS_AND_OVERLORDS(73851),
	YOU_HAVE_DEFEATED_S2_OF_S1_WARSMITHS_AND_OVERLORDS(73861),
	YOU_DESTROYED_THE_ENEMYS_PROFESSIONALS(73862),
	RETURN(1000170),
	EVENT_NUMBER(1000172),
	FIRST_PRIZE(1000173),
	SECOND_PRIZE(1000174),
	THIRD_PRIZE(1000175),
	FOURTH_PRIZE(1000176),
	THERE_HAS_BEEN_NO_WINNING_LOTTERY_TICKET(1000177),
	YOUR_LUCKY_NUMBERS_HAVE_BEEN_SELECTED_ABOVE(1000179),
	PREPARE_TO_DIE_FOREIGN_INVADERS_I_AM_GUSTAV_THE_ETERNAL_RULER_OF_THIS_FORTRESS_AND_I_HAVE_TAKEN_UP_MY_SWORD_TO_REPEL_THEE(1000275),
	GLORY_TO_ADEN_THE_KINGDOM_OF_THE_LION_GLORY_TO_SIR_GUSTAV_OUR_IMMORTAL_LORD(1000276),
	SOLDIERS_OF_GUSTAV_GO_FORTH_AND_DESTROY_THE_INVADERS(1000277),
	THIS_IS_UNBELIEVABLE_HAVE_I_REALLY_BEEN_DEFEATED_I_SHALL_RETURN_AND_TAKE_YOUR_HEAD(1000278),
	COULD_IT_BE_THAT_I_HAVE_REACHED_MY_END_I_CANNOT_DIE_WITHOUT_HONOR_WITHOUT_THE_PERMISSION_OF_SIR_GUSTAV(1000279),
	AH_THE_BITTER_TASTE_OF_DEFEAT_I_FEAR_MY_TORMENTS_ARE_NOT_OVER(1000280),
	THIS_WORLD_WILL_SOON_BE_ANNIHILATED(1000303),
	ALL_IS_LOST__PREPARE_TO_MEET_THE_GODDESS_OF_DEATH(1000415),
	ALL_IS_LOST__THE_PROPHECY_OF_DESTRUCTION_HAS_BEEN_FULFILLED(1000416),
	THE_END_OF_TIME_HAS_COME__THE_PROPHECY_OF_DESTRUCTION_HAS_BEEN_FULFILLED(1000417),
	THE_DAY_OF_JUDGMENT_IS_NEAR(1000305),
	THE_PROPHECY_OF_DARKNESS_HAS_BEEN_FULFILLED(1000421),
	AS_FORETOLD_IN_THE_PROPHECY_OF_DARKNESS__THE_ERA_OF_CHAOS_HAS_BEGUN(1000422),
	THE_PROPHECY_OF_DARKNESS_HAS_COME_TO_PASS(1000423),
	I_BESTOW_UPON_YOU_A_BLESSING(1000306),
	S1__I_GIVE_YOU_THE_BLESSING_OF_PROPHECY(1000424),
	HERALD_OF_THE_NEW_ERA__OPEN_YOUR_EYES(1000426),
	S1__I_BESTOW_UPON_YOU_THE_AUTHORITY_OF_THE_ABYSS(1000425),
	YOU_DONT_HAVE_ANY_HOPE__YOUR_END_HAS_COME(1000420),
	A_CURSE_UPON_YOU(1000304),
	S1__YOU_BRING_AN_ILL_WIND(1000418),
	S1__YOU_MIGHT_AS_WELL_GIVE_UP(1000419),
	THE_DEFENDERS_OF_S1_CASTLE_WILL_BE_TELEPORTED_TO_THE_INNER_CASTLE(1000443),
	COMPETITION(1000507),
	SEAL_VALIDATION(1000508),
	PREPARATION(1000509),
	DUSK(1000510),
	DAWN(1000511),
	NO_OWNER(1000512),
	__UNREGISTERED__(1000495),
	VALAKAS_ARROGAANT_FOOL_YOU_DARE_TO_CHALLENGE_ME(1000519),
	VALAKAS_FINDS_YOU_ATTACKS_ANNOYING_SILENCE(1801071),
	VALAKAS_PDEF_ISM_DECREACED_SLICED_DASH(1801072),
	VALAKAS_OVERWHELMED_BY_ATTACK_NOW_TIME_ATTACK(1801073),
	VALAKAS_RANGED_ATTACKS_PROVOKED(1801074),
	VALAKAS_HEIGHTENED_BY_COUNTERATTACKS(1801075),
	VALAKAS_RANGED_ATTACKS_ENRAGED_TARGET_FREE(1801076),
	VALAKAS_THE_EVIL_FIRE_DRAGON_VALAKAS_DEFEATED(1900151),
	ANTHARAS_YOU_CANNOT_HOPE_TO_DEFEAT_ME(1000520),
	ANTHARAS_THE_EVIL_LAND_DRAGON_ANTHARAS_DEFEATED(1900150),
	ANTHARAS_EARTH_ENERGY_GATHERING_LEGS(1900155),
	ANTHARAS_STARTS_ABSORB_EARTH_ENERGY(1900156),
	ANTHARAS_RAISES_ITS_THICK_TAIL(1900157),
	ANTHARAS_YOU_ARE_OVERCOME_(1900158),
	ANTHARAS_EYES_FILLED_WITH_RAGE(1900159),
	REQUIEM_OF_HATRED(1000522),
	FUGUE_OF_JUBILATION(1000523),
	FRENETIC_TOCCATA(1000524),
	HYPNOTIC_MAZURKA(1000525),
	MOURNFUL_CHORALE_PRELUDE(1000526),
	RONDO_OF_SOLITUDE(1000527),
	GLUDIO(1001001),
	DION(1001002),
	GIRAN(1001003),
	OREN(1001004),
	ADEN(1001005),
	INNADRIL(1001006),
	THE_KINGDOM_OF_ELMORE(1001100),
	GODDARD(1001007),
	RUNE(1001008),
	SCHUTTGART(1001009),
	A_BLACK_MOON_NOW_DO_YOU_UNDERSTAND_THAT_HE_HAS_OPENED_HIS_EYES(1010221),
	CLOUDS_OF_BLOOD_ARE_GATHERING_SOON_IT_WILL_START_TO_RAIN_THE_RAIN_OF_CRIMSON_BLOOD(1010222),
	WHILE_THE_FOOLISH_LIGHT_WAS_ASLEEP_THE_DARKNESS_WILL_AWAKEN_FIRST_UH(1010223),
	IT_IS_THE_DEEPEST_DARKNESS_WITH_ITS_ARRIVAL_THE_WORLD_WILL_SOON_DIE(1010224),
	DEATH_IS_JUST_A_NEW_BEGINNING_HUHU_FEAR_NOT(1010225),
	AHH_BEAUTIFUL_GODDES_OF_DEATH_COVER_OVER_THE_FILTH_OF_THOS_WORLD_YOUR_DARKNESS(1010226),
	THE_GODDESS_RESURRECTION_HAS_ALREADY_BEGUN_HUHU_INSIGNIFICANT_CREATURES_LIKE_YOU_CAN_DO_NOTHING(1010227),
	WHO_DARES_TO_COVET_THE_THRONE_OF_OUR_CASTLE__LEAVE_IMMEDIATELY_OR_YOU_WILL_PAY_THE_PRICE_OF_YOUR_AUDACITY_WITH_YOUR_VERY_OWN_BLOOD(1010623),
	HMM_THOSE_WHO_ARE_NOT_OF_THE_BLOODLINE_ARE_COMING_THIS_WAY_TO_TAKE_OVER_THE_CASTLE__HUMPH__THE_BITTER_GRUDGES_OF_THE_DEAD(1010624),
	AARGH_IF_I_DIE_THEN_THE_MAGIC_FORCE_FIELD_OF_BLOOD_WILL(1010625),
	ITS_NOT_OVER_YET__IT_WONT_BE__OVER__LIKE_THIS__NEVER(1010626),
	OOOH_WHO_POURED_NECTAR_ON_MY_HEAD_WHILE_I_WAS_SLEEPING(1010627),
	UNDECIDED(1010635),
	HEH_HEH_I_SEE_THAT_THE_FEAST_HAS_BEGAN_BE_WARY_THE_CURSE_OF_THE_HELLMANN_FAMILY_HAS_POISONED_THIS_LAND(1010636),
	ARISE_MY_FAITHFUL_SERVANTS_YOU_MY_PEOPLE_WHO_HAVE_INHERITED_THE_BLOOD(1010637),
	GRARR_FOR_THE_NEXT_2_MINUTES_OR_SO_THE_GAME_ARENA_ARE_WILL_BE_CLEANED(1010639),
	MATCH_BEGINS_IN_S1_MINUTES(1800080),
	THE_MATCH_IS_AUTOMATICALLY_CANCELED_BECAUSE_YOU_ARE_TOO_FAR_FROM_THE_ADMISSION_MANAGER(1800081),
	MATCH_CANCELLED(1800123),
	BEGIN_STAGE_1_FREYA(1801086),
	BEGIN_STAGE_2_FREYA(1801087),
	BEGIN_STAGE_3_FREYA(1801088),
	BEGIN_STAGE_4_FREYA(1801089),
	TIME_REMAINING_UNTIL_NEXT_BATTLE(1801090),
	FREYA_HAS_STARTED_TO_MOVE(1801097),
	S1_OF_BALANCE(1801100),
	SWIFT_S1(1801101),
	S1_OF_BLESSING(1801102),
	SHARP_S1(1801103),
	USEFUL_S1(1801104),
	RECKLESS_S1(1801105),
	ALPEN_KOOKABURRA(1801106),
	ALPEN_COUGAR(1801107),
	ALPEN_BUFFALO(1801108),
	ALPEN_GRENDEL(1801109),
	WE_HAVE_BROKEN_THROUGH_THE_GATE_DESTROY_THE_ENCAMPMENT_AND_MOVE_TO_THE_COMMAND_POST(1300001),
	THE_COMMAND_GATE_HAS_OPENED_CAPTURE_THE_FLAG_QUICKLY_AND_RAISE_IT_HIGH_TO_PROCLAIM_OUR_VICTORY(1300002),
	THE_GODS_HAVE_FORSAKEN_US__RETREAT(1300003),
	YOU_MAY_HAVE_BROKEN_OUR_ARROWS_BUT_YOU_WILL_NEVER_BREAK_OUR_WILL_ARCHERS_RETREAT(1300004),
	AT_LAST_THE_MAGIC_FIELD_THAT_PROTECTS_THE_FORTRESS_HAS_WEAKENED_VOLUNTEERS_STAND_BACK(1300005),
	AIIEEEE_COMMAND_CENTER_THIS_IS_GUARD_UNIT_WE_NEED_BACKUP_RIGHT_AWAY(1300006),
	FORTRESS_POWER_DISABLED(1300007),
	MACHINE_NO_1_POWER_OFF(1300009),
	MACHINE_NO_2_POWER_OFF(1300010),
	MACHINE_NO_3_POWER_OFF(1300011),
	SPIRIT_OF_FIRE_UNLEASH_YOUR_POWER_BURN_THE_ENEMY(1300014),
	DO_YOU_NEED_MY_POWER_YOU_SEEM_TO_BE_STRUGGLING(1300016),
	DONT_THINK_THAT_ITS_GONNA_END_LIKE_THIS(1300018),
	I_FEEL_SO_MUCH_GRIEF_THAT_I_CANT_EVEN_TAKE_CARE_OF_MYSELF(1300020),
	INDEPENDENT_STATE(1300122),
	NONPARTISAN(1300123),
	CONTRACT_STATE(1300124),
	FIRST_PASSWORD_HAS_BEEN_ENTERED(1300125),
	SECOND_PASSWORD_HAS_BEEN_ENTERED(1300126),
	PASSWORD_HAS_NOT_BEEN_ENTERED(1300127),
	ATTEMPT_S1__3_IS_IN_PROGRESS(1300128),
	THE_1ST_MARK_IS_CORRECT(1300129),
	THE_2ND_MARK_IS_CORRECT(1300130),
	THE_MARKS_HAVE_NOT_BEEN_ASSEMBLED(1300131),
	OLYMPIAD_CLASSFREE_TEAM_MATCH_IS_GOING_TO_BEGIN_IN_ARENA_S1_IN_A_MOMENT(1300132),
	DOMAIN_FORTRESS(1300133),
	BOUNDARY_FORTRESS(1300134),
	S1HOUR_S2MINUTE(1300135),
	BEGIN_STAGE_1(1300150),
	BEGIN_STAGE_2(1300151),
	BEGIN_STAGE_3(1300152),
	WHAT_A_PREDICAMENT_MY_ATTEMPTS_WERE_UNSUCCESSUFUL(1300162),
	COURAGE_AMBITION_PASSION_MERCENARIES_WHO_WANT_TO_REALIZE_THEIR_DREAM_OF_FIGHTING_IN_THE_TERRITORY_WAR_COME_TO_ME_FORTUNE_AND_GLORY_ARE_WAITING_FOR_YOU(1300163),
	DO_YOU_WISH_TO_FIGHT_ARE_YOU_AFRAID_NO_MATTER_HOW_HARD_YOU_TRY_YOU_HAVE_NOWHERE_TO_RUN(1300164),
	CHARGE_CHARGE_CHARGE(1300165),
	OLYMPIAD_CLASSFREE_INDIVIDUAL_MATCH_IS_GOING_TO_BEGIN_IN_ARENA_S1_IN_A_MOMENT(1300166),
	OLYMPIAD_CLASS_INDIVIDUAL_MATCH_IS_GOING_TO_BEGIN_IN_ARENA_S1_IN_A_MOMENT(1300167),
	AIRSHIP_IS_SUMMONED_IS_DEPART_IN_5_MINUTES(1800219),
	AIRSHIP_IS_ARRIVED_IT_WILL_DEPART_TO_ADEN_IN_1_MINUTE(1800220),
	AIRSHIP_IS_DEPARTED_TO_ADEN(1800221),
	AIRSHIP_IS_ARRIVED_IT_WILL_DEPART_TO_GRACIA_IN_1_MINUTE(1800222),
	AIRSHIP_IS_DEPARTED_TO_GRACIA(1800223),
	IN_AIR_HARBOR_ALREADY_AIRSHIP_DOCKED_PLEASE_WAIT_AND_TRY_AGAIN(1800224),
	ATTACK(1800243),
	DEFEND(1800244),
	MAGUEN_APPEARANCE(1801149),
	THERE_ARE_5_MINUTES_REMAINING_TO_REGISTER_FOR_KRATEIS_CUBE_MATCH(1800203),
	THERE_ARE_3_MINUTES_REMAINING_TO_REGISTER_FOR_KRATEIS_CUBE_MATCH(1800204),
	THERE_ARE_1_MINUTES_REMAINING_TO_REGISTER_FOR_KRATEIS_CUBE_MATCH(1800205),
	THE_MATCH_WILL_BEGIN_IN_S1_MINUTES(1800206),
	THE_MATCH_WILL_BEGIN_SHORTLY(1800207),
	REGISTRATION_FOR_THE_NEXT_MATCH_WILL_END_AT_S1_MINUTES_AFTER_HOUR(1800208),
	EVEN_THOUGH_YOU_BRING_SOMETHING_CALLED_A_GIFT_AMONG_YOUR_HUMANS_IT_WOULD_JUST_BE_PROBLEMATIC_FOR_ME(1801190),
	I_JUST_DONT_KNOW_WHAT_EXPRESSION_I_SHOULD_HAVE_IT_APPEARED_ON_ME(1801191),
	THE_FEELING_OF_THANKS_IS_JUST_TOO_MUCH_DISTANT_MEMORY_FOR_ME(1801192),
	BUT_I_KIND_OF_MISS_IT(1801193),
	I_AM_ICE_QUEEN_FREYA(1801194),
	DEAR_S1(1801195),
	RULERS_OF_THE_SEAL_I_BRING_YOU_WONDROUS_GIFTS(1000431),
	RULERS_OF_THE_SEAL_I_HAVE_SOME_EXCELLENT_WEAPONS_TO_SHOW_YOU(1000432),
	IVE_BEEN_SO_BUSY_LATELY_IN_ADDITION_TO_PLANNING_MY_TRIP(1000433),
	IF_YOU_HAVE_ITEMS_PLEASE_GIVE_THEM_TO_ME(1800279),
	MY_STOMACH_IS_EMPTY(1800280),
	IM_HUNGRY_IM_HUNGRY(1800281),
	IM_STILL_NOT_FULL(1800282),
	IM_STILL_HUNGRY(1800283),
	I_FEEL_A_LITTLE_WOOZY(1800284),
	GIVE_ME_SOMETHING_TO_EAT(1800285),
	NOW_ITS_TIME_TO_EAT(1800286),
	I_ALSO_NEED_A_DESSERT(1800287),
	IM_STILL_HUNGRY_(1800288),
	IM_FULL_NOW_I_DONT_WANT_TO_EAT_ANYMORE(1800289),
	ELAPSED_TIME(1911119),
	TIME_REMAINING(1911120),
	I_FEEL_STRONG_MAGIC_FLOW(1801111),
	I_HAVENT_EATEN_ANYTHING_IM_SO_WEAK(1800290),
	WE_MUST_SEARCH_HIGH_AND_LOW_IN_EVERY_ROOM_FOR_THE_READING_DESK_THAT_CONTAINS_THE_BOOK_WE_SEEK(1029450),
	REMEMBER_THE_CONTENT_OF_THE_BOOKS_THAT_YOU_FOUND(1029451),
	IT_SEEMS_THAT_YOU_CANNOT_REMEMBER_TO_THE_ROOM_OF_THE_WATCHER_WHO_FOUND_THE_BOOK(1029452),
	YOUR_WORK_HERE_IS_DONE_SO_RETURN_TO_THE_CENTRAL_GUARDIAN(1029453),
	THE_GUARDIAN_OF_THE_SEAL_DOESNT_SEEM_TO_GET_INJURED_AT_ALL_UNTIL_THE_BARRIER_IS_DESTROYED(1029550),
	THE_DEVICE_LOCATED_IN_THE_ROOM_IN_FRONT_OF_THE_GUARDIAN_OF_THE_SEAL_IS_DEFINITELY_THE_BARRIER_THAT_CONTROLS_THE_GUARDIANS_POWER(1029551),
	TO_REMOVE_THE_BARRIER_YOU_MUST_FIND_THE_RELICS_THAT_FIT_THE_BARRIER_AND_ACTIVATE_THE_DEVICE(1029552),
	ALL_THE_GUARDIANS_WERE_DEFEATED_AND_THE_SEAL_WAS_REMOVED(1029553),
	WHAT_TOOK_SO_LONG_I_WAITED_FOR_EVER(1029350),
	I_MUST_ASK_LIBRARIAN_SOPHIA_ABOUT_THE_BOOK(1029351),
	THIS_LIBRARY(1029352),
	AN_UNDERGROUND_LIBRARY(1029353),
	THE_BOOK_THAT_WE_SEEK_IS_CERTAINLY_HERE(1029354),
	YOU_FOOLISH_INVADERS_WHO_DISTURB_THE_REST_OF_SOLINA_BE_GONE_FROM_THIS_PLACE(1029460),
	I_KNOW_NOT_WHAT_YOU_SEEK_BUT_THIS_TRUTH_CANNOT_BE_HANDLED_BY_MERE_HUMANS(1029461),
	I_WILL_NOT_STAND_BY_AND_WATCH_YOUR_FOOLISH_ACTIONS(1029462),
	COME_AND_EAT(1801117),
	LOOKS_DELICIOUS(1801118),
	LETS_GO_EAT(1801119),
	HALL_OF_SUFFERING(1800240),
	HALL_OF_EROSION(1800241),
	HEART_OF_IMMORTALITY(1800242),
	YOU_CAN_HEAR_THE_UNDEAD_OF_EKIMUS_RUSHING_TOWARD_YOU(1800263),
	THE_TUMOR_INSIDE_S1_HAS_BEEN_DESTROYED_NIN_ORDER_TO_DRAW_OUT_THE_COWARDLY_COHEMENES_YOU_MUST_DESTROY_ALL_THE_TUMORS(1800274),
	THE_TUMOR_INSIDE_S1_HAS_COMPLETELY_REVIVED(1800275),
	ALL_THE_TUMORS_INSIDE_S1_HAVE_BEEN_DESTROYED_DRIVEN_INTO_A_CORNER_COHEMENES_APPEARS_CLOSE_BY(1800299),
	S1S_PARTY_HAS_MOVED_TO_A_DIFFERENT_LOCATION_THROUGH_THE_CRACK_IN_THE_TUMOR(1800247),
	S1S_PARTY_HAS_ENTERED_THE_CHAMBER_OF_EKIMUS_THROUGH_THE_CRACK_IN_THE_TUMOR(1800248),
	EKIMUS_HAS_SENSED_ABNORMAL_ACTIVITY(1800249),
	CMON_CMON_SHOW_YOUR_FACE_YOU_LITTLE_RATS_LET_ME_SEE_WHAT_THE_DOOMED_WEAKLINGS_ARE_SCHEMING(1800233),
	IMPRESSIVE(1800234),
	KYAHAHA_SINCE_THE_TUMOR_HAS_BEEN_RESURRECTED_I_NO_LONGER_NEED_TO_WASTE_MY_TIME_ON_YOU(1800235),
	KEU(1800236),
	S1_MINUTES_ARE_REMAINING(1010643),
	CONGRATULATIONS_YOU_HAVE_SUCCEEDED_AT_S1_S2_THE_INSTANCE_WILL_SHORTLY_EXPIRE(1800245),
	YOU_HAVE_FAILED_AT_S1_S2(1800246),
	YOU_WILL_PARTICIPATE_IN_S1_S2_SHORTLY(1800262),
	I_SHALL_ACCEPT_YOUR_CHALLENGE_S1_COME_AND_DIE_IN_THE_ARMS_OF_IMMORTALITY(1800261),
	THE_TUMOR_INSIDE_S1_THAT_HAS_PROVIDED_ENERGY_N_TO_EKIMUS_IS_DESTROYED(1800302),
	THE_TUMOR_INSIDE_S1_HAS_BEEN_COMPLETELY_RESURRECTED_N_AND_STARTED_TO_ENERGIZE_EKIMUS_AGAIN(1800303),
	WITH_ALL_CONNECTIONS_TO_THE_TUMOR_SEVERED_EKIMUS_HAS_LOST_ITS_POWER_TO_CONTROL_THE_FERAL_HOUND(1800269),
	WITH_THE_CONNECTION_TO_THE_TUMOR_RESTORED_EKIMUS_HAS_REGAINED_CONTROL_OVER_THE_FERAL_HOUND(1800270),
	THERE_IS_NO_PARTY_CURRENTLY_CHALLENGING_EKIMUS(1800229),
	YOU_CAN_FEEL_THE_SURGING_ENERGY_OF_DEATH_FROM_THE_TUMOR(1800264),
	THE_AREA_NEAR_THE_TUMOR_IS_FULL_OF_OMINOUS_ENERGY(1800265),
	THE_TUMOR_INSIDE_S1_HAS_BEEN_DESTROYED_NTHE_NEARBY_UNDEAD_THAT_WERE_ATTACKING_SEED_OF_LIFE_START_LOSING_THEIR_ENERGY_AND_RUN_AWAY(1800300),
	THE_TUMOR_INSIDE_S1_HAS_COMPLETELY_REVIVED_(1800301),
	THE_TUMOR_INSIDE_S1_HAS_BEEN_DESTROYED_NTHE_SPEED_THAT_EKIMUS_CALLS_OUT_HIS_PREY_HAS_SLOWED_DOWN(1800304),
	THE_TUMOR_INSIDE_S1_HAS_COMPLETELY_REVIVED__(1800305),
	BRING_MORE_MORE_SOULS(1800306),
	HA_HA_HA(7164),
	THE_SOUL_COFFIN_HAS_AWAKENED_EKIMUS(1800232),
	YUMYUM_YUMYUM(1800291),
	OLYMPIAD_CLASS_FREE_INDIVIDUAL_MATCH_IS_GOING_TO_BEGIN_IN_ARENA__S1_IN_A_MOMENT(1300166),
	OLYMPIAD_CLASS_INDIVIDUAL_MATCH_IS_GOING_TO_BEGIN_IN_ARENA__S1_IN_A_MOMENT(1300167),
	OLYMPIAD_CLASS_FREE_TEAM_MATCH_IS_GOING_TO_BEGIN_IN_ARENA__S1_IN_A_MOMENT(1300132);
	
	private final int _id;
	private final int _size;
	
	NpcString(int id)
	{
		_id = id;
		_size = name().contains("S4") ? 4 : name().contains("S3") ? 3 : name().contains("S2") ? 2 : name().contains("S1") ? 1 : 0;
	}
	
	public static NpcString valueOf(int id)
	{
		for(NpcString m : NpcString.values())
		{
			if(m.getId() != id)
				continue;
			return m;
		}
		throw new NoSuchElementException("Not find NpcString by id: " + id);
	}
	
	public int getId()
	{
		return _id;
	}
	
	public int getSize()
	{
		return _size;
	}
}