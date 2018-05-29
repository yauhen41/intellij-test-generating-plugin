package util;

import com.intellij.psi.PsiMethod;
import com.intellij.psi.javadoc.PsiDocComment;
import com.intellij.psi.javadoc.PsiDocTag;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class PsiJavadocUtil {

    public static List<PsiDocTag> getShouldTags(PsiMethod psiMethod) {
        PsiDocComment psiDocComment = psiMethod.getDocComment();
        if (psiDocComment == null)
            return Collections.emptyList();
        return Arrays.stream(psiDocComment.getTags())
                .filter(t -> "should".equals(t.getName()))
                .collect(Collectors.toList());
    }

    public static String getRequirementFromShouldTag(PsiDocTag tag) {
        String text = tag.getText().replace("@should ", "");
        if (text.contains("\n"))
            text = text.replace(text.substring(text.indexOf('\n')), "");
        return text.trim();
    }
}
