package action;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DataKeys;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.ui.Messages;
import com.intellij.psi.*;
import com.intellij.psi.javadoc.PsiDocTag;
import com.intellij.psi.util.PsiTreeUtil;
import exception.BadArchitectureException;
import org.apache.commons.lang.WordUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ui.ChooseTestsDialog;
import util.CreationUtil;
import util.PsiFileElementsUtil;
import util.PsiJavadocUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

public class GenerateTestsAction extends AnAction {

    @Override
    public void actionPerformed(AnActionEvent event) {
        PsiMethod psiMethod = getPsiMethod(event);
        List<PsiDocTag> shouldTags = PsiJavadocUtil.getShouldTags(psiMethod);
        List<String> requirements = new ArrayList<>();
        if (shouldTags.size() > 1) {
            ChooseTestsDialog dialog = new ChooseTestsDialog(psiMethod);
            dialog.show();
            if (!dialog.isOK())
                return;
            requirements = dialog.getRequirements();
        } else if (shouldTags.size() == 1) {
            requirements.add(PsiJavadocUtil.getRequirementFromShouldTag(shouldTags.get(0)));
        } else {
            requirements.add("Work");
        }

        try {
            PsiFile testFile = getTestFile(psiMethod);
            generateTests(psiMethod, (PsiJavaFile) testFile, requirements);
        } catch (BadArchitectureException ex) {
            Messages.showErrorDialog("Place your code inside `src` directory!", "Bad Architecture");
        }
    }

    private PsiFile getTestFile(PsiMethod method) throws BadArchitectureException {
        PsiDirectory directoryForTestFile = getDirectoryForTestFile(method);
        PsiJavaFile baseFile = (PsiJavaFile) method.getContainingFile();
        String testFileName = baseFile.getName().replace(".java", "Test.java");
        if (directoryForTestFile.findFile(testFileName) == null)
            CreationUtil.createFile(directoryForTestFile, testFileName, baseFile.getPackageName());
        return directoryForTestFile.findFile(testFileName);
    }

    private PsiDirectory getDirectoryForTestFile(PsiMethod method) throws BadArchitectureException {
        PsiDirectory dir = method.getContainingFile().getContainingDirectory();
        Stack<String> path = new Stack<>();
        while (!dir.getName().equals("src")) {
            path.push(dir.getName());
            dir = dir.getParent();
            if (dir == null)
                throw new BadArchitectureException();
        }
        while (!path.isEmpty()) {
            String nextDirName = path.pop();
            if ("main".equals(nextDirName))
                nextDirName = "test";
            if (dir.findSubdirectory(nextDirName) == null)
                CreationUtil.createDirectory(dir, nextDirName);
            dir = dir.findSubdirectory(nextDirName);
        }
        return dir;
    }

    private void generateTests(PsiMethod method, PsiJavaFile testFile, List<String> requirements) {
        new WriteCommandAction.Simple(testFile.getProject()) {
            @Override
            protected void run() throws Throwable {
                PsiClass testClass = testFile.getClasses()[0];
                for (String requirement : requirements) {
                    String testMethodName = method.getName() + "Should"
                            + WordUtils.capitalizeFully(requirement).replace(" ", "");
                    for (char c : testMethodName.toCharArray())
                        if (!Character.isJavaIdentifierPart(c))
                            testMethodName = testMethodName.replace("" + c, "");
                    if (!PsiFileElementsUtil.classContainsMethod(testClass, testMethodName))
                        PsiFileElementsUtil.addTestMethodToClass(testClass, testMethodName);
                }
            }
        }.execute();
    }

    @Override
    public void update(AnActionEvent e) {
        if (getPsiMethod(e) == null)
            e.getPresentation().setEnabled(false);
    }

    @Nullable
    private PsiMethod getPsiMethod(AnActionEvent e) {
        PsiFile psiFile = e.getData(DataKeys.PSI_FILE);
        Editor editor = e.getData(DataKeys.EDITOR);
        if (psiFile == null || editor == null)
            return null;
        return getPsiMethod(psiFile, editor.getCaretModel().getOffset());
    }

    private PsiMethod getPsiMethod(@NotNull PsiFile psiFile, int offset) {
        PsiElement elementAtCaret = psiFile.findElementAt(offset);
        return PsiTreeUtil.getParentOfType(elementAtCaret, PsiMethod.class);
    }
}
