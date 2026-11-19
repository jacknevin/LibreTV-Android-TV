package app.libretv.tv

import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.view.*
import android.view.inputmethod.InputMethodManager
import android.content.Context
import android.widget.*
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.LinearLayoutManager
import java.util.concurrent.Executors
import java.util.concurrent.Future

import app.libretv.tv.MainActivity.TextHolder

internal fun MainActivity.home(focus:Boolean=true){
  if(!ensureAccess())return
  val wasNav=navButtons.indexOf(currentFocus);reset("home")
  val history=store.history()
  val hero=history.firstOrNull()?.video ?: homeRows.firstOrNull()?.items?.firstOrNull()
  val image=ImageView(this).apply{scaleType=ImageView.ScaleType.CENTER_CROP;cropToPadding=true;background=TvUi.shape(TvUi.surface);clipToOutline=true};frame(image,170,200,560,24)
  if(hero!=null)Images.load(image,api.proxy(hero.cover))
  val shade=View(this).apply{background=GradientDrawable(GradientDrawable.Orientation.LEFT_RIGHT,intArrayOf(TvUi.bg,0xA009111C.toInt(),0x3009111C))};frame(shade,0,0,0,0)
  val lower=View(this).apply{background=GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM,intArrayOf(Color.TRANSPARENT,TvUi.bg))};frame(lower,0,0,0,0)
  frame(TvUi.label(this,"LIBRETV  /  大屏影院",14f,true).apply{setTextColor(TvUi.cyan)},450,24,32,42)
  val title=TvUi.label(this,hero?.title?:"好故事，在这里开始",30f,true).apply{maxLines=2;ellipsize=android.text.TextUtils.TruncateAt.END};frame(title,470,70,32,60)
  val sub=TvUi.label(this,if(history.isNotEmpty())"接着上次的故事，继续观看" else "发现精彩影片，享受大屏时光",16f).apply{setTextColor(TvUi.muted)};frame(sub,470,40,32,124)
  val action=TvUi.button(this,if(history.isNotEmpty())"继续观看" else "搜索影片",true){if(history.isNotEmpty())openDetail(history.first().video)else searchScreen()};action.textSize=20f;action.setTypeface(null,android.graphics.Typeface.BOLD);action.setCompoundDrawablesWithIntrinsicBounds(if(history.isNotEmpty())R.drawable.ic_play_arrow else R.drawable.ic_search,0,0,0);action.compoundDrawableTintList=android.content.res.ColorStateList.valueOf(Color.BLACK);frame(action,194,48,32,156);mainAction=action

  if(homeRows.isNotEmpty()){
    val scroll=ScrollView(this).apply{isFocusable=false;clipChildren=false}
    frame(scroll,-1,320,0,212);scrollRows=scroll
    val box=TvUi.vertical(this).apply{clipChildren=false}
    scroll.addView(box,FrameLayout.LayoutParams(-1,-2))
    homeRows.forEachIndexed{i,def->
      val block=TvUi.vertical(this).apply{clipChildren=false}
      block.addView(TvUi.label(this,def.title,19f,true),LinearLayout.LayoutParams(-1,d(30)))
      val list=posterRow(def.items,i){v,p->lastHomeRow=i;resultPosition=p;if(v.sourceKey.isBlank()){query=v.title;searchScreen();runSearch()}else openDetail(v)}
      block.addView(list,LinearLayout.LayoutParams(-1,d(148)))
      rowBlocks.add(block);homeRowViews.add(list)
      box.addView(block,LinearLayout.LayoutParams(-1,-2).apply{bottomMargin=d(10)})
    }
  }else{
    frame(TvUi.label(this,if(homeLoaded)"暂无推荐影片，可以使用搜索找片" else "正在连接影片服务…",17f),600,40,32,230)
  }

  action.setOnKeyListener{_,key,e->if(e.action!=KeyEvent.ACTION_DOWN)false else when(key){KeyEvent.KEYCODE_DPAD_LEFT->{navButtons[0].requestFocus();true};KeyEvent.KEYCODE_DPAD_DOWN->{focusHomeRow(0);true};KeyEvent.KEYCODE_DPAD_UP,KeyEvent.KEYCODE_DPAD_RIGHT->true;else->false}}
  if(wasNav>=0)navButtons[wasNav].requestFocus()else action.requestFocus();if(!homeLoaded&&homeRows.isEmpty()&&history.isEmpty())connect()
 }

internal fun MainActivity.dummyHeight()=360

internal fun MainActivity.posterRow(items:List<Video>,rowIndex:Int=-1,click:(Video,Int)->Unit):RecyclerView {
  val list=RecyclerView(this).apply{layoutManager=LinearLayoutManager(this@posterRow,RecyclerView.HORIZONTAL,false);clipToPadding=false;setPadding(d(6),d(7),d(20),d(5));clipChildren=false;itemAnimator=null;setHasFixedSize(true)}
  rowItems=items;val key="home#$rowIndex"
  list.adapter=PosterAdapter(items,api,
    {navButtons[section].requestFocus()},
    {if(rowIndex<=0)mainAction?.requestFocus() else focusHomeRow(rowIndex-1)},
    {v,p->focusMemory.remember(key,v.key,p);lastContent=list.findViewHolderForAdapterPosition(p)?.itemView;row=list;rowItems=items},
    store.history().filter{it.duration>0}.associate{it.video.key to (it.position*1000/it.duration).toInt().coerceIn(0,1000)},
    click,
    {if(rowIndex<homeRows.lastIndex)focusHomeRow(rowIndex+1)}
  );row=list;return list
 }

internal fun MainActivity.focusRow(position:Int=focusMemory.index(page,rowItems.map{it.key})){val list=row?:return;val p=position.coerceIn(0,((list.adapter?.itemCount?:1)-1).coerceAtLeast(0));list.scrollToPosition(p);list.post{list.findViewHolderForAdapterPosition(p)?.itemView?.requestFocus()}}
