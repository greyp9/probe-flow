/**
 * Licensed to the Apache Software Foundation (ASF) under one or more contributor license agreements.  See the NOTICE
 * file distributed with this work for additional information regarding copyright ownership.  The ASF licenses this
 * file to you under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations under the License.
 */
package io.github.greyp9.nifi.pf.core.xhtml;

import io.github.greyp9.nifi.pf.core.ProbeUtils;
import io.github.greyp9.nifi.pf.core.alert.Alert;
import io.github.greyp9.nifi.pf.core.alert.Alerts;
import io.github.greyp9.nifi.pf.core.common.Attribute;
import io.github.greyp9.nifi.pf.core.common.Probe;
import io.github.greyp9.nifi.pf.core.xml.XmlUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public final class XhtmlUtils {

    /**
     * <a href="https://checkstyle.sourceforge.io/config_design.html#FinalClass">Constructor</a>
     */
    private XhtmlUtils() {
    }

    public static Document initDocument() {
        return XmlUtils.create(Probe.Html.HTML, new Attribute(Probe.Html.LANG, Probe.Html.EN));
    }

    public static Element addHead(final Element parent, final String title) {
        final Element head = XmlUtils.addChild(parent, Probe.Html.HEAD);
        XmlUtils.addChild(head, Probe.Html.TITLE, title);
        XmlUtils.addChild(head, Probe.Html.LINK,
                new Attribute(Probe.Html.HREF, Probe.Resource.CSS),
                new Attribute(Probe.Html.REL, Probe.Html.STYLESHEET),
                new Attribute(Probe.Html.TYPE, Probe.Mime.TEXT_CSS));
        return head;
    }

    public static void addAlerts(final Element parent, final Alerts alerts) {
        final Collection<Alert> alertCollection = alerts.removeAll();
        if (!alertCollection.isEmpty()) {
            final Element divAlerts = XmlUtils.addChild(parent, Probe.Html.DIV,
                    new Attribute(Probe.Html.CLASS, Probe.CSS.ALERTS));
            alertCollection.forEach(a -> addAlert(divAlerts, a));
        }
    }

    private static void addAlert(final Element parent, final Alert alert) {
        final Element divAlert = XmlUtils.addChild(parent, Probe.Html.DIV);
        XmlUtils.addChild(divAlert, Probe.Html.SPAN, alert.getIcon(),
                new Attribute(Probe.Html.CLASS, String.join(" ", Probe.CSS.LEVEL, alert.getSeverity().toString())));
        XmlUtils.addChild(divAlert, Probe.Html.SPAN, ProbeUtils.toStringZ(alert.getDate()),
                new Attribute(Probe.Html.CLASS, Probe.CSS.TIMESTAMP));
        XmlUtils.addChild(divAlert, Probe.Html.SPAN, alert.getMessage(),
                new Attribute(Probe.Html.CLASS, Probe.CSS.MESSAGE));
    }

    public static void addNavBar(final Element parent, final String... links) {
        final Element div = XmlUtils.addChild(parent, Probe.Html.DIV,
                new Attribute(Probe.Html.CLASS, Probe.CSS.NAVBAR));
        final Element divRight = XmlUtils.addChild(div, Probe.Html.DIV,
                new Attribute(Probe.Html.CLASS, Probe.CSS.RIGHT));
        for (String link : links) {
            XmlUtils.addChild(divRight, Probe.Html.A, String.format("[%s]", Probe.Icon.HOME),
                    new Attribute(Probe.Html.HREF, link));
        }
    }

    public static Element createTable(final Element parent, final String[] columns) {
        final Element table = XmlUtils.addChild(parent, Probe.Html.TABLE,
                new Attribute(Probe.Html.CLASS, Probe.Html.TABLE));
        final Element thead = XmlUtils.addChild(table, Probe.Html.THEAD,
                new Attribute(Probe.Html.CLASS, Probe.Html.TABLE));
        final Element trHead = XmlUtils.addChild(thead, Probe.Html.TR);
        for (final String column : columns) {
            XmlUtils.addChild(trHead, Probe.Html.TH, column);
        }
        return table;
    }

    public static void addRow(final Element tbody, final Object... columns) {
        final Element tr = XmlUtils.addChild(tbody, Probe.Html.TR);
        for (final Object column : columns) {
            XmlUtils.addChild(tr, Probe.Html.TD, column.toString());
        }
    }

    public static int addTableHead(final Element table, final String... columns) {
        final Element thead = XmlUtils.addChild(table, Probe.Html.THEAD,
                new Attribute(Probe.Html.CLASS, Probe.Html.TABLE));
        final Element trHead = XmlUtils.addChild(thead, Probe.Html.TR);
        for (final String column : columns) {
            XmlUtils.addChild(trHead, Probe.Html.TH, column);
        }
        return columns.length;
    }

    public static Element createFooter(final Element parent) {
        final Element divFooter = XmlUtils.addChild(parent, Probe.Html.DIV,
                new Attribute(Probe.Html.CLASS, Probe.CSS.FOOTER));
        final String textLeft = String.format("[%s]", new Date().toInstant().toString());
        XmlUtils.addChild(divFooter, Probe.Html.SPAN, textLeft,
                new Attribute(Probe.Html.CLASS, Probe.CSS.LEFT));
        final Package p = XhtmlUtils.class.getPackage();
        final String textRight = String.format("[%s]", p.getSpecificationVersion());
        final String tooltipRight = String.format("[%s]", p.getImplementationVersion());
        XmlUtils.addChild(divFooter, Probe.Html.SPAN, textRight,
                new Attribute(Probe.Html.CLASS, Probe.CSS.RIGHT),
                new Attribute(Probe.Html.TITLE, tooltipRight));
        return divFooter;
    }

    public static void addTableAttributes(final Element div, final Map<String, String> attributes) {
        final Element table = XmlUtils.addChild(div, Probe.Html.TABLE,
                new Attribute(Probe.Html.CLASS, Probe.Html.TABLE));
        final int columns = XhtmlUtils.addTableHead(table, Probe.App.COLUMN_NAME, Probe.App.COLUMN_VALUE);

        final Element tbody = XmlUtils.addChild(table, Probe.Html.TBODY,
                new Attribute(Probe.Html.CLASS, Probe.Html.TABLE));
        final List<Map.Entry<String, String>> entries = attributes.entrySet().stream()
                .sorted(Map.Entry.comparingByKey()).collect(Collectors.toList());
        for (Map.Entry<String, String> entry : entries) {
            XhtmlUtils.addRow(tbody, entry.getKey(), entry.getValue());
        }

        if (entries.isEmpty()) {
            final Element tr = XmlUtils.addChild(tbody, Probe.Html.TR);
            XmlUtils.addChild(tr, Probe.Html.TD,
                    new Attribute(Probe.Html.COLSPAN, Integer.toString(columns)));
        }

        final Element tfoot = XmlUtils.addChild(table, Probe.Html.TFOOT,
                new Attribute(Probe.Html.CLASS, Probe.Html.TABLE));
        final Element trFoot = XmlUtils.addChild(tfoot, Probe.Html.TR);
        final String footer = String.format("%d attributes(s)", attributes.size());
        XmlUtils.addChild(trFoot, Probe.Html.TH, footer,
                new Attribute(Probe.Html.COLSPAN, Integer.toString(columns)));
    }
}
