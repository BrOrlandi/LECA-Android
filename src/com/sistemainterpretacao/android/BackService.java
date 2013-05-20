package com.sistemainterpretacao.android;

import java.io.File;
import java.util.ArrayList;

import leca.interpreter.User;
import leca.modelo.Question;
import leca.modelo.QuestionList;
import leca.util.Utilities;
import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;

/**
 * Serviço em Background para processar toda a interpretação do conteúdo. É a camada intermediária entre a interface gráfica e os objetos que são manipulados.
 * @author Bruno Orlandi
 *
 */
public class BackService extends Service{
	
	public final static String IFILTER = "com.sistemainterpretacao.BackService.LOADED"; ///< Intent Filter para enviar mensagem ao Broadcast Receiver
	public final static int START_NEW_QUESTIONLIST = 1; ///< Indica para o Service que estará iniciando a interpretação de uma nova lista.
	public final static int START_NEW_USERNAME = 2; ///< Indica para o Service o nome do usuário que está respondendo a nova lista que foi carregada.
	public final static int START_LOAD_USER = 3; ///< Indica para o Service que estará iniciando a interpretação de uma lista que já estava sendo respondida antes.
	
	
	private User mUser; ///< O usuário e suas informações da interpretação da lista.
	private QuestionList mQL; ///< Referencia a lista do prórpio usuário.
	private final IBinder mBinder = new BackBinder(); ///< Conecta às activitys
	private int mAtualQuestion = -1; ///< Corresponde à qual questão está carregada e sendo interpretada pelo usuário no momento.
	private int mAvaliacao; ///< Modo de avaliação durante a interpretação da lista.
	
	/**
	 * Binder para conectar com as Activitys
	 * @author Bruno Orlandi
	 *
	 */
	public class BackBinder extends Binder {
		BackService getService(){
			return BackService.this;
		}
	}
	
	@Override
	public void onCreate() {
		Log.v("BackService", "onCreate");
		super.onCreate();
	}

	@Override
	public void onDestroy() {
		Log.v("BackService", "onDestroy");
		super.onDestroy();
	}

