/*
 * $Id$
 * (c) Copyright 2010 atcard.de
 *
 * Created on 29.10.2010 by Ole Langbehn (ole.langbehn@googlemail.com)
 *
 * This file contains unpublished, proprietary trade secret information of
 * atcard.de. Use, transcription, duplication and
 * modification are strictly prohibited without prior written consent of
 * atcard.de.
 */
package de.frickelbude.wicket;

/**
 * @author Ole Langbehn (ole.langbehn@googlemail.com) (initial creation)
 */
public class Strings {
    
    public Strings() {
        // construction is superfluous
    }
    
    public static String join(Object[] array, char separator) {
        if (array == null || array.length <= 0) {
            return "";
        }

        final StringBuilder sb = new StringBuilder(128);

        boolean appendSeparator = false;
        for ( Object object : array ) {
            if (appendSeparator) {
                sb.append(separator);
            } else {
                appendSeparator = true;
            }
            sb.append( object );
            
        }
        
        return sb.toString();
    }
    

}
