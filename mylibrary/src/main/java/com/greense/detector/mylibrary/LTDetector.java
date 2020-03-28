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
import com.intellij.psi.PsiElement;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.uast.UClass;
import org.jetbrains.uast.UField;
import org.jetbrains.uast.UMethod;
import org.jetbrains.uast.UVariable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class LTDetector extends Detector implements Detector.UastScanner {

    private JavaContext context = null;

    @Override
    public List<String> applicableSuperClasses() {
        return Arrays.asList("androidx.appcompat.app.AppCompatActivity", "android.support.v7.app.AppCompatActivity",
                "androidx.fragment.app.Fragment", "android.support.v7.app.Fragment");
    }

    @Override
    public final void visitClass(@NotNull JavaContext context, @NotNull UClass classNode) {
        for (String className : Objects.requireNonNull(applicableSuperClasses())) {
            String qualifiedClassName = Objects.requireNonNull(classNode.getJavaPsi().getSuperClass()).getQualifiedName();
            if (Objects.requireNonNull(qualifiedClassName).equals(className)) {
                boolean onStopPresent = false;
                String onStopBody = "";
                UMethod onStopMethod = null;
                for (UMethod method: classNode.getMethods()) {
                    if(method.getName().equals("onStop")){
                        onStopPresent = true;
                        onStopMethod = method;
                        onStopBody = Objects.requireNonNull(method.getUastBody()).toString();
                    }
                }
                ArrayList<UField> fieldNames = new ArrayList<>();
                for (UField field: classNode.getFields()){
                    String element = Objects.requireNonNull(field.getTypeElement()).toString();
                    element = element.substring(element.lastIndexOf(":") +1, element.length());
                    if (element.equalsIgnoreCase("Handler")){
                        fieldNames.add(field);
                    }
                }
                if (fieldNames.size() > 0) {
                    for (UField fieldName : fieldNames) {
                        if (onStopPresent){
                            if(!onStopBody.contains(fieldName.getName() + ".remove")) {
                                context.report(ISSUE_LT, classNode.getRBrace(),
                                        context.getLocation(Objects.requireNonNull(Objects.requireNonNull(onStopMethod.getJavaPsi().getBody()).getLBrace())),
                                        "Leaking Thread",
                                        getFix(onStopMethod, fieldName.getName())
                                );
                            }
                        }
                        else {
                            context.report(ISSUE_LT, classNode.getRBrace(),
                                    context.getLocation(fieldName),
                                    "Leaking Thread",
                                    getFix(Objects.requireNonNull(classNode.getRBrace()), fieldName.getName())
                            );
                        }
                    }
                }
            }

        }
    }

    private LintFix getFix(PsiElement element, String fieldName) {

        String source = element.getText();
        LintFix.GroupBuilder fixGrouper = fix().group();
        String fix = "\t@Override \n" +
                "\tpublic void onStop(){\n" + "\t\t if(" + fieldName + "!= null){" + fieldName + ".removeCallbacksAndMessages(null); } " +
                "\t\t super.onStop();\n"
                + "\t}";
        fixGrouper.add(fix().replace().text(source).shortenNames().reformat(true).end().with(fix).build());
        return fixGrouper.build();
    }

    private LintFix getFix(UMethod element, String fieldName) {
        String fix = ("\t\t if(" + fieldName + "!= null){" + fieldName + ".removeCallbacksAndMessages(null); } ");
        fix = fix.replace("\r", "");
        String source = Objects.requireNonNull(element.getJavaPsi().getBody()).getLBrace().getText();
        LintFix.GroupBuilder fixGrouper = fix().group();
        fixGrouper.add(fix().replace().text(source).shortenNames().reformat(true).end().with(fix).build());
        return fixGrouper.build();
    }
//
//    @Override
//    public List<Class<? extends UElement>> getApplicableUastTypes() {
//        return Collections.singletonList(UVariable.class);
//    }

//    @Override
//    public UElementHandler createUastHandler(JavaContext context){
//        this.context = context;
//        return new MyUElementHandler();
//    }

    static final Issue ISSUE_LT =
            Issue.create("Leaking Thread",
                    "All callbacks for handler must be removed when app goes to background",
                    "",
                    Category.PERFORMANCE,
                    6,
                    Severity.WARNING,
                    new Implementation(LTDetector.class, Scope.JAVA_FILE_SCOPE));

    private class MyUElementHandler extends UElementHandler {

        @Override
        public void visitVariable(@NotNull UVariable expression){
            String typeName = expression.getTypeReference().getQualifiedName();
            if (typeName != null && typeName.equalsIgnoreCase("android.os.Handler")) {
//                context.report(ISSUE_LT, (UElement) expression,
//                        context.getLocation((UElement) expression),
//                        "Leaking Thread",
//                        getHandlerFix(expression)
//                );
            }
        }
    }

    private LintFix getHandlerFix(UVariable expression) {
        String handlerName = expression.getName();
        String fix = handlerName + ".removeCallbacksAndMessages(null);";
//            Method onStop = expression.getUastParent())().getMethod("onStop");

        String logCallSource = expression.asSourceString();
        LintFix.GroupBuilder fixGrouper = fix().group();
        fixGrouper.add(fix().replace().text(logCallSource).shortenNames().reformat(true).with(fix).build());
        return fixGrouper.build();

    }
}