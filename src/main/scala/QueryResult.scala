package tw.com.zhenhai.lifetest;

import org.eclipse.swt._
import org.eclipse.swt.widgets._
import org.eclipse.swt.layout._
import org.eclipse.swt.events._

object QueryResult {

  

  def createWindow(title: String, parentShell: Shell) = {

    val shell = new Shell(parentShell.getDisplay, SWT.SHELL_TRIM| SWT.APPLICATION_MODAL)
    val gridLayout = new GridLayout(2, true)

    gridLayout.horizontalSpacing = 20
    gridLayout.verticalSpacing = 20
    gridLayout.marginWidth = 200
    gridLayout.marginHeight = 200

    shell.setLayout(gridLayout)

    val titleLabel = new Label(shell, SWT.NONE)
    val titleLabelLayoutData = new GridData
    titleLabelLayoutData.horizontalAlignment = GridData.BEGINNING
    titleLabelLayoutData.grabExcessHorizontalSpace = true
    titleLabel.setText(title)
    titleLabel.setLayoutData(titleLabelLayoutData)

    val navigationButtons = new NavigationButtons(shell)
    val navigationButtonsLayoutData = new GridData
    navigationButtonsLayoutData.heightHint = 50
    navigationButtonsLayoutData.widthHint = 300
    navigationButtonsLayoutData.horizontalAlignment = GridData.END
    navigationButtonsLayoutData.grabExcessHorizontalSpace = true
    navigationButtons.setLayoutData(navigationButtonsLayoutData)


    val table = new Table(shell, SWT.BORDER)
    val tableLayoutData = new GridData
    tableLayoutData.horizontalAlignment = GridData.FILL
    tableLayoutData.verticalAlignment = GridData.FILL
    tableLayoutData.grabExcessHorizontalSpace = true
    tableLayoutData.grabExcessVerticalSpace = true
    tableLayoutData.horizontalSpan = 2
    table.setLayoutData(tableLayoutData)
    table.addListener (SWT.DefaultSelection, new Listener () {
      override def handleEvent(event: Event) {
        val orderSummaryWindow = OrderStatusSummary.createWindow(shell)
        orderSummaryWindow.open()
      }
    })
    val columns = Array(new TableColumn(table, SWT.CENTER), new TableColumn(table, SWT.CENTER))

    table.setHeaderVisible(true)
    table.setLinesVisible(true)

    columns(0).setText("訂單編號")
    columns(1).setText("訂單名稱")

    for (i <- 0 until 10) {
      val item = new TableItem(table, SWT.NONE)
      item.setText(0, f"$i%05d")
      item.setText(1, s"訂單 $i ")
    }

    (0 until columns.size).foreach(i => table.getColumn(i).pack())
 
    shell.setMaximized(true)
    shell
  }
}
