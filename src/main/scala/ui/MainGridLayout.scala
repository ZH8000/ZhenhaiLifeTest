package tw.com.zhenhai.lifetest;

import org.eclipse.swt._
import org.eclipse.swt.widgets._
import org.eclipse.swt.layout._
import org.eclipse.swt.events._

/**
 *  用來建立各頁面裡的主要的 GridLayout 文件
 *
 *  關於 SWT 的 Layout 說明，請參閱 http://www.eclipse.org/articles/article.php?file=Article-Understanding-Layouts/index.html
 */
object MainGridLayout {

  def createLayout(numOfColumns: Int) = {
    val gridLayout = new GridLayout(numOfColumns, true)

    gridLayout.horizontalSpacing = 20
    gridLayout.verticalSpacing = 20
    // gridLayout.marginWidth = 200
    // gridLayout.marginHeight = 200
    gridLayout
  }

}

