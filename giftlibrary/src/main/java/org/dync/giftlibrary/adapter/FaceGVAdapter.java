package org.dync.giftlibrary.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import org.dync.giftlibrary.R;
import org.dync.giftlibrary.widget.GiftModel;

import java.io.IOException;
import java.util.List;

public class FaceGVAdapter extends RecyclerView.Adapter<FaceGVAdapter.ViewHodler> {
    private List<GiftModel> list;
    private Context mContext;
    private boolean isNetData;

    private ViewHodler mHolder;
    private int clickTemp = -1;

    //标识选择的Item
    public void setSeclection(int position) {
        clickTemp = position;
    }

    public int getSecletion() {
        return clickTemp;
    }

    public FaceGVAdapter(List<GiftModel> list, Context mContext, boolean isNetData) {
        super();
        this.list = list;
        this.mContext = mContext;
        this.isNetData = isNetData;
    }

    public void clear() {
        this.mContext = null;
    }

    @Override
    public ViewHodler onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.face_image, parent, false);
        return new ViewHodler(view);
    }

    @Override
    public void onBindViewHolder(ViewHodler holder, final int position) {
        final GiftModel giftModel = list.get(position);
        if (isNetData){
            Glide.with(mContext).load(giftModel.getGiftPic()).placeholder(R.mipmap.loading).into(holder.iv);
            holder.tv.setText(giftModel.getGiftName());
        }else {
            try {
                Bitmap mBitmap = BitmapFactory.decodeStream(mContext.getAssets().open(giftModel.getGiftName()));
                holder.iv.setImageBitmap(mBitmap);
            } catch (IOException e) {
                e.printStackTrace();
            }
            holder.tv.setText(giftModel.getGiftName());
        }

        holder.llroot.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mOnItemClickListener != null) {
                    mOnItemClickListener.onItemClick(v, giftModel, position);
                }
            }
        });

        if (clickTemp == position) {
            holder.llroot.setBackgroundResource(R.drawable.shape_gift_chose);
            mHolder = holder;
        } else {
            holder.llroot.setBackgroundResource(R.drawable.shape_gift_tran);
        }
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public void clearSelection() {
        if (mHolder != null) {
            mHolder.llroot.setBackgroundResource(R.drawable.shape_gift_tran);
            mHolder = null;
        }
    }

    class ViewHodler extends RecyclerView.ViewHolder {
        LinearLayout llroot;
        ImageView iv;
        TextView tv;

        public ViewHodler(View view) {
            super(view);
            llroot = (LinearLayout) view.findViewById(R.id.ll_gift_root);
            iv = (ImageView) view.findViewById(R.id.face_img);
            tv = (TextView) view.findViewById(R.id.face_text);
        }
    }

    public interface OnItemClickListener {
        void onItemClick(View view, GiftModel giftModel, int position);
    }

    public OnItemClickListener mOnItemClickListener;

    public void setOnItemClickListener(OnItemClickListener listener) {
        mOnItemClickListener = listener;
    }
}
