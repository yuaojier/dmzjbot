package org.accen.dmzj.core.task;

import java.util.HashMap;
import java.util.Map;

import org.accen.dmzj.core.task.api.CqhttpClient;
import org.accen.dmzj.util.ApplicationContextUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;

public class TaskCoolqProcessor {
	private final static Logger logger = LoggerFactory.getLogger(TaskCoolqProcessor.class);
	public String processs(GeneralTask task) {
		if("group".equals(task.getType())) {
			Map<String, Object> resultMap = sendGroupMsg(task.getTargetId(),task.getMessage(),false);
			return new Gson().toJson(resultMap);
		}
		return "";
	}
	public Map<String, Object> sendGroupMsg(String groupId,String message,boolean autoEscape) {
		Map<String, Object> request = new HashMap<String, Object>();
		request.put("group_id", groupId);
		request.put("message", message);
		request.put("auto_escape", autoEscape);
		CqhttpClient client = ApplicationContextUtil.getBean(CqhttpClient.class);
		try {
			Map<String, Object> rs = client.sendGroupMsg(request);
			return rs;
		}catch (Exception e) {
			// TODO 暂时不处理
			logger.error(e.getMessage());
		}
		
		return null;
	}
}
