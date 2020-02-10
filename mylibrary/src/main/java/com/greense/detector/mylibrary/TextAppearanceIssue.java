package com.greense.detector.mylibrary;

import com.android.tools.lint.detector.api.Category;
import com.android.tools.lint.detector.api.Implementation;
import com.android.tools.lint.detector.api.Issue;
import com.android.tools.lint.detector.api.Scope;
import com.android.tools.lint.detector.api.Severity;

/**
 * Created by Iffat on 12/2/2019.
 */
public class TextAppearanceIssue {
    private static final String ID = "MissingTextAppearance";
    private static final String DESCRIPTION = "textAppearance attribute is missing";
    static final String EXPLANATION = "We should use textAppearance to style a TextView in order to provide consistent design";
    private static final Category CATEGORY = Category.TYPOGRAPHY;
    private static final int PRIORITY = 4;
    private static final Severity SEVERITY = Severity.WARNING;

    public static final Issue ISSUE = Issue.create(
            ID,
            DESCRIPTION,
            EXPLANATION,
            CATEGORY,
            PRIORITY,
            SEVERITY,
            new Implementation(
                    TextViewStyleDetector.class,
                    Scope.RESOURCE_FILE_SCOPE)
    );
}
