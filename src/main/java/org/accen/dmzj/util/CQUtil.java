package org.accen.dmzj.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Base64;
import java.util.Base64.Decoder;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.tomcat.util.http.fileupload.IOUtils;

import ch.qos.logback.core.pattern.parser.Parser;

public class CQUtil {
	
	/**
	 * at的CQ码
	 * @param targetQq
	 * @return
	 */
	public static String at(String targetQq) {
		return "[CQ:at,qq="+targetQq+"]";
	}
	/**
	 * 发送音乐分享的CQ码
	 * @param type qq/163/xiami
	 * @param id
	 * @return
	 */
	public static String music(String type,String id) {
//		return "[CQ:music,id="+id+",type="+type+"]";
		return "[CQ:music,id="+id+",type=163]";
	}
	/**
	 * 发送音乐分享的CQ码，但是会通过名字去匹配<br>
	 * 暂时使用各个应用提供的搜索功能去匹配第一条，未匹配到返回空串同时记录
	 * @param type
	 * @param wd
	 * @return
	 */
	public static String musicFuzzy(String type,String wd) {
		//https://music.163.com/#/search/m/?s=a&type=1
		return "";
	}
	public static String selfMusic(String url,String audio,String title,String content,String imageUrl) {
		return "[CQ:music,type=custom,url="+url+",audio="+audio+",title="+title+",content="+content+",image="+imageUrl+"]";
	}
	public static String image(String url) {
		return "[CQ:image,file="+url+"]";
	}
	public static String imageUrl(String url) {
		return "[CQ:image,cache=0,file="+url+"]";
	}
	public static String imageBs64(String encodedStr) {
		return "[CQ:image,file=base64://"+encodedStr+"]";
	}
	public static String imageUrlBase64(InputStream is) {
		String bs64 = StringUtil.is2Base64(is);
		if(bs64!=null) {
			return "[CQ:image,file=base64://"+bs64+"]";
		}else {
			return null;
		}
	}
	public static String imageUrlBase64(File file) {
		try {
			return imageUrlBase64(new FileInputStream(file));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		return null;
	}
	/**
	 * 网络语音
	 * @param url
	 * @return
	 */
	public static String recordUrl(String url) {
		return "[CQ:record,cache=0,file="+url+"]";
	}
	/**
	 * 截取at之后的字符串
	 * @param str
	 * @param selfNum
	 * @return
	 */
	public static String subAtAfter(String str,String selfNum) {
		Pattern pattern  = Pattern.compile("^\\[CQ:at,qq="+selfNum+"\\](.*)");
		Matcher mt = pattern.matcher(str);
		if(mt.matches()) {
			return mt.group(1);
		}
		return null;
	}
	
	private final static Pattern patternCq = Pattern
			.compile("\\[CQ\\:image,file=(.*?),url=(.*?)\\]");
	/**
	 * 抽取cq码中的图片url
	 * @param cqImg
	 * @return
	 */
	public static String grepImageUrl(String cqImg) {
		Matcher matcher = patternCq.matcher(cqImg);
		if(matcher.matches()) {
			return matcher.group(2);
		}
		return null;
	}
	/**
	 * 链接分享
	 * @param url
	 * @param title
	 * @param content
	 * @param image
	 * @return
	 */
	public static String share(String url,String title,String content,String image) {
		return "[CQ:share,url="+url+",title="+title+",content="+content+",image="+image+"]";
	}
	private final static Pattern patternCqImg = Pattern
			.compile(".*?\\[CQ\\:image,.*?\\].*");
	/**
	 * 判断是否含有图片
	 * @param str
	 * @return
	 */
	public static boolean hasImg(String str) {
		return patternCqImg.matcher(str).matches();
	}
	
}
