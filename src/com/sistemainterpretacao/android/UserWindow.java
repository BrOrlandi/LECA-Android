package com.sistemainterpretacao.android;


import java.util.ArrayList;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Parcelable;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.widget.Toast;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.sistemainterpretacao.android.QuestionFragment.QuestionListener;
import com.sistemainterpretacao.android.QuestionListFragment.QuestionListListener;

public class UserWindow extends SherlockFragmentActivity implements QuestionListListener, QuestionListener{

	public static final int LAYOUT_PORTRAIT = 0; ///< Layout do tipo retrato onde só uma tela é exibida.
	public static final int LAYOUT_LANDSCAPE = 1; ///< Layout do tipo paisagem onde podem ser exibidas até 3 telas.

	public static final int QUESTION_LIST_FOCUS = 0; ///< O foco do usuário é na tela lista de questões
	public static final int QUESTION_FOCUS = 1; ///< O foco do usuário é na tela da questão.

	public static final int TRANSITION_NONE = 0;
	public static final int TRANSITION_BACKWARD = 1;
	public static final int TRANSITION_FORWARD = 2;
	
	private int mFragmentFocused; ///< Guarda qual fragment está focado.
	private int mLayout = LAYOUT_PORTRAIT; ///< Guarda o tipo do Layout
	
	private BackService mService; ///< Referencia ao serviço em background.
	private Bundle mQuestionListInfo = null; ///< Pacote com informações da lista de questões
	private int mSelectedQuestion; ///< Questão selecionada.
	
	private Parcelable mListViewState; ///< O state salva o Scroll position do fragment de questões na activity.
	
