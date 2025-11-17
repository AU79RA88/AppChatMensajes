package com.example.firebaseapp.adapter;

import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.example.firebaseapp.R;
import com.example.firebaseapp.model.Mensaje;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class MensajeAdapter extends ListAdapter<Mensaje, MensajeAdapter.VH> {

    public MensajeAdapter() { super(DIFF); }

    private static final DiffUtil.ItemCallback<Mensaje> DIFF = new DiffUtil.ItemCallback<Mensaje>() {
        @Override public boolean areItemsTheSame(@NonNull Mensaje o1, @NonNull Mensaje o2) {
            return o1.getTimestamp() == o2.getTimestamp() &&
                    ((o1.getDe() == null && o2.getDe() == null) || (o1.getDe() != null && o1.getDe().equals(o2.getDe())));
        }
        @Override public boolean areContentsTheSame(@NonNull Mensaje o1, @NonNull Mensaje o2) {
            String t1 = o1.getTexto() == null ? "" : o1.getTexto();
            String t2 = o2.getTexto() == null ? "" : o2.getTexto();
            String n1 = o1.getDeName() == null ? "" : o1.getDeName();
            String n2 = o2.getDeName() == null ? "" : o2.getDeName();
            return t1.equals(t2) && n1.equals(n2);
        }
    };

    @NonNull @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_mensaje, parent, false);
        return new VH(v);
    }

    @Override public void onBindViewHolder(@NonNull VH holder, int position) {
        Mensaje m = getItem(position);
        holder.texto.setText(m.getTexto() == null ? "" : m.getTexto());
        String displayName = m.getDeName() == null ? "Desconocido" : m.getDeName();
        holder.author.setText(displayName);

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        String myUid = user != null ? user.getUid() : null;
        boolean isMine = myUid != null && myUid.equals(m.getDe());

        if (holder.bubbleParent.getLayoutParams() instanceof LinearLayout.LayoutParams) {
            LinearLayout.LayoutParams lp = (LinearLayout.LayoutParams) holder.bubbleParent.getLayoutParams();
            lp.gravity = isMine ? Gravity.END : Gravity.START;
            holder.bubbleParent.setLayoutParams(lp);
            holder.bubbleParent.setBackgroundResource(isMine ? R.drawable.bubble_sent : R.drawable.bubble_gray);
        } else {
            holder.texto.setTextAlignment(isMine ? View.TEXT_ALIGNMENT_TEXT_END : View.TEXT_ALIGNMENT_TEXT_START);
        }
    }

    static class VH extends RecyclerView.ViewHolder {
        TextView texto;
        TextView author;
        View bubbleParent;
        VH(@NonNull View itemView) {
            super(itemView);
            texto = itemView.findViewById(R.id.txtMensaje);
            author = itemView.findViewById(R.id.txtMensajeAuthor);
            bubbleParent = itemView.findViewById(R.id.bubbleParent);
        }
    }
}
