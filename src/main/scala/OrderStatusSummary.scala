package tw.com.zhenhai.lifetest;

import org.eclipse.swt._
import org.eclipse.swt.widgets._
import org.eclipse.swt.layout._
import org.eclipse.swt.events._

class CapacityBlock(title: String, parent: Composite) extends Composite(parent, SWT.NONE) {

  class CapacityButton(title: String, parent: Composite) extends Composite(parent, SWT.NONE) {

    val gridLayout = new GridLayout(1, true)
    gridLayout.verticalSpacing = 10
    this.setLayout(gridLayout)

    val titleButton = createTitleButton(title)
    val hourLabel = createHourLabel()

    def createTitleButton(title: String) = {
      val button = new Button(this, SWT.PUSH)
      val buttonLayoutData = new GridData
      buttonLayoutData.horizontalAlignment = GridData.FILL
      buttonLayoutData.verticalAlignment = GridData.FILL
      buttonLayoutData.grabExcessHorizontalSpace = true
      buttonLayoutData.grabExcessVerticalSpace = true
      button.setLayoutData(buttonLayoutData)
      button.setText(title)
      button
    }

    def createHourLabel() = {
      val label = new Label(this, SWT.CENTER)
      val labelLayoutData = new GridData
      labelLayoutData.horizontalAlignment = GridData.CENTER
      labelLayoutData.verticalAlignment = GridData.BEGINNING
      labelLayoutData.grabExcessHorizontalSpace = true
      labelLayoutData.grabExcessVerticalSpace = false
      label.setLayoutData(labelLayoutData)
      label
    }

    def setRunningHours(hoursHolder: Option[Int] = None) {
      hoursHolder.foreach(hour => hourLabel.setText(s"Run $hour Hrs"))
    }
  }


  val groupFrame = new Group(this, SWT.SHADOW_ETCHED_IN)

  this.setLayout(new FillLayout)
  groupFrame.setLayout(new GridLayout(4, true))
  groupFrame.setText(title)

  def createButtonLayoutData = {
    val layoutData = new GridData
    layoutData.horizontalAlignment = GridData.FILL
    layoutData.grabExcessHorizontalSpace = true
    layoutData.verticalAlignment = GridData.FILL
    layoutData.grabExcessVerticalSpace = true
    layoutData
  }


  val button1 = new CapacityButton("Cap1", groupFrame)
  val button2 = new CapacityButton("Cap2", groupFrame)
  val button3 = new CapacityButton("Cap3", groupFrame)
  val button4 = new CapacityButton("Cap4", groupFrame)
  val button5 = new CapacityButton("Cap5", groupFrame)
  val button6 = new CapacityButton("Cap6", groupFrame)
  val button7 = new CapacityButton("Cap7", groupFrame)
  val button8 = new CapacityButton("Cap8", groupFrame)
  val button9 = new CapacityButton("Cap9", groupFrame)
  val button10 = new CapacityButton("Cap10", groupFrame)

  button1.setLayoutData(createButtonLayoutData)
  button2.setLayoutData(createButtonLayoutData)
  button3.setLayoutData(createButtonLayoutData)
  button4.setLayoutData(createButtonLayoutData)
  button5.setLayoutData(createButtonLayoutData)
  button6.setLayoutData(createButtonLayoutData)
  button7.setLayoutData(createButtonLayoutData)
  button8.setLayoutData(createButtonLayoutData)
  button9.setLayoutData(createButtonLayoutData)
  button10.setLayoutData(createButtonLayoutData)

  button3.setRunningHours(Some(667))
  button5.setRunningHours(Some(30))

}

object OrderStatusSummary {

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
    dateTitle.setText("2015-10-07")

    val navigationButtons = new NavigationButtons(shell)
    val navigationButtonsLayoutData = new GridData
    navigationButtonsLayoutData.heightHint = 50
    navigationButtonsLayoutData.widthHint = 300
    navigationButtonsLayoutData.horizontalAlignment = GridData.END
    navigationButtonsLayoutData.grabExcessHorizontalSpace = true
    navigationButtons.setLayoutData(navigationButtonsLayoutData)

    val loadBlock = new CapacityBlock("Load", shell)
    val loadBlockLayoutData = new GridData
    loadBlockLayoutData.horizontalAlignment = GridData.FILL
    loadBlockLayoutData.grabExcessHorizontalSpace = true
    loadBlockLayoutData.verticalAlignment = GridData.FILL
    loadBlockLayoutData.grabExcessVerticalSpace = true
    loadBlockLayoutData.horizontalSpan = 3
    loadBlock.setLayoutData(loadBlockLayoutData)

    val unloadBlock = new CapacityBlock("UnLoad", shell)
    val unloadBlockLayoutData = new GridData
    unloadBlockLayoutData.horizontalAlignment = GridData.FILL
    unloadBlockLayoutData.grabExcessHorizontalSpace = true
    unloadBlockLayoutData.verticalAlignment = GridData.FILL
    unloadBlockLayoutData.grabExcessVerticalSpace = true
    unloadBlockLayoutData.horizontalSpan = 3
    unloadBlock.setLayoutData(loadBlockLayoutData)

    shell.setMaximized(true)
    shell
  }
}
