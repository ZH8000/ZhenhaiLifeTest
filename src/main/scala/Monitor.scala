package tw.com.zhenhai.lifetest;

import org.eclipse.swt._
import org.eclipse.swt.widgets._
import org.eclipse.swt.layout._
import org.eclipse.swt.events._


class MonitorWindow(mainWindowShell: Shell) extends Composite(mainWindowShell, SWT.NONE) {

  class AreaInfo(parent: Composite, areaNumber: Int, hasInfo: Boolean) extends Composite(parent, SWT.NONE) {
  
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
      areaButton.setText(s"區塊 $areaNumber")
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
  
      val areaInfoFillLayout = new GridLayout(2, true)
  
      areaInfoFillLayout.marginWidth = 10
      areaInfoFillLayout.marginHeight = 10
  
      areaInfoGroup.setLayout(areaInfoFillLayout)
  
      val label1 = new Label(areaInfoGroup, SWT.NONE)
      val text1 = new Text(areaInfoGroup, SWT.BORDER)
      val label2 = new Label(areaInfoGroup, SWT.NONE)
      val text2 = new Text(areaInfoGroup, SWT.BORDER)
      val label3 = new Label(areaInfoGroup, SWT.NONE)
      val text3 = new Text(areaInfoGroup, SWT.BORDER)
      val label4 = new Label(areaInfoGroup, SWT.NONE)
      val text4 = new Text(areaInfoGroup, SWT.BORDER)

      val textLayoutData = new GridData
  
      text1.setLayoutData(new GridData(GridData.FILL, GridData.BEGINNING, true, false))
      text2.setLayoutData(new GridData(GridData.FILL, GridData.BEGINNING, true, false))
      text3.setLayoutData(new GridData(GridData.FILL, GridData.BEGINNING, true, false))
      text4.setLayoutData(new GridData(GridData.FILL, GridData.BEGINNING, true, false))

 
      label1.setText("料號：")
      label2.setText("良品：")
      label3.setText("測試時間：")
      label4.setText("狀態：")
  
      areaButton.addSelectionListener(new SelectionAdapter() {
        override def widgetSelected(e: SelectionEvent) {
          MainWindow.appendLog(s"點選「${areaButton.getText}」")
          MainWindow.pushComposite(new OrderStatusSummary(mainWindowShell))
        }
      })
  
      areaInfoGroup
    }
  }

  def init() {

    val gridLayout = new GridLayout(6, true)

    gridLayout.horizontalSpacing = 20
    gridLayout.verticalSpacing = 20
    gridLayout.marginWidth = 200
    gridLayout.marginHeight = 200

    this.setLayout(gridLayout)

    val navigationButtons = new NavigationButtons(this)
    val navigationButtonsLayoutData = new GridData
    navigationButtonsLayoutData.heightHint = 50
    navigationButtonsLayoutData.widthHint = 300
    navigationButtonsLayoutData.horizontalAlignment = GridData.END
    navigationButtonsLayoutData.horizontalSpan = 6

    navigationButtons.setLayoutData(navigationButtonsLayoutData)

    val areas = Array(
      new AreaInfo(this, 1, true), 
      new AreaInfo(this, 2, true),
      new AreaInfo(this, 3, false),
      new AreaInfo(this, 4, true),
      new AreaInfo(this, 5, false),
      new AreaInfo(this, 6, true),
      new AreaInfo(this, 7, true), 
      new AreaInfo(this, 8, true),
      new AreaInfo(this, 9, false),
      new AreaInfo(this, 10, true),
      new AreaInfo(this, 11, false),
      new AreaInfo(this, 12, true)
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
  }

  init()
}

