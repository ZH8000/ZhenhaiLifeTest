package tw.com.zhenhai.lifetest;

import org.eclipse.swt._
import org.eclipse.swt.widgets._
import org.eclipse.swt.layout._
import org.eclipse.swt.events._

/**
 *  此類別用來顯示「歷史資料」（用日期來查詢）的查詢結果，會以列表的方式來
 *  顯示該日期內，有進行的測試。
 *
 *  @param    mainWindowShell   主視窗物件
 *  @param    dateString        以 yyyy-MM-dd 的字串形式表示的查詢日期
 */
class HistoryQueryResult(mainWindowShell: Shell, dateString: String) extends Composite(mainWindowShell, SWT.NONE) {

  val gridLayout = MainGridLayout.createLayout(2)
  val titleLabel = createTitleLabel()
  val navigationButtons = createNavigationButtons()
  val dataTable = createDataTable()
  val dataList = TestSetting.db.getTestingForDate(dateString)

  /**
   *  建立頁面上方的「查詢日期」的標頭的文字標籤
   *
   *  @return   「查詢日期」的標頭的文字標籤
   */
  def createTitleLabel() = {
    val titleLabel = new Label(this, SWT.NONE)
    val titleLabelLayoutData = new GridData
    titleLabelLayoutData.horizontalAlignment = GridData.BEGINNING
    titleLabelLayoutData.grabExcessHorizontalSpace = true
    titleLabel.setText(s"查詢日期：$dateString")
    titleLabel.setLayoutData(titleLabelLayoutData)
    titleLabel

  }

  /**
   *  建立頁面右上方導覽按鈕
   *
   *  @return     導覽按鈕
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
   *  建立頁面下方顯示查詢結果用的表格
   *
   *  @return     查詢結果表格
   */
  def createDataTable() = {
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
    table
  }

  /**
   *  將查詢結果的資料填入表格中
   */
  def addDataToTable() {
    dataList.foreach { data =>
      val item = new TableItem(dataTable, SWT.NONE)
      item.setText(0, s"# ${data.id}")
      item.setText(1, data.partNo)
      item.setText(2, f"${data.capacity}%.2f")
      item.setText(3, f"${data.dxValue}%.2f")
      item.setText(4, data.leakCurrent)
      item.setText(5, data.marginOfError)
      item.setText(6, data.testingTime.toString)
      item.setText(7, data.testingInterval.toString)
      item.setText(8, BoardToBlock.from(data.daughterBoard, data.testingBoard).toString)
      item.setText(9, data.statusDescription)
    }

    (0 until dataTable.getColumns.size).foreach { i => 
      val column = dataTable.getColumn(i)
      column.pack()
      column.setWidth(column.getWidth + 30)
    }
  }
 
  /**
   *  設定此頁的 Layout 並將查詢結果寫入表格元件中
   */
  def init() {
    this.setLayout(gridLayout)
    addDataToTable()
  }

  init()
}
