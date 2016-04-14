package tw.com.zhenhai.lifetest;

import org.eclipse.swt._
import org.eclipse.swt.widgets.{List => SWTList, _}
import org.eclipse.swt.layout._
import org.eclipse.swt.events._
import org.eclipse.swt.custom.StackLayout
import java.io._
import java.util.Date
import java.text.SimpleDateFormat

trait StackableWindow {

  val stackLayout = new StackLayout
  var compositeStack = List.empty[Composite]
  def mainWindowShell: Shell

  def pushComposite(composite: Composite) {
    compositeStack ::= composite
    stackLayout.topControl = composite
    //composite.pack()
    mainWindowShell.layout()
  }

  def popComposite() {

    val previousComposite = compositeStack.head

    previousComposite.setVisible(false)
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

  private var loggerHolder: Option[PrintWriter] = None
  private val dateFormatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")

  def appendLog(line: String) {
    loggerHolder.foreach { logger =>
      val timestamp = dateFormatter.format(new Date)
      logger.println(s"[$timestamp] $line")
      logger.flush()
    }
  }

  def closeLogFile() {
    loggerHolder.foreach(_.close())
  }
  
  def createWindow(parentShell: Shell) = {
    val shell = new Shell(parentShell.getDisplay, SWT.SHELL_TRIM| SWT.APPLICATION_MODAL)
    
    mainWindowShell = shell
    val dateFormatter = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss")
    val logFileName = dateFormatter.format(new Date) + ".txt"
    loggerHolder = Some(new PrintWriter(new BufferedWriter(new FileWriter(logFileName))))

    MainWindow.appendLog("Login.")

    shell.setLayout(stackLayout)
    pushComposite(new MainMenu(shell))

    shell.setSize(shell.getDisplay.getBounds.width, shell.getDisplay.getBounds.height)
    shell.addDisposeListener(new DisposeListener() {
      def widgetDisposed(event: DisposeEvent) {
        // When the child shell is disposed, change the message on the main shell
        appendLog("Logout.")
        closeLogFile()
      }
    })
    shell
  }

}
