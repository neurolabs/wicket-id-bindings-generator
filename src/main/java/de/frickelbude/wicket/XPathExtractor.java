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

import java.io.InputStream;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;

import javax.xml.XMLConstants;
import javax.xml.namespace.NamespaceContext;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

import de.frickelbude.wicket.AbstractBindingProcessor.MemberExtractor;

/**
 * An Extractor that extracts members out of xml files from values of nodes
 * found with an xpath expression.
 * 
 * @author Ole Langbehn (ole.langbehn@googlemail.com) (initial creation)
 */
public class XPathExtractor implements MemberExtractor {

    /**
     * Provides wicket namespace information.
     * 
     * @author Ole Langbehn (ole.langbehn@googlemail.com) (initial creation)
     */
    protected static class WicketNamespaceContext implements NamespaceContext {
        @Override
        public Iterator<?> getPrefixes( final String namespaceURI ) {
            return null;
        }

        @Override
        public String getPrefix( final String namespaceURI ) {
            if ( namespaceURI.equals( "http://wicket.apache.org/dtds.data/wicket-xhtml1.4-strict.dtd" ) ) {
                return "wicket";
            } else {
                return null;
            }
        }

        @Override
        public String getNamespaceURI( final String prefix ) {
            if ( prefix.equals( "wicket" ) ) {
                return "http://wicket.apache.org/dtds.data/wicket-xhtml1.4-strict.dtd";
            } else {
                return XMLConstants.NULL_NS_URI;
            }
        }
    }

    private final InputStream _stream;
    private final String _xpathExpression;

    /**
     * Constructor.
     * 
     * @param resourceAsStream
     *            the xml as stream
     * @param xpathExpression
     *            the xpath expression to select from the xml file.
     */
    public XPathExtractor( final InputStream resourceAsStream, final String xpathExpression ) {
        _stream = resourceAsStream;
        _xpathExpression = xpathExpression;
    }

    @Override
    public Collection<String> extract() throws Exception {

        // keep the order intact, therefore linkedlist
        final Collection<String> result = new LinkedList<String>();

        // factory that does not resolve dtds (offline support) and is namespace aware (for wicket extensions)
        final DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setAttribute( "http://apache.org/xml/features/nonvalidating/load-external-dtd", false );
        factory.setNamespaceAware( true );

        final DocumentBuilder builder = factory.newDocumentBuilder();
        final Document doc = builder.parse( _stream );

        // the xpath object needs a namespace context for resolving the wicket namespace
        final XPathFactory xFactory = XPathFactory.newInstance();
        final XPath xpath = xFactory.newXPath();
        xpath.setNamespaceContext( new WicketNamespaceContext() );

        // evaluate the xpath expression and construct the result
        final NodeList expr = (NodeList) xpath.evaluate( _xpathExpression, doc, XPathConstants.NODESET );
        for ( int i = 0; i < expr.getLength(); i++ ) {
            result.add( expr.item( i ).getNodeValue() );
        }

        return result;

    }

}