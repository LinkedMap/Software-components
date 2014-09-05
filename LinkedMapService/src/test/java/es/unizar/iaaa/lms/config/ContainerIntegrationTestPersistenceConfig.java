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
package es.unizar.iaaa.lms.config;

import java.io.IOException;

import javax.servlet.ServletContext;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;

import es.unizar.iaaa.lms.pubby.PubbyRepository;

@Configuration
public class ContainerIntegrationTestPersistenceConfig {

    @Autowired
    private ServletContext servletContext;

    @Autowired
    private ApplicationContext ctx;

    @Bean
    public PubbyRepository pubbyRepository() throws IOException {
        Resource configFile = ctx
            .getResource("classpath:ContainerIntegrationTestConfig.ttl");
        Model m = ModelFactory.createDefaultModel();
        m.read(configFile.getInputStream(), configFile.getURL().toString(),
            "TURTLE");
        return PubbyRepository.create(m, ctx, servletContext);
    }

}
