package com.greense.detector.mylibrary;

import com.android.tools.lint.client.api.UElementHandler;
import com.android.tools.lint.detector.api.Category;
import com.android.tools.lint.detector.api.Detector;
import com.android.tools.lint.detector.api.Implementation;
import com.android.tools.lint.detector.api.Issue;
import com.android.tools.lint.detector.api.JavaContext;
import com.android.tools.lint.detector.api.Scope;
import com.android.tools.lint.detector.api.Severity;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.uast.UElement;
import org.jetbrains.uast.UVariable;

import java.util.Collections;
import java.util.List;

public class ERBDetector extends Detector implements Detector.UastScanner {

    private JavaContext context = null;
    private String[] typeNames = {"android.database.sqlite.SQLiteDatabase"};

    @Override
    public List<Class<? extends UElement>> getApplicableUastTypes() {
        return Collections.singletonList(UVariable.class);
    }

    @Override
    public UElementHandler createUastHandler(JavaContext context){
        this.context = context;
        return new MyUElementHandler();
    }

    static final Issue ISSUE_ERB =
            Issue.create("Early Resource Binding",
                    "This variable should be lazy initialized",
                    "",
                    Category.PERFORMANCE,
                    6,
                    Severity.WARNING,
                    new Implementation(ERBDetector.class, Scope.JAVA_FILE_SCOPE));

    private class MyUElementHandler extends UElementHandler {

        @Override
        public void visitVariable(@NotNull UVariable expression){
            String typeName = expression.getTypeReference().getQualifiedName();
            for (String myTypeName: typeNames){
                if (typeName != null && typeName.equalsIgnoreCase(myTypeName) && !expression.textContains('=')) {
                    context.report(ISSUE_ERB, (UElement) expression,
                            context.getLocation((UElement) expression),
                            "Early Resource Binding",
                            null
                    );
                }
            }
        }
    }
}