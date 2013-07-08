package com.sistemainterpretacao.android;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;

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
//		if(mExternalStorageWriteable)
//		{
			//Directory
			dir = new File(root,directory);
			if(!dir.exists()) // if not exists
			{
				dir.mkdir();
			}
//		}
//		else
//		{
//			messageDialog(context, R.string.erro, R.string.nosdcard,null);
//		}
		return dir;
		
	}
	
	/**
	 * Dá acesso ao diretório principal da aplicação, criando-o caso ainda não exista.
	 * @param context cotnexto da aplicação
	 * @return um File que representa o diretório.
	 */
	public static File getRootPath(Context context) {
//		updateExternalStorageState();
		File dir = null;
//		if(mExternalStorageWriteable)
//		{
//			File f = Environment.getExternalStorageDirectory();
			File f = context.getFilesDir();
			// Pasta principal do aplicativo no SD Card
			dir = new File(f,ROOT_DIR);
			if(!dir.exists()) // if not exists
			{
				dir = context.getDir(ROOT_DIR, Context.MODE_PRIVATE);
				//dir.mkdir();
				File dirQ = new File(dir,QUESTIONLIST_DIR);
				dirQ.mkdir();
				try {
					InputStream is = context.getAssets().open("ListaC.xml");
					String read = readInputStreamFile(is);
					File lista = new File(dirQ, "ListaC.xml");
					writeFile(lista, read);
					
					is = context.getAssets().open("ListaConjuntos.xml");
					read = readInputStreamFile(is);
					lista = new File(dirQ, "ListaConjuntos.xml");
					writeFile(lista, read);
					
				} catch (FileNotFoundException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}

			}
//		}
//		else
//		{
//			messageDialog(context, R.string.erro, R.string.nosdcard,null);
//		}
		return dir;
	}
	
	public static void deleteRecursive(File f){
		if(f.isDirectory())
			for(File fin : f.listFiles())
				deleteRecursive(fin);
		f.delete();
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
	
	public static boolean writeFile(File f, String string){
		boolean saved = false;
		BufferedWriter bw = null;
	
		try{
			bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(f), "UTF-8"));
			try{
				bw.write(string);
				saved = true;
			}
			finally{
				bw.close();
			}
		}
		catch(IOException ex){
			ex.printStackTrace();
		}
		return saved;
	}
	
	
	public static String readInputStreamFile(InputStream input){
		BufferedReader br = null;
		StringBuilder sb = new StringBuilder();
	
		try{
			br = new BufferedReader(new InputStreamReader(input,"UTF-8"));
			try{
				String s;
				while((s = br.readLine()) != null)
				{
					sb.append(s);
					sb.append("\n");
				}
			}
			finally{
				br.close();
			}
		}
		catch (IOException ex) {
			ex.printStackTrace();
		}
		return sb.toString();
	}
    
}