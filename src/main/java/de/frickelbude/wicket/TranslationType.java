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


/**
 * Defines the possible types of wicket translation files.
 * 
 * @author Ole Langbehn (ole.langbehn@googlemail.com) (initial creation)
 * @author Sebastian Gaul (sgaul@milabent.de)
 */
public enum TranslationType {
        XML("properties.xml"),
        PROPERTIES("properties");
    
    private final String _extension;
        
    private TranslationType(String extension) {
		_extension = extension;
	}

    public String getExtension() {
        return _extension;
    }
}