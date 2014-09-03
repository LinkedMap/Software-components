/**
 * Copyright (C) 2007 Richard Cyganiak (richard@cyganiak.de)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package es.unizar.iaaa.lms.pubby;

import java.util.Iterator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.ResourceFactory;
import com.hp.hpl.jena.shared.PrefixMapping;

/**
 * Helper class that splits URIs into prefix and local name according to a Jena
 * PrefixMapping.
 */
public class URIPrefixer {
    private final Resource resource;
    private final String prefix;
    private final String localName;

    public URIPrefixer(String uri, PrefixMapping prefixes) {
        this(ResourceFactory.createResource(uri), prefixes);
    }

    public URIPrefixer(Resource resource, PrefixMapping prefixes) {
        this.resource = resource;
        String uri = resource.getURI();
        Iterator<String> it = prefixes.getNsPrefixMap().keySet().iterator();
        while (it.hasNext() && uri != null) {
            String entryPrefix = it.next();
            String entryURI = prefixes.getNsPrefixURI(entryPrefix);
            if (uri.startsWith(entryURI)) {
                prefix = entryPrefix;
                localName = uri.substring(entryURI.length());
                return;
            }
        }
        prefix = null;
        localName = null;
    }

    public boolean hasPrefix() {
        return prefix != null;
    }

    public String getPrefix() {
        return prefix;
    }

    public String getLocalName() {
        if (resource.isAnon())
            return null;
        if (localName == null) {
            Matcher matcher = Pattern.compile("([^#/:?]+)[#/:?]*$").matcher(
                resource.getURI());
            if (matcher.find()) {
                return matcher.group(1);
            }
            return ""; // Only happens if the URI contains only excluded chars
        }
        return localName;
    }

    public String toTurtle() {
        if (resource.isAnon())
            return "[]";
        if (hasPrefix()) {
            return getPrefix() + ":" + getLocalName();
        }
        return "<" + resource.getURI() + ">";
    }
}
