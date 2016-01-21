import java.io.*;

public class Copier {
	public static void main(String[] args) {
		try {
			String inputFile = args[0];
			String outputFile = args[1];

			byte[] buffer = new byte[1000];
			FileInputStream fis = new FileInputStream(inputFile);
			BufferedInputStream bis = new BufferedInputStream(fis);

			FileOutputStream fos = new FileOutputStream(outputFile);
			BufferedOutputStream bos = new BufferedOutputStream(fos);
			
			int numBytes = bis.read(buffer);

			while (numBytes > 0) {
				bos.write(buffer, 0, numBytes);
				numBytes = bis.read(buffer);
			}

			System.out.println(inputFile + " is successfully copied to " + outputFile);
			bis.close();
			bos.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}