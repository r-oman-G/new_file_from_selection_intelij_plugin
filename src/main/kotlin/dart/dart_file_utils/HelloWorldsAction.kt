import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.ui.Messages
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiManager


class CreateFileFromSelection : AnAction() {
    override fun actionPerformed(event: AnActionEvent) {
        val editor = event.getData(com.intellij.openapi.actionSystem.CommonDataKeys.EDITOR) ?: return
        val project = event.project ?: return
        val selectedText = editor.selectionModel.selectedText ?: return
        val currentFile = event.getData(com.intellij.openapi.actionSystem.CommonDataKeys.PSI_FILE) ?: return

        // Generate the default file name, appending ".new" if the original file has no extension
        val originalFileName = currentFile.name

        val fileName = Messages.showInputDialog(
            project,
            "Enter the name for the new file:",
            "New File Name",
            Messages.getQuestionIcon(),
            originalFileName,
            null,
            TextRange(0, originalFileName.lastIndexOf('.')),
        )
        if (fileName.isNullOrBlank()) return

        WriteCommandAction.runWriteCommandAction(project) {
            val directory = currentFile.containingDirectory?.virtualFile ?: return@runWriteCommandAction
            if (directory.findChild(fileName) != null) {
                Messages.showErrorDialog(
                    project,
                    "A file with this name already exists in the current directory.",
                    "Error Creating File"
                )
                return@runWriteCommandAction
            }

            val virtualFile = directory.createChildData(this, fileName)
            virtualFile.setBinaryContent(selectedText.toByteArray())
            PsiManager.getInstance(project).findFile(virtualFile)?.let {
                FileEditorManager.getInstance(project).openFile(virtualFile, true)
            }
        }
    }

    override fun update(e: AnActionEvent) {
        val editor = e.getData(com.intellij.openapi.actionSystem.CommonDataKeys.EDITOR)
        e.presentation.isEnabledAndVisible = editor != null && editor.selectionModel.hasSelection()
    }

    override fun getActionUpdateThread(): ActionUpdateThread {
        return ActionUpdateThread.EDT
    }
}


