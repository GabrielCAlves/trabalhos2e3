package br.ufc.quixada.dadm.trabalho2.Util;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class UsuarioAutenticado {
    public static FirebaseUser usuarioLogado(){
        FirebaseAuth usuario = ConfiguraBD.FirebaseAutenticacao();
        return usuario.getCurrentUser();
    }
}
