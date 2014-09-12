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

public class ReportEntry implements Comparable<ReportEntry> {

    public enum Level {
        success, info, warning, danger
    }

    private final String message;

    private final Level level;

    public ReportEntry(String message) {
        this(message, Level.info);
    }

    protected ReportEntry(String message, Level level) {
        this.message = message;
        this.level = level;
    }

    public ReportEntry withWarning() {
        return new ReportEntry(message, Level.warning);
    }

    public ReportEntry withSuccess() {
        return new ReportEntry(message, Level.success);
    }

    public ReportEntry withDanger() {
        return new ReportEntry(message, Level.danger);
    }

    public String getMessage() {
        return message;
    }

    public String getLevel() {
        return level.toString();
    }

    @Override
    public int compareTo(ReportEntry entry) {
        return level.compareTo(entry.level);
    }

}
