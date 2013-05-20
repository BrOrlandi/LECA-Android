package com.sistemainterpretacao.android;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;

public class UserMenu extends Activity{

	EditText username; ///< Caixa de texto para inserir o nome do usuário.
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Log.d(this.getLocalClassName(),"onCreate");
		setContentView(R.layout.usermenu);
		username = (EditText)findViewById(R.id.editText1);
		username.requestFocus();
		username.setOnEditorActionListener(new OnEditorActionListener() {
			
			@Override
			public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
				return sendUserName();
			}
		});
		
		Button bt = (Button)findViewById(R.id.button1);
		bt.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				sendUserName();
			}
		});
	}

	@Override
	public void onBackPressed() {
		stopService(new Intent(this, BackService.class));
		super.onBackPressed();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
	}
	
	/**
	 * Manda o nome para o Service e chama a próxima janela para iniciar a interpretação.
	 * @return boolean indicando se o nome está correto(true) ou está vazio(false).
	 */
	private boolean sendUserName(){
		String name = username.getText().toString();
		if(name.length() > 0)
		{
			Intent i = new Intent(this,BackService.class);
			i.putExtra("start",BackService.START_NEW_USERNAME);
			i.putExtra("username",name);
			startService(i);
			
			Intent i2 = new Intent(getApplicationContext(),UserWindow.class);
			startActivity(i2);
			finish();
			return true;
		}
		else
		{
			MainMenu.messageDialog(this, R.string.erro, R.string.erronomevazio, null);
			return false;
		}
	}

}
