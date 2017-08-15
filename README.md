QQ交流群：611902811，有兴趣的可以交流  [博客地址](http://blog.csdn.net/DyncKathline/article/details/55682053)  
## [更新日志](https://github.com/DyncKathline/LiveGiftLayout/wiki)  
首先先上图   
![image](https://raw.githubusercontent.com/DyncKathline/LiveGiftLayout/173ee2616f8e17d7971d766120d992a3f2a3d829/screenshot/GIF.gif)  
### 个人建议使用Gift1Activity项目中的库，Gift2Activity项目中的库后面不怎么维护了  
### 1：到[GitHub](https://github.com/DyncKathline/LiveGiftLayout) 把项目clone到本地。  
### 2: 把giftlibrary库依赖到你的项目中去  
### 3：在你要显示的xml文件中添加展示礼物和礼物面板的地方 以项目中的activity_gift1.xml为例
```
<?xml version="1.0" encoding="utf-8"?>
<RelativeLayout xmlns:android="http://schemas.android.com/apk/res/android"
    android:layout_width="match_parent"
    android:layout_height="match_parent">

    <ImageView
        android:layout_width="match_parent"
        android:layout_height="match_parent"
        android:scaleType="centerCrop"
        android:src="@mipmap/ic_bg" />

    <LinearLayout
        android:id="@+id/ll_gift_parent"
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:layout_marginTop="100dp"
        android:orientation="vertical">
    </LinearLayout>

    <Button
        android:id="@+id/action"
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:layout_centerHorizontal="true"
        android:layout_marginTop="20dp"
        android:text="礼物面板显示/隐藏" />

    <LinearLayout
        android:id="@+id/bottom"
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:layout_alignParentBottom="true"
        android:orientation="vertical">

        <include layout="@layout/chat_tool_box" />

    </LinearLayout>

</RelativeLayout>

```
上面的GiftFrameLayout是展示礼物的控件，我这里仅展示两条，你可以添加多个礼物同时展示。同时礼物面板可以使用DialogFragment来替代我这里。
  
### 4：在activity中找到控件后就可以初始化礼物模块了。  
**a.礼物面板**。  

代码如下：  
这里如果想使用本地的礼物图片直接使用  
giftPanelControl.init(null);//这里如果为null则加载本地礼物图片

```
 GiftPanelControl giftPanelControl = new GiftPanelControl(this, mViewpager, mRecyclerView, mDotsLayout);
        giftPanelControl.init(giftModels);//这里如果为null则加载本地礼物图片
        giftPanelControl.setGiftListener(new GiftPanelControl.GiftListener() {
            @Override
            public void getGiftInfo(String giftPic, String giftName, String giftPrice) {
                mGifturl = giftPic;
                mGiftName = giftName;
                mGiftPrice = giftPrice;
            }
        });
```
这里的giftPic参数我传的是资源文件中图片的名称，你也可以传的是图片的id。  

**b.展示礼物**  

这里的setGiftLayout(false, giftParent, 3)方法，参数一：是否开启上面的礼物轨道消失后下面的礼物轨道会移上去模式，true是开启，false是关闭；  
参数二：礼物控件的父容器  
参数三：礼物轨道的数量。  
setCustormAnim(new CustormAnim())方法，对礼物的动画可以进行扩展，可以在不修改源码的情况下定制属于你的效果。
```
final LinearLayout giftParent = (LinearLayout) findViewById(R.id.ll_gift_parent);
giftControl = new GiftControl(this);
giftControl.setGiftLayout(false, giftParent, 3)
        .setCustormAnim(new CustormAnim());//这里可以自定义礼物动画
```
**c.显示礼物数量的面板**  

```
tvGiftNum.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                showGiftDialog();
            }
        });
```  
**d.礼物面板中发送按钮发送礼物的操作**  

如果你想要从某个礼物数开始连击，你可以在giftControl.loadGift(giftModel);前创建的GiftModel设置setCurrentStart(true)和setHitCombo(giftnum)，  
这样就可以实现了。  
温馨提示：这里的setCurrentStart()方法必须设置为true，setHitCombo()方法才能生效哦。  

```
btnGift.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (TextUtils.isEmpty(giftstr)) {
                    Toast.makeText(getApplication(), "你还没选择礼物呢", Toast.LENGTH_SHORT).show();
                } else {
                    String numStr = tvGiftNum.getText().toString();
                    if (!TextUtils.isEmpty(numStr)) {
                        int giftnum = Integer.parseInt(numStr);
                        if (giftnum == 0) {
                            return;
                        } else {
                            giftControl.loadGift(new GiftModel(giftstr, "安卓机器人", giftnum, "http://www.baidu.com", "123", "Lee123", "http://www.baidu.com"));
                        }
                    }
                }
            }
        });
```
**e.简单的操作了横竖屏显示不同的面板**  

```
findViewById(R.id.action).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (giftLayout.getVisibility() == View.VISIBLE) {
                    giftLayout.setVisibility(View.GONE);
                } else {
                    giftLayout.setVisibility(View.VISIBLE);
                }
            }
        });
```
**f.类似于切换直播间可以重新发送礼物（切换效果没做，功能实现了）**  

```
findViewById(R.id.btn_clear_gift).setOnClickListener(new View.OnClickListener() {
    @Override
    public void onClick(View view) {
        if (giftControl != null) {
            giftControl.cleanAll();
        }
    }
});
findViewById(R.id.btn_reset_gift).setOnClickListener(new View.OnClickListener() {
    @Override
    public void onClick(View view) {
        giftControl.reSetGiftLayout(false, giftParent, 3);
    }
});
```
**最后如果对你有用，请动动你的鼠标给个start或者fork吧，不胜感激**


