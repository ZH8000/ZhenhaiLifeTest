package tw.com.zhenhai.lifetest;

import org.eclipse.swt._
import org.eclipse.swt.widgets._
import org.eclipse.swt.layout._
import org.eclipse.swt.events._

class HistoryQueryResult(mainWindowShell: Shell, title: String) extends Composite(mainWindowShell, SWT.NONE) {
  
  def init() {

    val gridLayout = new GridLayout(2, true)

    gridLayout.horizontalSpacing = 20
    gridLayout.verticalSpacing = 20
    gridLayout.marginWidth = 200
    gridLayout.marginHeight = 200

    this.setLayout(gridLayout)

    val titleLabel = new Label(this, SWT.NONE)
    val titleLabelLayoutData = new GridData
    titleLabelLayoutData.horizontalAlignment = GridData.BEGINNING
    titleLabelLayoutData.grabExcessHorizontalSpace = true
    titleLabel.setText(title)
    titleLabel.setLayoutData(titleLabelLayoutData)

    val navigationButtons = new NavigationButtons(this)
    val navigationButtonsLayoutData = new GridData
    navigationButtonsLayoutData.heightHint = 50
    navigationButtonsLayoutData.widthHint = 300
    navigationButtonsLayoutData.horizontalAlignment = GridData.END
    navigationButtonsLayoutData.grabExcessHorizontalSpace = true
    navigationButtons.setLayoutData(navigationButtonsLayoutData)


    val table = new Table(this, SWT.BORDER)
    val tableLayoutData = new GridData
    tableLayoutData.horizontalAlignment = GridData.FILL
    tableLayoutData.verticalAlignment = GridData.FILL
    tableLayoutData.grabExcessHorizontalSpace = true
    tableLayoutData.grabExcessVerticalSpace = true
    tableLayoutData.horizontalSpan = 2
    table.setLayoutData(tableLayoutData)
    table.addListener (SWT.DefaultSelection, new Listener () {
      override def handleEvent(event: Event) {
        val selectedIndex = table.getSelectionIndex
        val selectedItem = table.getItem(selectedIndex)
        MainWindow.appendLog(s"點選 [$selectedIndex] => ${selectedItem.getText(0)} / ${selectedItem.getText(1)}")
        MainWindow.pushComposite(new OrderStatusSummary(-1, -1, -1, MainWindow.mainWindowShell))
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
  }

  init()
}
