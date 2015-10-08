package tw.com.zhenhai.lifetest;

import org.eclipse.swt._
import org.eclipse.swt.widgets._
import org.eclipse.swt.layout._
import org.eclipse.swt.events._

class AreaInfo(parent: Shell, areaNumber: Int, hasInfo: Boolean) extends Composite(parent, SWT.NONE) {

  val areaButton = createButton
  val areaInfoBox = createInfoBox
  val gridLayout = new GridLayout(1, true)

  this.setLayout(gridLayout)

  def createButton = {
    val areaButton = new Button(this, SWT.PUSH)
    val areaButtonLayoutData = new GridData

    areaButtonLayoutData.horizontalAlignment = GridData.FILL
    areaButtonLayoutData.verticalAlignment = GridData.BEGINNING
    areaButtonLayoutData.grabExcessHorizontalSpace = true
    areaButtonLayoutData.grabExcessVerticalSpace = false
    areaButtonLayoutData.heightHint = 50
    areaButton.setText(s"Area $areaNumber")
    areaButton.setLayoutData(areaButtonLayoutData)
    areaButton
  }

  def createInfoBox = {

    val areaInfoGroup = new Group(this, SWT.SHADOW_ETCHED_IN)
    val areaInfoLayoutData = new GridData

    areaInfoLayoutData.horizontalAlignment = GridData.FILL
    areaInfoLayoutData.verticalAlignment = GridData.FILL
    areaInfoLayoutData.grabExcessHorizontalSpace = true
    areaInfoLayoutData.grabExcessVerticalSpace = true
    areaInfoLayoutData.minimumHeight = 50
    areaInfoGroup.setLayoutData(areaInfoLayoutData)

    val areaInfoFillLayout = new GridLayout(1, true)

    areaInfoFillLayout.marginWidth = 10
    areaInfoFillLayout.marginHeight = 10

    areaInfoGroup.setLayout(areaInfoFillLayout)

    if (hasInfo) {
      val label1 = new Label(areaInfoGroup, SWT.NONE)
      val label2 = new Label(areaInfoGroup, SWT.NONE)
      val label3 = new Label(areaInfoGroup, SWT.NONE)
      val label4 = new Label(areaInfoGroup, SWT.NONE)

      label1.setText("單號：12345")
      label2.setText("Load: 10 / 10")
      label3.setText("Unload: 10 / 10")
      label4.setText("Duration: 2HR")

      areaButton.addSelectionListener(new SelectionAdapter() {
        override def widgetSelected(e: SelectionEvent) {
          val summaryWindow = OrderStatusSummary.createWindow(parent)
          summaryWindow.open()
        }
      })

    } else {
      areaButton.addSelectionListener(new SelectionAdapter() {
        override def widgetSelected(e: SelectionEvent) {
          val addTestPlainWindow = AddTestPlain.createWindow(parent)
          addTestPlainWindow.open()
        }
      })
    }
    
    areaInfoGroup
  }


}

object MonitorWindow {

  def createWindow(parentShell: Shell) = {

    val shell = new Shell(parentShell.getDisplay, SWT.SHELL_TRIM| SWT.APPLICATION_MODAL)
    val gridLayout = new GridLayout(4, true)

    gridLayout.horizontalSpacing = 20
    gridLayout.verticalSpacing = 20
    gridLayout.marginWidth = 200
    gridLayout.marginHeight = 200

    shell.setLayout(gridLayout)

    val titleLabel = new Label(shell, SWT.CENTER)
    val titleLabelLayoutData = new GridData

    titleLabel.setText("2015-10-10")
    titleLabelLayoutData.horizontalAlignment = GridData.CENTER
    titleLabelLayoutData.verticalAlignment = GridData.CENTER
    titleLabelLayoutData.horizontalSpan = 3
    titleLabel.setLayoutData(titleLabelLayoutData)

    val navigationButtons = new NavigationButtons(shell)
    val navigationButtonsLayoutData = new GridData
    navigationButtonsLayoutData.heightHint = 50
    navigationButtonsLayoutData.widthHint = 300
    navigationButtonsLayoutData.horizontalAlignment = GridData.END
    navigationButtons.setLayoutData(navigationButtonsLayoutData)

    val areas = Array(
      new AreaInfo(shell, 1, true), 
      new AreaInfo(shell, 2, true),
      new AreaInfo(shell, 3, false),
      new AreaInfo(shell, 4, true),
      new AreaInfo(shell, 5, false),
      new AreaInfo(shell, 6, true),
      new AreaInfo(shell, 7, true),
      new AreaInfo(shell, 8, false)
    )

    def createGridData = {
      val gridData = new GridData
      gridData.horizontalAlignment = GridData.FILL
      gridData.verticalAlignment = GridData.FILL
      gridData.grabExcessHorizontalSpace = true
      gridData.grabExcessVerticalSpace = true
      gridData
    } 

    areas.foreach(_.setLayoutData(createGridData))
    shell.setSize(shell.getDisplay.getBounds.width, shell.getDisplay.getBounds.height)
    shell
  }
}
