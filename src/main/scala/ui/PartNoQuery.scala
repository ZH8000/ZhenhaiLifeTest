package tw.com.zhenhai.lifetest;

import zhenhai.lifetest.controller.model._
import org.eclipse.swt._
import org.eclipse.swt.widgets._
import org.eclipse.swt.layout._
import org.eclipse.swt.events._

class PartNoQuery(mainWindowShell: Shell) extends Composite(mainWindowShell, SWT.NONE) {

  val navigationButtons = createNavigationButtons()
  val searchComposite = createSearchComposite()
  val searchEntry = createSearchEntry()
  val searchTable = createSearchTable()

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


  def processQuery() {
    val resultData = TestSetting.db.getTestingOrderByPartNo(searchEntry.getText)
    if (resultData.size == 0) {
      val messageBox = new MessageBox(mainWindowShell, SWT.OK)
      messageBox.setMessage("查無資料")
      messageBox.open()
    } else {

      searchTable.removeAll()
      resultData.foreach { data =>
        println("===> data..." + data)
        val item = new TableItem(searchTable, SWT.NONE)
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
        item.setData(data)
      }

      (0 until searchTable.getColumnCount).foreach { i => 
        val column = searchTable.getColumn(i)
        column.pack()
        column.setWidth(column.getWidth + 30)
      }

    }
  }

  def createSearchComposite() = {
    val composite = new Composite(this, SWT.NONE)
    val compositeLayoutData = new GridData
    compositeLayoutData.horizontalAlignment = GridData.FILL
    compositeLayoutData.grabExcessHorizontalSpace = true
    compositeLayoutData.verticalAlignment = GridData.FILL
    compositeLayoutData.grabExcessVerticalSpace = true
    composite.setLayoutData(compositeLayoutData)
    composite.setLayout(new GridLayout(3, false))
    composite
  }

  def createSearchEntry() = {

    val title = new Label(searchComposite, SWT.LEFT)
    title.setText("料號：")

    val textEntry = new Text(searchComposite, SWT.BORDER|SWT.SEARCH|SWT.ICON_CANCEL)
    val textEntryLayoutData = new GridData
    textEntryLayoutData.horizontalAlignment = GridData.FILL
    textEntryLayoutData.grabExcessHorizontalSpace = true
    textEntry.setLayoutData(textEntryLayoutData)
    textEntry.addSelectionListener(new SelectionAdapter() {
      override def widgetDefaultSelected(e: SelectionEvent) {
	processQuery()
      }
    }) 
    val searchButton = new Button(searchComposite, SWT.PUSH)
    searchButton.setText("搜尋")
    searchButton.addSelectionListener(new SelectionAdapter() {
      override def widgetSelected(e: SelectionEvent) {
        MainWindow.appendLog(s"查詢料號：${textEntry.getText}")
        processQuery()
      }
    })
    textEntry
  }

  def createSearchTable() = {
    val dataTable = new Table(searchComposite, SWT.BORDER)
    val dataTableLayoutData = new GridData(SWT.FILL, SWT.FILL, true, true)
    dataTableLayoutData.horizontalSpan = 3
    dataTable.setLayoutData(dataTableLayoutData)
    val columns = Array(
      new TableColumn(dataTable, SWT.CENTER),   // 測試單編號
      new TableColumn(dataTable, SWT.CENTER),   // 料號
      new TableColumn(dataTable, SWT.CENTER),   // 電容值
      new TableColumn(dataTable, SWT.CENTER),   // 損失角
      new TableColumn(dataTable, SWT.CENTER),   // 漏電流
      new TableColumn(dataTable, SWT.CENTER),   // 誤差值
      new TableColumn(dataTable, SWT.CENTER),   // 測試時間
      new TableColumn(dataTable, SWT.CENTER),   // 測試間隔
      new TableColumn(dataTable, SWT.CENTER),   // 區塊
      new TableColumn(dataTable, SWT.CENTER)    // 目前狀態
    )

    dataTable.setHeaderVisible(true)
    dataTable.setLinesVisible(true)

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

    dataTable.addListener (SWT.DefaultSelection, new Listener () {
      override def handleEvent(event: Event) {
        val selectedIndex = dataTable.getSelectionIndex
        val selectedItem = dataTable.getItem(selectedIndex)
        val selectedData = selectedItem.getData.asInstanceOf[TestingOrder]
        println("===> selectedData:" + selectedData)
        MainWindow.appendLog(s"點選 [$selectedIndex] => ${selectedItem.getText(0)} / ${selectedItem.getText(1)}")
        MainWindow.pushComposite(
          new OrderStatusSummary(
            false, -1, -1, -1, MainWindow.mainWindowShell, Some(selectedData)
          )
        )
      }
    })

    dataTable
  }

  def init() {

    val gridLayout = new GridLayout(1, true)

    gridLayout.horizontalSpacing = 20
    gridLayout.verticalSpacing = 20
    //gridLayout.marginWidth = 200
    //gridLayout.marginHeight = 200

    this.setLayout(gridLayout)
  }

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


  init()
}
