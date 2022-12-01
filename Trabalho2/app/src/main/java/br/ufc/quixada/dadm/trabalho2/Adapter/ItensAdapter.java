package br.ufc.quixada.dadm.trabalho2.Adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import br.ufc.quixada.dadm.trabalho2.Model.ItensDeFeiraModel;
import br.ufc.quixada.dadm.trabalho2.R;

public class ItensAdapter extends RecyclerView.Adapter<ItensAdapter.ViewHolder> {

    List<ItensDeFeiraModel> listItens = new ArrayList<>();
    private Context context;

    public ItensAdapter(Context context) {
        this.context = context;
    }

    public void setItems(List<ItensDeFeiraModel> list){
        listItens.addAll(list);
    }

    public void clear(){
        listItens.clear();
    }

    @NonNull
    @Override
    public ItensAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.itens_de_feira, parent, false);
        return new ViewHolder(v);
    }

    @SuppressLint("DefaultLocale")
    @Override
    public void onBindViewHolder(@NonNull ItensAdapter.ViewHolder holder, int position) {
        ItensDeFeiraModel itensDeFeiraModel = listItens.get(position);

        holder.tvId.setText(String.valueOf(itensDeFeiraModel.getId()));
        holder.tvNome.setText(itensDeFeiraModel.getNome());
        holder.tvMarca.setText(itensDeFeiraModel.getMarca());
        holder.tvQuandidade.setText(String.valueOf(itensDeFeiraModel.getQuantidade()));
        holder.tvPreco.setText(String.format("%.2f",itensDeFeiraModel.getPreco()));
        holder.tvTotal.setText(String.format("%.2f",(itensDeFeiraModel.getQuantidade()*itensDeFeiraModel.getPreco())));
    }

    @Override
    public int getItemCount() {
        return listItens.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder{

        public TextView tvId;
        public TextView tvNome;
        public TextView tvMarca;
        public TextView tvQuandidade;
        public TextView tvPreco;
        public TextView tvTotal;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            tvId = itemView.findViewById(R.id.textViewId);
            tvNome = itemView.findViewById(R.id.textViewNome);
            tvMarca = itemView.findViewById(R.id.textViewMarca);
            tvQuandidade = itemView.findViewById(R.id.textViewQuantidade);
            tvPreco = itemView.findViewById(R.id.textViewPreco);
            tvTotal = itemView.findViewById(R.id.textViewTotal);
        }
    }
}
