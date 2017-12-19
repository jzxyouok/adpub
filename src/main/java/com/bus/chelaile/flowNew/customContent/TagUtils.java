/**
 * @author quekunkun
 *
 */
package com.bus.chelaile.flowNew.customContent;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.bus.chelaile.common.Constants;
import com.bus.chelaile.flowNew.FlowStartService;
import com.bus.chelaile.flowNew.model.FlowNewContent;
import com.bus.chelaile.util.HttpUtils;

public class TagUtils {
	
	protected static final Logger logger = LoggerFactory.getLogger(TagUtils.class);
	private static final String TAGS＿LINK = "http://api.chelaile.net.cn:7000/feed/native!tags.action?cityId=027";   // TODO　城市有用否？
	private static final String FEED_LINK_TEST = "https://dev.chelaile.net.cn/feed/native!feeds.action?feedCityId=-1";
	private static final String FEED_LINK_ONLINE = "https://api.chelaile.net.cn/feed/native!feeds.action?feedCityId=-1";
	/**
	 * 获取话题标签
	 * @param init
	 */
	public static void getInitTags(List<FlowNewContent> initFlows) {
		String url = TAGS＿LINK;
		String response = null;
		try{
			response = HttpUtils.get(url, "utf-8");
		} catch(Exception e) {
			logger.error("拉取tag列表出错， url={}, response={}", url, response);
			return;
		}
		
		if(response != null && response.length() > 12) {
			String resJ = response.substring(6, response.length() - 6);
//			System.out.println(resJ);
			JSONObject res = JSONObject.parseObject(resJ);
			JSONArray tags = res.getJSONObject("jsonr").getJSONObject("data").getJSONArray("tags");
			for(int i = 0; i < tags.size() && i < FlowStartService.LINEDETAIL_NUM; i++ ) {		 // TODO　随机取3条，获取的随机方法有待完善
				createTagFlow(initFlows, (JSONObject) tags.get(i));
			}
			logger.info("详情页下方滚动栏，话题标签数为：{}", tags.size());
		}
	}
	
	private static void createTagFlow(List<FlowNewContent> initFlows, JSONObject object) {
		FlowNewContent f = new FlowNewContent();
		f.setDestType(0);
		f.setFlowTitle(object.getString("tag"));
		f.setFlowTag("热门话题");
		f.setFlowTagColor("255,175,0");
		f.setFlowIcon("https://image3.chelaile.net.cn/4c860c68c14d468b90f29974f036bf96");
		f.setTag(object.getString("tag"));
		f.setTagId(String.valueOf(object.getIntValue("tagId")));
		f.setFeedId("633123838082781184");
		f.setFlowDesc("1234人参与");
		f.setPic("https://image3.chelaile.net.cn/4c860c68c14d468b90f29974f036bf96");
		
		initFlows.add(f);
	}
	
	
	
	/**
	 * 获取话题详情页
	 * @param init
	 * TODO 待完善
	 */
	public static void getInitTagDetails(List<FlowNewContent> initFlows) {
		String url = FEED_LINK_ONLINE;
		if(Constants.ISTEST) {
			url = FEED_LINK_TEST;
		}
		
		String response = null;
		try{
			response = HttpUtils.get(url, "utf-8");
		} catch(Exception e) {
			logger.error("拉取tag详情页列表出错， url={}, response={}", url, response);
			return;
		}
		
		if(response != null && response.length() > 12) {
			String resJ = response.substring(6, response.length() - 6);
			JSONObject res = JSONObject.parseObject(resJ);
			JSONArray feedsJ = res.getJSONObject("jsonr").getJSONObject("data").getJSONArray("feeds");
			for(int count = 0, index = 0; index < feedsJ.size() && count < FlowStartService.LINEDETAIL_NUM; index++ ) {		 // TODO　随机取3条，获取的随机方法有待完善
				JSONObject feedJ =  (JSONObject) feedsJ.get(index);
				JSONArray imagesJ = feedJ.getJSONArray("images");
				if(imagesJ == null || imagesJ.size() == 0) {
					continue;
				}
				createTagDetetailFlow(initFlows,feedJ);
				count ++ ;
			}
			logger.info("详情页下方滚动栏，话题详情页数为：{}", feedsJ.size());
		}
	}

	private static void createTagDetetailFlow(List<FlowNewContent> initFlows, JSONObject feedJ) {
		FlowNewContent f = new FlowNewContent();
		f.setDestType(7);
		f.setFlowTag(feedJ.getString("tag"));
		f.setFlowTitle(feedJ.getString("content"));
		f.setFlowTagColor("255,130,165");
		f.setFeedId(feedJ.getString("fid"));
		f.setPic(((JSONObject)feedJ.getJSONArray("images").get(0)).getString("picUrl"));
		f.setFlowDesc("1234人参与");
		
		initFlows.add(f);
	}
}