package com.greense.detector.mylibrary;

import com.android.tools.lint.client.api.UElementHandler;
import com.android.tools.lint.detector.api.Category;
import com.android.tools.lint.detector.api.Context;
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
import org.jetbrains.uast.UElement;
import org.jetbrains.uast.UMethod;

import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class LCDetector extends Detector implements Detector.UastScanner {

    private ArrayList<UClass> interfaceList = new ArrayList<>();

    @Override
    public void afterCheckEachProject(Context context) {
        super.afterCheckEachProject(context);
        if (context.getPhase() == 1) { // Rescan classes
            context.requestRepeat(this, LCDetector.ISSUE_LC.getImplementation().getScope());
        }
    }

    @Override
    public List<String> applicableSuperClasses() {
        return Arrays.asList("androidx.appcompat.app.AppCompatActivity", "android.support.v7.app.AppCompatActivity",
                "androidx.fragment.app.Fragment", "android.support.v7.app.Fragment");
    }

    @Override
    public final void visitClass(@NotNull JavaContext context, @NotNull UClass classNode) {
        if(context.getPhase() == 2) {
            for (String className : Objects.requireNonNull(applicableSuperClasses())) {
                String qualifiedClassName = Objects.requireNonNull(classNode.getJavaPsi().getSuperClass()).getQualifiedName();
                if (Objects.requireNonNull(qualifiedClassName).equals(className)) {
                    boolean onStopPresent = false;
                    String onStopBody = "";
                    for (UMethod method: classNode.getMethods()) {
                        if(method.getName().equals("onStop")){
                            onStopPresent = true;
                            onStopBody = Objects.requireNonNull(method.getUastBody()).toString();
                        }
                    }
                    for (int i = 0; i < classNode.getFields().length; i++) {
                        String field = classNode.getFields()[i].getType().toString();
                        field = field.substring(field.lastIndexOf(":")+1);
                        ArrayList<String> fieldNames = new ArrayList<>();
                        for (UClass interfaceNode : interfaceList) {
                            if (Objects.requireNonNull(interfaceNode.getName()).contains(field)) {
                                fieldNames.add(classNode.getFields()[i].getPsi().getName());
                            }
                        }
                        if(fieldNames.size() > 0) {
                            if (onStopPresent) {
                                boolean correctionNeeded = false;
                                for (String fieldName : fieldNames){
                                    if(!onStopBody.contains(fieldName+"=null")){
                                        correctionNeeded = true;
                                    }
                                }

                                if(correctionNeeded) {
                                    context.report(ISSUE_LC, classNode.getRBrace(),
                                            context.getLocation(classNode.getFields()[i]),
                                            "Leaking class",
                                            getFix(classNode.getRBrace(), onStopBody, fieldNames)
                                    );
                                }
                            } else {
                                context.report(ISSUE_LC, classNode.getRBrace(),
                                        context.getLocation(classNode.getFields()[i]),
                                        "Leaking class",
                                        getFix(classNode.getRBrace(), fieldNames)
                                );
                            }
                        }
                    }
                }
            }
        }
    }


    private LintFix getFix(PsiElement element, ArrayList<String> classInterfaces) {

        StringBuilder fix = new StringBuilder("\t@Override\n" +
                "\tpublic void onStop(){\n");
        for (String fieldName : classInterfaces){
            fix.append("\t\t").append(fieldName).append(" = null;\n");
        }
        fix.append("\t\tsuper.onStop();\n"
                + "\t}");
        String logCallSource = element.getText();
        LintFix.GroupBuilder fixGrouper = fix().group();
        fixGrouper.add(fix().replace().text(logCallSource).shortenNames().reformat(true).beginning().with(fix.toString()).build());
        return fixGrouper.build();
    }

    private LintFix getFix(PsiElement element, String onStopBody, ArrayList<String> fieldNames) {
        StringBuilder fix = new StringBuilder();
        String bodyChecker = onStopBody.replace(" ", "");
        StringBuilder part2 = new StringBuilder();
        for (String fieldName : fieldNames){
            if(!bodyChecker.contains(fieldName+"=null")){
                part2.append("\t\t").append(fieldName).append(" = null;\n");
            }
        }
        String part1 = onStopBody.substring(0, onStopBody.indexOf("{"));
        String part3 = onStopBody.substring(onStopBody.indexOf("{")+1, onStopBody.length()-1);
        fix.append( part1.concat(String.valueOf(part2)).concat(part3).replace("\r", "\n"));
        String logCallSource = element.getText();
        LintFix.GroupBuilder fixGrouper = fix().group();
        fixGrouper.add(fix().replace().text(logCallSource).shortenNames().reformat(true).beginning().with(String.valueOf(fix)).build());
        return fixGrouper.build();
    }

    static final Issue ISSUE_LC =
            Issue.create("Leaking class",
                    "Leaking class",
                    "Leaking class",
                    Category.PERFORMANCE,
                    6,
                    Severity.WARNING,
                    new Implementation(LCDetector.class, Scope.JAVA_FILE_SCOPE));

    @Override
    public List<Class<? extends UElement>> getApplicableUastTypes() {
        return Arrays.asList(UClass.class);
    }

    @Override
    public MyUElementHandler createUastHandler(JavaContext context){
        if (context.getPhase() == 1) {
            return new MyUElementHandler(context);
        }
        return null;
    }

    class MyUElementHandler extends UElementHandler {

        private final JavaContext context;

        MyUElementHandler(JavaContext context) {
            this.context = context;
        }

        @Override
        public void visitClass(@NotNull UClass node) {
            if (context.getPhase() == 1) {
                process(node);
                if (node.isInterface()) {
                    interfaceList.add(node);
                }
            }
        }

        private void process(UElement node) {
            if(node.getClass().getModifiers() == Modifier.INTERFACE){
                System.out.print(node.getClass());
            }
        }
    }

}