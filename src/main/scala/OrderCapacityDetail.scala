package tw.com.zhenhai.lifetest;

import org.eclipse.swt._
import org.eclipse.swt.widgets._
import org.eclipse.swt.layout._
import org.eclipse.swt.events._

class CapacityTestChart(title: String) {
  
  import org.jfree.data.xy.XYDataset
  import org.jfree.data.xy.XYSeries
  import org.jfree.data.xy.XYSeriesCollection
  import org.jfree.chart.ChartFactory
  import org.jfree.experimental.chart.swt.ChartComposite
  import org.jfree.chart.plot.PlotOrientation
  import java.awt.Color

  lazy val dataset = createDataSet
  lazy val chart = {
    val chart = ChartFactory.createXYLineChart(title, "Time", "Value", dataset, PlotOrientation.VERTICAL, false, false, false)
    chart.setBackgroundPaint(Color.LIGHT_GRAY)
    chart
  }

  def createDataSet = {
    val s1 = new XYSeries(title)

    for (index <- 0 until 30) {
      s1.add(index, (Math.random * 100).toInt)
    }

    val dataset = new XYSeriesCollection
    dataset.addSeries(s1)
    dataset
  }

  def createChartComposite(parent: Composite) = new ChartComposite(parent, SWT.NONE, chart, true)

}

object OrderCapacityDetail {

  

  def createWindow(parentShell: Shell) = {

    val shell = new Shell(parentShell.getDisplay, SWT.SHELL_TRIM| SWT.APPLICATION_MODAL)
    val gridLayout = new GridLayout(3, true)

    gridLayout.horizontalSpacing = 20
    gridLayout.verticalSpacing = 20
    gridLayout.marginWidth = 200
    gridLayout.marginHeight = 200

    shell.setLayout(gridLayout)

    val title = new Label(shell, SWT.NONE)
    title.setText("訂單編號：1001     區域：Area1")

    val dateTitle = new Label(shell, SWT.NONE)
    val dateTitleLayoutData = new GridData
    dateTitleLayoutData.horizontalAlignment = GridData.CENTER
    dateTitle.setLayoutData(dateTitleLayoutData)
    dateTitle.setText("Cap X")

    val navigationButtons = new NavigationButtons(shell)
    val navigationButtonsLayoutData = new GridData
    navigationButtonsLayoutData.heightHint = 50
    navigationButtonsLayoutData.widthHint = 300
    navigationButtonsLayoutData.horizontalAlignment = GridData.END
    navigationButtonsLayoutData.grabExcessHorizontalSpace = true
    navigationButtons.setLayoutData(navigationButtonsLayoutData)
  
    val chart1 = new CapacityTestChart("A")
    val chart1Composite = chart1.createChartComposite(shell)
    val chart1CompositeLayoutData = new GridData
    chart1CompositeLayoutData.horizontalAlignment = GridData.FILL
    chart1CompositeLayoutData.grabExcessHorizontalSpace = true
    chart1CompositeLayoutData.verticalAlignment = GridData.FILL
    chart1CompositeLayoutData.grabExcessVerticalSpace = true
    chart1Composite.setLayoutData(chart1CompositeLayoutData)

    val chart2 = new CapacityTestChart("B")
    val chart2Composite = chart2.createChartComposite(shell)
    val chart2CompositeLayoutData = new GridData
    chart2CompositeLayoutData.horizontalAlignment = GridData.FILL
    chart2CompositeLayoutData.grabExcessHorizontalSpace = true
    chart2CompositeLayoutData.verticalAlignment = GridData.FILL
    chart2CompositeLayoutData.grabExcessVerticalSpace = true
    chart2Composite.setLayoutData(chart2CompositeLayoutData)

    val chart3 = new CapacityTestChart("C")
    val chart3Composite = chart3.createChartComposite(shell)
    val chart3CompositeLayoutData = new GridData
    chart3CompositeLayoutData.horizontalAlignment = GridData.FILL
    chart3CompositeLayoutData.grabExcessHorizontalSpace = true
    chart3CompositeLayoutData.verticalAlignment = GridData.FILL
    chart3CompositeLayoutData.grabExcessVerticalSpace = true
    chart3Composite.setLayoutData(chart2CompositeLayoutData)


    shell.setMaximized(true)
    shell
  }
}
