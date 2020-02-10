package com.greense.detector.mylibrary;

import com.android.tools.lint.detector.api.Category;
import com.android.tools.lint.detector.api.Implementation;
import com.android.tools.lint.detector.api.Issue;
import com.android.tools.lint.detector.api.Scope;
import com.android.tools.lint.detector.api.Severity;

public class LeakingListenerIssue {
    private static final String ID = "LeakingListenerIssue";
    private static final String DESCRIPTION = "Listener is created but not unregistered";
    private static final String EXPLANATION = "Reference to an interface class must be set to null as user moves out of Activity or Fragment";
    private static final Category CATEGORY = Category.PERFORMANCE;
    private static final int PRIORITY = 5;
    private static final Severity SEVERITY = Severity.WARNING;

    public static final Issue ISSUE = Issue.create(
            ID,
            DESCRIPTION,
            EXPLANATION,
            CATEGORY,
            PRIORITY,
            SEVERITY,
            new Implementation(
                    LeakingListenerDetector.class,
                    Scope.JAVA_FILE_SCOPE)
    );
}
