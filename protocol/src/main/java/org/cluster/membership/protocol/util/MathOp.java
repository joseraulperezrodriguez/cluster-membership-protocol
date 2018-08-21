package org.cluster.membership.protocol.util;

public class MathOp {

	public static int log2n(int n) {
		int base = 2;
		int iterations = 0;
		
		while((base*=2) <= n*2) iterations++;
		
		return iterations;
		
	}
	
}