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
                    if (onStopPresent) {
                        for (UField fieldName : fieldNames) {
                            if (!onStopBody.contains(fieldName + ".remove")) {
                                context.report(ISSUE_LT, classNode.getRBrace(),
                                        context.getLocation(fieldName),
                                        "Leaking Thread",
                                        getFix(onStopMethod, onStopBody, fieldName.getName())
                                );
                            } else {
                                context.report(ISSUE_LT, classNode.getRBrace(),
                                        context.getLocation(Objects.requireNonNull(classNode.findFieldByName(fieldName.getName(), false))),
                                        "Leaking Thread",
                                        getFix(Objects.requireNonNull(classNode.getRBrace()), fieldName.getName())
                                );
                            }
                        }
                    }
                }
            }

        }
    }

    private LintFix getFix(PsiElement element, String fieldName) {

        StringBuilder fix = new StringBuilder("\t@Override\n" +
                "\tpublic void onStop(){\n");
        fix.append("\t\t if(").append(fieldName).append("!= null){").append(" .removeCallbacksAndMessages(null); } ");
        fix.append("\t\t super.onStop();\n"
                + "\t}");
        String logCallSource = element.getText();
        LintFix.GroupBuilder fixGrouper = fix().group();
        fixGrouper.add(fix().replace().text(logCallSource).shortenNames().reformat(true).beginning().with(fix.toString()).build());
        return fixGrouper.build();
    }

    private LintFix getFix(PsiElement element, String onStopBody, String fieldName) {
        StringBuilder fix = new StringBuilder();
        String part1 = onStopBody.substring(0, onStopBody.indexOf("{"));
        String part2 = onStopBody.substring(onStopBody.indexOf("{")+1, onStopBody.length()-1);
        String finalFix = part1.concat("\t\t if(" + fieldName + "!= null){" + fieldName + ".removeCallbacksAndMessages(null); } ").concat(part2);
        finalFix = finalFix.replace("\r", "\n");
        fix.append(finalFix);
        String source = element.getText();
        LintFix.GroupBuilder fixGrouper = fix().group();
        fixGrouper.add(fix().replace().text(source).shortenNames().reformat(true).with(String.valueOf(fix)).build());
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