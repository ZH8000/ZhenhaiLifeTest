package tw.com.zhenhai.lifetest;

import org.eclipse.swt._
import org.eclipse.swt.widgets.{List => SWTList, _}
import org.eclipse.swt.layout._
import org.eclipse.swt.events._
import org.eclipse.swt.custom.StackLayout

trait StackableWindow {

  val stackLayout = new StackLayout
  var compositeStack = List.empty[Composite]
  def mainWindowShell: Shell

  def pushComposite(composite: Composite) {
    compositeStack ::= composite
    stackLayout.topControl = composite
    composite.pack()
    mainWindowShell.layout()
  }

  def popComposite() {

    val previousComposite = compositeStack.head

    compositeStack = compositeStack.drop(1)
    stackLayout.topControl = compositeStack.head
    stackLayout.topControl.pack()
    mainWindowShell.layout()
    previousComposite.dispose()
  }

  def popUntilLastComposite() {

    val uselessComposite = compositeStack.dropRight(1)
    compositeStack = compositeStack.last :: Nil
    stackLayout.topControl = compositeStack.last
    stackLayout.topControl.pack()
    mainWindowShell.layout()
    uselessComposite.foreach(_.dispose())
  }

}

object MainWindow extends StackableWindow {

  var mainWindowShell: Shell = _

  def createWindow(parentShell: Shell) = {
    val shell = new Shell(parentShell.getDisplay, SWT.SHELL_TRIM| SWT.APPLICATION_MODAL)
    mainWindowShell = shell
    shell.setLayout(stackLayout)
    pushComposite(new MainMenu(shell))
    shell.setSize(shell.getDisplay.getBounds.width, shell.getDisplay.getBounds.height)
    shell
  }

}
