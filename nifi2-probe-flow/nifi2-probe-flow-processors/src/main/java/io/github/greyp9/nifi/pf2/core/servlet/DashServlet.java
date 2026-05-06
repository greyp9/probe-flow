package io.github.greyp9.nifi.pf2.core.servlet;

import io.github.greyp9.nifi.pf2.core.common.Attribute;
import io.github.greyp9.nifi.pf2.core.common.Probe;
import io.github.greyp9.nifi.pf2.core.http.HttpResponse;
import io.github.greyp9.nifi.pf2.core.state.ProbeServiceState;
import io.github.greyp9.nifi.pf2.core.xhtml.XhtmlUtils;
import io.github.greyp9.nifi.pf2.core.xml.XmlUtils;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.io.IOException;

/**
 * Provide dashboard of container state information.
 */
public final class DashServlet extends HttpServlet {
    private static final long serialVersionUID = -2964221207981800207L;

    private transient ProbeServiceState serviceState;

    @Override
    public void init() throws ServletException {
        super.init();
        serviceState = (ProbeServiceState) getServletContext().getAttribute(ProbeServiceState.class.getName());
    }

    @Override
    protected void doGet(final HttpServletRequest request, final HttpServletResponse response) throws IOException {
        // ref: io.github.greyp9.nifi.pf2.core.view.ServiceView
        final Document document = XhtmlUtils.initDocument();
        XhtmlUtils.addHead(document.getDocumentElement(), String.format("%s - NiFi", serviceState.getName()));
        final Element body = XmlUtils.addChild(document.getDocumentElement(), Probe.Html.BODY);
        XmlUtils.addChild(body, Probe.Html.DIV, "DashServlet", new Attribute(Probe.Html.CLASS, Probe.CSS.HEADER));
        addClassLoaderDivTo(body);
        XhtmlUtils.createFooter(body);
        final HttpResponse httpResponse = ServletUtils.toResponseOk(
                Probe.Mime.TEXT_HTML_UTF8, XmlUtils.toXhtml(document));
        ServletUtils.write(response, httpResponse);
    }

    private void addClassLoaderDivTo(final Element xhtml) {
        final Element divClassLoader = XmlUtils.addChild(xhtml, Probe.Html.DIV, "ClassLoader");
        ClassLoader classLoader = getClass().getClassLoader();
        while (classLoader != null) {
            XmlUtils.addChild(divClassLoader, Probe.Html.DIV, classLoader.toString());
            classLoader = classLoader.getParent();
        }
    }
}
