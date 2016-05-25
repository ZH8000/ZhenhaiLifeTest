package tw.com.zhenhai.lifetest

import org.eclipse.swt._
import org.eclipse.swt.widgets._
import org.eclipse.swt.layout._

/**
 *  附有標題的文字輸入方塊
 *
 *  @param    title         標題
 *  @param    isReadOnly    是否與唯讀
 *  @param    isEqualWidth  標題與文字方塊是否要平分空間
 *  @param    parent        上一層的 Composite
 */
class TextEntryField(title: String, isReadOnly: Boolean, isEqualWidth: Boolean, parent: Composite) extends Composite(parent, SWT.NONE) {
  val gridLayout = createLayout()
  val titleLabel = createTitleLabel()
  val entry = createEntry()

  /**
   *  建立 GridLayout 物件
   *
   *  @return     GridLayout 物件
   */
  def createLayout() = {
    val gridLayout = new GridLayout(2, isEqualWidth)
    gridLayout.verticalSpacing = 10
    gridLayout
  }

  /**
   *  建立文字方塊
   *
   *  @return     文字方塊
   */
  def createEntry() = {
    val attributes = if (isReadOnly) {SWT.BORDER|SWT.READ_ONLY} else {SWT.BORDER}
    val entry = new Text(this, attributes)
    entry.setLayoutData(new GridData(SWT.FILL, SWT.BEGINNING, true , false))
    entry
  }

  /**
   *  建立標題的文字標籤
   *
   *  @return     文字標籤
   */
  def createTitleLabel() = {
    val titleLabel = new Label(this, SWT.NONE)
    titleLabel.setText(title)
    titleLabel
  }

  /**
   *  設定背景顏色
   */
  override def setBackground(color: org.eclipse.swt.graphics.Color) {
    entry.setBackground(color)
  }

  /**
   *  初始化
   */
  def init() {
    this.setLayout(gridLayout)
  }

  /**
   *  文字方塊是否為空的
   *
   *  @return       若為空的則為 true，否則為 false
   */
  def isEmpty() = entry.getText.trim.size == 0

  /**
   *  取得文字方塊的內容
   *
   *  @return       文字方塊裡的字串
   */
  def getText() = entry.getText

  /**
   *  設定文字方塊內的字串
   *
   *  @param    text  要填入文字方塊的字串
   */
  def setText(text: String) {
    entry.setText(text)
  }

  init()
}

