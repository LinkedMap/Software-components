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
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import es.unizar.iaaa.lms.core.domain.ResourceClass;
import es.unizar.iaaa.lms.core.domain.ServerConfiguration;

public class ServerConfigurationReporter {

    private ServerConfiguration serverConfiguration;

    private List<Reporter> reporters = new ArrayList<Reporter>();

    private List<ReportEntry> reports = new ArrayList<ReportEntry>();

    public ServerConfigurationReporter(ServerConfiguration serverConfiguration) {
        this.serverConfiguration = serverConfiguration;
    }

    public void reporter(Reporter... reporter) {
        reporters.addAll(Arrays.asList(reporter));
    }

    public List<ResourceClass> getResources() {
        return serverConfiguration.getResources();
    }

    public List<ReportEntry> getReports() {
        return reports;
    }

    public void analyze() {
        reports.clear();
        for (Reporter reporter : reporters) {
            reports.addAll(reporter.analyze(serverConfiguration));
        }
    }

    public String getMaxLevel() {
        if (reports.isEmpty())
            return ReportEntry.Level.success.toString();
        return Collections.max(reports).getLevel();
    }

    public String getSummary() {
        if (reports.isEmpty())
            return "Ok";
        Map<String, Integer> m = new HashMap<String, Integer>();
        for (ReportEntry reporter : reports) {
            if (!m.containsKey(reporter.getLevel())) {
                m.put(reporter.getLevel(), 0);
            }
            m.put(reporter.getLevel(), m.get(reporter.getLevel()) + 1);
        }
        StringBuffer sb = new StringBuffer();
        for (ReportEntry.Level level : ReportEntry.Level.values()) {
            if (m.containsKey(level.toString())) {
                if (sb.length() > 0)
                    sb.append(", ");
                sb.append(String.format("%d %s", m.get(level.toString()),
                    level.toString()));
            }
        }
        return sb.toString();
    }
}
