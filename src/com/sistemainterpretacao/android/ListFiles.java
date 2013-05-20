package com.sistemainterpretacao.android;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FilenameFilter;
import java.io.IOException;

import android.app.AlertDialog;
import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;

/**
 * Basicamente um navegador de arquivos e pastas para carregar o conteúdo.
 * @author Bruno Orlandi
 *
 */
public class ListFiles extends ListActivity{

	private String[] mList; ///< Lista de arquivos e pastas do diretório atual.
	private String mPath; ///< Caminho do diretório atual.
	private String mAppDir; ///< Indica qual tipo de pasta da aplicação está sendo acessada, QuestionList ou Users.
	private int mMode; ///< Indica para que os arquivos serão carregas, uma nova lista de exercicios ou carregar um usuário já existente.

	/**
	 *  Cria a janela com a lista de arquivos.
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		Intent i = getIntent();
		mPath = i.getStringExtra("path"); // pega caminho do diretório.
		mAppDir = i.getStringExtra("appdir"); // pega a pasta do aplicativo "QuestionLists" ou "User".
		
		String title = null;
		if(mAppDir.equals(MainMenu.QUESTIONLIST_DIR)){
			title = (String)getText(R.string.questionlists_folder);
			mMode = BackService.START_NEW_QUESTIONLIST;
		}
		else if(mAppDir.equals(MainMenu.USERS_DIR)){
			title = (String)getText(R.string.users_folder);
			mMode = BackService.START_LOAD_USER;
		}
		setTitle(title+mPath.subSequence(13+MainMenu.ROOT_DIR.length()+mAppDir.length(), mPath.length()));// coloca um titulo "camuflado" na tela, Ex: "Lista Questões/pasta".

		carregarLista();
		
		getListView().setOnItemLongClickListener(new OnItemLongClickListener() {

			@Override
			public boolean onItemLongClick(AdapterView<?> arg0, View arg1,
					int position, long arg3) {
				File f = new File(mPath,mList[position]);
				if(!f.isFile()){
					return false;
				}
				deleteFile(ListFiles.this, f, ListFiles.this);
				return true;
			}
		});
	}
	
	/**
	 * Carrega a lista de arquivos do diretório para exibir de acordo com o filtro. Avisa se o diretório estiver vazio.
	 */
	public void carregarLista() {
		//Carregando lista
		File f = new File(mPath);
		FilenameFilter ft = new FilenameFilter() { // filtro para diretórios e arquivos XML

			@Override
			public boolean accept(File dir, String filename) {
				File f = new File(dir,filename);
				if(f.isFile())
				{
					String n = f.getName().toUpperCase();
					if(n.endsWith("XML")) // se o formato é XML
					{
						return true;
					}
					else return false;
				}
				return true; // se for um diretório
			}
		};
		mList = f.list(ft); // cria o vetor de strings com os arquivos xml e diretórios
		
		if(mList.length == 0) // pasta vazia
		{
			MainMenu.messageDialog(ListFiles.this, R.string.aviso, R.string.dirvazio, new DialogInterface.OnClickListener() { // aviso que está vazio
				
				@Override
				public void onClick(DialogInterface dialog, int which) {
					backDir(); // volta na pasta anterior
				}
			});
		}
		else
		{
			setListAdapter(new ArrayAdapter<String>(ListFiles.this,android.R.layout.simple_list_item_1,mList)); // set adapter
		}
		
	}

