package com.sistemainterpretacao.android;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

public class LoadingService extends Activity{
		
	Verif mBR = new Verif();
	String mFilename;
	int mMode;
	
	/**
	 * Runnable para inciar o Service em outra Thread.
	 * @author Bruno Orlandi
	 *
	 */
	class MyRun implements Runnable{
		
		String xml;
		
		public MyRun(String XML){
			xml = XML;
		}
		
		@Override
		public void run() {
			Log.d(this.getClass().getName(),"Starting Service: "+ mMode);
			Intent i = new Intent(getApplicationContext(),BackService.class);
			i.putExtra("xml", xml);
			i.putExtra("start", mMode);
			startService(i);	
			//Log.d("ListFiles","Run Thread file: "+filename);
		}
		
	}
	
	/**
	 * Registra o Broadcast Receiver, exibe o progresso de carregando, cria a Thread para iniciar o Service.
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.loadingservice);
		registerReceiver(mBR, new IntentFilter(BackService.IFILTER));
		
		Log.d(this.getLocalClassName(),"BroadcastReceiver Registrado!");
		Intent i = getIntent();
		mFilename = i.getStringExtra("filename");
		String result = i.getStringExtra("result");
		mMode = i.getIntExtra("mode", 0);
		
		TextView tv = (TextView)findViewById(R.id.textView1);
		switch(mMode){
		case BackService.START_NEW_QUESTIONLIST:
			tv.setText(R.string.carregandolista);
			break;
		case BackService.START_LOAD_USER:
			tv.setText(R.string.carregandouser);
			break;
		}
		
		Thread t = new Thread(new MyRun(result));
		t.start();
	}

	/**
	 * Remove o Broadcast Receiver ao finalizar.
	 */
	@Override
	protected void onDestroy() {
		Log.d(this.getLocalClassName(),"BroadcastReceiver desregistrado!");
		unregisterReceiver(mBR); // tira o BroadcastReceiver
		super.onDestroy();
	}
	
	/**
	 * Trata se o Service carregou com sucesso, chamando a próxima janela.
	 * @param carregou boolean que indica se o Service carregou com sucesso.
	 */
	protected void received(boolean carregou){
		if(carregou) // o service carregou o XML com sucesso
		{
			setResult(RESULT_FIRST_USER+1); // retorna o resultado para finalizar a tela de abrir arquivos.
			Intent i = null;
			switch(mMode){
			case BackService.START_NEW_QUESTIONLIST:
				Log.d(this.getLocalClassName(),"Loading UserMenu");
				i = new Intent(getApplicationContext(),UserMenu.class); // inicia a interpretação da lista de questõs com a o menu do usuario.
				break;
			case BackService.START_LOAD_USER:
				i = new Intent(getApplicationContext(),UserWindow.class); // inicia a interpretação da lista de questõs com a o menu do usuario.
				Log.d(this.getLocalClassName(),"Loading UserWindow");
				break;
			}
			startActivity(i);
			finish();
		}
		else // o arquivo XML não é uma lista de questões válida. Exibe erro e retorna a activity para ver outros arquivos.
		{
			Log.d(this.getLocalClassName(),"Erro no XML");
			String s1=null,s2=null;
			switch(mMode){
			case BackService.START_NEW_QUESTIONLIST:
				s1 = (String)getText(R.string.qlerro1);
				s2 = (String)getText(R.string.qlerro2);
				break;
			case BackService.START_LOAD_USER:
				s1 = (String)getText(R.string.usererro1);
				s2 = (String)getText(R.string.usererro2);
				break;
			}
			setResult(RESULT_FIRST_USER+2);
			MainMenu.messageDialog(this, R.string.erro, s1+mFilename+s2, new DialogInterface.OnClickListener() {
				
				@Override
				public void onClick(DialogInterface dialog, int which) {
					finish();
				}
			});
		}
	}
	
	/**
	 * Broadcast Receiver para qual o Service diz se foi aberto com sucesso. 
	 * @author Bruno Orlandi
	 *
	 */
	class Verif extends BroadcastReceiver{ // usado para receber uma Intent do service dizendo se o arquivo XML foi carregado com sucesso

		@Override
		public void onReceive(Context context, Intent intent) {
			boolean carregou = intent.getBooleanExtra("load", false); // true se carregou e false se não carregou
			Log.d("BroadcastReceiver","onReceive, load = "+ carregou);
			received(carregou);
			
		}
		
	}
	
}
