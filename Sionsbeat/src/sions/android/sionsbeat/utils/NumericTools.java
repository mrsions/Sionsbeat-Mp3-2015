package sions.android.sionsbeat.utils;

public class NumericTools {

	public static class Integer {
		
		public static int compare (int lhs, int rhs) {
			return lhs < rhs ? -1 : ( lhs == rhs ? 0 : 1 );
		}
		
	}
	
	public static class Float {

		public static int compare (float float1, float float2) {
			// Non-zero, non-NaN checking.
			if (float1 > float2) { return 1; }
			if (float2 > float1) { return -1; }
			if (float1 == float2 && 0.0f != float1) { return 0; }

			// NaNs are equal to other NaNs and larger than any other float
			if (java.lang.Float.isNaN(float1)) {
				if (java.lang.Float.isNaN(float2)) { return 0; }
				return 1;
			} else if (java.lang.Float.isNaN(float2)) { return -1; }

			// Deal with +0.0 and -0.0
			int f1 = java.lang.Float.floatToRawIntBits(float1);
			int f2 = java.lang.Float.floatToRawIntBits(float2);
			// The below expression is equivalent to:
			// (f1 == f2) ? 0 : (f1 < f2) ? -1 : 1
			// because f1 and f2 are either 0 or Integer.MIN_VALUE
			return ( f1 >> 31 ) - ( f2 >> 31 );
		}
	}
	
}
