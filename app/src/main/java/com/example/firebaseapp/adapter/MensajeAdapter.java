package com.example.firebaseapp.adapter;

import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.firebaseapp.R;
import com.example.firebaseapp.model.Mensaje;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class MensajeAdapter extends ListAdapter<Mensaje, MensajeAdapter.VH> {

    public MensajeAdapter() { super(DIFF); }

    private static final DiffUtil.ItemCallback<Mensaje> DIFF = new DiffUtil.ItemCallback<Mensaje>() {
        @Override
        public boolean areItemsTheSame(@NonNull Mensaje o1, @NonNull Mensaje o2) {
            return o1.getTimestamp() == o2.getTimestamp() &&
                    ((o1.getDe() == null && o2.getDe() == null) ||
                            (o1.getDe() != null && o1.getDe().equals(o2.getDe())));
        }

        @Override
        public boolean areContentsTheSame(@NonNull Mensaje o1, @NonNull Mensaje o2) {
            String t1 = o1.getTexto() == null ? "" : o1.getTexto();
            String t2 = o2.getTexto() == null ? "" : o2.getTexto();
            String n1 = o1.getDeName() == null ? "" : o1.getDeName();
            String n2 = o2.getDeName() == null ? "" : o2.getDeName();
            String i1 = o1.getImageUrl() == null ? "" : o1.getImageUrl();
            String i2 = o2.getImageUrl() == null ? "" : o2.getImageUrl();
            return t1.equals(t2) && n1.equals(n2) && i1.equals(i2);
        }
    };

    @NonNull @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_mensaje, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH holder, int position) {
        Mensaje m = getItem(position);


        if (m.getTexto() != null && !m.getTexto().isEmpty()) {
            holder.texto.setVisibility(View.VISIBLE);
            holder.texto.setText(m.getTexto());
        } else {
            holder.texto.setVisibility(View.GONE);
        }


        if (m.getImageUrl() != null && !m.getImageUrl().isEmpty()) {
            holder.msgImage.setVisibility(View.VISIBLE);
            Glide.with(holder.itemView.getContext())
                    .load(m.getImageUrl())
                    .into(holder.msgImage);
        } else {
            holder.msgImage.setVisibility(View.GONE);
        }


        String displayName = m.getDeName() == null ? "Desconocido" : m.getDeName();
        holder.author.setText(displayName);


        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm", Locale.getDefault());
        holder.msgTime.setText(sdf.format(new Date(m.getTimestamp())));


        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        String myUid = user != null ? user.getUid() : null;
        boolean isMine = myUid != null && myUid.equals(m.getDe());

        View parent = holder.bubbleParent;

        if (parent.getLayoutParams() instanceof LinearLayout.LayoutParams) {
            LinearLayout.LayoutParams lp = (LinearLayout.LayoutParams) parent.getLayoutParams();
            lp.gravity = isMine ? Gravity.END : Gravity.START;
            parent.setLayoutParams(lp);
            parent.setBackgroundResource(isMine ? R.drawable.bubble_sent : R.drawable.bubble_gray);
        }
    }

    static class VH extends RecyclerView.ViewHolder {
        TextView texto, author, msgTime;
        ImageView msgImage;
        View bubbleParent;

        VH(@NonNull View itemView) {
            super(itemView);
            texto = itemView.findViewById(R.id.txtMensaje);
            author = itemView.findViewById(R.id.txtMensajeAuthor);
            msgTime = itemView.findViewById(R.id.msgTime);
            msgImage = itemView.findViewById(R.id.msgImage);
            bubbleParent = itemView.findViewById(R.id.bubbleParent);
        }
    }
}
