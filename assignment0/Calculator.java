public class Calculator {
	static final int NUMBER_OF_ARGUMENTS = 3;
	static final String ERROR_MESSAGE = "Error in expression";

	public static void main(String[] args) {
		if (args.length != NUMBER_OF_ARGUMENTS) {
			System.out.println(ERROR_MESSAGE);
		} else {
			try {
				int a = Integer.parseInt(args[0]);
				int b = Integer.parseInt(args[2]);
				String op = args[1];
				int result = 0;
				switch (op) {
					case "+":
						result = a + b;
						break;
					case "-":
						result = a - b;
						break;
					case "*":
						result = a * b;
						break;
					case "/":
						result = a / b;
						break;
					default:
						System.out.println(ERROR_MESSAGE);
						break;
				}
				System.out.println(a + " " + op + " " + b + " = " + result);
			} catch (Exception e) {
				System.out.println(ERROR_MESSAGE);
			}
		}
	}
}