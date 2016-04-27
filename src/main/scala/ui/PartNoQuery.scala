package tw.com.zhenhai.lifetest;

import org.eclipse.swt._
import org.eclipse.swt.widgets._
import org.eclipse.swt.layout._
import org.eclipse.swt.events._

class PartNoQuery(mainWindowShell: Shell) extends Composite(mainWindowShell, SWT.NONE) {

  def init() {

    val gridLayout = new GridLayout(1, true)

    gridLayout.horizontalSpacing = 20
    gridLayout.verticalSpacing = 20
    //gridLayout.marginWidth = 200
    //gridLayout.marginHeight = 200

    this.setLayout(gridLayout)

    val navigationButtons = new NavigationButtons(this)
    val navigationButtonsLayoutData = new GridData
    navigationButtonsLayoutData.heightHint = 50
    navigationButtonsLayoutData.widthHint = 300
    navigationButtonsLayoutData.horizontalAlignment = GridData.END
    navigationButtonsLayoutData.grabExcessHorizontalSpace = true
    navigationButtons.setLayoutData(navigationButtonsLayoutData)
   
    val composite = new Composite(this, SWT.NONE)
    val compositeLayoutData = new GridData
    compositeLayoutData.horizontalAlignment = GridData.FILL
    compositeLayoutData.grabExcessHorizontalSpace = true
    compositeLayoutData.verticalAlignment = GridData.FILL
    compositeLayoutData.grabExcessVerticalSpace = true
    composite.setLayoutData(compositeLayoutData)
    composite.setLayout(new GridLayout(3, false))

    val title = new Label(composite, SWT.LEFT)
    title.setText("料號：")

    val textEntry = new Text(composite, SWT.BORDER|SWT.SEARCH|SWT.ICON_CANCEL)
    val textEntryLayoutData = new GridData
    textEntryLayoutData.horizontalAlignment = GridData.FILL
    textEntryLayoutData.grabExcessHorizontalSpace = true
    textEntry.setLayoutData(textEntryLayoutData)

    val searchButton = new Button(composite, SWT.PUSH)
    searchButton.setText("搜尋")
    searchButton.addSelectionListener(new SelectionAdapter() {
      override def widgetSelected(e: SelectionEvent) {

        MainWindow.appendLog(s"查詢料號：${textEntry.getText}")
      }
    })
    val dataTable = new Table(composite, SWT.BORDER)
    val dataTableLayoutData = new GridData(SWT.FILL, SWT.FILL, true, true)
    dataTableLayoutData.horizontalSpan = 3
    dataTable.setLayoutData(dataTableLayoutData)
  }

  init()
}
