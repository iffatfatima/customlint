package com.greense.detector.mylibrary;


import com.android.tools.lint.detector.api.ResourceXmlDetector;
import com.android.tools.lint.detector.api.XmlContext;

import org.w3c.dom.Element;

import java.util.Collection;
import java.util.Collections;

/**
 * Created by Iffat on 12/2/2019.
 */
public class TextViewStyleDetector extends ResourceXmlDetector {

    private static final String SCHEMA = "http://schemas.android.com/apk/res/android";
    private static final String TEXT_APPEARANCE = "textAppearance";
    private static final String TEXTVIEW = "TextView";

    @Override
    public Collection<String> getApplicableElements() {
        return Collections.singletonList(TEXTVIEW);
    }

    @Override
    public void visitElement(XmlContext context, Element element) {
        if (!element.hasAttributeNS(SCHEMA, TEXT_APPEARANCE)) {
            context.report(
                    TextAppearanceIssue.ISSUE,
                    context.getLocation(element),
                    TextAppearanceIssue.EXPLANATION);
        }
    }
}