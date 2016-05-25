package tw.com.zhenhai.lifetest

import java.awt.Color
import java.text.SimpleDateFormat
import java.util.Date
import org.eclipse.swt._
import org.eclipse.swt.events._
import org.eclipse.swt.layout._
import org.eclipse.swt.widgets.{List => SWTList, _}
import org.jfree.chart.axis.NumberAxis
import org.jfree.chart.axis.NumberTickUnit
import org.jfree.chart.ChartFactory
import org.jfree.chart.plot.PlotOrientation
import org.jfree.chart.plot.XYPlot
import org.jfree.data.time._
import org.jfree.data.xy.XYDataset
import org.jfree.data.xy.XYSeries
import org.jfree.data.xy.XYSeriesCollection
import org.jfree.experimental.chart.swt.ChartComposite
import zhenhai.lifetest.controller.model._

/**
 *  圖表類型
 */
sealed trait ChartType

/**
 *  電容值圖表
 */
object CapacityValueChart extends ChartType

/**
 *  損失角圖表
 */
object DXValueChart extends ChartType

/**
 *  漏電流圖表
 */
object LCValueChart extends ChartType

/**
 *  測試結果圖表
 *
 *  @param      tile            圖表標題
 *  @param      capacityID      電容編號
 *  @param      chartType       圖表類型
 *  @param      testingResult   測試結果
 */
class TestResultChart(title: String, capacityID: Int, chartType: ChartType, testingResult: Array[TestingResult]) {
  
  /**
   *  資料集
   */
  lazy val dataset = createDataSet

  /**
   *  jFreeChart 的圖表物件
   */
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

  /**
   *  建立資料集
   *
   *  @return       依照 chartType 決定的測試結果資料集
   */
  def createDataSet = {

    val series = new TimeSeries(title)
    for (row <- testingResult) {
      val timestamp = new Date(row.timestamp)
      val value = chartType match {
        case CapacityValueChart => row.capacity
        case DXValueChart => row.dxValue
        case LCValueChart => row.leakCurrent
      }

      series.add(new Second(timestamp) , row.capacity)
    }
    new TimeSeriesCollection(series)

  }

  /**
   *  建立 jFreeChart 的 SWT Widget
   *
   *  @param    parent      上一層的 Compsoite
   *  @return               jFreeChart 的 SWT Widget
   */
  def createChartComposite(parent: Composite) = {
    val composite = new ChartComposite(parent, SWT.NONE, chart, false, false, false, false, true)
    composite.setDomainZoomable(false)
    composite.setRangeZoomable(false)
    composite
  }

}

