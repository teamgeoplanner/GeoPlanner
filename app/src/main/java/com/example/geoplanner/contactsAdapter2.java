package com.example.geoplanner;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class contactsAdapter2 extends RecyclerView.Adapter<contactsAdapter2.ViewHolder> {

    private List<String> mData;
    private LayoutInflater mInflater;

    contactsAdapter2(Context context, List<String> data) {
        this.mInflater = LayoutInflater.from(context);
        this.mData = data;
    }

    @Override
    public void onBindViewHolder(final contactsAdapter2.ViewHolder holder, final int position) {
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
                AutoMsgDetailFragment.contactsNo.remove(holder.getAdapterPosition());
                notifyDataSetChanged();

                System.out.println("numbers:"+ AutoMsgDetailFragment.contactsNo);
                System.out.println("names:"+ mData);
            }
        });
    }

    @Override
    public int getItemCount() {
        return mData.size();
    }

    @Override
    public contactsAdapter2.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = mInflater.inflate(R.layout.layout_contact, parent, false);
        return new contactsAdapter2.ViewHolder(view);
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
