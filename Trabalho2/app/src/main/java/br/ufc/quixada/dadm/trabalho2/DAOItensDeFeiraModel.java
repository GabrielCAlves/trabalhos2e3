package br.ufc.quixada.dadm.trabalho2;

import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;

import java.util.HashMap;

import br.ufc.quixada.dadm.trabalho2.Model.ItensDeFeiraModel;

public class DAOItensDeFeiraModel {
    private DatabaseReference databaseReference;
    private String userId;

    public DAOItensDeFeiraModel(String userId){
        FirebaseDatabase db = FirebaseDatabase.getInstance();
        databaseReference = db.getReference(ItensDeFeiraModel.class.getSimpleName());
        this.userId = userId;
    }

    public Task<Void> add(ItensDeFeiraModel itensDeFeiraModel){
        itensDeFeiraModel.setUserId(userId);
        return databaseReference.push().setValue(itensDeFeiraModel);
    }

    public Task<Void> update(String key, HashMap<String, Object> hashMap){
        return databaseReference.child(key).updateChildren(hashMap);
    }

    public Task<Void> remove(String key){
        return databaseReference.child(key).removeValue();
    }

    public Query get(){
        return databaseReference.orderByChild("userId").equalTo(userId); //databaseReference.orderByKey();
    }
}
