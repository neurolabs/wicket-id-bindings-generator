/*
 * Copyright 2010 frickelbude.de
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may
 * not use this file except in compliance with the License. You may obtain a
 * copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */
package de.frickelbude.wicket;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Writer;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.TypeElement;
import javax.tools.JavaFileObject;

/**
 * An annotation processor that generates bindings for wicket templates.
 * 
 * @author Ole Langbehn (ole.langbehn@googlemail.com) (initial creation)
 */
@SupportedAnnotationTypes( { "de.frickelbude.wicket.HasTemplate" } )
@SupportedSourceVersion( SourceVersion.RELEASE_6 )
public class IdBindingProcessor extends AbstractProcessor {

    private Config _config;

    @Override
    public synchronized void init( final ProcessingEnvironment processingEnv ) {
        _config = new Config( processingEnv );
        super.init( processingEnv );
    }

    @Override
    public boolean process( final Set<? extends TypeElement> annotations, final RoundEnvironment roundEnv ) {

        for ( final Element element : roundEnv.getElementsAnnotatedWith( HasTemplate.class ) ) {

            if ( element.getKind() == ElementKind.CLASS ) {

                final TypeElement type = (TypeElement) element;
                final String filePath = getTemplateFilePath( type );
                final InputStream resourceAsStream = getTemplateStream( filePath, type );

                if ( resourceAsStream != null ) {
                    if ( _config.getDebug() ) {
                        LogUtil.note( processingEnv, "processing template with path '%s'.", filePath );
                    }
                    try {
                        processTemplate( type, resourceAsStream );
                    } finally {
                        try {
                            resourceAsStream.close();
                        } catch ( final IOException e ) {
                            LogUtil.error( processingEnv, type, e.getMessage() );
                        }
                    }
                }

            }

        }
        return true;
    }

    private InputStream getTemplateStream( final String filePath, final TypeElement type ) {

        InputStream resourceAsStream = null;
        final String[] templateFolders = _config.getTemplateFolders();

        if ( templateFolders == null ) {

            // from classpath as a last resort
            resourceAsStream = type.getClass().getResourceAsStream( filePath );

            if ( resourceAsStream == null ) {
                LogUtil.warn( processingEnv, type, "Could not load a template from the classpath using the path '%s'.", filePath );
            }

        } else {

            for ( final String folder : templateFolders ) {
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
                LogUtil.warn( processingEnv, type,
                        "Could not load a template from configured template folders '%s' using the path '%s'.",
                        Arrays.toString( templateFolders ), filePath );
            }

        }
        return resourceAsStream;
    }

    private String getTemplateFilePath( final TypeElement type ) {
        return type.getQualifiedName().toString().replace( '.', File.separatorChar ) + "." + _config.getTemplateExtension();
    }

    private void processTemplate( final TypeElement type, final InputStream templateAsStream ) {

        final Pattern idPattern =
                Pattern.compile( "\\p{javaWhitespace}wicket:id\\p{javaWhitespace}*=\\p{javaWhitespace}*\"([^\"]*)\"(\\p{javaWhitespace}|>|/>)" );
        BufferedReader reader;
        final Collection<String> ids = new HashSet<String>();

        try {

            reader = new BufferedReader( new InputStreamReader( templateAsStream, _config.getTemplateEncoding() ) );
            String currentLine;
            while ( ( currentLine = reader.readLine() ) != null ) {
                final Matcher matcher = idPattern.matcher( currentLine );
                while ( matcher.find() ) {
                    final String id = matcher.group( 1 );
                    ids.add( id );
                    if ( _config.getDebug() ) {
                        LogUtil.note( processingEnv, "    found wicket id '%s'.", id );
                    }
                }
            }

            final BindingGenerator bindingGenerator =
                    new BindingGenerator( type.getQualifiedName() + _config.getBindingSuffix(), ids );
            save( bindingGenerator, type );

        } catch ( final IOException e ) {
            LogUtil.error( processingEnv, type, e.getMessage() );
        }

    }

    private void save( final BindingGenerator generator, final Element element ) {

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
