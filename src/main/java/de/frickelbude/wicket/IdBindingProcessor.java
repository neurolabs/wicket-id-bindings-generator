/*
 * $Id$
 * (c) Copyright 2010 atcard.de
 *
 * Created on 28.10.2010 by Ole Langbehn (ole.langbehn@googlemail.com)
 *
 * This file contains unpublished, proprietary trade secret information of
 * atcard.de. Use, transcription, duplication and
 * modification are strictly prohibited without prior written consent of
 * atcard.de.
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
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.annotation.Generated;
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
import javax.tools.Diagnostic.Kind;

import joist.sourcegen.GClass;
import joist.sourcegen.GField;

/**
 * @author Ole Langbehn (ole.langbehn@googlemail.com) (initial creation)
 */
@SupportedAnnotationTypes({ "de.frickelbude.wicket.HasTemplate" })
@SupportedSourceVersion(SourceVersion.RELEASE_6)
public class IdBindingProcessor extends AbstractProcessor {
    
    private Config _config;

    /**
     * {@inheritDoc}
     */
    @Override
    public synchronized void init( ProcessingEnvironment processingEnv ) {
        _config = new Config(processingEnv);
        super.init( processingEnv );
    }

    @Override
    public boolean process( Set<? extends TypeElement> annotations, RoundEnvironment roundEnv ) {
        String[] templateFolders = _config.getTemplateFolders();
        for(Element element : roundEnv.getElementsAnnotatedWith( HasTemplate.class )) {
            if (element.getKind() == ElementKind.CLASS) {
                TypeElement type = (TypeElement) element;
                processingEnv.getMessager().printMessage(Kind.WARNING, "bearbeite: "+type.getQualifiedName());
                String fileName = type.getQualifiedName().toString().replace( '.', File.separatorChar) + "." + _config.getTemplateSuffix();
                // TODO: Stream wieder schließen
                InputStream resourceAsStream = null;
                System.out.println(Arrays.toString( templateFolders ) + templateFolders);
                if (templateFolders == null) {
                    // from classpath as a last resort
                    System.out.println("loading template from classpath: " + fileName);
                    resourceAsStream = type.getClass().getResourceAsStream( fileName );
                    // warnung ausgeben, dass nicht gefunden im classpath, auf config verweisen
                } else {
                    for ( String folder : templateFolders ) {
                        File file = new File(folder, fileName);
                        System.out.println("testing for template: " + file.getAbsolutePath());
                        if (file.exists() && file.isFile() && file.canRead()) {
                            try {
                                System.out.println("loading template: " + file.getAbsolutePath());
                                resourceAsStream = new FileInputStream( file );
                            } catch ( FileNotFoundException e ) {
                                throw new RuntimeException( e );
                            }
                            break;
                        }
                    }
                    // warnung ausgeben, dass nicht gefunden an stellen
                }
               
                System.out.println("stream "+fileName);
                if (resourceAsStream != null) {
                    processingEnv.getMessager().printMessage(Kind.WARNING, "Template gefunden: "+type.getQualifiedName());
                    Pattern idPattern = Pattern.compile("\\p{javaWhitespace}wicket:id\\p{javaWhitespace}*=\"([^\"]*)\"");
                    BufferedReader reader;
                    final Collection<String> ids = new HashSet<String>(); 
                    try {
                        System.out.println("die");
                        reader = new BufferedReader(new InputStreamReader(resourceAsStream, _config.getTemplateEncoding()));
                        String currentLine;
                        while ((currentLine = reader.readLine()) != null) {
                            Matcher matcher = idPattern.matcher( currentLine );
                            while (matcher.find()) {
                                String id = matcher.group( 1 );
                                ids.add( id);
                                processingEnv.getMessager().printMessage(Kind.WARNING, "Member gefunden: "+id);
                                System.out.println("waldfee" + id);
                            }
                        }
                        GClass gClass = new GClass(type.getQualifiedName()+_config.getBindingSuffix());
                        gClass.getConstructor().setPrivate().body.line("// creation of instances is superfluous");
                        gClass.addImports(Generated.class);
                        gClass.addAnnotation( "@Generated(value = \"" + IdBindingProcessor.class.getName() + "\", date = \"" + new SimpleDateFormat("dd MMM yyyy hh:mm").format(new Date()) + "\")" );
                        for ( String string : ids ) {
                            GField gField = gClass.getField( string );
                            gField.initialValue( "\"{}\"", string );
                            gField.setStatic().setFinal().setPublic();
                            gField.type( String.class );
                        }
                        saveCode(gClass, element);
                    } catch ( IOException e ) {
                        processingEnv.getMessager().printMessage(Kind.ERROR, e.getMessage(), element);
                    }
                }
            }
        }
        return true;
    }

    private void saveCode(GClass gc, Element element) {
        try {
            JavaFileObject jfo = processingEnv.getFiler()
                .createSourceFile(gc.getFullClassNameWithoutGeneric());
            Writer w = jfo.openWriter();
            System.out.println("holla"+gc.toCode());

            w.write(gc.toCode());
            w.close();
        } catch (IOException io) {
            processingEnv.getMessager().printMessage(Kind.ERROR, io.getMessage(), element);
        }
    }

}
