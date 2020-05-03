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
import org.jetbrains.uast.UExpression;

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
                        getFix(call, method.getName()), null
//                        getFix(call, method.getName())
                );
            }
        }
    }

    @Override
    public final void visitConstructor(@NotNull JavaContext context, @NotNull UCallExpression call, @NotNull PsiMethod constructor){
        //todo: check if already compressed
        if (call.getValueArguments().size()==1){
            UExpression exp = call.getValueArguments().get(0);
            String strExp = exp.toString();
            if (strExp.contains(".")) {
                strExp = strExp.substring(strExp.lastIndexOf("."), strExp.length() - 1);
                if (!strExp.contains("zip")){
                    context.report(ISSUE_DTWC, call,
                            context.getLocation(call),
                            "Load zip files if it is to be transmitted over network",
                            null
                    );
                }
            }
        }
    }

    private String getFix(UCallExpression call, String methodName) {
        String fixBefore = "ByteArrayOutputStream out = new ByteArrayOutputStream();";
        String varName = getVarName(Objects.requireNonNull(call.getSourcePsi()));
        String fixAfter = "";
//        if (varName.isEmpty()){
//            fixBefore+="(";
//            fixAfter = ").compress(Bitmap.CompressFormat.JPEG, 100, out)";
//        }
//        else{
        if(!varName.isEmpty()) {
            fixAfter = "Compress bitmap before sending it over the network" +
                    "\nExample:" +
                    "\nnew Handler().post(new Runnable() {\n" +
                    "            @Override\n" +
                    "            public void run() {\n\t" +
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
        return fixAfter;
    }

    private boolean hasEquals = false;

    private String getVarName(PsiElement source) {
        if (source != null) {
            if (source.getPrevSibling().getText().equals(" ")) {
                return getVarName(source.getPrevSibling());
            } else if (source.getPrevSibling().getText().equals("=")) {
                hasEquals = true;
                return getVarName(source.getPrevSibling());
            } else if (hasEquals) {
                hasEquals = false;
                return source.getPrevSibling().getText();
            }
        }
        return "";
    }

    static final Issue ISSUE_DTWC =
            Issue.create("Data Transmission Without Compression",
                    "Data Transmission Without Compression",
                    "Compress Bitmap before sending over network",
                    Category.PERFORMANCE,
                    6,
                    Severity.WARNING,
                    new Implementation(DTWCDetector.class, Scope.JAVA_FILE_SCOPE));

}