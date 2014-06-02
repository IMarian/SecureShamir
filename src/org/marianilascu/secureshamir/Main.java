package org.marianilascu.secureshamir;

import java.math.BigInteger;
import java.util.ArrayList;

public class Main {
	public static void main(String[] args) {
		SecureShamir shamir = new SecureShamir(new int[] {432, 256, 375, 192}, 3, 5);
		ArrayList<BigInteger[]> shares = shamir.buildShares();
		
		for(BigInteger[] valueShares : shares) {
			for(int i = 0; i < valueShares.length; i++) {
				System.out.println(valueShares[i] + " ");
			}
			
			System.out.println("Secret value: " + shamir.reconstructSecretValue(valueShares));
		}
		
		shamir.splitDomain();
	}
}
