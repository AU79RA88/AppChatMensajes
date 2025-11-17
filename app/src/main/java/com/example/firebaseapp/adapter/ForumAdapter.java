package com.example.firebaseapp.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.firebaseapp.R;
import com.example.firebaseapp.model.Post;

import java.util.List;

public class ForumAdapter extends RecyclerView.Adapter<ForumAdapter.ViewHolder> {

    private List<Post> posts;

    public ForumAdapter(List<Post> posts) { this.posts = posts; }

    public void update(List<Post> newData) {
        this.posts = newData;
        notifyDataSetChanged();
    }

    @NonNull @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_post, parent, false);
        return new ViewHolder(v);
    }

    @Override public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Post p = posts.get(position);
        holder.author.setText(p.getAuthorName() != null ? p.getAuthorName() : "Desconocido");
        holder.texto.setText(p.getTexto() != null ? p.getTexto() : "");
    }

    @Override public int getItemCount() { return posts == null ? 0 : posts.size(); }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView author, texto;
        ViewHolder(@NonNull View itemView) {
            super(itemView);
            author = itemView.findViewById(R.id.postAuthor);
            texto = itemView.findViewById(R.id.postText);
        }
    }
}
