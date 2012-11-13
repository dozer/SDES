import java.util.*;
/**
 * Test the implementation of SDES
 * 
 * @author (sdb) 
 * @version (Sep 2010)
 * 
 * Modified by:
 * @author Bill Girtain
 * @author Jim Wakemen
 * @author Scott Stephenson
 */
public class Driver
{
   public static void main(String args[])
{   SDES sdes = new SDES();

    Scanner scanner = new Scanner (System.in);


//    String plain = "x";
//    System.out.println ("Enter plain text, or hit 'Enter' to terminate");
//        plain = scanner.nextLine();
//    byte [] cipher;
//    while (plain.length() > 0)
//    {   
//        sdes.getKey10(scanner);
//        sdes.show(sdes.byteToBitArray(plain.substring(0, 1).getBytes()[0], 8));
//        cipher = sdes.encrypt  (plain);
//        System.out.print ("Cipher is ");
//        sdes.show (cipher);
//        System.out.println (sdes.byteArrayToString (sdes.decrypt (cipher)));
//        sdes.show(sdes.byteToBitArray(cipher[0], 8));
//        System.out.println ("Enter plain text, or hit 'Enter' to terminate");
//        plain = scanner.nextLine();
//    }
    
    System.out.println("Enter the magic key to get the super secret message!");
    sdes.getKey10(scanner);
    byte[] ciph = {-115, -17, -47, -113, -43, -47, 15, 84, -43, -113, -17, 84, -43 ,79 ,58 ,15 ,64 ,-113 ,-43, 65 ,-47, 127, 84, 64, -43, -61, 79 ,-43, 93, -61, -14, 15, -43, -113, 84, -47, 127, -43, 127, 84, 127,10, 84, 15, 64, 43};
   // sdes.show(sdes.decrypt(ciph));
    System.out.println (sdes.byteArrayToString (sdes.decrypt (ciph)));
    
    byte[] answer = {-126, 58, -86, -86, 62, -43, 76, 58 ,127, 62, -43, 40, -33, -61, -113, -113};
    System.out.println(sdes.byteArrayToString(sdes.decrypt(answer)));
}
}
