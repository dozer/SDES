import java.util.Scanner;

/**
 * Simplified Data Encryption Standard. 
 * 
 * @author Bill Girtain
 * @author Jim Wakemen
 * @author Scott Stephenson
 */
public class SDES {
	// Permutation vectors
	private static final int[] IP = {1, 5, 2, 0, 3, 7, 4, 6};
	private static final int[] IP_INVERSE = {3, 0, 2, 4, 6, 1, 7, 5};
	private static final int[] K1 = {0, 6, 8, 3, 7, 2, 9, 5};
	private static final int[] K2 = {7, 2, 5, 4, 9, 1, 8, 0};
	private static final int[] EP = {3, 0, 1, 2, 1, 2, 3, 0};
	private static final int[] P4 = {1, 3, 2, 0};
	
	// S-box 0
	private static final boolean[][][] S0 = {{{false, true},  {false, false}, {true, true},  {true, false}},
		{{true, true},   {true, false},  {false, true}, {false, false}},
		{{false, false}, {true, false},  {false, true}, {true, true}},
		{{true, true},   {false, true},  {true, true},  {true, false}}};

	// S-box 1
	private static final boolean[][][] S1 = {{{false, false}, {false, true},  {true, false},  {true, true}},
		{{true, false},  {false, false}, {false, true},  {true, true}},
		{{true, true},   {false, false}, {false, true},  {false, false}},
		{{true, false},  {false, true},  {false, false}, {true, true}}};

	private boolean[] key;

	/**
	 * Get a 10 bit key from the keyboard, such as 101010101. Store it as
	 *  an array of booleans in a field.
	 * @param scanner The scanner to read the key
	 * @throws IllegalArgumentException 
	 */
	public void getKey10 (Scanner scanner) throws IllegalArgumentException{
		String tmp_key = "";
		System.out.println("Please enter a 10-bit key: ");
		if (scanner.hasNextLine())
			tmp_key = scanner.nextLine();
		if (tmp_key.length() != 10)
			throw new IllegalArgumentException("The entered key" +
					" must be 10 bits long.");
		key = new boolean[10];

		for (int i = 0; i < tmp_key.length(); i++)
		{
			if (tmp_key.charAt(i) == '1')
				key[i] = true;
			else if(tmp_key.charAt(i) == '0')
				key[i] = false;
			else
				throw new IllegalArgumentException("The entered key has an invalid character");
		}
	}

	/**
	 * Convert the given byte array to a String
	 * @param inp An array of bytes, hopefully storing the codes of printable
	 *  characters.
	 * @return The characters as a String.
	 */
	public String byteArrayToString(byte [ ] inp){
		String s = "";
		for (byte b : inp)
		{
			s += (char) b;
		}

		return s;
	}

	/**
	 * Left half of x, L(x)
	 * @param inp The input to be split
	 * @return a bit array which is the left half of the parameter, inp.
	 */
	public boolean[] lh (boolean [ ] inp){
		boolean lh[] = new boolean[(inp.length/2)];
		for (int i = 0; i < (inp.length/2); i++)
		{
			lh[i] = inp[i];
		}
		return lh;
	}

	/**
	 * Right half of x, R(x)
	 * @param inp The input to be split
	 * @return a bit array which is the right half of the parameter, inp.
	 */
	public  boolean[] rh (boolean [ ] inp){
		boolean rh[] = new boolean[(inp.length/2)];
		for (int i = inp.length/2; i < inp.length; i++)
		{
			rh[i-rh.length] = inp[i];
		}
		return rh;
	}

	/**
	 * Exclusive OR. x and y must have the same length. x xor y is the
	 *  same as x != y
	 * @param x The first boolean array
	 * @param y The second boolean array
	 * @return The XOR of x and y
	 */
	public  boolean[] xor (boolean [ ] x, boolean [ ] y){
		if (x.length != y.length)
		{
			throw new IllegalArgumentException("Both arrays must be the same size");
		}
		boolean result[] = new boolean[x.length];
		for (int i = 0; i < x.length; i++)
		{
			result[i] = x[i]^y[i];
		}
		return result;
	}

