package org.dync.giftlibrary.util;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.ImageSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import org.dync.giftlibrary.R;
import org.dync.giftlibrary.adapter.FaceGVAdapter;
import org.dync.giftlibrary.gif.AnimatedGifDrawable;
import org.dync.giftlibrary.gif.AnimatedImageSpan;
import org.dync.giftlibrary.widget.GiftModel;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ExpressionUtil {
    public static String ASSETS_ROOT = "p/";
    public static String ASSETS_GIF_ROOT = "g/";
    private boolean isNetData = false;//是否来自网络数据

    public ExpressionUtil(){

    }

    public ExpressionUtil(boolean isNetData){
        this.isNetData = isNetData;
    }

    /**
     * 从静态图找到对应的动态图，为了方便此处我动态图和静态图名称保持一致
     *
     * @param mContext
     * @param gifTextView
     * @param content
     * @return
     */
    public SpannableStringBuilder prase(Context mContext, final TextView gifTextView, String content) {
        SpannableStringBuilder sb = new SpannableStringBuilder(content);
        String regex = "\\[[^\\]]+\\]";
        Pattern p = Pattern.compile(regex);
        Matcher m = p.matcher(content);
        while (m.find()) {
            String tempText = m.group();
            try {
                String num = tempText.substring(("[" + ASSETS_ROOT).length(), tempText.length() - ".png]".length());
                String gif = ASSETS_GIF_ROOT + num + ".gif";
                /**
                 * 如果open这里不抛异常说明存在gif，则显示对应的gif
                 * 否则说明gif找不到，则显示png\\[[^\\]]+\\]
                 * */
                InputStream is = mContext.getAssets().open(gif);
                sb.setSpan(
                        new AnimatedImageSpan(
                                new AnimatedGifDrawable(is, new AnimatedGifDrawable.UpdateListener() {
                                    @Override
                                    public void update() {
                                        gifTextView.postInvalidate();
                                    }
                                })),
                        m.start(),
                        m.end(),
                        Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                is.close();
            } catch (Exception e) {//没找到对应的GIF，显示静态图
                String png = tempText.substring("[".length(), tempText.length() - "]".length());
                try {
                    sb.setSpan(
                            new ImageSpan(mContext, BitmapFactory.decodeStream(mContext.getAssets().open(png))),
                            m.start(),
                            m.end(),
                            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
                e.printStackTrace();
            }
        }
        return sb;
    }

    public SpannableStringBuilder getFace(Context mContext, String content) {
        SpannableStringBuilder sb = new SpannableStringBuilder();
        try {
            /**
             * 经过测试，虽然这里tempText被替换为png显示，但是但我单击发送按钮时，获取到輸入框的内容是tempText的值而不是png
             * 所以这里对这个tempText值做特殊处理
             * 格式：[face/png/f_static_000.png]，以方便判斷當前圖片是哪一個
             * */
            String tempText = "[" + content + "]";
            sb.append(tempText);
            sb.setSpan(
                    new ImageSpan(mContext, BitmapFactory.decodeStream(mContext.getAssets().open(content))),
                    sb.length() - tempText.length(),
                    sb.length(),
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

        } catch (Exception e) {
            e.printStackTrace();
        }

        return sb;
    }

    public void setView(Context mContext, final View view, String content) {
        if (view != null && view instanceof ImageView) {//图片不显示GIF
            Bitmap bitmap = null;
            try {
                bitmap = BitmapFactory.decodeStream(mContext.getAssets().open(content));
            } catch (IOException e1) {
                e1.printStackTrace();
            }
            ((ImageView) view).setImageBitmap(bitmap);
        } else if (view != null && view instanceof TextView) {//文字可显示GIF
            TextView gifTextView = (TextView) view;
            String tempText = "[" + content + "]";
            SpannableStringBuilder sb = prase(mContext, gifTextView, tempText);
            gifTextView.setText(sb);
        }

    }

    /**
     * 横屏时显示
     * @param context
     * @param recyclerView
     *@param staticGiftsList  @return
     */
    public void giftView(final Context context, RecyclerView recyclerView, List<GiftModel> staticGiftsList){
        LinearLayoutManager layoutManager = new LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false);
        recyclerView.setLayoutManager(layoutManager);

        final FaceGVAdapter mGvAdapter = new FaceGVAdapter(recyclerView, staticGiftsList, context, isNetData);
        recyclerView.setAdapter(mGvAdapter);

        // 单击表情执行的操作
        mGvAdapter.setOnItemClickListener(new FaceGVAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View view, GiftModel giftModel, int position) {
                try {
                    String giftPic = giftModel.getGiftPic();
                    String giftName = giftModel.getGiftName();
                    String giftPrice = giftModel.getGiftPrice();
//                    mGvAdapter.setSeclection(position);
//                    mGvAdapter.notifyDataSetChanged();
                    if (giftClickListener != null) {
                        giftClickListener.onClick(position, giftPic, giftName, giftPrice);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    /**
     * 竖屏时显示，每一页的数据
     *
     * @param context
     * @param position        第几页
     * @param staticGiftsList 表情集合
     * @param columns         列数
     * @param rows            行数
     * @param showView        View
     * @return
     */
    public View viewPagerItem(final Context context, int position, List<GiftModel> staticGiftsList, int columns, int rows, final View showView) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View layout = inflater.inflate(R.layout.face_gridview, null);//表情布局

        RecyclerView recyclerView = (RecyclerView) layout.findViewById(R.id.chart_face_gv);
        GridLayoutManager girdLayoutManager = new GridLayoutManager(context, columns);
        recyclerView.setLayoutManager(girdLayoutManager);

        List<GiftModel> subList = new ArrayList<>();
        subList.addAll(staticGiftsList
                .subList(position * (columns * rows - 0),
                        (columns * rows - 0) * (position + 1) > staticGiftsList
                                .size() ? staticGiftsList.size() : (columns * rows - 0)
                                * (position + 1)));

        final FaceGVAdapter mGvAdapter = new FaceGVAdapter(recyclerView, subList, context, isNetData);
        recyclerView.setAdapter(mGvAdapter);
        // 单击表情执行的操作
        mGvAdapter.setOnItemClickListener(new FaceGVAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View view, GiftModel giftModel, int position) {
                try {
                    String giftPic = giftModel.getGiftPic();
                    String pngStr = giftModel.getGiftName();
                    String giftPrice = giftModel.getGiftPrice();
                    setView(context, showView, pngStr);
//                    mGvAdapter.setSeclection(position);
//                    mGvAdapter.notifyDataSetChanged();
                    if (giftClickListener != null) {
                        giftClickListener.onClick(position, giftPic, pngStr, giftPrice);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        return recyclerView;
    }

    public interface GiftClickListener {
        void onClick(int position, String giftPic, String giftName, String giftPrice);
    }

    private GiftClickListener giftClickListener;

    public void setGiftClickListener(GiftClickListener listener) {
        giftClickListener = listener;
    }

    /**
     * 根据表情数量以及GridView设置的行数和列数计算Pager数量
     *
     * @return
     */
    public int getPagerCount(int listSize, int columns, int rows) {
        return listSize % (columns * rows - 0) == 0 ? listSize / (columns * rows - 0) : listSize / (columns * rows - 0) + 1;
    }

    /**
     * 初始化表情列表staticGiftsList
     */
    public List<GiftModel> initStaticGifts(Context context) {
        List<GiftModel> giftsList = null;
        try {
            giftsList = new ArrayList<>();
            GiftModel giftModel;
            String[] gifts = context.getAssets().list(ASSETS_ROOT.substring(0, ASSETS_ROOT.length() - 1));
            //将Assets中的表情名称转为字符串一一添加进staticGiftsList
            for (int i = 0; i < gifts.length; i++) {
                giftModel = new GiftModel(ASSETS_ROOT+gifts[i], "", i+1+"");
                giftsList.add(giftModel);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return giftsList;
    }

}
