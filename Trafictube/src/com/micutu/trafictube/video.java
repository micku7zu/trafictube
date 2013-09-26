package com.micutu.trafictube;

public class video {
	public String title, uploader, link, image, date, rating;
	public int comments, votes;
	
	
	public video(){
		
	}
	
	public video(String title, String uploader, String link, String image, String Date, int comments){
		this.title = title;
		this.uploader = uploader;
		this.link = link;
		this.image = image;
		this.date = date;
		this.comments = comments;
	}
}
