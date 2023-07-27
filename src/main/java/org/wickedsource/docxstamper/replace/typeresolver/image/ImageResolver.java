package org.wickedsource.docxstamper.replace.typeresolver.image;

import org.docx4j.dml.wordprocessingDrawing.Inline;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.openpackaging.parts.WordprocessingML.BinaryPartAbstractImage;
import org.docx4j.wml.Drawing;
import org.docx4j.wml.ObjectFactory;
import org.docx4j.wml.R;
import org.wickedsource.docxstamper.api.DocxStamperException;
import org.wickedsource.docxstamper.api.typeresolver.ITypeResolver;

import java.util.Random;

import static org.docx4j.openpackaging.parts.WordprocessingML.BinaryPartAbstractImage.createImagePart;

/**
 * This ITypeResolver allows context objects to return objects of type Image. An expression that resolves to an Image
 * object will be replaced by an actual image in the resulting .docx document. The image will be put as an inline into
 * the surrounding paragraph of text.
 *
 * @author joseph
 * @version $Id: $Id
 */
public class ImageResolver implements ITypeResolver<Image> {

	private static final Random random = new Random();

    /**
     * Creates a run containing the given image.
     *
     * @param filenameHint  filename hint for the image
     * @param altText       alt text for the image
     * @param maxWidth      max width of the image
     * @param abstractImage the image
     * @return the run containing the image
     */
	public static R createRunWithImage(
			String filenameHint,
			String altText,
			Integer maxWidth,
			BinaryPartAbstractImage abstractImage
	) {
		// creating random ids assuming they are unique
		// id must not be too large, otherwise Word cannot open the document
		int id1 = random.nextInt(100000);
		int id2 = random.nextInt(100000);
		if (filenameHint == null) filenameHint = "dummyFileName";
		if (altText == null) altText = "dummyAltText";

		Inline inline = tryCreateImageInline(filenameHint, altText, maxWidth, abstractImage, id1, id2);

		// Now add the inline in w:p/w:r/w:drawing
		ObjectFactory factory = new ObjectFactory();
		R run = factory.createR();
		Drawing drawing = factory.createDrawing();
		run.getContent().add(drawing);
		drawing.getAnchorOrInline().add(inline);

		return run;

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public R resolve(WordprocessingMLPackage document, Image image) {
        try {
            // TODO: adding the same image twice will put the image twice into the docx-zip file. make the second
            //       addition of the same image a reference instead.
            return createRunWithImage(
                    image.getFilename(),
                    image.getAltText(),
                    image.getMaxWidth(),
                    createImagePart(document, image.getImageBytes())
            );
        } catch (Exception e) {
            throw new DocxStamperException("Error while adding image to document!", e);
        }
    }

	private static Inline tryCreateImageInline(String filenameHint, String altText, Integer maxWidth, BinaryPartAbstractImage abstractImage, int id1, int id2) {
		try {
			return maxWidth == null
					? abstractImage.createImageInline(filenameHint, altText, id1, id2, false)
					: abstractImage.createImageInline(filenameHint, altText, id1, id2, false, maxWidth);
		} catch (Exception e) {
			throw new DocxStamperException(e);
		}
	}

}
