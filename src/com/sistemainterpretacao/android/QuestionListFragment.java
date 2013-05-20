package com.sistemainterpretacao.android;

import java.util.ArrayList;

import android.os.Build;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;

public class QuestionListFragment extends Fragment{

	public interface QuestionListListener{
		public void onQuestionSelect(int questionPosition);
		public void saveListViewState(Parcelable state);
		public Parcelable loadListViewState();
		
	}
	
	private QuestionListListener mListener;
	private Bundle mArguments;
	private int mLayout;
	private ListView mQuestionListView;
	
	public QuestionListFragment(){} // precisa ter o construtor default, pois ao mudar a orientação este é usado.

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mArguments = getArguments();
		mLayout = mArguments.getInt("layout");

		mListener = (QuestionListListener) getActivity();
		
		Log.v("QuestionListFragment", "onCreate Fragment");
		
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, 
	        Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.questionlist_fragment, container, false);
		mQuestionListView = (ListView)v.findViewById(R.id.listView1);
		ArrayList<String> questions = mArguments.getStringArrayList("list");

		Log.d(this.getClass().getName(),"Build.VERSION.SDK_INT = "+Build.VERSION.SDK_INT);
		if(Build.VERSION.SDK_INT <= Build.VERSION_CODES.GINGERBREAD_MR1){
			getActivity().setTitle(mArguments.getString("title"));
		}
		
		mQuestionListView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view, int pos, long row) {
				mListener.onQuestionSelect(pos);
			}
		});
		int resid = Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB && mLayout == UserWindow.LAYOUT_LANDSCAPE?
                R.layout.question_list_item_activated : R.layout.question_list_simple_item;
		//Log.d(this.getClass().getName(),"resid == R.layout.question_list_item_activated ? :"+ (resid == R.layout.question_list_item_activated));
		ArrayAdapter<String> adapter = new ArrayAdapter<String>(getActivity(), resid, questions);
		mQuestionListView.setAdapter(adapter);
		mQuestionListView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
		
		Parcelable state = mListener.loadListViewState();
		if(state != null){
			mQuestionListView.onRestoreInstanceState(state);
		}
		
		return v;
	}
	
	@Override
	public void onPause() {
		mListener.saveListViewState(mQuestionListView.onSaveInstanceState());
		super.onPause();
	}
	
	public void setSelected(int pos){
		mQuestionListView.setItemChecked(pos, true);
		mQuestionListView.setSelection(pos-3);
		return;
	}
	
}
