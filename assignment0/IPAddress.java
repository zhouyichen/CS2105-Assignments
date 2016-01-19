import java.util.Scanner;

public class IPAddress {
	private static Scanner scanner = new Scanner(System.in);
	private static final int SUB_STRING_LENGTH = 8;
	private static final int STRING_LENGTH = 32;

	public static void main(String[] args) {
		String binaryString = scanner.nextLine();
		
		String output = "";
		for (int i = 0; i < STRING_LENGTH; i += SUB_STRING_LENGTH) {
			String subString = binaryString.substring(i, i + SUB_STRING_LENGTH);
			output += binaryToDecimal(subString);
			if (i < STRING_LENGTH - SUB_STRING_LENGTH) {
				output += ".";
			}
		}
		System.out.println(output);
	}

	/**
	 * Convert binary substring of length 8 to decimal representation
	 * @param  binarySubString [description]
	 * @return                 [description]
	 */
	static String binaryToDecimal(String binarySubString) {
		int result = 0;
		for (int i = 0; i < SUB_STRING_LENGTH; i++){
			char number = binarySubString.charAt(i);
			if (number == '1') {
				result += Math.pow(2, SUB_STRING_LENGTH - 1 - i);
			}
		}
		return String.valueOf(result);
	}
}