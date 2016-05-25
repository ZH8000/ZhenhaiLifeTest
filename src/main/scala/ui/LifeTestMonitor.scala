package tw.com.zhenhai.lifetest

import org.eclipse.swt._
import org.eclipse.swt.widgets._
import org.eclipse.swt.layout._
import org.eclipse.swt.events._

object LifeTestMonitor {

  def main(args: Array[String]) {
    val display = new Display
    val shell = new Shell(display)

    val layout = new GridLayout(1, false)
    shell.setLayout(layout)
   
    val loginFrame = new LoginWindow(shell)
    val gridData = new GridData
    gridData.horizontalAlignment = GridData.CENTER
    gridData.grabExcessHorizontalSpace = true
    gridData.verticalAlignment = GridData.CENTER
    gridData.grabExcessVerticalSpace = true

    loginFrame.setLayoutData(gridData)
    //shell.setSize(shell.getDisplay.getBounds.width, shell.getDisplay.getBounds.height)
    shell.setMaximized(true)
    //shell.setFullScreen(true)
    shell.open()
    
    while (!shell.isDisposed()) {
      if (!display.readAndDispatch()) {
        display.sleep()
      }
    }
    display.dispose()
  }
}
