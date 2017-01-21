package org.dync.livegiftlayout;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by KathLine on 2017/1/21.
 */

public class GiftMsgAdapter extends RecyclerView.Adapter<GiftMsgAdapter.ViewHolder> {
    private final String TAG = getClass().getSimpleName();
    private Context context;
    private ArrayList<String> mDatas;

    public GiftMsgAdapter(Context context) {
        this.context = context;
        mDatas = new ArrayList<>();
    }

    public void add(String item){
        mDatas.add(item);
        notifyDataSetChanged();
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_gift_msg, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        String item = mDatas.get(position);
        holder.tvGiftMsg.setText("送了礼物" + item);
        Log.e(TAG, "送了礼物" + item);
    }

    @Override
    public int getItemCount() {
        return mDatas.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder{

        private final TextView tvGiftMsg;

        public ViewHolder(View itemView) {
            super(itemView);
            tvGiftMsg = (TextView) itemView.findViewById(R.id.tv_gift_msg);
        }
    }
}
