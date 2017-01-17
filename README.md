首先先上图  
![image](https://github.com/DyncKathline/LiveGiftLayout/blob/master/screenshot/GIF.gif)  
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
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:layout_marginTop="100dp"
        android:orientation="vertical">

        <org.dync.giftlibrary.widget.GiftFrameLayout
            android:id="@+id/gift_layout1"
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"/>

        <org.dync.giftlibrary.widget.GiftFrameLayout
            android:id="@+id/gift_layout2"
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"/>
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
上面的GiftFrameLayout是展示礼物的控件，我这里仅展示两条，你可以添加多个礼物同时展示，但是你需要GiftControl类中相应的修改代码来实现。同时礼物面板可以使用DialogFragment来替代我这里。  
### 4：在activity中找到控件后就可以初始化礼物模块了。  
**a.礼物面板**。  
代码如下：  

```
GiftPanelControl giftPanelControl = new GiftPanelControl(this, mViewpager, mRecyclerView, mDotsLayout);
        giftPanelControl.setGiftListener(new GiftPanelControl.GiftListener() {
            @Override
            public void getGiftStr(String giftStr) {
                giftstr = giftStr;
            }
        });
```
这里的giftStr参数我传的是资源文件中图片的名称，你也可以传的是图片的id。  
**b.展示礼物**  

```
giftControl = new GiftControl(Gift1Activity.this);
        giftControl.setGiftLayout(giftFrameLayout1, giftFrameLayout2);
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
**最后如果对你有用，请动动你的鼠标给个start或者fork吧，不胜感激**


