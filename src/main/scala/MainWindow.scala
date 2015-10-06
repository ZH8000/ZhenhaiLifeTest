package tw.com.zhenhai.lifetest;

import org.eclipse.swt._
import org.eclipse.swt.widgets._

object MainWindow {
  def main(args: Array[String]) {
    val display = new Display
    val shell = new Shell(display)

    shell.open()
    
    while (!shell.isDisposed()) {
      if (!display.readAndDispatch()) {
        display.sleep()
      }
    }
    display.dispose()
  }
}
