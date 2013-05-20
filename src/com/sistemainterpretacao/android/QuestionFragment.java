package com.sistemainterpretacao.android;

import java.util.ArrayList;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

public class QuestionFragment extends Fragment{

		public interface QuestionListener{
			public void onAnswerQuestion(int alternativePosition);
		}
		
		private QuestionListener mListener; ///< Listener da tela de questões
		private Bundle mArguments;
		private ListView mAlternativesListView;

		public QuestionFragment(){}

		@Override
		public void onCreate(Bundle savedInstanceState) {
			super.onCreate(savedInstanceState);
			mArguments = getArguments();
			mListener = (QuestionListener) getActivity();
		}

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container, 
		        Bundle savedInstanceState) {
			if(savedInstanceState != null){
				return null;
			}
			super.onCreate(savedInstanceState);			
			Log.d("QuestionFragment","onCreateView");
			View v = inflater.inflate(R.layout.question_fragment, container, false);
			((TextView)v.findViewById(R.id.textView1)).setText(mArguments.getString("text"));  // acha o texto do enunciado
 
			// se a tela for retrato, coloca o titulo da questao como titulo da tela
			int layout = mArguments.getInt("layout");
			if(layout == UserWindow.LAYOUT_PORTRAIT){
				String title = mArguments.getString("title");
				if(!title.equals("")){
					getActivity().setTitle(title);
				}
			}
			
			//Lista
			mAlternativesListView = (ListView)v.findViewById(R.id.listView1); // acha a lista
			ArrayList<String> list = mArguments.getStringArrayList("list"); // carrega a lista de strings
			
			mAlternativesListView.setChoiceMode(ListView.CHOICE_MODE_SINGLE); // seta o modo de seleção
			ArrayAdapter<String> adapter = new ArrayAdapter<String>(getActivity(), R.layout.alternative_list_item, list); // declara o layout dos items da lista
			mAlternativesListView.setAdapter(adapter); // seta o layout dos items
			
			mAlternativesListView.setOnItemClickListener(new OnItemClickListener() { // seta o listener

				@Override
				public void onItemClick(AdapterView<?> parent, View view, int pos, long row) {
					mListener.onAnswerQuestion(pos);
				}
			});

			int resp = mArguments.getInt("answer"); // carrega a resposta assinalada anteriormente
			Log.d("QuestionFragment","Carregou resposta: "+resp);
			if(resp != -1)
			{
				mAlternativesListView.setItemChecked(resp, true);
			}
			return v;
		}

/*
	private ListView mListView;
	private boolean canSwitch = false;
	
	private float x,move;
	/*
	private final ServiceConnection mConnection = new ServiceConnection() {
		
		@Override
		public void onServiceDisconnected(ComponentName name) {
			mService = null;
			erroBackService();
		}
		
		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			mService = ((BackService.BackBinder)service).getService();
			canSwitch = true;
		}
	};
	*/
/*	@Override
	public void onBackPressed() {
		if(canSwitch)
		{
			respondeuQuestao();
			super.onBackPressed();
		}
	}
	
	private void respondeuQuestao(){
		int resp = mListView.getCheckedItemPosition();
		//Log.d("QuestionWindow","Assinalou resposta: " + resp);
		int verif = mService.respondeuQuestao(resp);
		switch(verif)
		{
		
		case User.ACERTOU:
			Toast.makeText(QuestionWindow.this, R.string.rightanswer, 1000).show();
		break;
		
		case User.ERROU:
			Toast.makeText(QuestionWindow.this, R.string.wronganswer, 1000).show();
		break;
		
		}
	}
	
	//private static final int NEXT_QUESTION = 1;
	//private static final int PREVIOUS_QUESTION = 2;
	
	private void loadQuestion(int move){
		if(canSwitch)
		{
			boolean vai = false;
			respondeuQuestao();
			int anim1=0,anim2=0;
			switch(move){
			case NEXT_QUESTION:
				//Log.d("QuestionWindow", "Next");
				vai = mService.nextQuestion();
				anim1 = R.anim.right_in;
				anim2 = R.anim.left_out;
				break;
			case PREVIOUS_QUESTION:
				//Log.d("QuestionWindow", "Previous");
				vai = mService.previousQuestion();
				anim1 = R.anim.left_in;
				anim2 = R.anim.right_out;
				break;
			}
			if(vai)
			{
				Bundle b = mService.getQuestionInfo();
				Intent i = new Intent(getApplicationContext(),QuestionWindow.class);
				i.putExtras(b);
				startActivity(i);
				overridePendingTransition(anim1, anim2);
				Thread t = new Thread(new Runnable() {
					
					@Override
					public void run() {
						SystemClock.sleep(400);
						finish();
					}
				});
				t.start();
			}
			else
			{
				//onBackPressed();
				Animation anim = null;
				switch(move){
				case NEXT_QUESTION:
					anim = AnimationUtils.loadAnimation(QuestionWindow.this, R.anim.last_right);
					break;
				case PREVIOUS_QUESTION:
					anim = AnimationUtils.loadAnimation(QuestionWindow.this, R.anim.last_left);
					break;
				}
				getWindow().getDecorView().findViewById(android.R.id.content).startAnimation(anim);
				
			}
		}
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.questionwindow);
		
		Display d = getWindowManager().getDefaultDisplay();
		move = d.getWidth()/3;
		
		
		Bundle b = getIntent().getExtras();
		setTitle(b.getString("title"));
		((TextView)findViewById(R.id.textView1)).setText(b.getString("enunciado"));
		mListView = (ListView)findViewById(R.id.listView1);
		ArrayList<String> list = b.getStringArrayList("list");
		
		//ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_single_choice, list);
		//AlternativeListAdapter adapter = new AlternativeListAdapter(this, list);
		ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, R.layout.alternative_list_item, list);
		mListView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
		mListView.setAdapter(adapter);
		mListView.setOnItemClickListener(this);
		int resp = b.getInt("resposta");
		//Log.d("QuestionWindow","Carregou resposta: "+resp);
		if(resp != -1)
		{
			mListView.setItemChecked(resp, true);
		}
		if(!bindBackService())
		{
			erroBackService();
		}
	}
	
	@Override
	public boolean dispatchTouchEvent(MotionEvent ev) {
		int action = ev.getAction();
		switch(action){
		case MotionEvent.ACTION_DOWN:
			x = ev.getX();
			break;
		case MotionEvent.ACTION_UP:
			float ax = ev.getX();
			if((ax - x) <= -move)
			{
				loadQuestion(NEXT_QUESTION);
			}
			else if((ax - x) >= move)
			{
				loadQuestion(PREVIOUS_QUESTION);
			}
			break;
		case MotionEvent.ACTION_MOVE:
			return true;
		}
		return super.dispatchTouchEvent(ev);
	}
	

	private boolean bindBackService(){
		return bindService(new Intent(getApplicationContext(),BackService.class), mConnection, BIND_AUTO_CREATE);
	}

	protected void erroBackService(){
		MainMenu.messageDialog(QuestionWindow.this, R.string.erro, R.string.erroservice, new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				finish();
			}
		});
	}
	
	@Override
	protected void onDestroy() {
		unbindService(mConnection);
		super.onDestroy();
	}

	@Override
	public void onItemClick(AdapterView<?> arg0, View view, int pos, long row) {
		//respondeuQuestao();
	}	
	*/
}
