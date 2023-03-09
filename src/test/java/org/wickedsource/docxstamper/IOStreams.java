package org.wickedsource.docxstamper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wickedsource.docxstamper.util.ThrowingSupplier;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

public class IOStreams {
	public static final boolean KEEP_OUTPUT_FILE = Boolean.parseBoolean(System.getenv()
																			  .getOrDefault("keepOutputFile", "false"));
	private static final Map<OutputStream, Supplier<InputStream>> streams = new HashMap<>();


	private static final Logger logger = LoggerFactory.getLogger(TestDocxStamper.class);

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
			Supplier<InputStream> in = () -> new ByteArrayInputStream(out.toByteArray());
			streams.put(out, in);
			return out;
		}
	}

	public static InputStream getInputStream(OutputStream out) {
		return streams.get(out).get();
	}
}
