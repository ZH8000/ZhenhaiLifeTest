package tw.com.zhenhai.lifetest;

import zhenhai.lifetest.controller.model._
import org.eclipse.swt._
import org.eclipse.swt.widgets.{List => SWTList, _}
import org.eclipse.swt.layout._
import org.eclipse.swt.events._
import java.util.Date
import java.text.SimpleDateFormat

trait ChartType
object CapacityValueChart extends ChartType
object DXValueChart extends ChartType
object LCValueChart extends ChartType

class CapacityTestChart(title: String, capacityID: Int, chartType: ChartType, testingResult: Array[TestingResult]) {
  
  import org.jfree.data.xy.XYDataset
  import org.jfree.data.xy.XYSeries
  import org.jfree.data.xy.XYSeriesCollection
  import org.jfree.data.time._
  import org.jfree.chart.ChartFactory
  import org.jfree.experimental.chart.swt.ChartComposite
  import org.jfree.chart.plot.PlotOrientation
  import org.jfree.chart.plot.XYPlot
  import java.awt.Color
  import org.jfree.chart.axis.NumberAxis
  import org.jfree.chart.axis.NumberTickUnit

  lazy val dataset = createDataSet
  lazy val chart = {
    val chart = ChartFactory.createTimeSeriesChart(title, "時間", title, dataset)
    val rangeAxis = chart.getXYPlot.getRangeAxis.asInstanceOf[NumberAxis]
    rangeAxis.setUpperBound(rangeAxis.getUpperBound + 2)
    rangeAxis.setLowerBound(rangeAxis.getLowerBound - 2)
    rangeAxis.setTickUnit(new NumberTickUnit(0.5))
    val plot = chart.getPlot.asInstanceOf[XYPlot]
    val renderer = plot.getRenderer.asInstanceOf[org.jfree.chart.renderer.xy.XYLineAndShapeRenderer]
    renderer.setSeriesLinesVisible(0, true)
    renderer.setSeriesShapesVisible(0, true)
    chart.setBackgroundPaint(Color.LIGHT_GRAY)
    chart
  }

  def createDXValueDataset = {
    val series = new TimeSeries(title)
    for (row <- testingResult) {
      val timestamp = new Date(row.timestamp)
      series.add(new Second(timestamp) , row.dxValue)
    }
    new TimeSeriesCollection(series)
  }

  def createCapacityValueDataset = {
    val series = new TimeSeries(title)
    for (row <- testingResult) {
      val timestamp = new Date(row.timestamp)
      series.add(new Second(timestamp) , row.capacity)
    }
    new TimeSeriesCollection(series)
  }


  def createDataSet = {

    chartType match {
      case CapacityValueChart => createCapacityValueDataset
      case DXValueChart => createDXValueDataset
      case LCValueChart => new XYSeriesCollection
    }
  }

  def createChartComposite(parent: Composite) = {
    val composite = new ChartComposite(parent, SWT.NONE, chart, false, false, false, false, true)
    composite.setDomainZoomable(false)
    composite.setRangeZoomable(false)
    composite
  }

}

class OrderCapacityDetail(blockNo: Int, orderInfo: TestingOrder, capacityID: Int, mainWindowShell: Shell) extends Composite(mainWindowShell, SWT.NONE) {

  lazy val testingResult = TestSetting.db.getAllTestingResult(orderInfo.id, capacityID).toArray

  def createChartComposite(parent: Composite) = {
    val composite = new Composite(parent, SWT.NONE)
    composite.setLayout(new GridLayout(3, true))

    val chart1 = new CapacityTestChart("電容值", capacityID, CapacityValueChart, testingResult)
    val chart1Composite = chart1.createChartComposite(composite)
    val chart1CompositeLayoutData = new GridData
    chart1CompositeLayoutData.horizontalAlignment = GridData.FILL
    chart1CompositeLayoutData.grabExcessHorizontalSpace = true
    chart1CompositeLayoutData.verticalAlignment = GridData.FILL
    chart1CompositeLayoutData.grabExcessVerticalSpace = true
    chart1Composite.setLayoutData(chart1CompositeLayoutData)

    val chart2 = new CapacityTestChart("損失角", capacityID,DXValueChart, testingResult)
    val chart2Composite = chart2.createChartComposite(composite)
    val chart2CompositeLayoutData = new GridData
    chart2CompositeLayoutData.horizontalAlignment = GridData.FILL
    chart2CompositeLayoutData.grabExcessHorizontalSpace = true
    chart2CompositeLayoutData.verticalAlignment = GridData.FILL
    chart2CompositeLayoutData.grabExcessVerticalSpace = true
    chart2Composite.setLayoutData(chart2CompositeLayoutData)

    val chart3 = new CapacityTestChart("漏電流", capacityID, LCValueChart, testingResult)
    val chart3Composite = chart3.createChartComposite(composite)
    val chart3CompositeLayoutData = new GridData
    chart3CompositeLayoutData.horizontalAlignment = GridData.FILL
    chart3CompositeLayoutData.grabExcessHorizontalSpace = true
    chart3CompositeLayoutData.verticalAlignment = GridData.FILL
    chart3CompositeLayoutData.grabExcessVerticalSpace = true
    chart3Composite.setLayoutData(chart2CompositeLayoutData)

    composite
  }

