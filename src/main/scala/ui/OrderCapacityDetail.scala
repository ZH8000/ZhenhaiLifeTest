package tw.com.zhenhai.lifetest;

import zhenhai.lifetest.controller.model._
import org.eclipse.swt._
import org.eclipse.swt.widgets._
import org.eclipse.swt.layout._
import org.eclipse.swt.events._
import java.util.Date

trait ChartType
object CapacityValueChart extends ChartType
object DXValueChart extends ChartType
object LCValueChart extends ChartType

class CapacityTestChart(title: String, testingID: Long, capacityID: Int, chartType: ChartType) {
  
  import org.jfree.data.xy.XYDataset
  import org.jfree.data.xy.XYSeries
  import org.jfree.data.xy.XYSeriesCollection
  import org.jfree.data.time._
  import org.jfree.chart.ChartFactory
  import org.jfree.experimental.chart.swt.ChartComposite
  import org.jfree.chart.plot.PlotOrientation
  import java.awt.Color
  import org.jfree.chart.axis.NumberAxis
  import org.jfree.chart.axis.NumberTickUnit

  lazy val testingResult = TestSetting.db.getAllTestingResult(testingID, capacityID)
  lazy val dataset = createDataSet
  lazy val chart = {
    val chart = ChartFactory.createTimeSeriesChart(title, "時間", title, dataset)
    val rangeAxis = chart.getXYPlot.getRangeAxis.asInstanceOf[NumberAxis]
    rangeAxis.setUpperBound(rangeAxis.getUpperBound + 2)
    rangeAxis.setLowerBound(rangeAxis.getLowerBound - 2)
    rangeAxis.setTickUnit(new NumberTickUnit(0.5))
    chart.setBackgroundPaint(Color.LIGHT_GRAY)
    chart
  }

  def createDXValueDataset = {
    val series = new TimeSeries(title)
    for (row <- testingResult) {
      val timestamp = new Date(row.timestamp)
      series.add(new Minute(timestamp) , row.dxValue)
    }
    new TimeSeriesCollection(series)
  }

  def createCapacityValueDataset = {
    val series = new TimeSeries(title)
    for (row <- testingResult) {
      val timestamp = new Date(row.timestamp)
      series.add(new Minute(timestamp) , row.capacity)
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

  def createChartComposite(parent: Composite) = new ChartComposite(parent, SWT.NONE, chart, true)

}

class OrderCapacityDetail(blockNo: Int, orderInfo: TestingOrder, capacityID: Int, mainWindowShell: Shell) extends Composite(mainWindowShell, SWT.NONE) {

  def init() {
    val gridLayout = new GridLayout(3, true)

    gridLayout.horizontalSpacing = 20
    gridLayout.verticalSpacing = 20
    gridLayout.marginWidth = 200
    gridLayout.marginHeight = 200

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
  
    val chart1 = new CapacityTestChart("電容值", orderInfo.id, capacityID, CapacityValueChart)
    val chart1Composite = chart1.createChartComposite(this)
    val chart1CompositeLayoutData = new GridData
    chart1CompositeLayoutData.horizontalAlignment = GridData.FILL
    chart1CompositeLayoutData.grabExcessHorizontalSpace = true
    chart1CompositeLayoutData.verticalAlignment = GridData.FILL
    chart1CompositeLayoutData.grabExcessVerticalSpace = true
    chart1Composite.setLayoutData(chart1CompositeLayoutData)

    val chart2 = new CapacityTestChart("損失角", orderInfo.id, capacityID,DXValueChart)
    val chart2Composite = chart2.createChartComposite(this)
    val chart2CompositeLayoutData = new GridData
    chart2CompositeLayoutData.horizontalAlignment = GridData.FILL
    chart2CompositeLayoutData.grabExcessHorizontalSpace = true
    chart2CompositeLayoutData.verticalAlignment = GridData.FILL
    chart2CompositeLayoutData.grabExcessVerticalSpace = true
    chart2Composite.setLayoutData(chart2CompositeLayoutData)

    val chart3 = new CapacityTestChart("漏電流", orderInfo.id, capacityID, LCValueChart)
    val chart3Composite = chart3.createChartComposite(this)
    val chart3CompositeLayoutData = new GridData
    chart3CompositeLayoutData.horizontalAlignment = GridData.FILL
    chart3CompositeLayoutData.grabExcessHorizontalSpace = true
    chart3CompositeLayoutData.verticalAlignment = GridData.FILL
    chart3CompositeLayoutData.grabExcessVerticalSpace = true
    chart3Composite.setLayoutData(chart2CompositeLayoutData)
  }

  init()
}
