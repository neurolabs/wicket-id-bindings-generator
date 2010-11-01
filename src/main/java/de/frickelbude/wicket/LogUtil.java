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

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.Element;
import javax.tools.Diagnostic.Kind;

/**
 * Helps logging to the messager in a unified style.
 * 
 * @author Ole Langbehn (ole.langbehn@googlemail.com) (initial creation)
 */
public class LogUtil {

    private static final String PREFIX = "wicket-id-binding-generator: ";

    /**
     * Hide Constructor.
     */
    private LogUtil() {
        // construction is superfluous
    }

    /**
     * Writes a message of type {@link Kind#NOTE} to the messager substituting
     * arguments in the message using {@link String#format(String, Object...)}.
     * 
     * @param message
     *            the message, containing placeholders for the arguments
     * @param args
     *            the arguments
     */
    public static void note( final ProcessingEnvironment processingEnv, final String message, final Object... args ) {
        processingEnv.getMessager().printMessage( Kind.NOTE, PREFIX + String.format( message, args ) );
    }

    /**
     * Writes a message of type {@link Kind#ERROR} to the messager for an
     * element substituting arguments in the message using
     * {@link String#format(String, Object...)}.
     * 
     * @param message
     *            the message, containing placeholders for the arguments
     * @param args
     *            the arguments
     */
    public static void error( final ProcessingEnvironment processingEnv, final Element element, final String message,
            final Object... args ) {
        processingEnv.getMessager().printMessage( Kind.ERROR, PREFIX + String.format( message, args ), element );
    }

    /**
     * Writes a message of type {@link Kind#ERROR} to the messager substituting
     * arguments in the message using {@link String#format(String, Object...)}.
     * 
     * @param message
     *            the message, containing placeholders for the arguments
     * @param args
     *            the arguments
     */
    public static void error( final ProcessingEnvironment processingEnv, final String message, final Object... args ) {
        processingEnv.getMessager().printMessage( Kind.ERROR, PREFIX + String.format( message, args ) );
    }

    /**
     * Writes a message of type {@link Kind#WARNING} to the messager for an
     * element substituting arguments in the message using
     * {@link String#format(String, Object...)}.
     * 
     * @param message
     *            the message, containing placeholders for the arguments
     * @param args
     *            the arguments
     */
    public static void warn( final ProcessingEnvironment processingEnv, final Element element, final String message,
            final Object... args ) {
        processingEnv.getMessager().printMessage( Kind.WARNING, PREFIX + String.format( message, args ), element );
    }

}
