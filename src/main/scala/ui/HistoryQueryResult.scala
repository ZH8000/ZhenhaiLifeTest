package tw.com.zhenhai.lifetest;

import org.eclipse.swt._
import org.eclipse.swt.widgets._
import org.eclipse.swt.layout._
import org.eclipse.swt.events._

class HistoryQueryResult(mainWindowShell: Shell, dateString: String) extends Composite(mainWindowShell, SWT.NONE) {

  def boardToBlock(daughterBoard: Int, testingBoard: Int): Int = {
    (daughterBoard, testingBoard) match {
      case (0, 0) => 1
      case (0, 1) => 2
      case (1, 0) => 3
      case (1, 1) => 4
      case (2, 0) => 5
      case (2, 1) => 6
      case (3, 0) => 7
      case (3, 1) => 8 
      case (4, 0) => 9
      case (4, 1) => 10
      case (5, 0) => 11
      case (5, 1) => 12
      case _      => -1
    }
  }
  
  def init() {

    val gridLayout = new GridLayout(2, true)

    gridLayout.horizontalSpacing = 20
    gridLayout.verticalSpacing = 20
    // gridLayout.marginWidth = 200
    // gridLayout.marginHeight = 200

    this.setLayout(gridLayout)

    val titleLabel = new Label(this, SWT.NONE)
    val titleLabelLayoutData = new GridData
    titleLabelLayoutData.horizontalAlignment = GridData.BEGINNING
    titleLabelLayoutData.grabExcessHorizontalSpace = true
    titleLabel.setText(s"查詢日期：$dateString")
    titleLabel.setLayoutData(titleLabelLayoutData)

    val navigationButtons = new NavigationButtons(this)
    val navigationButtonsLayoutData = new GridData
    navigationButtonsLayoutData.heightHint = 50
    navigationButtonsLayoutData.widthHint = 300
    navigationButtonsLayoutData.horizontalAlignment = GridData.END
    navigationButtonsLayoutData.grabExcessHorizontalSpace = true
    navigationButtons.setLayoutData(navigationButtonsLayoutData)

    val dataList = TestSetting.db.getTestingForDate(dateString)
    println(dataList)

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
        MainWindow.pushComposite(
          new OrderStatusSummary(
            false, -1, -1, -1, MainWindow.mainWindowShell, Some(dataList(selectedIndex))
          )
        )
      }
    })

    val columns = Array(
      new TableColumn(table, SWT.CENTER),   // 測試單編號
      new TableColumn(table, SWT.CENTER),   // 料號
      new TableColumn(table, SWT.CENTER),   // 電容值
      new TableColumn(table, SWT.CENTER),   // 損失角
      new TableColumn(table, SWT.CENTER),   // 漏電流
      new TableColumn(table, SWT.CENTER),   // 誤差值
      new TableColumn(table, SWT.CENTER),   // 測試時間
      new TableColumn(table, SWT.CENTER),   // 測試間隔
      new TableColumn(table, SWT.CENTER),   // 區塊
      new TableColumn(table, SWT.CENTER)    // 目前狀態
    )

    table.setHeaderVisible(true)
    table.setLinesVisible(true)

    columns(0).setText("測試單編號")
    columns(1).setText("料號")
    columns(2).setText("電容值")
    columns(3).setText("損失角")
    columns(4).setText("漏電流")
    columns(5).setText("誤差值")
    columns(6).setText("測試時間")
    columns(7).setText("測試間隔")
    columns(8).setText("區塊")
    columns(9).setText("目前狀態")

    dataList.foreach { data =>
      val item = new TableItem(table, SWT.NONE)
      item.setText(0, s"# ${data.id}")
      item.setText(1, data.partNo)
      item.setText(2, f"${data.capacity}%.2f")
      item.setText(3, f"${data.dxValue}%.2f")
      item.setText(4, data.leakCurrent)
      item.setText(5, data.marginOfError)
      item.setText(6, data.testingTime.toString)
      item.setText(7, data.testingInterval.toString)
      item.setText(8, boardToBlock(data.daughterBoard, data.testingBoard).toString)
      item.setText(9, data.statusDescription)
    }

    (0 until columns.size).foreach { i => 
      val column = table.getColumn(i)
      column.pack()
      column.setWidth(column.getWidth + 30)
    }

  }

  init()
}
