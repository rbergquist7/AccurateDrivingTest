package com.wsuproj5.accuratedrivingtest;

import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;

public class DuringEvaluation extends ActionBarActivity {
	
	private static final int VISIBLE = 0;
	private static final int INVISIBLE = 4;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_during_evaluation);
	}

	 public void extendCommentMenu(View view) {
	    	LinearLayout commentMenu = (LinearLayout) findViewById(R.id.menu_comments);
	    	commentMenu.setVisibility(VISIBLE);
	    	//Intent intent = new Intent(this, DisplayMessageActivity.class);
	    	//EditText editText = (EditText) findViewById(R.id.edit_message);
	    	//String message = editText.getText().toString();
	    	//intent.putExtra(EXTRA_MESSAGE, message);
	    	//startActivity(intent);
	    }
	    
	    public void hideCommentMenu(View view) {
	    	LinearLayout commentMenu = (LinearLayout) findViewById(R.id.menu_comments);
	    	commentMenu.setVisibility(INVISIBLE);
	    }
	    
	    public void revealOBDDataMenu(View view) {
	    	LinearLayout obdDataMenu = (LinearLayout) findViewById(R.id.menu_OBD_data);
	    	obdDataMenu.setVisibility(VISIBLE);
	    }
	    
	    public void hideOBDDataMenu(View view) {
	    	LinearLayout obdDataMenu = (LinearLayout) findViewById(R.id.menu_OBD_data);
	    	obdDataMenu.setVisibility(INVISIBLE);
	    }
	    
	    public void revealRouteProgress(View view) {
	    	LinearLayout routeProgress = (LinearLayout) findViewById(R.id.menu_route_progress);
	    	routeProgress.setVisibility(VISIBLE);
	    }
	    
	    public void hideRouteProgress(View view) {
	    	LinearLayout routeProgress = (LinearLayout) findViewById(R.id.menu_route_progress);
	    	routeProgress.setVisibility(INVISIBLE);
	    }
	    
	    public void revealTestProgress(View view) {
	    	LinearLayout testProgress = (LinearLayout) findViewById(R.id.menu_test_progress);
	    	testProgress.setVisibility(VISIBLE);
	    }
	    
	    public void hideTestProgress(View view) {
	    	LinearLayout testProgress = (LinearLayout) findViewById(R.id.menu_test_progress);
	    	testProgress.setVisibility(INVISIBLE);
	    }
}