  def createDataTable(parent: Composite) = {
    val dateFormatter = new SimpleDateFormat("yyyy-MM-dd")
    val timeFormatter = new SimpleDateFormat("HH:mm:ss")
    val dataTable = new Table(parent, SWT.BORDER|SWT.VIRTUAL)
    val tableColumns = Array(
      new TableColumn(dataTable, SWT.CENTER),   // 編號
      new TableColumn(dataTable, SWT.CENTER),   // 時間
      new TableColumn(dataTable, SWT.CENTER),   // 日期
      new TableColumn(dataTable, SWT.CENTER),   // 容量值
      new TableColumn(dataTable, SWT.CENTER)    // dx 值
    )

    tableColumns(0).setText("編號")
    tableColumns(1).setText("日期")
    tableColumns(2).setText("時間")
    tableColumns(3).setText("電容值")
    tableColumns(4).setText("DX 值")

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
        item.setText(0, s"# $index")
        item.setText(1, dateFormatter.format(dateTime))
        item.setText(2, timeFormatter.format(dateTime))
        item.setText(3, "%.02f".format(row.capacity))
        item.setText(4, "%.02f".format(row.dxValue))
        item.setBackground(0, white)
        item.setBackground(1, white)
        item.setBackground(2, white)
        item.setBackground(3, white)
        item.setBackground(4, white)
      }
    })

    (0 until tableColumns.size).foreach { i => 
      val column = dataTable.getColumn(i)
      column.pack()
      column.setWidth(column.getWidth + 150)
    }


    dataTable
  }

  def init() {
    val gridLayout = new GridLayout(3, true)

    gridLayout.horizontalSpacing = 20
    gridLayout.verticalSpacing = 20
    // gridLayout.marginWidth = 200
    // gridLayout.marginHeight = 200

    this.setLayout(gridLayout)

    val title = new Label(this, SWT.NONE)
    title.setText(s"區塊 $blockNo         料號：${orderInfo.partNo}")

    val dateTitle = new Label(this, SWT.NONE)
    val dateTitleLayoutData = new GridData
    dateTitleLayoutData.horizontalAlignment = GridData.CENTER
    dateTitle.setLayoutData(dateTitleLayoutData)
    dateTitle.setText(s"電容 $capacityID")

    val navigationButtons = new NavigationButtons(this)
    val navigationButtonsLayoutData = new GridData
    navigationButtonsLayoutData.heightHint = 50
    navigationButtonsLayoutData.widthHint = 300
    navigationButtonsLayoutData.horizontalAlignment = GridData.END
    navigationButtonsLayoutData.grabExcessHorizontalSpace = true
    navigationButtons.setLayoutData(navigationButtonsLayoutData)

    val tabFolder = new TabFolder(this, SWT.NONE)
    val tabFolderLayoutData = new GridData(SWT.FILL, SWT.FILL, true, true)
    tabFolderLayoutData.horizontalSpan = 3
    tabFolder.setLayoutData(tabFolderLayoutData)


    val tabItem1 = new TabItem(tabFolder, SWT.NONE)
    val tabItem2 = new TabItem(tabFolder, SWT.NONE)
    val chartComposite = createChartComposite(tabFolder)
    val dataTable = createDataTable(tabFolder)

    tabItem1.setText("測試數據")
    tabItem2.setText("折線圖")
    tabItem1.setControl(dataTable)
    tabItem2.setControl(chartComposite)
  }

  init()
}
