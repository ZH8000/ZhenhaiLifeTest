package tw.com.zhenhai.lifetest

import org.eclipse.swt._
import org.eclipse.swt.widgets.{List => SWTList, _}
import org.eclipse.swt.layout._

/**
 *  用來附有標題的下拉示選單的類別
 *
 *  @param    title         標題
 *  @param    selection     下拉式選單的選項
 *  @param    parent        上一層的 Composite
 *  @param    equalWidth    標題與選單是否要平分空間
 */
class DropdownField[T](title: String, selection: List[T], 
                       parent: Composite, equalWidth: Boolean = false) extends Composite(parent, SWT.NONE) {

  val gridLayout = createGridLayout()
  val titleLabel = createTitleLabel()
  val combo = createCombo()

  /**
   *  建立 Layout 物件
   *
   *  @return   GridLayout 物件
   */
  def createGridLayout() = {
    val gridLayout = new GridLayout(2, equalWidth)
    gridLayout.verticalSpacing = 10
    gridLayout
  }

  /**
   *  建立標題文字標籤
   *
   *  @return     立標題文字標籤
   */
  def createTitleLabel() = {
    val titleLabel = new Label(this, SWT.END)
    titleLabel.setText(title)
    titleLabel
  }

  /**
   *  建立 Combo 的下拉式選單
   *
   *  @return     下拉式選單的物件
   */
  def createCombo() = {
    val combo = new Combo(this, SWT.DROP_DOWN|SWT.BORDER|SWT.READ_ONLY)
    combo.setLayoutData(new GridData(SWT.FILL, SWT.BEGINNING, true , false))
    combo
  }

  /**
   *  設定 Layout 並初始化 Combo 裡的選項
   */
  def init() {
    this.setLayout(gridLayout)
    selection.foreach { item => combo.add(item.toString) }
  }

  /**
   *  取消選擇
   */
  def deselectAll() {
    combo.deselectAll()
  }

  /**
   *  設定 Combo 裡顯示的文字
   *
   *  @param    text    要顯示的文字
   */
  def setText(text: String)  = combo.setText(text)

  /**
   *  取得 Combo 裡現在顯示的文字
   *
   *  @return           Combo 裡顯示的文字
   */
  def getText() = combo.getText()

  /**
   *  取得使用者選擇的選擇
   *
   *  @return           若使用者有選擇的選擇，則為 Some(選項)，若無選擇任何東西則為 None
   */
  def getSelection() = Option(combo.getSelectionIndex).filter(_ != -1)

  init()
}

