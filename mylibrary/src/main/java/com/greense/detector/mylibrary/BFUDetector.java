package com.greense.detector.mylibrary;

import com.android.tools.lint.detector.api.Category;
import com.android.tools.lint.detector.api.Detector;
import com.android.tools.lint.detector.api.Implementation;
import com.android.tools.lint.detector.api.Issue;
import com.android.tools.lint.detector.api.JavaContext;
import com.android.tools.lint.detector.api.Scope;
import com.android.tools.lint.detector.api.Severity;
import com.android.tools.lint.detector.api.TextFormat;
import com.intellij.psi.PsiMethod;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.uast.UCallExpression;

import java.util.Arrays;
import java.util.List;

public class BFUDetector extends Detector implements Detector.UastScanner {

    private final String CREATE_BITMAP = "createBitmap";

    @Override
    public List<String> getApplicableMethodNames() {
        return Arrays.asList(CREATE_BITMAP);
    }

    @Override
    public final void visitMethodCall(@NotNull JavaContext context, @NotNull UCallExpression call, @NotNull PsiMethod method) {
        if(method.getName().equals(CREATE_BITMAP)){
            context.report(ISSUE_BFU, call,
                    context.getLocation(call),
                    ISSUE_BFU.getExplanation(TextFormat.TEXT),
                    null
            );
        }
    }

    static final Issue ISSUE_BFU =
            Issue.create("Bitmap Format Usage",
                    "Bitmap Format Usage",
                    "Using a large number of bitmaps in an application can lead to increase in energy consumption",
                    Category.PERFORMANCE,
                    6,
                    Severity.WARNING,
                    new Implementation(BFUDetector.class, Scope.JAVA_FILE_SCOPE));

}