package com.dreamcloud.esa_wiki.annoatation.linkParser;

import java.io.IOException;
import java.io.PushbackReader;
import java.util.ArrayList;

public class LinkParser {
    private final PushbackReader reader;

    public LinkParser(PushbackReader reader) {
        this.reader = reader;
    }

    public boolean isLinkStartValid(int c) {
        return c != '{'  && c != '<' && c != '>' && c != '[' && c !=']' && c != '}'  && c != '|';
    }

    public boolean isLinkNameValid(String name) {
        if (name == null) {
            return false;
        }

        if (name.length() == 0) {
            return false;
        }

        return isLinkStartValid(name.charAt(0));
    }

    public Link parse() throws IOException {
        int bracketsSeen = 0;
        int c;
        while ((c = reader.read()) != -1) {
            if (c == '[') {
                if (++bracketsSeen == 2) {
                    int peek = reader.read();
                    reader.unread(peek);
                    if (isLinkStartValid(peek)) {
                        Link link = parseLink();
                        if (link != null) {
                            return link;
                        }
                    }
                    bracketsSeen = 0;
                }
            } else {
                bracketsSeen = 0;
            }
        }
        return null;
    }

    private Link parseLink() throws IOException {
        int depth = 2;
        StringBuilder linkText = new StringBuilder("[[");
        StringBuilder content = new StringBuilder();

        ArrayList<LinkParameter> parameters = new ArrayList<>();

        boolean inParameter = false;
        String parameterName = null;

        String linkTarget = null;
        String linkAnchor = null;

        while (depth > 0) {
            int c = reader.read();
            if (c == -1) {
                return null;
            }
            linkText.append((char) c);

            switch (c) {
                case '[':
                    depth++;
                    content.append((char) c);
                    break;
                case ']':
                    if (depth-- > 2) {
                        content.append((char) c);
                    }
                    break;
                    //missing break intentional
                default:
                    if (depth == 2) {
                        switch (c) {
                            case '|':
                                if (inParameter) {
                                    parameters.add(new LinkParameter(parameterName, content.toString()));
                                    parameterName = null;
                                } else {
                                    inParameter = true;
                                    linkTarget = content.toString();
                                }
                                content = new StringBuilder();
                                break;
                            case '=':
                                if (inParameter) {
                                    parameterName = content.toString();
                                    content = new StringBuilder();
                                }
                                break;
                            default:
                                content.append((char) c);
                        }
                    } else {
                        content.append((char) c);
                    }
            }
        }

        if (content.length() > 0) {
            if (inParameter) {
                if (parameterName == null) {
                    //This is the last param and has no name, so it is the anchor
                    linkAnchor = content.toString();
                }
            } else {
                linkTarget = content.toString();
            }
        }

        if (linkTarget != null) {
            linkTarget = linkTarget.trim();
        }
        if (linkAnchor != null) {
            linkAnchor = linkAnchor.trim();
        }

        if (!isLinkNameValid(linkTarget)) {
            return null;
        }

        Link link = new Link(linkTarget, linkAnchor, parameters);
        link.setText(linkText.toString());;
        return link;
    }
}
