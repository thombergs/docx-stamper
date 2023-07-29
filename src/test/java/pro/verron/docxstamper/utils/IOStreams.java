package pro.verron.docxstamper.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

/**
 * <p>IOStreams class.</p>
 *
 * @author joseph
 * @version $Id: $Id
 * @since 1.6.5
 */
public class IOStreams {
	/**
	 * Constant <code>KEEP_OUTPUT_FILE=Boolean.parseBoolean(System.getenv()
	 * .getOrDefault(&quot;keepOutputFile&quot;, &quot;false&quot;))</code>
	 */
	public static final boolean KEEP_OUTPUT_FILE = Boolean.parseBoolean(System.getenv()
																			  .getOrDefault("keepOutputFile", "false"));
	private static final Map<OutputStream, Supplier<InputStream>> streams = new HashMap<>();


	private static final Logger logger = LoggerFactory.getLogger(TestDocxStamper.class);

	/**
	 * <p>getOutputStream.</p>
	 *
	 * @return a {@link java.io.OutputStream} object
	 * @throws java.io.IOException if any.
	 */
	public static OutputStream getOutputStream() throws IOException {
		if (KEEP_OUTPUT_FILE) {
			Path temporaryFile = Files.createTempFile(TestDocxStamper.class.getSimpleName(), ".docx");
			logger.info("Saving DocxStamper output to temporary file %s".formatted(temporaryFile));
			OutputStream out = Files.newOutputStream(temporaryFile);
			ThrowingSupplier<InputStream> in = () -> Files.newInputStream(temporaryFile);
			streams.put(out, in);
			return out;
		} else {
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			Supplier<InputStream> inSupplier = () -> new ByteArrayInputStream(out.toByteArray());
			streams.put(out, inSupplier);
			return out;
		}
	}

	/**
	 * <p>getInputStream.</p>
	 *
	 * @param out a {@link java.io.OutputStream} object
	 * @return a {@link java.io.InputStream} object
	 */
	public static InputStream getInputStream(OutputStream out) {
		return streams.get(out).get();
	}
}
