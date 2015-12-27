package org.wickedsource.docxstamper.docx4j.commentprocessing;

import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.junit.Assert;
import org.junit.Test;
import org.wickedsource.docxstamper.docx4j.processor.CommentProcessorRegistry;
import org.wickedsource.docxstamper.docx4j.processor.ICommentProcessor;
import org.wickedsource.docxstamper.docx4j.processor.displayif.DisplayIfProcessor;
import org.wickedsource.docxstamper.docx4j.processor.displayif.IDisplayIfProcessor;

import java.lang.reflect.Method;

public class CommentProcessorRegistryTest {

    @Test
    public void emptyRegistry() throws NoSuchMethodException {
        CommentProcessorRegistry registry = new CommentProcessorRegistry();
        Assert.assertTrue(registry.getRegisteredInterfaces().isEmpty());
        Assert.assertNull(registry.getProcessorForInterface(IDisplayIfProcessor.class));
        Assert.assertNull(registry.getProcessorForMethod(IDisplayIfProcessor.class.getMethod("displayParagraphIf", Boolean.class)));
    }

    @Test
    public void getRegisteredInterfacesReturnsAllRegisteredInterfaces() {
        CommentProcessorRegistry registry = new CommentProcessorRegistry();
        registry.registerCommentProcessor(ICommentProcessor.class, new DisplayIfProcessor());
        registry.registerCommentProcessor(IDisplayIfProcessor.class, new DisplayIfProcessor());
        Assert.assertEquals(2, registry.getRegisteredInterfaces().size());
        Assert.assertTrue(registry.getRegisteredInterfaces().contains(ICommentProcessor.class));
        Assert.assertTrue(registry.getRegisteredInterfaces().contains(IDisplayIfProcessor.class));
    }

    @Test
    public void getProcessorMethodsReturnCorrectProcessors() throws NoSuchMethodException {
        CommentProcessorRegistry registry = new CommentProcessorRegistry();
        ICommentProcessor processor1 = new DisplayIfProcessor();
        ICommentProcessor processor2 = new DisplayIfProcessor();
        registry.registerCommentProcessor(ICommentProcessor.class, processor1);
        registry.registerCommentProcessor(IDisplayIfProcessor.class, processor2);
        Assert.assertSame(processor1, registry.getProcessorForInterface(ICommentProcessor.class));
        Assert.assertSame(processor2, registry.getProcessorForInterface(IDisplayIfProcessor.class));
        Assert.assertNull(registry.getProcessorForMethod(CommentProcessorRegistry.class.getMethod("getProcessorForInterface", Class.class)));

        Method displayIf = IDisplayIfProcessor.class.getMethod("displayParagraphIf", new Class[]{Boolean.class});
        Method commitChanges = ICommentProcessor.class.getMethod("commitChanges", WordprocessingMLPackage.class);
        Assert.assertSame(processor2, registry.getProcessorForMethod(displayIf));
        Assert.assertSame(processor1, registry.getProcessorForMethod(commitChanges));
    }


}