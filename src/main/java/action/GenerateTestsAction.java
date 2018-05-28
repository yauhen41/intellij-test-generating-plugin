package action;

import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiMethod;
import com.intellij.psi.javadoc.PsiDocComment;
import com.intellij.psi.javadoc.PsiDocTag;
import com.intellij.psi.util.PsiTreeUtil;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class GenerateTestsAction extends AnAction {

    @Override
    public void actionPerformed(AnActionEvent e) {
        PsiMethod psiMethod = getPsiMethod(e);
        assert psiMethod != null; // it is not null for sure as checked at `update` method
        List<PsiDocTag> shouldTags = getShouldTags(psiMethod);
//        Messages.showMessageDialog(project, method.getName(), "Greeting", Messages.getInformationIcon());
    }

    private List<PsiDocTag> getShouldTags(PsiMethod psiMethod) {
        PsiDocComment psiDocComment = psiMethod.getDocComment();
        if (psiDocComment == null)
            return Collections.emptyList();
        return Arrays.stream(psiDocComment.getTags())
                .filter(t -> "should".equals(t.getName()))
                .collect(Collectors.toList());
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
