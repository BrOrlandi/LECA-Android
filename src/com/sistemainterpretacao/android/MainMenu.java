package com.sistemainterpretacao.android;

import java.io.File;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
/**
 * Menu principal da aplicação.
 * @author Bruno Orlandi
 *
 */
public class MainMenu extends Activity implements OnClickListener{
	
	public final static String QUESTIONLIST_DIR = "QuestionLists"; ///< Diretório das listas de questões
	public final static String USERS_DIR = "Users"; ///< Diretório das listas salvas pelos usuários.
	public final static String ROOT_DIR = "LECA"; ///< Diretório raiz de armazenamento do conteúdo do LECA no SD Card.
	
	public static boolean mExternalStorageAvailable; ///< boolean para verificar se é possivel LER o SD Card.
	public static boolean mExternalStorageWriteable; ///< boolean para verificar se é possível ESCREVER no SD Card.
	
    /**
     * Instancia o Menu principal e seta os listeners para os botões.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        findViewById(R.id.bNew).setOnClickListener(this);
        findViewById(R.id.bLoad).setOnClickListener(this);
        findViewById(R.id.bExit).setOnClickListener(this);
        Log.d(this.getLocalClassName(), "onCreate");
    }

    /**
     * Um dos três botões foram clicados chama este método.
     */
	@Override
	public void onClick(View v) {
		File path;
		switch(v.getId())
		{
		case R.id.bNew:
	        Log.d(this.getLocalClassName(), "Abrir pasta QuestionList");
			path = getPath(MainMenu.this, QUESTIONLIST_DIR);
			if(path != null)
			{
				Intent i = new Intent(getApplicationContext(),ListFiles.class);
				i.putExtra("path",path.getPath());
				i.putExtra("appdir", QUESTIONLIST_DIR);
				startActivity(i);
			}
			break;
		case R.id.bLoad:
	        Log.d(this.getLocalClassName(), "Abrir pasta Users, para carregar.");
			path = getPath(MainMenu.this, USERS_DIR);
			if(path != null)
			{
				Intent i = new Intent(getApplicationContext(),ListFiles.class);
				i.putExtra("path",path.getPath());
				i.putExtra("appdir", USERS_DIR);
				startActivity(i);
			}
			break;
		case R.id.bExit:
	        Log.d(this.getLocalClassName(), "Botão Sair");
			finish();
			break;
		}
		
	}
    
	/**
	 * Função para acessar um diretório no SD Card, verifica se possui permissão de escrita e cria o diretório se não existir.
	 * @param context contexto da aplicação
	 * @param directory string para o diretório
	 * @return um File que representa o diretório.
	 */
	public static File getPath(Context context, String directory){
		File root = getRootPath(context);
		File dir = null;
		if(mExternalStorageWriteable)
		{
			//Directory
			dir = new File(root,directory);
			if(!dir.exists()) // if not exists
			{
				dir.mkdir();
			}
		}
		else
		{
			messageDialog(context, R.string.erro, R.string.nosdcard,null);
		}
		return dir;
	}
	
	/**
	 * Dá acesso ao diretório principal da aplicação, criando-o caso ainda não exista.
	 * @param context cotnexto da aplicação
	 * @return um File que representa o diretório.
	 */
	public static File getRootPath(Context context) {
		updateExternalStorageState();
		File dir = null;
		if(mExternalStorageWriteable)
		{
			File f = Environment.getExternalStorageDirectory();
			// Pasta principal do aplicativo no SD Card
			dir = new File(f,ROOT_DIR);
			if(!dir.exists()) // if not exists
			{
				dir.mkdir();
			}
		}
		else
		{
			messageDialog(context, R.string.erro, R.string.nosdcard,null);
		}
		return dir;
	}
	
	/**
	 * Verifica as permissões de Leitura e Escrita do SD Card.
	 */
	public static void updateExternalStorageState() {
	    String state = Environment.getExternalStorageState();
	    if (Environment.MEDIA_MOUNTED.equals(state)) {
	        mExternalStorageAvailable = mExternalStorageWriteable = true;
	    } else if (Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
	        mExternalStorageAvailable = true;
	        mExternalStorageWriteable = false;
	    } else {
	        mExternalStorageAvailable = mExternalStorageWriteable = false;
	    }
	}
	
	/**
	 * Método para criar uma caixa de diálogo com um botão de Ok.
	 * @param context contexto da aplicação.
	 * @param title Titulo da caixa de diálogo.
	 * @param message Mensagem da caixa de diálogo
	 * @param listener Listener para o botão "Ok" da caixa.
	 */
	public static void messageDialog(Context context, int title, String message, DialogInterface.OnClickListener listener){
        Log.d("MainMenu", "Chamada a caixa de Dialogo: "+message);
		String t = (String)context.getText(title);
		AlertDialog d = new AlertDialog.Builder(context).create();
		d.setTitle(t);
		d.setMessage(message);
		if(listener == null)
		{
			listener = new DialogInterface.OnClickListener() {
				
				@Override
				public void onClick(DialogInterface dialog, int which) {
					
				}
			};
		}
		d.setButton(AlertDialog.BUTTON_POSITIVE, (String)context.getText(android.R.string.ok), listener);
		d.show();
	}
	
	/**
	 * Método para criar uma caixa de diálogo com um botão de Ok.
	 * @param context contexto da aplicação.
	 * @param title Titulo da caixa de diálogo.
	 * @param message Mensagem da caixa de diálogo
	 * @param listener Listener para o botão "Ok" da caixa.
	 */
	public static void messageDialog(Context context, int title, int message, DialogInterface.OnClickListener listener){
		String m = (String)context.getText(message);
		messageDialog(context, title, m, listener);
	}
    
}