	/**
	 * Concatenate the two bit arrays, x || y
	 * @param x The first boolean array
	 * @param y The second boolean array
	 * @return The concatenation of x and y
	 */
	public  boolean[] concat (boolean [ ] x, boolean [ ] y){
		boolean[] result = new boolean[x.length + y.length];
		int i = 0;
		while (i < x.length)
		{
			result[i] = x[i];
			i++;
		}
		for(int a = 0; a < y.length; a++)
		{
			result[i] = y[a];
			i++;
		}
		return result;
	}

	/**
	 * Convert the given bit array to a single byte
	 * @param inp A bit array, max length is 8 bits
	 * @return The byte
	 */
	public  byte bitArrayToByte (boolean [ ] inp){
		if (inp.length > 8)
			throw new IllegalArgumentException("Input must be 8 bits long");

		byte result = 0;
		for (int i = 0; i < inp.length; i++)
		{
			result *= 2;
			result += inp[i]?1:0;
		}
		return result;
	}

	/**
	 * Convert the given byte to a bit array, of the given size.
	 * @param b
	 * @param size The size of the resulting bit array. The operator
	 *   >>> can be used for an unsigned right shift.
	 * @return The boolean array
	 */
	public  boolean[] byteToBitArray (byte b, int size){
		boolean[] result = new boolean[size];
		for (int i = size-1; i >= 0; i--)
		{
			result[i] = (b&0x01)==1;
			b >>>= 1;

		}
		return result;
	}

	/**
	 * Encrypt the given string using SDES Each character produces a byte of cipher.
	 * @param msg The message to encrypt
	 * @return An array of bytes representing the cipher text.
	 */
	public byte[] encrypt (String msg){
		byte[] cipher = msg.getBytes();
		for (int i=0; i < cipher.length; i++) {
			cipher[i] = encryptByte(cipher[i]);
		}
		return cipher;
	}
	
	/**
	 * Decrypt the given byte array.
	 * @param cipher An array of bytes representing the cipher text.
	 * @return An array of bytes representing the original plain text.
	 */
	public byte[] decrypt (byte [] cipher){
		byte[] plain = new byte[cipher.length];
		for (int i=0; i < cipher.length; i++) {
			plain[i] = decryptByte(cipher[i]);
		}
		return plain;
	}
	
	/**
	 * Encrypt a single byte using SDES
	 * @param b The byte to encrypt
	 * @return The encrypted byte
	 */
	public byte encryptByte (byte b){
		// convert byte to an array of bits
		boolean[] t = byteToBitArray(b, 8);
		// calculate the sub keys 
		boolean[] k1 = expPerm(key, K1);
		boolean[] k2 = expPerm(key, K2);
		t = expPerm(t, IP);
		t = f(t, k1); // round 1
		t = concat (rh(t), lh(t)); // swap nybbles
		t = f(t, k2); // round 2
		t = expPerm(t, IP_INVERSE);
		// return the result as a byte
		return bitArrayToByte(t);
	}
	
	/**
	 * Decrypt a single byte using SDES 
	 * @param b The byte to decrypt
	 * @return The decrypted byte
	 */
	public byte decryptByte (byte b){
		// convert byte to an array of bits
		boolean[] t = byteToBitArray(b, 8);
		// calculate the sub keys
		boolean[] k1 = expPerm(key, K1);
		boolean[] k2 = expPerm(key, K2);
		t = expPerm(t, IP);
		t = f(t, k2); // round 1
		t = concat (rh(t), lh(t)); // swap nybbles
		t = f(t, k1); // round 2
		t = expPerm(t, IP_INVERSE);
		// return the result as a byte
		return bitArrayToByte(t);
	}
	
