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
import javax.tools.Diagnostic.Kind;
import javax.tools.JavaFileObject;

import joist.sourcegen.GClass;
import joist.sourcegen.GField;

/**
 * @author Ole Langbehn (ole.langbehn@googlemail.com) (initial creation)
 */
@SupportedAnnotationTypes( { "de.frickelbude.wicket.HasTemplate" } )
@SupportedSourceVersion( SourceVersion.RELEASE_6 )
public class IdBindingProcessor extends AbstractProcessor {

    private Config _config;

    /**
     * {@inheritDoc}
     */
    @Override
    public synchronized void init( final ProcessingEnvironment processingEnv ) {
        _config = new Config( processingEnv );
        super.init( processingEnv );
    }

    @Override
    public boolean process( final Set<? extends TypeElement> annotations, final RoundEnvironment roundEnv ) {
        final String[] templateFolders = _config.getTemplateFolders();
        for ( final Element element : roundEnv.getElementsAnnotatedWith( HasTemplate.class ) ) {
            if ( element.getKind() == ElementKind.CLASS ) {
                final TypeElement type = (TypeElement) element;
                processingEnv.getMessager().printMessage( Kind.WARNING, "bearbeite: " + type.getQualifiedName() );
                final String fileName =
                        type.getQualifiedName().toString().replace( '.', File.separatorChar ) + "." + _config.getTemplateSuffix();
                // TODO: Stream wieder schließen
                InputStream resourceAsStream = null;
                if ( templateFolders == null ) {
                    // from classpath as a last resort
                    resourceAsStream = type.getClass().getResourceAsStream( fileName );
                    // warnung ausgeben, dass nicht gefunden im classpath, auf config verweisen
                } else {
                    for ( final String folder : templateFolders ) {
                        final File file = new File( folder, fileName );
                        if ( file.exists() && file.isFile() && file.canRead() ) {
                            try {
                                resourceAsStream = new FileInputStream( file );
                            } catch ( final FileNotFoundException e ) {
                                throw new RuntimeException( e );
                            }
                            break;
                        }
                    }
                    // warnung ausgeben, dass nicht gefunden an stellen
                }

                System.out.println( "stream " + fileName );
                if ( resourceAsStream != null ) {
                    processingEnv.getMessager().printMessage( Kind.WARNING, "Template gefunden: " + type.getQualifiedName() );
                    final Pattern idPattern = Pattern.compile( "\\p{javaWhitespace}wicket:id\\p{javaWhitespace}*=\"([^\"]*)\"" );
                    BufferedReader reader;
                    final Collection<String> ids = new HashSet<String>();
                    try {
                        System.out.println( "die" );
                        reader = new BufferedReader( new InputStreamReader( resourceAsStream, _config.getTemplateEncoding() ) );
                        String currentLine;
                        while ( ( currentLine = reader.readLine() ) != null ) {
                            final Matcher matcher = idPattern.matcher( currentLine );
                            while ( matcher.find() ) {
                                final String id = matcher.group( 1 );
                                ids.add( id );
                                processingEnv.getMessager().printMessage( Kind.WARNING, "Member gefunden: " + id );
                                System.out.println( "waldfee" + id );
                            }
                        }
                        final GClass gClass = new GClass( type.getQualifiedName() + _config.getBindingSuffix() );
                        gClass.getConstructor().setPrivate().body.line( "// creation of instances is superfluous" );
                        gClass.addImports( Generated.class );
                        gClass.addAnnotation( "@Generated(value = \"" + IdBindingProcessor.class.getName() + "\", date = \""
                                + new SimpleDateFormat( "dd MMM yyyy hh:mm" ).format( new Date() ) + "\")" );
                        for ( final String string : ids ) {
                            final GField gField = gClass.getField( string );
                            gField.initialValue( "\"{}\"", string );
                            gField.setStatic().setFinal().setPublic();
                            gField.type( String.class );
                        }
                        saveCode( gClass, element );
                    } catch ( final IOException e ) {
                        processingEnv.getMessager().printMessage( Kind.ERROR, e.getMessage(), element );
                    }
                }
            }
        }
        return true;
    }

    private void saveCode( final GClass gc, final Element element ) {
        try {
            final JavaFileObject jfo = processingEnv.getFiler().createSourceFile( gc.getFullClassNameWithoutGeneric() );
            final Writer w = jfo.openWriter();
            System.out.println( "holla" + gc.toCode() );

            w.write( gc.toCode() );
            w.close();
        } catch ( final IOException io ) {
            processingEnv.getMessager().printMessage( Kind.ERROR, io.getMessage(), element );
        }
    }

}
