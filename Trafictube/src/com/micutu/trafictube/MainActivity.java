package com.micutu.trafictube;

import java.io.IOException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.androidquery.AQuery;
import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshBase.Mode;
import com.handmark.pulltorefresh.library.PullToRefreshBase.OnRefreshListener;
import com.handmark.pulltorefresh.library.PullToRefreshScrollView;
import com.slidingmenu.lib.SlidingMenu;
import com.slidingmenu.lib.app.SlidingActivity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends SlidingActivity {

	private SlidingMenu sMenu;
	private AQuery aq;
	List<video> videos;
	private String currentLink = "";
	private int currentMode = 0;
	private boolean hasNext = false;
	private boolean changed;
	
	final private String siteLink = "http://www.trafictube.ro/";
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		setTheme(R.style.Theme_Sherlock);
		super.onCreate(savedInstanceState);
		
		/* configure SlidingMenu */
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		
		currentMode = R.id.menuButtonLogo;
		setContentView(R.layout.trafictube);
		setBehindContentView(R.layout.menu);
		sMenu = getSlidingMenu();
		sMenu.setBehindOffset(100);
		sMenu.setShadowDrawable(R.drawable.shadow);
		sMenu.setShadowWidth(10);
		sMenu.setMode(SlidingMenu.LEFT);
        sMenu.setTouchModeAbove(SlidingMenu.TOUCHMODE_FULLSCREEN);
        sMenu.setFadeDegree(0.35f);
        
        aq = new AQuery(this);
        
	}
	public boolean onKeyUp(int keyCode, KeyEvent event) {
	    if (keyCode == KeyEvent.KEYCODE_MENU) {
	    	toggle();
	        return true;
	    }
	    return super.onKeyUp(keyCode, event);
	}
	
	public boolean onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
 
        return super.onCreateOptionsMenu(menu);
    }
	
	public boolean onOptionsItemSelected(MenuItem item){
		
		String title = item.getTitle()+"";
		
		if(title.indexOf("Trafictube") >= 0)
			if(!sMenu.isMenuShowing())
				toggle();
		
		
		return true;
	}
	
	
	
	public void intreaba(){
		new AlertDialog.Builder(this)
	    .setTitle("Sunteți sigur?")
	    .setMessage("Doriți să închideți aplicația?")
	    .setPositiveButton("Da", new DialogInterface.OnClickListener() {
	        public void onClick(DialogInterface dialog, int which) { 
	        	finish();
	        }
	     })
	    .setNegativeButton("Nu", new DialogInterface.OnClickListener() {
	        public void onClick(DialogInterface dialog, int which) { 
	            // do nothing
	        }
	     })
	     .show();
	}
	
	@Override
	public void onBackPressed() {
		
		if(!sMenu.isMenuShowing())
			toggle();
		else
			intreaba();
			
		return;
	}
	
	public void load(final String link){ load(link, true);}
	public void load(final String link, boolean loading){
		
		if(loading)
			setContentViewM(R.layout.loading);
		
		if(!isOnline())
			setContentViewM(R.layout.offline);
		else
			new Thread(new Runnable() {
			    public void run() {
			    	
			    	changed = false;
			    	if(currentMode == R.id.menuButtonUltimeleClipuri)
			    		loadVideosUltimeleClipuri(link);
			    	else if(currentMode == R.id.menuButtonTopulSaptamanii)
			    		loadVideosTopulSaptamanii(link);
			    	else if(currentMode == R.id.menuButtonCauta)
			    		loadVideosUltimeleClipuri(link);
			    	else if(currentMode == R.id.menuButtonTop10Zile)
			    		loadVideosTop10Zile(link);
			    	else if(currentMode == R.id.menuButtonTop)
			    		loadVideosTop(link);
			    	else if(currentMode == R.id.menuButtonClipulZilei)
			    		clipulSidebar(0);
			    	else if(currentMode == R.id.menuButtonClipulLunii)
			    		clipulSidebar(1);
			    		
			    		
			    }
			}).start();
	}
	
	
	public void loadVideosTop10Zile(String link){
		loadTable(1);
	}
	
	public void loadVideosTop(String link){
		loadTable(0);
	}
	
	public void loadTable(final int care){
		List<video> lista = new ArrayList<video>();
		String link = "http://www.trafictube.ro/top-rated/";
		try {
			
			Document doc = Jsoup.connect(link).timeout(0).get();
			Elements tabele = doc.select("#continut .page table");

			Elements videos;
			if(care == 1)
				videos = tabele.last().select("tbody tr");
			else
				videos = tabele.first().select("tbody tr");
			
			for(Element video : videos){
				video v = new video();
				
				v.title = Jsoup.parse(video.select(".title a").html()).text();
				v.link = video.select(".title a").attr("href");
				v.votes = Integer.parseInt(video.select(".votes").html());
				v.rating = video.select(".rating").html();
				
				lista.add(v);
			}
			
			final List<video> parametru = lista;
			
			hasNext = false;
			currentLink = link;
			
			if(!changed)
				runOnUiThread(new Runnable() {
				     public void run() {
				    	 manipulateContentList(parametru);
				    }
				});
			
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void loadVideosTopulSaptamanii(String link){
		List<video> lista = new ArrayList<video>();
	
		try {
			
			Document doc = Jsoup.connect(link).timeout(0).get();
			Elements videos = doc.select("#top-saptamana .post");

			for(Element video : videos){
				video v = new video();
				
				String[] parts;
				
				
				v.title = Jsoup.parse(video.select("h3 a").attr("title")).text();
				v.link = video.select("a").first().attr("href");
				v.uploader = video.select(".post-meta-out a").last().html();
				v.image = video.select("img").attr("src");
				//UrlImageViewHelper.loadUrlDrawable(getBaseContext(), v.image);
				
				parts = video.select(".post-meta-out").html().split(" ");
				v.date = parts[0];
				
				lista.add(v);
			}
			
			final List<video> parametru = lista;
			
			hasNext = false;
			currentLink = link;
			
			if(!changed)
				runOnUiThread(new Runnable() {
				     public void run() {
				    	 manipulateContent(parametru);
				    }
				});
				
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
	
	public void loadVideosUltimeleClipuri(String link){
		List<video> lista = new ArrayList<video>();
	
		try {
			
			Document doc = Jsoup.connect(link).timeout(0).get();
			Elements select = doc.select("#continut .bloc.desp.grila");
			int pos = 0;
			if(select.size() >= 1)
				pos = 1;
			Elements videos = select.eq(pos).select(".post");

			for(Element video : videos){
				video v = new video();
				
				v.title = Jsoup.parse(video.select("h3 a").attr("title")).text();
				v.link = video.select("a").first().attr("href");
				
				String[] parts = video.select(".post-meta-out b a").html().split(" ");
				
				v.comments = Integer.parseInt(parts[0]);
				v.uploader = video.select(".post-meta-out a").last().html();
				v.image = video.select("img").attr("src");
				
				//UrlImageViewHelper.loadUrlDrawable(getBaseContext(), v.image);
				
				parts = video.select("h3").html().split(" ");
				
				v.date = parts[parts.length-1];
				
				lista.add(v);
			}
			
			final List<video> parametru = lista;
			
			currentLink = link;
			
			hasNext = false;
			if(parametru.size() > 20 )
			{
				if(doc.select(".wp-pagenavi").size()>0)
				{
					hasNext = true;
					
				}
			}
			
			System.out.println(changed);
			if(!changed)
				runOnUiThread(new Runnable() {
				     public void run() {
				    	 manipulateContent(parametru);
				    }
				});
			
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
	
	public View separator(){
		View ruler = new View(this);
		ruler.setBackgroundResource(R.color.gri);
		LinearLayout.LayoutParams x = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 2);
		ruler.setLayoutParams(x);
		
		return ruler;
	}
	
	public void next(PullToRefreshBase<ScrollView> refreshView){
		
		if(currentMode == R.id.menuButtonUltimeleClipuri)
		{
			makeToast("Așteaptă", 0);
			
			String link = currentLink;
			String parts[] = link.split("page/");
			
			int page = 2;
			if(parts.length > 1)
			{
				parts = parts[1].split("/");
				page = Integer.parseInt(parts[0]) + 1;
				
			}
			
			load("http://www.trafictube.ro/page/"+page+"/", false);
		}else if(currentMode == R.id.menuButtonCauta)
		{
			makeToast("Așteaptă", 0);
			
			String link = currentLink;
			String parts[] = link.split("page/");
			String[] parts2 = null;
			
			int page = 2;
			if(parts.length > 1)
			{
				parts2 = parts[1].split("/");
				page = Integer.parseInt(parts2[0]) + 1;
				
			}
			
			load("http://www.trafictube.ro/page/"+page+"/"+parts2[1], false);
		}
		else
			refreshView.onRefreshComplete();
		
	}
	
	public void manipulateContent(List<video> lista){
		setContentViewM(R.layout.activity_main);
		
		PullToRefreshScrollView mPullRefreshScrollView = (PullToRefreshScrollView) findViewById(R.id.scrollViewMain);
		
		
		System.out.println(hasNext);
		System.out.println("test");
		if(hasNext){
			mPullRefreshScrollView.setMode(Mode.PULL_FROM_END);
		}else
			mPullRefreshScrollView.setMode(Mode.DISABLED);
		
		mPullRefreshScrollView.setOnRefreshListener(new OnRefreshListener<ScrollView>() {

			@Override
			public void onRefresh(PullToRefreshBase<ScrollView> refreshView) {
				// TODO Auto-generated method stub
				next(refreshView);
			}
		});

		
		View ruler;
		
		LinearLayout layout = (LinearLayout) findViewById(R.id.contentView);
		
		int i = 1;
		
		ruler = separator();
		
		LinearLayout twoVideos = new LinearLayout(this);
		twoVideos.setOrientation(LinearLayout.HORIZONTAL);
		twoVideos.setWeightSum(1.0f);
		
		for(video v : lista)
		{
           
            LinearLayout videoLayout = (LinearLayout) getLayoutInflater().inflate(R.layout.video_layout, twoVideos, false);
            
            videoLayout.setContentDescription(v.link);
            
            TextView txt = (TextView) videoLayout.getChildAt(2);
            txt.setText(v.title);
            
            txt = (TextView) videoLayout.getChildAt(0);
            txt.setText(v.uploader);
            
            ImageView img = (ImageView) videoLayout.getChildAt(1);
            aq.id(img).image(v.image);
            
			twoVideos.addView(videoLayout);
			
			if(i%2==0)
			{
				layout.addView(twoVideos);
				twoVideos = new LinearLayout(this);
				twoVideos.setOrientation(LinearLayout.HORIZONTAL);
				twoVideos.setWeightSum(1.0f);
				
				ruler = separator();
				layout.addView(ruler);
				
			}
			i++;
		}
		
		if(i%2==0)
		{
			layout.addView(twoVideos);
			ruler = separator();
			layout.addView(ruler);
			
		}
		
	}
	
	public void manipulateContentList(List<video> lista){
		setContentViewM(R.layout.activity_main);
		
		PullToRefreshScrollView mPullRefreshScrollView = (PullToRefreshScrollView) findViewById(R.id.scrollViewMain);
		mPullRefreshScrollView.setMode(Mode.DISABLED);
		
		LinearLayout layout = (LinearLayout) findViewById(R.id.contentView);
		
		LinearLayout videoLayout = (LinearLayout) getLayoutInflater().inflate(R.layout.video_layout_list, layout, false);
        ViewGroup view = (ViewGroup) videoLayout.getChildAt(0);
        TextView txt = (TextView) view.getChildAt(0);
        txt.setText("# Titlu");
        txt = (TextView) view.getChildAt(1);
        txt.setText("Voturi");
        txt = (TextView) view.getChildAt(2);
        txt.setText("Rating");
		layout.addView(videoLayout);
		
		int i = 1;
		for(video v : lista)
		{
			videoLayout = (LinearLayout) getLayoutInflater().inflate(R.layout.video_layout_list, layout, false);
            
            view = (ViewGroup) videoLayout.getChildAt(0);
            view.setContentDescription(v.link);
            
            txt = (TextView) view.getChildAt(0);
            txt.setText(i+". "+v.title);
            
            txt = (TextView) view.getChildAt(1);
            txt.setText(v.votes+"");
            
            txt = (TextView) view.getChildAt(2);
            txt.setText(v.rating);
            
			i++;
			
			layout.addView(videoLayout);
		}
				
	}
	
	public void contentCauta(){
		
		currentMode = R.id.menuButtonCauta;
		
		setContentViewM(R.layout.search);
		
		aq.id(R.id.buttonSearch).clicked(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				String text = aq.id(R.id.editTextSearch).getText()+"";
				
				load("http://www.trafictube.ro/page/1/?s=" + URLEncoder.encode(text));
			}
		});
		
	}
	
	public void clipulSidebar(final int care){
		try {
					
			Document doc = Jsoup.connect(siteLink).get();
			Elements sidebar = doc.select(".lista-mica");
			
			
			Elements video = sidebar.eq((care+1));
			
			video v = new video();
			
			String[] parts;
			
			
			v.title = Jsoup.parse(video.select("h3 a").attr("title")).text();
			v.link = video.select("h3 a").first().attr("href");
			v.uploader = video.select(".post-meta-out a").last().html();
			v.image = video.select("img").attr("src");
			//UrlImageViewHelper.loadUrlDrawable(getBaseContext(), v.image);
			
			parts = video.select(".post-meta-out").html().split(" ");
			v.date = parts[0];
			
			
			hasNext = false;
			currentLink = siteLink;
			
			openVideo(v.link);
			
			runOnUiThread(new Runnable() {
			     public void run() {
			    	 setContentViewM(R.layout.trafictube);
			    }
			});
			
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		    	
	}
	
	public void openMenu(View view){
		toggle();
	}
	
	public void showDespre(View view){
		setContentViewM(R.layout.despre);
	}
	
	public void changeDespre(View view){
		String t = "";
		
		switch(view.getId())
		{
		case R.id.buttonDespreDespreNoi:
			t = getString(R.string.despre_noi_text);
		break;
		case R.id.buttonDespreTermeniSiConditii:
			t = getString(R.string.termeni_si_conditii_text);
		break;
		case R.id.buttonDespreReguliComentarii:
			t = getString(R.string.reguli_comentarii_text);
		break;
		
		}
		
		if(t=="")
		{
			setContentViewM(R.layout.despre_aplicatie);
		}
		else
		{
			setContentViewM(R.layout.despre_text);
			aq.id(R.id.textViewDespreText).text(t);
		}
	}
	
	public void openVideoClick(View view){
		openVideo(view.getContentDescription()+"");
	}
	
	public void openVideo(String link){
		if(link.length()>1)
		{
			Intent i = new Intent(this, videoActivity.class);
			i.putExtra("link", link);
			startActivity(i);
			overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
		}
		else
			makeToast("Linkul nu este bun", 0);
	}
	
	public void menuPress(View view){
		
		changed = true;
		
		
		switch(view.getId())
		{
		case R.id.menuButtonDespre:
			currentMode = R.id.menuButtonDespre;
			showDespre(new View(getApplicationContext()));
		break;
		
		case R.id.menuButtonCauta:
			currentMode = R.id.menuButtonCauta;
			contentCauta();
			
			break;
		
		case R.id.menuButtonClipulLunii:
			currentMode = R.id.menuButtonClipulLunii;
			
			setContentViewM(R.layout.loading);
			
			load("test");
			
			break;
		
		case R.id.menuButtonClipulZilei:
			currentMode = R.id.menuButtonClipulZilei;
			
			setContentViewM(R.layout.loading);
			
			load("test");
			
			break;
		
		case R.id.menuButtonLogo:
			currentMode = R.id.menuButtonLogo;
			
			setContentViewM(R.layout.trafictube);
			
			break;
			
		case R.id.menuButtonTop:
			currentMode = R.id.menuButtonTop;
			
			load("http://www.trafictube.ro/top-rated/");
			
			break;
			
		case R.id.menuButtonTop10Zile:
			currentMode = R.id.menuButtonTop10Zile;
			
			load("http://www.trafictube.ro/top-rated/");
			
			break;
			
		case R.id.menuButtonTopulSaptamanii:
			currentMode = R.id.menuButtonTopulSaptamanii;
			
			load("http://trafictube.ro/page/1/");
			
			break;
		
		case R.id.menuButtonUltimeleClipuri:
			
			currentMode = R.id.menuButtonUltimeleClipuri;
			
			load("http://www.trafictube.ro/page/1/");
			
			break;
		}
		
		if(sMenu.isMenuShowing())
			toggle();
			
		
	}
	
	public void makeToast(String text){ makeToast(text, 0); }
	public void makeToast(String text, int len){
		int length;
		if(len==0)
			length = Toast.LENGTH_SHORT;
		else
			length = Toast.LENGTH_LONG;
		
		Toast.makeText(getApplicationContext(), text, length).show();
	}
	
	public boolean isOnline() {
	    ConnectivityManager cm =
	        (ConnectivityManager) getSystemService(getApplicationContext().CONNECTIVITY_SERVICE);
	    NetworkInfo netInfo = cm.getActiveNetworkInfo();
	    if (netInfo != null && netInfo.isConnectedOrConnecting()) {
	        return true;
	    }
	    return false;
	}
	
	
	public void setContentViewM(final int id)
	{
		final View root = getWindow().getDecorView().findViewById(android.R.id.content);

		final Animation animFadein = AnimationUtils.loadAnimation(getApplicationContext(),
                R.anim.fade_in);      
		
		animFadein.setFillAfter(true);

		setContentView(id);
		root.startAnimation(animFadein);

	}

}
