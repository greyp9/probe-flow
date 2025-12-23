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
package io.github.greyp9.nifi.pf.core.common;

/**
 * Application constants.
 */
public class Probe {

    public static class Action {
        public static final String CLONE = "CLONE";
        public static final String DROP = "DROP";
        public static final String EDIT = "EDIT";
    }

    public static class App {
        public static final String ACCEPT = "accept";
        public static final String ADD_ATTRIBUTE = "addAttribute";
        public static final String CREATE = "create";
        public static final String DELETE_ATTRIBUTE = "deleteAttribute";
        public static final String FLOWFILE = "flowfile";
        public static final String FLOWFILE_ID_IN = "flowfileIdIn";
        public static final String ID_ATTRIBUTE = "attribute";
        public static final String ID_ATTRIBUTES = "attributes";
        public static final String ID_METADATA = "metadata";
        public static final String RESET = "reset";
        public static final String ROUTE = "route";
        public static final String UPDATE_TEXT = "updateText";
        public static final String UPDATE_TEXT_BASE64 = "updateTextBase64";
        public static final String UPLOAD_FILE = "uploadFile";

        public static final String COLUMN_NAME = "Name";
        public static final String COLUMN_VALUE = "Value";
    }

    public static class CSS {
        public static final String ALERTS = "alerts";
        public static final String CONTENT = "content";
        public static final String FOOTER = "footer";
        public static final String HEADER = "header";
        public static final String LEFT = "left";
        public static final String LEVEL = "level";
        public static final String MESSAGE = "message";
        public static final String NAVBAR = "navbar";
        public static final String RIGHT = "right";
        public static final String TIMESTAMP = "timestamp";
    }

    public static class Html {
        public static final String A = "a";
        public static final String ACCESS_KEY = "accesskey";
        public static final String ACTION = "action";
        public static final String BODY = "body";
        public static final String BUTTON = "button";
        public static final String CLASS = "class";
        public static final String COLSPAN = "colspan";
        public static final String DIV = "div";
        public static final String EN = "en";
        public static final String FORM = "form";
        public static final String H1 = "h1";
        public static final String H2 = "h2";
        public static final String HEAD = "head";
        public static final String HREF = "href";
        public static final String HTML = "html";
        public static final String ID = "id";
        public static final String INPUT = "input";
        public static final String LANG = "lang";
        public static final String LI = "li";
        public static final String LINK = "link";
        public static final String METHOD = "method";
        public static final String NAME = "name";
        public static final String P = "p";
        public static final String POST = "post";
        public static final String REL = "rel";
        public static final String SPAN = "span";
        public static final String STYLESHEET = "stylesheet";
        public static final String TABLE = "table";
        public static final String TBODY = "tbody";
        public static final String TD = "td";
        public static final String TFOOT = "tfoot";
        public static final String TH = "th";
        public static final String THEAD = "thead";
        public static final String TITLE = "title";
        public static final String TR = "tr";
        public static final String TYPE = "type";
        public static final String UL = "ul";
        public static final String VALUE = "value";
    }

    // https://en.wikipedia.org/wiki/List_of_Unicode_characters
    // http://www.unicode.org/reports/tr44/#General_Category_Values
    @SuppressWarnings("UnnecessaryUnicodeEscape")
    public static class Icon {
        public static final String CONTENT = "\u25c9";
        public static final String DOWNLOAD = "\u25bc";
        public static final String EDITOR = "\u270e";
        public static final String HOME = "\u23cf";
        public static final String HREF = "\u21d7";
        public static final String METADATA = "\u24d8";
        public static final String UPLOAD = "\u25b2";
    }

    public static class Http {
        public static final String CONTENT_LENGTH = "Content-Length";
        public static final String CONTENT_TYPE = "Content-Type";
        public static final String ENCTYPE = "enctype";
        public static final String FORM_MULTIPART = "multipart/form-data";
        public static final String FORM_URL_ENCODED = "application/x-www-form-urlencoded";
        public static final String LOCATION = "Location";
    }

    public static class Form {
        public static final String COLS = "cols";
        public static final String COUNT = "count";
        public static final String FILE = "file";
        public static final String NAME = "name";
        public static final String PLACEHOLDER = "placeholder";
        public static final String ROWS = "rows";
        public static final String SUBMIT = "submit";
        public static final String TEXT = "text";
        public static final String TEXTAREA = "textarea";
        public static final String VALUE = "value";
    }

    public static class Mime {
        public static final String IMAGE_ICON = "image/x-icon";
        public static final String TEXT_CSS = "text/css";
        public static final String TEXT_HTML_UTF8 = "text/html; charset='UTF-8'";
        public static final String TEXT_PLAIN = "text/plain";
        public static final String TEXT_XML_UTF8 = "text/xml; charset='UTF-8'";
    }

    public static class Resource {
        public static final String CSS = "/probe.css";
        public static final String FAVICON = "/favicon.ico";
        public static final String ROOT = "/";
    }

    public static class State {
        public static final String ATTRIBUTE = "attribute";
        public static final String CONTENT = "content";
        public static final String FLOWFILE = "flowfile";
        public static final String NAME = "name";
        public static final String STATE = "state";
    }

    public static class NiFi {
        public static final String ATTR_MIME_TYPE = "mime.type";
    }

    public static class Xml {
        public static final String PREFIX_STATE = "st";
        public static final String URI_STATE = "urn:probe:state";
    }
}
