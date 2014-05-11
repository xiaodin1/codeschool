package cn.fh.codeschool.service;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import cn.fh.codeschool.model.Member;

@Repository
public class AccountService {
	private static final Logger logger = LoggerFactory.getLogger(AccountService.class);
	
	@PersistenceContext
	private EntityManager em;
	
	private String message;
	
	
	/**
	 * 从数据库中查找用户，并判断用户名密码是否正确
	 * @param username
	 * @param pwd
	 * @return 验证通过返回 Member 实体，否则返回null.错误信息保存在 message 成员变量中
	 */
	@Transactional
	public Member findMember(String username, String pwd) {
		List<Member> ms = em.createNamedQuery("Member.findMember", Member.class)
				.setParameter("username", username)
				.getResultList();
		
		if (0 == ms.size()) {
			this.message = "该用户不存在!";
			System.out.println(this.message);
			return null;
		}
		
		Member m = ms.get(0);
		if (!m.getPassword().equals(pwd)) {
			this.message = "密码错误";
			System.out.println(this.message);
			return null;
		}
		
		logger.info("用户 " + username + " 验证成功");
		return m;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

}
