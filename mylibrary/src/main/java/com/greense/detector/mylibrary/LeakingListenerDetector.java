package com.greense.detector.mylibrary;

import com.android.tools.lint.client.api.UElementHandler;
import com.android.tools.lint.detector.api.Detector;
import com.android.tools.lint.detector.api.JavaContext;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.uast.UClass;
import org.jetbrains.uast.UElement;
import org.jetbrains.uast.UTypeReferenceExpression;
import org.jetbrains.uast.UVariable;

import java.lang.reflect.Modifier;

public class LeakingListenerDetector extends Detector implements Detector.UastScanner {

    @Override
    public InterfaceUElementHandler createUastHandler(JavaContext context){
        return new InterfaceUElementHandler(context);
    }

   class InterfaceUElementHandler extends  UElementHandler {

       private final JavaContext context;

       InterfaceUElementHandler(JavaContext context) {
           this.context = context;
       }

       @Override
       public void visitVariable(@NotNull UVariable node){
           process(node);
       }

       @Override
       public void visitClass(@NotNull UClass node) {
           for (UTypeReferenceExpression it: node.getUastSuperTypes()) {
               process(it);
           }
       }

       private void process(UElement node) {
           if(node.getClass().getModifiers() == Modifier.INTERFACE){
               context.report(LeakingListenerIssue.ISSUE,
                       node, context.getLocation(node),
                       "Should not be using Interface");
               System.out.print(node.getClass());
           }
       }
   }

}
