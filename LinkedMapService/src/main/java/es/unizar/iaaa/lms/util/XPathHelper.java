/**
 * This file is part of Linked Map Service (LMS).
 *
 * Linked Map Service (LMS) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Linked Map Service (LMS) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Linked Map Service (LMS).  If not, see <http://www.gnu.org/licenses/>.
 */
package es.unizar.iaaa.lms.util;

import java.io.InputStream;
import java.io.StringReader;
import java.util.Collections;
import java.util.Map;

import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.springframework.util.CollectionUtils;
import org.springframework.util.xml.SimpleNamespaceContext;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.InputSource;

/**
 * Rossen Stoyanchev's XPath helper class without assertions.
 * 
 * @author Rossen Stoyanchev
 * @since 3.2
 */
public class XPathHelper {

    private final XPathExpression xpathExpression;

    private final boolean hasNamespaces;

    /**
     * 
     * @param expression
     * @param namespaces
     * @param args
     * @throws XPathExpressionException
     */
    public XPathHelper(final String expression,
            final Map<String, String> namespaces, final Object... args)
            throws XPathExpressionException {

        xpathExpression = compileXpathExpression(
            String.format(expression, args), namespaces);
        hasNamespaces = !CollectionUtils.isEmpty(namespaces);
    }

    /**
     * 
     * @param expression
     * @param namespaces
     * @return a compiled XPath exception
     * @throws XPathExpressionException
     */
    private XPathExpression compileXpathExpression(final String expression,
            final Map<String, String> namespaces)
            throws XPathExpressionException {

        SimpleNamespaceContext namespaceContext = new SimpleNamespaceContext();
        namespaceContext.setBindings((namespaces != null) ? namespaces
                : Collections.<String, String> emptyMap());
        XPath xpath = XPathFactory.newInstance().newXPath();
        xpath.setNamespaceContext(namespaceContext);
        return xpath.compile(expression);
    }

    /**
     * Evaluate the enclosed XPath expression against a document.
     * 
     * @param document
     *            the document to be evaluated
     * @param evaluationType
     *            the type of XPath evaluation
     * @param expectedClass
     *            the class expected in the result
     * @param <T>
     *            the class expected in the result
     * @return The Object that is the result of evaluating the expression and
     *         converting the result to expectedClass type.
     * @throws XPathExpressionException
     *             if XPath fails
     */
    @SuppressWarnings("unchecked")
    public final <T> T evaluateXpath(final Node document,
            final QName evaluationType, final Class<T> expectedClass)
            throws XPathExpressionException {

        return (T) xpathExpression.evaluate(document, evaluationType);
    }

    /**
     * 
     * @param xml
     * @return
     * @throws Exception
     */
    public final Document parseXmlString(final String xml) throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(this.hasNamespaces);
        DocumentBuilder documentBuilder = factory.newDocumentBuilder();
        InputSource inputSource = new InputSource(new StringReader(xml));
        Document document = documentBuilder.parse(inputSource);
        return document;
    }

    /**
     * 
     * @param xml
     * @return
     * @throws Exception
     */
    public final Document parseXmlInputStream(final InputStream is)
            throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(this.hasNamespaces);
        DocumentBuilder documentBuilder = factory.newDocumentBuilder();
        InputSource inputSource = new InputSource(is);
        Document document = documentBuilder.parse(inputSource);
        return document;
    }

}
