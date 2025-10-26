package sions.android.sionsbeat.game;

public class GameOption 
{
	public static int[] LEVEL = {
		Integer.MIN_VALUE,
		60,	// 1
		90,	// 2 
		120,	// 3
		150,	// 4
		180,	// 5
		230,	// 6
		280,	// 7
		330,	// 8
		380,	// 9
		430,	// 10
		530,	// 11
		580,	// 12
		630,	// 13
		680,	// 14
		730,	// 15
		780,
		830,
		880,
		930,
		980,
		1030,
		1080,
		1130,
		1180,
		1300,
	};
	

	public int TIMING_EARLY = 40;
	public int TIMING_COMBO = 200;
	public int TIMING_GOOD = 400;
	public int TIMING_GREAT = 560;
	public int TIMING_PERFECT = 720;
	public int TIMING_I_GOOD = 800;
	public int TIMING_I_COMBO = 920;
	public int TIMING_FAILED = 1000;

	public int ANIMATION_TIMING = TIMING_GOOD + ((TIMING_PERFECT-TIMING_GOOD)/2);

	public int ANIMATION_TOUCH = 300;
	
	

	
}
