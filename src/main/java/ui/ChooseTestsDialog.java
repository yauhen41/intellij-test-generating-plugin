package ui;

import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.ui.LabeledComponent;
import com.intellij.psi.PsiMethod;
import com.intellij.ui.CollectionListModel;
import com.intellij.ui.ToolbarDecorator;
import com.intellij.ui.components.JBList;
import org.jetbrains.annotations.Nullable;
import util.PsiJavadocUtil;

import javax.swing.*;
import java.util.List;
import java.util.stream.Collectors;

public class ChooseTestsDialog extends DialogWrapper {

    private CollectionListModel<String> requirements;
    private final LabeledComponent<JPanel> component;

    public ChooseTestsDialog(PsiMethod psiMethod) {
        super(psiMethod.getProject());
        setTitle("Generating Test Methods");
        requirements = new CollectionListModel<>(
                PsiJavadocUtil.getShouldTags(psiMethod).stream()
                        .map(PsiJavadocUtil::getRequirementFromShouldTag)
                        .collect(Collectors.toList())
        );
        JBList<String> requirementList = new JBList<>(requirements);
        requirementList.setCellRenderer(new DefaultListCellRenderer());
        ToolbarDecorator decorator = ToolbarDecorator.createDecorator(requirementList);
        JPanel panel = decorator.createPanel();
        component = LabeledComponent.create(panel, "Select requirements to test:");
        init();
    }

    @Nullable
    @Override
    protected JComponent createCenterPanel() {
        return component;
    }

    public List<String> getRequirements() {
        return requirements.getItems();
    }
}
