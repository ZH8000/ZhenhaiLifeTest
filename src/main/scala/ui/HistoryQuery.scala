package tw.com.zhenhai.lifetest

import org.eclipse.swt._
import org.eclipse.swt.widgets._
import org.eclipse.swt.layout._
import org.eclipse.swt.events._

/**
 *  用來顯示「歷史資料」（以月曆形式選擇日期）的頁面
 *
 *  @param    mainWindowShell     主視窗物件
 */
class HistoryQuery(mainWindowShell: Shell) extends Composite(mainWindowShell, SWT.NONE) {

  val gridLayout = MainGridLayout.createLayout(1)
  val navigationButtons = createNavigationButtons()
  val calendar = createCalendar()

  /**
   *  建立右上方的導覽按鈕
   *
   *  @return   導覽按鈕
   */
  def createNavigationButtons() = {
    val navigationButtons = new NavigationButtons(this)
    val navigationButtonsLayoutData = new GridData
    navigationButtonsLayoutData.heightHint = 50
    navigationButtonsLayoutData.widthHint = 300
    navigationButtonsLayoutData.horizontalAlignment = GridData.END
    navigationButtonsLayoutData.grabExcessHorizontalSpace = true
    navigationButtons.setLayoutData(navigationButtonsLayoutData)
    navigationButtons
  }

  /**
   *  建立月曆元件
   *
   *  @return       月曆元件
   */
  def createCalendar() = {
    val calendar = new DateTime (this, SWT.CALENDAR)
    val calendarLayoutData = new GridData
    calendarLayoutData.widthHint = 400
    calendarLayoutData.horizontalAlignment = GridData.CENTER
    calendarLayoutData.verticalAlignment = GridData.BEGINNING
    calendarLayoutData.grabExcessHorizontalSpace = true
    calendarLayoutData.grabExcessVerticalSpace = false
    calendar.setLayoutData(calendarLayoutData)
    calendar.addListener(SWT.DefaultSelection, new Listener() {
      override def handleEvent(e: Event) {
        val queryDate = f"${calendar.getYear}-${calendar.getMonth+1}%02d-${calendar.getDay}%02d"
        val queryResultWindow = new HistoryQueryResult(MainWindow.mainWindowShell, queryDate)
        MainWindow.appendLog(s"點選「$queryDate」")
        MainWindow.pushComposite(queryResultWindow)
      }
    })
  }

  def init() = {
    this.setLayout(gridLayout)
  }

  init()
}
