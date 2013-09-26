package com.micutu.trafictube;


import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.androidquery.AQuery;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.text.Html;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

public class videoActivity extends Activity {

	private AQuery aq;
	private String link;
	private String videoLink = "";
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
    	setContentView(R.layout.loading);
		
		Bundle b = getIntent().getExtras();
		
		if(b!=null)
			link = b.getString("link");
		
		loadVideo();
        
        aq = new AQuery(this);
        
        //makeToast(link, 0);
	}
	
	public void loadVideo(){
		
		new Thread(new Runnable() {
		    public void run() {
		    	
				try {
					
					Document doc = Jsoup.connect(link).get();
					Element post = doc.select("#continut .post").get(0);
					
					String[] parts;
					
					String title = Jsoup.parse(post.select("h1").get(0).html()).text();
					
					Elements descrieri = post.select(".post-meta-out b");
					
					String descriere = "";
					
					int i = 0;
					for(Element x : descrieri)
					{
						if(i==0)
							descriere = Jsoup.parse(x.html()).text()+"\n";
						if(i==1)
							descriere += "Filmat de "+Jsoup.parse(x.select("a").html()).text()+"\n";
						if(i==2)
							descriere += "Filmează cu "+Jsoup.parse(x.html()).text();
						
						i++;
					}
					
					
					String webUrl = post.select(".video-player iframe").attr("src");
					
					String voturi_pozitive = "+"+post.select(".vote-positive").html();
					String voturi_negative = "-"+post.select(".vote-negative").html();
					
					String comentarii;
					
					if(post.select("#comments").html().length()>1)
					{
						parts = post.select("#comments h3").html().split(" ");
						comentarii = parts[0];
					}
					else
						comentarii = "0";
					
					List<comentariu> lista = new ArrayList<comentariu>();
					
					if(comentarii != "0")
					{
						Elements cc = post.select(".comment");
						for(Element c : cc)
						{
							comentariu comment = new comentariu();
							
							if(c.select(".comment-author .fn a").isEmpty())
								comment.nume = c.select(".comment-author .fn").html();
							else
								comment.nume = c.select(".comment-author .fn a").html();
							comment.data = c.select(".comment-author .comment-meta a").html();
							comment.comentariu = c.select(".comment-text p").html();
							
							lista.add(comment);
						}
					}
					
					final String titlu = title;
					final String description = descriere;
					videoLink = webUrl;
					final String pozitive = voturi_pozitive;
					final String negative = voturi_negative;
					final String comments = comentarii;
					final List<comentariu> comentariiList = lista;
					
					runOnUiThread(new Runnable() {
					    public void run() {
					    	setContentView(R.layout.single_video);
					    	
					    	aq.id(R.id.textViewVideoTitle).text(titlu);
					    	aq.id(R.id.textViewVideoDetail).text(description);
					    	
					    	aq.id(R.id.textViewVoturiPozitive).text(pozitive);
					        aq.id(R.id.textViewVoturiNegative).text(negative);
					        
					        aq.id(R.id.textViewComentarii).text(comments+" comentarii");
					        
					        LinearLayout layout = (LinearLayout) findViewById(R.id.layoutComentarii);
					        
					        for(comentariu c : comentariiList)
					        {
					        	LinearLayout comentariuLayout = (LinearLayout) getLayoutInflater().inflate(R.layout.comentarii, layout, false);
					            
					            ViewGroup view = (ViewGroup) comentariuLayout.getChildAt(0);
					            
					            TextView txt = (TextView) view.getChildAt(0);
					            txt.setText(c.nume);
					            
					            txt = (TextView) view.getChildAt(1);
					            txt.setText(c.data);
					            
					            txt = (TextView) comentariuLayout.getChildAt(1);
					            txt.setText(Html.fromHtml(c.comentariu));
					            
								layout.addView(comentariuLayout);
					        }
					        
					    }
					});
					
					
				} catch (IOException e) {
					e.printStackTrace();
				}
		    }
		}).start();
		
	}
	
	
	public void playVideo(View view){
		if(videoLink.length() > 1)
		{
			String[] parts = videoLink.split("youtube");
			if(parts.length > 1)
			{
				String id;
				parts = videoLink.split("/embed/");
				id = parts[1];
				
				parts = id.split("\\?");
				id = parts[0];
				
				makeToast("Pornesc clipul", 0);
				
				try{
			     Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("vnd.youtube:" + id));
			     startActivity(intent);                 
			     }catch (ActivityNotFoundException ex){
			         Intent intent=new Intent(Intent.ACTION_VIEW, 
			         Uri.parse("http://www.youtube.com/watch?v="+id));
			         startActivity(intent);
			     }
			}
			
			
			parts = videoLink.split("vimeo");
			if(parts.length > 1){
				makeToast("Așteaptă puțin");
				
				String id = null;
				parts = null;
				
				parts = videoLink.split("/video/");
				id = parts[1];
				
				parts = id.split("\\?");
				id = parts[0];
				
				final String linkul = "http://vimeo.com/m/"+id;
				
				System.out.println(linkul);
				
				new Thread(new Runnable() {
				    public void run() {
				    	
						try {
							
							Document doc = Jsoup.connect(linkul).get();
							Element x = doc.select("#cu").get(0);
							
							String l = x.attr("src");
							
							Intent viewMediaIntent = new Intent();   
							viewMediaIntent.setAction(android.content.Intent.ACTION_VIEW);   
							viewMediaIntent.setDataAndType(Uri.parse(l), "video/*");   
							viewMediaIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP);
							startActivity(viewMediaIntent);        
							
						} catch (IOException e) {
							e.printStackTrace();
						}
				    }
				}).start();
			}
		}
	}
	
	public void openBrowser(View view){
		Intent intent=new Intent(Intent.ACTION_VIEW, 
		 Uri.parse(link));
		 startActivity(intent);
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		return true;
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

}
