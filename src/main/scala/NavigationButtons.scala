package tw.com.zhenhai.lifetest;

import org.eclipse.swt._
import org.eclipse.swt.widgets._
import org.eclipse.swt.layout._
import org.eclipse.swt.events._

class NavigationButtons(parent: Composite) extends Composite(parent, SWT.NONE) {
  
  val rowLayout = new FillLayout

  this.setLayout(rowLayout)
  rowLayout.spacing = 20
  
  val homeButton = new Button(this, SWT.PUSH)
  val backButton = new Button(this, SWT.PUSH)

  homeButton.setText("主選單")
  homeButton.addSelectionListener(new SelectionAdapter() {
    override def widgetSelected(e: SelectionEvent) {
      MainWindow.popUntilLastComposite()
    }
  })

  backButton.setText("上一頁")
  backButton.addSelectionListener(new SelectionAdapter() {
    override def widgetSelected(e: SelectionEvent) {
      MainWindow.popComposite()
    }
  })

}


