package tw.com.zhenhai.lifetest;

import org.eclipse.swt._
import org.eclipse.swt.widgets._
import org.eclipse.swt.layout._
import org.eclipse.swt.events._

object HistoryQuery {

  

  def createWindow(parentShell: Shell) = {

    val shell = new Shell(parentShell.getDisplay, SWT.SHELL_TRIM| SWT.APPLICATION_MODAL)
    val gridLayout = new GridLayout(1, true)

    gridLayout.horizontalSpacing = 20
    gridLayout.verticalSpacing = 20
    gridLayout.marginWidth = 200
    gridLayout.marginHeight = 200

    shell.setLayout(gridLayout)

    val navigationButtons = new NavigationButtons(shell)
    val navigationButtonsLayoutData = new GridData
    navigationButtonsLayoutData.heightHint = 50
    navigationButtonsLayoutData.widthHint = 300
    navigationButtonsLayoutData.horizontalAlignment = GridData.END
    navigationButtonsLayoutData.grabExcessHorizontalSpace = true
    navigationButtons.setLayoutData(navigationButtonsLayoutData)

    val calendar = new DateTime (shell, SWT.CALENDAR)
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
        val queryResultWindow = QueryResult.createWindow(s"查詢日期：$queryDate", shell)
        queryResultWindow.open()
      }
    })
   
  
    shell.setMaximized(true)
    shell
  }
}
