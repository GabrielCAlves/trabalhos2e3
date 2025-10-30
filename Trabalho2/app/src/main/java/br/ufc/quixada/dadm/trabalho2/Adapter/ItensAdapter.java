package br.ufc.quixada.dadm.trabalho2.Adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

import br.ufc.quixada.dadm.trabalho2.Model.ItensDeFeiraModel;
import br.ufc.quixada.dadm.trabalho2.R;

public class ItensAdapter extends RecyclerView.Adapter<ItensAdapter.ViewHolder> {

    static List<ItensDeFeiraModel> listItens = new ArrayList<>();
    private static OnItemClickListener itemClickListener;
    private Context context;

    public ItensAdapter(Context context) {
        this.context = context;
    }

    public void setItems(List<ItensDeFeiraModel> list) {
        listItens.addAll(list);
    }

    public void clear() {
        listItens.clear();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.itens_de_feira, parent, false);
        return new ViewHolder(v);
    }

    @SuppressLint("DefaultLocale")
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ItensDeFeiraModel item = listItens.get(position);

        // Configura os textos
        holder.tvId.setText(String.valueOf(item.getId()));
        holder.tvNome.setText(item.getNome());
        holder.tvMarca.setText(item.getMarca());
        holder.tvQuandidade.setText(String.valueOf(item.getQuantidade()));
        holder.tvPreco.setText(String.format("%.2f", item.getPreco()));
        holder.tvDesconto.setText(String.format("%.2f", item.getDesconto()));

        // CARREGA A IMAGEM DO IMGBB (URL)
        if (item.getImagemUri() != null && !item.getImagemUri().isEmpty()) {
            // Usa Picasso para carregar a imagem da URL do ImgBB
            Picasso.get()
                    .load(item.getImagemUri())
                    .placeholder(R.drawable.ic_img_profile) // Imagem enquanto carrega
                    .error(R.drawable.ic_img_profile) // Imagem se der erro
                    .resize(120, 120) // Redimensiona para otimização
                    .centerCrop()
                    .into(holder.ivImagem);

            Log.d("ItensAdapter", "Carregando imagem: " + item.getImagemUri());
        } else {
            // Se não há imagem, usa a padrão
            holder.ivImagem.setImageResource(R.drawable.ic_img_profile);
        }

        // Cálculo do total
        if (item.getDesconto() == 0) {
            holder.tvTotal.setText(String.format("%.2f", (item.getQuantidade() * item.getPreco())));
        } else {
            holder.tvTotal.setText(String.format("%.2f", (item.getQuantidade() * item.getPreco() -
                    (item.getPreco() * item.getQuantidade() * (item.getDesconto() / 100)))));
        }
    }

    @Override
    public int getItemCount() {
        return listItens.size();
    }

    // Interface para o clique
    public interface OnItemClickListener {
        void onItemClick(ItensDeFeiraModel item);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.itemClickListener = listener;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView tvId;
        public ImageView ivImagem;
        public TextView tvNome;
        public TextView tvMarca;
        public TextView tvQuandidade;
        public TextView tvPreco;
        public TextView tvDesconto;
        public TextView tvTotal;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvId = itemView.findViewById(R.id.textViewId);
            ivImagem = itemView.findViewById(R.id.image_item);
            tvNome = itemView.findViewById(R.id.textViewNome);
            tvMarca = itemView.findViewById(R.id.textViewMarca);
            tvQuandidade = itemView.findViewById(R.id.textViewQuantidade);
            tvPreco = itemView.findViewById(R.id.textViewPreco);
            tvDesconto = itemView.findViewById(R.id.textViewDesconto);
            tvTotal = itemView.findViewById(R.id.textViewTotal);

            // Torna o item clicável
            itemView.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION && itemClickListener != null) {
                    itemClickListener.onItemClick(listItens.get(position));
                }
            });
        }
    }
}