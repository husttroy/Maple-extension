import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.zip.Deflater;
import java.util.zip.DeflaterOutputStream;


public class Example {
	public byte[] compress(String s) throws IOException{
		Deflater deflater = new Deflater();
		
		byte[] input;
		ByteArrayOutputStream stream = new ByteArrayOutputStream();
		Deflater compresser = new Deflater(Deflater.BEST_COMPRESSION, true);
		DeflaterOutputStream deflaterOutputStream = new DeflaterOutputStream(stream, compresser);
		deflaterOutputStream.write(input);
		deflaterOutputStream.close();
		
		byte[] output;
		return output;
	}
}