	/**
	 * Exclui um arquivo do diretório.
	 * @param context contexto da aplicação.
	 * @param f arquivo a ser excluido.
	 * @param activity Janela para atualizar a lista.
	 */
	public void deleteFile(Context context, final File f, final ListFiles activity){
		final AlertDialog d = new AlertDialog.Builder(context).create();
		d.setTitle(R.string.delete);
		d.setMessage((String)context.getText(R.string.deletemessage1)+f.getName()+(String)context.getText(R.string.deletemessage2));
		DialogInterface.OnClickListener listener = new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				switch(which){
				case AlertDialog.BUTTON_POSITIVE:
					f.delete();
					activity.carregarLista();
					break;
				case AlertDialog.BUTTON_NEUTRAL:
					break;
				}
			}
		};

		d.setButton(AlertDialog.BUTTON_POSITIVE, context.getText(R.string.yes), listener);
		d.setButton(AlertDialog.BUTTON_NEUTRAL, context.getText(R.string.no), listener);

		d.show();
	}
	
	/**
	 * Listener da Lista de arquivos. Acessa um diretório ou carrega um arquivo.
	 */
	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) { // ao clicar em um item da lista
		super.onListItemClick(l, v, position, id);
		File f = new File(mPath,mList[position]); // cria a representação do arquivo criado em File
		if(f.isFile())
		{
			new LoadFile().execute(f); // se for arquivo, é um XML pelo filtro então carrega o arquivo.
		}
		else // se não, é um diretório, cria esta mesma activity para ver os arquivos deste diretório.
		{
			String nPath = f.getPath();
			Intent i = new Intent(ListFiles.this,ListFiles.class);
			i.putExtra("path",nPath);
			i.putExtra("appdir",mAppDir);
			startActivity(i);
			overridePendingTransition(R.anim.right_in, R.anim.left_out);
			finish();
		}
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
	}
	
	/**
	 * Chama o método para voltar no diretório anterior quando o botão "voltar" foi pressionado no dispositivo.
	 */
	@Override
	public void onBackPressed() {
		backDir(); // cria a activity com o diretório anterior e impede que volte para o menu principal
		//super.onBackPressed(); 
	}
	
	/**
	 * Carrega o diretório anterior, caso não existe volta ao menu principal.
	 */
	private void backDir(){
		String qldir = MainMenu.getPath(ListFiles.this, mAppDir).getPath();
		//if(!path.equals(Environment.getExternalStorageDirectory().getPath()))
		if(!mPath.equals(qldir))// só pode voltar até o diretório das listas de questões
		{
			File f = new File(mPath);
			String fname = f.getName();
			int tam = mPath.length() - fname.length();
			String pathback = mPath.substring(0, tam-1);  // calcula o caminho do diretório anterior

			Intent i = new Intent(ListFiles.this,ListFiles.class); // inicia esta Activity para ver os arquivos do diretório anterior
			i.putExtra("path",pathback);
			i.putExtra("appdir",mAppDir);
			startActivity(i);
			
			overridePendingTransition(R.anim.left_in, R.anim.right_out); // animação de transição de tela da esquerda para a direita, dando a sensação de "voltar" para o usuário
		}
		finish();
	}
	
	/**
	 * Tenta interpretar o arquivo XML criando o service e abrindo outra activity de loading, verificando se é um XML válido.
	 * @param filename String com o nome do arquivo.
	 * @param result conteúdo do arquivo de texto XML.
	 */
	protected void forResult(String filename, String result){ // inicia o carregamento do service que irá converter, no seu inicio, o Xml, e esta activity aguarda o resultado se é xml válido ou não
		Intent i = new Intent(this,LoadingService.class);
		i.putExtra("result", result);
		i.putExtra("filename", filename);
		i.putExtra("mode", mMode);
		Log.d("ListFiles","Mode: "+mMode);
		startActivityForResult(i, 0);
	}

	/**
	 * Verifica se o arquivo XML foi interpretado com sucesso e finaliza esta activity.
	 * Permite que o usuário volte diretamente ao menu principal quando está interpretando uma lista de exercicios.
	 * Caso o arquivo não tenha sido lido com sucesso, mantem-se essa activty para permitir que o usuário selecione outro arquivo, ou exlcua o corrompido.
	 */
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if(resultCode == RESULT_FIRST_USER+1) // significa que o xml lido pelo service é válido.
		{
			finish(); // finaliza a activity, para o usuario voltar no menu principal depois. Não é necessário mais visualizar arquivos.
		}
	}

	/**
	 * Async Task para carregar um arquivo em Background.
	 * @author Bruno Orlandi
	 *
	 */
	class LoadFile extends AsyncTask<File, Integer, String> implements DialogInterface.OnCancelListener{

		ProgressDialog pd;
		String filename;
		
		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			pd = ProgressDialog.show(ListFiles.this, (String)getText(R.string.carregando), (String)getText(R.string.carregandoarquivo), true,true,this);
		}

		@Override
		protected String doInBackground(File... params) {
			File f = params[0];
			filename = f.getName();
			FileReader fr = null;
			try {
				fr = new FileReader(f);
			} catch (FileNotFoundException e) {
				e.printStackTrace();
				Log.e("ERRO", "FileNotFoundException não deveria ocorrer");
			}
			BufferedReader br = new BufferedReader(fr);
			StringBuilder sb = new StringBuilder();
			try{				
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

		@Override
		protected void onPostExecute(String result) {
			super.onPostExecute(result);
			//Log.d("AsyncTask","PostExecute: "+result);
			pd.dismiss();
			forResult(filename, result);
		}

		@Override
		public void onCancel(DialogInterface dialog) {
			pd.dismiss();
			this.cancel(true);
		}
		
	}
}
