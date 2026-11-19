package app.libretv.tv

import org.junit.Assert.*
import org.junit.Test

class HomeCatalogTest {
  private fun video(id:String)=Video(id,"片名-$id","","","","","")

  @Test fun visibleRows_dropsEmptyRows() {
    val rows=listOf(
      HotRow("有内容",listOf(video("1"))),
      HotRow("空榜单",emptyList()),
      HotRow("有内容2",listOf(video("2"),video("3")))
    )
    val visible=Core.visibleRows(rows)
    assertEquals(2,visible.size)
    assertEquals("有内容",visible[0].title)
    assertEquals("有内容2",visible[1].title)
  }

  @Test fun visibleRows_allEmpty() {
    assertTrue(Core.visibleRows(listOf(HotRow("空",emptyList()))).isEmpty())
  }

  @Test fun rowStep_clampsAtBoundaries() {
    assertEquals(-1,Core.rowStep(0,0,1))
    assertEquals(0,Core.rowStep(0,3,-1))
    assertEquals(2,Core.rowStep(2,3,1))
    assertEquals(1,Core.rowStep(0,3,1))
    assertEquals(1,Core.rowStep(2,3,-1))
  }
}
