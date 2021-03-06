package tw.com.zhenhai.lifetest

import zhenhai.lifetest.controller.model._
import org.eclipse.swt._
import org.eclipse.swt.widgets.{List => SWTList, _}
import org.eclipse.swt.layout._
import org.eclipse.swt.events._
import java.util.Date
import java.text.SimpleDateFormat
import java.io._
import org.eclipse.swt.program.Program

/**
 *  電容測試結果詳細圖表頁面
 *
 *  @param    blockNo           測試區塊編號
 *  @param    orderInfo         測試單物件
 *  @param    capacityID        電容編號
 *  @param    mainWindowShell   主視窗的 Shell 物件
 */
class OrderCapacityDetail(blockNo: Int, orderInfo: TestingOrder, 
                          capacityID: Int, mainWindowShell: Shell) extends Composite(mainWindowShell, SWT.NONE) {

  lazy val testingResult = LifeTestOptions.db.getAllTestingResult(orderInfo.id, capacityID).toArray

  val gridLayout = MainGridLayout.createLayout(4)
  val title = createTitleLabel()
  val capacityTitle = createCapacityTitle()
  val exportButton = createExportButton()
  val navigationButtons = createNavigationButtons()
  val tabFolder = createTabFolder()

  def saveToCSV(fileName: String) {
    
    val printWriter = new PrintWriter(new File(fileName))
    val dateFormatter = new SimpleDateFormat("yyyy-MM-dd")
    val timeFormatter = new SimpleDateFormat("HH:mm:ss")

    printWriter.println(s"No,BlockNo,CapacityID,Date,Time,Capacity,DX,LC")

    testingResult.zipWithIndex.foreach { case (data, index) =>
      val date = dateFormatter.format(data.timestamp)
      val time = timeFormatter.format(data.timestamp)
      printWriter.println(s"$index,$blockNo,${data.capacityID},$date,$time,${data.capacity},${data.dxValue},${data.leakCurrent}")
    }

    printWriter.close()
    val messageBox = new MessageBox(mainWindowShell, SWT.OK)
    messageBox.setText("已儲存")
    messageBox.setMessage("已儲存")
    messageBox.open()
    Program.launch(fileName)

  }

  def createExportButton() = {
    val button = new Button(this, SWT.PUSH)
    val layoutData = new GridData(SWT.END, SWT.FILL, true, false)
    layoutData.widthHint = 300
    button.setLayoutData(layoutData)
    button.setText("匯出 CSV 檔")
    button.addSelectionListener(new SelectionAdapter() {
      override def widgetSelected(e: SelectionEvent) {
        val fileDialog = new FileDialog(mainWindowShell, SWT.SAVE)
        fileDialog.setFilterExtensions(Array("*.csv"))
        val fileNameHolder = Option(fileDialog.open())
        fileNameHolder.foreach { fileName =>
          saveToCSV(fileName)
          println("存到：" + fileName)
        }
      }
    })
    button
  }

  /**
   *  建立折線圖的 Composite
   *
   *  @param      parent    上一層的 Composite
   *  @return               放三張折線圖的 Composite
   */
  def createChartComposite(parent: Composite) = {
    val composite = new Composite(parent, SWT.NONE)
    composite.setLayout(new GridLayout(3, true))

    val chart1 = new TestResultChart("電容值", capacityID, CapacityValueChart, testingResult)
    val chart1Composite = chart1.createChartComposite(composite)
    val chart1CompositeLayoutData = new GridData
    chart1CompositeLayoutData.horizontalAlignment = GridData.FILL
    chart1CompositeLayoutData.grabExcessHorizontalSpace = true
    chart1CompositeLayoutData.verticalAlignment = GridData.FILL
    chart1CompositeLayoutData.grabExcessVerticalSpace = true
    chart1Composite.setLayoutData(chart1CompositeLayoutData)

    val chart2 = new TestResultChart("損失角", capacityID,DXValueChart, testingResult)
    val chart2Composite = chart2.createChartComposite(composite)
    val chart2CompositeLayoutData = new GridData
    chart2CompositeLayoutData.horizontalAlignment = GridData.FILL
    chart2CompositeLayoutData.grabExcessHorizontalSpace = true
    chart2CompositeLayoutData.verticalAlignment = GridData.FILL
    chart2CompositeLayoutData.grabExcessVerticalSpace = true
    chart2Composite.setLayoutData(chart2CompositeLayoutData)

    val chart3 = new TestResultChart("漏電流", capacityID, LCValueChart, testingResult)
    val chart3Composite = chart3.createChartComposite(composite)
    val chart3CompositeLayoutData = new GridData
    chart3CompositeLayoutData.horizontalAlignment = GridData.FILL
    chart3CompositeLayoutData.grabExcessHorizontalSpace = true
    chart3CompositeLayoutData.verticalAlignment = GridData.FILL
    chart3CompositeLayoutData.grabExcessVerticalSpace = true
    chart3Composite.setLayoutData(chart2CompositeLayoutData)

    composite
  }

  /**
   *  建立顯示測試結果的表格元件
   *
   *  @param      parent    上一層的 Composite
   *  @return               放三張折線圖的 Composite
   */
  def createDataTable(parent: Composite) = {
    val dateFormatter = new SimpleDateFormat("yyyy-MM-dd")
    val timeFormatter = new SimpleDateFormat("HH:mm:ss")
    val dataTable = new Table(parent, SWT.BORDER|SWT.VIRTUAL)
    val tableColumns = Array(
      new TableColumn(dataTable, SWT.CENTER),   // 編號
      new TableColumn(dataTable, SWT.CENTER),   // 時間
      new TableColumn(dataTable, SWT.CENTER),   // 日期
      new TableColumn(dataTable, SWT.CENTER),   // 容量值
      new TableColumn(dataTable, SWT.CENTER),   // dx 值
      new TableColumn(dataTable, SWT.CENTER)    // 漏電流
    )

    tableColumns(0).setText("編號")
    tableColumns(1).setText("日期")
    tableColumns(2).setText("時間")
    tableColumns(3).setText("電容值")
    tableColumns(4).setText("DX 值")
    tableColumns(5).setText("漏電流")

    val white = getDisplay.getSystemColor(SWT.COLOR_WHITE)
    dataTable.setHeaderVisible(true)
    dataTable.setLinesVisible(true)
    dataTable.setItemCount(testingResult.size)
    dataTable.addListener(SWT.SetData, new Listener() {
      override def handleEvent(event: Event) {
        val item = event.item.asInstanceOf[TableItem]
        val index = dataTable.indexOf(item)
        val row = testingResult(index)
        val dateTime = new Date(row.timestamp)
        val leakCurrent = if (row.leakCurrent == -1) "尚未測定" else "%.02f".format(row.leakCurrent)
        item.setText(0, s"# $index")
        item.setText(1, dateFormatter.format(dateTime))
        item.setText(2, timeFormatter.format(dateTime))
        item.setText(3, "%.02f".format(row.capacity))
        item.setText(4, "%.02f".format(row.dxValue))
        item.setText(5, leakCurrent)
        item.setBackground(0, white)
        item.setBackground(1, white)
        item.setBackground(2, white)
        item.setBackground(3, white)
        item.setBackground(4, white)
        item.setBackground(5, white)
      }
    })

    (0 until tableColumns.size).foreach { i => 
      val column = dataTable.getColumn(i)
      column.pack()
      column.setWidth(column.getWidth + 150)
    }

    dataTable
  }

  /**
   *  建立標題的文字標籤
   *
   *  @return       標題的文字標籤
   */
  def createTitleLabel() = {
    val title = new Label(this, SWT.NONE)
    title.setText(s"區塊 $blockNo         料號：${orderInfo.partNo}")
    title
  }

  /**
   *  建立電容標編號標頭
   *  
   *  @return     電容編號標頭的文字標籤
   */
  def createCapacityTitle() = {
    val capacityTitle = new Label(this, SWT.NONE)
    val capacityTitleLayoutData = new GridData
    capacityTitleLayoutData.horizontalAlignment = GridData.CENTER
    capacityTitle.setLayoutData(capacityTitleLayoutData)
    capacityTitle.setText(s"電容 $capacityID")
    capacityTitle
  }

  /**
   *  建立導覽按鈕
   *
   *  @return     導覽按鈕
   */
  def createNavigationButtons() = {
    val navigationButtons = new NavigationButtons(this)
    val navigationButtonsLayoutData = new GridData
    navigationButtonsLayoutData.heightHint = 50
    navigationButtonsLayoutData.widthHint = 300
    navigationButtonsLayoutData.horizontalAlignment = GridData.END
    navigationButtonsLayoutData.grabExcessHorizontalSpace = false
    navigationButtons.setLayoutData(navigationButtonsLayoutData)
    navigationButtons
  }

  /**
   *  建立 TabFolder
   *
   *  @return     TabFolder 物件
   */
  def createTabFolder() = {
    val tabFolder = new TabFolder(this, SWT.NONE)
    val tabFolderLayoutData = new GridData(SWT.FILL, SWT.FILL, true, true)
    tabFolderLayoutData.horizontalSpan = 4
    tabFolder.setLayoutData(tabFolderLayoutData)
    tabFolder
  }
  
  /**
   *  設定 TabFolder 裡面的物件
   */
  def setupTabItems() {
    val chartComposite = createChartComposite(tabFolder)
    val dataTable = createDataTable(tabFolder)
    val tabItem1 = new TabItem(tabFolder, SWT.NONE)
    val tabItem2 = new TabItem(tabFolder, SWT.NONE)
    tabItem1.setText("測試數據")
    tabItem2.setText("折線圖")
    tabItem1.setControl(dataTable)
    tabItem2.setControl(chartComposite)
  }

  def init() {
    this.setLayout(gridLayout)
    setupTabItems()
  }

  init()
}
