package util;

import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiFile;

public class CreationUtil {

    public static void createFile(PsiDirectory directory, String fileName, String packageName) {
        new WriteCommandAction.Simple(directory.getProject()) {
            @Override
            protected void run() {
                directory.createFile(fileName);
                PsiFile file = directory.findFile(fileName);
                if (!packageName.isEmpty())
                    PsiFileElementsUtil.addPackageStatenentToFile(file, packageName);

                PsiFileElementsUtil.addClassToFile(file);
            }
        }.execute();
    }

    public static void createDirectory(PsiDirectory parentDirectory, String fileName) {
        new WriteCommandAction.Simple(parentDirectory.getProject()) {
            @Override
            protected void run() {
                parentDirectory.createSubdirectory(fileName);
            }
        }.execute();
    }

}
