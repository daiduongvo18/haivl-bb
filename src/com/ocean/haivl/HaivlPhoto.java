package com.ocean.haivl;

import java.util.Vector;
import com.ocean.util.DecomposeUnicode;

public class HaivlPhoto {
	private String _title;
	private String _url;
	private int _id;
	private String _viewCount;
	private String _commentCount;	
	
	public HaivlPhoto(){
		
	}
	
	public HaivlPhoto(String title, String url, String viewCount, String commentCount){
		this._title = DecomposeUnicode.getUnicodeComposeString(title);
		this._url = url;
		this._viewCount = viewCount;
		this._commentCount = commentCount;
	}
	
	public void setId(int id){
		this._id = id;
	}
	
	public int getId(){
		return this._id;
	}
	
	public void setTitle(String title){
		this._title = DecomposeUnicode.getUnicodeComposeString(title);
	}
	
	public String getTitle(){
		return this._title;
	}
	
	public void setUrl(String url){
		this._url = url;
	}
	
	public String getUrl(){
		return this._url;
	}
	
	public void setViewCount(String count){
		this._viewCount = count;
	}
	
	public String getViewCount(){
		return this._viewCount;
	}
	
	public void setCommentCount(String count){
		this._commentCount = count;
	}
	
	public String getCommentCount(){
		return this._commentCount;
	}
	
	public boolean isYoutube()
	{		
		if(_url == null){
			return false;
		}
			
		if(_url.indexOf("img.youtube.com") > -1){
			return true;
		}
		return false;
	}
	
	public String getYoutubeId(){		
		if(_url == null){
			return "";
		}
		
		String[] tmp = split(_url, "/");
		return tmp[4];
	}
	
	private String[] split(String original, String separator) {
        Vector nodes = new Vector();
        int index = original.indexOf(separator);
        while (index >= 0) {
            nodes.addElement(original.substring(0, index));
            original = original.substring(index + separator.length());
            index = original.indexOf(separator);
        }
        nodes.addElement(original);
        String[] result = new String[nodes.size()];
        if (nodes.size() > 0) {
            for (int loop = 0; loop < nodes.size(); loop++) {
                result[loop] = (String) nodes.elementAt(loop);
                System.out.println(result[loop]);
            }
        }
        return result;
    }
}
