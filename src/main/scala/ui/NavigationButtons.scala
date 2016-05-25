package tw.com.zhenhai.lifetest

import org.eclipse.swt._
import org.eclipse.swt.widgets._
import org.eclipse.swt.layout._
import org.eclipse.swt.events._

/**
 *  建立導覽（上一頁、回主畫面）按鈕
 *
 *  @param    parent    上一層的 Compsoite 物件
 */
class NavigationButtons(parent: Composite) extends Composite(parent, SWT.NONE) {
  
  val homeButton = createHomeButton()
  val backButton = createBackButton()

  /**
   *  建立「回主畫面」按鈕
   *
   *  @param      「回主畫面」的按鈕
   */
  def createHomeButton() = {
    val homeButton = new Button(this, SWT.PUSH)
    homeButton.setText("主選單")
    homeButton.addSelectionListener(new SelectionAdapter() {
      override def widgetSelected(e: SelectionEvent) {
        MainWindow.appendLog("點選「主選單」")
        MainWindow.popUntilLastComposite()
      }
    })
    homeButton
  }

  /**
   *  建立「回上一頁」的按鈕
   *
   *  @param      「回上一頁」的按鈕
   */
  def createBackButton() = {
    val backButton = new Button(this, SWT.PUSH)
    backButton.setText("上一頁")
    backButton.addSelectionListener(new SelectionAdapter() {
      override def widgetSelected(e: SelectionEvent) {
        MainWindow.appendLog("點選「上一頁」")
        MainWindow.popComposite()
      }
    })
    backButton
  }

  /**
   *  初始化並設定 Layout
   */
  def init() = {
    val rowLayout = new FillLayout
    this.setLayout(rowLayout)
    rowLayout.spacing = 20
  }


  init()

}