	private QuestionListFragment mQLF; ///< Armazena o fragment da lista de questões para alterar a questão selecionada quando em modo paisagem. 
	
	
	/**
	 * Conexão com o service em background.
	 */
	private final ServiceConnection mConnection = new ServiceConnection() {
		
		@Override
		public void onServiceDisconnected(ComponentName name) {
			mService = null;
			Log.d("ServiceConnection", "onServiceDisconnected");
			erroBackService();
		}
		
		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			mService = ((BackService.BackBinder)service).getService();
			Log.d("ServiceConnection", "onServiceConnected");
			if(mQuestionListInfo == null)
			{
				mQuestionListInfo = mService.getQuestionListInfo();
			}
			createFragments(TRANSITION_NONE);
			
			//action bar
			if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB){ // se for 3.0 ou mais
				ActionBar ab = getSupportActionBar();
				ab.setTitle(mQuestionListInfo.getString("title"));
			}

		}
	};
	
	/*
	public int getResIDLayout(int type){ // nao usado ainda
		if(mLayout == LAYOUT_LANDSCAPE){
			if(type == QUESTION_LIST_FOCUS){
				return R.id.frag1;
			}else if(type == QUESTION_FOCUS){
				return R.id.frag2;
			}
		}
		return R.id.UserWindow_Portrait;
	}*/

	public void createFragments(int questionTransition){
		if(mLayout == LAYOUT_LANDSCAPE){ // se o layout for paisagem
			createQuestionListFragment(R.id.frag1); // cria a tela da lista de questoes
			if(mFragmentFocused == QUESTION_FOCUS){
				createQuestionFragment(R.id.frag2,questionTransition);  // cria tambem a questão se estiver com foco nas questoes
			}
		}
		else
		{
			int resid = R.id.UserWindow_Portrait;
			switch(mFragmentFocused){
			case QUESTION_LIST_FOCUS:
				//action bar
				if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB){ // se for 3.0 ou mais
					ActionBar ab = getSupportActionBar();
					ab.setSubtitle("");
				}
				createQuestionListFragment(resid);
				break;
			case QUESTION_FOCUS:
				createQuestionFragment(resid,questionTransition);
				break;
			}
		}
		
		//if(Build.VERSION.SDK_INT > Build.VERSION_CODES.GINGERBREAD_MR1){ // se for mais recente que 2.3
		//	setTitle(mQuestionListInfo.getString("title")); // coloca o titulo para lista de questões
		//}
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.user_window);
        if(findViewById(R.id.UserWindow_Landscape) != null){ // a tela esta em modo para exibir os 3 fragments
        	mLayout = LAYOUT_LANDSCAPE;
        }
        else{
        	mLayout = LAYOUT_PORTRAIT;
        }
		
		if(savedInstanceState != null){
			mQuestionListInfo = savedInstanceState.getBundle("QuestionList");
        	mFragmentFocused = savedInstanceState.getInt("fragmentfocused");
        	mSelectedQuestion = savedInstanceState.getInt("selectedQuestion");
		}
		else
		{
        	mFragmentFocused = QUESTION_LIST_FOCUS;
    		mSelectedQuestion = -1;
		}
		
		if(!bindBackService())
		{
			erroBackService();
		}
		
		mListViewState = null; // primeiro estado é null.

	}

	public void createQuestionListFragment(int resid){
		mQLF = new QuestionListFragment();
		mQLF.setArguments(mQuestionListInfo);
		FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
		ft.replace(resid, mQLF);
		ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
		mQuestionListInfo.putInt("layout", mLayout);
		ft.commit();
	}
	

	
	public void createQuestionFragment(int resid, int transition){
		mService.setAtualQuestion(mSelectedQuestion);
		Bundle QuestionInfo = mService.getQuestionInfo();
		QuestionInfo.putInt("layout", mLayout);
		Log.d("Question"," " +mSelectedQuestion+" : "+ QuestionInfo.getString("text"));
		
		//QuestionFragment 
		QuestionFragment qf = new QuestionFragment();
		qf.setArguments(QuestionInfo);
		FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
		ft.replace(resid, qf);

		switch(transition)
		{
		case 0:
			break;
		case 1:
			ft.setCustomAnimations(R.anim.left_in, R.anim.right_out);
			break;
		case 2:
			ft.setCustomAnimations(R.anim.right_in, R.anim.left_out);
			break;
		}
		
		ft.commit();
		
		//action bar
		if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB && resid == R.id.UserWindow_Portrait){ // se for 3.0 ou mais
			ActionBar ab = getSupportActionBar();
			ab.setSubtitle(QuestionInfo.getString("title"));
		}
		
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putBundle("QuestionList", mQuestionListInfo);
		outState.putInt("fragmentfocused",mFragmentFocused);
		outState.putInt("selectedQuestion",mSelectedQuestion);
	}
	
	@Override
	protected void onPause() {
		super.onPause();
	}

	private boolean bindBackService(){
		return bindService(new Intent(getApplicationContext(),BackService.class), mConnection, BIND_AUTO_CREATE);
	}
	
	protected void erroBackService(){
		MainMenu.messageDialog(this, R.string.erro, R.string.erroservice, new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				finish();
			}
		});
	}
	
	@Override
	public void onBackPressed() {
		if(mLayout == LAYOUT_PORTRAIT && mFragmentFocused == QUESTION_FOCUS){
			mFragmentFocused = QUESTION_LIST_FOCUS;
			createFragments(TRANSITION_BACKWARD);
		}
		else
		{
		final AlertDialog d = new AlertDialog.Builder(UserWindow.this).create();
		d.setTitle(R.string.backSalvar);
		d.setMessage(getText(R.string.backSalvarMessage));

		DialogInterface.OnClickListener listener = new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				switch(which){
				case AlertDialog.BUTTON_POSITIVE:
					//String path = MainMenu.getQuestionListsPath(QuestionListMenu.this);
					new SaveUserFile().execute();
					break;
				case AlertDialog.BUTTON_NEUTRAL:
					break;
				case AlertDialog.BUTTON_NEGATIVE:
					finalizeWork();
					break;
				}
			}
		};

		d.setButton(AlertDialog.BUTTON_POSITIVE, getText(R.string.yes), listener);
		d.setButton(AlertDialog.BUTTON_NEUTRAL, getText(R.string.cancel), listener);
		d.setButton(AlertDialog.BUTTON_NEGATIVE, getText(R.string.no), listener);
		d.show();
		//super.onBackPressed();
		}
	}

	@Override
	protected void onDestroy() {
		unbindService(mConnection);
		super.onDestroy();
	}
	
	private void finalizeWork(){
		mService.stopBackService();
		finish();
	}
	
	private class SaveUserFile extends AsyncTask<Void, Void, Boolean>{

		private ProgressDialog pd;

		@Override
		protected void onPreExecute() {
			pd = ProgressDialog.show(UserWindow.this, getText(R.string.saving), getText(R.string.savingmessage), true, true, new DialogInterface.OnCancelListener() {
				
				@Override
				public void onCancel(DialogInterface dialog) {
					SaveUserFile.this.cancel(true);
				}
			});
			super.onPreExecute();
		}
		@Override
		protected Boolean doInBackground(Void... params) {
			return mService.saveUser();
			//return null;
		}

		@Override
		protected void onCancelled() {
			pd.dismiss();
			super.onCancelled();
		}

		@Override
		protected void onPostExecute(Boolean result) {
			pd.dismiss();
			finalizeWork();
			super.onPostExecute(result);
		}

		@Override
		protected void onProgressUpdate(Void... values) {

			super.onProgressUpdate(values);
		}
		
	}
	
	public void onQuestionSelect(int questionPosition) {
		mSelectedQuestion = questionPosition;
		mFragmentFocused = QUESTION_FOCUS;
		if(mLayout == LAYOUT_LANDSCAPE){
			createQuestionFragment(R.id.frag2,TRANSITION_FORWARD);
		}
		else
		{
			createQuestionFragment(R.id.UserWindow_Portrait,TRANSITION_FORWARD);
		}

	}

	@Override
	public void onAnswerQuestion(int alternativePosition) {
		Log.d("UserWindow","Atual: "+mSelectedQuestion+ "  onAnswerQuestion: "+alternativePosition);
		int ret = mService.responderQuestao(alternativePosition);
		if(ret == leca.interpreter.User.ACERTOU){
			Toast t = Toast.makeText(this, R.string.rightanswer, Toast.LENGTH_SHORT);
			t.show();
		}else if(ret == leca.interpreter.User.ERROU)
		{
			Toast t = Toast.makeText(this, R.string.wronganswer, Toast.LENGTH_SHORT);
			t.show();
		}
		Bundle b = mService.verificaRespondeuTodas();
		if(b != null){
			ProgressDialog dialog = ProgressDialog.show(UserWindow.this, "", 
                    this.getText(R.string.loading_results), true);
	        Log.d("UserWindow", "Diálogo: Mostrar resultados.");
			String t = (String)this.getText(R.string.results);
			AlertDialog d = new AlertDialog.Builder(this).create();
			d.setTitle(t);
			
			StringBuilder sb = new StringBuilder();
			sb.append(this.getText(R.string.corrects)+"/"+this.getText(R.string.total)+" : ");
			sb.append(Integer.toString(b.getInt("acertos")) + "/" + Integer.toString(b.getInt("total")));
			//sb.append("\n"+this.getText(R.string.trys)+" : "+Integer.toString(b.getInt("tentativas")));
			sb.append("\n");
			sb.append("\n");
			
			boolean[] corretas = b.getBooleanArray("corretas");
			ArrayList<String> als = mQuestionListInfo.getStringArrayList("list");
			int i=0;
			String substr;
			for(String s : als){
				if(s.length() < 50){
					substr = s.replace('\n',' ');
				}
				else
				{
					substr = s.substring(0, 50);
				}
				if(corretas[i]){
					sb.append("V "+substr+"...\n");
				}
				else
				{
					sb.append("X "+substr+"...\n");
				}
				i++;
			}
			d.setMessage(sb.toString());

			
			OnClickListener listener = new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					
				}
			};
			d.setButton(AlertDialog.BUTTON_POSITIVE, (String)this.getText(android.R.string.ok), listener);
			dialog.dismiss();
			d.show();
		}
	}

	@Override
	public void saveListViewState(Parcelable state) {
		mListViewState = state;
	}

	@Override
	public Parcelable loadListViewState() {
		return mListViewState;
	}
	
	@Override
	  public boolean onCreateOptionsMenu(Menu menu) {
	    MenuInflater inflater = getSupportMenuInflater();
	    inflater.inflate(R.menu.userwindow_actionbar, menu);
	    return true;
	  }

	@Override
	  public boolean onOptionsItemSelected(MenuItem item) {
    	int max = mQuestionListInfo.getInt("number_of_questions") - 1;
	    switch (item.getItemId()) {
	    case R.id.previous:
	      //Toast.makeText(this, "Previous selected", Toast.LENGTH_SHORT).show();
			if(mLayout == LAYOUT_LANDSCAPE){ // se o layout for paisagem
				if(mSelectedQuestion > 0){
					mSelectedQuestion--;
					createQuestionFragment(R.id.frag2,TRANSITION_BACKWARD); 
					mQLF.setSelected(mSelectedQuestion);
				}
			}
			else
			{
				int resid = R.id.UserWindow_Portrait;
				switch(mFragmentFocused){
				case QUESTION_LIST_FOCUS:
					/*
					//action bar
					if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB){ // se for 3.0 ou mais
						ActionBar ab = getActionBar();
						ab.setSubtitle("");
					}
					createQuestionListFragment(resid);
					*/
					mSelectedQuestion = max;
					createQuestionFragment(resid,TRANSITION_BACKWARD);
					mFragmentFocused = QUESTION_FOCUS;
					break;
				case QUESTION_FOCUS:
					if(mSelectedQuestion == 0){
						mSelectedQuestion = -1;
						//action bar
						if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB){ // se for 3.0 ou mais
							ActionBar ab = getSupportActionBar();
							ab.setSubtitle("");
						}
						createQuestionListFragment(resid);
						mFragmentFocused = QUESTION_LIST_FOCUS;
					}
					else if(mSelectedQuestion > 0)
					{
						mSelectedQuestion--;
						createQuestionFragment(resid,TRANSITION_BACKWARD);
					}
					break;
				}
			}
	    	
	      break;
	    case R.id.next:
	      //Toast.makeText(this, "Next selected", Toast.LENGTH_SHORT).show();
			if(mLayout == LAYOUT_LANDSCAPE){ // se o layout for paisagem
				if(mSelectedQuestion < max){
					mSelectedQuestion++;
					createQuestionFragment(R.id.frag2,TRANSITION_FORWARD); 
					mQLF.setSelected(mSelectedQuestion);
				}
			}
			else
			{
				int resid = R.id.UserWindow_Portrait;
				switch(mFragmentFocused){
				case QUESTION_LIST_FOCUS:
					/*
					//action bar
					if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB){ // se for 3.0 ou mais
						ActionBar ab = getActionBar();
						ab.setSubtitle("");
					}
					createQuestionListFragment(resid);
					*/
					mSelectedQuestion = 0;
					createQuestionFragment(resid,TRANSITION_FORWARD);
					mFragmentFocused = QUESTION_FOCUS;
					break;
				case QUESTION_FOCUS:
					if(mSelectedQuestion == max){
						mSelectedQuestion = -1;
						//action bar
						if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB){ // se for 3.0 ou mais
							ActionBar ab = getSupportActionBar();
							ab.setSubtitle("");
						}
						createQuestionListFragment(resid);
						mFragmentFocused = QUESTION_LIST_FOCUS;
					}
					else if(mSelectedQuestion < max)
					{
						mSelectedQuestion++;
						createQuestionFragment(resid,TRANSITION_FORWARD);
					}
					break;
				}
			}
	      break;

	    default:
	      break;
	    }

	    return true;
	  }
}
