package com.greense.detector.mylibrary;

import com.android.tools.lint.detector.api.Category;
import com.android.tools.lint.detector.api.Detector;
import com.android.tools.lint.detector.api.Implementation;
import com.android.tools.lint.detector.api.Issue;
import com.android.tools.lint.detector.api.JavaContext;
import com.android.tools.lint.detector.api.LintFix;
import com.android.tools.lint.detector.api.Scope;
import com.android.tools.lint.detector.api.Severity;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiMethod;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.uast.UCallExpression;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class DTWCDetector extends Detector implements Detector.UastScanner {

    private final String [] METHOD_LIST = {"decodeByteArray",
            "decodeFile",
            "decodeFileDescriptor",
            "decodeResource",
            "decodeResourceStream",
            "decodeStream"};

    @Override
    public List<String> getApplicableMethodNames() {
        return Arrays.asList(METHOD_LIST);
    }

    @Override
    public final void visitMethodCall(@NotNull JavaContext context, @NotNull UCallExpression call, @NotNull PsiMethod method) {
        for(String methodName : METHOD_LIST) {
            if (method.getName().equals(methodName)) {

                context.report(ISSUE_DTWC, call,
                        context.getLocation(call),
                        "Use internal storage instead of extenral Storage",
                        getFix(call, method.getName())
                );
            }
        }
    }

    private LintFix getFix(UCallExpression call, String methodName) {
        String fixBefore = "ByteArrayOutputStream out = new ByteArrayOutputStream();";
        String varName = getVarName(Objects.requireNonNull(call.getSourcePsi()));
        String fixAfter = "";
//        if (varName.isEmpty()){
//            fixBefore+="(";
//            fixAfter = ").compress(Bitmap.CompressFormat.JPEG, 100, out)";
//        }
//        else{
        if(!varName.isEmpty()) {
            fixAfter = ";\nnew Handler().post(new Runnable() {\n" +
                    "            @Override\n" +
                    "            public void run() {\n" +
                    "                " + varName + ".compress(Bitmap.CompressFormat.JPEG, 100, new ByteArrayOutputStream());\n" +
                    "            }\n" +
                    "        });";
        }
//        }

        String source = call.asSourceString();
        LintFix.GroupBuilder fixGrouper = fix().group();
        fixGrouper.add(fix().replace().text(source).shortenNames().reformat(true)
                .beginning().with(fixBefore)
                .end().with(fixAfter)
                .build());
        return fixGrouper.build();
    }

    private boolean hasEquals = false;

    private String getVarName(PsiElement source) {
        if (source.getPrevSibling().getText().equals(" ")){
            return getVarName(source.getPrevSibling());
        }
        else if(source.getPrevSibling().getText().equals("=")){
            hasEquals = true;
            return getVarName(source.getPrevSibling());
        }
        else if(hasEquals){
            hasEquals = false;
            return source.getPrevSibling().getText();
        }

        return "";
    }

    static final Issue ISSUE_DTWC =
            Issue.create("Public Directory",
                    "Use internal storage instead of extenral Storage",
                    "",
                    Category.PERFORMANCE,
                    6,
                    Severity.WARNING,
                    new Implementation(DTWCDetector.class, Scope.JAVA_FILE_SCOPE));

}