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
import org.jetbrains.uast.UField;
import org.jetbrains.uast.UMethod;

import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class LCDetector extends Detector implements Detector.UastScanner {

    private ArrayList<UClass> interfaceList = new ArrayList<>();

    @Override
    public void afterCheckEachProject(@NotNull Context context) {
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
                    UMethod onStopMethod = null;
                    for (UMethod method: classNode.getMethods()) {
                        if(method.getName().equals("onStop")){
                            onStopPresent = true;
                            onStopBody = Objects.requireNonNull(method.getUastBody()).toString();
                            onStopBody = onStopBody.replace(" ", "");
                            onStopBody = onStopBody.replace("\t", "");
                            onStopMethod = method;
                        }
                    }
                    ArrayList<UField> fields = new ArrayList<>();
                    for (int i = 0; i < classNode.getFields().length; i++) {
                        String field = classNode.getFields()[i].getType().toString();
                        field = field.substring(field.lastIndexOf(":") + 1);
                        for (UClass interfaceNode : interfaceList) {
                            if (Objects.requireNonNull(interfaceNode.getName()).contains(field)) {
                                fields.add(classNode.getFields()[i]);
                            }
                        }
                    }
                    if(fields.size() > 0) {
                        for (UField field : fields) {
                            if (onStopPresent) {
                                if (!onStopBody.contains(field.getName() + "=null")) {
                                    context.report(ISSUE_LC,  classNode.getRBrace(),
                                            context.getLocation(Objects.requireNonNull(Objects.requireNonNull(onStopMethod.getJavaPsi().getBody()).getLBrace())),
                                            "Leaking class",
                                            getFix(onStopMethod, field.getName())
                                    );
                                }
                            } else {
                                context.report(ISSUE_LC, classNode.getRBrace(),
                                        context.getLocation(field),
                                        "Leaking class",
                                        getFix(Objects.requireNonNull(classNode.getRBrace()), field.getName())
                                );
                            }
                        }
                    }
                }
            }
        }
    }


    private LintFix getFix(PsiElement element, String fieldName) {

        String source = element.getText();
        LintFix.GroupBuilder fixGrouper = fix().group();
        String fix = "\t@Override\n" +
                "\tpublic void onStop(){\n" + "\t\t" + fieldName + " = null;\n" +
                "\t\tsuper.onStop();\n"
                + "\t}";
        fixGrouper.add(fix().replace().text(source).shortenNames().reformat(true).end().with(fix).build());
        return fixGrouper.build();
    }

    private LintFix getFix(UMethod onStopMethod, String fieldName) {
        LintFix.GroupBuilder fixGrouper = fix().group();
        String fix = ("\t\t" + fieldName + " = null;\n");
        fix = fix.replace("\r", "");
        String source = Objects.requireNonNull(onStopMethod.getJavaPsi().getBody()).getLBrace().getText();
        fixGrouper.add(fix().replace().text(source).shortenNames().reformat(true).end().with(fix).build());
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