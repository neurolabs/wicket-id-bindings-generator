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

import java.lang.annotation.ElementType;
import java.lang.annotation.Target;

/**
 * Marks Classes as wicket components that have a corresponding translation
 * file, so that a translation binding can be generated.
 * 
 * @author Ole Langbehn (ole.langbehn@googlemail.com) (initial creation)
 */
@Target( value = { ElementType.TYPE } )
public @interface HasTranslation {

}
