package com.bus.chelaile.flowNew;

import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.bus.chelaile.common.CacheUtil;
import com.bus.chelaile.common.Constants;
import com.bus.chelaile.flow.ToutiaoHelp;
import com.bus.chelaile.flow.model.ActivityEntity;
import com.bus.chelaile.flow.model.FlowContent;
import com.bus.chelaile.flowNew.model.ActivityType;
import com.bus.chelaile.flowNew.model.FlowNewContent;
import com.bus.chelaile.model.client.ClientDto;
import com.bus.chelaile.mvc.AdvParam;
import com.bus.chelaile.util.New;

public class FeedService {
	@Autowired
	private ToutiaoHelp toutiaoHelp;
	
	protected static final Logger logger = LoggerFactory.getLogger(FeedService.class);
	
	/**
	 * 详情页下方 feed 流 。3.0 版
	 * @param param
	 * @return
	 */
	public String getResponseLineDetailFeeds(AdvParam param) {
		// 单栏信息流
		List<FlowNewContent> flows = getLineDetailFeeds(param);
		if (flows != null && flows.size() > 0) {
			JSONObject responseJ = new JSONObject();
			responseJ.put("flows", flows);
			return getClienSucMap(responseJ, Constants.STATUS_REQUEST_SUCCESS);
		} else {
			return getClienSucMap(new JSONObject(), Constants.STATUS_REQUEST_SUCCESS);
		}
	}

	/**
	 * 获取详情页下方滚动栏内容
	 * 
	 * @return
	 */
	public List<FlowNewContent> getLineDetailFeeds(AdvParam param) {
		List<FlowNewContent> flows = New.arrayList();
		// 条件判断
		if (FlowStaticContents.isReturnLineDetailFlows(param)) {
			createList(param, flows);
		}
		return flows;
	}

	/*
	 * 可能涉及到一些链接增加用户id之类的修正
	 */
	private void createList( AdvParam param, List<FlowNewContent> flows) {

		List<FlowNewContent> flowTagDetail = New.arrayList();
		List<FlowNewContent> flowActivity = New.arrayList();
		List<FlowNewContent> flowGame = New.arrayList();
		List<FlowNewContent> flowArticle = New.arrayList();
		List<FlowNewContent> flowWeal = New.arrayList();
		
		String key;
		String lineFlowsStr ;
		
		key = "QM_LINEDETAIL_FLOW_" + 7;	// 话题详情页
		lineFlowsStr = (String) CacheUtil.getNew(key);
		if (StringUtils.isNoneBlank(lineFlowsStr)) {
			flowTagDetail = JSON.parseArray(lineFlowsStr, FlowNewContent.class);
		}
		
		
		key = "QM_LINEDETAIL_FLOW_" + 1;	 //普通活动
		lineFlowsStr = (String) CacheUtil.getNew(key);
		if (StringUtils.isNoneBlank(lineFlowsStr)) {
			flowActivity = JSON.parseArray(lineFlowsStr, FlowNewContent.class);
		}
		
		key = "QM_LINEDETAIL_FLOW_" + 10;	// 游戏
		lineFlowsStr = (String) CacheUtil.getNew(key);
		if (StringUtils.isNoneBlank(lineFlowsStr)) {
			flowGame = JSON.parseArray(lineFlowsStr, FlowNewContent.class);
		}
		
		List<FlowContent> flowsApi;
		try {
//			flowsApi = wuliToutiaoHelp.getArticlesFromCache(null, null, -1);
			flowsApi = toutiaoHelp.getInfoByApi(param, 0L, null, -1, false);
			for(FlowContent f : flowsApi) {
				flowArticle.add(f.createFeeds());
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		key = "QM_LINEDETAIL_FLOW_" + 11;	// 福利
		lineFlowsStr = (String) CacheUtil.getNew(key);
		if (StringUtils.isNoneBlank(lineFlowsStr)) {
			flowWeal = JSON.parseArray(lineFlowsStr, FlowNewContent.class);
		}
		
		
		for(int index = 0; index < FlowStartService.LINEDETAIL_NUM; index ++) {
			addIntoFlows(flowTagDetail, flows, index, param);
			addIntoFlows(flowActivity, flows, index, param);
			addIntoFlows(flowGame, flows, index, param);
			addIntoFlows(flowArticle, flows, index, param);
			addIntoFlows(flowWeal, flows, index, param);
		}
		
		// TODO 手动添加活动内容
//		FlowNewContent f = new FlowNewContent();
//		f.setDestType(1);
//		f.setFlowTitle("活动标题---");
//		f.setFlowTag("这活动厉害了");
//		f.setFlowTagColor("52,152,219");
//		f.setFlowDesc("12312人参与");
//		f.setPic("https://image3.chelaile.net.cn/a6f96bcf5ee742d7aa732259c32d1b8c");
//		f.setLink("http://www.baidu.com");
//		
//		ActivityEntity ac = new ActivityEntity();
//		ac.setLinkUrl("http://www.baidu.com");
//		ac.setImageUrl("https://image3.chelaile.net.cn/a6f96bcf5ee742d7aa732259c32d1b8c");
//		ac.setType(ActivityType.H5.getType());
//		ac.setImageUrl("");
//		f.setActivityEntity(ac);
		
//		flows.add(f);
	}
	
	

	private void addIntoFlows(List<FlowNewContent> flowElement, List<FlowNewContent> flows, int index, AdvParam param) {
		if(index < flowElement.size()) {
			FlowNewContent flow = new FlowNewContent();
			flow.deal(flowElement.get(index), param);
			if(flow.getDestType() == 7) {	 // 话题详情页的标志type，客户端识别为0
				flow.setDestType(0);
			}
			flows.add(flow);
		}
	}
	
	public String getClienSucMap(Object obj, String status) {
		ClientDto clientDto = new ClientDto();
		clientDto.setSuccessObject(obj, status);
		try {
			String json = JSON.toJSONString(clientDto, SerializerFeature.BrowserCompatible);
			return "**YGKJ" + json + "YGKJ##";
		} catch (Exception e1) {
			logger.error(e1.getMessage(), e1);
			return "";
		}
	}

	public String getClientErrMap(String errmsg, String status) {
		ClientDto clientDto = new ClientDto();
		clientDto.setErrorObject(errmsg, status);
		try {
			String json = JSON.toJSONString(clientDto);
			return "**YGKJ" + json + "YGKJ##";
		} catch (Exception e1) {
			logger.error(e1.getMessage(), e1);
			return "";
		}
	}
	
}