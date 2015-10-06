package tw.com.zhenhai.lifetest;

import org.eclipse.swt._
import org.eclipse.swt.widgets._
import org.eclipse.swt.layout._
import org.eclipse.swt.events._

object MainWindow {
  def createWindow(shell: Shell) = new Shell(shell, SWT.TITLE|SWT.SYSTEM_MODAL|SWT.CLOSE|SWT.MAX|SWT.MIN)
}