	/**
	 * Send the byteArray to stdout 
	 * @param byteArray The byteArray to send to stdout
	 */
	public void show(byte [ ] byteArray){
		for (int i = 0; i < byteArray.length; i++) {
			System.out.print(String.format("%d ", byteArray[i]));
		}
		System.out.println();
	}
	
	/**
	 * Send the bitArray to stdout as 1's and 0's 
	 * @param inp The bitArray to send to stdout
	 */
	public void show(boolean [ ] inp){
		for (int i = 0; i < inp.length; i++)
			System.out.print(inp[i]?1:0);
		System.out.println();
	}
	
	/**
	 * Expand and/or permute and/or select from the bit array, 
	 * inp, producing an expanded/permuted/selected bit array.
	 *  Use the expansion/permutation vector epv. 
	 * @param inp A bit array represented as booleans, true=1, false=0.
	 * @param epv An expansion and/or permutation and/or selection vector; 
	 *   all numbers in epv must be in the range 0..inp.length, 
	 *   i.e. they must be valid subscripts for inp. 
	 * @return The permuted/expanded/selected bit array, or null
	 *   if there is an error. 
	 * @throws java.lang.IndexOutOfBoundsException
	 */
	public boolean[] expPerm (boolean[ ] inp, int[ ] epv) 
			throws IndexOutOfBoundsException {
		boolean [] result = new boolean [epv.length];
		for (int i = 0; i < epv.length; i++)
			if (epv[i] >= 0 && epv[i] < inp.length)
				result[i] = inp[epv[i]];
			else
				throw new IndexOutOfBoundsException(String.format("Item %d Value %d not in input range.", i, epv[i]));
		return result;
	}

	/**
	 * This is the 'round' function. It is its own inverse. f(x,k) = (L(x)
	 *   xor F(R(x), k)) || R(x) 
	 * @param x The input bits
	 * @param k The key
	 * @return The resulting bits
	 */
	public boolean[] f(boolean[] x, boolean[] k){
		boolean[] feist = feistel(k, rh(x));	// F(R(x), k)
		boolean[] left = xor(lh(x), feist);		// L(x) xor F
		return concat(left, rh(x));				// left || R(x)
	}

	/**
	 * F(k,x) is a Feistel function F(k,x) = P4 (s0 (L (k xor EP(x))) ||
	 *    s1 (R (k xor EP(x))) 
	 * @param k The key
	 * @param x The input bits
	 * @return The resulting bits
	 */
	public boolean[] feistel(boolean[] k, boolean[] x){
		boolean[] sZero = s0(lh(xor(k, expPerm(x, EP))));	// S0(L(k xor EP(x)))
		boolean[] sOne = s1(rh(xor(k, expPerm(x, EP))));	// s1(R(k xor EP(x)))
		return expPerm(concat(sZero, sOne), P4);			// P4(s0 || s1)
	}

	/**
	 * Does the S-box 0 on x
	 * @param x The 4 bit input
	 * @return The 2 bit result
	 * @throws IllegalArgumentException
	 */
	private boolean[] s0(boolean[] x) throws IllegalArgumentException{
		if(x.length != 4)
			throw new IllegalArgumentException("The length for the s-box must be 4");
		int row, col;
		row = x[0] ? 2 : 0;
		row += x[3] ? 1 : 0;

		col = x[1] ? 2 : 0;
		col += x[2] ? 1 : 0;

		return S0[row][col];
	}

	/**
	 * Does the S-box 1 on x
	 * @param x The 4 bit input
	 * @return The 2 bit result
	 * @throws IllegalArgumentException
	 */
	private boolean[] s1(boolean[] x) throws IllegalArgumentException{
		if(x.length != 4)
			throw new IllegalArgumentException("The length for the s-box must be 4");
		int row, col;
		row = x[0] ? 2 : 0;
		row += x[3] ? 1 : 0;

		col = x[1] ? 2 : 0;
		col += x[2] ? 1 : 0;

		return S1[row][col];
	}
}
