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

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.annotation.processing.ProcessingEnvironment;
import javax.tools.FileObject;
import javax.tools.JavaFileManager.Location;
import javax.tools.StandardLocation;

/**
 * @author Ole Langbehn (ole.langbehn@googlemail.com) (initial creation)
 */
public class Config {

    private static final String OPTION_TEMPLATE_FOLDERS = "template.folders";
    private static final String OPTION_TEMPLATE_EXTENSION = "template.extension";
    private static final String OPTION_TEMPLATE_ENCODING = "template.encoding";
    private static final String OPTION_BINDINGS_SUFFIX = "bindings.suffix";
    private final Map<String, String> _options = new HashMap<String, String>();

    public Config( final ProcessingEnvironment env ) {
        this.loadDefaultOptions();
        this.loadAptKeyValueOptions( env );
        this.loadBindgenDotProperties( env );
    }

    private void loadDefaultOptions() {
        _options.put( OPTION_TEMPLATE_FOLDERS, "" );
        _options.put( OPTION_TEMPLATE_EXTENSION, "html" );
        _options.put( OPTION_TEMPLATE_ENCODING, "UTF-8" );
        _options.put( OPTION_BINDINGS_SUFFIX, "WID" );
    }

    private void loadBindgenDotProperties( final ProcessingEnvironment env ) {
        final File propertiesFile = resolvePropertiesIfExists( env, "wicket-id-bindings-generator.properties" );
        if ( propertiesFile != null ) {
            final Map<String, String> properties = loadProperties( env, propertiesFile );
            _options.putAll( filterPaths( properties, propertiesFile.getParentFile() ) );
        }
    }

    private Map<String, String> filterPaths( final Map<String, String> properties, final File baseDir ) {
        final String templateFolders = properties.get( OPTION_TEMPLATE_FOLDERS );
        final Map<String, String> result = new HashMap<String, String>( properties );
        final List<String> resolvedFolders = new LinkedList<String>();
        if ( templateFolders != null ) {
            for ( final String templateFolder : templateFolders.split( "," ) ) {
                final File file = new File( baseDir, templateFolder );
                if ( file.exists() && file.isDirectory() ) {
                    resolvedFolders.add( file.getAbsolutePath() );
                }
            }
            if ( resolvedFolders.size() > 0 ) {
                final String joined = Strings.join( resolvedFolders.toArray(), ',' );
                result.put( OPTION_TEMPLATE_FOLDERS, joined );
            }
        }
        return result;
    }

    private void loadAptKeyValueOptions( final ProcessingEnvironment env ) {
        for ( final Map.Entry<String, String> entry : env.getOptions().entrySet() ) {
            _options.put( entry.getKey(), entry.getValue() );
        }
    }

    /*
     * from here the rest of the class is humbly taken from bindgens ConfUtil.
     */
    /** Attempts to load {@code fileName} and return its properties. */
    public static Map<String, String> loadProperties( final ProcessingEnvironment env, final File propertiesFile ) {
        final Map<String, String> properties = new LinkedHashMap<String, String>();

        final Properties p = new Properties();
        InputStream inputStream = null;
        try {
            inputStream = new FileInputStream( propertiesFile );
            p.load( inputStream );
        } catch ( final Exception e ) {
            e.printStackTrace();
        } finally {
            if ( inputStream != null ) {
                try {
                    inputStream.close();
                } catch ( IOException e ) {
                    e.printStackTrace();
                }
            }
        }
        for ( final Map.Entry<Object, Object> entry : p.entrySet() ) {
            properties.put( (String) entry.getKey(), (String) entry.getValue() );
        }

        return properties;
    }

    /**
     * Finds a file by starting by <code>location</code> and walkig up.
     * 
     * This uses a heuristic because in Eclipse we will not know what our
     * working directory is (it is wherever Eclipse was started from), so
     * project/workspace-relative paths will not work.
     * 
     * As far as passing in a the properties location as a {@code -Afile=path}
     * setting, Eclipse also lacks any {@code $ basepath} -type interpolation in
     * its APT key/value pairs (like Ant would be able to do). So only fixed
     * values are accepted, meaning an absolute path, which would be too tied to
     * any one developer's particular machine.
     * 
     * The one thing the APT API gives us is the CLASS_OUTPUT (e.g. bin/apt). So
     * we start there and walk up parent directories looking for
     * {@code wicket-id-bindings-generator.properties} files.
     */
    private static File resolvePropertiesIfExists( final ProcessingEnvironment env, final String fileName ) {
        // Eclipse, ant, and maven all act a little differently here, so try both source and class output
        for ( final Location location : new Location[] { StandardLocation.SOURCE_OUTPUT, StandardLocation.CLASS_OUTPUT } ) {
            // Find a dummy /bin/apt/dummy.txt path to start at
            final String dummyPath;
            try {
                // We don't actually create this, we just want its URI
                final FileObject dummyFileObject = env.getFiler().getResource( location, "", "dummy.txt" );
                dummyPath = dummyFileObject.toUri().toString().replaceAll( "file:", "" );
            } catch ( final IOException e1 ) {
                return null;
            }

            // Walk up looking for a wicket-id-bindings-generator.properties
            File current = new File( dummyPath ).getParentFile();
            while ( current != null ) {
                final File possible = new File( current, fileName );
                if ( possible.exists() ) {
                    return possible;
                }
                current = current.getParentFile();
            }

            // Before giving up, try just grabbing it from the current directory
            final File possible = new File( fileName );
            if ( possible.exists() ) {
                return possible;
            }

        }
        // No file found
        return null;
    }

    public String[] getTemplateFolders() {
        final String templateFolders = _options.get( OPTION_TEMPLATE_FOLDERS );
        if ( templateFolders != null && templateFolders.length() > 0 ) {
            return templateFolders.split( "," );
        }
        return null;
    }

    public String getTemplateSuffix() {
        return _options.get( OPTION_TEMPLATE_EXTENSION );
    }

    public String getTemplateEncoding() {
        return _options.get( OPTION_TEMPLATE_ENCODING );
    }

    public String getBindingSuffix() {
        return _options.get( OPTION_BINDINGS_SUFFIX );
    }

}
