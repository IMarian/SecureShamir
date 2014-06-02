package org.marianilascu.secureshamir;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Hashtable;
import java.util.Random;

public class SecureShamir {
	/**
	 * Byte size of the generated prime number that will serve as a field
	 */
	private final static int BIT_LENGTH = 128;
	
	/**
	 * The secret values that will be shared
	 */
	private int[] values;
	
	/** 
	 * Minimum number of agents able to reconstruct the secret
	 */
	private int k;
	
	/**
	 * Number of servers
	 */
	private int n;
	
	/**
	 * Secret x values
	 */
	private ArrayList<BigInteger[]> x;
	
	/**
	 * Shares that will be distributed among the servers
	 */
	private ArrayList<BigInteger[]> shares;
	
	/**
	 * Quotients of the k-1 degree generated function
	 */
	private BigInteger[] quotients;
	
	/**
	 * Large prime security parameter
	 */
	private BigInteger largePrime;
	
	/**
	 * Subsets of the [0...largePrime] set that will be associated to
	 * each value
	 */
	private Hashtable<Integer, BigInteger[]> domains;
	/**
	 * 
	 * @param value
	 * @param k
	 * @param n
	 */
	public SecureShamir(int[] values, int k, int n) {
		Arrays.sort(values);
		this.values = values;
		this.k = k;
		this.n = n;
		this.largePrime = this.generatePrime(SecureShamir.BIT_LENGTH);
		this.x = new ArrayList<BigInteger[]>(values.length);
		this.shares = new ArrayList<BigInteger[]>(values.length);
		this.domains = new Hashtable<Integer, BigInteger[]>(values.length);
		
		System.out.println("Field: " + this.largePrime);
	}
	
	public ArrayList<BigInteger[]> buildShares() {
		this.splitDomain();
		
		for(int i = 0; i < values.length; i++) {
			this.shares.add(buildValueShares(this.values[i]));
		}
		
		return shares;
	}
	
	/**
	 * Reconstruct secret value from shares using Lagrange interpolation
	 * @param shares
	 * @return The secret that will be obtained by interpolating the values from each server
	 */
	public BigInteger reconstructSecretValue(BigInteger[] shareValues) {
		BigInteger[] x = this.x.get(this.shares.indexOf(shareValues));
		BigInteger secret = BigInteger.ZERO;
		
		for(int i = 0; i < k; i++) {
			BigInteger prod = BigInteger.ONE;
			
			for(int j = 0; j < k; j++) {
				if(j != i) {
					// prod = prod * (x[j] / (x[j] - x[i]))
					prod = prod.multiply(x[j].multiply((x[j].subtract(x[i])).modInverse(this.largePrime)));
				}
			}
			
			secret = secret.add(shareValues[i].multiply(prod));
		}
		
		return secret.mod(this.largePrime);
	}
	
	/**
	 * Build n shares that will be distributed among n servers
	 * @return The shares that will be distributed to each of n servers
	 */
	private BigInteger[] buildValueShares(int value) {
		BigInteger[] secret = new BigInteger[n];
		this.quotients = new BigInteger[this.k-1];
		BigInteger[] shareValues = new BigInteger[n];
		
		System.out.println("Generating values between " + this.domains.get(value)[0] + " and " + this.domains.get(value)[1]);
		/*
		 * Generate polynomial quotients
		 */
		for(int i = 0; i < this.k - 1; i++) {
			quotients[i] = this.generateFieldElement(this.domains.get(value)[0], this.domains.get(value)[1]);
			System.out.println("Quotient: " + quotients[i]);
		}
		
		/*
		 * Generate n secret values x and
		 * calculate each value of generated polynom in x
		 */
		for(int i = 0; i < this.n; i++) {
			secret[i] = this.generateFieldElement(this.domains.get(value)[0], this.domains.get(value)[1]);
			shareValues[i] = BigInteger.valueOf(value).add(this.valueInX(quotients, secret[i]));
		}
		
		this.x.add(secret);
		return shareValues;
	}
	
	/**
	 * Split [0...largePrime] into the same amount as the number of values
	 */
	public void splitDomain() {
		BigInteger split = this.largePrime.divide(BigInteger.valueOf(this.values.length));
		BigInteger currentLimit = BigInteger.ZERO;
		
		for(int i = 0; i < this.values.length; i++) {
			BigInteger[] limits = new BigInteger[2];
			limits[0] = currentLimit;
			
			if(this.largePrime.compareTo(currentLimit.add(split)) != -1) {
				limits[1] = currentLimit.add(split);
			} else {
				limits[1] = this.largePrime;
			}
			
			this.domains.put(this.values[i], limits);
			
			//System.out.println("Limits: [" + limits[0] + ", " + limits[1] + "]");
			currentLimit = currentLimit.add(split.add(BigInteger.ONE));
			//System.out.println("Lower than large prime: " + (this.largePrime.compareTo(limits[1]) != -1));
		}
	}
	
	/**
	 * Find value of specific function in point x
	 * @param quotients
	 * @param x
	 * @return
	 */
	private BigInteger valueInX(BigInteger[] quotients, BigInteger x) {
		BigInteger result = BigInteger.ZERO;
		
		for(int j = 0; j < quotients.length; j++) {
			// result = result + quotients[j] * x^(j+1)
			result = result.add(quotients[j].multiply(x.modPow(BigInteger.valueOf(j + 1), this.largePrime)));
		}
//		for(int i = 0; i < quotients.length; i++) {
//			result = quotients[i].add(x.multiply(result)));
//		}
		
		return result;
	}
	
	/**
	 * Generate prime number of specific byte size
	 * @param length
	 * @return
	 */
	private BigInteger generatePrime(int length) {
		BigInteger prime = BigInteger.ZERO;
		
		do{
			prime = BigInteger.probablePrime(length, new Random());
		} while(!prime.isProbablePrime(1));
		
		return prime;
	}
	
	/**
	 * Generate an element from a specific field
	 * @param field
	 * @return
	 */
	private BigInteger generateFieldElement(BigInteger lowerLimit, BigInteger higherLimit) {
		BigInteger c;
		do {
			c = new BigInteger(SecureShamir.BIT_LENGTH, new Random());
		} while(c.compareTo(lowerLimit) < 0 || c.compareTo(higherLimit) >= 0);
		return c;
	}
}