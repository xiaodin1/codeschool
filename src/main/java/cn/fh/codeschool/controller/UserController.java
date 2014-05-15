package cn.fh.codeschool.controller;

import java.util.ArrayList;
import java.util.List;

import javax.json.Json;
import javax.json.JsonObject;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import cn.fh.codeschool.model.CourseProgressWrapper;
import cn.fh.codeschool.model.Member;
import cn.fh.codeschool.model.RecentActivity;
import cn.fh.codeschool.service.AccountService;
import cn.fh.codeschool.service.CourseService;

@Controller
public class UserController {
	private static final int ACTIVITY_AMOUNT = 5;
	private static final Logger logger = LoggerFactory.getLogger(UserController.class);
	
	@Autowired
	private AccountService accountService;
	
	@Autowired
	private CourseService courseService;


	/**
	 * 为用户点赞
	 * @param username
	 * @return
	 */
	@RequestMapping(value = "/user/{username}/thumbUp", method = RequestMethod.GET, produces = "application/json; charset=utf-8")
	public @ResponseBody String thumbbUp(@PathVariable String username) {
		JsonObject json = null;
		Integer thumbs = -1;
		try {
			Member m = accountService.findMember(username);
			thumbs = m.thumbUp();
			accountService.saveMember(m);
		} catch (Exception e) {
			e.printStackTrace();
			logger.info("赞失败!!");
			
			json = Json.createObjectBuilder()
					.add("result", false)
					.add("thumbs", thumbs)
					.build();
		}
		
		json = Json.createObjectBuilder()
				.add("result", true)
				.add("thumbs", thumbs)
				.build();
		
		return json.toString();
	}
	
	/**
	 * 响应添加好友请求
	 * @return
	 */
	@RequestMapping(value = "/user/{username}/addFriend", method = RequestMethod.GET)
	public String addFriend(@PathVariable String username, HttpSession session) {
		Member targetMember = accountService.findMember(username);
		
		// 防止恶意用户自行发送请求
		if (null == targetMember) {
			return "redirect:/error/not-exist";
		}
		
		Member m = (Member)session.getAttribute("currentUser");
		m.getFriendList().add(targetMember);
		targetMember.setParent(m);
		accountService.saveMember(targetMember);
		//accountService.saveMember(m);
		
		

		return "redirect:/user/" + username + "/profile";
	}
	
	@RequestMapping(value = "/user/{username}/profile", method = RequestMethod.GET)
	public String showPage(@PathVariable String username, HttpServletRequest req, Model model) {
		Member m = accountService.findMember(username);
		
		// 用户名不存在,返回错误页面
		if (null == m) {
			return "redirect:/error/not-exist";
		}

		// 更新排名信息
		accountService.updateRank(m);

		// 将member放入model中
		model.addAttribute("member", m);
		
		// 如果用户已登陆,判断页面上的用户是否是当前用户的好友
		boolean isFriend = false;
		HttpSession session = req.getSession(false);
		if (null != session) {
			Member currentUser = (Member)session.getAttribute("currentUser");
			if (null != currentUser) {
				isFriend = currentUser.hasFriend(username);
			}
		}
		model.addAttribute("isFriend", isFriend);
		
		
		
		// 得到用户正在学习的课程
		List<CourseProgressWrapper> wrapperList = fetchStartedCourseList(m);
		model.addAttribute("startedWrapperList", wrapperList);
		
		
		// 得到用户未开始学习的课程
		List<CourseProgressWrapper> unstartedWrapperList = fetchNotStartedCourseList(m);
		model.addAttribute("unstartedWrapperList", unstartedWrapperList);
		
		// 取5条好友最近动态
		List<RecentActivity> activityList = fetchRecentActivities(m);
		model.addAttribute("activityList", activityList);
		
		return "/user/profile";
	}
	
	/**
	 * 得到用户未开始学习的课程
	 * @param m
	 * @return
	 */
	private List<CourseProgressWrapper> fetchNotStartedCourseList(Member m) {
		List<Integer> allCourseIds = courseService.fetchCourseIds();
		List<Integer> startedCourseIds = m.getStartedCourseIdList();
		
		// 如果开始的课程为空,则把所有课程都放到list中
		List<CourseProgressWrapper> wrapperList = new ArrayList<CourseProgressWrapper>();
		if (startedCourseIds.isEmpty()) {
			for (Integer id : allCourseIds) {
				CourseProgressWrapper wrapper = new CourseProgressWrapper();
				wrapper.setCourseId(id);
				wrapper.setCourseName(courseService.fetchCourseName(id));
				wrapper.setProgress(0);

				wrapperList.add(wrapper);
			}
		} else {
			for (Integer id : allCourseIds) {
				logger.info("测试id: {}", id);
				
				if (false == startedCourseIds.contains(id))  {
					CourseProgressWrapper wrapper = new CourseProgressWrapper();
					wrapper.setCourseId(id);
					wrapper.setCourseName(courseService.fetchCourseName(id));
					wrapper.setProgress(0);
					
					wrapperList.add(wrapper);
				}
			}
		}
		
		
		return wrapperList;
	}
	
	/**
	 * 得到用户存在学习的课程,封装成 CourseProgressWrapper对象返回
	 * @param courseIds
	 * @return
	 */
	private List<CourseProgressWrapper> fetchStartedCourseList(Member m) {
		List<Integer> courseIds = m.getStartedCourseIdList();
		List<CourseProgressWrapper> wrapperList = new ArrayList<CourseProgressWrapper>();
		
		for (Integer id : courseIds) {
			CourseProgressWrapper wrap = new CourseProgressWrapper();
			wrap.setCourseName(courseService.fetchCourseName(id));
			wrap.setProgress(accountService.fetchPercentageByCourse(m, id));
			wrap.setCourseId(id);
			
			wrapperList.add(wrap);
		}
		
		return wrapperList;
	}
	
	/**
	 * 得到最近几条朋友动态
	 * @param friendList
	 * @return
	 */
	private List<RecentActivity> fetchRecentActivities(Member m) {
		List<Member> friendList = m.getFriendList();
		List<RecentActivity> activityList = new ArrayList<RecentActivity>();

		int ix = 0;
		for ( Member friend : friendList ) {
			if (ix >= ACTIVITY_AMOUNT) {
				break;
			}
			
			Member fr = accountService.findMember(friend.getUsername());
			if (fr.getRecentActivity().size() > 0) {
				activityList.add(fr.getRecentActivity().get(0));
			}
			
			++ix;
		}
		
		return activityList;
	}
}


