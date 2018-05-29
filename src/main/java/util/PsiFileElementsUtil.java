package util;

import com.intellij.psi.*;
import com.intellij.psi.codeStyle.JavaCodeStyleManager;

import java.util.Arrays;

public class PsiFileElementsUtil {
    public static void addPackageStatenentToFile(PsiFile psiFile, String packageName) {
        PsiElementFactory factory = JavaPsiFacade.getElementFactory(psiFile.getProject());
        PsiPackageStatement packageStatement = factory.createPackageStatement(packageName);
        psiFile.add(packageStatement);
    }

    public static void addClassToFile(PsiFile psiFile) {
        PsiElementFactory factory = JavaPsiFacade.getElementFactory(psiFile.getProject());
        PsiClass psiClass = factory.createClass(psiFile.getName().replace(".java", ""));
        psiFile.add(psiClass);
    }

    public static void addTestMethodToClass(PsiClass psiClass, String methodName) {
        PsiElementFactory factory = JavaPsiFacade.getElementFactory(psiClass.getProject());
        PsiMethod method = factory.createMethod(methodName, PsiType.VOID);
        method = factory.createMethodFromText("@org.junit.Test\n" + method.getText(), psiClass);
        JavaCodeStyleManager.getInstance(method.getProject()).shortenClassReferences(method);
        psiClass.add(method);
    }

    public static boolean classContainsMethod(PsiClass psiClass, String methodName) {
        return Arrays.stream(psiClass.getMethods()).anyMatch(m -> m.getName().equals(methodName));
    }
}
