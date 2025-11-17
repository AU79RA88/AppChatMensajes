package com.example.firebaseapp.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.firebaseapp.R;
import com.example.firebaseapp.model.Usuario;

import java.util.List;

public class UsuarioAdapter extends RecyclerView.Adapter<UsuarioAdapter.UsuarioViewHolder> {

    private final List<Usuario> lista;
    private final OnUsuarioClickListener listener;

    public interface OnUsuarioClickListener { void onUsuarioClick(Usuario usuario); }

    public UsuarioAdapter(List<Usuario> lista, OnUsuarioClickListener listener) {
        this.lista = lista;
        this.listener = listener;
    }

    @NonNull @Override
    public UsuarioViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_usuario, parent, false);
        return new UsuarioViewHolder(v);
    }

    @Override public void onBindViewHolder(@NonNull UsuarioViewHolder holder, int position) {
        Usuario u = lista.get(position);
        holder.nombre.setText(u.getNombre() != null ? u.getNombre() : "Desconocido");
        holder.correo.setText(u.getCorreo() != null ? u.getCorreo() : "");
        holder.itemView.setOnClickListener(v -> listener.onUsuarioClick(u));
    }

    @Override public int getItemCount() { return lista == null ? 0 : lista.size(); }

    static class UsuarioViewHolder extends RecyclerView.ViewHolder {
        TextView nombre, correo;
        UsuarioViewHolder(@NonNull View itemView) {
            super(itemView);
            nombre = itemView.findViewById(R.id.txtNombreUsuario);
            correo = itemView.findViewById(R.id.txtCorreoUsuario);
        }
    }
}
