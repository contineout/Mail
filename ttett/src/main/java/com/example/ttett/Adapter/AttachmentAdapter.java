package com.example.ttett.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.ttett.Entity.Attachment;
import com.example.ttett.R;
import com.example.ttett.util.SelectIcon;
import com.example.ttett.util.SizeTran;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class AttachmentAdapter extends RecyclerView.Adapter<AttachmentAdapter.FolderViewHolder> {

    private List<Attachment> mAttachments ;
    private Context mContext;
    private int OPEN_MESSAGE = 1;
    private int ATTACHMENT_FRAG = 2;
    private int display_flag;
    private String TAG = "AttachmentAdapter";

    public AttachmentAdapter(Context context, List<Attachment> Attachments){
        this.mContext = context;
        this.mAttachments = Attachments;
    }

    public void setOPEN_MESSAGE(){
        display_flag = OPEN_MESSAGE;
    }
    public void setATTACHMENT_FRAG(){
        display_flag = ATTACHMENT_FRAG;
    }



    @NonNull
    @Override
    public AttachmentAdapter.FolderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if(mContext == null){
            mContext = parent.getContext();
        }
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.attachment_rv_item,parent,false);
        return new FolderViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AttachmentAdapter.FolderViewHolder holder, int position) {
        Attachment attachment = mAttachments.get(position);
        SelectIcon selectIcon = new SelectIcon();
        SizeTran sizeTran = new SizeTran();
        holder.name.setText(attachment.getName());
        sizeTran.start(holder.size,attachment.getSize());
        if(display_flag == OPEN_MESSAGE){
            holder.save_attachment.setVisibility(View.VISIBLE);
            holder.date.setVisibility(View.GONE);
        }else if(display_flag == ATTACHMENT_FRAG){
            holder.date.setText(attachment.getSaveDate());
            holder.save_attachment.setVisibility(View.GONE);
        }

        selectIcon.Attachment(holder.icon,attachment.getType());
    }

    @Override
    public int getItemCount() {
        return mAttachments.size();
    }

    static class FolderViewHolder extends RecyclerView.ViewHolder{
        TextView name,size,date;
        ImageView icon;
        ImageView save_attachment;

        public FolderViewHolder(@NonNull View itemView) {
            super(itemView);

            name = itemView.findViewById(R.id.attachment_name);
            size = itemView.findViewById(R.id.attachment_size);
            date = itemView.findViewById(R.id.attachment_datetime);
            icon = itemView.findViewById(R.id.attachment_Iv);
            save_attachment = itemView.findViewById(R.id.save_attachment);
        }
    }
}
