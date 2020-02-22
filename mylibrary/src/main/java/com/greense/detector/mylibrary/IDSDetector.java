package com.greense.detector.mylibrary;

import com.android.tools.lint.client.api.UElementHandler;
import com.android.tools.lint.detector.api.Category;
import com.android.tools.lint.detector.api.Detector;
import com.android.tools.lint.detector.api.Implementation;
import com.android.tools.lint.detector.api.Issue;
import com.android.tools.lint.detector.api.JavaContext;
import com.android.tools.lint.detector.api.LintFix;
import com.android.tools.lint.detector.api.Scope;
import com.android.tools.lint.detector.api.Severity;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.uast.UCallExpression;
import org.jetbrains.uast.UElement;
import org.jetbrains.uast.UVariable;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

public class IDSDetector extends Detector implements Detector.UastScanner {

    private static final CharSequence HASH_MAP = "HashMap";
    private JavaContext context = null;

    @Override
    public List<Class<? extends UElement>> getApplicableUastTypes() {
        return Collections.singletonList(UVariable.class);
    }

    @Override
    public UElementHandler createUastHandler(JavaContext context){
        this.context = context;
        return new MyUElementHandler();
    }

    private LintFix getFix(UCallExpression call) {
        String fix = "";
        String logCallSource = call.asSourceString();
        LintFix.GroupBuilder fixGrouper = fix().group();
        fixGrouper.add(fix().replace().text(logCallSource).shortenNames().reformat(true).with(fix).build());
        return fixGrouper.build();
    }

    static final Issue ISSUE_IDS =
            Issue.create("Inefficient Data Structure Bug",
                    "Sparse Array should be used instead of Sparse Array",
                    "Sparse Array should be used instead of Sparse Array",
                    Category.PERFORMANCE,
                    6,
                    Severity.WARNING,
                    new Implementation(IDSDetector.class, Scope.JAVA_FILE_SCOPE));

    private class MyUElementHandler extends UElementHandler {

        HashMap <String, String> map;
        @Override
        public void visitVariable(@NotNull UVariable expression){

            if(Objects.requireNonNull(Objects.requireNonNull(expression.getTypeReference()).getQualifiedName()).contains(HASH_MAP)){
                String typeName = Objects.requireNonNull(expression.getTypeReference().getSourcePsi()).getFirstChild().toString();
                String checkString = typeName.substring(typeName.indexOf("<") + 1, typeName.indexOf(","));
                if(checkString.equals("Integer")) {
                    String requiredString = typeName.substring(typeName.indexOf(",") + 1, typeName.indexOf(">"));
                    String fix = "SparseArray<" + requiredString + ">" + expression.getName().toString() + " = new SparseArray<>();";
                    String logCallSource = expression.asSourceString();
                    LintFix.GroupBuilder fixGrouper = fix().group();
                    fixGrouper.add(fix().replace().text(logCallSource).shortenNames().reformat(true).with(fix).build());
                    context.report(ISSUE_IDS, (UElement) expression,
                            context.getLocation((UElement) expression),
                            "Inefficient Data Structure Bug",
                            fixGrouper.build()
                    );
                }
            }
        }
    }
}