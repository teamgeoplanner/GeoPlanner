package com.example.geoplanner;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class contactsAdapter extends RecyclerView.Adapter<contactsAdapter.ViewHolder> {

    private List<String> mData;
    private LayoutInflater mInflater;

    contactsAdapter(Context context, List<String> data) {
        this.mInflater = LayoutInflater.from(context);
        this.mData = data;
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {
        String cont = mData.get(position);
        holder.txtContact.setText(cont);

        holder.cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                mData.remove(holder.getAdapterPosition());
//                notifyDataSetChanged();
//                AutoMsgFragment.contactNo.remove(position);
//                System.out.println(AutoMsgFragment.contactNo);

                mData.remove(holder.getAdapterPosition());
                AutoMsgFragment.contactsNo.remove(holder.getAdapterPosition());
                notifyDataSetChanged();

                System.out.println("numbers:"+ AutoMsgFragment.contactsNo);
                System.out.println("names:"+ mData);
            }
        });
    }

    @Override
    public int getItemCount() {
        return mData.size();
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = mInflater.inflate(R.layout.layout_contact, parent, false);
        return new ViewHolder(view);
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView txtContact;
        ImageButton cancel;

        ViewHolder(View itemView) {
            super(itemView);
            txtContact = itemView.findViewById(R.id.txtContact);
            cancel = itemView.findViewById(R.id.btnCancel);

        }
    }


}
