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

import java.io.IOException;
import java.io.InputStream;
import java.util.Set;

import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.TypeElement;

import de.frickelbude.wicket.AbstractBindingProcessor.ClassHandler;

/**
 * An annotation processor that generates bindings for wicket templates.
 * 
 * @author Ole Langbehn (ole.langbehn@googlemail.com) (initial creation)
 */
@SupportedAnnotationTypes( { "de.frickelbude.wicket.HasTemplate" } )
@SupportedSourceVersion( SourceVersion.RELEASE_6 )
public class IdBindingProcessor extends AbstractBindingProcessor implements ClassHandler {

    @Override
    public boolean process( final Set<? extends TypeElement> annotations, final RoundEnvironment roundEnv ) {
        if ( config.getTemplateEnabled() ) {
            handleAnnotatedClasses( HasTemplate.class, roundEnv, this );
        }
        return true;
    }

    @Override
    public void handle( final TypeElement type ) {
        final String filePath = getPath( type, config.getTemplateExtension() );
        final InputStream resourceAsStream = getStream( filePath, type );

        if ( resourceAsStream != null ) {
            if ( config.getDebug() ) {
                LogUtil.note( processingEnv, "processing template with path '%s'.", filePath );
            }
            try {
                processFile( type, new XPathExtractor( resourceAsStream, "//*/@wicket:id" ), config.getTemplateBindingSuffix() );
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
