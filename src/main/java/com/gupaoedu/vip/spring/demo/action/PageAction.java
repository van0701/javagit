package com.gupaoedu.vip.spring.demo.action;



import com.gupaoedu.vip.spring.demo.service.IQueryService;
import com.huyabin.spring.fromework.annotation.GPAutowired;
import com.huyabin.spring.fromework.annotation.GPController;
import com.huyabin.spring.fromework.annotation.GPRequestMapping;
import com.huyabin.spring.fromework.annotation.GPRequestParam;
import com.huyabin.spring.fromwork.webmvc.GPModelAndView;

import java.util.HashMap;
import java.util.Map;

/**
 * 公布接口url
 * @author Tom
 *
 */
@GPController
@GPRequestMapping("/")
public class PageAction {

	@GPAutowired
	IQueryService queryService;
	
	@GPRequestMapping("/first.html")
	public GPModelAndView query(@GPRequestParam("teacher") String teacher){
		String result = queryService.query(teacher);
		Map<String,Object> model = new HashMap<String,Object>();
		model.put("teacher", teacher);
		model.put("data", result);
		model.put("token", "123456");
		return new GPModelAndView("first.html",model);
	}
	
}