	/**
	 * Trata os pedidos de inicialização do Service: se começou uma nova lista, se enviou o nome do usuário da nova lista, se carregou uma lista que ja estava sendo respodida.
	 */
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		Log.v("BackService", "onStartCommand");
		int start = intent.getIntExtra("start", 0);
		switch(start)		
		{
		case START_NEW_QUESTIONLIST:
			Intent i = new Intent(IFILTER);
			String xml = intent.getStringExtra("xml");
	
			QuestionList ql = QuestionList.fromXML(xml);
			if(ql == null)
			{
				Log.v("BackService", "Erro ao carregar question list.");
				i.putExtra("load", false);
				sendBroadcast(i);
				stopSelf();
				return 0;
			}
			mQL = ql;
			i.putExtra("load", true);
			sendBroadcast(i);
			Log.v("BackService","Question list carregada com sucesso,Broadcast enviado!");
			break;
		case START_NEW_USERNAME:
			mUser = new User(intent.getStringExtra("username"), mQL);
			Log.v("BackService","Username "+mUser.getNome());
			mAvaliacao = mUser.QL.getAvaliacao();
			break;
		case START_LOAD_USER:
			Intent i2 = new Intent(IFILTER);
			String xml2 = intent.getStringExtra("xml");
			User usr = User.fromXML(xml2);
			if(usr == null)
			{
				Log.v("BackService", "Erro ao carregar user.");
				i2.putExtra("load", false);
				sendBroadcast(i2);
				stopSelf();
				return 0;
			}
			Log.v("BackService", "User carregado com sucesso.");
			mUser = usr;
			i2.putExtra("load", true);
			sendBroadcast(i2);
			Log.v("BackService","Broadcast enviado!");
			mAvaliacao = mUser.QL.getAvaliacao();
			break;
		}
		return Service.START_STICKY;
	}

	@Override
	public IBinder onBind(Intent intent) {
		Log.v("BackService","onBind");
		return mBinder;
	}
	
	public QuestionList getQuestionList(){
		return mUser.QL;
	}
	
	public int getAcertos(){
		return mUser.getAcertos();
	}
	
	/**
	 * Responde a questão atual
	 * @param resposta posição da resposta na lista.
	 * @return inteiro retornado pela lista de questões.
	 */
	public int responderQuestao(int resposta){
		//int questionPos = mAtualQuestion;
		int ret = mUser.responderQuestao(mAtualQuestion, resposta);
		Log.v("BackService",mAtualQuestion + " : Acertou?: "+(ret==User.ACERTOU)+"Acertos: "+getAcertos());
		return ret;
	}
	
	/**
	 * Bundle contendo: 
	 * ArrayList<String> list;
	 * String title;
	 * int number_of_questions;
	 * @return
	 */
	public Bundle getQuestionListInfo(){
		Bundle b = new Bundle();
		ArrayList<Question> qlist = mUser.QL.getArrayListQuestion();
		ArrayList<String> list = new ArrayList<String>();
		int i = 1;
		for(Question q : qlist){
			StringBuilder sb = new StringBuilder();
			sb.append((i++) + " : ");
			if(q.Titulo.equals("")){
				if(q.Enunciado.length() > 100){
					sb.append(q.Enunciado.subSequence(0, 100).toString());
				}
				else{
					sb.append(q.Enunciado);
				}
			}
			else
			{
				sb.append(q.Titulo);
			}
			list.add(sb.toString());
		}
		b.putStringArrayList("list", list);
		b.putString("title", mUser.QL.Titulo);
		b.putInt("number_of_questions", mUser.QL.size());
		return b;
	}
	
	/**
	 * A questão atual será setada para a requisitada.
	 * Bundle contendo: 
	 * String title;
	 * String text;
	 * ArrayList<String> list;
	 * int answer;
	 * @param pos posição da questão na lista.
	 * @return 
	 */
	public Bundle getQuestionInfo(int pos){
		mAtualQuestion = pos; // a questão atual é esta que se pede.
		
		Bundle b = new Bundle();
		
		Question q = mUser.QL.getQuestion(pos);
		b.putString("title",q.Titulo);
		b.putString("text", q.Enunciado);
		ArrayList<String> list = q.getArrayListStringAlternatives();
		b.putStringArrayList("list", list);
		b.putInt("answer", mUser.getResposta(q.ID));
		
		return b;
	}
	
	public Bundle getQuestionInfo(){
		return getQuestionInfo(mAtualQuestion);
	}
	
	public int getAvaliacao(){
		return this.mAvaliacao;
	}

	public void setAtualQuestion(int pos) {
		mAtualQuestion = pos;
	}
	
	/**
	 * Muda para a próxima questão.
	 * @return
	 */
	public boolean nextQuestion(){
		if(mAtualQuestion +1 < mUser.QL.size()){
			mAtualQuestion++;
			return true;
		}
		return false;
	}
	
	/**
	 * Muda para a questão anterior.
	 * @return
	 */
	public boolean previousQuestion(){
		if(mAtualQuestion - 1 >= 0){
			mAtualQuestion--;
			return true;
		}
		return false;
	}
	
	/**
	 * Salvar o usuário para continuar a responder a lista posteriormente.
	 * @return true ou false se foi salvo com sucesso.
	 */
	public boolean saveUser(){
		File dir = MainMenu.getPath(BackService.this,MainMenu.USERS_DIR);
		if(dir != null)
		{
			String username = mUser.getNome();
			String listname = mUser.QL.Titulo;
			String total = dir.getPath()+"/"+username+" - "+listname+".xml";
			Log.d("BackService", "File: "+total);
			mUser.setUltimaQuestao(mAtualQuestion); // seta que a ultima questão que foi respondida é esta atual.
			return Utilities.saveStringToFile(total, mUser.toXML());
		}
		return false;
	}
	
	public void stopBackService(){
		stopSelf();
	}
	
	/**
	 * Bundle contendo:
	 * int acertos;
	 * int total;
	 * int tentativas;
	 * boolean[] corretas;
	 * @return o bundle contendo as informações da lista totalmente respondida
	 * @return null se nao tiver sido totalmente respondida
	 */
	public Bundle verificaRespondeuTodas(){
		Log.d("BackService","verificaRespondeuTodas");
		if(mUser.isCompletelyAnswered()){
			Bundle b = new Bundle();
			b.putInt("acertos", mUser.getAcertos());
			b.putInt("total",mUser.QL.getArrayListQuestion().size());
//			b.putInt("tentativas", mUser.getTentativas());
			boolean[] corretas = mUser.getRespostasCorretas();
			boolean[] corretas2 = new boolean[corretas.length];
			int i = 0;
			for(Question q : mUser.QL.getArrayListQuestion()){
				corretas2[i] = corretas[q.ID];
				i++;
			}
			b.putBooleanArray("corretas", corretas2);
			return b;
		}
		return null;
	}
}
