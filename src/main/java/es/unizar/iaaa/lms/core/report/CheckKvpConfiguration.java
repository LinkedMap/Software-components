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
package es.unizar.iaaa.lms.core.report;

import java.util.ArrayList;
import java.util.List;

import es.unizar.iaaa.lms.core.domain.ResourceClass;
import es.unizar.iaaa.lms.core.domain.ServerConfiguration;
import es.unizar.iaaa.lms.core.endpoint.Endpoint;
import es.unizar.iaaa.lms.core.endpoint.KvpEndpoint;

public class CheckKvpConfiguration implements Reporter {

    @Override
    public List<ReportEntry> analyze(ServerConfiguration serverConfiguration) {
        List<ReportEntry> report = new ArrayList<ReportEntry>();
        for (ResourceClass resource : serverConfiguration.getResources()) {
            for (Endpoint endpoint : resource.getEndpoints()) {
                if (endpoint instanceof KvpEndpoint) {
                    KvpEndpoint kvp = (KvpEndpoint) endpoint;
                    if (kvp.getMapStore() == null) {
                        report.add(new ReportEntry(String.format(
                            "Missing map store for %s endpoint %s %s",
                            resource.getName(), endpoint.getMethod(),
                            endpoint.getPath())).withDanger());
                    }
                    if (kvp.getMapCollection() == null) {
                        report.add(new ReportEntry(String.format(
                            "Missing map collection for %s endpoint %s %s",
                            resource.getName(), endpoint.getMethod(),
                            endpoint.getPath())).withDanger());
                    }
                    if (kvp.getInfoStore() == null) {
                        report.add(new ReportEntry(String.format(
                            "Missing info store for %s endpoint %s %s",
                            resource.getName(), endpoint.getMethod(),
                            endpoint.getPath())).withDanger());
                    }
                    if (kvp.getInfoCollection() == null) {
                        report.add(new ReportEntry(String.format(
                            "Missing info collection for %s endpoint %s %s",
                            resource.getName(), endpoint.getMethod(),
                            endpoint.getPath())).withDanger());
                    }
                }
            }
        }
        return report;
    }

}
