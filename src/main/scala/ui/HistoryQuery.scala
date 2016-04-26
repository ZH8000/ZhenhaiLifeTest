package tw.com.zhenhai.lifetest;

import org.eclipse.swt._
import org.eclipse.swt.widgets._
import org.eclipse.swt.layout._
import org.eclipse.swt.events._

class HistoryQuery(mainWindowShell: Shell) extends Composite(mainWindowShell, SWT.NONE) {

  def init() = {

    val gridLayout = new GridLayout(1, true)

    gridLayout.horizontalSpacing = 20
    gridLayout.verticalSpacing = 20
    // gridLayout.marginWidth = 200
    // gridLayout.marginHeight = 200

    this.setLayout(gridLayout)

    val navigationButtons = new NavigationButtons(this)
    val navigationButtonsLayoutData = new GridData
    navigationButtonsLayoutData.heightHint = 50
    navigationButtonsLayoutData.widthHint = 300
    navigationButtonsLayoutData.horizontalAlignment = GridData.END
    navigationButtonsLayoutData.grabExcessHorizontalSpace = true
    navigationButtons.setLayoutData(navigationButtonsLayoutData)

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
        val queryDate = s"${calendar.getYear}-${calendar.getMonth}-${calendar.getDay}"
        val queryResultWindow = new HistoryQueryResult(MainWindow.mainWindowShell, s"查詢日期：$queryDate")
        MainWindow.appendLog(s"點選「$queryDate」")
        MainWindow.pushComposite(queryResultWindow)
      }
    })
  }

  init()
}
