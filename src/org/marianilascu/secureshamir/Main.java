package org.marianilascu.secureshamir;

import java.math.BigInteger;
import java.util.ArrayList;

public class Main {
	public static void main(String[] args) {
		SecureShamir shamir = new SecureShamir(new BigInteger[] { new BigInteger("4354563465464562"), new BigInteger("4354563465464562"), new BigInteger("867945634547654746756"), new BigInteger("67645635546")}, 3, 5);
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
