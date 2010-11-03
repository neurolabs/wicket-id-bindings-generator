/*
 * $Id$
 * (c) Copyright 2010 atcard.de
 *
 * Created on 02.11.2010 by Ole Langbehn (ole.langbehn@googlemail.com)
 *
 * This file contains unpublished, proprietary trade secret information of
 * atcard.de. Use, transcription, duplication and
 * modification are strictly prohibited without prior written consent of
 * atcard.de.
 */
package de.frickelbude.wicket;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.Writer;
import java.lang.annotation.Annotation;
import java.util.Arrays;
import java.util.Collection;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.TypeElement;
import javax.tools.JavaFileObject;

/**
 * Provides functionality for {@link Processor}s to work on classes annotated
 * with an annotation and files lying next to them in order to construct
 * interfaces out of them.
 * 
 * @author Ole Langbehn (ole.langbehn@googlemail.com) (initial creation)
 */
public abstract class AbstractBindingProcessor extends AbstractProcessor {

    protected Config config;

    /**
     * Interface for handling a single Class Element.
     * 
     * @author Ole Langbehn (ole.langbehn@googlemail.com) (initial creation)
     */
    public static interface ClassHandler {
        /**
         * Handles the Class Element.
         */
        void handle( TypeElement type );
    }

    /**
     * Extracts the elements that shall become members of the generated
     * interfaces.
     * 
     * @author Ole Langbehn (ole.langbehn@googlemail.com) (initial creation)
     */
    public static interface MemberExtractor {
        /**
         * Extracts the elements that shall become members of the generated
         * interfaces.
         */
        Collection<String> extract() throws Exception;
    }

    @Override
    public synchronized void init( final ProcessingEnvironment processingEnv ) {
        config = new Config( processingEnv );
        super.init( processingEnv );
    }

    protected void handleAnnotatedClasses( final Class<? extends Annotation> annotationClass, final RoundEnvironment roundEnv,
            final ClassHandler classWorker ) {
        for ( final Element element : roundEnv.getElementsAnnotatedWith( annotationClass ) ) {
            if ( element.getKind() == ElementKind.CLASS ) {
                classWorker.handle( (TypeElement) element );
            }
        }
    }

    protected String getPath( final TypeElement type, final String extension ) {
        return type.getQualifiedName().toString().replace( '.', File.separatorChar ) + "." + extension;
    }

    protected InputStream getStream( final String filePath, final TypeElement type ) {

        final String[] folders = config.getSourceFolders();
        InputStream resourceAsStream = null;

        if ( folders.length == 0 ) {

            // from classpath as a last resort
            resourceAsStream = type.getClass().getResourceAsStream( filePath );

            if ( resourceAsStream == null ) {
                LogUtil.warn( processingEnv, type, "Could not load a file from the classpath using the path '%s'.", filePath );
            }

        } else {

            for ( final String folder : folders ) {
                final File file = new File( folder, filePath );
                if ( file.exists() && file.isFile() && file.canRead() ) {
                    try {
                        resourceAsStream = new FileInputStream( file );
                    } catch ( final FileNotFoundException e ) {
                        throw new RuntimeException( e );
                    }
                    break;
                }
            }

            if ( resourceAsStream == null ) {
                LogUtil.warn( processingEnv, type, "Could not load a file from configured folders '%s' using the path '%s'.",
                        Arrays.toString( folders ), filePath );
            }

        }
        return resourceAsStream;
    }

    protected void processFile( final TypeElement type, final MemberExtractor extractor, final String bindingSuffix ) {

        try {

            final Collection<String> members = extractor.extract();
            if ( config.getDebug() ) {
                LogUtil.note( processingEnv, "    found members '%s'.", members.toString() );
            }

            final BindingGenerator bindingGenerator =
                    new BindingGenerator( type.getQualifiedName() + bindingSuffix, members, this.getClass() );
            save( bindingGenerator, type );

        } catch ( final Exception e ) {
            LogUtil.error( processingEnv, type, e.getMessage() );
        }

    }

    protected void save( final BindingGenerator generator, final Element element ) {

        try {
            final JavaFileObject jfo = processingEnv.getFiler().createSourceFile( generator.getClassName() );
            final Writer w = jfo.openWriter();
            w.write( generator.getCode() );
            w.close();
        } catch ( final IOException io ) {
            LogUtil.error( processingEnv, element, io.getMessage() );
        }

    }

}
